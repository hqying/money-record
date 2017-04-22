package com.wordpress.jlvivit.moneyrecord.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MoneyRecordContract {
    public static final String CONTENT_AUTHORITY = "com.wordpress.jlvivit.moneyrecord";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MONEY_RECORD = "money_record";



    public static final class MoneyRecordEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MONEY_RECORD).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MONEY_RECORD;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MONEY_RECORD;

        public static final String TABLE_NAME = "money_record";

        public static final String COLUMN_INOUT = "inout";  // int type, 1: income, 0: spent
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_NOTE = "note";


        public static Uri buildMoneyRecordUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoneyRecordInout(int inout) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(inout)).build();
        }

        public static Uri buildMoneyRecordDate(Date date) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
            return CONTENT_URI.buildUpon().appendPath(dateStr).build();
        }

        public static Uri buildMoneyRecordInoutAndDate(int inout, Date date) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(inout)).appendPath(dateStr).build();
        }

        public static Uri buildMoneyRecordCategory(int inout, String category) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(inout)).appendPath(category).build();
        }



        public static String getInoutFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getCategoryFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUriWithInout(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }
}
