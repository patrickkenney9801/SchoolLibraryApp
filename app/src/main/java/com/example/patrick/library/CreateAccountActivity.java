package com.example.patrick.library;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import com.example.patrick.library.logic.Book;

public class CreateAccountActivity extends AppCompatActivity {

    //private EditText firstNameEnter;
   // private EditText lastNameEnter;
    private EditText emailAddressEnter;
    private EditText passwordEnter;
    private EditText retypePasswordEnter;
    private Button signUpButton;
    private Button backButton;

    private String email;
    private String password;
    private String reTypePassword;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //firstNameEnter = findViewById(R.id.textEmail);
        //lastNameEnter = findViewById(R.id.create_Password);
        emailAddressEnter = findViewById(R.id.textEmail);
        passwordEnter = findViewById(R.id.createPassword);
        retypePasswordEnter = findViewById(R.id.createRetypePassword);
        signUpButton = findViewById(R.id.buttonSignUp);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateAccount();
            }
        });

        backButton = findViewById(R.id.create_account_back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLogin();
            }
        });
    }

    private void attemptCreateAccount() {
            boolean cancel = false;
            View focusView = null;

            //lastName = lastNameEntry.getText().toString();
            //firstName = firstNameEntry.getText().toString();
            email = emailAddressEnter.getText().toString();
            password = passwordEnter.getText().toString();
            reTypePassword = retypePasswordEnter.getText().toString();

            Book.books[0] = new Book("The Count of Monte Cristo", "Alexandre", "Dumas", "1884");
            Book.books[1] = new Book("The Three Musketeers", "Alexandre", "Dumas", "July 1844");
            Book.books[2] = new Book("Twenty Years After", "Alexandre", "Dumas", "1845");
            Book.books[3] = new Book("War and Peace", "Leo", "Tolstoy", "1867");
            Book.books[4] = new Book("Anna Karenina", "Leo", "Tolstoy", "1877");
            Book.books[5] = new Book("The Brothers Karamazov", "Fyodor", "Dostoyevsky", "1880");
            Book.books[6] = new Book("Animal Farm", "George", "Orwell", "August 17, 1945");
            Book.books[7] = new Book("Resurrection", "Leo", "Tolstoy", "1899");
            Book.books[8] = new Book("The Idiot","Fyodor", "Dostoyevsky", "1869");
            Book.books[9] = new Book("The Death of Ivan Ilyich", "Leo", "Tolstoy", "1886");

            Intent intent = new Intent(this, BrowseActivity.class);
            startActivity(intent);
    }

    private void backToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
