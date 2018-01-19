package com.example.patrick.library;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.patrick.library.logic.Book;

public class BookDetailActivity extends AppCompatActivity {
    private Button back;
    private Button checkOut;
    private Book book;
    private int bookID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Intent startingIntent = getIntent();
        bookID = Integer.parseInt(startingIntent.getStringExtra("BOOK_ID"));
        book = Book.books[bookID];

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
        if (book.checkedOut)
            checkOut.setText("RETURN");
        else checkOut.setText("CHECK OUT");

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!book.checkedOut) {
                    book.checkedOut = true;
                    checkOut.setText("RETURN");
                } else {
                    book.checkedOut = false;
                    checkOut.setText("CHECK OUT");
                }
            }
        });
    }
    private void goBack() {
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
    }
}
