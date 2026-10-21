package model;

public class InTake {
    private String intakeId;
    private Vehicle vehicle;
    private MapLocation mapLocation;

    public InTake(String intakeId, Vehicle vehicle, MapLocation mapLocation) {
        this.intakeId = intakeId;
        this.vehicle = vehicle;
        this.mapLocation = mapLocation;
    }
}
