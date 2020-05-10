package cse403.sp2020.tidy.data;

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
  // Runs through a basic setup with creating a household and adding a user
  public void singleUserTest() throws InterruptedException {
    String userId = "test_user_id";
    CallbackChecker checker = new CallbackChecker();
    UserModel user = new UserModel(userId, "Test", "Name");
    ModelInterface mi = new ModelInterface(mFirestore, user);
    mi.registerHouseholdCallback(checker);
    mi.registerTaskCallback(checker);
    mi.registerUserCallback(checker);
    // Wait for initial query to fail
    checker.setHouseholdWaiting();
    checker.block();

    // Nothing is in the database yet, should be totally empty
    assertNull("(Make sure to clear the database)", mi.getHousehold());
    assertNull(mi.getTasks());
    assertNull(mi.getUsers());

    // Create a household for the user and add 'em to it
    checker.setHouseholdWaiting();
    checker.setUserWaiting();
    mi.makeHousehold();
    checker.block();

    // A household with a user should now exist
    assertNotNull(mi.getHousehold());
    assertEquals(1, mi.getUsers().size());

    // Add a number of tasks
    checker.setTaskWaiting();
    mi.addTaskToHouse(new TaskModel("name 1", "task 1", 1));
    checker.block();
    checker.setTaskWaiting();
    mi.addTaskToHouse(new TaskModel("name 2", "task 2", 2));
    checker.block();
    checker.setTaskWaiting();
    mi.addTaskToHouse(new TaskModel("name 3", "task 3", 1));
    checker.block();
    assertEquals(mi.getTasks().size(), 3);
    // TODO: accuracy checks

    // Delete the tasks
    checker.setTaskWaiting();
    mi.removeTaskFromHouse(mi.getTasks().get(0));
    checker.block();
    assertEquals(2, mi.getTasks().size());
    checker.setTaskWaiting();
    mi.removeTaskFromHouse(mi.getTasks().get(0));
    checker.block();
    assertEquals(1, mi.getTasks().size());
    checker.setTaskWaiting();
    mi.removeTaskFromHouse(mi.getTasks().get(0));
    checker.block();
    assertEquals(0, mi.getTasks().size());

    // Add one back
    checker.setTaskWaiting();
    mi.addTaskToHouse(new TaskModel("name 3", "task 3", 1));
    checker.block();
    assertEquals(1, mi.getTasks().size());

    // Leave the household, but grab info first
    HouseholdModel household = mi.getHousehold();
    checker.setHouseholdWaiting();
    //    checker.setTaskWaiting();
    checker.setUserWaiting();
    mi.removeUserFromHouse();
    checker.block();

    assertNull(mi.getHousehold());
    assertNull(mi.getTasks());
    assertNull(mi.getUsers());

    // Rejoin
    checker.setHouseholdWaiting();
    checker.setUserWaiting();
    mi.setHousehold(household);
    checker.block();

    // Check that data is still retained and queried
    assertNotNull(mi.getHousehold());
    assertEquals(1, mi.getUsers().size());
    assertEquals(1, mi.getTasks().size());

    // Always clean up and remove listeners
    mi.cleanUp();
  }

  @Test
  public void multiUserTest() {
    // TODO
  }

  @After
  public void tearDown() throws Exception {
    mFirestore.terminate();
  }
}

// Simple class to keep track of callbacks and mark when the callbacks have succeeded
class CallbackChecker
    implements HouseholdCallbackInterface, TaskCallbackInterface, UserCallbackInterface {
  private boolean householdWaiting;
  private boolean taskWaiting;
  private boolean userWaiting;

  public void block() throws InterruptedException {
    while (isWaiting()) {
      // Just wait... maybe add a really small sleep?
      Thread.sleep(10);
    }
  }

  public boolean isWaiting() {
    return householdWaiting || taskWaiting || userWaiting;
  }

  public void setHouseholdWaiting() {
    setHouseholdWaiting(true);
  }

  public void setHouseholdWaiting(boolean setTo) {
    householdWaiting = setTo;
  }

  public void setTaskWaiting() {
    setTaskWaiting(true);
  }

  public void setTaskWaiting(boolean setTo) {
    taskWaiting = setTo;
  }

  public void setUserWaiting() {
    setUserWaiting(true);
  }

  public void setUserWaiting(boolean setTo) {
    userWaiting = setTo;
  }

  @Override
  public void householdCallback(HouseholdModel household) {
    householdWaiting = false;
  }

  @Override
  public void householdCallbackFailed() {
    householdWaiting = false;
  }

  @Override
  public void taskCallback(List<TaskModel> users) {
    taskWaiting = false;
  }

  @Override
  public void taskCallbackFail() {
    taskWaiting = false;
  }

  @Override
  public void userCallback(List<UserModel> users) {
    userWaiting = false;
  }

  @Override
  public void userCallbackFailed() {
    userWaiting = false;
  }
}
