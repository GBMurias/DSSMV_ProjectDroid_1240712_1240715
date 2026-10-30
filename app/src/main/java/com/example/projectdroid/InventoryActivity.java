package com.example.projectdroid;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import controller.InfoController;

public class InventoryActivity extends AppCompatActivity {

    private ListView lvInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        lvInventory = findViewById(R.id.lvInventory);

        new Thread(() -> {
            InfoController controller = new InfoController();
            try {
                ArrayList<String> storedParts = controller.processInventoryInfo(InventoryActivity.this);

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            InventoryActivity.this,
                            android.R.layout.simple_list_item_1,
                            storedParts
                    );
                    lvInventory.setAdapter(adapter);
                });
            } catch (Exception e) {
                String error = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(InventoryActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
