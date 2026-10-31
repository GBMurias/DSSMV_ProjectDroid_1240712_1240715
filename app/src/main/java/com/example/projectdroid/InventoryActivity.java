package com.example.projectdroid;


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.List;
import adapter.InventoryAdapter;

import controller.PartsController;
import model.Parts;

public class InventoryActivity extends AppCompatActivity {

    private ListView lvInventory;
    private CardView cardPopup;
    private TextView tvPartDetails;
    private Button btnClosePopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        lvInventory = findViewById(R.id.lvInventory);
        cardPopup = findViewById(R.id.cardPopup);
        tvPartDetails = findViewById(R.id.tvPartDetails);
        btnClosePopup = findViewById(R.id.btnClosePopup);

        btnClosePopup.setOnClickListener(v -> {
            cardPopup.setVisibility(View.GONE);
        });

        new Thread(() -> {
            PartsController controller = new PartsController();
            try {
                List<Parts> storedParts = controller.processInventoryInfo(InventoryActivity.this);

                runOnUiThread(() -> {
                    InventoryAdapter adapter = new InventoryAdapter(InventoryActivity.this, storedParts);
                    lvInventory.setAdapter(adapter);
                    lvInventory.setOnItemClickListener((parent, view, position, id) -> {
                        Parts clickedPart = (Parts) adapter.getItem(position);
                        String details = "Make: " + clickedPart.getAssociatedMake() + "\n" +
                                "Model: " + clickedPart.getAssociatedModel() + "\n" +
                                "Year: " + clickedPart.getAssociatedYear();

                        tvPartDetails.setText(details);
                        cardPopup.setVisibility(View.VISIBLE);
                    });
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
