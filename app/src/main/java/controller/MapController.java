package controller;

import static dto.Mapper.Slot2slotDTO;
import static dto.Mapper.slotDTO2Slot;
import static dto.Mapper.storedPartsList2StoredPartsDTOList;
import static dto.Mapper.slotDTOList2SlotList;
import static dto.Mapper.vehicle2VehicleDTO;
import static dto.Mapper.vehicleDTO2Vehicle;

import android.content.Context;
import android.util.Log;

import java.util.List;

import dto.VehicleDTO;
import model.Parts;
import model.Slot;
import model.Vehicle;
import service.MapService;
import service.PartsService;
import dto.SlotDTO;
import service.VehicleService;

public class MapController {

    private static final String TAG = "MapCtrl";
    private final MapService mapService;
    private final VehicleService vehicleService;

    private final PartsService partsService;

    public MapController() {
        this.mapService = new MapService();
        this.partsService = new PartsService();
        this.vehicleService = new VehicleService();
    }

    public List<Slot> processMapSlots(Context ctx) throws Exception {

        List<SlotDTO> slotDTOs = mapService.fetchMapSlots(ctx);
        List<Slot> slots = slotDTOList2SlotList(slotDTOs);

        return slots;   // pode vir vazia, não é erro

    }

    public void partsFromMap2Inventory (Context ctx, List<Parts> partsList) throws Exception {
        if (partsList == null || partsList.isEmpty()) {
            throw new IllegalArgumentException("Parts data cannot be null.");
        }

        boolean isRegistered = partsService.registerParts2Inventory(ctx, storedPartsList2StoredPartsDTOList(partsList));
        if (!isRegistered) {
            Log.e(TAG, "Error while registering parts in the inventory.");
            throw new RuntimeException("Error while registering vehicle in the data base.");
        }
    }

    public boolean registerVIN2Slot(Context ctx, Slot slot){
        boolean success = mapService.registerVIN2Slot(ctx,Slot2slotDTO(slot));
        return success;
    }
    public Vehicle getVehicle(Context context, String VIN)throws Exception {
        if(VIN == null) { throw new IllegalArgumentException("VIN cannot be null.");}

        VehicleDTO dto = vehicleService.fetchVehicle(context,VIN);
        Vehicle vehicle = vehicleDTO2Vehicle(dto);

        return vehicle;
    }

    public void moveIntake2History(Context ctx, Vehicle vehicle) throws Exception {
        if (vehicle == null || vehicle.getVin() == null) {
            throw new IllegalArgumentException("Vehicle data cannot be null.");
        }

        boolean isRegistered = vehicleService.registerIntake(ctx, vehicle2VehicleDTO(vehicle), "history");
        if (!isRegistered) {
            Log.e(TAG, "Error while moving intake to History.");
            throw new RuntimeException("Error while moving intake to History.");
        }
    }

    public void updateIntakeParts(Context ctx, String vin, List<Parts> partsList) throws Exception {
        if (partsList == null) {
            throw new IllegalArgumentException("Parts data cannot be null.");
        }
        boolean isUpdated = vehicleService.updateVehicleParts(ctx, vin, storedPartsList2StoredPartsDTOList(partsList));
        if (!isUpdated) {
            Log.e(TAG, "Error while updating vehicle parts.");
            throw new RuntimeException("Error while updating vehicle parts.");
        }
    }

    public void deleteIntake(Context ctx, String vin) throws Exception {
        if (vin == null){
            throw new IllegalArgumentException("VIN cannot be null.");
        }

        boolean isDeleted = vehicleService.deleteVehicle(ctx, vin);
        if (!isDeleted) {
            Log.e(TAG, "Error while deleting vehicle.");
            throw new RuntimeException("Error while deleting vehicle.");
        }
    }
}