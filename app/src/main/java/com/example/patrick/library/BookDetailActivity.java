package com.example.patrick.library;

import android.content.Intent;
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
    private Button back;
    private Button checkOut;
    private Button reserve;
    private Book book;
    private int bookID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent startingIntent = getIntent();
        bookID = Integer.parseInt(startingIntent.getStringExtra("BOOK_ID"));
        book = Book.books.get(bookID);

        ((TextView) findViewById(R.id.book_name)).setText(book.name);
        ((TextView) findViewById(R.id.author_name)).setText(book.authorFirstName + " " + book.authorLastName);
        ((TextView) findViewById(R.id.date_published)).setText(book.datePublished);

        back = findViewById(R.id.book_detail_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        checkOut = findViewById(R.id.checkout);
        reserve = findViewById(R.id.reserve);
        if (book.checkedOut)
            checkOut.setText("RETURN");
        else checkOut.setText("CHECK OUT");

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!book.checkedOut) {
                    book.checkedOut = true;
                    checkOut.setText("RETURN");
                    book.reserved = false;
                    reserve.setEnabled(false);
                    reserve.setText("RESERVE");
                } else {
                    book.checkedOut = false;
                    checkOut.setText("CHECK OUT");
                    reserve.setEnabled(true);
                }
            }
        });

        reserve.setEnabled(true);
        if (book.checkedOut) {
            reserve.setText("RESERVE");
            reserve.setEnabled(false);
        }
        else if (book.reserved)
            reserve.setText("UN-RESERVE");
        else reserve.setText("RESERVE");

        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!book.reserved) {
                    book.reserved = true;
                    reserve.setText("UN-RESERVE");
                } else {
                    book.reserved = false;
                    reserve.setText("RESERVE");
                }
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

    private void goBack() {
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
    }
}
