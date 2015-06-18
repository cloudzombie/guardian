package net.bychawski.guardian.guardian.util;

import android.util.Log;
import android.util.Pair;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.R;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marcin on 5/13/14.
 */
public class JSONParser {
    public static Pair<User, Item> parseUserItemfromTag(String jsonStr) {
        Pair<User,Item> result = null;
        try {
            JSONObject jsonItem = new JSONObject(jsonStr);
            JSONObject jsonOwner = jsonItem.getJSONObject("owner");
            jsonItem.put("owner", jsonOwner.getString("id"));

            User owner = User.fromJSON(jsonOwner.toString());
            Item item = Item.fromJSON(jsonItem.toString());

            result = new Pair<User, Item>(owner, item);
        } catch (JSONException e) {
        }

        return result;
    }

    public static List<Item> parseItemsFromJsonArray(String jsonStr) {
        ArrayList<Item> result = new ArrayList<Item>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                result.add( Item.fromJSON(jsonObject.toString()) );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String stringifyItemForTag(Item item) {
        String itemStr = item.toJSON();
        String ownerStr = GuardianApp.getInstance().getUsersCollection().getUserById(item.getOwnerId()).toJSON();
        try {
            JSONObject itemJson = new JSONObject(itemStr);
            JSONObject ownerJson = new JSONObject(ownerStr);

            itemJson.remove("localization");
            itemJson.remove("status");
            itemJson.remove("owner");

            itemJson.put("owner", ownerJson);

            return itemJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
