package cse403.sp2020.tidy.data.model;

import java.util.Date;
import java.util.HashMap;



public class Task {
    private static final int MAX_PRIORITY = 10;
    private static final int MIN_PRIORITY = 0;

    private String taskName;
    private String description;
    private int priority; // Int bound between MAX_PRIORITY and MIN_PRIORITY
    private Date dueDate;
    private User assignedTo;
    private Boolean markedCompleted;
    private HashMap<User, Boolean> previousAssignments;

    public Task(String taskName, String description, int priority, Date dueDate, User assignedTo, Boolean markedCompleted,
                HashMap<User, Boolean> previousAssignments) {
        // Possible checks to add/invariants to maintain
            // dueDate is in the future
            // Assigned to is a user in the house
            // previous assignments only talks about users in the house
            // priority is within
        this.taskName = taskName;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.markedCompleted = markedCompleted;
        this.previousAssignments = previousAssignments;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public Boolean getMarkedCompleted() {
        return markedCompleted;
    }

    public HashMap<User, Boolean> getPreviousAssignments() {
        return previousAssignments;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}


