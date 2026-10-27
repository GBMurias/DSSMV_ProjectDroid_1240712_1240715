package controller;

import android.content.Context;
import java.util.List;
import service.MapService;
import dto.SlotDTO;
public class MapController {

    private final MapService mapService;

    public MapController() {
        this.mapService = new MapService();
    }

    public List<SlotDTO> processMapSlots(Context cxt) throws Exception {
        List<SlotDTO> slots = mapService.fetchMapSlots(cxt);

        if (slots == null || slots.isEmpty()) {
            throw new RuntimeException("Não foram encontrados slots na base de dados.");
        }

        return slots;
    }
}