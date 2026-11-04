package model;

public class Slot {
    private String slotID;
    private String current_vin;

    private String column;

    private Integer row;

    public Slot(String slotID, String current_vin) {
        this.slotID = slotID;
        this.current_vin = current_vin;

        if (slotID != null && slotID.length() >= 2) {
            this.column = slotID.substring(0, 1);
            try {
                this.row = Integer.parseInt(slotID.substring(1));
            } catch (NumberFormatException e) {
                this.row = null;
            }
        }
    }
    public Slot(Integer row, String column,String current_vin){
        this.row = row;
        this.column = column;
        this.current_vin = current_vin;

    }

    public void setSlotID(String slotID) { this.slotID = slotID; }
    public void setCurrent_vin(String current_vin) { this.current_vin = current_vin; }

    public Integer getRow() { return row; }
    public String getColumn() { return column; }
    public String getSlotID() { return slotID; }
    public String getCurrent_vin() { return current_vin; }

    public boolean isOccupied() {
        return current_vin != null && !current_vin.trim().isEmpty() && !current_vin.equalsIgnoreCase("null");
    }
}