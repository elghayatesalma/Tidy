package cse403.sp2020.tidy.data.model;

public class HouseholdModel {
  private String householdId;

  // Empty constructor for firestore
  public HouseholdModel() {}

  public HouseholdModel(String householdId) {
    this.householdId = householdId;
  }

  public String getHouseholdId() {
    return householdId;
  }

  public void setHouseholdId(String householdId) {
    this.householdId = householdId;
  }

  public boolean equals(HouseholdModel other) {
    if (other != null) {
      return other.getHouseholdId().equals(getHouseholdId());
    } else {
      return false;
    }
  }
}
