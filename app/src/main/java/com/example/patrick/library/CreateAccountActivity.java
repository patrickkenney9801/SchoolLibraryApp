package com.example.patrick.library;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText firstNameEnter;
    private EditText lastNameEnter;
    private EditText emailAddressEnter;
    private EditText passwordEnter;
    private EditText retypePasswordEnter;
    private Button signUpButton;

    private String


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailAddressEnter = findViewById(R.id.textEmail);
        passwordEnter = findViewById(R.id.create_Password);
        retypePassword = findViewById(R.id.create_Password);
        signUpButton = findViewById(R.id.buttonSignUp);


        retypePassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                                     @Override
                                                     public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                                                         if (actionId == R.id.create_Account_Name || actionId == EditorInfo.IME_NULL) {
                                                             attemptCreateAccount();
                                                             return true;
                                                         }
                                                         return false;
                                                     }
                                                 });
        signUpButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                attemptCreateAccount():}
                                        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateAccountWithGoogle();
            }
            });

        private void attemptCreateAccount() {
            booleancancel = false;
            View focusView = null;

            lastName = lastNameEntry.getText().toString();
            firstName = firstNameEntry.getText().toString();
            email = emailEntry.getText().toString();
            passwordEnter = passwordEnter.getText().toString();
            reTypePassword = confirmPwEntry.getText().toString();


    }
    }
}
