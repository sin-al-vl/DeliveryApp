package com.prideapp.deliveryapp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.prideapp.deliveryapp.R;
import com.prideapp.deliveryapp.Report;
import com.prideapp.deliveryapp.WorkTime;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Александр on 28.06.2016.
 */
public class NotificationService extends Service {
    private Timer timer;

    public void onCreate() {
        super.onCreate();

        notificationMayBeChanged(getApplicationContext());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("tag", "notif " + startId);

        timer = new Timer();
        NotificationTimerTask myTimerTask = new NotificationTimerTask();
        timer.schedule(myTimerTask, 1000, 60000);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }

    private static void confirmNotification(boolean wasNotificated, Context context){
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(context.getString(R.string.key_was_notificated), wasNotificated);
        editor.apply();
    }

    public static void notificationMayBeChanged(Context context){

        if (WorkTime.isWorkingDayStarts(context)
                && !WorkTime.isWorkingDayEnds(context)) {

            confirmNotification(false, context);
            Log.e("tag", "notif will be");

        }else {
            confirmNotification(true, context);
            Log.e("tag", "notif won't be");
        }
    }

    private Notification createNotification(){

        Intent reportIntent = new Intent(this, Report.class);

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0, reportIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(getBaseContext());

        builder.setContentIntent(contentIntent)
                .setTicker("Don't forget to report\nDeliveryApp")
                .setSmallIcon(R.drawable.ic_send_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white_24dp))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("DeliveryApp")
                .setContentText("Don't forget about daily reporting to your boss");

        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.DEFAULT_SOUND;

        return notification;
    }


    private class NotificationTimerTask extends TimerTask {

        private SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        @Override
        public void run() {

            if(!pref.getBoolean(getString(R.string.key_was_notificated), false)) {

                if (WorkTime.isWorkingDayEnds(getApplicationContext())) {

                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                            .notify(1, createNotification());

                    confirmNotification(true, getApplicationContext());
                }

            } else if(WorkTime.isWorkingDayStarts(getApplicationContext())) {
                confirmNotification(false, getApplicationContext());
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
