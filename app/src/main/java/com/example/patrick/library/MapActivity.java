package com.example.patrick.library;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.map);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.show_map:
                return true;

            case R.id.change_library:
                intent = new Intent(this, BrowseLibraryActivity.class);
                startActivity(intent);
                return true;

            case R.id.advanced:
                intent = new Intent(this, AdvancedMenuActivity.class);
                startActivity(intent);
                return true;

            case R.id.report_bug:
                intent = new Intent(this, ReportBugActivity.class);
                startActivity(intent);
                return true;

            default:
                intent = new Intent(this, BrowseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("BROWSE_TYPE", "1");
                startActivity(intent);
                return super.onOptionsItemSelected(item);

        }
    }
}
