package controller;

import static dto.Mapper.vehicleDTOList2VehicleList;

import android.content.Context;

import java.util.List;
import dto.Mapper;
import service.HistoryService;
import model.Vehicle;
import dto.VehicleDTO;

public class HistoryController {
    private final HistoryService historyService;

    public HistoryController() {
        this.historyService = new HistoryService();
    }

    public List<Vehicle> getScrappedVehicles(Context ctx){
        List<VehicleDTO> vehicleDTOs = historyService.fetchScrappedVehicles(ctx);
        List<Vehicle> vehicles = vehicleDTOList2VehicleList(vehicleDTOs);

        if (vehicles == null || vehicles.isEmpty()) {
            throw new RuntimeException("No vehicles were found in the data base.");
        }

        return vehicles;
    }
}
