package com.marc.masterthesis.registerathome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by marc on 10.04.15.
 */


public final class LogHomeData {

    private static final String TAG = "DatabaseUtil";


    private final Context context;

    private LogHomeDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    public static abstract class LogEntry implements BaseColumns {
        public static final String TABLE_NAME = "log_entry";
        public static final String COLUMN_NAME_STATE = "status";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }


    private static final String SQL_CREATE_LOG_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + LogEntry.TABLE_NAME +
                    " (" + LogEntry._ID + " INTEGER PRIMARY KEY," +
                    LogEntry.COLUMN_NAME_STATE + " CHAR(20), " +
                    LogEntry.COLUMN_NAME_TIMESTAMP + " TIMESTAMP DEFAULT (datetime('now','localtime')));";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LogEntry.TABLE_NAME;



    public class LogHomeDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 3;
        public static final String DATABASE_NAME = "LogHome.db";

        public LogHomeDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, "Creating DataBase: " + SQL_CREATE_LOG_ENTRIES);
            db.execSQL(SQL_CREATE_LOG_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            db.execSQL(SQL_DELETE_ENTRIES);
            db.execSQL(SQL_CREATE_LOG_ENTRIES);
        }
    }

    /**
     *
     * @param context the Context within which to work
     */
    public LogHomeData(Context context) {
        this.context = context;
    }

    /**
     * Create and open the db connection
     * @return instance of the LogHomeData
     * @throws SQLException
     */
    public LogHomeData open() throws SQLException {
        mDbHelper = new LogHomeDbHelper(context);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Closes the db connection.
     */
    public void close() {
        mDbHelper.close();
    }

    /**
     * This method creates a new LogEntry
     * @param state
     * @return long
     */
    public long createLogEntry(String state) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LogEntry.COLUMN_NAME_STATE, state);
        return mDb.insert(LogEntry.TABLE_NAME, null, initialValues);
    }

    public Cursor getLogsSince(String timestamp) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + LogEntry.TABLE_NAME
                + " WHERE " + LogEntry.COLUMN_NAME_TIMESTAMP + " > ?"
                , new String[] { timestamp });
        return cursor;
    }
}



