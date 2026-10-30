package com.example.projectdroid;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import controller.MapController;
import dto.SlotDTO;
import model.Slot;

public class MapActivity extends AppCompatActivity {

    private List<Slot> globalSlotsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        loadMapData();

        GridLayout gridLayout = findViewById(R.id.grid_layout);
        for(int i = 0; i < gridLayout.getChildCount(); i++){

            View child = gridLayout.getChildAt(i);

            if(child instanceof FrameLayout){
                FrameLayout slot = (FrameLayout) child;
                String slotIdName = getResources().getResourceEntryName(slot.getId());

                slot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){

                        popUpInformacaoSlot(slotIdName);
                    }
                });
            }
        }
    }

    private void loadMapData() {
        new Thread(() -> {
            MapController controller = new MapController();

            try {

                List<Slot> slots = controller.processMapSlots(MapActivity.this);

                globalSlotsList = slots;


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

    private void updateMapUI(List<Slot> slots) {
        for (Slot slot : slots) {
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

    private void popUpInformacaoSlot(String clickedSlotId) {

        if (globalSlotsList == null || globalSlotsList.isEmpty()) {
            Toast.makeText(this, "Loading map data...", Toast.LENGTH_SHORT).show();
            return;
        }

        Slot selectedSlot = null;


        for (Slot slot : globalSlotsList) {
            if (slot.getSlotID().equals(clickedSlotId)) {
                selectedSlot = slot;
                break;
            }
        }

        if (selectedSlot != null) {
            String vin = selectedSlot.getCurrent_vin();
            String mensagem;

            if (selectedSlot.isOccupied()) {
                mensagem = "Slot: " + clickedSlotId + "\nVehicle Identification Number: " + vin + "\nStatus: Occupied";
            } else {
                mensagem = "Slot " + clickedSlotId + " is currently FREE. ";
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Slot info")
                    .setMessage(mensagem)
                    .setPositiveButton("Close", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();

        } else {
            Toast.makeText(this, "No information found for this slot. ", Toast.LENGTH_SHORT).show();
        }
    }
}