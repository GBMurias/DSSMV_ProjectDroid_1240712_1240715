package model;

public class Vehicle {
    private String vin;

    private String make;

    private String model;

    private int year;

    private String parkSlot;

    private boolean status;

    public Vehicle(String vin, String make, String model, int year, String parkSlot, boolean status) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.parkSlot = parkSlot;
        this.status = status;
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

    public void setYear(int year) {
        this.year = year;
    }

    public void setParkSlot(String parkSlot) {
        this.parkSlot = parkSlot;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getVin() { return vin; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getParkSlot() { return parkSlot; }
    public boolean getStatus() { return status; }

}
