package controller;

import dto.VehicleDTO;
import service.NHTSAService;
import service.VehicleService;
import android.content.Context;
public class VehicleController {
    private final NHTSAService nhtsaService = new NHTSAService();
    private final VehicleService vehicleService = new VehicleService();

    public VehicleDTO processVehicleIntake(String vin, Context cxt) throws Exception {

        if (vin == null || vin.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo VIN não pode estar vazio.");
        }

        // 1. Obter dados da NHTSA
        VehicleDTO vehicleDTO = nhtsaService.fetchVehicleDetails(vin.trim());
        if (vehicleDTO == null) {
            throw new RuntimeException("Não foi possível obter os dados do veículo. VIN inválido.");
        }

        // 2. Regra de negócio
        if (vehicleDTO.getYear() > 0 && vehicleDTO.getYear() < 1990) {
            throw new RuntimeException("Veículo rejeitado: O parque não aceita veículos anteriores a 1990.");
        }

        // 3. Guardar na BD
        boolean isRegistered = vehicleService.registerIntake(cxt, vehicleDTO);
        if (!isRegistered) {
            throw new RuntimeException("Falha ao registar o veículo na base de dados.");
        }

        // Sucesso: retorna diretamente o DTO do carro!
        return vehicleDTO;
    }
}
