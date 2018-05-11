package com.booksonthego.patrick.library;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.booksonthego.patrick.library.R;

public class AdvancedMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.advanced);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button createLibrary = findViewById(R.id.go_to_create_library);
        createLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCreateLibrary();
            }
        });

        Button createBook = findViewById(R.id.go_to_add_book);
        createBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddBook();
            }
        });
        createBook.setEnabled(false);

        Button checkoutBooks = findViewById(R.id.go_to_checkout_books);
        checkoutBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCheckoutBrowse();
            }
        });
        checkoutBooks.setEnabled(false);

        Button returnBooks = findViewById(R.id.go_to_return_books);
        returnBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoReturnBrowse();
            }
        });
        returnBooks.setEnabled(false);


        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        char permissions = savedData.getString(getString(R.string.user_role), "1").charAt(0);
        if (permissions == 'L' || permissions == 'C') {
            createBook.setEnabled(true);
            checkoutBooks.setEnabled(true);
            returnBooks.setEnabled(true);
        }
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
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                return true;

            case R.id.change_library:
                intent = new Intent(this, BrowseLibraryActivity.class);
                startActivity(intent);
                return true;

            case R.id.advanced:
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
                return true;

        }
    }

    private void gotoCreateLibrary() {
        Intent intent = new Intent(this, CreateLibraryActivity.class);
        startActivity(intent);
    }

    private void gotoAddBook() {
        Intent intent = new Intent(this, CreateBookActivity.class);
        startActivity(intent);
    }

    private void gotoCheckoutBrowse() {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra("BROWSE_TYPE", "2");
        startActivity(intent);
    }

    private void gotoReturnBrowse() {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra("BROWSE_TYPE", "3");
        startActivity(intent);
    }
}
