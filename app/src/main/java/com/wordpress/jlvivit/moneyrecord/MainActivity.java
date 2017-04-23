package com.wordpress.jlvivit.moneyrecord;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

import java.util.Calendar;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    private RecordAdapter recordAdapter;
    private ListView recordDisplayListview;
//    private TextView totalTextview;
    private static final Calendar calendar = Calendar.getInstance();
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private Spinner daySpinner;
    private Spinner inoutSpinner;
    private Spinner categorySpinner;

    private ArrayAdapter<String> monthAdapter;
    private ArrayAdapter<String> dayAdapter;
    private ArrayAdapter<String> categoryAdapter;

    private int inoutSelection;  // income: 1; spent: 0; all: -1
    private String categorySelection;
    private int yearSelection;
    private int monthSelection;
    private int daySelection;



    private static final int RECORD_LOADER = 0;

    private static final String[] RECORD_COLUMNS = {
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry._ID, MoneyRecordEntry.COLUMN_INOUT,
            MoneyRecordEntry.COLUMN_DATE, MoneyRecordEntry.COLUMN_CATEGORY,
            MoneyRecordEntry.COLUMN_AMOUNT, MoneyRecordEntry.COLUMN_NOTE};

    static final int COL_MONEY_RECORD_ID = 0;
    static final int COL_MONEY_RECORD_INOUT = 1;
    static final int COL_MONEY_RECORD_DATE = 2;
    static final int COL_MONEY_RECORD_CATEGORY = 3;
    static final int COL_MONEY_RECORD_AMOUNT = 4;
    static final int COL_MONEY_RECORD_NOTE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inoutSelection = -1;
        categorySelection = null;
        yearSelection = monthSelection = daySelection = -1;

        getSupportLoaderManager().initLoader(RECORD_LOADER, null, this);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);

                // TODO: Might change add item from activity to dialog.... sometime
