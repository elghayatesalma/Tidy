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
  private List<String> preferences;

  // Empty constructor for firestore
  public UserModel() {
    this.preferences = new ArrayList<>();
  }

  public UserModel(UserModel other) {
    if (other != null) {
      this.firebaseId = other.firebaseId;
      this.firstName = other.firstName;
      this.lastName = other.lastName;

      if (other.preferences != null) {
        this.preferences = new ArrayList<>(other.preferences);
      } else {
        this.preferences = new ArrayList<>();
      }
    }
  }

  public String getFirebaseId() {
    return firebaseId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public List<String> getPreferences() {
    if (preferences != null) {
      return new ArrayList<>(preferences);
    } else {
      return null;
    }
  }

  public void setFirebaseId(String newId) {
    this.firebaseId = newId;
  }

  public void setFirstName(String newName) {
    this.firstName = newName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setPreferences(List<String> preferences) {
    if (preferences != null) {
      this.preferences = new ArrayList<>(preferences);
    }
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
