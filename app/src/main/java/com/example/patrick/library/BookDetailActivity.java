package com.example.patrick.library;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.patrick.library.logic.Book;

public class BookDetailActivity extends AppCompatActivity {
    private Button action;
    private Book book;
    private int bookID;
    private int bookDetailType;
    private String userKey;

    private View mProgressForm;
    private View mBookDetailForm;

    private final String reserve = "Reserve";
    private final String unreserve = "Unreserve";
    private final String reserved = "Reserved";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent startingIntent = getIntent();
        bookID = Integer.parseInt(startingIntent.getStringExtra("BOOK_ID"));
        book = Book.books.get(bookID);
        bookDetailType = 1;//Integer.parseInt(startingIntent.getStringExtra("BOOK_DETAIL_TYPE"));
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        userKey = savedData.getString(getString(R.string.user_key), null);

        ((TextView) findViewById(R.id.book_name)).setText(book.name);
        ((TextView) findViewById(R.id.author_name)).setText(book.authorFirstName + " " + book.authorLastName);
        ((TextView) findViewById(R.id.date_published)).setText(book.datePublished);

        action = findViewById(R.id.book_detail_action);
        switch (bookDetailType) {
            case 1:     if (book.userKey == null || book.userKey.length() != 36)
                            action.setText(reserve);
                        else if (book.userKey.equals(userKey))
                            action.setText(unreserve);
                        else {
                            action.setEnabled(false);
                            action.setText(reserved);
                        }
                        break;
            case 2:     break;
            case 3:     break;
        }
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (action.getText().toString().equals(reserve))
                    reserveBook();
                else if (action.getText().toString().equals(unreserve))
                    unreserveBook();
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
                intent = new Intent(this, AdvancedMenuActivity.class);
                startActivity(intent);
                return true;

            case R.id.report_bug:
                intent = new Intent(this, ReportBugActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void reserveBook() {

    }

    private void unreserveBook() {

    }
}
