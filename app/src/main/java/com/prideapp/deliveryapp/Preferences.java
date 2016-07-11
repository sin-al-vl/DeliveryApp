package com.prideapp.deliveryapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TimePicker;

import com.prideapp.deliveryapp.Services.NotificationService;


public class Preferences extends PreferenceActivity {

    public static final int DEFAULT_END_HOURS = 17;
    public static final int DEFAULT_START_HOURS = 9;
    public static final int DEFAULT_MINUTES = 0;

    public static final String CURRENT_STOCK = "curStock";

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }


    public static class MyPreferenceFragment extends PreferenceFragment
    {
        SharedPreferences pref;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            //customization e-mail preference
            findPreference(getString(R.string.pref_key_boss_email))
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            preference.setSummary(newValue.toString());
                            return true;
                        }
                    });

            //customization working days preference
            findPreference(getString(R.string.pref_key_days_picker))
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {

                            NotificationService.notificationMayBeChanged(getActivity());

                            return true;
                        }
                    });

            //customization begin time preference
            findPreference(getString(R.string.pref_key_start_time_picker))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {

                            new TimePickerDialog(getActivity(), myBeginTimeCallBack,
                                    pref.getInt(getString(R.string.key_start_hours), DEFAULT_START_HOURS),
                                    pref.getInt(getString(R.string.key_start_minutes), DEFAULT_MINUTES), true).show();

                            return true;
                        }
                    });

            //customization end time preference
            findPreference(getString(R.string.pref_key_end_time_picker))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {

                            new TimePickerDialog(getActivity(), myEndTimeCallBack,
                                    pref.getInt(getString(R.string.key_end_hours), DEFAULT_END_HOURS),
                                    pref.getInt(getString(R.string.key_end_minutes), DEFAULT_MINUTES), true).show();

                            return true;
                        }
                    });

            //customization notification preference
            findPreference(getString(R.string.pref_key_notif))
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {

                            if (!((CheckBoxPreference) preference).isChecked())
                                getActivity().startService(new Intent(getActivity(), NotificationService.class));
                            else
                                getActivity().stopService(new Intent(getActivity(), NotificationService.class));


                            return true;
                        }
                    });
        }

        TimePickerDialog.OnTimeSetListener myBeginTimeCallBack = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(getString(R.string.key_start_hours), hourOfDay);
                editor.putInt(getString(R.string.key_start_minutes), minute);
                editor.apply();

                loadBeginTime();

                NotificationService.notificationMayBeChanged(getActivity());
            }
        };

        TimePickerDialog.OnTimeSetListener myEndTimeCallBack = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(getString(R.string.key_end_hours), hourOfDay);
                editor.putInt(getString(R.string.key_end_minutes), minute);
                editor.apply();

                loadEndTime();

                NotificationService.notificationMayBeChanged(getActivity());
            }
        };


        @Override
        public void onResume() {
            super.onResume();

            loadEmailPref();
            loadBeginTime();
            loadEndTime();
        }

        private void loadEmailPref(){
            String key = getString(R.string.pref_key_boss_email);

            findPreference(key).setSummary(pref.getString(key, "ERROR"));
        }

        private void loadBeginTime(){
            String time = getString(R.string.pref_title_start_time_picker) + " " +
                    pref.getInt(getString(R.string.key_start_hours), DEFAULT_START_HOURS) + ":";

            if(pref.getInt(getString(R.string.key_start_minutes), DEFAULT_MINUTES) < 10)
                time += "0" + pref.getInt(getString(R.string.key_start_minutes), DEFAULT_MINUTES);
            else
                time += pref.getInt(getString(R.string.key_start_minutes), DEFAULT_MINUTES);

            findPreference(getString(R.string.pref_key_start_time_picker)).setTitle(time);
        }

        private void loadEndTime(){
            String time = getString(R.string.pref_title_end_time_picker) + " " +
                    pref.getInt(getString(R.string.key_end_hours), DEFAULT_END_HOURS) + ":";

            if(pref.getInt(getString(R.string.key_end_minutes), DEFAULT_MINUTES) < 10)
                time += "0" + pref.getInt(getString(R.string.key_end_minutes), DEFAULT_MINUTES);
            else
                time += pref.getInt(getString(R.string.key_end_minutes), DEFAULT_MINUTES);

            findPreference(getString(R.string.pref_key_end_time_picker)).setTitle(time);
        }

    }

}
