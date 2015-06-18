package net.bychawski.guardian.guardian;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by marcin on 4/22/14.
 */
public class LoginDialogFragment extends DialogFragment {

    public LoginDialogFragment() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Dialog dialog = null;

        builder.setView(inflater.inflate(R.layout.dialog_login, null))
                .setTitle(R.string.login_dialog_title)
                .setPositiveButton(R.string.button_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editEmail = (EditText) ((AlertDialog) dialog).findViewById(R.id.edit_login_email);
                        EditText editName = (EditText) ((AlertDialog) dialog).findViewById(R.id.edit_login_name);
                        GuardianApp.getInstance().login(editEmail.getText().toString(), editName.getText().toString());
                    }
                })
                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                            getActivity().finish();
                        return false;
                    }
                });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
