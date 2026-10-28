package dto;
public class VehicleDTO {

    private String vin;

    private String make;

    private String model;

    private Integer year;

    private boolean is_salvageable;

    public VehicleDTO(String vin, String make, String model, Integer year) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
    }

    public VehicleDTO(String vin, String make, String model, Integer year, boolean is_salvageable) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.is_salvageable = is_salvageable;
    }

    public void setSalvageable(boolean salvageable) {
        this.is_salvageable = salvageable;
    }

    public String getVin() { return vin; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public boolean isSalvageable() { return is_salvageable; }
}
