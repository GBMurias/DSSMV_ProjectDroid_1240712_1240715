package dto;

public class SlotDTO {
    private Integer row;
    private String column;
    private String current_vin;

    public SlotDTO(Integer row, String column, String current_vin) {
        this.row = row;
        this.column = column;
        this.current_vin = current_vin;
    }

    public String getSlotID() { return column + row; }
    public String getCurrent_vin() { return current_vin; }
    public boolean isOccupied() {
        return current_vin != null && !current_vin.trim().isEmpty() && !current_vin.equalsIgnoreCase("null");
    }
}
