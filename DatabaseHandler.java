package com.troff.evemarketbrowser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/* When we pull market data from the CREST server, we are given a list of TypeID's and
   pull a history json and an order json from the marketdata endpoint. To keep things very
   simple just stuff these two jsons into an SQLite database and reparse them as needed.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private Context myContext;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "evemarketbrowser";

    // Contacts table name
    private static final String TABLE_MARKETDATA = "marketdata";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_REGION = "region";
    private static final String KEY_STATION = "station";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_AVGDAILY_ORDERS = "spread";
    private static final String KEY_AVGDAILY_VOLUME = "avgvolume";


    // SQL Create statements
    private static final String CREATE_MARKETDATA_TABLE = "CREATE TABLE " + TABLE_MARKETDATA + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_REGION + " INTEGER,"
            + KEY_STATION + " INTEGER,"
            + KEY_AVGDAILY_ORDERS + " INTEGER,"
            + KEY_AVGDAILY_VOLUME + " INTEGER,"
            + KEY_HISTORY + " TEXT,"
            + KEY_ORDERS + " TEXT"
            + ")";
    // SQL Create statements


    // XTOR
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;





    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MARKETDATA_TABLE);
    }

    // Upgrading Database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exists and create new table again
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKETDATA);
        db.execSQL(CREATE_MARKETDATA_TABLE);
    }

    public void eraseMarketData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKETDATA);
        db.execSQL(CREATE_MARKETDATA_TABLE);
    }

    // Get row count
    public int getMarketDataCount() {
        String query = "SELECT * FROM " + TABLE_MARKETDATA; // TODO just select _id ?
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        // return Count
        return count;
    }

    // Add new TypeID data, which is the history and marketorder jsons
    public void addTypeIDData(Long typeID, String jsonHistory, String jsonMarketOrder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, typeID);
        values.put(KEY_HISTORY, jsonHistory);
        values.put(KEY_ORDERS, jsonMarketOrder);

        db.insert(TABLE_MARKETDATA, null, values);
        db.close();
    }

    public void addHistory(Long typeID, String jsonHistory) {

        // First, calculate the daily volume
        MarketContainer cont = new MarketContainer(0, "");

        if (!cont.parseCRESTGetAveragesFromHistory(jsonHistory, 21)) {
            Log.d("troff", "DatabaseHandler:addHistory() - Failed to call parseCRESTGetAveragesFromHistory()");
        }


        // Put it in the database
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, typeID);
        values.put(KEY_HISTORY, jsonHistory);
        values.put(KEY_AVGDAILY_ORDERS, cont.historyAverageDailyOrders);
        values.put(KEY_AVGDAILY_VOLUME, cont.historyAverageDailyVolume);
        // TODO is this the best way to do this?
        int id = (int) db.insertWithOnConflict(TABLE_MARKETDATA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(TABLE_MARKETDATA, values, "_id=?", new String[] { typeID.toString() });
        }
        db.close();
    }

    public void addMarketOrder(Long typeID, String jsonMarketOrder, long region, long station, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, typeID);
        values.put(KEY_NAME, name);
        values.put(KEY_REGION, region);
        values.put(KEY_STATION, station);
        values.put(KEY_ORDERS, jsonMarketOrder);
        // TODO is this the best way to do this?
        int id = (int) db.insertWithOnConflict(TABLE_MARKETDATA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(TABLE_MARKETDATA, values, "_id=?", new String[] { typeID.toString() });
        }
        db.close();
    }


    public void logTableStatus() {
    }


    public boolean addOrdersAndHistoryToContainer(MarketContainer cont, long limitToStation) {
        String query;
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();

        query = "SELECT "
                + KEY_ORDERS + ", "
                + KEY_HISTORY
                + " FROM " + TABLE_MARKETDATA
                + " WHERE " + KEY_ID + " = " + cont.typeID;
        cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst() != true) {
            Log.d("troff", "DatabaseHandler - addOrdersAndHistoryToContainer() could not cursor.moveToFirst");
            cursor.close();
            db.close();
            return false;
        }

        if (!cont.parseCRESTMarketOrders(limitToStation, cursor.getString(0)) ||
                !cont.parseCRESTHistory(cursor.getString(1))) {
            Log.d("troff", "DatabaseHandler - addOrdersAddHistoryToContainer() failed to parse json from db");
            return false;
        }

        return true;
    }

    public Map<Long, MarketContainer> getAllDataAsMap() {
        Map<Long, MarketContainer> map = new HashMap<Long, MarketContainer>();

        String query;
        Cursor cursor;

        Log.d("troff", "------- about to access sql -------------");

        SQLiteDatabase db = this.getWritableDatabase();

        // First get just the typeids
        query = "SELECT "
                + KEY_ID + ", "
                + KEY_NAME + ", "
                + KEY_REGION + ", "
                + KEY_STATION + ", "
                + KEY_AVGDAILY_ORDERS + ", "
                + KEY_AVGDAILY_VOLUME
                + " FROM " + TABLE_MARKETDATA;
        cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst() != true) {
            Log.d("troff", "DatabaseHandler - getAllDataAsMap() could not cursor.moveToFirst");
            cursor.close();
            db.close();
            return map;
        }

        do {
            Long typeID = cursor.getLong(0);
            String name = cursor.getString(1);
            Long regionID = cursor.getLong(2);
            Long stationID = cursor.getLong(3);
            Long avgDailyOrders = cursor.getLong(4);
            Long avgDailyVolume = cursor.getLong(5);
            MarketContainer cont = new MarketContainer(typeID, name);
            cont.regionID = regionID;
            cont.stationID = stationID;
            cont.historyAverageDailyOrders = avgDailyOrders;
            cont.historyAverageDailyVolume = avgDailyVolume;
            map.put(typeID, cont);
        } while (cursor.moveToNext());

        cursor.close();

        /*
             // OLD CODE START - load order/history for calculating orders/volume,
             // we cache this now,   TODO delete
        // Now populate each entry
        // TODO Can I update the values while iterating or do I need to use entryset/setvalue?
        for (MarketContainer cont : map.values()) {
            query = "SELECT "
                    + KEY_HISTORY + ", "
                    + KEY_ORDERS
                    + " FROM " + TABLE_MARKETDATA
                    + " WHERE " + KEY_ID + " = " + cont.typeID;
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst() != true) {
                Log.d("troff", "DatabaseHandler - getAllDataAsMap() could not query typeid");
                cursor.close();
                db.close();
                return map;
            }

            String jsonHistory = cursor.getString(0);
            String jsonOrders = cursor.getString(1);

            if (!cont.parseCRESTHistory(jsonHistory)
                 || !cont.parseCRESTMarketOrders(cont.stationID, jsonOrders)) {
                String msg = "Failed to parse cached data for "
                        + cont.typeID + " [" + cont.itemName + "]";
                Toast.makeText(this.myContext, msg, Toast.LENGTH_LONG).show();
                Log.d("troff", msg);
            }
        }

        cursor.close();
         // OLD CODE END

        */


        db.close();

        Log.d("troff", "------- finished access sql -------------");


        return map;
    }



}
