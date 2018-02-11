package com.example.patrick.library;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class ReportBugActivity extends AppCompatActivity {

    private Button changeLibrary;
    private Button reportBug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_bug);
    }
}
