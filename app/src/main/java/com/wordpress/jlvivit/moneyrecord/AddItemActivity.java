package com.wordpress.jlvivit.moneyrecord;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddItemActivity extends AppCompatActivity {

    protected Calendar calendar;
    protected EditText dateView;
    protected Spinner categorySpinner;
    protected DatePickerDialog.OnDateSetListener dateSetListener;
    protected RadioGroup inoutRadioGroup;
    protected EditText amountView;
    protected  EditText noteView;
    protected ArrayAdapter<CharSequence> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categorySpinner = (Spinner) findViewById(R.id.additem_spinner_category);
        categoryAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.addAll(getResources().getTextArray(R.array.category_spinner_income));  //TODO: initialize spinner based on context
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        inoutRadioGroup = (RadioGroup) findViewById(R.id.inout_radio_group);
        inoutRadioGroup.check(R.id.additem_radio_income); // TODO: set default checked based on intent mother
        inoutRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                categoryAdapter.clear();
                int spinnerStrRes = checkedId == R.id.additem_radio_income ?
                        R.array.category_spinner_income : R.array.category_spinner_spend;
                categoryAdapter.addAll(getResources().getStringArray(spinnerStrRes));
            }
        });

        calendar = Calendar.getInstance();
        dateView = (EditText) findViewById(R.id.additem_date_edittext);
        updateLabel();


        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                updateLabel();
            }
        };


        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddItemActivity.this, dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        amountView = (EditText) findViewById(R.id.additem_amount_edittext);
        noteView = (EditText) findViewById(R.id.additem_note_edittext);
    }

    protected void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateView.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.additem, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            addItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected ContentValues createContentValues() {
        ContentValues cv = new ContentValues();
        if (amountView.getText().toString().equals("")) {
            Toast.makeText(this, getString(R.string.empty_amount_warning), Toast.LENGTH_LONG).show();
            return null;
        }
        int income = inoutRadioGroup.getCheckedRadioButtonId() == R.id.additem_radio_income ? 1 : 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(calendar.getTime());
        String category = (String) categorySpinner.getSelectedItem();
        double amount = Double.parseDouble(amountView.getText().toString());
        String note = noteView.getText().toString();

        cv.put(MoneyRecordEntry.COLUMN_INOUT, income);
        cv.put(MoneyRecordEntry.COLUMN_DATE, dateStr);
        cv.put(MoneyRecordEntry.COLUMN_CATEGORY, category);
        cv.put(MoneyRecordEntry.COLUMN_AMOUNT, amount);
        cv.put(MoneyRecordEntry.COLUMN_NOTE, note.trim());

        return cv;
    }

    private void addItem() {
        ContentValues cv = createContentValues();
        if (cv != null) {
            getContentResolver().insert(MoneyRecordEntry.CONTENT_URI, cv);
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
