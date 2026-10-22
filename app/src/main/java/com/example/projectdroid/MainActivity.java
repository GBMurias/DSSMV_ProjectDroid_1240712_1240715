package com.example.projectdroid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import temp.ObjCreationTest;

public class MainActivity extends AppCompatActivity {

    private Button respectBotton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        respectBotton = findViewById(R.id.respectBotton);

        respectBotton.setOnClickListener(v -> {

            ObjCreationTest tester = new ObjCreationTest();
            tester.createAllSlots();

        });
    }
}
