package controller;

import static dto.Mapper.slotDTOList2SlotList;
import static dto.Mapper.vehicleDTO2Vehicle;

import android.content.Context;

import com.example.projectdroid.MapActivity;

import java.util.List;

import dto.VehicleDTO;
import model.Slot;
import model.Vehicle;
import service.MapService;
import dto.SlotDTO;
import service.VehicleService;

public class MapController {

    private final MapService mapService;
    private final VehicleService vehicleService;

    public MapController() {
        this.mapService = new MapService();
        this.vehicleService = new VehicleService();
    }

    public List<Slot> processMapSlots(Context cxt) throws Exception {

        List<SlotDTO> slotDTOs = mapService.fetchMapSlots(cxt);
        List<Slot> slots = slotDTOList2SlotList(slotDTOs);

        if (slots == null || slots.isEmpty()) {
            throw new RuntimeException("No slots were found in the data base.");
        }

        return slots;
    }
    public boolean registerVIN2Slot(Context ctx,SlotDTO tempdto){
        boolean success = mapService.registerVIN2Slot(ctx, tempdto);
        return success;
    }
    public Vehicle getVehicle(Context context, String VIN)throws Exception {
        if(VIN == null){ throw new IllegalArgumentException("VIN cannot be null.");}

        VehicleDTO dto = vehicleService.fetchVehicle(context,VIN);

        Vehicle vehicle = vehicleDTO2Vehicle(dto);

        return vehicle;
    }
}