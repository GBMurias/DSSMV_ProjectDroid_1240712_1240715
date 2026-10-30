package controller;

import static dto.Mapper.slotDTOList2SlotList;

import android.content.Context;
import java.util.List;

import model.Slot;
import service.MapService;
import dto.SlotDTO;

public class MapController {

    private final MapService mapService;

    public MapController() {
        this.mapService = new MapService();
    }

    public List<Slot> processMapSlots(Context cxt) throws Exception {

        List<SlotDTO> slotDTOs = mapService.fetchMapSlots(cxt);
        List<Slot> slots = slotDTOList2SlotList(slotDTOs);

        if (slots == null || slots.isEmpty()) {
            throw new RuntimeException("No slots were found in the data base.");
        }

        return slots;
    }
}