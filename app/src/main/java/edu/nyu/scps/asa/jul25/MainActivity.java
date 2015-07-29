package edu.nyu.scps.asa.jul25;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// This application searches a database of World statistics for averages to calculate averages based on country, continent or language
// To find the source code I used to create the SQLite database, look in the string.xml file

/**
 *  A list of features I haven't figured out how to implement:
 *  1. How do I keep the combobox from defaulting to the first item in the list if I don't make the first item blank?
 *  2. How do I round amounts in a cursorAdapter to 0.00?
 */

public class MainActivity extends AppCompatActivity {
    private boolean[] firstTime = {true,true}; //first time any item is selected

    String Type = null;
    String SelectBy = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // listener for all 2 views
        AdapterView.OnItemSelectedListener cbxListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getId() == (R.id.spinner2)) {
                    if (firstTime[0]) {
                        firstTime[0] = false;
                        return;
                    }
                    Type = (String) parent.getItemAtPosition(position);
                }
                if (parent.getId() == (R.id.spinner3)) {
                    if (firstTime[1]) {
                        firstTime[1] = false;
                        return;
                    }
                    SelectBy = (String) parent.getItemAtPosition(position);
                }

                GetReadableDatabaseTask getReadableDatabaseTask = new GetReadableDatabaseTask();
                getReadableDatabaseTask.execute(Type, SelectBy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };


        // category view
        Spinner spinnerType = (Spinner) findViewById(R.id.spinner2);

        String[] categories2 = {"", "GNP", "Life Expectancy", "Population", "Surface Area"};

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories2
        );

        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(arrayAdapter2);

        spinnerType.setOnItemSelectedListener(cbxListener);


        // select by view
        Spinner spinnerSelectBy = (Spinner) findViewById(R.id.spinner3);

        String[] categories3 = {"", "Continent", "Country", "Language"};

        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories3
        );

        arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectBy.setAdapter(arrayAdapter3);

        spinnerSelectBy.setOnItemSelectedListener(cbxListener);

    }

    private class GetReadableDatabaseTask extends AsyncTask<String, Void, SQLiteDatabase> {

        private String searchType = null;
        private String searchBy = null;

        //This method is executed by the second thread.
        //It gets its arguments from the execute method of GetReadableDatabaseTask.
        //Its return value is passed as an argument to onPostExecute.

        @Override
        protected SQLiteDatabase doInBackground(String... args) {
            searchType = args[0];
            searchBy = args[1];
            Thread thread = Thread.currentThread();
            Log.d("myTag", "doInBackground " + args[0] + " " + thread.getId() + " " + thread.getName());
            AssetHelper helper = new AssetHelper(MainActivity.this, "world.db");
            return helper.getReadableDatabase();
        }

        //This method is executed by the UI thread when doInBackground has finished.
        //Its argument is the return value ofDoInBackground.

        @Override
        protected void onPostExecute(SQLiteDatabase database) {
            Thread thread = Thread.currentThread();
            Log.d("myTag", "onPostExecute " + thread.getId() + " " + thread.getName());
            useTheDatabase(database, searchType, searchBy);
        }
    }

    //This method of the Activity is called by onPostExecute.
    //It is executed by the UI thread after the second thread has finished.

    private void useTheDatabase(SQLiteDatabase database, final String selectType, final String selectBy) {

        if ((selectType != null) && (selectBy != null)) {

            String columnName = null;
            String select = null;

            if (selectType.equals("GNP")) {
                columnName = "GNP";
            }
            if (selectType.equals("Life Expectancy")) {
                columnName = "LifeExpectancy";
            }
            if (selectType.equals("Population")) {
                columnName = "Population";
            }
            if (selectType.equals("Surface Area")) {
                columnName = "SurfaceArea";
            }

            if ((columnName != null)) {

                    if (selectBy.equals("Continent")) {
                        select = "select _id, Continent as Name , avg(" + columnName + ")*100 as Value from newcountry group by Continent";
                    }

                    if (selectBy.equals("Country")) {
                        select = "select _id, Name as Name, " + columnName + "*100 as Value from newcountry order by Code";
                    }

                    if (selectBy.equals("Language")) {
                        select = "select newcountrylanguage._id, Language as Name , avg(" + columnName + ")*100 as Value from newcountry " +
                                "inner join newcountrylanguage on newcountry.Code = newcountrylanguage.CountryCode group by Language";
                    }
            }

            if (select != null) {
                Cursor cursor = database.rawQuery(select, null);

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        this,
                        android.R.layout.simple_list_item_2,
                        cursor,
                        new String[]{"Name", "Value"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0     //Flags are not needed when using a CursorLoader.
                );

                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        if(!selectType.equals("Population") && (columnIndex == 2)) {
                            TextView text = (TextView) view;  // get your View
                            double amount = cursor.getDouble(2)/100;

                            text.setText(String.valueOf(amount));  //set some data
                            return true;
                        }
                        return false;
                    }
                });

                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "Invalid Selection", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
