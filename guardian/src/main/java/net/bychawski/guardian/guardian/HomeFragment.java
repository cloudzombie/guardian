package net.bychawski.guardian.guardian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.bychawski.guardian.guardian.models.User;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventListener;

/**
 * Created by marcin on 4/11/14.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, AppEventListener {
    private TextView textLoggedIn = null;
    private Button scanQRButton, newItemButton, scanRfidButton;

    public HomeFragment() {
        super();
        GuardianApp.getInstance().addAppEventListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        scanQRButton = (Button) rootView.findViewById(R.id.button_scan_QR);
        newItemButton = (Button) rootView.findViewById(R.id.button_new_item);
        scanRfidButton = (Button) rootView.findViewById(R.id.button_scan_rfid);
        textLoggedIn = (TextView) rootView.findViewById(R.id.text_loggedin_user);
        scanQRButton.setOnClickListener(this);
        newItemButton.setOnClickListener(this);
        scanRfidButton.setOnClickListener(this);

        User loggedUser = GuardianApp.getInstance().getLoggedUser();
        if( loggedUser != null ) {
            String userName = loggedUser.getName();
            textLoggedIn.setText(String.format(getResources().getString(R.string.logged_as), userName));
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch ( view.getId() ) {
            case R.id.button_scan_QR:
                intent = new Intent(getActivity(), BarcodeScannerActivity.class);
                getActivity().startActivityForResult(intent, 1);
                break;
            case R.id.button_new_item:
                intent = new Intent(getActivity(), ItemDetailsActivity.class);
                intent.putExtra(ItemDetailsActivity.EXTRA_MODE, ItemDetailsActivity.Mode.NEW_ITEM);
                getActivity().startActivity(intent);
                break;
            case R.id.button_scan_rfid:
                intent = new Intent(getActivity(), NfcScannerActivity.class);
                intent.putExtra(NfcScannerActivity.EXTRA_MODE, NfcScannerActivity.Mode.SCAN_ITEM);
                getActivity().startActivity(intent);
                break;
        }
    }

    @Override
    public void onAppEvent(AppEvent event) {
        switch ( event.getType() ){
            case USER_LOGGED_IN:
                String userName = GuardianApp.getInstance().getLoggedUser().getName();
                textLoggedIn.setText(String.format(getResources().getString(R.string.logged_as), userName));
                break;
        }
    }
}
