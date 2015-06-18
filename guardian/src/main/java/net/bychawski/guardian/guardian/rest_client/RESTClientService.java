package net.bychawski.guardian.guardian.rest_client;

import android.app.IntentService;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.bychawski.guardian.guardian.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by marcin on 5/15/14.
 */
public class RESTClientService extends IntentService {

    HttpClient _httpClient = null;

    public RESTClientService() {
        super("RESTClientService");
        _httpClient = new DefaultHttpClient();
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        switch ((Constants.ClientTask)intent.getSerializableExtra(Constants.CLIENT_TASK_EXTRA)) {
            case GET_RELATED_ITEMS:
                getRelatedItems(intent);
                break;
            case GET_USER:
                getUser(intent);
                break;
            case GET_ITEM:
                getItem(intent);
                break;
            case POST_USER:
                postUser(intent);
                break;
            case POST_ITEM:
                postItem(intent);
                break;
            case PUT_ITEM:
                putItem(intent);
                break;
            case POST_OR_PUT_ITEM:
                postOrPutItem(intent);
                break;
            case DELETE_ITEM:
                deleteItem(intent);
                break;
        }
    }

    private void deleteItem(Intent intent) {
        String itemId = intent.getStringExtra(Constants.ITEM_ID_EXTRA);

        String url = Constants.API_HOST + Constants.API_ITEMS_BASE + "/" + itemId;
        HttpDelete request = new HttpDelete(url);
        try {
            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.TEXT_INFO)
                    .putExtra(Constants.TEXT_INFO_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void postOrPutItem(Intent intent) {
        String itemId = intent.getStringExtra(Constants.ITEM_ID_EXTRA);
        boolean sendEmail = intent.getBooleanExtra(Constants.SEND_EMAIL_EXTRA, false);

        String url = new StringBuilder()
                .append(Constants.API_HOST)
                .append(Constants.API_ITEMS_BASE)
                .append("/")
                .append(itemId)
                .toString();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            if(responseStr.length() > 0) {
                putItem(intent);
            }
            else {
                postItem(intent);
            }

            if(sendEmail) {
                url = new StringBuilder()
                        .append(Constants.API_HOST)
                        .append(Constants.API_ITEMS_BASE)
                        .append("/")
                        .append(itemId)
                        .append(Constants.API_QRCODE)
                        .append(Constants.API_ITEM_SEND_MAIL_QUERY)
                        .toString();
                request = new HttpGet(url);
                _httpClient.execute(request);
                Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                        .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.TEXT_INFO)
                        .putExtra(Constants.TEXT_INFO_EXTRA, getString(R.string.email_sent));
                LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putItem(Intent intent) {
        String itemStr = intent.getStringExtra(Constants.SINGLE_ITEM_EXTRA);
        String itemId = intent.getStringExtra(Constants.ITEM_ID_EXTRA);

        String url = Constants.API_HOST + Constants.API_ITEMS_BASE + "/" + itemId;
        HttpPut request = new HttpPut(url);
        try {
            StringEntity stringEntity = new StringEntity(itemStr, "UTF-8");
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            request.setEntity(stringEntity);

            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.TEXT_INFO)
                    .putExtra(Constants.TEXT_INFO_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postItem(Intent intent) {
        String itemStr = intent.getStringExtra(Constants.SINGLE_ITEM_EXTRA);

        String url = Constants.API_HOST + Constants.API_ITEMS_BASE;
        HttpPost request = new HttpPost(url);
        try {
            StringEntity stringEntity = new StringEntity(itemStr, "UTF-8");
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            request.setEntity(stringEntity);

            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.TEXT_INFO)
                    .putExtra(Constants.TEXT_INFO_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postUser(Intent intent) {
        String userStr = intent.getStringExtra(Constants.SINGLE_USER_EXTRA);

        String url = Constants.API_HOST + Constants.API_USERS_BASE;
        HttpPost request = new HttpPost(url);
        try {
            StringEntity stringEntity = new StringEntity(userStr, "UTF-8");
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            request.setEntity(stringEntity);

            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.TEXT_INFO)
                    .putExtra(Constants.TEXT_INFO_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getItem(Intent intent) {
        String itemId = intent.getStringExtra(Constants.ITEM_ID_EXTRA);

        String url = Constants.API_HOST + Constants.API_ITEMS_BASE + "/" + itemId;
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.SINGLE_ITEM)
                    .putExtra(Constants.SINGLE_ITEM_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUser(Intent intent) {
        String userId = intent.getStringExtra(Constants.USER_ID_EXTRA);

        String url = Constants.API_HOST + Constants.API_USERS_BASE + "/" + userId;
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.SINGLE_USER)
                    .putExtra(Constants.SINGLE_USER_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRelatedItems(Intent intent) {
        String userId = intent.getStringExtra(Constants.USER_ID_EXTRA);
        String url;
        HttpGet request;

        //OWNED ITEMS
        url = Constants.API_HOST + Constants.API_ITEMS_BASE + Constants.API_ITEMS_OWNER_QUERY + userId;
        request = new HttpGet(url);
        try {
            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.ITEMS_ARRAY)
                    .putExtra(Constants.ITEMS_ARRAY_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //RENTED ITEMS
        url = Constants.API_HOST + Constants.API_ITEMS_BASE + Constants.API_ITEMS_RENTED_QUERY + userId;
        request = new HttpGet(url);
        try {
            HttpResponse response = _httpClient.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());

            Intent responseIntent = new Intent(Constants.BROADCATS_ACTION)
                    .putExtra(Constants.RESPONSE_TYPE_EXTRA, Constants.ResponseType.ITEMS_ARRAY)
                    .putExtra(Constants.ITEMS_ARRAY_EXTRA, responseStr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
