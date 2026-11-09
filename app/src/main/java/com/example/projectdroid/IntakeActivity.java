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

import controller.IntakeController;
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

        buttonVIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String vin = etInput.getText().toString().trim();

                if (vin.isEmpty()) {
                    etInput.setError("Please enter a VIN.");
                    return;
                }

                buttonVIN.setEnabled(false);

                IntakeController.validateVehicleIntake(IntakeActivity.this, vin, new IntakeController.VehicleCallback() {
                    @Override
                    public void onSuccess(Vehicle vehicle) {
                        pendingVehicle = vehicle;
                        String info = vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")";
                        tvVehicleInfo.setText(info);
                        cardPopup.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(IntakeActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        buttonVIN.setEnabled(true);
                    }
                });
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_CODE_39,
                                Barcode.FORMAT_CODE_128
                        )
                        .build();

                GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(IntakeActivity.this, options);

                scanner.startScan()
                        .addOnSuccessListener(barcode -> {
                            String rawVin = barcode.getRawValue();

                            if (rawVin != null && rawVin.length() == 17) {
                                etInput.setText(rawVin);
                                Toast.makeText(IntakeActivity.this, "VIN scanned: " + rawVin, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(IntakeActivity.this, "The scanned code does not contain na valid VIN. ", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnCanceledListener(() -> {
                            Toast.makeText(IntakeActivity.this, "Scan canceled", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(IntakeActivity.this, "Error while scanning: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pendingVehicle != null) {
                    pendingVehicle.setStatus(true);
                }
                cardPopup.setVisibility(View.GONE);
                cardPartsPopup.setVisibility(View.VISIBLE);

                loadParts();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pendingVehicle != null) {
                    pendingVehicle.setStatus(false);
                }
                cardPopup.setVisibility(View.GONE);
                saveVehicle("Vehicle registered as non-salvageable.");
            }
        });

        btnAddPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
    }
    private void saveVehicle(String successMessage) {
        if (pendingVehicle == null) return;

        IntakeController.processVehicleIntake(this, pendingVehicle, new IntakeController.SaveCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(IntakeActivity.this, successMessage, Toast.LENGTH_LONG).show();
                cardPartsPopup.setVisibility(View.GONE);

                Intent intent = new Intent(IntakeActivity.this, MapActivity.class);
                intent.putExtra("Where", "IntakeAct");
                intent.putExtra("VIN", pendingVehicle.getVin());
                intent.putExtra("Modelo", pendingVehicle.getModel());
                startActivity(intent);

                etInput.setText("");
                buttonVIN.setEnabled(true);
                pendingVehicle = null;
            }

            @Override
            public void onError(String message) {
                Toast.makeText(IntakeActivity.this, "Save Error: " + message, Toast.LENGTH_LONG).show();
                buttonVIN.setEnabled(true);
                pendingVehicle = null;
            }
        });
    }

    private void loadParts() {
        IntakeController.processPartsInfo(this, new IntakeController.PartsCallback() {
            @Override
            public void onSuccess(List<Parts> parts) {
                fetchedPartsList = parts;

                ArrayAdapter<Parts> adapter = new ArrayAdapter<>(
                        IntakeActivity.this,
                        android.R.layout.simple_list_item_multiple_choice,
                        fetchedPartsList
                );
                listViewParts.setAdapter(adapter);
                listViewParts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(IntakeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}