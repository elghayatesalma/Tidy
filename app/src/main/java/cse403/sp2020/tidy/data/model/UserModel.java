package cse403.sp2020.tidy.data.model;

public class UserModel {
  private String firebaseId;
  private String firstName;
  private String lastName;

  // Empty constructor for firestore
  public UserModel() {}

  public UserModel(String firebaseId, String firstName, String lastName) {
    this.firebaseId = firebaseId;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getFirebaseId() {
    return firebaseId;
  }
}
