package controller;

import static dto.Mapper.Slot2slotDTO;
import static dto.Mapper.slotDTOList2SlotList;
import static dto.Mapper.storedPartsList2StoredPartsDTOList;
import static dto.Mapper.vehicle2VehicleDTO;
import static dto.Mapper.vehicleDTO2Vehicle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import dto.SlotDTO;
import dto.VehicleDTO;
import model.Parts;
import model.Slot;
import model.Vehicle;
import service.MapService;
import service.PartsService;
import service.VehicleService;

public class MapController {

    private static final String TAG = "MapCtrl";
    private static final MapService mapService = new MapService();
    private static final PartsService partsService = new PartsService();
    private static final VehicleService vehicleService = new VehicleService();

    public interface SlotsCallback {
        void onSuccess(List<Slot> slots);
        void onError(String message);
    }

    public interface VehicleCallback {
        void onSuccess(Vehicle vehicle);
        void onError(String message);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface MovePartsCallback {
        void onSuccess(boolean vehicleScrapped);
        void onError(String message);
    }

    public static void processMapSlots(final Activity activity, final SlotsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<SlotDTO> slotDTOs = mapService.fetchMapSlots(activity);
                    final List<Slot> slots = slotDTOList2SlotList(slotDTOs); // may be empty, not an error

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(slots);
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Error while loading map data: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public static void registerVIN2Slot(final Activity activity, final Slot slot, final SaveCallback callback) {
        if (slot == null) {
            callback.onError("Slot data cannot be null.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = mapService.registerVIN2Slot(activity, Slot2slotDTO(slot));
                    if (!success) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Error while updating the slot in the database.");
                            }
                        });
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Error updating slot: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public static void getVehicle(final Activity activity, final String vin, final VehicleCallback callback) {
        if (vin == null) {
            callback.onError("VIN cannot be null.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Vehicle vehicle = fetchVehicle(activity, vin);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(vehicle);
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG, "Error fetching vehicle", e);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public static void sendVehicleToHistory(final Activity activity, final Slot slot, final String vin,
                                            final SaveCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scrapVehicleAndEmptySlot(activity, slot, vin);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG, "Error sending vehicle to history", e);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public static void sendAllPartsAndEmptySlot(final Activity activity, final Slot slot, final String vin,
                                                final SaveCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Vehicle vehicle = fetchVehicle(activity, vin);

                    List<Parts> toInventory = new ArrayList<>();
                    List<Parts> vehicleParts = vehicle.getPartsList();
                    if (vehicleParts != null) {
                        for (Parts p : vehicleParts) {
                            toInventory.add(new Parts(p.getName(), vehicle.getVin(),
                                    vehicle.getModel(), vehicle.getMake(), vehicle.getYear()));
                        }
                    }

                    if (!toInventory.isEmpty()) {
                        // a) Add to inventory
                        partsFromMap2Inventory(activity, toInventory);

                        // b) Only then leave the vehicle without parts
                        updateIntakeParts(activity, vin, new ArrayList<Parts>());
                    }

                    // c) Vehicle -> history and slot emptied
                    scrapVehicleAndEmptySlot(activity, slot, vin);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG, "Error sending all parts to inventory", e);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public static void movePartsToInventory(final Activity activity, final Slot slot, final String vin,
                                            final List<String> selectedNames, final MovePartsCallback callback) {
        if (selectedNames == null || selectedNames.isEmpty()) {
            callback.onError("No parts selected.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Vehicle vehicle = fetchVehicle(activity, vin);

                    List<Parts> toInventory = new ArrayList<>();
                    List<Parts> remaining = new ArrayList<>();
                    splitPartsBySelection(vehicle, selectedNames, toInventory, remaining);

                    if (toInventory.isEmpty()) {
                        throw new RuntimeException("Peças não encontradas no veículo.");
                    }

                    // a) Add to inventory
                    partsFromMap2Inventory(activity, toInventory);

                    // b) Only then remove from the vehicle
                    updateIntakeParts(activity, vin, remaining);

                    // c) If the vehicle has no parts left -> remove from slot (user already confirmed)
                    final boolean vehicleScrapped = remaining.isEmpty();
                    if (vehicleScrapped) {
                        scrapVehicleAndEmptySlot(activity, slot, vin);
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(vehicleScrapped);
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG, "Error moving parts to inventory", e);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private static Vehicle fetchVehicle(Context ctx, String vin) {
        VehicleDTO dto = vehicleService.fetchVehicle(ctx, vin);
        if (dto == null) {
            throw new RuntimeException("Vehicle not found.");
        }
        return vehicleDTO2Vehicle(dto);
    }

    private static void scrapVehicleAndEmptySlot(Context ctx, Slot slot, String vin) throws Exception {
        Vehicle vehicle = fetchVehicle(ctx, vin);

        moveIntake2History(ctx, vehicle);
        deleteIntake(ctx, vehicle.getVin());

        slot.setCurrent_vin("");
        boolean ok = mapService.registerVIN2Slot(ctx, Slot2slotDTO(slot));
        if (!ok) throw new RuntimeException("Não foi possível esvaziar o slot.");
    }

    private static void splitPartsBySelection(Vehicle vehicle, List<String> selectedNames,
                                              List<Parts> toInventory, List<Parts> remaining) {
        List<String> pending = new ArrayList<>(selectedNames);

        for (Parts p : vehicle.getPartsList()) {
            if (pending.remove(p.getName())) {
                toInventory.add(new Parts(p.getName(), vehicle.getVin(),
                        vehicle.getModel(), vehicle.getMake(), vehicle.getYear()));
            } else {
                remaining.add(p);
            }
        }
    }

    private static void partsFromMap2Inventory(Context ctx, List<Parts> partsList) throws Exception {
        if (partsList == null || partsList.isEmpty()) {
            throw new IllegalArgumentException("Parts data cannot be null.");
        }

        boolean isRegistered = partsService.registerParts2Inventory(ctx, storedPartsList2StoredPartsDTOList(partsList));
        if (!isRegistered) {
            Log.e(TAG, "Error while registering parts in the inventory.");
            throw new RuntimeException("Error while registering parts in the inventory.");
        }
    }

    private static void moveIntake2History(Context ctx, Vehicle vehicle) throws Exception {
        if (vehicle == null || vehicle.getVin() == null) {
            throw new IllegalArgumentException("Vehicle data cannot be null.");
        }

        boolean isRegistered = vehicleService.registerIntake(ctx, vehicle2VehicleDTO(vehicle), "history");
        if (!isRegistered) {
            Log.e(TAG, "Error while moving intake to History.");
            throw new RuntimeException("Error while moving intake to History.");
        }
    }

    private static void updateIntakeParts(Context ctx, String vin, List<Parts> partsList) throws Exception {
        if (partsList == null) {
            throw new IllegalArgumentException("Parts data cannot be null.");
        }

        boolean isUpdated = vehicleService.updateVehicleParts(ctx, vin, storedPartsList2StoredPartsDTOList(partsList));
        if (!isUpdated) {
            Log.e(TAG, "Error while updating vehicle parts.");
            throw new RuntimeException("Error while updating vehicle parts.");
        }
    }

    private static void deleteIntake(Context ctx, String vin) throws Exception {
        if (vin == null) {
            throw new IllegalArgumentException("VIN cannot be null.");
        }

        boolean isDeleted = vehicleService.deleteVehicle(ctx, vin);
        if (!isDeleted) {
            Log.e(TAG, "Error while deleting vehicle.");
            throw new RuntimeException("Error while deleting vehicle.");
        }
    }
}