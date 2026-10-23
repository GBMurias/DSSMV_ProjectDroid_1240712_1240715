package com.example.projectdroid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import controller.VehicleController;
import dto.VehicleDTO;

public class IntakeActivity extends AppCompatActivity {

    private EditText etInput;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);


        etInput = findViewById(R.id.etInput);
        button = findViewById(R.id.aaa);

                button.setOnClickListener(v -> {

            String vin = etInput.getText().toString().trim();


            if (vin.isEmpty()) {
                etInput.setError("Por favor, insere um VIN!");
                return;
            }

            new Thread(() -> {
                VehicleController controller = new VehicleController();

                try {

                    VehicleDTO vehicle = controller.processVehicleIntake(vin, IntakeActivity.this);

                    runOnUiThread(() -> {
                        Toast.makeText(IntakeActivity.this,
                                "Sucesso! " + vehicle.getMake() + " " + vehicle.getModel() + " registado.",
                                Toast.LENGTH_LONG).show();

                        etInput.setText("");
                    });

                } catch (Exception e) {
                    String errorMessage = e.getMessage();

                    runOnUiThread(() -> {
                        Toast.makeText(IntakeActivity.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }
}