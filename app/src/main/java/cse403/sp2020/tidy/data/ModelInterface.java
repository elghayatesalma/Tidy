package cse403.sp2020.tidy.data;

import java.util.List;

import cse403.sp2020.tidy.data.model.Household;
import cse403.sp2020.tidy.data.model.Task;
import cse403.sp2020.tidy.data.model.User;

public class ModelInterface {
  // NOT NEEDED?
  public static User getUser(String userName, String password) {
    return null;
  }

  // NOT NEEDED?
  public static User signUpUser(String fName, String lName, String email, String password) {
    return null;
  }

  // Possibly needed?
  // Takes in a user object
  // Returns a household if the user exists and belongs to one
  // Returns null otherwise
  public static Household getHouseholdByUser(User user) {
    return null;
  }

  // Possibly needed?
  // Takes in the id of a household
  // Returns a household if the ID is valid
  // Returns null otherwise
  public static Household getHouseholdByID(String householdID) {
    return null;
  }

  // Takes in a household object
  // Returns a (potentially empty) list of users if household exists
  // Returns null if household doesn't exist
  public static List<User> getUsers(Household household) {
    return null;
  }

  // Takes in a household object
  // Returns a (potentially empty) list of tasks of household exists
  // Returns null if household doesn't exist
  public static List<Task> getTasks(Household household) {
    return null;
  }

  // Takes in a name for a new household and creates a DB entry for it
  // Returns a household object on success
  // Returns null on failure
  public static Household createHousehold(String name) {
    return null;
  }

  // Takes in user and household objects
  // Returns true if user is successfully added to the household
  // Returns false otherwise
  public static boolean addUserToHouse(Household household, User user) {
    return false;
  }

  // Takes in user and household objects
  // Returns true if user is successfully removed from the household
  // Returns false otherwise
  public static boolean removeUserFromHouse(Household household, User user) {
    return false;
  }

  // Takes in task and household objects
  // Returns true if tasks is successfully added to the household
  // Returns false otherwise
  public static boolean addTaskToHouse(Household household, Task task) {
    return false;
  }

  // Takes in task and household objects
  // Returns true if tasks is successfully removed from the household
  // Returns false otherwise
  public static boolean removeTaskFromHouse(Household household, Task task) {
    return false;
  }

  // Takes in no arguments (only current user can be removed)
  // Returns true if user is deleted
  // Returns false otherwise
  public static boolean deleteUser() {
    return false;
  }

  // Takes in a household object
  // Returns true if household is deleted
  // Returns false otherwise
  public static boolean deleteHousehold(Household household) {
    return false;
  }
}
