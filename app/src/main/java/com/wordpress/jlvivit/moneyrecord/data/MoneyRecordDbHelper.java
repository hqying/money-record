package com.wordpress.jlvivit.moneyrecord.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

public class MoneyRecordDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "money_record.db";

    public MoneyRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE =
                "CREATE TABLE " + MoneyRecordEntry.TABLE_NAME + " (" +
                        MoneyRecordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoneyRecordEntry.COLUMN_INOUT + " INTEGER NOT NULL, " +
                        MoneyRecordEntry.COLUMN_DATE + " DATE NOT NULL, " +
                        MoneyRecordEntry.COLUMN_CATEGORY + " TEXT NOT NULL, " +
                        MoneyRecordEntry.COLUMN_AMOUNT + " REAL NOT NULL, " +
                        MoneyRecordEntry.COLUMN_NOTE + " TEXT);";
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  //TODO
        db.execSQL("DROP TABLE IF EXISTS " + MoneyRecordEntry.TABLE_NAME);
        onCreate(db);
    }


}
