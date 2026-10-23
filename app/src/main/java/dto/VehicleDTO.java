package dto;
public class VehicleDTO {

    private String vin;

    private String make;

    private String model;

    private Integer year;

    public VehicleDTO(String vin, String make, String model, Integer year) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
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

    public String getVin() { return vin; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
}
