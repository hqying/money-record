package com.wordpress.jlvivit.moneyrecord;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditItemActivity extends AddItemActivity {  // TODO: okay or not so good?

    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        itemId = intent.getIntExtra(Intent.EXTRA_TEXT, 1);
        Cursor cursor = getContentResolver().query(
//                MoneyRecordEntry.buildMoneyRecordUri(itemId), null, null, null, null);
                MoneyRecordEntry.CONTENT_URI, null, MoneyRecordEntry.TABLE_NAME + "._ID = ?", new String[] {Integer.toString(itemId)}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int inout = cursor.getInt(MainActivity.COL_MONEY_RECORD_INOUT);
            int inoutCheckedId = inout == 1 ? R.id.additem_radio_income : R.id.additem_radio_spend;
            inoutRadioGroup.check(inoutCheckedId);

            String dateStr = cursor.getString(MainActivity.COL_MONEY_RECORD_DATE);
            try {
                Date date = (new SimpleDateFormat("yyyy-MM-dd")).parse(dateStr);
                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //dateView.setText(cursor.getString(MainActivity.COL_MONEY_RECORD_DATE));
            updateLabel();

            categorySpinner.setSelection(categoryAdapter.getPosition(cursor.getString(MainActivity.COL_MONEY_RECORD_CATEGORY)));
            amountView.setText(String.format("%.2f", cursor.getDouble(MainActivity.COL_MONEY_RECORD_AMOUNT)));
            noteView.setText(cursor.getString(MainActivity.COL_MONEY_RECORD_NOTE));
            cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            updateItemInDb();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateItemInDb() {
        ContentValues cv = createContentValues();
        if (cv != null) {
            getContentResolver().update(MoneyRecordEntry.CONTENT_URI, cv,
                    MoneyRecordEntry.TABLE_NAME + "._ID = ?", new String[]{Integer.toString(itemId)});
            finish();
        }

    }

}
