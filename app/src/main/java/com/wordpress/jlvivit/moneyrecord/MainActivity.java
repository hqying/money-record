package com.wordpress.jlvivit.moneyrecord;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    private RecordAdapter recordAdapter;
    private ListView recordDisplayListview;
    private TextView totalTextview;
    private TextView allDatesTextview;
    private Calendar calendar;
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private Spinner daySpinner;

    private ArrayAdapter<String> yearAdapter;
    private ArrayAdapter<String> monthAdapter;
    private ArrayAdapter<String> dayAdapter;

    private int state;  // income: 1; spent: 0; all: null
    private Date dateSelection;
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
        getSupportLoaderManager().initLoader(RECORD_LOADER, null, this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        state = 2;
        dateSelection = null;
        yearSelection = monthSelection = daySelection = -1;


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


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        calendar = Calendar.getInstance();

        yearSpinner = (Spinner) findViewById(R.id.year_spinner);
        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        daySpinner = (Spinner) findViewById(R.id.day_spinner);

        yearAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        String[] yearRange = new String[calendar.get(Calendar.YEAR) - 2016 + 2];
        yearRange[0] = "Year";
        for (int i = 2016, k = 1; i <= calendar.get(Calendar.YEAR); i++, k++) {
            yearRange[k] = Integer.toString(i);
        }
        yearAdapter.addAll(yearRange);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        monthAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        monthAdapter.add("Month");
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);


        dayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        dayAdapter.add("Day");
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        yearSpinner.setOnItemSelectedListener(this);
        monthSpinner.setOnItemSelectedListener(this);
        daySpinner.setOnItemSelectedListener(this);

        recordDisplayListview = (ListView) findViewById(R.id.record_display_listview);
        registerForContextMenu(recordDisplayListview);
        recordAdapter = new RecordAdapter(this, null, 0);
        recordDisplayListview.setAdapter(recordAdapter);

        updateRecordDisplay();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = (String) parent.getSelectedItem();
        switch (parent.getId()) {
            case R.id.year_spinner:
                if (!selected.equals("Year")) {
                    yearSelection = Integer.parseInt(selected);
                    int maxMonth =  ((int) calendar.get(Calendar.YEAR) == yearSelection) ?
                            calendar.get(Calendar.MONTH) + 1 : 12;

                    String[] monthRange = new String[maxMonth + 1];
                    monthRange[0] = "Month";
                    for (int i = 1; i <= maxMonth; i++) {
                        monthRange[i] = Integer.toString(i);
                    }
                    monthAdapter.clear();
                    dayAdapter.clear();
                    monthSelection = -1;
                    daySelection = -1;
                    monthAdapter.addAll(monthRange);
                    dayAdapter.add("Day");
                    monthSpinner.setSelection(0);
                    daySpinner.setSelection(0);
                }
                break;
            case R.id.month_spinner:
                if (!selected.equals("Month")) {
                    monthSelection = Integer.parseInt(selected) - 1;
                    Integer[] days31 = new Integer[] {1, 3, 5, 7, 8, 10, 12};
                    Integer[] days30 = new Integer[] {4, 6, 9, 11};
                    int maxDay;
                    if (yearSelection == calendar.get(Calendar.YEAR) &&
                            monthSelection == (int) calendar.get(Calendar.MONTH)) {
                        maxDay = calendar.get(Calendar.DAY_OF_MONTH);
                    } else {
                        if (Arrays.asList(days31).contains(monthSelection + 1)) {
                            maxDay = 31;
                        } else if (Arrays.asList(days30).contains(monthSelection + 1)) {
                            maxDay = 30;
                        } else if (monthSelection + 1 == 2) {
                            maxDay = yearSelection % 4 == 0 ? 29 : 28;
                        } else {
                            throw new UnsupportedOperationException("Wrong month selection??!!??");
                        }
                    }
                    String[] dayRange = new String[maxDay + 1];
                    dayRange[0] = "Day";
                    for (int i = 1; i <= maxDay; i++) {
                        dayRange[i] = Integer.toString(i);
                    }
                    dayAdapter.clear();
                    daySelection = -1;
                    dayAdapter.addAll(dayRange);
                    daySpinner.setSelection(0);
                }
                break;
            case R.id.day_spinner:
                if (!selected.equals("Day")) {
                    daySelection = Integer.parseInt(selected);
                }
                break;
            default:
                // TODO
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void updateState(int stateChanged) {
        if (state != stateChanged) {
            state = stateChanged;
            updateRecordDisplay();
        }
    }

//    private void onDateSelectionChanged() {
//        if (dateSelection != null) {
//            String dateFormatStr = "yyyy-MM-dd";
//            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatStr);
//            dateTextview.setText(sdf.format(dateSelection.getTime()));
//        } else {
//            dateTextview.setText(getResources().getString(R.string.homepage_date_default));
//        }
//        updateRecordDisplay();
//    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MoneyRecordEntry.COLUMN_DATE + " DESC";
        Uri uri;
        if (dateSelection == null) {
            if (state == 2) {
                uri = MoneyRecordEntry.CONTENT_URI;
            } else {
                uri = MoneyRecordEntry.buildMoneyRecordInout(state);
            }
        } else {
            if (state == 2) {
                uri = MoneyRecordEntry.buildMoneyRecordDate(dateSelection);
            } else {
                uri = MoneyRecordEntry.buildMoneyRecordInoutAndDate(state, dateSelection);
            }
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();  //TODO: Don't go back to edit/add page
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            updateState(2);
        } else if (id == R.id.nav_income) {
            updateState(1);
        } else if (id == R.id.nav_spent) {
            updateState(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

