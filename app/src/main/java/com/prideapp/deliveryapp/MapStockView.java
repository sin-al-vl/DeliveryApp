package com.prideapp.deliveryapp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Александр on 12.02.2016.
 */
public class MapStockView extends FragmentActivity {
    private DataBase db;

    public static final String STOCK_NUMBER = "groupID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_maps_itemlist);

        ListView listView = (ListView) findViewById(R.id.lvStock);

        db = new DataBase(this);
        db.open();

        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Cursor cursor = db.getProductData(getIntent().getIntExtra(STOCK_NUMBER, 0));


        int columnNameIndex = cursor.getColumnIndex(DataBase.PRODUCT_COLUMN_NAME);
        int columnAmountIndex = cursor.getColumnIndex(DataBase.PRODUCT_COLUMN_AMOUNT);
        while (cursor.moveToNext()) {
            HashMap<String, Object> m = new HashMap<>();
            m.put(DataBase.PRODUCT_COLUMN_NAME, cursor.getString(columnNameIndex));
            m.put(DataBase.PRODUCT_COLUMN_AMOUNT, cursor.getInt(columnAmountIndex));

            data.add(m);
        }

        listView.setAdapter(new SimpleAdapter(this, data, R.layout.item_product,
                new String[] { DataBase.PRODUCT_COLUMN_NAME, DataBase.PRODUCT_COLUMN_AMOUNT},
                new int[] { R.id.tvName, R.id.tvAmount }));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
