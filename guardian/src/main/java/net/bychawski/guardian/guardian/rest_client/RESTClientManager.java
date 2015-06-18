package net.bychawski.guardian.guardian.rest_client;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.database.RemoteSource;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.util.JSONParser;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Created by marcin on 5/16/14.
 */
public class RESTClientManager{
    private static RESTClientManager _instance = null;

    private GuardianApp _app;

    public static RESTClientManager getInstance() {
        if(_instance == null) {
            _instance = new RESTClientManager();
        }
        return _instance;
    }

    private RESTServiceBroadcastReceiver _broadcatReceiver = null;

    private RESTClientManager() {
        _broadcatReceiver = new RESTServiceBroadcastReceiver(this);
        _app = GuardianApp.getInstance();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCATS_ACTION);
        LocalBroadcastManager.getInstance(_app).registerReceiver(_broadcatReceiver, intentFilter);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // Public methods -- Tasks to start for service
    ////////////////////////////////////////////////////////////////////////////////////////
    public void getUsersByIds( Collection<UUID> usersIds ) {
        for(UUID id : usersIds){
            getUserById(id);
        }
    }

    private void getUserById(UUID userId) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.GET_USER);
        serviceIntent.putExtra(Constants.USER_ID_EXTRA, userId.toString());
        _app.startService(serviceIntent);
    }

    public void getRelatedItems(User loggedUser) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.GET_RELATED_ITEMS);
        serviceIntent.putExtra(Constants.USER_ID_EXTRA, loggedUser.getId().toString());
        _app.startService(serviceIntent);
    }

    public void getItemsByIds( Collection<UUID> itemsIds) {
        for(UUID id : itemsIds){
            getItemById(id);
        }
    }

    public void getItemById(UUID itemId){
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.GET_ITEM);
        serviceIntent.putExtra(Constants.ITEM_ID_EXTRA, itemId.toString());
        _app.startService(serviceIntent);
    }

    public void postItem(Item item) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.POST_ITEM);
        serviceIntent.putExtra(Constants.SINGLE_ITEM_EXTRA, item.toJSON());
        _app.startService(serviceIntent);
    }

    public void putItem(Item item) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.PUT_ITEM);
        serviceIntent.putExtra(Constants.SINGLE_ITEM_EXTRA, item.toJSON());
        serviceIntent.putExtra(Constants.ITEM_ID_EXTRA, item.getId().toString());
        _app.startService(serviceIntent);
    }

    public void postOrPutItem(Item item) {
        postOrPutItem(item, false);
    }
    public void postOrPutItem(Item item, boolean sendEmailWithQR) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.POST_OR_PUT_ITEM);
        serviceIntent.putExtra(Constants.SINGLE_ITEM_EXTRA, item.toJSON());
        serviceIntent.putExtra(Constants.SEND_EMAIL_EXTRA, sendEmailWithQR);
        serviceIntent.putExtra(Constants.ITEM_ID_EXTRA, item.getId().toString());
        _app.startService(serviceIntent);
    }

    public void deleteItem(UUID itemId) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.DELETE_ITEM);
        serviceIntent.putExtra(Constants.ITEM_ID_EXTRA, itemId.toString());
        _app.startService(serviceIntent);
    }

    public void postUser(User user) {
        Intent serviceIntent = new Intent(_app, RESTClientService.class);
        serviceIntent.putExtra(Constants.CLIENT_TASK_EXTRA, Constants.ClientTask.POST_USER);
        serviceIntent.putExtra(Constants.SINGLE_USER_EXTRA, user.toJSON());
        _app.startService(serviceIntent);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) GuardianApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
