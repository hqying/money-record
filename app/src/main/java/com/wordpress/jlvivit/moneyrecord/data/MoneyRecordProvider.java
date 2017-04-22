package com.wordpress.jlvivit.moneyrecord.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

public class MoneyRecordProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoneyRecordDbHelper mOpenHelper;

    static final int MONEY_RECORD = 100;
    static final int MONEY_RECORD_WITH_INOUT = 101;
    static final int MONEY_RECORD_WITH_DATE = 102;
    static final int MONEY_RECORD_WITH_INOUT_AND_DATE = 103;
    static final int MONEY_RECORD_WITH_CATEGORY = 104;

    private static final SQLiteQueryBuilder queryBuilder;
    static{
        queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MoneyRecordEntry.TABLE_NAME);
    }

    private static final String inoutSelection =
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_INOUT + " = ? ";
    private static final String dateSelection =
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_DATE + " = ? ";
    private static final String categorySelection =
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_CATEGORY + " = ? ";
    private static final String inoutAndDateSelection =
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_INOUT + " = ? AND " +
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_DATE + " = ?";

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoneyRecordContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MoneyRecordContract.PATH_MONEY_RECORD, MONEY_RECORD);
        matcher.addURI(authority, MoneyRecordContract.PATH_MONEY_RECORD + "/#", MONEY_RECORD_WITH_INOUT);
        matcher.addURI(authority, MoneyRecordContract.PATH_MONEY_RECORD + "/*", MONEY_RECORD_WITH_DATE);
        matcher.addURI(authority, MoneyRecordContract.PATH_MONEY_RECORD + "/#/*", MONEY_RECORD_WITH_INOUT_AND_DATE);
        //matcher.addURI(authority, MoneyRecordContract.PATH_MONEY_RECORD + "/#/*", MONEY_RECORD_WITH_CATEGORY);
        return matcher;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MONEY_RECORD:
                return MoneyRecordEntry.CONTENT_ITEM_TYPE;
            case MONEY_RECORD_WITH_INOUT:
                return MoneyRecordEntry.CONTENT_TYPE;
            case MONEY_RECORD_WITH_DATE:
                return MoneyRecordEntry.CONTENT_TYPE;
            case MONEY_RECORD_WITH_CATEGORY:
                return MoneyRecordEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoneyRecordDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        if (match == MONEY_RECORD) {
            long _id = db.insert(MoneyRecordEntry.TABLE_NAME, null, values);
            if (_id > 0) {
                Log.v("insert", "uri: " + MoneyRecordEntry.buildMoneyRecordUri(_id));
                return MoneyRecordEntry.buildMoneyRecordUri(_id);
            } else {
                throw new android.database.SQLException("Failed to insert row into " + uri);
            }
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        if (match == MONEY_RECORD) {
            int updatedRows = db.update(MoneyRecordEntry.TABLE_NAME, values, selection, selectionArgs);
            if (updatedRows != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return updatedRows;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        if (selection == null) {
            selection = "1";    // selection == null -> delete all rows
        }
        if (match == MONEY_RECORD) {
            int deletedRows = db.delete(MoneyRecordEntry.TABLE_NAME, selection, selectionArgs);
            if (deletedRows != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return deletedRows;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);

        Cursor cursor;
        switch (match) {
            case MONEY_RECORD:
                cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MONEY_RECORD_WITH_INOUT:  // content_uri/inout (1,2)
                String[] inout = new String[] {MoneyRecordEntry.getInoutFromUri(uri)};
                cursor = queryBuilder.query(db, projection, inoutSelection, inout, null, null, sortOrder);
                break;
            case MONEY_RECORD_WITH_DATE:   // content_uri/date
                String[] date = new String[] {MoneyRecordEntry.getDateFromUri(uri)};
                cursor = queryBuilder.query(db, projection, dateSelection, date, null, null, sortOrder);
                break;
            case MONEY_RECORD_WITH_INOUT_AND_DATE:   // content_uri/inout/date
                String[] selectArgs = new String[] {MoneyRecordEntry.getInoutFromUri(uri), MoneyRecordEntry.getDateFromUriWithInout(uri)};
                cursor = queryBuilder.query(db, projection, inoutAndDateSelection, selectArgs, null, null, sortOrder);
                break;
            case MONEY_RECORD_WITH_CATEGORY:   //content_uri/inout/date/category
                String[] category = new String[] {MoneyRecordEntry.getCategoryFromUri(uri)};
                cursor = queryBuilder.query(db, projection, categorySelection, category, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


}
