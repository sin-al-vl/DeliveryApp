package com.prideapp.deliveryapp.Services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.prideapp.deliveryapp.Preferences;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Александр on 05.07.2016.
 */
public class ReportService extends Service{

    public static final String ACTION = "actionType";

    public static final int PERMISSION_GRANTED = 201;
    public static final int PERMISSION_DENIED = 202;
    public static final int ENTER_STOCK = 203;
    public static final int LEAVE_STOCK = 204;
    public static final int EDIT_ITEM = 205;
    public static final int ADD_ITEM = 206;
    public static final int DELETE_ITEM = 207;
    public static final int LOCATION_REPORT = 208;
    public static final int PROVIDER_ENABLED = 209;
    public static final int PROVIDER_DISABLED = 210;
    public static final int PROVIDER_STATUS_CHANGED = 211;

    public static final String NEW_ITEM_NAME = "newName";
    public static final String OLD_ITEM_NAME = "oldName";
    public static final String NEW_AMOUNT = "newAmount";
    public static final String OLD_AMOUNT = "oldAmount";
    public static final String STOCK_NUMBER = "stockNumber";
    public static final String LOCATION = "location";
    public static final String STATUS = "status";

    static public final String FILE_TYPE = "Report ";

    private ExecutorService es;

    @Override
    public void onCreate() {
        super.onCreate();

        es = Executors.newFixedThreadPool(1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("tag", intent.getIntExtra(ACTION, 0) + "");

        Reporter myRun = new Reporter(intent, startId);
        es.execute(myRun);

        return super.onStartCommand(intent, flags, startId);
    }

    public static String formatDate(Calendar calendar){
        return  calendar.get(Calendar.YEAR) + "-" +
                calendar.get(Calendar.MONTH) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static String formatTime(Calendar calendar){

        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
        String second = String.valueOf(calendar.get(Calendar.SECOND));

        if(Integer.valueOf(hour) < 10)
            hour = "0" + hour;
        if(Integer.valueOf(minute) < 10)
            minute = "0" + minute;
        if(Integer.valueOf(second) < 10)
            second = "0" + second;

        return hour + ":" + minute + ":" + second + " - ";
    }

    private void setCurrentStock(int stock){
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putInt(Preferences.CURRENT_STOCK, stock);
        editor.apply();
    }


    class Reporter implements Runnable {
        Intent intent;
        int startId;

        public Reporter(Intent intent, int startId) {
            this.intent = intent;
            this.startId = startId;
        }

        public void run() {

            switch (intent.getIntExtra(ACTION, 0)){
                case PERMISSION_GRANTED :
                    reportPermissionGranted();
                    break;

                case PERMISSION_DENIED :
                    reportPermissionDenied();
                    break;

                case ENTER_STOCK :
                    reportEnterStock(intent.getIntExtra(STOCK_NUMBER, 0));
                    break;

                case LEAVE_STOCK :
                    reportLeaveStock();
                    break;

                case EDIT_ITEM :
                    reportEditItem(intent.getStringExtra(OLD_ITEM_NAME), intent.getIntExtra(OLD_AMOUNT, 0),
                            intent.getStringExtra(NEW_ITEM_NAME), intent.getIntExtra(NEW_AMOUNT, 0));
                    break;

                case ADD_ITEM :
                    reportAddItem(intent.getStringExtra(NEW_ITEM_NAME), intent.getIntExtra(NEW_AMOUNT, 0));
                    break;

                case DELETE_ITEM :
                    reportDeleteItem(intent.getStringExtra(OLD_ITEM_NAME), intent.getIntExtra(OLD_AMOUNT, 0));
                    break;

                case LOCATION_REPORT :
                    reportLocation(intent.getStringExtra(LOCATION));
                    break;

                case PROVIDER_ENABLED :
                    reportProviderEnabled();
                    break;

                case PROVIDER_DISABLED :
                    reportProviderDisabled();
                    break;

                case PROVIDER_STATUS_CHANGED :
                    reportStatusChanged(intent.getStringExtra(STATUS));
                    break;

                default:
                    break;
            }

            stopSelf(startId);
        }

        private void reportPermissionGranted() {
            write(formatTime(Calendar.getInstance()) + "permission granted");
        }

        private void reportPermissionDenied() {
            write(formatTime(Calendar.getInstance()) + "permission denied");
        }

        private void reportEnterStock(int stockNumber){

            String infoLine = formatTime(Calendar.getInstance()) + "visit stock#" + stockNumber;
            write(infoLine);

            setCurrentStock(stockNumber);
        }

        private void reportLeaveStock(){

            String infoLine = formatTime(Calendar.getInstance()) + "leave stock#" +
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getInt(Preferences.CURRENT_STOCK, 0);
            write(infoLine);

            setCurrentStock(0);
        }


        private void reportEditItem(String oldName, int oldAmount, String newName, int newAmount){
            write(formatTime(Calendar.getInstance()) + "edit item in DB (" + oldName + "|" + oldAmount
                    + ") to (" + newName + "|" + newAmount + ")");
        }

        private void reportAddItem(String name, int amount){
            String infoLine = formatTime(Calendar.getInstance()) + "add to DB product (" + name
                    + "|" + amount + ")";
            write(infoLine);
        }

        private void reportDeleteItem(String name, int amount){
            String infoLine = formatTime(Calendar.getInstance()) + "delete from DB product (" + name
                    + "|" + amount + ")";
            write(infoLine);
        }

        private void reportLocation(String location){
            String info = formatTime(Calendar.getInstance()) + location;
            write(info);
        }

        private void reportProviderDisabled() {
            write(formatTime(Calendar.getInstance()) + "Network provider was disabled");
        }

        private void reportProviderEnabled() {
            write(formatTime(Calendar.getInstance()) + "Network provider was enabled");
        }

        private void reportStatusChanged(String status){
            write(formatTime(Calendar.getInstance()) + status);
        }

        private void write(String str){
            try {

                FileWriter fw = new FileWriter(getApplicationContext().getFilesDir() + "/" + FILE_TYPE +
                        formatDate(Calendar.getInstance()) + ".txt", true);
                fw.write(str + "\n");
                fw.flush();
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
