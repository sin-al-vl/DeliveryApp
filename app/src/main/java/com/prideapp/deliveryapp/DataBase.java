package com.prideapp.deliveryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Александр on 01.02.2016.
 */
public class DataBase {
    private static final String DB_NAME = "deliveryappdb";
    private static final int DB_VERSION = 1;

    //table of stocks
    private static final String STOCK_TABLE = "stock";
    public static final String STOCK_COLUMN_ID = "_id";
    public static final String STOCK_COLUMN_NAME = "name";

    private static final String STOCK_TABLE_CREATE = "create table " +
            STOCK_TABLE + "(" + STOCK_COLUMN_ID + " integer primary key, " +
            STOCK_COLUMN_NAME + " text" + ");";

    //table of goods and their amount on every stock
    private static final String PRODUCT_TABLE = "phone";
    public static final String PRODUCT_COLUMN_ID = "_id";
    public static final String PRODUCT_COLUMN_NAME = "name";
    public static final String PRODUCT_COLUMN_AMOUNT = "amount";
    public static final String PRODUCT_COLUMN_STOCK = "stock";

    private static final String PRODUCT_TABLE_CREATE = "create table " +
            PRODUCT_TABLE + "(" + PRODUCT_COLUMN_ID + " integer primary key autoincrement, " +
            PRODUCT_COLUMN_NAME + " text, " + PRODUCT_COLUMN_AMOUNT + " integer, " +
            PRODUCT_COLUMN_STOCK + " integer" + ");";


    private final Context myContext;

    private DBHelper myDBHelper;
    private SQLiteDatabase mySQLiteDB;

    public DataBase(Context myContext) {
        this.myContext = myContext;
    }

    public void open(){
        myDBHelper = new DBHelper(myContext, DB_NAME, null, DB_VERSION);
        mySQLiteDB = myDBHelper.getWritableDatabase();
    }

    public void close(){
        if(myDBHelper != null)
            myDBHelper.close();
    }

    public Cursor getStockData(){
        return mySQLiteDB.query(STOCK_TABLE, null, null, null, null, null, null);
    }

    public Cursor getProductData(long stock_id){
        return mySQLiteDB.query(PRODUCT_TABLE, null, PRODUCT_COLUMN_STOCK + " = " + stock_id,
                null, null, null, null);
    }

    public void addProduct(String name, int amount, long stock_id){
        ContentValues cv = new ContentValues();
        cv.put(PRODUCT_COLUMN_NAME, name);
        cv.put(PRODUCT_COLUMN_AMOUNT, amount);
        cv.put(PRODUCT_COLUMN_STOCK, stock_id);
        mySQLiteDB.insert(PRODUCT_TABLE, null, cv);
    }

    public void delProduct(long id){
        mySQLiteDB.delete(PRODUCT_TABLE, PRODUCT_COLUMN_ID + " = " + id, null);
    }

    public void editProduct(String newProduct, int newAmount, long id){
        ContentValues cv = new ContentValues();
        cv.put(PRODUCT_COLUMN_NAME, newProduct);
        cv.put(PRODUCT_COLUMN_AMOUNT, newAmount);
        mySQLiteDB.update(PRODUCT_TABLE, cv, PRODUCT_COLUMN_ID + " = " + id, null);
    }

    public Cursor getOneProduct(long id){
        return mySQLiteDB.query(PRODUCT_TABLE, null, PRODUCT_COLUMN_ID + " = " + id,
                null,null,null,null);
    }

    private class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            ContentValues cv = new ContentValues();

            String stocks [] = new String[]{"Stock#1", "Stock#2", "Stock#3"};

            db.execSQL(STOCK_TABLE_CREATE);
            for(int i = 0; i < stocks.length; i++){
                cv.put(STOCK_COLUMN_ID, i + 1);
                cv.put(STOCK_COLUMN_NAME, stocks[i]);
                db.insert(STOCK_TABLE, null, cv);
            }

            String[] goodsOfStock1 = new String[] {"HTC One V", "Samsung Wave", "OnePlusTwo", "Sumsung Galaxy S5"};
            String[] goodsOfStock2 = new String[] {"HTC Desire", "HTC Magician", "Samsung Galaxy S6"};
            String[] goodsOfStock3 = new String[] {"OnePlusOne", "Nokia Lumia 920", "Nokia Lumia 730", "LG V10", "LG Joy"};

            db.execSQL(PRODUCT_TABLE_CREATE);
            cv.clear();
            for(int i = 0; i < goodsOfStock1.length; i++){
                cv.put(PRODUCT_COLUMN_STOCK, 1);
                cv.put(PRODUCT_COLUMN_NAME, goodsOfStock1[i]);
                cv.put(PRODUCT_COLUMN_AMOUNT, i*5 + 3);
                db.insert(PRODUCT_TABLE, null, cv);
            }

            for(int i = 0; i < goodsOfStock2.length; i++){
                cv.put(PRODUCT_COLUMN_STOCK, 2);
                cv.put(PRODUCT_COLUMN_NAME, goodsOfStock2[i]);
                cv.put(PRODUCT_COLUMN_AMOUNT, i*6 + 1);
                db.insert(PRODUCT_TABLE, null, cv);
            }

            for(int i = 0; i < goodsOfStock3.length; i++){
                cv.put(PRODUCT_COLUMN_STOCK, 3);
                cv.put(PRODUCT_COLUMN_NAME, goodsOfStock3[i]);
                cv.put(PRODUCT_COLUMN_AMOUNT, i*10 + 2);
                db.insert(PRODUCT_TABLE, null, cv);
            }
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
