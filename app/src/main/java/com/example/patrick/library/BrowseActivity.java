package com.example.patrick.library;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BrowseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
\\
        String[] bookNames = new String[10];
        Book.books[0] = new Book("The Count of Monte Cristo", "Alexandre", "Dumas", "1884");
        bookNames[0] = Book.books[0].name;
        Book.books[1] = new Book("The Three Musketeers", "Alexandre", "Dumas", "July 1844");
        bookNames[1] = Book.books[1].name;
        Book.books[2] = new Book("Twenty Years After", "Alexandre", "Dumas", "1845");
        bookNames[2] = Book.books[2].name;
        Book.books[3] = new Book("War and Peace", "Leo", "Tolstoy", "1867");
        bookNames[3] = Book.books[3].name;
        Book.books[4] = new Book("Anna Karenina", "Leo", "Tolstoy", "1877");
        bookNames[4] = Book.books[4].name;
        Book.books[5] = new Book("The Brothers Karamazov", "Fyodor", "Dostoyevsky", "1880");
        bookNames[5] = Book.books[5].name;
        Book.books[6] = new Book("Animal Farm", "George", "Orwell", "August 17, 1945");
        bookNames[6] = Book.books[6].name;
        Book.books[7] = new Book("Resurrection", "Leo", "Tolstoy", "1899");
        bookNames[7] = Book.books[7].name;
        Book.books[8] = new Book("The Idiot","Fyodor", "Dostoyevsky", "1869");
        bookNames[8] = Book.books[8].name;
        Book.books[9] = new Book("The Death of Ivan Ilyich", "Leo", "Tolstoy", "1886");
        bookNames[9] = Book.books[9].name;


    }
}
