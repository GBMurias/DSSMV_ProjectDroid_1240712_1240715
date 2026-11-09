package handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import dto.SlotDTO;
import dto.VehicleDTO;
import dto.PartsDTO;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {


    public static VehicleDTO deserializeNHTSA(JSONObject vehicleData, String vin) throws JSONException {
        String make = vehicleData.optString("Make", "Unknown");
        String model = vehicleData.optString("Model", "Unknown");
        String yearStr = vehicleData.optString("ModelYear", "0");
        int year = 0;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException ignored) {}
        return new VehicleDTO(vin, make, model, year);
    }

    public static JSONObject serializeVehicle(VehicleDTO dto, String infoType) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        if(infoType.equals("intakes")) {
            jsonBody.put("VIN", dto.getVin());
            jsonBody.put("Make", dto.getMake());
            jsonBody.put("Model", dto.getModel());
            jsonBody.put("Year", dto.getYear());
            jsonBody.put("is_salvageable", dto.isSalvageable());

            if (dto.getPartsList() != null && !dto.getPartsList().isEmpty()) {
                JSONArray partsArray = new JSONArray();
                for (PartsDTO part : dto.getPartsList()) {
                    JSONObject partObj = new JSONObject();
                    partObj.put("name", part.getName());
                    partObj.put("associatedVIN", part.getAssociatedVIN());
                    partObj.put("associatedModel", part.getAssociatedModel());
                    partObj.put("associatedMake", part.getAssociatedMake());
                    partObj.put("associatedYear", part.getAssociatedYear());
                    partsArray.put(partObj);
                }
                jsonBody.put("parts", partsArray);
            }
        } else if(infoType.equals("history")){
            jsonBody.put("VIN", dto.getVin());
            jsonBody.put("Make", dto.getMake());
            jsonBody.put("Model", dto.getModel());
            jsonBody.put("Year", dto.getYear());
        }
        return jsonBody;
    }

    public static VehicleDTO deserializeVehicle(JSONObject vehicleJson) throws JSONException {
        VehicleDTO dto = new VehicleDTO();
        if (vehicleJson.has("VIN")) dto.setVin(vehicleJson.getString("VIN"));
        if (vehicleJson.has("Make")) dto.setMake(vehicleJson.getString("Make"));
        if (vehicleJson.has("Model")) dto.setModel(vehicleJson.getString("Model"));
        if (vehicleJson.has("Year")) dto.setYear(vehicleJson.getInt("Year"));
        if (vehicleJson.has("is_salvageable")) dto.setSalvageable(vehicleJson.getBoolean("is_salvageable"));

        if (vehicleJson.has("parts")) {
            JSONArray partsArray = vehicleJson.getJSONArray("parts");
            List<PartsDTO> partsList = new java.util.ArrayList<>();
            for (int i = 0; i < partsArray.length(); i++) {
                JSONObject partObj = partsArray.getJSONObject(i);
                PartsDTO part = new PartsDTO();
                if (partObj.has("name")) part.setName(partObj.getString("name"));
                if (partObj.has("associatedVIN"))
                    part.setAssociatedVIN(partObj.getString("associatedVIN"));
                if (partObj.has("associatedModel"))
                    part.setAssociatedModel(partObj.getString("associatedModel"));
                if (partObj.has("associatedMake"))
                    part.setAssociatedMake(partObj.getString("associatedMake"));
                if (partObj.has("associatedYear"))
                    part.setAssociatedYear(partObj.getInt("associatedYear"));
                partsList.add(part);
            }
            dto.setPartsList(partsList);
        }
        return dto;
    }

    public static SlotDTO deserializeSlot(JSONObject obj) throws JSONException {
        Integer row = obj.optInt("row", 0);
        String column = obj.optString("column", "");
        String currentVin = obj.optString("current_vin", "");
        return new SlotDTO(row, column, currentVin);
    }

    public static List<SlotDTO> deserializeMap(String json) throws JSONException {
        List<SlotDTO> slotList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.optJSONObject(i);
            slotList.add(deserializeSlot(obj));
        }
        return slotList;
    }

    public static JSONObject serializeSlotCurrentVinField(SlotDTO dto) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("current_vin", dto.getCurrent_vin());
        return jsonBody;
    }

    public static List<PartsDTO> deserializeParts(JSONArray jsonArray, String infoType) throws JSONException {
        List<PartsDTO> partsList = new ArrayList<>();
        if (infoType.equals("parts")) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String partName = obj.getString("name");
                partsList.add(new PartsDTO(partName));
            }
        } else if (infoType.equals("inventory")) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String partName = obj.getString("name");
                String associatedVIN = obj.getString("associatedVIN");
                String associatedModel = obj.getString("associatedModel");
                String associatedMake = obj.getString("associatedMake");
                Integer associatedYear = obj.getInt("associatedYear");
                partsList.add(new PartsDTO(partName, associatedVIN, associatedModel, associatedMake, associatedYear));
            }
        }
        return partsList;
    }

    public static JSONArray serializeParts(List<PartsDTO> dtoList) throws JSONException {
        JSONArray partsArray = new JSONArray();
        for(PartsDTO dto : dtoList) {
            JSONObject part = new JSONObject();
            part.put("name", dto.getName());
            part.put("associatedVIN", dto.getAssociatedVIN());
            part.put("associatedModel", dto.getAssociatedModel());
            part.put("associatedMake", dto.getAssociatedMake());
            part.put("associatedYear", dto.getAssociatedYear());
            partsArray.put(part);
        }
        return partsArray;
    }

    /*public static JSONObject serializeParts4Update(List<PartsDTO> partsDtoList) throws JSONException {
        JSONArray partsArray = new JSONArray();
        if (partsDtoList != null) {
            for (PartsDTO partDto : partsDtoList) {
                JSONObject partObj = new JSONObject();
                partObj.put("name", partDto.getName());
                partObj.put("associatedVIN", partDto.getAssociatedVIN());
                partObj.put("associatedModel", partDto.getAssociatedModel());
                partObj.put("associatedMake", partDto.getAssociatedMake());
                partObj.put("associatedYear", partDto.getAssociatedYear());
                partsArray.put(partObj);
            }
        }
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("parts", partsArray);
        return jsonBody;
    }*/
}
