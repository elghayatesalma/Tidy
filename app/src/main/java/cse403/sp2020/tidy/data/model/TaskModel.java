package cse403.sp2020.tidy.data.model;

/**
 * Data model class that represents a Task Contains an Id field that is used for identification All
 * other fields are effectively arbitrary for backend
 */
public class TaskModel {
  private String taskId;
  private String name;
  private String description;
  private int priority;
  private String assignedUser;

  // Empty constructor for firestore
  public TaskModel() {}

  public TaskModel(TaskModel other) {
    if (other != null) {
      this.taskId = other.taskId;
      this.name = other.name;
      this.description = other.description;
      this.priority = other.priority;
      this.assignedUser = other.assignedUser;
    }
  }

  public String getTaskId() {
    return taskId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getPriority() {
    return priority;
  }

  public String getAssignedUser() {
    return assignedUser;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setAssignedUser(String assignedUser) {
    this.assignedUser = assignedUser;
  }

  public boolean equals(TaskModel other) {
    if (other == null) {
      return false;
    }

    if (other.getTaskId() != null) {
      return other.getTaskId().equals(getTaskId());
    } else {
      // Both are null
      return getTaskId() == null;
    }
  }
}
