package com.prideapp.deliveryapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

import com.prideapp.deliveryapp.Dialogs.ItemDialog;
import com.prideapp.deliveryapp.Services.NotificationService;
import com.prideapp.deliveryapp.Services.ReportService;

/**
 * Created by Александр on 03.02.2016.
 */

public class DataBaseView extends AppCompatActivity
        implements LoaderCallbacks<Cursor>, ItemDialog.onAddItemEventListener {

    private static final int CM_DELETE_ID = 0, CM_ADD_ID = 1, CM_EDIT_ID = 2;

    private DataBase db;
    private SimpleCursorTreeAdapter sctAdapter;
    private long currentItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        db = new DataBase(this);
        db.open();

        String []groupFrom ={ DataBase.STOCK_COLUMN_NAME };
        int[] groupTo = {android.R.id.text1};
        String []childFrom ={ DataBase.PRODUCT_COLUMN_NAME, DataBase.PRODUCT_COLUMN_AMOUNT};
        int[] childTo = {R.id.tvName, R.id.tvAmount};

        sctAdapter = new MyAdapter(this, null,
                android.R.layout.simple_expandable_list_item_1, groupFrom, groupTo,
                R.layout.item_product, childFrom, childTo);

        ExpandableListView elvMain = (ExpandableListView) findViewById(R.id.elvDB);
        elvMain.setAdapter(sctAdapter);

        registerForContextMenu(elvMain);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    protected void onDestroy(){
        super.onDestroy();
        db.close();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info){
        super.onCreateContextMenu(menu, v, info);

        menu.add(0, CM_ADD_ID, 0, R.string.db_activity_cm_add);
        menu.add(0, CM_EDIT_ID, 1, R.string.db_activity_cm_edit);
        menu.add(0, CM_DELETE_ID, 2, R.string.db_activity_cm_delete);

    }

    public boolean onContextItemSelected(MenuItem item){

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();


        switch (item.getItemId()) {
            case CM_DELETE_ID:

                if (ExpandableListView.getPackedPositionType(info.packedPosition) == 1) {

                    Cursor cursor = db.getOneProduct(info.id);
                    cursor.moveToFirst();
                    String name = cursor.getString(cursor.getColumnIndex(DataBase.PRODUCT_COLUMN_NAME));
                    int amount = cursor.getInt(cursor.getColumnIndex(DataBase.PRODUCT_COLUMN_AMOUNT));
                    cursor.close();

                    startService(new Intent(this, ReportService.class)
                            .putExtra(ReportService.ACTION, ReportService.DELETE_ITEM)
                            .putExtra(ReportService.OLD_ITEM_NAME, name)
                            .putExtra(ReportService.OLD_AMOUNT, amount));

                    db.delProduct(info.id);

                    getSupportLoaderManager().getLoader(0).forceLoad();

                } else
                    Toast.makeText(this, getString(R.string.db_activity_cm_error),
                            Toast.LENGTH_LONG).show();

                return true;

            case CM_ADD_ID:

                currentItemId = ExpandableListView.getPackedPositionGroup(info.packedPosition) + 1;

                ItemDialog itemDialog = new ItemDialog();
                itemDialog.show(getFragmentManager(), "ItemDialog");

                return true;

            case CM_EDIT_ID:

                if (ExpandableListView.getPackedPositionType(info.packedPosition) == 1) {

                    currentItemId = info.id;

                    Cursor cursor = db.getOneProduct(currentItemId);
                    cursor.moveToFirst();

                    Bundle bundle = new Bundle();
                    bundle.putString(ItemDialog.OLD_PRODUCT,
                            cursor.getString(cursor.getColumnIndex(DataBase.PRODUCT_COLUMN_NAME)));
                    bundle.putInt(ItemDialog.OLD_AMOUNT,
                            cursor.getInt(cursor.getColumnIndex(DataBase.PRODUCT_COLUMN_AMOUNT)));

                    cursor.close();

                    itemDialog = new ItemDialog();
                    itemDialog.setArguments(bundle);
                    itemDialog.show(getFragmentManager(), "ItemDialog");

                } else
                    Toast.makeText(this, getString(R.string.db_activity_cm_error),
                            Toast.LENGTH_LONG).show();

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public CursorLoader onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        sctAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void addItemEvent(String newProduct, int newAmount) {

        if(currentItemId != -1) {
            db.addProduct(newProduct, newAmount, currentItemId);
            currentItemId = -1;
        }

        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    public void editItemEvent(String newProduct, int newAmount) {

        if(currentItemId != -1) {
            db.editProduct(newProduct, newAmount, currentItemId);
            currentItemId = -1;
        }

        getSupportLoaderManager().getLoader(0).forceLoad();

    }

    class MyAdapter extends SimpleCursorTreeAdapter{

        public MyAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom,
                         int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            int idColumn = groupCursor.getColumnIndex(DataBase.STOCK_COLUMN_ID);
            return db.getProductData(groupCursor.getInt(idColumn));
        }
    }


    static class MyCursorLoader extends CursorLoader {
        DataBase db;

        public MyCursorLoader(Context context, DataBase db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground(){
            return db.getStockData();
        }
    }
}
