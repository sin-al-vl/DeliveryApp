package com.prideapp.deliveryapp.Services;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import com.prideapp.deliveryapp.R;
import com.prideapp.deliveryapp.Services.NotificationService;

/**
 * Created by Александр on 05.07.2016.
 */
public class ServiceAutoLoader extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        //Autoloading notification service
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_key_notif), false))

            context.startService(new Intent(context, NotificationService.class));

        //Autoloading spy service
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)

            context.startService(new Intent(context, Spy.class));
    }
}
