package cse403.sp2020.tidy.data.model;

import java.util.Date;
import java.util.HashMap;

public class Task {
  private String taskName;
  private String description;
  private int priority; // Int bound between
  private Date dueDate;
  private User assignedTo;
  private Boolean markedCompleted;
  private HashMap<User, Boolean> previousAssignments;

  public Task(
      String taskName,
      String description,
      int priority,
      Date dueDate,
      User assignedTo,
      Boolean markedCompleted,
      HashMap<User, Boolean> previousAssignments) {}
}
