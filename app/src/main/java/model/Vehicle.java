package model;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {
    private String VIN; // Matrícula ou VIN
    private String brand;
    private String model;
    private List<VehiclePart> parts;

    public Vehicle(String VIN, String brand, String model) {
        this.VIN = VIN;
        this.brand = brand;
        this.model = model;
        this.parts = new ArrayList<>();
    }

    public void addPart(VehiclePart part) {
        this.parts.add(part);
    }
}
