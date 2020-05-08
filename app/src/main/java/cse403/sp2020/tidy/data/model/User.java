package cse403.sp2020.tidy.data.model;

import java.util.ArrayList;

public class User {
  String userId;
  String userName;
  ArrayList<Task> preferences;

  // TODO: Need to figure out how to import FirebaseUser in order to construct these.
  //    public User(FirebaseUser user, ArrayList<Task> preferences) {
  //        this.userId = user.getUid;
  //        this.username = user.getDisplayName();
  //        this.preferences = preferences;
  //    }

  public ArrayList<Task> getPreferences() {
    return preferences;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setPreferences(ArrayList<Task> preferences) {
    this.preferences = preferences;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
