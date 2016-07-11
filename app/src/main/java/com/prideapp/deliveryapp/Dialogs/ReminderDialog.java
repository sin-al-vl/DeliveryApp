package com.prideapp.deliveryapp.Dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.prideapp.deliveryapp.R;

/**
 * Created by Александр on 26.06.2016.
 */
public class ReminderDialog extends DialogFragment implements View.OnClickListener {

    public interface onChangeReminderEventListener {
        void dialogLoadedEvent(EditText reminder);
        void changeReminderEvent(String reminder);
    }

    private onChangeReminderEventListener changeReminderEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            changeReminderEventListener = (onChangeReminderEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    EditText editText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(getString(R.string.dialog_reminder_title));
        View v = inflater.inflate(R.layout.dialog_reminder, null);

        editText = (EditText) v.findViewById(R.id.etReminder);

        Button ok = (Button) v.findViewById(R.id.btnOK);
        ok.setOnClickListener(this);

        setCancelable(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        changeReminderEventListener.dialogLoadedEvent(editText);
    }

    @Override
    public void onClick(View v) {

        changeReminderEventListener.changeReminderEvent(editText.getText().toString());
        dismiss();
    }


}
