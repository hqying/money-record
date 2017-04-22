package com.wordpress.jlvivit.moneyrecord;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordContract.MoneyRecordEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_MONEY_RECORD_ID = 0;
    static final int COL_MONEY_RECORD_INOUT = 1;
    static final int COL_MONEY_RECORD_DATE = 2;
    static final int COL_MONEY_RECORD_CATEGORY = 3;
    static final int COL_MONEY_RECORD_AMOUNT = 4;
    static final int COL_MONEY_RECORD_NOTE = 5;
    private static final int RECORD_LOADER = 0;
    private static final String[] RECORD_COLUMNS = {
            MoneyRecordEntry.TABLE_NAME + "." + MoneyRecordEntry._ID, MoneyRecordEntry.COLUMN_INOUT,
            MoneyRecordEntry.COLUMN_DATE, MoneyRecordEntry.COLUMN_CATEGORY,
            MoneyRecordEntry.COLUMN_AMOUNT, MoneyRecordEntry.COLUMN_NOTE};
    private RecordAdapter recordAdapter;
    private ListView recordDisplayListview;
    private TextView dateTextview;
    private TextView totalTextview;
    private TextView allDatesTextview;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private int state;  // income: 1; spent: 0; all: null
    private Date dateSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportLoaderManager().initLoader(RECORD_LOADER, null, this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        state = 2;
        dateSelection = null;


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
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                dateSelection = calendar.getTime();
                updateOnDateSelectionChanged();
            }
        };
        dateTextview = (TextView) findViewById(R.id.date_display);
        dateTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this, dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

//        totalTextview = (TextView) findViewById(R.id.total_textview);
        allDatesTextview = (TextView) findViewById(R.id.all_dates);
        allDatesTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSelection = null;
                updateOnDateSelectionChanged();
            }
        });

        recordDisplayListview = (ListView) findViewById(R.id.record_display_listview);
        registerForContextMenu(recordDisplayListview);
        recordAdapter = new RecordAdapter(this, null, 0);
        recordDisplayListview.setAdapter(recordAdapter);

        updateRecordDisplay();
    }

    private void updateState(int stateChanged) {
        if (state != stateChanged) {
            state = stateChanged;
            updateRecordDisplay();
        }
    }

    private void updateOnDateSelectionChanged() {
        if (dateSelection != null) {
            String dateFormatStr = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatStr);
            dateTextview.setText(sdf.format(dateSelection.getTime()));
        } else {
            dateTextview.setText(getResources().getString(R.string.homepage_date_default));
        }
        updateRecordDisplay();
    }


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

    /*
     * updateRecordDisplay method for CursorAdapter
     */
    private void updateRecordDisplay() {
        //TODO
        getSupportLoaderManager().restartLoader(RECORD_LOADER, null, this);
    }

    /*
     * updateRecordDisplay method for ArrayAdapter<String>
     */
//    private void updateRecordDisplay() {
//        Cursor cursor;
//        recordAdapter.clear();
//
//        Uri uri;
//        if(dateSelection == null) {
//            if (state == 2) {
//                uri = MoneyRecordEntry.CONTENT_URI;
//            } else {
//                uri = MoneyRecordEntry.buildMoneyRecordInout(state);
//            }
//        } else {
//            if (state == 2) {
//                uri = MoneyRecordEntry.buildMoneyRecordDate(dateSelection);
//            } else {
//                uri = MoneyRecordEntry.buildMoneyRecordInoutAndDate(state, dateSelection);
//            }
//        }
//
//        cursor = getContentResolver().query(uri, RECORD_COLUMNS, null, null, "DATE DESC");
//
//        double total = 0;
//
//        if (cursor == null) {
//            return;
//        }
//        if (cursor.moveToFirst()) {
//            do {
//                int _id = cursor.getInt(COL_MONEY_RECORD_ID);
//                int income = cursor.getInt(COL_MONEY_RECORD_INOUT);
//                String inoutStr = income == 1 ? "Income:" : "Spent:";
//                String dateStr = cursor.getString(COL_MONEY_RECORD_DATE);
//                String category = cursor.getString(COL_MONEY_RECORD_CATEGORY);
//                double amount = cursor.getDouble(COL_MONEY_RECORD_AMOUNT);
//                total += income == 1 ? amount : -amount;
//                String note = cursor.getString(COL_MONEY_RECORD_NOTE);
//                String additemInfo = Integer.toString(_id) + " " + inoutStr + " " + dateStr + " " +
//                        category + " " + String.format("%.2f", amount) + " " + note;
//                Log.v("see id", additemInfo);
//                recordAdapter.add(additemInfo);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//
//        totalTextview.setText(String.format("%.2f", total));
//    }

//    private void deleteRecord() {
//        getContentResolver().delete(, null, null);
//        updateRecordDisplay();
//    }


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
                        MoneyRecordEntry._ID + " = ?", new String[]{Integer.toString(itemId)});
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
