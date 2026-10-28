package com.example.projectdroid;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import controller.MapController;
import dto.SlotDTO;

public class MapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        loadMapData();
    }
    private void loadMapData() {
        new Thread(() -> {
            MapController controller = new MapController();

            try {
                List<SlotDTO> slots = controller.processMapSlots(MapActivity.this);

                runOnUiThread(() -> {
                    updateMapUI(slots);
                });

            } catch (Exception e) {
                String errorMsg = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this, "Error while loading map data: " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    private void updateMapUI(List<SlotDTO> slots) {
        for (SlotDTO slot : slots) {
            String slotIdString = slot.getSlotID();
            android.util.Log.d("PARKING_DEBUG", "Slot: " + slotIdString + " | VIN: " + slot.getCurrent_vin() + " | Occupied: " + slot.isOccupied());
            int frameResId = getResources().getIdentifier(slotIdString, "id", getPackageName());
            int imgResId = getResources().getIdentifier("img_" + slotIdString, "id", getPackageName());

            if (frameResId != 0 && imgResId != 0) {
                View slotContainer = findViewById(frameResId);
                ImageView carImageView = findViewById(imgResId);

                if (slot.isOccupied()) {
                    carImageView.setVisibility(View.VISIBLE);
                } else {
                    carImageView.setVisibility(View.GONE);
                }
            }
        }
    }
}
