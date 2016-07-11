package com.prideapp.deliveryapp;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.prideapp.deliveryapp.Dialogs.ReminderDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Reminder extends FragmentActivity implements ReminderDialog.onChangeReminderEventListener {

    private final String FILE_TYPE = "Reminder";
    private int lastReminder = -1;
    private int currentReminderPos = -1;

    private static final int CM_DELETE_ID = 2, CM_EDIT_ID = 1, CM_CHANGE_STATE_ID = 0;

    private final String ATTRIBUTE_NAME_NUMBER = "number",
            ATTRIBUTE_NAME_TEXT = "text",
            ATTRIBUTE_NAME_STATE = "state";

    private final String STATE_DONE = "DONE", STATE_NOT_DONE = "not done";

    private SimpleAdapter sAdapter;
    private ArrayList<Map<String, Object>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        data = new ArrayList<>();

        readAllRemindersFromFile();

        String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_STATE, ATTRIBUTE_NAME_NUMBER};
        int[] to = { R.id.tvReminder, R.id.tvState, R.id.tvNumber };

        sAdapter = new SimpleAdapter(this, data, R.layout.item_reminder, from, to);

        ListView lvReminder = (ListView) findViewById(R.id.lvReminder);
        lvReminder.setAdapter(sAdapter);
        registerForContextMenu(lvReminder);

    }

    private void readAllRemindersFromFile(){

        for(int i = 0; i < Integer.MAX_VALUE; i++){

            File reminderFile = new File(getFilesDir() + "/" + FILE_TYPE + i);

            if(reminderFile.exists()){

                String reminder = "";
                String state = "";

                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            openFileInput(FILE_TYPE + i)));

                    String str;

                    if(br.readLine().startsWith(STATE_DONE))
                        state = STATE_DONE;
                    else
                        state = STATE_NOT_DONE;

                    while ((str = br.readLine()) != null) {
                        reminder += str;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                addItemOnScreen(i, reminder, state);

                lastReminder = i;

            } else break;
        }
    }

    private void addItemOnScreen(int number, String text, String state){
        Map<String, Object> m = new HashMap<>();
        m.put(ATTRIBUTE_NAME_NUMBER, getString(R.string.reminder_title_start) + (number + 1));
        m.put(ATTRIBUTE_NAME_TEXT, text);
        m.put(ATTRIBUTE_NAME_STATE, state);
        data.add(m);
    }


    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, CM_CHANGE_STATE_ID, 0, R.string.reminder_activity_cm_state);
        menu.add(0, CM_EDIT_ID, 1, R.string.reminder_activity_cm_edit);
        menu.add(0, CM_DELETE_ID, 2, R.string.reminder_activity_cm_delete);
    }


    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = acmi.position;

        switch (item.getItemId()){
            case CM_CHANGE_STATE_ID :

                if(data.get(pos).get(ATTRIBUTE_NAME_STATE).equals(STATE_DONE))

                    data.get(pos).put(ATTRIBUTE_NAME_STATE, STATE_NOT_DONE);

                else
                    data.get(pos).put(ATTRIBUTE_NAME_STATE, STATE_DONE);


                writeItemInFile(pos, (String) data.get(pos).get(ATTRIBUTE_NAME_TEXT),
                        (String)data.get(pos).get(ATTRIBUTE_NAME_STATE));

                sAdapter.notifyDataSetChanged();
                return true;

            case CM_EDIT_ID :

                currentReminderPos = pos;
                callReminderDialog();

                return true;

            case CM_DELETE_ID:

                deleteItem(pos);

                sAdapter.notifyDataSetChanged();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void callReminderDialog(){
        ReminderDialog dialog = new ReminderDialog();
        dialog.show(getFragmentManager(), "ReminderDialog");
    }


    public void onButtonClick(View v) {
        lastReminder++;

        writeItemInFile(lastReminder, "", STATE_NOT_DONE);

        addItemOnScreen(lastReminder, "", STATE_NOT_DONE);

        sAdapter.notifyDataSetChanged();

        currentReminderPos = lastReminder;
        callReminderDialog();
    }

    private void writeItemInFile(int fileNumber, String str, String state){
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILE_TYPE + fileNumber, MODE_PRIVATE)));

            if(state.equals(STATE_DONE))
                bw.write(STATE_DONE + "\n");
            else
                bw.write(STATE_NOT_DONE + "\n");
            bw.write(str);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(int pos){

        deleteItemFromScreen(pos);

        deleteItemFromFile(pos);

        lastReminder--;
    }

    private void deleteItemFromScreen(int pos){

        for(int i = pos; i < lastReminder; i++){

            data.set(i, data.get(i + 1));
            data.get(i).put(ATTRIBUTE_NAME_NUMBER, getString(R.string.reminder_title_start) + (i + 1));

        }
        data.remove(lastReminder);
    }

    private void deleteItemFromFile(int fileNumber) {

        for(int i = fileNumber; i <= lastReminder; i++) {

            File file = new File(getFilesDir() + "/" + FILE_TYPE + i);

            if (file.exists())
                file.delete();
        }

        for(int i = fileNumber; i < lastReminder; i++){

            writeItemInFile(i, (String) data.get(i).get(ATTRIBUTE_NAME_TEXT),
                    (String) data.get(i).get(ATTRIBUTE_NAME_STATE));

        }
    }

    @Override
    public void dialogLoadedEvent(EditText reminder) {
        reminder.setText((String)data.get(currentReminderPos)
                .get(ATTRIBUTE_NAME_TEXT));
    }

    @Override
    public void changeReminderEvent(String reminder) {

        data.get(currentReminderPos).put(ATTRIBUTE_NAME_TEXT, reminder);

        writeItemInFile(currentReminderPos, (String) data.get(currentReminderPos).get(ATTRIBUTE_NAME_TEXT),
                (String) data.get(currentReminderPos).get(ATTRIBUTE_NAME_STATE));

        sAdapter.notifyDataSetChanged();

        currentReminderPos = -1;
    }

}
