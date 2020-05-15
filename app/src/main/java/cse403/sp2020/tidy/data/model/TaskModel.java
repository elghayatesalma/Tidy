package cse403.sp2020.tidy.data.model;

/**
 * Data model class that represents a Task
 * Contains an Id field that is used for identification
 * All other fields are effectively arbitrary for backend
 */
public class TaskModel {
  private String taskId;
  private String name;
  private String description;
  private int priority;

  // Empty constructor for firestore
  public TaskModel() {}

  public TaskModel(String name, String description, int priority) {
    this.taskId = null;
    this.name = name;
    this.description = description;
    this.priority = priority;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
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
}
