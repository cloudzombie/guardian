package net.bychawski.guardian.guardian;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.bychawski.guardian.guardian.database.RemoteSource;
import net.bychawski.guardian.guardian.models.Category;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.Status;
import net.bychawski.guardian.guardian.rest_client.RESTClientManager;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventListener;
import net.bychawski.guardian.guardian.util.AppEventType;
import net.bychawski.guardian.guardian.util.JSONParser;

import java.util.UUID;

import me.dm7.barcodescanner.core.ViewFinderView;


public class ItemDetailsActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener, AppEventListener {

    public static final String EXTRA_MODE = "Mode";
    public static final String EXTRA_MODEL_ID = "modelId,";
    public static final String EXTRA_MODEL = "model";
    public static final String EXTRA_REMOTE_SOURCE = "remoteSource";

    public static enum Mode {
        NEW_ITEM,
        ITEM_DETAILS,
        ITEM_TO_BORROW;
    }

    private Mode _mode;
    private Item _item;
    private RemoteSource _remoteSource;

    private Spinner categorySpinner, statusSpinner;
    private EditText nameInput;
    private TextView ownerText, localizationText;
    private Button borrowButton, sendEmailButton;
    private LinearLayout emailButtonGroup;

    ArrayAdapter<Category> categoryAdapter;
    ArrayAdapter<Status> statusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details);

        GuardianApp.getInstance().addAppEventListener(this);

        _mode = (Mode) getIntent().getSerializableExtra(EXTRA_MODE);
        switch (_mode){
            case NEW_ITEM:
                _item = new Item( GuardianApp.getInstance().getLoggedUser().getId() );
                break;
            case ITEM_DETAILS:
                UUID itemId = UUID.fromString(getIntent().getStringExtra(EXTRA_MODEL_ID));
                _item = GuardianApp.getInstance().getItemsCollection().getItemById(itemId);
                Log.i("ITEM: ", _item.toJSON());
                break;
            case ITEM_TO_BORROW:
                _item = getIntent().getParcelableExtra(EXTRA_MODEL);
                _remoteSource = (RemoteSource) getIntent().getSerializableExtra(EXTRA_REMOTE_SOURCE);
                break;
        }

        //setup the action bar
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        switch (_mode) {
            case NEW_ITEM:
                actionBar.setTitle(R.string.title_activity_new_item);
                break;
            case ITEM_DETAILS:
                actionBar.setTitle(R.string.title_activity_item_details);
                break;
            case ITEM_TO_BORROW:
                actionBar.setTitle(R.string.title_activity_item_to_borrow);
        }

        //find all Views
        categorySpinner = (Spinner) findViewById(R.id.input_category);
        statusSpinner = (Spinner) findViewById(R.id.input_status);
        nameInput = (EditText) findViewById(R.id.input_name);
        ownerText = (TextView) findViewById(R.id.text_owner);
        localizationText = (TextView) findViewById(R.id.text_localization);
        LinearLayout qrButtonGroup = (LinearLayout) findViewById(R.id.button_qr_group);
        LinearLayout tagButtonGroup = (LinearLayout) findViewById(R.id.button_tag_group);

        borrowButton = (Button) findViewById(R.id.button_borrow);
        sendEmailButton = (Button) findViewById(R.id.button_email);
        emailButtonGroup = (LinearLayout) findViewById(R.id.button_email_group);
        Button saveOnTagButton = (Button) findViewById(R.id.button_save_on_tag);
        Button generateQRButton = (Button) findViewById(R.id.button_generate_qr);

        //buttons
        borrowButton.setOnClickListener(this);
        sendEmailButton.setOnClickListener(this);
        saveOnTagButton.setOnClickListener(this);
        generateQRButton.setOnClickListener(this);

        //Category Spinner
        categoryAdapter = new ArrayAdapter<Category>(this, R.layout.spinner_item, Category.values());
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(this);

        //Status Spinner
        statusAdapter = new ArrayAdapter<Status>(this, R.layout.spinner_item, Status.values());
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setOnItemSelectedListener(this);

        //set values
        setViewValues();

        if(_item.getOwnerId().equals( GuardianApp.getInstance().getLoggedUser().getId() )) {
            qrButtonGroup.setVisibility(View.VISIBLE);
            tagButtonGroup.setVisibility(View.VISIBLE);
        }
        else {
            nameInput.setEnabled(false);
            categorySpinner.setEnabled(false);
        }
    }

    private void setViewValues() {
        nameInput.setText(_item.getName());
        ownerText.setText(GuardianApp.getInstance().getUsersCollection().getUserById(_item.getOwnerId()).getName());
        localizationText.setText(GuardianApp.getInstance().getUsersCollection().getUserById(_item.getLocalizationId()).getName());
        categorySpinner.setSelection( categoryAdapter.getPosition( _item.getCategory() ) );
        statusSpinner.setSelection( statusAdapter.getPosition( _item.getStatus() ) );

        UUID userId = GuardianApp.getInstance().getLoggedUser().getId();
        if(!_item.getLocalizationId().equals(userId)){
            borrowButton.setVisibility(View.VISIBLE);
            statusSpinner.setEnabled(false);
        }
        else {
            borrowButton.setVisibility(View.GONE);
            statusSpinner.setEnabled(true);
        }
        if(!userId.equals(_item.getOwnerId()) || !userId.equals(_item.getLocalizationId())){
            emailButtonGroup.setVisibility(View.VISIBLE);
            if(_item.getOwnerId().equals( GuardianApp.getInstance().getLoggedUser().getId())) {
                sendEmailButton.setText(R.string.button_email_holder);
            }
            else {
                sendEmailButton.setText(R.string.button_email_owner);
            }
        }
        else {
            emailButtonGroup.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch( _mode ){
            case NEW_ITEM:
                getMenuInflater().inflate(R.menu.new_item, menu);
                break;
            case ITEM_DETAILS:
                getMenuInflater().inflate(R.menu.item_details, menu);
                break;
            case ITEM_TO_BORROW:
                getMenuInflater().inflate(R.menu.item_details, menu);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                Intent upIntent = new Intent(this, MainActivity.class);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            case R.id.action_ok :
                if( saveItem() ) finish();
                return true;
            case R.id.action_remove :
                removeItem();
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_borrow:
                _item.setLocalizationId(GuardianApp.getInstance().getLoggedUser().getId(), true);
                saveItem();
                break;
            case R.id.button_save_on_tag:
                if( saveItem() ) saveItemTotag();
                break;
            case R.id.button_generate_qr:
                saveItem(true);
                break;
            case R.id.button_email :
                sendEmail();
        }
    }

    private void sendEmail() {
        String email = null;
        if(_item.getOwnerId().equals( GuardianApp.getInstance().getLoggedUser().getId())){
            email = GuardianApp.getInstance().getUsersCollection().getUserById(_item.getLocalizationId()).getEmail();
        }
        else {
            email = GuardianApp.getInstance().getUsersCollection().getUserById(_item.getOwnerId()).getEmail();
        }
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[GUARDIAN APP] " + _item.getName());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
    }

    private void saveItemTotag() {
        Intent intent = new Intent(this, NfcScannerActivity.class);
        intent.putExtra(NfcScannerActivity.EXTRA_MODE, NfcScannerActivity.Mode.SAVE_ITEM);
        intent.putExtra(NfcScannerActivity.EXTRA_ITEM_STR, JSONParser.stringifyItemForTag(_item));
        intent.putExtra(NfcScannerActivity.EXTRA_ITEM_NAME, _item.getName());
        this.startActivity(intent);
    }

    private boolean saveItem() { return saveItem(false); }
    private boolean saveItem(boolean sendMailWithQR) {
        if( nameInput.getText().toString().length() > 0 ) {
            _item.setName(nameInput.getText().toString(), false);
            _item.setCategory(categoryAdapter.getItem(categorySpinner.getSelectedItemPosition()), false);
            _item.setStatus(statusAdapter.getItem(statusSpinner.getSelectedItemPosition()), true);
            if (_mode == Mode.NEW_ITEM) {
                GuardianApp.getInstance().getItemsCollection().addItem(_item);
            } else if (_mode == Mode.ITEM_TO_BORROW) {
                GuardianApp.getInstance().getItemsCollection().syncItemFromRemote(_item, _remoteSource);
            }
            if (GuardianApp.getInstance().getLoggedUser().getId().equals( _item.getOwnerId() )) {
                RESTClientManager.getInstance().postOrPutItem(_item, sendMailWithQR);
            }
            else {
                RESTClientManager.getInstance().putItem(_item);
            }
            return true;
        }
        else {
            GuardianApp.getInstance().displayMessage(getString(R.string.error_empty_item_name));
        }
        return false;
    }

    private void removeItem() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_dialog_title)
                .setMessage(R.string.confirm_delete_dialog_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(GuardianApp.getInstance().getItemsCollection().deleteItem(_item)) {
                            if( GuardianApp.getInstance().getLoggedUser().getId().equals(_item.getOwnerId()) ) {
                                RESTClientManager.getInstance().deleteItem(_item.getId());
                            }
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
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch(adapterView.getId()){
            case R.id.input_category:
                Category category = (Category) adapterView.getItemAtPosition(i);
                ImageView categoryImg = (ImageView) findViewById(R.id.category_icon);
                categoryImg.setImageResource(category.getIconId());
                break;
            case R.id.input_status:
                Status status = (Status) adapterView.getItemAtPosition(i);
                ImageView statusImg = (ImageView) findViewById(R.id.status_icon);
                statusImg.setImageResource(status.getIconId());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onAppEvent(AppEvent event) {
        if (event.getType() == AppEventType.ITEMS_COLLECTION_CHANGED || event.getType() == AppEventType.USERS_COLLECTION_CHANGED) {
            setViewValues();
        }
    }
}
