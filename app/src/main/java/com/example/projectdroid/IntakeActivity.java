package com.example.projectdroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.List;

import controller.PartsController;
import controller.VehicleController;
import model.Parts;
import model.Vehicle;

public class IntakeActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvVehicleInfo;
    private Button buttonVIN, btnYes, btnNo, btnAddPart, btnScan;
    private CardView cardPopup, cardPartsPopup;
    private ListView listViewParts;
    private Vehicle pendingVehicle = null;
    private List<Parts> fetchedPartsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);


        etInput = findViewById(R.id.etInput);
        buttonVIN = findViewById(R.id.btnConfirm);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        cardPopup = findViewById(R.id.cardPopup);
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo);
        cardPartsPopup = findViewById(R.id.cardPartsPopup);
        btnAddPart = findViewById(R.id.btnAddPart);
        listViewParts = findViewById(R.id.listViewParts);
        btnScan = findViewById(R.id.btnScan);

        buttonVIN.setOnClickListener(v -> {

            String vin = etInput.getText().toString().trim();


            if (vin.isEmpty()) {
                etInput.setError("Please enter a VIN.");
                return;
            }

            new Thread(() -> {
                VehicleController controller = new VehicleController();

                try {

                    Vehicle vehicle = controller.validateVehicleIntake(vin, IntakeActivity.this);
                    pendingVehicle = vehicle;

                    runOnUiThread(() -> {
                        String info = vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")";
                        tvVehicleInfo.setText(info);
                        cardPopup.setVisibility(View.VISIBLE);
                        buttonVIN.setEnabled(false);
                    });

                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    runOnUiThread(() -> {
                        Toast.makeText(IntakeActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        buttonVIN.setEnabled(true);
                    });
                }
            }).start();
        });

        btnScan.setOnClickListener(v -> {
            GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_CODE_39,
                            Barcode.FORMAT_CODE_128
                    )
                    .enableAutoZoom()
                    .build();

            GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        String rawVin = barcode.getRawValue();

                        if (rawVin != null && rawVin.length() == 17) {
                            etInput.setText(rawVin);
                            Toast.makeText(this, "VIN scanned: " + rawVin, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "The scanned code does not contain na valid VIN. ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnCanceledListener(() -> {
                        Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error while scanning: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        btnYes.setOnClickListener(v -> {
            if (pendingVehicle != null) {
                pendingVehicle.setStatus(true);
            }
            cardPopup.setVisibility(View.GONE);
            cardPartsPopup.setVisibility(View.VISIBLE);

            loadParts();
        });

        btnNo.setOnClickListener(v -> {
            if (pendingVehicle != null) {
                pendingVehicle.setStatus(false);
            }
            cardPopup.setVisibility(View.GONE);
            saveVehicle("Vehicle registered as non-salvageable.");
        });

        btnAddPart.setOnClickListener(v -> {
            if (pendingVehicle != null) {
                SparseBooleanArray checkedPositions = listViewParts.getCheckedItemPositions();
                List<Parts> selectedPartsList = new ArrayList<>();

                for (int i = 0; i < checkedPositions.size(); i++) {
                    int position = checkedPositions.keyAt(i);
                    if (checkedPositions.valueAt(i)) {
                        Parts part = fetchedPartsList.get(position);

                        part.setAssociatedVIN(pendingVehicle.getVin());
                        part.setAssociatedMake(pendingVehicle.getMake());
                        part.setAssociatedModel(pendingVehicle.getModel());
                        part.setAssociatedYear(pendingVehicle.getYear());

                        selectedPartsList.add(part);
                    }
                }

                if (selectedPartsList.isEmpty()) {
                    Toast.makeText(IntakeActivity.this, "Please select at least one part.", Toast.LENGTH_SHORT).show();
                    return;
                }

                pendingVehicle.setPartsList(selectedPartsList);

                cardPartsPopup.setVisibility(View.GONE);
                saveVehicle("Success! Vehicle and selected parts registered.");
            }
        });
    }
    private void saveVehicle(String successMessage) {
        if (pendingVehicle == null) return;

        new Thread(() -> {
            VehicleController controller = new VehicleController();
            try {
                controller.processVehicleIntake(IntakeActivity.this, pendingVehicle);

                runOnUiThread(() -> {
                    Toast.makeText(IntakeActivity.this, successMessage, Toast.LENGTH_LONG).show();

                    cardPartsPopup.setVisibility(View.GONE);

                    Intent intent = new Intent(IntakeActivity.this, MapActivity.class);
                    intent.putExtra("Where", "IntakeAct");
                    intent.putExtra("VIN", pendingVehicle.getVin());
                    intent.putExtra("Modelo",pendingVehicle.getModel());
                    startActivity(intent);

                    etInput.setText("");
                    buttonVIN.setEnabled(true);
                    pendingVehicle = null;
                });

            } catch (Exception e) {
                String err = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(IntakeActivity.this, "Save Error: " + err, Toast.LENGTH_LONG).show();
                    buttonVIN.setEnabled(true);
                    pendingVehicle = null;
                });
            }
        }).start();
    }
    private void loadParts() {
        new Thread(() -> {
            try {
                PartsController partsController = new PartsController();
                fetchedPartsList = partsController.processPartsInfo(IntakeActivity.this);

                runOnUiThread(() -> {
                    if (fetchedPartsList != null && !fetchedPartsList.isEmpty()) {
                        ArrayAdapter<Parts> adapter = new ArrayAdapter<>(
                                IntakeActivity.this,
                                android.R.layout.simple_list_item_multiple_choice,
                                fetchedPartsList
                        );
                        listViewParts.setAdapter(adapter);
                        listViewParts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    } else {
                        Toast.makeText(IntakeActivity.this, "No parts found in database.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(IntakeActivity.this, "Error loading parts: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}