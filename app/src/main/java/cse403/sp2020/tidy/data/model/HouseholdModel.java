package cse403.sp2020.tidy.data.model;

/** Data structure representing a household */
public class HouseholdModel {
  private String householdId;

  /** Empty constructor for firestore */
  public HouseholdModel() {}

  /**
   * Constructs a Household with given id
   *
   * @param householdId the id for the new household
   */
  public HouseholdModel(String householdId) {
    this.householdId = householdId;
  }

  /**
   * Constructs a Household with the same id as the given household
   *
   * @param other another household with the desired id
   */
  public HouseholdModel(HouseholdModel other) {
    if (other != null) {
      this.householdId = other.householdId;
    }
  }

  /**
   * Returns the id of the household
   *
   * @return the household's id
   */
  public String getHouseholdId() {
    return householdId;
  }

  /**
   * Sets the id of this household to the given id
   *
   * @param householdId the id to change to
   */
  public void setHouseholdId(String householdId) {
    this.householdId = householdId;
  }

  /**
   * Determines if two households are equal
   *
   * @param other the other household to compare with
   * @return Returns true if this household is equal to other false otherwise
   */
  public boolean equals(HouseholdModel other) {
    if (other != null) {
      return other.getHouseholdId().equals(getHouseholdId());
    } else {
      return false;
    }
  }
}
