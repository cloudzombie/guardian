package net.bychawski.guardian.guardian.models;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.bychawski.guardian.guardian.database.UsersCollection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

@DatabaseTable
public class User {

    public static User fromJSON(String jsonStr) {
        User newUser = new User();
        try {
            JSONObject json = new JSONObject(jsonStr);

            newUser.setId(UUID.fromString  (json.getString("id"))    );
            newUser.setName                (json.getString("name")   );
            newUser.setEmail               (json.getString("email")  );
            newUser.setTimestamp           (json.getLong("timestamp"));

        } catch (JSONException e) {
            Log.e("Error when parsing user from JSON", e.getMessage());
            e.printStackTrace();
        }

        return newUser;
    }

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private String email;

    @DatabaseField(canBeNull = false)
    private long timestamp;

    private UsersCollection _parentCollection;

    User() {
        //empty
    }

    public User(final String name, final String email) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.timestamp = System.currentTimeMillis();
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id",       this.id.toString() );
            json.put("name",     this.name          );
            json.put("email",    this.email         );
            json.put("timestamp",this.timestamp     );

        } catch (JSONException e) {
            Log.e("Error when stringify User ", e.getMessage());
            e.printStackTrace();
        }

        return json.toString();
    }

    public long updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
        return this.timestamp;
    }


    /////////////////////////////////////////////////////////////////////////////////
    //Getters and setters
    /////////////////////////////////////////////////////////////////////////////////
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
        notifyParentCollection();
    }

    public void setEmail(String email) {
        this.email = email;
        notifyParentCollection();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        notifyParentCollection();
    }

    public void setParentCollection(UsersCollection usersCollection) {
        this._parentCollection = usersCollection;
    }

    private void notifyParentCollection() {
        if(_parentCollection != null) {
            _parentCollection.userChanged(this);
        }
    }
}
