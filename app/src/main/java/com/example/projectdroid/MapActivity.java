package com.example.projectdroid;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.List;

import model.Parts;
import model.Vehicle;
import service.MapService;
import controller.MapController;
import dto.SlotDTO;
import model.Slot;

public class MapActivity extends AppCompatActivity {

    // ==========================================
    // 1. VARIÁVEIS
    // ==========================================

    // --- Estado / dados ---
    private List<Slot> globalSlotsList;
    private String selectedSlotId = "";
    private String vehicleModel = "";
    private String origin;

    // --- Popup de confirmação (Intake) ---
    private CardView cardPopup;
    private TextView tvVehicleInfo;
    private Button btnNo;
    private Button btnYes;

    // --- Popup de opções do slot ---
    private CardView cardParkingOptions;
    private TextView txtVehicleDescription;
    private TextView txtVehicleVin;
    private TextView btnEmptySpot;
    private TextView btnRemovePieces;

    // --- Popup de peças ---
    private CardView cardPartsPopup;
    private TextView btnClosePartsPopup;
    private ListView listViewParts;
    private Button btnCancelParts;
    private Button btnAddPart;

    // ==========================================
    // 2. CICLO DE VIDA
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        readIntentExtras();
        setupSlotClickListeners();
        setupBtnYes();
        setupBtnNo();

        loadMapData();
    }

    // ==========================================
    // 3. INICIALIZAÇÃO (chamada pelo onCreate)
    // ==========================================

    private void initViews() {
        cardPopup = findViewById(R.id.mapcardPopup);
        tvVehicleInfo = findViewById(R.id.mapVehicleInfo);
        btnNo = findViewById(R.id.mapBtnNo);
        btnYes = findViewById(R.id.mapBtnYes);

        cardParkingOptions = findViewById(R.id.card_parking_options);
        txtVehicleDescription = findViewById(R.id.txt_vehicle_description);
        txtVehicleVin = findViewById(R.id.txt_vehicle_vin);
        btnEmptySpot = findViewById(R.id.btn_empty_spot);
        btnRemovePieces = findViewById(R.id.btn_remove_pieces);

        cardPartsPopup = findViewById(R.id.MapcardPartsPopup);
        btnClosePartsPopup = findViewById(R.id.btnClosePartsPopup);
        listViewParts = findViewById(R.id.listViewParts);
        btnCancelParts = findViewById(R.id.btnCancelParts);
        btnAddPart = findViewById(R.id.btnAddPart);
    }

    private void readIntentExtras() {
        if (getIntent() != null) {
            origin = getIntent().getStringExtra("Where");
            vehicleModel = getIntent().getStringExtra("Modelo");
        }
    }

    private void setupSlotClickListeners() {
        GridLayout gridLayout = findViewById(R.id.grid_layout);

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);

            if (child instanceof FrameLayout) {
                FrameLayout tempslot = (FrameLayout) child;
                String slotIdName = getResources().getResourceEntryName(tempslot.getId());


                tempslot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Slot clickedSlot = null;
                        if (globalSlotsList != null) {
                            for (Slot slot : globalSlotsList) {
                                if (slot.getSlotID().equals(slotIdName)) {
                                    clickedSlot = slot;
                                    break;
                                }
                            }
                        }


                        if ("IntakeAct".equals(origin)) {
                            if (clickedSlot != null && clickedSlot.isOccupied()) {
                                tvVehicleInfo.setText("Slot: " + slotIdName + "\nO Slot está Ocupado!");
                                btnYes.setVisibility(View.GONE);
                                btnNo.setText("Back");

                                cardPopup.setVisibility(View.VISIBLE);
                                return;
                            }

                            selectedSlotId = slotIdName;
                            tvVehicleInfo.setText("Modelo: " + vehicleModel + "\nSlot: " + selectedSlotId);
                            if (btnYes != null) btnYes.setVisibility(View.VISIBLE);
                            if (btnNo != null) btnNo.setText("No");

                            cardPopup.setVisibility(View.VISIBLE);
                        } else {
                            popUpInformationSlot(slotIdName);

                        }
                    }
                });
            }
        }
    }

    private void setupBtnYes() {
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
                            MapController ctrl = new MapController();
                            boolean sucess = ctrl.registerVIN2Slot(MapActivity.this, tempdto);

                            if (vin == null || vin.isEmpty()) {
                                Toast.makeText(MapActivity.this, "Erro: O VIN do veículo não foi recebido!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (sucess) {
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
    }

    private void setupBtnNo() {
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardPopup.setVisibility(View.GONE);
            }
        });
    }

    // ==========================================
    // 4. CARREGAMENTO DE DADOS E ATUALIZAÇÃO DA UI
    // ==========================================

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

    // ==========================================
    // 5. POPUPS
    // ==========================================

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

            txtVehicleDescription.setText(mensagem);
            txtVehicleVin.setText("VIN: " + vin);
            cardParkingOptions.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(this, "No information found for this slot. ", Toast.LENGTH_SHORT).show();
        }
    }

    private void popUpMeuParts(String clickedSlotId) {
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

        if (selectedSlot == null) {
            Toast.makeText(this, "Slot not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Slot finalSlot = selectedSlot;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapController controller = new MapController();
                    Vehicle vehicle = controller.getVehicle(MapActivity.this, finalSlot.getCurrent_vin());
                    final List<Parts> parts = vehicle.getPartsList();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                } catch (final Exception e) {
                    Log.e("MapActivity", "Error fetching vehicle", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }
}
