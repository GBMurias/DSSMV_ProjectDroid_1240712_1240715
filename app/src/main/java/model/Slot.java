package model;

public class Slot {
    private String slotID;
    private String current_vin;

    public Slot(String slotID, String current_vin) {
        this.slotID = slotID;
        this.current_vin = current_vin;
    }

    public void setSlotID(String slotID) { this.slotID = slotID; }
    public void setCurrent_vin(String current_vin) { this.current_vin = current_vin; }

    public String getSlotID() { return slotID; }
    public String getCurrent_vin() { return current_vin; }
    public boolean isOccupied() {
        return current_vin != null && !current_vin.trim().isEmpty() && !current_vin.equalsIgnoreCase("null");
    }
}
