package com.prideapp.deliveryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.prideapp.deliveryapp.Dialogs.EnterDialog;
import com.prideapp.deliveryapp.Services.ReportService;
import com.prideapp.deliveryapp.Services.Spy;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final byte MY_PERMISSIONS_REQUEST = 1;
    private Timer timer;

    Button btnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDB = (Button) findViewById(R.id.btnDB);
        btnMap = (Button) findViewById(R.id.btnMap);
        Button btnReminder = (Button) findViewById(R.id.btnReminder);
        Button btnReport = (Button) findViewById(R.id.btnReport);
        Button btnOptions = (Button) findViewById(R.id.btnOptions);
        Button btnExit = (Button) findViewById(R.id.btnExit);

        btnDB.setOnClickListener(this);
        btnMap.setOnClickListener(this);
        btnReminder.setOnClickListener(this);
        btnReport.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        setBtnMapEnable(isNetworkEnable());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);

        }


        timer = new Timer();
        timer.schedule(new InternetCheckingTask(), 0, 5000);

        firstEnter();
    }

    private void firstEnter(){

        if(!EnterDialog.isEmailCorrect(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_boss_email), ""))){

            EnterDialog dialog = new EnterDialog();
            dialog.show(getSupportFragmentManager(), "LoginDialog");

            //if don't ask for permission
            startService(new Intent(this, Spy.class));
        }

    }

    private void setBtnMapEnable(boolean isEnable){
        if (isEnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                btnMap.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_room_black_24dp, 0, 0, 0);
            else
                btnMap.setError(null);

            btnMap.setClickable(true);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                btnMap.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_location_off_black_24dp, 0, 0, 0);
            else
                btnMap.setError("No connection");

            btnMap.setClickable(false);
        }
    }

    private boolean isNetworkEnable() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() == null)
            return false;

        return     cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(0, 1, 0, R.string.main_activity_btn_options);
        mi.setIntent(new Intent(this, Preferences.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:

                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    startService(new Intent(this, ReportService.class)
                            .putExtra(ReportService.ACTION, ReportService.PERMISSION_GRANTED));

                    startService(new Intent(this, Spy.class));

                } else {
                    startService(new Intent(this, ReportService.class)
                            .putExtra(ReportService.ACTION, ReportService.PERMISSION_DENIED));
                }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnDB :

                startActivity(new Intent(this, DataBaseView.class));
                break;

            case R.id.btnMap :

                startActivity(new Intent(this, MyMaps.class));
                break;

            case R.id.btnReminder :

                startActivity(new Intent(this, Reminder.class));
                break;

            case R.id.btnReport :

                startActivity(new Intent(this, Report.class));
                break;

            case R.id.btnOptions :

                startActivity(new Intent(this, Preferences.class));
                break;

            case R.id.btnExit :

                finish();
                break;
        }
    }

    class InternetCheckingTask extends TimerTask{

        private boolean isEnable = isNetworkEnable();

        @Override
        public void run() {

            if (isEnable != isNetworkEnable()) {

                isEnable = !isEnable;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBtnMapEnable(isEnable);
                    }
                });
            }
        }

    }
}
