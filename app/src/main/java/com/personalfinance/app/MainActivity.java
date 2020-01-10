package com.personalfinance.app;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabaseHelper dbHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new SQLiteDatabaseHelper(this, "personal.db", null, 1);
        db=dbHelper.getWritableDatabase();
        Intent intent = new Intent(MainActivity.this, tally.class);
        startActivity(intent);
        finish();


    }
}