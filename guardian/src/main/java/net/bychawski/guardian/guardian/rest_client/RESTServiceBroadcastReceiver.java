package net.bychawski.guardian.guardian.rest_client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.database.ItemsCollection;
import net.bychawski.guardian.guardian.database.RemoteSource;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventType;
import net.bychawski.guardian.guardian.util.JSONParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Created by marcin on 5/16/14.
 */
public class RESTServiceBroadcastReceiver extends BroadcastReceiver {
    private RESTClientManager _restClientMannager;
    private GuardianApp _app;

    public RESTServiceBroadcastReceiver(RESTClientManager restClientManager) {
        _restClientMannager = restClientManager;
        _app = GuardianApp.getInstance();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onReceive(Context context, Intent intent) {
        switch ( (Constants.ResponseType) intent.getSerializableExtra(Constants.RESPONSE_TYPE_EXTRA) ) {
            case ITEMS_ARRAY :
               syncItems(intent.getStringExtra(Constants.ITEMS_ARRAY_EXTRA));
               _app.emitAppEvent(new AppEvent(this, AppEventType.ITEMS_ARRAY_SYNCED));
               break;
            case SINGLE_USER:
                syncUser(intent.getStringExtra(Constants.SINGLE_USER_EXTRA));
                break;
            case SINGLE_ITEM:
               syncItem(intent.getStringExtra(Constants.SINGLE_ITEM_EXTRA));
            case TEXT_INFO:
                //_app.displayMessage( intent.getStringExtra(Constants.TEXT_INFO_EXTRA) );
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void syncItems(String itemsArrayStr) {
        List<Item> items = JSONParser.parseItemsFromJsonArray(itemsArrayStr);

        syncItemsCollection(items);
    }

    private void syncItem(String itemStr) {
        if (itemStr.length() > 0) {
            Item item = Item.fromJSON(itemStr);
            ArrayList<Item> items = new ArrayList<Item>();
            items.add(item);

            syncItemsCollection(items);
        }
    }

    private void syncItemsCollection(Collection<Item> items){
        Set<UUID> unknowUsersIds = new TreeSet<UUID>();
        ItemsCollection itemsCollection = _app.getItemsCollection();

        for(Item item : items) {
            if(! _app.getUsersCollection().isInCollection(item.getLocalizationId()) ){
                unknowUsersIds.add(item.getLocalizationId());
            }
            if(! _app.getUsersCollection().isInCollection(item.getOwnerId()) ){
                unknowUsersIds.add(item.getOwnerId());
            }

            itemsCollection.syncItemFromRemote(item, RemoteSource.SERVER);
        }
        _restClientMannager.getUsersByIds(unknowUsersIds);
    }

    public void syncUser(String userStr) {
        if(userStr.length() > 0) {
            _app.getUsersCollection().syncUserFromRemote(User.fromJSON(userStr), RemoteSource.SERVER);
        }
    }
}
