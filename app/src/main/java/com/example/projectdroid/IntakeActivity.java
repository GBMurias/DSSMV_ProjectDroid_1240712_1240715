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
    private Button respectBotton; // Mantive o nome que tinhas no teu código

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);

        // 1. Inicializar as views do layout (ajusta os IDs se forem diferentes no teu XML)
        etInput = findViewById(R.id.etInput);
        respectBotton = findViewById(R.id.respectBotton); // Certifica-te que este é o ID do botão no teu XML

        // 2. Configurar o evento de clique do botão
        respectBotton.setOnClickListener(v -> {
            // Ler o VIN inserido pelo utilizador no momento do clique
            String vin = etInput.getText().toString().trim();

            // Validação visual rápida no ecrã
            if (vin.isEmpty()) {
                etInput.setError("Por favor, insere um VIN!");
                return;
            }

            // 3. Como o Controller faz chamadas de rede, abrimos a Thread aqui
            new Thread(() -> {
                VehicleController controller = new VehicleController();

                try {
                    // Executa o processo completo (NHTSA + Regras + Registo no RestDB)
                    // Passamos 'IntakeActivity.this' como o Context necessário
                    VehicleDTO vehicle = controller.processVehicleIntake(vin, IntakeActivity.this);

                    // Sucesso! Voltamos à thread principal para atualizar o ecrã
                    runOnUiThread(() -> {
                        Toast.makeText(IntakeActivity.this,
                                "Sucesso! " + vehicle.getMake() + " " + vehicle.getModel() + " registado.",
                                Toast.LENGTH_LONG).show();

                        // Opcional: limpar o campo após o sucesso
                        etInput.setText("");
                    });

                } catch (Exception e) {
                    // Erro (seja VIN inválido, falha de rede ou regra de negócio rejeitada)
                    String errorMessage = e.getMessage();

                    runOnUiThread(() -> {
                        Toast.makeText(IntakeActivity.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }
}