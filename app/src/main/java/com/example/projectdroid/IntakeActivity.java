package com.example.projectdroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import controller.VehicleController;
import dto.VehicleDTO;

public class IntakeActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvVehicleInfo;
    private Button buttonVIN, btnYes, btnNo;
    private CardView cardPopup;
    private VehicleDTO pendingVehicle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);


        etInput = findViewById(R.id.etInput);
        buttonVIN = findViewById(R.id.aaa);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        cardPopup = findViewById(R.id.cardPopup);
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo);

                buttonVIN.setOnClickListener(v -> {

            String vin = etInput.getText().toString().trim();


            if (vin.isEmpty()) {
                etInput.setError("Please enter a VIN.");
                return;
            }

            new Thread(() -> {
                VehicleController controller = new VehicleController();

                try {

                    VehicleDTO vehicle = controller.validateVehicleIntake(vin, IntakeActivity.this);
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
                    });
                }
            }).start();
        });

        btnYes.setOnClickListener(v -> {
            setVehicleStatus(true);
        });

        btnNo.setOnClickListener(v -> {
            setVehicleStatus(false);
        });
    }
    private void setVehicleStatus (boolean is_salvageable) {
        if (pendingVehicle == null) return;

        pendingVehicle.setSalvageable(is_salvageable);
        new Thread(() -> {
            VehicleController controller = new VehicleController();
            try {
                controller.processVehicleIntake(IntakeActivity.this, pendingVehicle);

                runOnUiThread(() -> {
                    Toast.makeText(IntakeActivity.this,
                            "Success! Vehicle registered. Salvageable: " + is_salvageable,
                            Toast.LENGTH_LONG).show();

                    etInput.setText("");
                    cardPopup.setVisibility(View.GONE);
                    buttonVIN.setEnabled(true);
                    pendingVehicle = null;
                });

            } catch (Exception e) {
                String err = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(IntakeActivity.this, "Save Error: " + err, Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}