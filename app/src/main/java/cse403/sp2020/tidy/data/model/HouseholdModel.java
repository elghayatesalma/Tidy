package cse403.sp2020.tidy.data.model;

public class HouseholdModel {
  private long householdNum;

  // Empty constructor for firestore
  public HouseholdModel() {}

  public HouseholdModel(long householdNum) {
    this.householdNum = householdNum;
  }

  public long getHouseholdNum(){
    return householdNum;
  }

  public boolean equals(HouseholdModel other) {
    if (other != null) {
      return other.getHouseholdNum() == getHouseholdNum();
    } else {
      return false;
    }
  }
}
