package cse403.sp2020.tidy.data;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

public class ModelInterfaceTest {
  private FirebaseFirestore mFirestore;

  @Before
  public void setUp() throws Exception {
    // 10.0.2.2 is the special IP address to connect to the 'localhost' of
    // the host computer from an Android emulator.
    FirebaseFirestoreSettings settings =
        new FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build();

    mFirestore = FirebaseFirestore.getInstance();
    mFirestore.setFirestoreSettings(settings);
  }

  @Test
  // Runs through tests on a single model interface
  public void singleUserTest() throws InterruptedException {
    String userId = "single_test_user_id";
    CallbackChecker checker = new CallbackChecker();
    UserModel user = new UserModel(userId, "Test", "Name");
    ModelInterface model = new ModelInterface(mFirestore, null);
    model.registerHouseholdCallback(checker);
    model.registerTaskCallback(checker);
    model.registerUserCallback(checker);
    // Wait for initial query to fail
    checker.setHouseholdWaiting();
    model.setUser(user); // Add user in, which runs initial queries
    checker.block();

    // Nothing is in the database yet, should be totally empty
    assertNull("(Make sure to clear the database)", model.getHousehold());
    assertNull(model.getTasks());
    assertNull(model.getUsers());

    // Create a household for the user and add 'em to it
    checker.setHouseholdWaiting();
    checker.setUserWaiting();
    model.makeHousehold(new HouseholdModel());
    checker.block();

    // A household with a user should now exist
    assertNotNull(model.getHousehold());
    assertEquals(1, model.getUsers().size());

    // Add a number of tasks
    checker.setTaskWaiting();
    model.addTaskToHouse(new TaskModel("name 1", "task 1", 1));
    checker.block();
    checker.setTaskWaiting();
    model.addTaskToHouse(new TaskModel("name 2", "task 2", 2));
    checker.block();
    checker.setTaskWaiting();
    model.addTaskToHouse(new TaskModel("name 3", "task 3", 3));
    checker.block();
    assertEquals(model.getTasks().size(), 3);
    // TODO: accuracy checks

    // Delete the tasks
    checker.setTaskWaiting();
    model.removeTaskFromHouse(model.getTasks().get(0));
    checker.block();
    assertEquals(2, model.getTasks().size());
    checker.setTaskWaiting();
    model.removeTaskFromHouse(model.getTasks().get(0));
    checker.block();
    assertEquals(1, model.getTasks().size());
    checker.setTaskWaiting();
    model.removeTaskFromHouse(model.getTasks().get(0));
    checker.block();
    assertEquals(0, model.getTasks().size());

    // Add one back
    checker.setTaskWaiting();
    model.addTaskToHouse(new TaskModel("name 3", "task 3", 1));
    checker.block();
    assertEquals(1, model.getTasks().size());

    // Leave the household, but grab info first
    HouseholdModel household = model.getHousehold();
    checker.setHouseholdWaiting();
    //    checker.setTaskWaiting();
    checker.setUserWaiting();
    model.removeUserFromHousehold();
    checker.block();

    assertNull(model.getHousehold());
    assertNull(model.getTasks());
    assertNull(model.getUsers());

    // Rejoin
    checker.setHouseholdWaiting();
    checker.setUserWaiting();
    model.setHousehold(household);
    checker.block();

    // Check that data is still retained and queried
    assertNotNull(model.getHousehold());
    assertEquals(1, model.getUsers().size());
    assertEquals(1, model.getTasks().size());

    // Always clean up and remove listeners
    model.cleanUp();
  }

