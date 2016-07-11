package com.prideapp.deliveryapp.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.prideapp.deliveryapp.WorkTime;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Александр on 12.02.2016.
 */
public class Spy extends Service{

    private Timer timer;
    private SpyTask spyTask;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("tag", "spy onStart " + startId);
        if(startId != 1) {
            return START_NOT_STICKY;
        }
        Log.e("tag", "spy onStart " + startId);

        initLocationListener();
        initLocationManager();

        timer = new Timer();
        spyTask = new SpyTask();
        timer.schedule(spyTask, 0, 60000);

        return START_REDELIVER_INTENT;
    }

    private void initLocationManager() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            stopSelf();

        } else {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60, 50, locationListener);
        }

    }


    private void initLocationListener(){

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                startService(new Intent(getBaseContext(), ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.PROVIDER_STATUS_CHANGED)
                        .putExtra(ReportService.STATUS, provider + " - STATUS: " + String.valueOf(status)));
            }

            @Override
            public void onProviderEnabled(String provider) {

                startService(new Intent(getBaseContext(), ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.PROVIDER_ENABLED));
            }

            @Override
            public void onProviderDisabled(String provider) {

                startService(new Intent(getBaseContext(), ReportService.class)
                        .putExtra(ReportService.ACTION, ReportService.PROVIDER_DISABLED));
            }

        };
    }

    private void saveLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                String location = formatLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                if (!location.equals(""))
                    startService(new Intent(getBaseContext(), ReportService.class)
                            .putExtra(ReportService.ACTION, ReportService.LOCATION_REPORT)
                            .putExtra(ReportService.LOCATION, location));
            }

        } else {
            stopSelf();
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f",
                location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(timer != null)
            timer.cancel();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            locationManager.removeUpdates(locationListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SpyTask extends TimerTask {
        private boolean spyMode;

        public SpyTask() {
            spyMode = false;
        }


        @Override
        public void run() {

            if (!spyMode
                    && WorkTime.isWorkingDayStarts(getApplicationContext())
                    && !WorkTime.isWorkingDayEnds(getApplicationContext())) {

                spyMode = true;

            } else if (spyMode && WorkTime.isWorkingDayEnds(getApplicationContext())) {

                Looper.prepare();
                spyMode = false;
            }

            if(spyMode)
                saveLocation();

        }
    }

}
