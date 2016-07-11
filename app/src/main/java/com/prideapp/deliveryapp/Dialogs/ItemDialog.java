package com.prideapp.deliveryapp.Dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prideapp.deliveryapp.Services.NotificationService;
import com.prideapp.deliveryapp.R;
import com.prideapp.deliveryapp.Services.ReportService;

/**
 * Created by Александр on 26.06.2016.
 */
public class ItemDialog extends DialogFragment implements View.OnClickListener {

    public interface onAddItemEventListener {
        void addItemEvent(String newProduct, int newAmount);
        void editItemEvent(String newProduct, int newAmount);
    }

    private onAddItemEventListener addEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            addEventListener = (onAddItemEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    private EditText newProduct, newAmount;
    private String oldProduct, oldAmount;
    private Boolean isEditing;

    public static final String OLD_PRODUCT = "oldProduct", OLD_AMOUNT = "oldAmount";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.dialog_db_title));
        View v = inflater.inflate(R.layout.dialog_database_save, null);

        newProduct = (EditText) v.findViewById(R.id.etNewProduct);
        newAmount = (EditText) v.findViewById(R.id.etNewAmount);

        try {
            oldProduct = getArguments().getString(OLD_PRODUCT, "");
            oldAmount = getArguments().getInt(OLD_AMOUNT, 0) + "";

            isEditing = !(newProduct.getText().toString().equals("") && oldAmount.equals("0"));

        } catch (Exception e) {
            isEditing = false;
        }

        if(isEditing) {
            newProduct.setText(oldProduct);
            newAmount.setText(oldAmount);
        }

        Button save = (Button) v.findViewById(R.id.btnSave);
        save.setOnClickListener(this);

        setCancelable(true);
        return v;
    }

    @Override
    public void onClick(View v) {
        if(isSaveDataRight()) {

            if (isEditing) {
                addEventListener.editItemEvent(newProduct.getText().toString(),
                        Integer.valueOf(newAmount.getText().toString()));

                getActivity().startService(new Intent(getActivity(), ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.EDIT_ITEM)
                        .putExtra(ReportService.OLD_ITEM_NAME, oldProduct)
                        .putExtra(ReportService.OLD_AMOUNT, oldAmount)
                        .putExtra(ReportService.NEW_ITEM_NAME, newProduct.getText().toString())
                        .putExtra(ReportService.NEW_AMOUNT, newAmount.getText().toString()));

            } else {
                addEventListener.addItemEvent(newProduct.getText().toString(),
                        Integer.valueOf(newAmount.getText().toString()));

                getActivity().startService(new Intent(getActivity(), ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.ADD_ITEM)
                        .putExtra(ReportService.NEW_ITEM_NAME, newProduct.getText().toString())
                        .putExtra(ReportService.NEW_AMOUNT, newAmount.getText().toString()));

            }

            dismiss();

        } else
            Toast.makeText(getActivity(), getString(R.string.dialog_db_error),
                    Toast.LENGTH_LONG).show();
    }

    private boolean isSaveDataRight(){

        return newProduct.getText().toString().length() > 0 &&
                newAmount.getText().toString().length() > 0 &&
                Integer.valueOf(newAmount.getText().toString()) > 0;
    }
}
