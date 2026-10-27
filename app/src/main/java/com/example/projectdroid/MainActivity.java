package com.example.projectdroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import temp.ObjCreationTest;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private Button respectBotton;
    private Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        respectBotton = findViewById(R.id.respectBotton);
        mapButton = findViewById(R.id.mapButton);

        respectBotton.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, IntakeActivity.class);
            startActivity(intent);

        });

        mapButton.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);

        });
    }
}
