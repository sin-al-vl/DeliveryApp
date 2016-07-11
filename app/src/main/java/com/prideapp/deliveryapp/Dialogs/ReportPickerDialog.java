package com.prideapp.deliveryapp.Dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.prideapp.deliveryapp.R;

import java.util.List;

/**
 * Created by Александр on 06.07.2016.
 */
public class ReportPickerDialog extends DialogFragment implements View.OnClickListener{

    public interface onReportChooseEventListener {
        void reportChooseEvent(String fileName);
    }

    private onReportChooseEventListener reportChooseEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            reportChooseEventListener = (onReportChooseEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    private List<String> listReports;
    private ListView listView;

    public void setListReports(List<String> listReports) {
        this.listReports = listReports;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(getString(R.string.dialog_report_title));
        View v = inflater.inflate(R.layout.dialog_report_picker, null);

        listView = (ListView) v.findViewById(R.id.lvReport);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, listReports);

        listView.setAdapter(adapter);

        Button btnChoose = (Button) v.findViewById(R.id.btnChoose);
        btnChoose.setOnClickListener(this);

        setCancelable(true);

        return v;
    }

    @Override
    public void onClick(View v) {
        if(listView.getCheckedItemPosition() != -1) {

            reportChooseEventListener.reportChooseEvent(listReports.get(listView.getCheckedItemPosition()));
            dismiss();
        }
    }
}
