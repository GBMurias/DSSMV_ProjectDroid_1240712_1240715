package controller;

import android.content.Context;

import java.util.ArrayList;

import service.InfoService;

public class InfoController {

    private final InfoService infoService;

    public InfoController() {
        this.infoService = new InfoService();
    }

    public ArrayList<String> processPartsInfo(Context cxt) throws Exception {
        ArrayList<String> partsInfo = infoService.fetchSimpleInfo(cxt, "parts");
        if (partsInfo == null || partsInfo.isEmpty()) {
            throw new RuntimeException("No parts were found in the data base.");
        }
        return partsInfo;
    }

    public ArrayList<String> processInventoryInfo(Context cxt) throws Exception {
        ArrayList<String> inventoryInfo = infoService.fetchSimpleInfo(cxt, "inventory");
        if (inventoryInfo == null || inventoryInfo.isEmpty()) {
            throw new RuntimeException("No parts were found in the data base.");
        }
        return inventoryInfo;
    }
}
