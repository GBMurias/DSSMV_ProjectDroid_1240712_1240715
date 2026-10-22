package model;

import com.google.gson.annotations.SerializedName;

public class Vehicle {
    @SerializedName("VIN")
    private String vin;

    @SerializedName("Make")
    private String make;

    @SerializedName("Model")
    private String model;

    @SerializedName("Year")
    private int year;

    @SerializedName("ParkSlot")
    private String parkSlot;

    @SerializedName("status")
    private boolean status;

    public Vehicle(String vin, String make, String model, int year, String parkSlot, boolean status) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.parkSlot = parkSlot;
        this.status = status;
    }

    public String getVin() { return vin; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getParkSlot() { return parkSlot; }
    public boolean getStatus() { return status; }

}
