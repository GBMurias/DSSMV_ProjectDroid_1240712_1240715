package com.example.projectdroid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import temp.ObjCreationTest;
import android.content.Context;

public class IntakeActivity extends AppCompatActivity {

    private Button respectBotton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);
        EditText editText = findViewById(R.id.etInput);

        String VIN = editText.getText().toString();


    }
}