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

    public Integer getRow() { return row; }

    public String getColumn() { return column; }

    public String getCurrent_vin() { return current_vin; }
}
