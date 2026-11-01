package com.example.projectdroid;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.List;
import service.MapService;
import controller.MapController;
import dto.SlotDTO;
import model.Slot;
import model.Vehicle;

public class MapActivity extends AppCompatActivity {

    private List<Slot> globalSlotsList;
    private String selectedSlotId = "";
    private String vehicleModel = "";

    private String origin;
    private CardView cardPopup;
    private TextView tvVehicleInfo;
    private Button btnNo;
    private Button btnYes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        cardPopup = findViewById(R.id.MapcardPopup);
        tvVehicleInfo = findViewById(R.id.MapVehicleInfo);
        btnNo = findViewById(R.id.MapbtnNo);
        btnYes = findViewById(R.id.MapbtnYes);

        loadMapData();

        if (getIntent() != null) {
            origin = getIntent().getStringExtra("Where");
            vehicleModel = getIntent().getStringExtra("Modelo");
        }


        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedSlotId != null && !selectedSlotId.isEmpty()) {


                    String columnStr = selectedSlotId.substring(0, 1);
                    int rowInt = Integer.parseInt(selectedSlotId.substring(1));
                    String vin = getIntent().getStringExtra("VIN");


                    SlotDTO tempdto = new SlotDTO(rowInt, columnStr, vin);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MapService service = new MapService();
                            boolean success = service.registerVIN2Slot(MapActivity.this, tempdto);

                            if (vin == null || vin.isEmpty()) {
                                Toast.makeText(MapActivity.this, "Erro: O VIN do veículo não foi recebido!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (success) {
                                        Toast.makeText(MapActivity.this, "Lugar " + selectedSlotId + " ocupado com sucesso!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MapActivity.this, "Erro ao atualizar o slot na base de dados.", Toast.LENGTH_SHORT).show();
                                    }
                                    cardPopup.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardPopup.setVisibility(View.GONE);
            }
        });

        GridLayout gridLayout = findViewById(R.id.grid_layout);

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);

            if (child instanceof FrameLayout) {
                FrameLayout tempslot = (FrameLayout) child;
                String slotIdName = getResources().getResourceEntryName(tempslot.getId());

                tempslot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if ("IntakeAct".equals(origin)) {

                            selectedSlotId = slotIdName;


                            tvVehicleInfo.setText("Modelo: " + vehicleModel + "\nSlot: " + selectedSlotId);
                            cardPopup.setVisibility(View.VISIBLE);

                            // escreve aqui
                        } else {
                            popUpInformationSlot(slotIdName);
                        }
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

    private void popUpInformationSlot(String clickedSlotId) {

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