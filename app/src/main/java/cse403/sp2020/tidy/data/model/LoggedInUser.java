package cse403.sp2020.tidy.data.model;

/** Data class that captures user information for logged in users retrieved from LoginRepository */
public class LoggedInUser {

  private String userId;
  private String displayName;

  /**
   * Constructor with given id and name
   * @param userId loggedInUser Id
   * @param displayName name of the user
   */
  public LoggedInUser(String userId, String displayName) {
    this.userId = userId;
    this.displayName = displayName;
  }

  /**
   * Returns the user's id
   * @return user's id
   */
  public String getUserId() {
    return userId;
  }
  /**
   * Returns the user's name
   * @return user's name
   */
  public String getDisplayName() {
    return displayName;
  }

  // test comment by kcee
}
