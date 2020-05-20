package cse403.sp2020.tidy.data.model;
/** Data structure representing a task */
public class TaskModel {
  private String taskId;
  private String name;
  private String description;
  private int priority;

  /** Empty constructor for firestore */
  public TaskModel() {}

  /**
   * Constructor for Task with given name, description, and priority
   *
   * @param name name of the task
   * @param description description of task activity
   * @param priority integer representing how important the task is
   */
  public TaskModel(String name, String description, int priority) {
    this.taskId = null;
    this.name = name;
    this.description = description;
    this.priority = priority;
  }
  /**
   * Returns the id of the task
   *
   * @return the task's id
   */
  public String getTaskId() {
    return taskId;
  }
  /**
   * Sets the id of this task to the given id
   *
   * @param taskId the id to change to
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  /**
   * Returns the name of the task
   *
   * @return the task's name
   */
  public String getName() {
    return name;
  }
  /**
   * Returns the description of the task
   *
   * @return the task's description
   */
  public String getDescription() {
    return description;
  }
  /**
   * Returns the priority of the task
   *
   * @return the task's priority
   */
  public int getPriority() {
    return priority;
  }
}
