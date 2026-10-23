package controller;

import dto.VehicleDTO;
import service.NHTSAService;
import service.VehicleService;
import android.content.Context;
import android.util.Log;

public class VehicleController {
    private static final String TAG = "VehicleCtrl";
    private final NHTSAService nhtsaService = new NHTSAService();
    private final VehicleService vehicleService = new VehicleService();

    public VehicleDTO processVehicleIntake(String vin, Context cxt) throws Exception {

        if (vin == null || vin.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo VIN não pode estar vazio.");
        }

        VehicleDTO vehicleDTO = nhtsaService.fetchVehicleDetails(vin.trim());
        if (vehicleDTO == null) {
            throw new RuntimeException("Não foi possível obter os dados do veículo. VIN inválido.");
        }
        if ( vin.trim().length() != 17) {
            throw new IllegalArgumentException("Numero de caracters do VIN errado.");
        }


        if (vehicleDTO.getYear() > 0 && vehicleDTO.getYear() < 1990) {
            throw new RuntimeException("Veículo rejeitado: O parque não aceita veículos anteriores a 1990.");
        }

        boolean isRegistered = vehicleService.registerIntake(cxt, vehicleDTO);
        if (!isRegistered) {
            Log.e(TAG, "AQUI");
            throw new RuntimeException("Falha ao registar o veículo na base de dados.");
        }


        return vehicleDTO;
    }
}
