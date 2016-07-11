package com.prideapp.deliveryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.prideapp.deliveryapp.Preferences;
import com.prideapp.deliveryapp.R;

import java.util.Calendar;

/**
 * Created by Александр on 06.07.2016.
 */
public class WorkTime {

    public static boolean isWorkingDayStarts(Context context){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return isWorkingDay(context)
                && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >
                pref.getInt(context.getString(R.string.key_start_hours), Preferences.DEFAULT_START_HOURS)
                || (Calendar.getInstance().get(Calendar.MINUTE) >=
                pref.getInt(context.getString(R.string.key_start_minutes), Preferences.DEFAULT_MINUTES)
                && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) ==
                pref.getInt(context.getString(R.string.key_start_hours), Preferences.DEFAULT_START_HOURS));

    }

    private static boolean isWorkingDay(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        Object[] arr;
        try {
            arr = pref.getStringSet(context.getString(R.string.pref_key_days_picker), null).toArray();
        }catch (Exception e) {
            return false;
        }

        for (Object day : arr) {
            if (day.toString().equals(context.getResources()
                    .getStringArray(R.array.days)[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]))
                return true;
        }

        return false;
    }

    public static boolean isWorkingDayEnds(Context context){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return ( isWorkingDay(context)
                && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >
                pref.getInt(context.getString(R.string.key_end_hours), Preferences.DEFAULT_END_HOURS)
                ||  (Calendar.getInstance().get(Calendar.MINUTE) >=
                pref.getInt(context.getString(R.string.key_end_minutes), Preferences.DEFAULT_MINUTES)
                && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) ==
                pref.getInt(context.getString(R.string.key_end_hours), Preferences.DEFAULT_END_HOURS)));
    }
}
