package net.bychawski.guardian.guardian.database;

import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.rest_client.RESTClientManager;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventListener;
import net.bychawski.guardian.guardian.util.AppEventType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by marcin on 5/15/14.
 */
public class ItemsCollection implements AppEventListener{
    private static ItemsCollection instance = null;

    public static ItemsCollection getInstance() {
        if(instance == null) {
            instance = new ItemsCollection();
        }
        return instance;
    }

    /////////////////////////
    // FIELDS
    ////////////////////////
    private DatabaseHelper _databaseHelper;
    private User _currentUser = null;

    private Vector<UUID> _serverSyncedItems = new Vector<UUID>();
    private int _syncedEventCount = 0;

    private Vector<Item> _ownedItems = new Vector<Item>();
    private Vector<Item> _rentedItems = new Vector<Item>();


    private ItemsCollection() {
        _databaseHelper = OpenHelperManager.getHelper(GuardianApp.getInstance(), DatabaseHelper.class);
        GuardianApp.getInstance().addAppEventListener(this);
    }

    public boolean setCurrentUser(User currentUser) {
        _currentUser = currentUser;
        return true;
    }

    public Vector<Item> getOwnedItems() {
        return _ownedItems;
    }

    public Vector<Item> getRentedItems() {
        return _rentedItems;
    }

    public Item getItemById(UUID itemId) {
        for(Item item : _ownedItems) {
            if( item.getId().equals( itemId )) return item;
        }
        for(Item item : _rentedItems) {
            if( item.getId().equals( itemId )) return item;
        }
        return null;
    }

    public boolean addItem(Item item) {
        if( isInCollection(item) ) return false;
        if( isOwned(item) || isRented(item) ) {
            if( addItemDB(item) ) {
                item.setParentCollection(this);
                if( isOwned(item) ) _ownedItems.add( item );
                else _rentedItems.add( item );
                itemsCollectionChanged();
                return true;
            }
        }
        return false;
    }

    public void syncItemFromRemote(Item remoteItem, RemoteSource remoteSource) {
        UUID currentUserId = _currentUser.getId();

        if(! currentUserId.equals( remoteItem.getOwnerId() ) && ! currentUserId.equals( remoteItem.getLocalizationId() ) ){
            deleteItem( remoteItem );
        }
        else if( isInCollection(remoteItem) ) {
            Item localItem = getItemById(remoteItem.getId());
            if( localItem.getTimestamp() <= remoteItem.getTimestamp() ) {
                localItem.setName( remoteItem.getName(), false );
                localItem.setCategory( remoteItem.getCategory(), false );
                localItem.setTimestamp( remoteItem.getTimestamp(), false );
            }

            localItem.setLocalizationId( remoteItem.getLocalizationId(), false );
            localItem.setStatus( remoteItem.getStatus(), true);

        }
        else {
            addItem( remoteItem );
        }

        if (remoteSource == RemoteSource.SERVER) {
            _serverSyncedItems.add(remoteItem.getId());
        }
    }

    public void itemChanged(Item item) {
        if( isInCollection( item ) ) {
            if( (!isOwned(item)) && (!isRented(item)) ){
                deleteItem(item);
            }
            else {
                updateItemDB(item);
                itemsCollectionChanged();
            }
        }
    }

    public boolean deleteItem(Item item) {
        if( ! isInCollection( item ) ) return false;
        deleteItemDB(item);
        if( _ownedItems.contains(item) ) _ownedItems.remove( item );
        else _rentedItems.remove( item );
        item.setParentCollection(null);
        itemsCollectionChanged();
        return true;
    }

    private void fetchDb(){
        _ownedItems.clear();
        _ownedItems.addAll(getOwnedItemsDB());

        _rentedItems.clear();
        _rentedItems.addAll(getRentedItemsDB());

        for(Item item : _ownedItems) {
            item.setParentCollection(this);
        }
        for(Item item : _rentedItems) {
            item.setParentCollection(this);
        }

        itemsCollectionChanged();
    }

    private List<Item> getOwnedItemsDB() {
        List<Item> result = null;
        try {
            result = _databaseHelper.getItemDao().queryForEq("ownerId", _currentUser.getId());
        } catch (SQLException e) {
            Log.e("Error when getting items by owner: ", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private List<Item> getRentedItemsDB() {
        List<Item> result = null;
        try {
            UUID userId = _currentUser.getId();
            Dao<Item, UUID> itemDao = _databaseHelper.getItemDao();
            QueryBuilder<Item, UUID> queryBuilder = itemDao.queryBuilder();
            queryBuilder.where()
                    .eq("localizationId", userId)
                    .and()
                    .ne("ownerId", userId);

            result = itemDao.query(queryBuilder.prepare());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean addItemDB(Item item) {
        try {
            if( _databaseHelper.getItemDao().create(item) == 1) return true;

        } catch (SQLException e) {
            Log.e("Error when adding item: ", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void updateItemDB(Item item) {
        try {
            _databaseHelper.getItemDao().update(item);
        } catch (SQLException e) {
            Log.e("Error when updating item: ", e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteItemDB(Item item) {
        try {
            _databaseHelper.getItemDao().delete(item);
        } catch (SQLException e) {
            Log.e("Error when updating item: ", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isInCollection(Item item) {
        return _rentedItems.contains(item) || _ownedItems.contains(item);
    }

    private boolean isRented(Item item) {
        UUID userId = _currentUser.getId();
        return  ( ! item.getOwnerId().equals(userId) ) && item.getLocalizationId().equals( userId );
    }

    private boolean isOwned(Item item) {
        return item.getOwnerId().equals(_currentUser.getId());
    }

    private void itemsCollectionChanged() {
        GuardianApp.getInstance().emitAppEvent(new AppEvent(this, AppEventType.ITEMS_COLLECTION_CHANGED));
    }

    @Override
    public void onAppEvent(AppEvent event) {


        if ( event.getType() == AppEventType.USER_LOGGED_IN ) {
            setCurrentUser(event.getUser());

            if(event.getUser() != null) {
                fetchDb();
                syncCollectionWithServer();
            }
        }

        if ( event.getType() == AppEventType.ITEMS_ARRAY_SYNCED) {
            if ( ++_syncedEventCount == 2){
                syncRestOfTheItems();
            }
        }
    }

    public void syncCollectionWithServer() {
        if(_currentUser != null) {
            _serverSyncedItems.clear();
            _syncedEventCount = 0;
            GuardianApp.getInstance().getRestClientManager().getRelatedItems(_currentUser);
        }
    }

    private void syncRestOfTheItems() {
        RESTClientManager restClientManager = RESTClientManager.getInstance();
        for(Item item : _ownedItems) {
            if(! _serverSyncedItems.contains(item.getId())) {
                restClientManager.postOrPutItem(item);
            }
        }
        ArrayList<Item> itemsToRemove = new ArrayList<Item>();
        for(Item item : _rentedItems) {
            if(! _serverSyncedItems.contains(item.getId())) {
                itemsToRemove.add(item);
            }
        }
        for(Item item : itemsToRemove) {
            deleteItem(item);
        }
    }
}
