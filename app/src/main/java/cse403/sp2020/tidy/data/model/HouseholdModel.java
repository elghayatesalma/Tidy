package cse403.sp2020.tidy.data.model;

/**
 * Data model class that represents a household Contains an Id field that is used for identification
 * All other fields are effectively arbitrary for backend
 */
public class HouseholdModel {
  private String householdId;

  // Empty constructor for firestore
  public HouseholdModel() {}

  public HouseholdModel(HouseholdModel other) {
    if (other != null) {
      this.householdId = other.householdId;
    }
  }

  public String getHouseholdId() {
    return householdId;
  }

  public void setHouseholdId(String householdId) {
    this.householdId = householdId;
  }

  public boolean equals(HouseholdModel other) {
    if (other == null) {
      return false;
    }
    if (other.getHouseholdId() != null) {
      return other.getHouseholdId().equals(getHouseholdId());
    } else {
      // Both are null
      return getHouseholdId() == null;
    }
  }
}
