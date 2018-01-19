package com.example.patrick.library;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MapActivity extends AppCompatActivity {
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        back = findViewById(R.id.map_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToBrowse();
            }
        });
    }

    private void backToBrowse() {
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
    }
}
