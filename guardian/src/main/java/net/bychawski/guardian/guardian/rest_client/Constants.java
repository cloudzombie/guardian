package net.bychawski.guardian.guardian.rest_client;

/**
 * Created by marcin on 5/16/14.
 */
public class Constants {
    public static final String CLIENT_TASK_EXTRA = "taskName";
    public static final String USER_ID_EXTRA = "userId";
    public static final String ITEM_ID_EXTRA = "itemId";
    public static final String ITEMS_ARRAY_EXTRA = "itemsArray";
    public static final String SINGLE_USER_EXTRA = "user";
    public static final String SINGLE_ITEM_EXTRA = "item";
    public static final String TEXT_INFO_EXTRA = "textInfo";

    public static final String RESPONSE_TYPE_EXTRA = "responseType";
    public static final String API_HOST = "{HOST}";
    public static final String API_ITEMS_BASE = "/items";
    public static final String API_USERS_BASE = "/users";
    public static final String API_ITEMS_OWNER_QUERY = "?owner=";
    public static final String API_ITEMS_RENTED_QUERY = "?rented=";
    public static final String API_QRCODE = "/qrcode";
    public static final String API_ITEM_SEND_MAIL_QUERY = "?mail=true";
    public static final String SEND_EMAIL_EXTRA = "sendEmail";


    public static enum ClientTask {
        GET_RELATED_ITEMS,  //Need extra "USER_ID_EXTRA"
        GET_ITEM,           //Need extra "ITEM_ID_EXTRA"
        GET_USER,           //Need extra "USER_ID_EXTRA"
        POST_ITEM,          //Need extra "SINGLE_ITEM_EXTRA"
        POST_USER           //Need extra "SINGLE_USER_EXTRA"
        ,
        PUT_ITEM, POST_OR_PUT_ITEM, DELETE_ITEM;
    }

    public static final String BROADCATS_ACTION = "net.bychawski.guardian.REST_RESPONSE";

    public static enum ResponseType {
        ITEMS_ARRAY,
        USERS_ARRAY,
        SINGLE_ITEM,
        SINGLE_USER,
        TEXT_INFO
        ;
    }
}
