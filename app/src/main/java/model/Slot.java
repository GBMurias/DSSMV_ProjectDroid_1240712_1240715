package model;

public class Slot {
    private Integer row;
    private String column;
    private String current_vin;

    public Slot(Integer row, String column, String current_vin) {
        this.row = row;
        this.column = column;
        this.current_vin = current_vin;
    }

    public void setRow(Integer row) { this.row = row; }
    public void setColumn(String column) { this.column = column; }
    public void setCurrent_vin(String current_vin) { this.current_vin = current_vin; }

    public Integer getRow() {return row; }
    public String getColumn() {return column; }
    public String getCurrent_vin() { return current_vin; }

}