//                LayoutInflater inflater = LayoutInflater.from(context);
//                View dialogView = inflater.inflate(R.layout.dialogue_add_item, null);
//                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//                dialogBuilder.setView(dialogView);
//
//                dialogBuilder.setCancelable(false)
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //TODO pass user input info after input finished
//                        // 1. content provider insert item into database
//                        // 2. back to last activity
//                    }
//                })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        });
//
//                AlertDialog dialog = dialogBuilder.create();
//                dialog.show();
            }
        });


        yearSpinner = (Spinner) findViewById(R.id.year_spinner);
        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        daySpinner = (Spinner) findViewById(R.id.day_spinner);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        String[] yearRange = new String[calendar.get(Calendar.YEAR) - 2016 + 2];
        yearRange[0] = getString(R.string.filter_spinner_default_year);
        for (int i = 2016, k = 1; i <= calendar.get(Calendar.YEAR); i++, k++) {
            yearRange[k] = Integer.toString(i);
        }
        yearAdapter.addAll(yearRange);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        monthAdapter.add(getString(R.string.filter_spinner_default_month));
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);


        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        dayAdapter.add(getString(R.string.filter_spinner_default_day));
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        yearSpinner.setOnItemSelectedListener(this);
        monthSpinner.setOnItemSelectedListener(this);
        daySpinner.setOnItemSelectedListener(this);

        inoutSpinner = (Spinner) findViewById(R.id.filter_inout_spinner);
        categorySpinner = (Spinner) findViewById(R.id.filter_category_spinner);
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.add(getString(R.string.filter_spinner_default_category));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        inoutSpinner.setOnItemSelectedListener(this);
        categorySpinner.setOnItemSelectedListener(this);

        Button clearFilterButton = (Button) findViewById(R.id.button_clear_filter);
        Button filterSetButton = (Button) findViewById(R.id.button_filter_set);
        clearFilterButton.setOnClickListener(this);
        filterSetButton.setOnClickListener(this);

        recordDisplayListview = (ListView) findViewById(R.id.record_display_listview);
        registerForContextMenu(recordDisplayListview);
        recordAdapter = new RecordAdapter(this, null, 0);
        recordDisplayListview.setAdapter(recordAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = (String) parent.getSelectedItem();
        switch (parent.getId()) {
            case R.id.year_spinner:
                resetMonthSpinner();
                resetDaySpinner();
                if (selected.equals(getString(R.string.filter_spinner_default_year))) {
                    yearSelection = -1;
                } else {
                    yearSelection = parseInt(selected);
                    int maxMonth =  ((int) calendar.get(Calendar.YEAR) == yearSelection) ?
                            calendar.get(Calendar.MONTH) + 1 : 12;
                    String[] monthRange = new String[maxMonth];
                    for (int i = 1; i <= maxMonth; i++) {
                        monthRange[i-1] = Integer.toString(i);
                    }
                    monthAdapter.addAll(monthRange);
                }
                break;

            case R.id.month_spinner:
                resetDaySpinner();
                if (selected.equals(getString(R.string.filter_spinner_default_month))) {
                    monthSelection = -1;
                } else {
                    monthSelection = parseInt(selected) - 1;
                    int maxDay = Utility.getMaxDay(yearSelection, monthSelection);
                    String[] dayRange = new String[maxDay];
                    for (int i = 1; i <= maxDay; i++) {
                        dayRange[i-1] = Integer.toString(i);
                    }
                    dayAdapter.addAll(dayRange);
                }
                break;

            case R.id.day_spinner:
                daySelection = selected.equals(getString(R.string.filter_spinner_default_day)) ?
                                -1 : Integer.parseInt(selected);
                break;


            case R.id.filter_inout_spinner:
                inoutSelection = selected.equals(getString(R.string.filter_spinner_default_inout)) ?
                                -1 : selected.equals("Income") ? 1 : 0;
                resetCategorySpinner();
                if (selected.equals("Income")) {
                    categoryAdapter.addAll(getResources().getStringArray(R.array.category_spinner_income));
                } else if (selected.equals("Spent")) {
                    categoryAdapter.addAll(getResources().getStringArray(R.array.category_spinner_spend));
                }
                break;

            case R.id.filter_category_spinner:
                categorySelection = selected.equals(
                        getString(R.string.filter_spinner_default_category)) ? null : selected;
            default:
                // TODO
        }
    }

    private void resetMonthSpinner() {
        monthAdapter.clear();
        monthSelection = -1;
        monthAdapter.add(getString(R.string.filter_spinner_default_month));
        monthSpinner.setSelection(0);
    }

    private void resetDaySpinner() {
        dayAdapter.clear();
        daySelection = -1;
        dayAdapter.add(getString(R.string.filter_spinner_default_day));
        daySpinner.setSelection(0);
    }

    private void resetCategorySpinner() {
        categoryAdapter.clear();
        categorySelection = null;
        categoryAdapter.add(getString(R.string.filter_spinner_default_category));
        categorySpinner.setSelection(0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_clear_filter:
                yearSpinner.setSelection(0);
                yearSelection = -1;
                resetMonthSpinner();
                resetDaySpinner();

                inoutSpinner.setSelection(0);
                inoutSelection = -1;
                resetCategorySpinner();

                updateRecordDisplay();
                break;
            case R.id.button_filter_set:
                updateRecordDisplay();
                break;
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MoneyRecordEntry.COLUMN_DATE + " DESC";
        Uri uri;
        String inoutStr = inoutSelection == -1 ? getString(R.string.uri_all) :
                inoutSelection == 1 ? getString(R.string.uri_inout_income) : getString(R.string.uri_inout_spend);
        String categoryStr = categorySelection == null ? getString(R.string.uri_all) : categorySelection;
        if (yearSelection == -1) {
            uri = MoneyRecordEntry.buildMoneyRecordInoutAndCategory(inoutStr, categoryStr);
        } else {
            uri = MoneyRecordEntry.buildMoneyRecordDate(
                    inoutStr, categoryStr, yearSelection, monthSelection, daySelection);
        }
        return new CursorLoader(this, uri, RECORD_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        recordAdapter.swapCursor(data);
        // TODO: restore position
//        recordDisplayListview.smoothScrollToPosition(int position, if position != ListView.INVALID_POSITION);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recordAdapter.swapCursor(null);
    }


    private void updateRecordDisplay() {
        //TODO
        getSupportLoaderManager().restartLoader(RECORD_LOADER, null, this);
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.record_display_listview) {
            getMenuInflater().inflate(R.menu.record_item, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor cursor = (Cursor) recordAdapter.getItem(info.position);
        int itemId = cursor.getInt(COL_MONEY_RECORD_ID);
        //cursor.close(); // CAN'T CLOSE CURSOR! OR IT DOESN'T AUTOMATICALLY REFRESH AFTER DELETING
        switch (item.getItemId()) {
            case R.id.item_edit:
                startActivity(new Intent(this, EditItemActivity.class).putExtra(Intent.EXTRA_TEXT, itemId));
                return true;
            case R.id.item_delete:
                getContentResolver().delete(MoneyRecordEntry.CONTENT_URI,
                        MoneyRecordEntry._ID + " = ?", new String[] {Integer.toString(itemId)});
                updateRecordDisplay();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        // TODO: Don't go back to edit or add activity
//        super.onBackPressed();
//    }
}
