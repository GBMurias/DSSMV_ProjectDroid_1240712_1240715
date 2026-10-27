package com.example.projectdroid;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import controller.MapController;
import dto.SlotDTO;

public class MapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        loadMapData();
    }
    private void loadMapData() {
        new Thread(() -> {
            MapController controller = new MapController();

            try {
                // Pede a lista de SlotDTO atualizada através da arquitetura Controller-Service
                List<SlotDTO> slots = controller.processMapSlots(MapActivity.this);

                // Atualiza a interface gráfica na thread principal
                runOnUiThread(() -> {
                    updateMapUI(slots);
                });

            } catch (Exception e) {
                String errorMsg = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this, "Erro ao carregar mapa: " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
public
