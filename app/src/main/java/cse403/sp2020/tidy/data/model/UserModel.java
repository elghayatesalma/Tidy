package cse403.sp2020.tidy.data.model;

/**
 * Data structure representing a user
 */
public class UserModel {
  private String firebaseId;
  private String firstName;
  private String lastName;

  /**
   * Empty constructor for firestore
   */
  public UserModel() {}

  /**
   * Constructs a user with given id and first and last names
   * @param firebaseId if of the user
   * @param firstName first name of the user
   * @param lastName last name of the user
   */
  public UserModel(String firebaseId, String firstName, String lastName) {
    this.firebaseId = firebaseId;
    this.firstName = firstName;
    this.lastName = lastName;
  }
  /**
   * Constructs a User with the same id, first and last name as the given user
   * @param other another user to copy
   */
  public UserModel(UserModel other) {
    if (other != null) {
      this.firebaseId = other.firebaseId;
      this.firstName = other.firstName;
      this.lastName = other.lastName;
    }
  }
  /**
   * Returns the last name of the user
   * @return the user's last name
   */
  public String getLastName() {
    return lastName;
  }
  /**
   * Returns the first name of the user
   * @return the user's first name
   */
  public String getFirstName() {
    return firstName;
  }
  /**
   * Returns the id of the user
   * @return the user's id
   */
  public String getFirebaseId() {
    return firebaseId;
  }

  /**
   * Sets the id of this user to the given id
   * @param newId the id to change to
   */
  public void setFirebaseId(String newId) {
    this.firebaseId = newId;
  }
  /**
   * Sets the first name of this user to the given name
   * @param newName the name to change to
   */
  public void setFirstName(String newName) {
    this.firstName = newName;
  }
  /**
   * Determines if two users are equal
   * @param other the other user to compare with
   * @return Returns true if this user is equal to other false otherwise
   */
  public boolean equals(UserModel other) {
    if (other != null) {
      return other.getFirebaseId() == getFirebaseId();
    }
    return false;
  }
}
