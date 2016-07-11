package com.prideapp.deliveryapp.Dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.prideapp.deliveryapp.R;

/**
 * Created by Александр on 26.06.2016.
 */
public class UserMarkerDialog extends DialogFragment implements View.OnClickListener {

    public interface onMarkerEventListener {
        void dialogLoadedEvent(EditText title);
        void changeMarkerEvent(String title);
        void deleteMarkerEvent();
    }

    private onMarkerEventListener markerEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            markerEventListener = (onMarkerEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    EditText editText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(getString(R.string.dialog_maps_hint));
        View v = inflater.inflate(R.layout.dialog_maps_infowindow, null);

        editText = (EditText) v.findViewById(R.id.etInfoWindow);

        Button save = (Button) v.findViewById(R.id.btnSave);
        save.setOnClickListener(this);
        Button delete = (Button) v.findViewById(R.id.btnDelete);
        delete.setOnClickListener(this);

        setCancelable(false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        markerEventListener.dialogLoadedEvent(editText);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnSave:

                editText.setError(null);

                if (editText.getText().toString().length() > 0) {

                    markerEventListener.changeMarkerEvent(editText.getText().toString());
                    dismiss();

                } else
                    editText.setError(getString(R.string.dialog_login_error_empty));

                break;

            case R.id.btnDelete:

                markerEventListener.deleteMarkerEvent();
                dismiss();

                break;
        }
    }

}
