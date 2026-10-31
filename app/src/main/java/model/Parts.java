package model;

public class Parts {

    private String name;
    private String associatedVIN;
    private String associatedModel;
    private String associatedMake;
    private Integer associatedYear;

    public Parts(String name) {
        this.name = name;
    }

    public Parts(String name, String associatedVIN, String associatedModel, String associatedMake, Integer associatedYear) {
        this.name = name;
        this.associatedVIN = associatedVIN;
        this.associatedModel = associatedModel;
        this.associatedMake = associatedMake;
        this.associatedYear = associatedYear;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getName() { return name; }
    public String getAssociatedVIN() { return associatedVIN; }
    public String getAssociatedModel() { return associatedModel; }
    public String getAssociatedMake() { return associatedMake; }
    public Integer getAssociatedYear() { return associatedYear; }

    public void setAssociatedVIN(String associatedVIN) { this.associatedVIN = associatedVIN; }
    public void setAssociatedModel(String associatedModel) { this.associatedModel = associatedModel; }
    public void setAssociatedMake(String associatedMake) { this.associatedMake = associatedMake; }
    public void setAssociatedYear(Integer associatedYear) { this.associatedYear = associatedYear; }

}
