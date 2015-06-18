package net.bychawski.guardian.guardian;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

import net.bychawski.guardian.guardian.database.ItemsCollection;
import net.bychawski.guardian.guardian.database.UsersCollection;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.rest_client.RESTClientManager;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventListener;
import net.bychawski.guardian.guardian.util.AppEventType;

import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by marcin on 4/11/14.
 */
public class GuardianApp extends Application {
    private int mLastSelectedTabPosition = 1;

    public static GuardianApp getInstance() {
        return _application;
    }

    /////////////////////////////////
    // FIELDS
    /////////////////////////////////
    private static GuardianApp _application;

    private SharedPreferences _settings = null;
    private ItemsCollection _itemsCollection = null;
    private UsersCollection _usersCollection = null;
    private RESTClientManager _restClientManager = null;

    private Vector<AppEventListener> _appEventListeners = new Vector<AppEventListener>();
    private User _loggedUser = null;

    /////////////////////////////////
    // PUBLIC MEMBERS
    /////////////////////////////////
    @Override
    public void onCreate(){
        super.onCreate();

        _application = this;
        _usersCollection = UsersCollection.getInstance();
        _itemsCollection = ItemsCollection.getInstance();
        _restClientManager = RESTClientManager.getInstance();
        _settings = getSharedPreferences("guardianSettings", MODE_PRIVATE);

        String loggedUserIdStr = _settings.getString("_loggedUser", null);
        if(loggedUserIdStr != null) {
            _loggedUser = _usersCollection.getUserById( UUID.fromString(loggedUserIdStr) );
            userLoggedIn();
        }
    }

    public void login(String email, String name) {
        User user = _usersCollection.getUserByName(name);
        if(user == null) {
            user = new User(name, email);
            _usersCollection.addUser(user);
        }
        _loggedUser = user;
        getSettings().edit().putString("_loggedUser", _loggedUser.getId().toString()).apply();
        _restClientManager.postUser(user);

        userLoggedIn();
    }

    public void userLoggedIn() {
        emitAppEvent( new AppEvent(this, AppEventType.USER_LOGGED_IN, _loggedUser) );
    }

    public void addAppEventListener(AppEventListener listener) {
        _appEventListeners.add(listener);
    }
    public void removeAppEventListener(AppEventListener listener) {
        _appEventListeners.remove(listener);
    }

    public void emitAppEvent(AppEvent event) {
        Iterator<AppEventListener> iter = _appEventListeners.iterator();
        while(iter.hasNext()) {
            iter.next().onAppEvent(event);
        }
    }

    public void displayMessage(String message) {
        if(message.length() > 0) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /////////////////////////////////
    // GETTERS AND SETTERS
    /////////////////////////////////
    public ItemsCollection getItemsCollection() {
        return _itemsCollection;
    }

    public UsersCollection getUsersCollection() {
        return _usersCollection;
    }

    public User getLoggedUser() {
        return _loggedUser;
    }

    public SharedPreferences getSettings() {
        return _settings;
    }

    public RESTClientManager getRestClientManager() {
        return _restClientManager;
    }

    public int getLastSelectedTabPosition() {
        return mLastSelectedTabPosition;
    }
    public void setLastSelectedTabPosition(int selectedTabPosition) {
        mLastSelectedTabPosition = selectedTabPosition;
    }
}
