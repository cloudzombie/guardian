package net.bychawski.guardian.guardian;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;

import net.bychawski.guardian.guardian.database.RemoteSource;
import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.util.JSONParser;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    AppSelectionsPagerAdapter mAppSelectionsPagerAdapter;
    ViewPager mViewPager;
    private PendingIntent _pendingIntent;
    private IntentFilter[] _intentFiltersArray;
    private String[][] _techListsArray;
    private NfcAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _adapter = NfcAdapter.getDefaultAdapter(this);
        setupForegroundDispatch();
        setContentView(R.layout.activity_main);

        mAppSelectionsPagerAdapter = new AppSelectionsPagerAdapter(getSupportFragmentManager(), this);
        final ActionBar actionBar = getActionBar();

        //setup the view pager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSelectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position){
                actionBar.setSelectedNavigationItem(position);
            }
        });

        //setup the action bar
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for(int i = 0; i < mAppSelectionsPagerAdapter.getCount(); i++){
            actionBar.addTab(
                actionBar.newTab()
                    .setText(mAppSelectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this)
            );
        }
        actionBar.selectTab(actionBar.getTabAt(GuardianApp.getInstance().getLastSelectedTabPosition()));

        if( GuardianApp.getInstance().getLoggedUser() == null ) {
            LoginDialogFragment loginDialog = new LoginDialogFragment();
            loginDialog.show(getSupportFragmentManager(), "LoginDialog");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        _adapter.enableForegroundDispatch(this, _pendingIntent, _intentFiltersArray, _techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GuardianApp.getInstance().setLastSelectedTabPosition(getActionBar().getSelectedNavigationIndex());
        _adapter.disableForegroundDispatch(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_new_item :
                Intent intent = new Intent(this, ItemDetailsActivity.class);
                intent.putExtra(ItemDetailsActivity.EXTRA_MODE, ItemDetailsActivity.Mode.NEW_ITEM);
                startActivity(intent);
                return true;
            case R.id.action_sync :
                GuardianApp.getInstance().getItemsCollection().syncCollectionWithServer();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        //result from qr_scan
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                String qrJsonStr = intent.getStringExtra("result");
                Pair<User, Item> userItem = JSONParser.parseUserItemfromTag(qrJsonStr);
                User owner = userItem.first;
                Item item = userItem.second;
                GuardianApp.getInstance().getUsersCollection().syncUserFromRemote(owner, RemoteSource.QR_CODE);
                Intent newIntent = new Intent(this, ItemDetailsActivity.class);
                newIntent.putExtra(ItemDetailsActivity.EXTRA_MODE, ItemDetailsActivity.Mode.ITEM_TO_BORROW);
                newIntent.putExtra(ItemDetailsActivity.EXTRA_REMOTE_SOURCE, RemoteSource.QR_CODE);
                newIntent.putExtra(ItemDetailsActivity.EXTRA_MODEL, item);
                startActivity(newIntent);
            }
        }
    }

    private void setupForegroundDispatch() {
        _pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, NfcScannerActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        _intentFiltersArray = new IntentFilter[]{ndef, };
        _techListsArray = new String[][]{new String[]{ Ndef.class.getName() }};
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    public static class AppSelectionsPagerAdapter extends FragmentPagerAdapter {
        String[] mTabNames;
        Activity parentActivity;
        GuardianApp _app;

        public AppSelectionsPagerAdapter(FragmentManager fm, Activity context) {
            super(fm);
            parentActivity = context;
            mTabNames = parentActivity.getResources().getStringArray(R.array.tab_names);
            _app = (GuardianApp) parentActivity.getApplicationContext();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0 :
                    return ItemsListFragment.newInstance(_app.getItemsCollection().getOwnedItems());
                case 1:
                    return new HomeFragment();
                case 2:
                    return ItemsListFragment.newInstance(_app.getItemsCollection().getRentedItems());
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        public CharSequence getPageTitle(int position) {
            return mTabNames[position];
        }
    }
}
