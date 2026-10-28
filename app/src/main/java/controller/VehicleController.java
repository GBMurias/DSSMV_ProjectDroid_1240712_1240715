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

    public VehicleDTO validateVehicleIntake(String vin, Context cxt) throws Exception {

        if (vin == null || vin.trim().isEmpty()) {
            throw new IllegalArgumentException("The VIN field can't be empty.");
        }

        VehicleDTO vehicleDTO = nhtsaService.fetchVehicleDetails(vin.trim());
        if (vehicleDTO == null) {
            throw new RuntimeException("Couldn't get vehicle data. invalid VIN.");
        }
        if ( vin.trim().length() != 17) {
            throw new IllegalArgumentException("Wrong VIN format - not enough characters.");
        }
        if (vehicleDTO.getYear() > 0 && vehicleDTO.getYear() < 1980) {
            throw new RuntimeException("Vehicle rejected: The system doesn't accept models prior to 1980.");
        }

        return vehicleDTO;
    }

    public void processVehicleIntake(Context cxt, VehicleDTO vehicleDTO) throws Exception {
        if (vehicleDTO == null) {
            throw new IllegalArgumentException("Vehicle data cannot be null.");
        }

        boolean isRegistered = vehicleService.registerIntake(cxt, vehicleDTO);
        if (!isRegistered) {
            Log.e(TAG, "Error while registering vehicle in the data base.");
            throw new RuntimeException("Error while registering vehicle in the data base.");
        }
    }
}
