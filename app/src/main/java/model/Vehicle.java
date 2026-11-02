package model;

import java.util.List;

public class Vehicle {
    private String vin;
    private String make;
    private String model;
    private Integer year;
    private boolean is_salvageable;
    private List<Parts> partsList;

    public Vehicle(String vin, String make, String model, Integer year, boolean is_salvageable, List<Parts> partsList) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.is_salvageable = is_salvageable;
        this.partsList = partsList;
    }

    public void setModel(String model) {
        this.model = model;
    }
    public void setStatus(boolean status) { this.is_salvageable = status; }
    public void setPartsList(List<Parts> partsList) { this.partsList = partsList; }

    public String getVin() { return vin; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public boolean getStatus() { return is_salvageable; }
    public List<Parts> getPartsList() { return partsList; }
}
