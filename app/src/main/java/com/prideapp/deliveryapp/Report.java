package com.prideapp.deliveryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.prideapp.deliveryapp.Dialogs.ReportPickerDialog;
import com.prideapp.deliveryapp.Services.ReportService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Report extends AppCompatActivity implements View.OnClickListener,
        ReportPickerDialog.onReportChooseEventListener {

    final static String MAIL_SUBJECT = "Daily report";

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setContentView(R.layout.activity_report);

        if(pref.getInt(Preferences.CURRENT_STOCK, 0) != 0) {
            getLayoutInflater().inflate(R.layout.report_leave_btn,
                    (LinearLayout) findViewById(R.id.inflation_region), true);

            Button btnLeave = (Button) findViewById(R.id.btnLeave);

            btnLeave.setOnClickListener(this);

        } else {
            getLayoutInflater().inflate(R.layout.report_stock_btns,
                    (LinearLayout) findViewById(R.id.inflation_region), true);

            Button btn1 = (Button) findViewById(R.id.btn1);
            Button btn2 = (Button) findViewById(R.id.btn2);
            Button btn3 = (Button) findViewById(R.id.btn3);

            btn1.setOnClickListener(this);
            btn2.setOnClickListener(this);
            btn3.setOnClickListener(this);
        }

        Button btnSend = (Button) findViewById(R.id.btnSendReport);
        Button btnClear = (Button) findViewById(R.id.btnClear);

        btnSend.setOnClickListener(this);
        btnClear.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn1 :
                startService(new Intent(this, ReportService.class)
                                .putExtra(ReportService.ACTION, ReportService.ENTER_STOCK)
                                .putExtra(ReportService.STOCK_NUMBER, 1));
                this.onBackPressed();
                break;

            case R.id.btn2 :
                startService(new Intent(this, ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.ENTER_STOCK)
                        .putExtra(ReportService.STOCK_NUMBER, 2));
                this.onBackPressed();
                break;

            case R.id.btn3 :
                startService(new Intent(this, ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.ENTER_STOCK)
                        .putExtra(ReportService.STOCK_NUMBER, 3));
                this.onBackPressed();
                break;

            case R.id.btnLeave :
                startService(new Intent(this, ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.LEAVE_STOCK));
                this.onBackPressed();
                break;

            case R.id.btnSendReport :

                List<String> reports = getAllReportsFileNames();

                if(reports.size() != 0) {

                    ReportPickerDialog reportPickerDialog =
                            new ReportPickerDialog();
                    reportPickerDialog.setListReports(reports);
                    reportPickerDialog.show(getSupportFragmentManager(), "ReportPickerDialog");

                } else
                    Toast.makeText(this, getString(R.string.report_activity_toast_no_info),
                            Toast.LENGTH_LONG).show();

                break;

            case R.id.btnClear :
                deleteReports(new File(getFilesDir() + ""));
                break;
        }
    }

    private List<String> getAllReportsFileNames(){

        File[] listFiles = getFilesDir().listFiles();
        List<String> listReports = new ArrayList<>(listFiles.length);

        for (File file : listFiles)
            if (file.getName().contains(ReportService.FILE_TYPE))
                listReports.add(file.getName());


        return listReports;

    }

    private void deleteReports(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                if (child.getName().contains(ReportService.FILE_TYPE))
                    child.delete();
    }

    public String readReport(String fileName){

        File file = new File(getFilesDir() + "/" + fileName);

        if(file.exists()){
            String info = "";
            try {
                FileReader fr = new FileReader(file);

                int c;
                while((c = fr.read())!=-1) {
                    info += (char)c;
                }

                fr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return info;

        } else return "";
    }

    @Override
    public void reportChooseEvent(String fileName) {

        String report = readReport(fileName);

        if(report.length() > 0) {

            String email = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_key_boss_email), "ERROR");

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, MAIL_SUBJECT);
            emailIntent.putExtra(Intent.EXTRA_TEXT, report);

            startActivity(Intent.createChooser(emailIntent
                    , getString(R.string.report_activity_send_summary)));

        } else {
            Toast.makeText(this, R.string.report_activity_toast_no_info, Toast.LENGTH_LONG).show();
            this.onBackPressed();
        }
    }
}
