package com.example.eceandroidproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DB_Sqlite extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "CBdata.db";
    private static final String SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + RateManager.RateEntry.TABLE_NAME + " (" +
                    RateManager.RateEntry._ID + " INTEGER PRIMARY KEY," +
                    RateManager.RateEntry.COLUMN_NAME + " TEXT," +
                    RateManager.RateEntry.COLUMN_RATE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RateManager.RateEntry.TABLE_NAME;


    public DB_Sqlite(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public final class RateManager {

        private RateManager() {} // make the constructor private.
        //defines the data base table contents
        public class RateEntry implements BaseColumns {
            public static final String TABLE_NAME = "CBDate";
            public static final String COLUMN_NAME = "devise";
            public static final String COLUMN_RATE = "rate";
        }
    }
}