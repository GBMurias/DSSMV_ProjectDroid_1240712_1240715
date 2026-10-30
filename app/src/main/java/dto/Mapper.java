package dto;

import java.util.ArrayList;
import java.util.List;

import model.Slot;
import model.Vehicle;

public class Mapper {

    public static Slot slotDTO2Slot(SlotDTO dto) throws NullPointerException {
        Slot mappedData = null;
        mappedData = new Slot(dto.getSlotID(), dto.getCurrent_vin());
        return mappedData;
    }

    public static List<Slot> slotDTOList2SlotList(List<SlotDTO> dtoList) throws NullPointerException {
        List<Slot> slotList = new ArrayList<>();
        for (SlotDTO dto : dtoList) {
            slotList.add(slotDTO2Slot(dto));
        }
        return slotList;
    }

    public static Vehicle vehicleDTO2Vehicle(VehicleDTO dto) throws NullPointerException {
        Vehicle mappedData = null;
        mappedData = new Vehicle(dto.getVin(), dto.getMake(), dto.getModel(), dto.getYear(), dto.isSalvageable());
        return mappedData;
    }

    public static VehicleDTO vehicle2VehicleDTO(Vehicle vehicle) throws NullPointerException {
        VehicleDTO dto = null;
        dto = new VehicleDTO(vehicle.getVin(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus());
        return dto;
    }
}
