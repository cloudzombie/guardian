package net.bychawski.guardian.guardian.database;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventType;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by marcin on 5/15/14.
 */
public class UsersCollection {
    private static UsersCollection _instance = null;
    public static UsersCollection getInstance() {
        if(_instance == null) {
            _instance = new UsersCollection();
        }
        return _instance;
    }

    private DatabaseHelper _databaseHelper;
    private ArrayMap<UUID, User> _users = new ArrayMap<UUID, User>();

    private UsersCollection(){
        _databaseHelper = OpenHelperManager.getHelper(GuardianApp.getInstance(), DatabaseHelper.class);
        this.fetchDB();
    }

    public User getUserById(UUID userId) {
        User user = _users.get(userId);
        if (user == null) {
            user = new User("unknown", "unknown");
        }
        return user;
    }

    public User getUserByName(String name) {
        for(User user : _users.values()) {
            if(user.getName().equals(name)) return user;
        }
        return null;
    }

    public boolean addUser(User user) {
        if( isInCollection(user) ) return false;
        if( addUserDB(user) ) {
            _users.put(user.getId(), user);
            user.setParentCollection(this);
            usersCollectionChanged();
            return true;
        }
        return false;
    }

    public void userChanged(User user) {
        if( isInCollection(user) ) {
            updateUserDB(user);
            usersCollectionChanged();
        }
    }

    public void syncUserFromRemote(User remoteUser, RemoteSource source) {
        if( isInCollection(remoteUser) ) {
            User localUser = _users.get(remoteUser.getId());
            if (localUser.getTimestamp() <= remoteUser.getTimestamp() ){
                localUser.setName(remoteUser.getName());
                localUser.setEmail(remoteUser.getEmail());
                localUser.setTimestamp(remoteUser.getTimestamp());
            }
        }
        else {
            addUser(remoteUser);
        }
    }

    public boolean deleteUser(User user) {
        if (! isInCollection(user) ) return false;
        deleteUserDB(user);
        _users.remove(user.getId());
        user.setParentCollection(null);
        usersCollectionChanged();
        return true;
    }

    private void fetchDB() {
        _users.clear();
        List<User> users =  getUsersDB();

        for(User user : users) {
            _users.put(user.getId(), user);
            user.setParentCollection(this);
        }

        usersCollectionChanged();
    }

    private List<User> getUsersDB() {
        List<User> result = null;
        try {
            result = _databaseHelper.getUserDao().queryForAll();
        } catch (SQLException e) {
            Log.e("Error when getting users ", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    private boolean addUserDB(User user) {
        try {
            if( _databaseHelper.getUserDao().create(user) == 1) return true;

        } catch (SQLException e) {
            Log.e("Error when adding user: ", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void updateUserDB(User user) {
        try {
            _databaseHelper.getUserDao().update(user);
        } catch (SQLException e) {
            Log.e("Error when updating user: ", e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteUserDB(User user) {
        try {
            _databaseHelper.getUserDao().delete(user);
        } catch (SQLException e) {
            Log.e("Error when updating user: ", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isInCollection(User user) {
        return _users.containsKey(user.getId());
    }
    public boolean isInCollection(UUID userId) {
        return _users.containsKey(userId);
    }

    private void usersCollectionChanged() {
        GuardianApp.getInstance().emitAppEvent(new AppEvent(this, AppEventType.USERS_COLLECTION_CHANGED));
    }
}
