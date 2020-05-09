package cse403.sp2020.tidy.data.model;

import java.util.ArrayList;

public class Household {
  private String houseId;
  private ArrayList<Task> tasks;
  private ArrayList<User> users;

  public Household(String houseId, ArrayList<Task> tasks, ArrayList<User> users) {
    this.houseId = houseId;
    this.tasks = tasks;
    this.users = users;
  }

  public String getHouseId() {
    return houseId;
  }

  public ArrayList<Task> getTasks() {
    return tasks;
  }

  public ArrayList<User> getUsers() {
    return users;
  }
}