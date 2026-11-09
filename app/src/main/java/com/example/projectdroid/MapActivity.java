package com.example.projectdroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

import model.Parts;
import model.Vehicle;
import controller.MapController;
import model.Slot;

public class MapActivity extends AppCompatActivity {

    private List<Slot> globalSlotsList;
    private String selectedSlotId = "", currentClickedSlotId = "", vehicleModel = "", origin;
    private boolean intakeRegistered = false;
    private CardView cardPopup, cardParkingOptions, cardPartsPopup;;
    private TextView tvVehicleInfo, txtMapQuestion, txtVehicleDescription, txtVehicleVin,  btnEmptySpot, btnRemovePieces, btnBackParkingOpt, btnClosePartsPopup;
    private Button btnNo, btnYes, btnCancelParts, btnAddPart, btnBackToMain;;
    private ListView listViewParts;

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
        setupBtnEmptySpot();
        setupBtnRemovePieces();
        setupBtnBackParkingOpt();
        setupBtnClosePartsPopup();
        setupBtnCancelParts();
        setupBtnAddPart();

        loadMapData();
    }

    private void initViews() {
        cardPopup = findViewById(R.id.mapcardPopup);
        tvVehicleInfo = findViewById(R.id.mapVehicleInfo);
        txtMapQuestion = findViewById(R.id.mapQuestionText);
        btnNo = findViewById(R.id.mapBtnNo);
        btnYes = findViewById(R.id.mapBtnYes);

        cardParkingOptions = findViewById(R.id.card_parking_options);
        txtVehicleDescription = findViewById(R.id.txt_vehicle_description);
        txtVehicleVin = findViewById(R.id.txt_vehicle_vin);
        btnEmptySpot = findViewById(R.id.btn_empty_spot);
        btnRemovePieces = findViewById(R.id.btn_remove_pieces);
        btnBackParkingOpt = findViewById(R.id.btn_back_parking_opt);

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
                final String slotIdName = getResources().getResourceEntryName(tempslot.getId());

                tempslot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSlotClicked(slotIdName);
                    }
                });
            }
        }
    }

    private void onSlotClicked(String slotIdName) {
        Slot clickedSlot = findSlotById(slotIdName);

        if ("IntakeAct".equals(origin)) {
            if (intakeRegistered) {
                showToast("Intake registered. You can return to the Main Menu.");
                return;
            }

            if (clickedSlot != null && clickedSlot.isOccupied()) {
                showIntakeOccupiedPopup(slotIdName);
                return;
            }
            showIntakeConfirmPopup(slotIdName);
        } else {
            currentClickedSlotId = slotIdName;
            popUpInformationSlot(slotIdName);
        }
    }

    private void setupBtnYes() {
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirmIntakeClicked();
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

    private void setupBtnEmptySpot() {
        btnEmptySpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEmptySpotClicked();
            }
        });
    }

    private void setupBtnRemovePieces() {
        btnRemovePieces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpMeuParts(currentClickedSlotId);
            }
        });
    }

    private void setupBtnBackParkingOpt() {
        btnBackParkingOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardParkingOptions.setVisibility(View.GONE);
            }
        });
    }

    private void setupBtnClosePartsPopup() {
        btnClosePartsPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardPartsPopup.setVisibility(View.GONE);
            }
        });
    }

    private void setupBtnCancelParts() {
        btnCancelParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listViewParts.clearChoices();
                cardPartsPopup.setVisibility(View.GONE);
            }
        });
    }

    private void setupBtnAddPart() {
        btnAddPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPartsClicked();
                cardPartsPopup.setVisibility(View.GONE);
                cardParkingOptions.setVisibility(View.GONE);
            }
        });
    }

    private void onConfirmIntakeClicked() {
        if (selectedSlotId == null || selectedSlotId.isEmpty()) {
            return;
        }

        String columnStr = selectedSlotId.substring(0, 1);
        int rowInt = Integer.parseInt(selectedSlotId.substring(1));
        String vin = getIntent().getStringExtra("VIN");

        if (vin == null || vin.isEmpty()) {
            showToast("Error: No VIN was received!");
            return;
        }
        occupySlotInBackground(new Slot(rowInt, columnStr, vin));
    }

    private void occupySlotInBackground(final Slot slot) {
        MapController.registerVIN2Slot(this, slot, new MapController.SaveCallback() {
            @Override
            public void onSuccess() {
                showToast("Slot " + selectedSlotId + " successfully occupied!");
                onIntakeSlotRegistered();
                cardPopup.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                showToast(message);
                cardPopup.setVisibility(View.GONE);
            }
        });
    }

    private void onIntakeSlotRegistered() {
        intakeRegistered = true;
        loadMapData();
        showBackToMainButton();
    }

    private void onEmptySpotClicked() {
        Slot slotToEmpty = findSlotById(currentClickedSlotId);

        if (slotToEmpty == null || !slotToEmpty.isOccupied()) {
            showToast("Slot not found or already empty.");
            return;
        }

        String vin = slotToEmpty.getCurrent_vin();
        if (vin == null || vin.isEmpty()) {
            showToast("Errro: This slot is empty.");
            return;
        }
        checkPartsBeforeEmptyingInBackground(slotToEmpty, vin);
    }

    private void checkPartsBeforeEmptyingInBackground(final Slot slot, final String vin) {
        MapController.getVehicle(this, vin, new MapController.VehicleCallback() {
            @Override
            public void onSuccess(Vehicle vehicle) {
                List<Parts> parts = vehicle.getPartsList();
                int partsCount = (parts == null) ? 0 : parts.size();

                if (partsCount == 0) {
                    cardParkingOptions.setVisibility(View.GONE);
                    sendVehicleToHistoryInBackground(slot, vin);
                } else {
                    showSendAllPartsDialog(slot, vin, partsCount);
                }
            }

            @Override
            public void onError(String message) {
                showToastLong("Error: " + message);
            }
        });
    }

    private void showSendAllPartsDialog(final Slot slot, final String vin, int partsCount) {
        new AlertDialog.Builder(MapActivity.this)
                .setTitle("Vehicle has parts")
                .setMessage("This vehicle still has " + partsCount + " part(s).\n"
                        + "Do you want to send all parts to inventory and empty this slot?")
                .setCancelable(false)
                .setPositiveButton("Send all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cardParkingOptions.setVisibility(View.GONE);
                        sendAllPartsAndEmptySlotInBackground(slot, vin);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void sendAllPartsAndEmptySlotInBackground(final Slot slot, final String vin) {
        MapController.sendAllPartsAndEmptySlot(this, slot, vin, new MapController.SaveCallback() {
            @Override
            public void onSuccess() {
                onVehicleScrapped(vin);
            }

            @Override
            public void onError(String message) {
                showToastLong("Error: " + message);
                loadMapData();
            }
        });
    }

    private void sendVehicleToHistoryInBackground(final Slot slot, final String vin) {
        MapController.sendVehicleToHistory(this, slot, vin, new MapController.SaveCallback() {
            @Override
            public void onSuccess() {
                showToast("Vehicle (VIN: " + vin + ") sent to inventory!");
                loadMapData();
            }

            @Override
            public void onError(String message) {
                showToastLong("Error: " + message);
            }
        });
    }

    private void onAddPartsClicked() {
        // 1. Ler os nomes das peças selecionadas na ListView
        List<String> selectedNames = getSelectedPartNames();

        if (selectedNames.isEmpty()) {
            showToast("Select at least one part.");
            return;
        }

        Slot currentSlot = findSlotById(currentClickedSlotId);

        if (currentSlot == null || !currentSlot.isOccupied()) {
            showToast("Invalid slot.");
            return;
        }

        String vin = currentSlot.getCurrent_vin();

        if (willRemoveAllParts(selectedNames)) {
            showScrapConfirmDialog(currentSlot, vin, selectedNames);
            return;
        }
        startMovingParts(currentSlot, vin, selectedNames);
    }

    private void startMovingParts(Slot slot, String vin, List<String> selectedNames) {
        removeNamesFromListView(selectedNames);
        movePartsToInventoryInBackground(slot, vin, selectedNames);
    }

    private List<String> getSelectedPartNames() {
        SparseBooleanArray checked = listViewParts.getCheckedItemPositions();
        List<String> selectedNames = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                selectedNames.add((String) listViewParts.getItemAtPosition(checked.keyAt(i)));
            }
        }
        return selectedNames;
    }

    private boolean willRemoveAllParts(List<String> selectedNames) {
        return selectedNames.size() >= listViewParts.getAdapter().getCount();
    }

    private void removeNamesFromListView(List<String> names) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listViewParts.getAdapter();
        for (String name : names) {
            adapter.remove(name);
        }
        listViewParts.clearChoices();
        adapter.notifyDataSetChanged();
    }

    private void movePartsToInventoryInBackground(final Slot slot, final String vin, final List<String> selectedNames) {
        MapController.movePartsToInventory(this, slot, vin, selectedNames, new MapController.MovePartsCallback() {
            @Override
            public void onSuccess(boolean vehicleScrapped) {
                if (vehicleScrapped) {
                    onVehicleScrapped(vin);
                } else {
                    showToast("Parts added to inventory!");
                }
            }

            @Override
            public void onError(String message) {
                showToastLong("Error: " + message);
                // Reload the real list to restore the parts that failed
                popUpMeuParts(currentClickedSlotId);
            }
        });
    }

    private void onVehicleScrapped(String vin) {
        cardPartsPopup.setVisibility(View.GONE);
        cardParkingOptions.setVisibility(View.GONE);
        showToastLong("Vehicle (VIN: " + vin + ") removed form slot and sent to inventory!");
        loadMapData();
    }

    private void onScrapCancelled() {
        listViewParts.clearChoices();
        ((ArrayAdapter<?>) listViewParts.getAdapter()).notifyDataSetChanged();
    }

    private void loadMapData() {
        MapController.processMapSlots(this, new MapController.SlotsCallback() {
            @Override
            public void onSuccess(List<Slot> slots) {
                globalSlotsList = slots;
                updateMapUI(slots);
            }

            @Override
            public void onError(String message) {
                showToastLong(message);
            }
        });
    }

    private void updateMapUI(List<Slot> slots) {
        for (Slot slot : slots) {
            String slotIdString = slot.getSlotID();
            Log.d("PARKING_DEBUG", "Slot: " + slotIdString + " | VIN: " + slot.getCurrent_vin() + " | Occupied: " + slot.isOccupied());
            int frameResId = getResources().getIdentifier(slotIdString, "id", getPackageName());
            int imgResId = getResources().getIdentifier("img_" + slotIdString, "id", getPackageName());

            if (frameResId != 0 && imgResId != 0) {
                ImageView carImageView = findViewById(imgResId);

                if (slot.isOccupied()) {
                    carImageView.setVisibility(View.VISIBLE);
                } else {
                    carImageView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showIntakeOccupiedPopup(String slotIdName) {
        tvVehicleInfo.setText("Slot: " + slotIdName + "\nThis slot is occupied!");
        txtMapQuestion.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setText("Back");
        cardPopup.setVisibility(View.VISIBLE);
    }

    private void showIntakeConfirmPopup(String slotIdName) {
        selectedSlotId = slotIdName;
        tvVehicleInfo.setText("Model: " + vehicleModel + "\nSlot: " + selectedSlotId);
        txtMapQuestion.setVisibility(View.VISIBLE);
        btnYes.setVisibility(View.VISIBLE);
        btnNo.setText("No");

        cardPopup.setVisibility(View.VISIBLE);
    }

    private void popUpInformationSlot(String clickedSlotId) {
        if (!isMapDataLoaded()) return;

        Slot selectedSlot = findSlotById(clickedSlotId);

        if (selectedSlot != null && selectedSlot.isOccupied()) {
            showOccupiedSlotOptions(clickedSlotId, selectedSlot.getCurrent_vin());
        } else {
            showEmptySlotPopup();
        }
    }

    private void showOccupiedSlotOptions(String slotId, String vin) {
        String mensagem = "Slot: " + slotId
                + "\nStatus: Occupied";

        txtVehicleDescription.setText(mensagem);
        txtVehicleVin.setText("VIN: " + vin);

        cardPopup.setVisibility(View.GONE);
        cardParkingOptions.setVisibility(View.VISIBLE);
    }

    private void showEmptySlotPopup() {
        tvVehicleInfo.setText("This slot is empty.");
        txtMapQuestion.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setText("Back");

        cardParkingOptions.setVisibility(View.GONE);
        cardPopup.setVisibility(View.VISIBLE);
    }

    private void popUpMeuParts(String clickedSlotId) {
        if (!isMapDataLoaded()) return;
        Slot selectedSlot = findSlotById(clickedSlotId);

        if (selectedSlot == null) {
            showToast("Slot not found.");
            return;
        }

        if (!selectedSlot.isOccupied()) {
            showToast("This slot is free.");
            return;
        }
        loadVehiclePartsInBackground(selectedSlot);
    }

    private void loadVehiclePartsInBackground(final Slot slot) {
        MapController.getVehicle(this, slot.getCurrent_vin(), new MapController.VehicleCallback() {
            @Override
            public void onSuccess(Vehicle vehicle) {
                showPartsPopup(vehicle.getPartsList());
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void showPartsPopup(List<Parts> parts) {
        if (parts == null || parts.isEmpty()) {
            showToast("This vehicle has no parts.");
            return;
        }

        List<String> nomes = new ArrayList<>();
        for (Parts p : parts) {
            nomes.add(p.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MapActivity.this,
                android.R.layout.simple_list_item_multiple_choice,
                nomes);

        listViewParts.setAdapter(adapter);
        cardPartsPopup.setVisibility(View.VISIBLE);
    }

    private void showScrapConfirmDialog(final Slot slot, final String vin, final List<String> selectedNames) {
        new AlertDialog.Builder(MapActivity.this)
                .setTitle("Vehicle has no parts")
                .setMessage("This vehicle will have no parts left.\nDo you want to remove it from this slot?")
                .setCancelable(false)
                .setPositiveButton("Remove vehicle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startMovingParts(slot, vin, selectedNames);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onScrapCancelled();
                    }
                })
                .show();
    }

    private void showBackToMainButton() {
        if (btnBackToMain != null) {
            btnBackToMain.setVisibility(View.VISIBLE);
            return;
        }

        btnBackToMain = new Button(MapActivity.this);
        btnBackToMain.setText("Back to Main Menu");
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackToMain();
            }
        });

        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.setMargins(0, 0, 0, margin);
        addContentView(btnBackToMain, params);
    }

    private void goBackToMain() {
        Intent intent = new Intent(MapActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private Slot findSlotById(String slotId) {
        if (globalSlotsList == null) return null;

        for (Slot slot : globalSlotsList) {
            if (slot.getSlotID().equals(slotId)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isMapDataLoaded() {
        if (globalSlotsList == null) {
            showToast("Loading map data...");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(MapActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToastLong(String message) {
        Toast.makeText(MapActivity.this, message, Toast.LENGTH_LONG).show();
    }
}