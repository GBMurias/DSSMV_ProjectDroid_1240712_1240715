package model;

public class Vehicle {
    private String vin;

    private String make;

    private String model;

    private Integer year;

    private boolean is_salvageable;

    public Vehicle(String vin, String make, String model, Integer year, boolean is_salvageable) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.is_salvageable = is_salvageable;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }
    public void setMake(String make) {
        this.make = make;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public void setYear(Integer year) {
        this.year = year;
    }
    public void setStatus(boolean status) { this.is_salvageable = status; }

    public String getVin() { return vin; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public boolean getStatus() { return is_salvageable; }

}
