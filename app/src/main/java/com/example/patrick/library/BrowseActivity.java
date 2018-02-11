package com.example.patrick.library;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Debug;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.patrick.library.logic.Book;

public class BrowseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                                        Context.MODE_PRIVATE);
        if (savedData.getString(getString(R.string.last_library_key), null) == null ||
                savedData.getString(getString(R.string.last_library_key), null).length() != 36) {
            Intent intent = new Intent(this, BrowseLibraryActivity.class);
            startActivity(intent);
            return;
        }

        String[] bookNames = new String[10];

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, bookNames);

        ListView listView = findViewById(R.id.book_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                openBookDetail(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_map:
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                return true;

            case R.id.change_library:
                return true;

            case R.id.report_bug:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void openBookDetail(int position) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("BOOK_ID", "" + position);
        startActivity(intent);
    }
}
