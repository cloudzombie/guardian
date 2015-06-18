package net.bychawski.guardian.guardian;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.bychawski.guardian.guardian.database.RemoteSource;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.util.JSONParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Vector;


public class NfcScannerActivity extends Activity {

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_ITEM_STR = "itemStr";
    public static final String EXTRA_ITEM_NAME = "itemName";

    public static enum Mode {
        SCAN_ITEM,
        SAVE_ITEM
        ;
    }

    private Mode _mode;
    private IntentFilter[] _intentFiltersArray;
    private String[][] _techListsArray;
    private NfcAdapter _adapter;
    private PendingIntent _pendingIntent;

    private String _itemJsonStr;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _adapter = NfcAdapter.getDefaultAdapter(this);
        _mode = (Mode) getIntent().getSerializableExtra(EXTRA_MODE);
        if (_mode == null) _mode = Mode.SCAN_ITEM;

        if( getIntent().getAction() != null ) {
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                _mode = Mode.SCAN_ITEM;
                onNewIntent( getIntent() );
            }
        }

        //setup layout
        setContentView(R.layout.activity_nfc_scanner);
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(_mode == Mode.SAVE_ITEM) {
            _itemJsonStr = getIntent().getStringExtra(EXTRA_ITEM_STR);
            String itemName = getIntent().getStringExtra(EXTRA_ITEM_NAME);
            TextView savingTagText = (TextView) findViewById(R.id.saving_item_text);
            savingTagText.setText(String.format(getString(R.string.saving_item), itemName));
            savingTagText.setVisibility(View.VISIBLE);
        }

        //setup foreground dispatch system
        setupForegroundDispatch();
    }

    @Override
    public void onPause() {
        super.onPause();
        _adapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        _adapter.enableForegroundDispatch(this, _pendingIntent, _intentFiltersArray, _techListsArray);
    }

    public void onNewIntent(Intent intent) {
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);

        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Item item;
        switch(_mode) {
            case SCAN_ITEM:
                item = readItemFromTag(tag);
                if(item != null) {
                    openItemDetails(item);
                    finish();
                }
                else {
                    GuardianApp.getInstance().displayMessage(getString(R.string.wrong_tag_format));
                }
                break;
            case SAVE_ITEM:
                if(intent.getAction().equals( NfcAdapter.ACTION_NDEF_DISCOVERED )) {
                    item = readItemFromTag(tag);
                    if(item != null) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.confirm_teg_replace_title)
                                .setMessage(R.string.confirm_tag_replace_msg)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if ( saveItemOntag(tag) ) {
                                            finish();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //do nothing
                                    }
                                })
                                .show();
                        return;
                    }
                }
                if( saveItemOntag(tag) ) {
                    finish();
                }
                break;
        }
    }

    private boolean saveItemOntag(Tag tag) {
        NdefMessage message = prepareNdefItemMessage();
        try {
            Ndef ndef = Ndef.get(tag);
            if(ndef != null) {
                ndef.connect();
                if(! ndef.isWritable()) {
                    return false;
                }
                if(ndef.getMaxSize() < message.toByteArray().length ) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                GuardianApp.getInstance().displayMessage(getString(R.string.item_saved_on_tag));
                return true;

            }
            else {
                NdefFormatable format = NdefFormatable.get(tag);
                if(format != null) {
                    format.connect();
                    format.format(message);
                    GuardianApp.getInstance().displayMessage(getString(R.string.item_saved_on_tag));
                    return true;
                }
            }
        }
        catch (Exception e) {
            GuardianApp.getInstance().displayMessage(getString(R.string.error_when_saving_tag));
        }
        return false;
    }

    private NdefMessage prepareNdefItemMessage() {
        byte[] langBytes = Locale.getDefault().getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        byte[] textBytes = _itemJsonStr.getBytes(utfEncoding);
        int utfBit = 0;
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);

        return new NdefMessage(new NdefRecord[] {record});
    }

    private void openItemDetails(Item item) {
        Intent newIntent = new Intent(this, ItemDetailsActivity.class);
        newIntent.putExtra(ItemDetailsActivity.EXTRA_MODE, ItemDetailsActivity.Mode.ITEM_TO_BORROW);
        newIntent.putExtra(ItemDetailsActivity.EXTRA_MODEL, item);
        startActivity(newIntent);
    }

    private Item readItemFromTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if(ndef != null) {
            NdefMessage message = ndef.getCachedNdefMessage();
            NdefRecord record = message.getRecords()[0];
            String itemJsonTxt = null;

            if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                byte[] payload = record.getPayload();
                String textEncoding = "UTF-8";
                int languageCodeLength = payload[0] & 0063;
                try {
                    itemJsonTxt = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (itemJsonTxt != null) {
                    Pair<User, Item> userItem = JSONParser.parseUserItemfromTag(itemJsonTxt);
                    if( userItem != null) {
                        User owner = userItem.first;
                        Item item = userItem.second;
                        GuardianApp.getInstance().getUsersCollection().syncUserFromRemote(owner, RemoteSource.RFID);
                        return item;
                    }
                }
            }
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nfc_scanner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupForegroundDispatch() {
        _pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        switch (_mode) {
            case SCAN_ITEM:
                _intentFiltersArray = new IntentFilter[]{ndef, };
                _techListsArray = new String[][]{new String[]{ Ndef.class.getName() }};
                break;

            case SAVE_ITEM:
                _intentFiltersArray = new IntentFilter[]{ndef, tag, };
                _techListsArray = new String[][]{new String[]{ Ndef.class.getName(), NdefFormatable.class.getName() }};
                break;
        }
    }
}
