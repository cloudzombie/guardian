package net.bychawski.guardian.guardian.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by marcin on 4/16/14.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "guardian.db";
    private static final int DATABASE_VERSION = 2;

    //DAO objects
    private Dao<User, UUID> userDao = null;
    private Dao<Item, UUID> itemDao = null;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Item.class);
        }
        catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, Item.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        }
        catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop database.", e);
            throw new RuntimeException(e);
        }
    }


    public Dao<User, UUID> getUserDao() throws SQLException{
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    public Dao<Item, UUID> getItemDao() throws SQLException {
        if (itemDao == null) {
            itemDao = getDao(Item.class);
        }
        return itemDao;
    }

    @Override
    public void close(){
        super.close();
        userDao = null;
        itemDao = null;
    }
}
