package com.example.patrick.library;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Hello, World!");
            }
            });
    }
}
