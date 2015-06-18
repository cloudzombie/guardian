package net.bychawski.guardian.guardian.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.database.ItemsCollection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

@DatabaseTable
public class Item implements Parcelable{

    public static Item fromJSON(String jsonStr) {
        Item newItem = new Item();
        try {
            JSONObject json = new JSONObject(jsonStr);

            newItem.setId             (UUID.fromString (json.getString("id")          ));
            newItem.setName                            (json.getString("name")         , false);
            newItem.setOwnerId        (UUID.fromString (json.getString("owner")       ), false);
            newItem.setTimestamp                       (json.getLong("timestamp")      , false);
            newItem.setCategory       (Category.valueOf(json.getString("category")    ), false);

            if(json.has("localization")) newItem.setLocalizationId(UUID.fromString(json.getString("localization")), false);
            else {
                Item item = GuardianApp.getInstance().getItemsCollection().getItemById(newItem.getId());
                if(item != null) {
                    newItem.setLocalizationId(item.getLocalizationId(), false);
                }
                else {
                    newItem.setLocalizationId(UUID.randomUUID(), false);
                }
            }

            if ( json.has("status") ) newItem.setStatus(Status.valueOf(json.getString("status")), true);
            else {
                Item item = GuardianApp.getInstance().getItemsCollection().getItemById(newItem.getId());
                if(item != null) {
                    newItem.setStatus(item.getStatus(), true);
                }
                else {
                    newItem.setStatus(Status.Free, true);
                }
            }


        } catch (JSONException e) {
            Log.e("Error when parsing item from JSON", e.getMessage());
            e.printStackTrace();
        }

        return newItem;
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel pc) {
            Item newItem = new Item();

            newItem.setId             (UUID.fromString( pc.readString()));
            newItem.setName           (                 pc.readString() , false);
            newItem.setOwnerId        (UUID.fromString( pc.readString()), false);
            newItem.setLocalizationId (UUID.fromString( pc.readString()), false);
            newItem.setCategory       (Category.valueOf(pc.readString()), false);
            newItem.setStatus         (Status.valueOf(  pc.readString()), false);
            newItem.setTimestamp      (                 pc.readLong()   , true);

            return newItem;
        }

        public Item[] newArray(int size) {
            return new Item[size];

        }
    };

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField(canBeNull = false)
    private UUID ownerId;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private Category category;

    @DatabaseField(canBeNull = false)
    private UUID localizationId;

    @DatabaseField(canBeNull = false)
    private Status status;

    @DatabaseField(canBeNull = false)
    private long timestamp;

    private ItemsCollection _parentCollection = null;

    Item() {

    }

    public Item(UUID ownerId){
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.name = "";
        this.category = Category.Book;
        this.localizationId = ownerId;
        this.status = Status.Free;
        this.timestamp = System.currentTimeMillis();
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id",           this.id.toString()            );
            json.put("name",         this.name                     );
            json.put("owner",        this.ownerId.toString()       );
            json.put("localization", this.localizationId.toString());
            json.put("category",     this.category.name()          );
            json.put("status",       this.status.name()            );
            json.put("timestamp",    this.timestamp                );

        } catch (JSONException e) {
            Log.e("Error when stringify Item ", e.getMessage());
            e.printStackTrace();
        }

        return json.toString();
    }

    public long updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
        return this.timestamp;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Item)) return false;
        return this.getId().equals( ((Item) other).getId() );
    }

    /////////////////////////////////////////////////////////////////////////////////
    //Getters and setters
    /////////////////////////////////////////////////////////////////////////////////
    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public UUID getLocalizationId() {
        return localizationId;
    }

    public Status getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private void setId(UUID id) {
        this.id = id;
    }
    
    public void setOwnerId(UUID ownerId, boolean notify) {
        this.ownerId = ownerId;
        if( notify ) notifyParentCollection();
    }
    
    public void setName(String name, boolean notify) {
        this.name = name;
        updateTimestamp();
        if(notify) notifyParentCollection();
    }
    
    public void setCategory(Category category, boolean notify) {
        this.category = category;
        updateTimestamp();
        if(notify) notifyParentCollection();
    }
    
    public void setLocalizationId(UUID localizationId, boolean notify) {
        this.localizationId = localizationId;
        if(notify) notifyParentCollection();
    }
    
    public void setStatus(Status status, boolean notify) {
        this.status = status;
        if(notify) notifyParentCollection();
    }
    
    public void setTimestamp(long timestamp, boolean notify) {
        this.timestamp = timestamp;
        if(notify) notifyParentCollection();
    }

    public void setParentCollection(ItemsCollection collection){
        _parentCollection = collection;
    }

    void notifyParentCollection() {
        if(_parentCollection != null) {
            _parentCollection.itemChanged(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id.toString()            );
        parcel.writeString(this.name                     );
        parcel.writeString(this.ownerId.toString()       );
        parcel.writeString(this.localizationId.toString());
        parcel.writeString(this.category.name()          );
        parcel.writeString(this.status.name()            );
        parcel.writeLong  (this.timestamp                );
    }
}
