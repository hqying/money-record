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

import com.wordpress.jlvivit.moneyrecord.R;
import com.wordpress.jlvivit.moneyrecord.Utility;
import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

public class MoneyRecordProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoneyRecordDbHelper mOpenHelper;

//  C_U/inout/category?year=yyyy&month=MM&day=dd (in loader, with date)

    static final int MONEY_RECORD = 100;
    static final int MONEY_RECORD_WITH_INOUT_AND_CATEGORY = 101;

    private static final SQLiteQueryBuilder queryBuilder;
    static{
        queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MoneyRecordEntry.TABLE_NAME);
    }

    private static final String inoutSelection =
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_INOUT + " = ? ";
    private static final String categorySelection =
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_CATEGORY + " = ? ";



    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoneyRecordContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MoneyRecordContract.PATH_MONEY_RECORD, MONEY_RECORD);
        matcher.addURI(authority,
                MoneyRecordContract.PATH_MONEY_RECORD + "/*/*", MONEY_RECORD_WITH_INOUT_AND_CATEGORY);
        return matcher;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MONEY_RECORD:
                return MoneyRecordEntry.CONTENT_ITEM_TYPE;
            case MONEY_RECORD_WITH_INOUT_AND_CATEGORY:
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
            case MONEY_RECORD_WITH_INOUT_AND_CATEGORY:
                String inoutStr = MoneyRecordEntry.getInoutFromUri(uri);
                String category = MoneyRecordEntry.getCategoryFromUri(uri);
                String[] date = MoneyRecordEntry.getDateFromUri(uri);

                String inoutAndCategorySelectionStr;
                String[] inoutAndCategorySelectionArgs = new String[1];
                String dateSelectionStr;
                String[] dateSelectionArgs = new String[2];

                if (inoutStr.equals(getContext().getString(R.string.uri_all))) {
                    inoutAndCategorySelectionStr = "";
                }  else if (category.equals(getContext().getString(R.string.uri_all))) {
                    int inout = inoutStr.equals(getContext().getString(R.string.uri_inout_income)) ? 1 : 0;
                    inoutAndCategorySelectionStr = inoutSelection;
                    inoutAndCategorySelectionArgs[0] = Integer.toString(inout);
                } else {
                    inoutAndCategorySelectionStr = categorySelection;
                    inoutAndCategorySelectionArgs[0] = category;
                }

                if (date[0] == null) {
                    dateSelectionStr = "";
                } else {
                    dateSelectionStr = MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry.COLUMN_DATE + " BETWEEN ? AND ?";
                    int year = Integer.parseInt(date[0]);
                    String yearStr = date[0];
                    if (date[1] == null) {
                        dateSelectionArgs[0] = String.format("%04d-01-01", year);
                        dateSelectionArgs[1] = String.format("%04d-12-31", year);
                    } else {
                        int month = Integer.parseInt(date[1]);
                        String monthStr = month < 10 ? "0" + Integer.toString(month) : Integer.toString(month);
                        if (date[2] == null) {
                            int maxDay = Utility.getMaxDay(year, month);
                            String maxDayStr = maxDay < 10 ? "0" + Integer.toString(maxDay) : Integer.toString(maxDay);
                            dateSelectionArgs[0] = String.format("%04d-%02d-01", year, month);
                            dateSelectionArgs[1] = String.format("%04d-%02d-%02d", year, month, maxDay);
                        } else {
                            int day = Integer.parseInt(date[2]);
                            String dayStr = day < 10 ? "0" + Integer.toString(day) : Integer.toString(day);
                            dateSelectionArgs[0] = String.format("%04d-%02d-%02d", year, month, day);
                            dateSelectionArgs[1] = String.format("%04d-%02d-%02d", year, month, day);
                        }
                    }
                }

                boolean inoutAndCategoryIsNull = inoutAndCategorySelectionStr.equals("");
                boolean dateIsNull = dateSelectionStr.equals("");
                if (inoutAndCategoryIsNull && dateIsNull) {
                    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                } else if (inoutAndCategoryIsNull) {
                    cursor = queryBuilder.query(db, projection, dateSelectionStr, dateSelectionArgs, null, null, sortOrder);
                } else if (dateIsNull) {
                    cursor = queryBuilder.query(db, projection, inoutAndCategorySelectionStr,
                            inoutAndCategorySelectionArgs, null, null, sortOrder);
                } else {
                    String[] combinedSelectionArgs = new String[3];
                    combinedSelectionArgs[0] = inoutAndCategorySelectionArgs[0];
                    combinedSelectionArgs[1] = dateSelectionArgs[0];
                    combinedSelectionArgs[2] = dateSelectionArgs[1];
                    cursor = queryBuilder.query(
                            db, projection, inoutAndCategorySelectionStr + " AND " + dateSelectionStr,
                            combinedSelectionArgs, null, null, sortOrder);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


}