  @Test
  // Tests adding a number of users to different households (each user has an interface)
  // Each user has an associated model interface, each household has a checker
  // Users are assigned to households in alternating pattern, i.e.:
  //   users              : 1 2 3 4 ...
  //   household assigned : 1 2 1 2 ...
  public void multiUserTest() throws InterruptedException {
    // Can change as needed, but pay attention to asserts
    int numHouseholds = 10;
    int numUsers = 50;
    int usersPerHousehold = numUsers / numHouseholds;

    assert (numHouseholds >= 2); // At least 2 households
    assert (numUsers >= 2 * numHouseholds); // At least 2 users per household
    assert (numUsers % numHouseholds == 0); // Same number of users per household

    List<UserModel> users = new ArrayList<>();
    List<HouseholdModel> households = new ArrayList<>();
    List<ModelInterface> models = new ArrayList<>();
    List<CallbackChecker> checkers = new ArrayList<>();

    // Create households and checkers
    // All users within a household will have the same callback triggers
    // So use the same checker for each (nicer for callback counting too)
    for (int i = 0; i < numHouseholds; i++) {
      households.add(new HouseholdModel("householdId_" + i)); // id currently ignored
      checkers.add(new CallbackChecker());
    }

    // Create users and models interfaces, add to households
    // Do basic init checks for each user/model interface
    for (int i = 0; i < numUsers; i++) {
      // Make user
      UserModel user = new UserModel("firebaseId_" + i, "fname " + i, "lname" + i);
      users.add(user);

      // Make interface without user for now
      ModelInterface model = new ModelInterface(mFirestore, null);
      models.add(model);

      HouseholdModel household = households.get(i % numHouseholds);
      CallbackChecker checker = checkers.get(i % numHouseholds);

      // Register checker with model
      model.registerHouseholdCallback(checker);
      model.registerTaskCallback(checker);
      model.registerUserCallback(checker);

      // Set user
      checker.setHouseholdWaiting();
      model.setUser(user);
      checker.block();

      assertNull("(Make sure to clear the database)", model.getHousehold());
      assertNull(model.getTasks());
      assertNull(model.getUsers());

      // Set household
      checker.setHouseholdWaiting();
      checker.setUserWaiting(i / numHouseholds + 1); // Wait for each user to get an update
      // If on the first occurrence of the household, make it
      if (i < numHouseholds) {
        model.makeHousehold(household);
      } else {
        model.setHousehold(household);
      }
      checker.block();

      assertNotNull(model.getHousehold());
      assertEquals(i / numHouseholds + 1, model.getUsers().size());
    }

    // Test tasks add and delete
    int numTasks = 3;
    // Add
    for (int i = 0; i < numTasks; i++) {
      for (int j = 0; j < numHouseholds; j++) {
        ModelInterface model = models.get(j);
        CallbackChecker checker = checkers.get(j);

        checker.setTaskWaiting(usersPerHousehold);
        model.addTaskToHouse(new TaskModel("tname_" + i, "tdesc" + i, i));
        checker.block();

        assertEquals(i + 1, model.getTasks().size());
      }
    }
    // Delete
    for (int i = 0; i < numTasks; i++) {
      for (int j = 0; j < numHouseholds; j++) {
        ModelInterface model = models.get(j);
        CallbackChecker checker = checkers.get(j);

        checker.setTaskWaiting(usersPerHousehold);
        model.removeTaskFromHouse(model.getTasks().get(0));
        checker.block();

        assertEquals(numTasks - i - 1, model.getTasks().size());
      }
    }

    // Delete a user from each household
    for (int i = 0; i < numHouseholds; i++) {
      ModelInterface model = models.get(i);
      CallbackChecker checker = checkers.get(i);

      Log.w("ModelInterface", "Here");
      checker.setUserWaiting(usersPerHousehold);
      checker.setHouseholdWaiting(); // Wait for clear data
      model.removeUserFromHousehold();
      checker.block();
      Log.w("ModelInterface", "There");

      assertNull(model.getHousehold());
      assertNull(model.getTasks());
      assertNull(model.getUsers());
      assertEquals(usersPerHousehold - 1, models.get(i + numHouseholds).getUsers().size());
    }
    // Add them back
    for (int i = 0; i < numHouseholds; i++) {
      ModelInterface model = models.get(i);
      CallbackChecker checker = checkers.get(i);

      checker.setUserWaiting(usersPerHousehold);
      checker.setHouseholdWaiting(); // Wait for household to be set
      model.setHousehold(households.get(i));
      checker.block();

      assertEquals(usersPerHousehold, model.getUsers().size());
    }

    // Stress test, add numTasks per household
    numTasks = 100;
    for (CallbackChecker checker : checkers) {
      checker.setTaskWaiting(numTasks * usersPerHousehold);
    }
    for (int i = 0; i < numTasks * numHouseholds; i++) {
      ModelInterface model = models.get(i % numUsers);
      model.addTaskToHouse(new TaskModel("tname_" + i, "tdesc" + i, i % 10));
    }
    for (CallbackChecker checker : checkers) {
      checker.block();
    }
    for (ModelInterface model : models) {
      assertEquals(numTasks, model.getTasks().size());
    }

    // Always clean up and remove listeners
    for (ModelInterface model : models) {
      model.cleanUp();
    }
  }

  @After
  public void tearDown() throws Exception {
    mFirestore.terminate();
  }
}

// Simple class to keep track of callbacks and mark when the callbacks have succeeded
// Uses callback counting, but does nothing to handle too many/few callbacks
//  - If the number of callbacks is greater than the wait amount, nothing will happen on excess
// calls
//  - If it is less, block will never stop
class CallbackChecker
    implements HouseholdCallbackInterface, TaskCallbackInterface, UserCallbackInterface {
  private int mHouseholdWaiting;
  private int mTaskWaiting;
  private int mUserWaiting;

  public void block() throws InterruptedException {
    while (isWaiting()) {
      // Just wait... maybe add a really small sleep?
      Thread.sleep(10);
    }
  }

  public boolean isWaiting() {
    return householdWaiting() || taskWaiting() || userWaiting();
  }

  private boolean householdWaiting() {
    return mHouseholdWaiting > 0;
  }

  private boolean taskWaiting() {
    return mTaskWaiting > 0;
  }

  private boolean userWaiting() {
    return mUserWaiting > 0;
  }

  public void setHouseholdWaiting() {
    setHouseholdWaiting(1);
  }

  public void setHouseholdWaiting(int setTo) {
    mHouseholdWaiting = setTo;
  }

  public void setTaskWaiting() {
    setTaskWaiting(1);
  }

  public void setTaskWaiting(int setTo) {
    mTaskWaiting = setTo;
  }

  public void setUserWaiting() {
    setUserWaiting(1);
  }

  public void setUserWaiting(int setTo) {
    mUserWaiting = setTo;
  }

  @Override
  public void householdCallback(HouseholdModel household) {
    householdCallbackFailed();
  }

  @Override
  public void householdCallbackFailed() {
    if (mHouseholdWaiting > 0) {
      mHouseholdWaiting--;
    }
  }

  @Override
  public void taskCallback(List<TaskModel> users) {
    taskCallbackFail();
  }

  @Override
  public void taskCallbackFail() {
    if (mTaskWaiting > 0) {
      mTaskWaiting--;
    }
  }

  @Override
  public void userCallback(List<UserModel> users) {
    userCallbackFailed();
  }

  @Override
  public void userCallbackFailed() {
    if (mUserWaiting > 0) {
      mUserWaiting--;
    }
  }
}
