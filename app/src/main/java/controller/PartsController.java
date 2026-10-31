package controller;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import dto.PartsDTO;
import model.Parts;
import service.PartsService;
import static dto.Mapper.partsDTOList2PartsList;
import static dto.Mapper.storedPartsDTOList2StoredPartsList;

public class PartsController {

    private final PartsService partsService;

    public PartsController() { this.partsService = new PartsService(); }

    public List<Parts> processPartsInfo(Context cxt) throws Exception {
        List<PartsDTO> partsInfo = partsService.fetchPartsInfo(cxt, "parts");
        if (partsInfo == null || partsInfo.isEmpty()) {
            throw new RuntimeException("No parts were found in the data base.");
        }
        return partsDTOList2PartsList(partsInfo);
    }

    public List<Parts> processInventoryInfo(Context cxt) throws Exception {
        List<PartsDTO> inventoryInfo = partsService.fetchPartsInfo(cxt, "inventory");
        if (inventoryInfo == null || inventoryInfo.isEmpty()) {
            throw new RuntimeException("No parts were found in the inventory.");
        }
        return storedPartsDTOList2StoredPartsList(inventoryInfo);
    }
}
