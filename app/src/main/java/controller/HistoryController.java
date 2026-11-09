package controller;

import static dto.Mapper.vehicleDTOList2VehicleList;
import android.app.Activity;
import java.util.List;
import dto.VehicleDTO;
import model.Vehicle;
import service.HistoryService;

public class HistoryController {

    private static final HistoryService historyService = new HistoryService();

    public interface HistoryCallback {
        void onSuccess(List<Vehicle> vehicles);
        void onError(String message);
    }

    public static void getScrappedVehicles(final Activity activity, final HistoryCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<VehicleDTO> vehicleDTOs = historyService.fetchScrappedVehicles(activity);
                    final List<Vehicle> vehicles = vehicleDTOList2VehicleList(vehicleDTOs);

                    if (vehicles == null || vehicles.isEmpty()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("No vehicles were found in the data base.");
                            }
                        });
                        return;
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(vehicles);
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Error loading history: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}
