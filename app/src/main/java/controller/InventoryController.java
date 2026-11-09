package controller;

import android.app.Activity;

import java.util.List;

import dto.PartsDTO;
import model.Parts;
import service.PartsService;

import static dto.Mapper.storedPartsDTOList2StoredPartsList;

public class InventoryController {

    private static final PartsService partsService = new PartsService();

    public interface InventoryCallback {
        void onSuccess(List<Parts> parts);
        void onError(String message);
    }

    public static void processInventoryInfo(final Activity activity, final InventoryCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PartsDTO> inventoryInfo = partsService.fetchPartsInfo(activity, "inventory");
                    if (inventoryInfo == null || inventoryInfo.isEmpty()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("No parts were found in the inventory.");
                            }
                        });
                        return;
                    }

                    final List<Parts> parts = storedPartsDTOList2StoredPartsList(inventoryInfo);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(parts);
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Error loading inventory: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}