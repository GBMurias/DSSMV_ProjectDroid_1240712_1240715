package dto;

import java.util.ArrayList;
import java.util.List;

import model.Parts;
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
        mappedData = new Vehicle(dto.getVin(), dto.getMake(), dto.getModel(), dto.getYear(), dto.isSalvageable(), dto.getPartsList());
        return mappedData;
    }

    public static VehicleDTO vehicle2VehicleDTO(Vehicle vehicle) throws NullPointerException {
        VehicleDTO dto = null;
        dto = new VehicleDTO(vehicle.getVin(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus(), vehicle.getPartsList());
        return dto;
    }

    public static Parts partsDTO2Parts(PartsDTO dto) throws NullPointerException {
        Parts mappedData = null;
        mappedData = new Parts(dto.getName());
        return mappedData;
    }

    public static List<Parts> partsDTOList2PartsList(List<PartsDTO> dtoList) throws NullPointerException {
        List<Parts> partsList = new ArrayList<>();
        for (PartsDTO dto : dtoList){
            partsList.add(partsDTO2Parts(dto));
        }
        return partsList;
    }

    public static Parts storedPartsDTO2StoredParts(PartsDTO dto) throws NullPointerException {
        Parts mappedData = null;
        mappedData = new Parts(dto.getName(), dto.getAssociatedVIN(), dto.getAssociatedModel(), dto.getAssociatedMake(), dto.getAssociatedYear());
        return mappedData;
    }

    public static List<Parts> storedPartsDTOList2StoredPartsList(List<PartsDTO> dtoList) throws NullPointerException {
        List<Parts> storedPartsList = new ArrayList<>();
        for (PartsDTO dto : dtoList){
            storedPartsList.add(storedPartsDTO2StoredParts(dto));
        }
        return storedPartsList;
    }
    public static SlotDTO Slot2slotDTO(Slot x){
        Slot mapped_data = x;
        SlotDTO y = new SlotDTO(x.getRow(),x.getColumn(),x.getCurrent_vin());
        return y;
    }
}
