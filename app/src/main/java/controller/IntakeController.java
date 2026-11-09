package controller;

import android.app.Activity;
import android.util.Log;
import dto.VehicleDTO;
import model.Vehicle;
import service.NHTSAService;
import service.VehicleService;
import java.util.List;
import dto.PartsDTO;
import model.Parts;
import service.PartsService;
import static dto.Mapper.partsDTOList2PartsList;
import static dto.Mapper.vehicle2VehicleDTO;
import static dto.Mapper.vehicleDTO2Vehicle;

public class IntakeController {
    private static final String TAG = "VehicleCtrl";
    private static final NHTSAService nhtsaService = new NHTSAService();
    private static final VehicleService vehicleService = new VehicleService();
    private static final PartsService partsService = new PartsService();

    public interface VehicleCallback {
        void onSuccess(Vehicle vehicle);
        void onError(String message);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface PartsCallback {
        void onSuccess(List<Parts> parts);
        void onError(String message);
    }

    public static void validateVehicleIntake(Activity activity, String vin, VehicleCallback callback) {
        if (vin == null || vin.trim().isEmpty()) {
            callback.onError("The VIN field can't be empty.");
            return;
        }
        if (vin.trim().length() != 17) {
            callback.onError("Wrong VIN format - not enough characters.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    VehicleDTO vehicleDTO = nhtsaService.fetchVehicleDetails(vin.trim());
                    if (vehicleDTO == null) {
                        activity.runOnUiThread(() ->
                                callback.onError("Couldn't get vehicle data. Invalid VIN."));
                        return;
                    }
                    if (vehicleDTO.getYear() > 0 && vehicleDTO.getYear() < 1980) {
                        activity.runOnUiThread(() ->
                                callback.onError("Vehicle rejected: The system doesn't accept models prior to 1980."));
                        return;
                    }

                    Vehicle vehicle = vehicleDTO2Vehicle(vehicleDTO);
                    activity.runOnUiThread(() -> callback.onSuccess(vehicle));
                } catch (Exception e) {
                    activity.runOnUiThread(() ->
                            callback.onError("Error validating vehicle: " + e.getMessage()));
                }
            }
        }).start();
    }

    public static void processVehicleIntake(Activity activity, Vehicle vehicle, SaveCallback callback) {
        if (vehicle == null) {
            callback.onError("Vehicle data cannot be null.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isRegistered = vehicleService.registerIntake(activity, vehicle2VehicleDTO(vehicle), "intakes");
                    if (!isRegistered) {
                        Log.e(TAG, "Error while registering vehicle in the data base.");
                        activity.runOnUiThread(() ->
                                callback.onError("Error while registering vehicle in the data base."));
                        return;
                    }
                    activity.runOnUiThread(callback::onSuccess);
                } catch (Exception e) {
                    activity.runOnUiThread(() ->
                            callback.onError("Error saving vehicle: " + e.getMessage()));
                }
            }
        }).start();
    }

    public static void processPartsInfo(Activity activity, PartsCallback callback) {
        new Thread(() -> {
            try {
                List<PartsDTO> partsInfo = partsService.fetchPartsInfo(activity, "parts");
                if (partsInfo == null || partsInfo.isEmpty()) {
                    activity.runOnUiThread(() ->
                            callback.onError("No parts were found in the data base."));
                    return;
                }

                List<Parts> parts = partsDTOList2PartsList(partsInfo);
                activity.runOnUiThread(() -> callback.onSuccess(parts));
            } catch (Exception e) {
                activity.runOnUiThread(() ->
                        callback.onError("Error loading parts: " + e.getMessage()));
            }
        }).start();
    }
}