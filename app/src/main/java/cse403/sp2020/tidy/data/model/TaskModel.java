package cse403.sp2020.tidy.data.model;

import com.google.firebase.Timestamp;

/**
 * Data model class that represents a Task Contains an Id field that is used for identification All
 * other fields are effectively arbitrary for backend
 */
public class TaskModel {
  private String taskId;
  private String name;
  private String description;
  private int priority;
  private String assignedTo;
  private boolean completed;
  private Timestamp assignedDate;

  // Empty constructor for firestore
  public TaskModel() {}

  public TaskModel(TaskModel other) {
    if (other != null) {
      this.taskId = other.taskId;
      this.name = other.name;
      this.description = other.description;
      this.priority = other.priority;
      this.assignedTo = other.assignedTo;
      this.completed = other.completed;
      this.assignedDate = other.assignedDate;
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

  public String getAssignedTo() {
    return assignedTo;
  }

  public Timestamp getAssignedDate() {
    return assignedDate;
  }

  public void setAssignedDate(Timestamp assignedDate) {
    this.assignedDate = assignedDate;
  }

  public boolean isCompleted() {
    return completed;
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

  public void setAssignedTo(String assignedTo) {
    this.assignedTo = assignedTo;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
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
