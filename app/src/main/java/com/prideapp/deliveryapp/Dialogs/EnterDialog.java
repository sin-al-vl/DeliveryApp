package com.prideapp.deliveryapp.Dialogs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prideapp.deliveryapp.R;

/**
 * Created by Александр on 26.06.2016.
 */
public class EnterDialog extends DialogFragment implements View.OnClickListener{

    EditText email;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(getString(R.string.dialog_login_title));
        View v = inflater.inflate(R.layout.dialog_login, null);

        email = (EditText) v.findViewById(R.id.et_email);

        Button save = (Button) v.findViewById(R.id.btnNext);
        save.setOnClickListener(this);

        setCancelable(false);

        return v;
    }

    private boolean isInputCorrect(){

        email.setError(null);

        if(isEmailCorrect(String.valueOf(email.getText())))
            return true;

        else if(TextUtils.isEmpty(email.getText()))
            email.setError(getString(R.string.dialog_login_error_empty));

        else
            email.setError(getString(R.string.dialog_login_error_invalid));

        return false;
    }

    public static boolean isEmailCorrect(String email){
        return email.contains("@");
    }

    @Override
    public void onClick(View v) {
        if(isInputCorrect()){

            SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putString(getString(R.string.pref_key_boss_email), String.valueOf(email.getText()));
            editor.apply();

            dismiss();

            Toast.makeText(getContext(), getString(R.string.dialog_login_toast_instructions), Toast.LENGTH_LONG)
                    .show();
        }
    }
}
