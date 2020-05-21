package cse403.sp2020.tidy.data.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Data model class that represents a User Contains an Id field that is used for identification All
 * other fields are effectively arbitrary for backend
 */
public class UserModel {
  private String firebaseId;
  private String firstName;
  private String lastName;
  private List<String> chorePreferences;

  // Empty constructor for firestore
  public UserModel() {}

  public UserModel(UserModel other) {
    if (other != null) {
      this.firebaseId = other.firebaseId;
      this.firstName = other.firstName;
      this.lastName = other.lastName;

      if (other.chorePreferences != null) {
        this.chorePreferences = new ArrayList<>(other.chorePreferences);
      }
    }
  }

  public String getFirebaseId() { return firebaseId; }

  public String getFirstName() { return firstName; }

  public String getLastName() { return lastName; }

  public List<String> getChorePreference() {
    if (chorePreferences != null) {
      return new ArrayList<>(chorePreferences);
    } else {
      return null;
    }
  }

  public void setFirebaseId(String newId) { this.firebaseId = newId; }

  public void setFirstName(String newName) { this.firstName = newName; }

  public void setLastName(String lastName) { this.lastName = lastName; }

  public void setChorePreferences(List<String> chorePreferences) {
    this.chorePreferences = new ArrayList<>(chorePreferences);
  }

  public boolean equals(UserModel other) {
    if (other == null) {
      return false;
    }
    if (other.getFirebaseId() != null) {
      return other.getFirebaseId().equals(getFirebaseId());
    } else {
      // Both are null
      return getFirebaseId() == null;
    }
  }
}
