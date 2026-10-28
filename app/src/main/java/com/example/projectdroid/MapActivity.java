package com.example.projectdroid;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import controller.MapController;
import dto.SlotDTO;

public class MapActivity extends AppCompatActivity {


    private List<SlotDTO> globalSlotsList;
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

                List<SlotDTO> slots = controller.processMapSlots(MapActivity.this);

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

    private void updateMapUI(List<SlotDTO> slots) {
        for (SlotDTO slot : slots) {
            String slotIdString = slot.getSlotID();
            android.util.Log.d("PARKING_DEBUG", "Slot: " + slotIdString + " | VIN: " + slot.getCurrent_vin() + " | Occupied: " + slot.isOccupied());
            int frameResId = getResources().getIdentifier(slotIdString, "id", getPackageName());
            int imgResId = getResources().getIdentifier("img_" + slotIdString, "id", getPackageName());

            if (frameResId != 0 && imgResId != 0) {
                View slotContainer = findViewById(frameResId);
                ImageView carImageView = findViewById(imgResId);

                // 1. Atualizar a visibilidade da imagem do carro com base no current_vin
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
            Toast.makeText(this, "Os dados do mapa ainda estão a carregar...", Toast.LENGTH_SHORT).show();
            return;
        }

        SlotDTO selectedSlot = null;


        for (SlotDTO slot : globalSlotsList) {
            if (slot.getSlotID().equals(clickedSlotId)) {
                selectedSlot = slot;
                break;
            }
        }

        if (selectedSlot != null) {
            String vin = selectedSlot.getCurrent_vin();
            String mensagem;

            if (vin == null || vin.trim().isEmpty()) {
                mensagem = "O slot " + clickedSlotId + " está atualmente LIVRE.";
            } else {
                mensagem = "Slot: " + clickedSlotId + "\nVIN do Veículo: " + vin + "\nEstado: Ocupado";
            }


            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Informação do Slot")
                    .setMessage(mensagem)
                    .setPositiveButton("Fechar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();

        } else {
            Toast.makeText(this, "Informação não encontrada para este slot.", Toast.LENGTH_SHORT).show();
        }
    }
}