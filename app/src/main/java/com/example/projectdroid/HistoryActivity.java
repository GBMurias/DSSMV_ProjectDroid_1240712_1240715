package com.example.projectdroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.List;

import adapter.HistoryAdapter;
import controller.HistoryController;
import model.Vehicle;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvHistory;
    private CardView cardPopup;
    private TextView tvVehicleDetails;
    private Button btnClosePopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lvHistory = findViewById(R.id.lvHistory);
        cardPopup = findViewById(R.id.cardPopup);
        tvVehicleDetails = findViewById(R.id.tvVehicleDetails);
        btnClosePopup = findViewById(R.id.btnClosePopup);

        btnClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardPopup.setVisibility(View.GONE);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                HistoryController controller = new HistoryController();
                try {
                    List<Vehicle> scrappedVehicles = controller.getScrappedVehicles(HistoryActivity.this);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HistoryAdapter adapter = new HistoryAdapter(HistoryActivity.this, scrappedVehicles);
                            lvHistory.setAdapter(adapter);
                            lvHistory.setOnItemClickListener((parent, view, position, id) -> {
                                Vehicle clickedVehicle = (Vehicle) adapter.getItem(position);
                                String details = clickedVehicle.getMake() + " " + clickedVehicle.getModel() + " " + clickedVehicle.getYear();
                                tvVehicleDetails.setText(details);
                                cardPopup.setVisibility(View.VISIBLE);
                            });
                        }
                    });

                } catch (Exception e) {
                    String error = e.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HistoryActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
