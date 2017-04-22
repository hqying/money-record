package com.wordpress.jlvivit.moneyrecord;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecordAdapter extends CursorAdapter {



    public RecordAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.record_display_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setTag(cursor.getInt(MainActivity.COL_MONEY_RECORD_ID));
        TextView recordTextview = (TextView) view.findViewById(R.id.record_item_textview);

        int income = cursor.getInt(MainActivity.COL_MONEY_RECORD_INOUT);
        String inoutStr = income == 1 ? "Income:" : "Spent:";
        String dateStr = cursor.getString(MainActivity.COL_MONEY_RECORD_DATE);
        String category = cursor.getString(MainActivity.COL_MONEY_RECORD_CATEGORY);
        double amount = cursor.getDouble(MainActivity.COL_MONEY_RECORD_AMOUNT);

        String additemInfo = inoutStr + " " + dateStr + " " +
                        category + " " + String.format("%.2f", amount);
        recordTextview.setText(additemInfo);
    }
}
