package cse403.sp2020.tidy.data;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.FirebaseFirestore;

import cse403.sp2020.tidy.data.model.HouseholdModel;
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

  @After
  public void tearDown() throws Exception {
    mFirestore.terminate();
  }

   @Test
   // Tests basic household operations
   public void householdTest() throws InterruptedException {
     ModelInterface model = new ModelInterface(mFirestore);
     final String userId = "buildTest_userId";
     final CallbackCounter counter = new CallbackCounter();

     // Test that a user can be added
     counter.increment();
     model.setCurrentUser(userId, user ->{
       counter.decrement();
       assertNotNull(user);
       assertNull("(Make sure to clear the database)", model.getHousehold());
       assertNull(model.getUsers());
       assertNull(model.getTasks());
     });
     counter.block();

     // Update the user
     counter.increment();
     UserModel updatedUser = new UserModel("ignore", "fname", "lname");
     model.updateCurrentUser(updatedUser, user ->{
       counter.decrement();
       assertNotNull(user);
       assertEquals(userId, user.getFirebaseId());
       assertEquals("fname", user.getFirstName());
       assertEquals("lname", user.getLastName());
     });
     counter.block();

     // Add the user to a new household
     counter.increment();
     model.createHousehold(new HouseholdModel("ignore"), household ->{
       counter.decrement();
       assertNotNull(household);
       assertNotEquals("ignore", household.getHouseholdId());
     });
     counter.block();

     // Grab the first household data
     HouseholdModel household1 = model.getHousehold();

     // Remove user
     counter.increment();
     model.removeUserFromHousehold(user ->{
       counter.decrement();
       assertNotNull(user);
       assertEquals(userId, user.getFirebaseId());
       assertEquals("fname", user.getFirstName());
       assertEquals("lname", user.getLastName());
       assertNull("(Make sure to clear the database)", model.getHousehold());
       assertNull(model.getUsers());
       assertNull(model.getTasks());
     });
     counter.block();

     // Add a new household
     counter.increment();
     model.createHousehold(household1, household -> {
       counter.decrement();
       assertNotNull(household);
       assertNotEquals("ignore", household.getHouseholdId());
       assertNotEquals(household1.getHouseholdId(), household.getHouseholdId());
     });
     counter.block();

     // Again, remove user
     counter.increment();
     model.removeUserFromHousehold(user ->{
       counter.decrement();
       assertNotNull(user);
       assertEquals(userId, user.getFirebaseId());
       assertEquals("fname", user.getFirstName());
       assertEquals("lname", user.getLastName());
       assertNull("(Make sure to clear the database)", model.getHousehold());
       assertNull(model.getUsers());
       assertNull(model.getTasks());
     });
     counter.block();

     // Add the user back to original household
     counter.increment();
     model.setCurrentHousehold(household1.getHouseholdId(), household -> {
       counter.decrement();
       assertNotNull(household);
       assertNotEquals("ignore", household.getHouseholdId());
       assertEquals(household1.getHouseholdId(), household.getHouseholdId());
     });
     counter.block();

     model.cleanUp();
   }

   private void basicSetup(ModelInterface model, CallbackCounter counter, String userId) throws InterruptedException {
     // Add user
     counter.increment();
     model.setCurrentUser(userId, user ->{
       counter.decrement();
       assertNotNull(user);
       assertNull("(Make sure to clear the database)", model.getHousehold());
       assertNull(model.getUsers());
       assertNull(model.getTasks());
     });
     counter.block();

     // Make new household
     counter.increment();
     model.createHousehold(new HouseholdModel("ignore"), household ->{
       counter.decrement();
       assertNotNull(household);
       assertNotEquals("ignore", household.getHouseholdId());
       assertEquals(1, model.getUsers().size());
       assertEquals(0, model.getTasks().size());
     });
     counter.block();
   }
 }

 // Simple class to keep track of callbacks and block until they finish
 class CallbackCounter {
   private int count;

  public boolean isPending() {
    return count > 0;
  }

  public void block() throws InterruptedException {
    while (isPending()) {
      // Just wait... maybe add a really small sleep?
      Thread.sleep(10);
    }
  }

  public void increment() {
    increment(0);
  }

  public void increment(int amount) {
    count++;
  }

  public void decrement() {
    decrement(0);
  }

  public void decrement(int amount) {
    if (count > 0) {
      count--;
    }
  }
}

//
//  @Test
//  // Runs through building and rebuilding the model interface for the same user
//  public void rebuildTest() throws InterruptedException {
//    String userId = "rebuildTest_userId";
//    CallbackChecker checker = new CallbackChecker();
//    ModelInterface model = new ModelInterface(mFirestore);
//
//    // Do initial build (includes checks
//    basicSetup(userId, model, checker);
//
//    // Grab household
//    HouseholdModel household = model.getHousehold();
//
//    // Destroy the current interface and rebuild
//    model.cleanUp();
//    model = new ModelInterface(mFirestore);
//    basicSetup(userId, model, checker, true);
//
//    // Destroy the current interface and rebuild with a new user in the same household
//    model.cleanUp();
//    model = new ModelInterface(mFirestore);
//    basicSetup(userId + "_2", model, checker, household);
//
//    // A second user should exist
//    assertTrue(household.equals(model.getHousehold()));
//    assertEquals(2, model.getUsers().size());
//
//    // Destroy the current interface and rebuild with original user
//    model.cleanUp();
//    model = new ModelInterface(mFirestore);
//    basicSetup(userId, model, checker, true);
//
//    // Add a task
//
//    // Destroy and rebuild again
//    model.cleanUp();
//    model = new ModelInterface(mFirestore);
//    basicSetup(userId, model, checker, true);
//
//    // Check that the data exists
//    assertEquals(2, model.getUsers().size());
//
//    // Always clean up and remove listeners
//    model.cleanUp();
//  }
//
//  @Test
//  // Does basic operations on tasks and checks for correctness
//  public void taskTest() throws InterruptedException {
//    String userId = "taskTest_userId";
//    CallbackChecker checker = new CallbackChecker();
//    ModelInterface model = new ModelInterface(mFirestore);
//    basicSetup(userId, model, checker);
//
//    // Add a task
//    checker.setTaskWaiting();
//    model.addTaskToHousehold(new TaskModel("name 1", "task 1", 1));
//    checker.block();
//    assertEquals(1, model.getTasks().size());
//    assertEquals("name 1", model.getTasks().get(0).getName());
//
//    // Update a task
//    checker.setTaskWaiting();
//    TaskModel task = new TaskModel("name 1 updated", "task 1", 1);
//    task.setTaskId(model.getTasks().get(0).getTaskId());
//    model.updateTask(task);
//    checker.block();
//    assertEquals(1, model.getTasks().size());
//    assertEquals("name 1 updated", model.getTasks().get(0).getName());
//
//    // Delete a task
//    checker.setTaskWaiting();
//    model.removeTaskFromHousehold(model.getTasks().get(0));
//    checker.block();
//    assertEquals(0, model.getTasks().size());
//
//    // Add a bunch of tasks
//    int numTasks = 10;
//    for (int i = 0; i < numTasks; i++) {
//      checker.setTaskWaiting();
//      model.addTaskToHousehold(new TaskModel("name " + i, "task " + i, i));
//      checker.block();
//      assertEquals(i + 1, model.getTasks().size());
//    }
//
//    // Update a bunch of tasks
//    List<TaskModel> tasks = model.getTasks();
//    checker.setTaskWaiting(tasks.size());
//    for (int i = 0; i < numTasks; i++) {
//      task = new TaskModel("updated", "task all", i);
//      task.setTaskId(tasks.get(i).getTaskId());
//      model.updateTask(task);
//    }
//    checker.block();
//
//    // Confirm updates
//    Set<Integer> prioritiesSeen = new HashSet<>();
//    for (TaskModel t : model.getTasks()) {
//      assertEquals("updated", t.getName());
//      assertFalse(prioritiesSeen.contains(t.getPriority()));
//      prioritiesSeen.add(t.getPriority());
//    }
//
//    // Delete a bunch of tasks
//    for (int i = 0; i < numTasks; i++) {
//      checker.setTaskWaiting();
//      model.removeTaskFromHousehold(model.getTasks().get(0));
//      checker.block();
//      assertEquals(numTasks - i - 1, model.getTasks().size());
//    }
//
//    assertTrue(model.getTasks().isEmpty());
//
//    model.cleanUp();
//  }
//
//  @Test
//  // Runs through mixed tests on a single model interface
//  public void singleUserTest() throws InterruptedException {
//    String userId = "single_test_user_id";
//    CallbackChecker checker = new CallbackChecker();
//    ModelInterface model = new ModelInterface(mFirestore);
//
//    // Do initial build (includes checks
//    basicSetup(userId, model, checker);
//
//    // Update the user's name
//    checker.setUserWaiting();
//    UserModel updatedUser = model.getCurrentUser();
//    updatedUser.setFirstName("New name");
//    model.updateCurrentUser(updatedUser);
//    checker.block();
//
//    // Add a number of tasks
//    checker.setTaskWaiting(3);
//    model.addTaskToHousehold(new TaskModel("name 1", "task 1", 1));
//    model.addTaskToHousehold(new TaskModel("name 2", "task 2", 2));
//    model.addTaskToHousehold(new TaskModel("name 3", "task 3", 3));
//    checker.block();
//    assertEquals(3, model.getTasks().size());
//
//    // Delete the tasks
//    checker.setTaskWaiting(3);
//    List<TaskModel> tasks = model.getTasks();
//    model.removeTaskFromHousehold(tasks.get(0));
//    model.removeTaskFromHousehold(tasks.get(1));
//    model.removeTaskFromHousehold(tasks.get(2));
//    checker.block();
//    assertEquals(0, model.getTasks().size());
//
//    // Add one back
//    checker.setTaskWaiting();
//    model.addTaskToHousehold(new TaskModel("name 3", "task 3", 1));
//    checker.block();
//    assertEquals(1, model.getTasks().size());
//
//    // Leave the household, but grab info first
//    HouseholdModel household = model.getHousehold();
//    checker.setHouseholdWaiting();
//    checker.setUserWaiting();
//    model.removeUserFromHousehold();
//    checker.block();
//
//    // Check that all local data was cleared after leaving
//    assertNull(model.getHousehold());
//    assertNull(model.getTasks());
//    assertNull(model.getUsers());
//
//    // Add some other user
//    checker.setUserWaiting();
//    model.setUser("some_new_id");
//    checker.block();
//
//    // Get the original user back
//    checker.setUserWaiting();
//    model.setUser(userId);
//    checker.block();
//
//    // Check that metadata was kept
//    assertEquals(updatedUser.getFirstName(), model.getCurrentUser().getFirstName());
//
//    // Rejoin the household
//    checker.setHouseholdWaiting();
//    checker.setUserWaiting();
//    model.setHousehold(household);
//    checker.block();
//
//    // Check that data is still retained and queried
//    assertNotNull(model.getHousehold());
//    assertEquals(1, model.getUsers().size());
//    assertEquals(1, model.getTasks().size());
//
//    // Always clean up and remove listeners
//    model.cleanUp();
//  }
//
//  @Test
//  // Runs mixed tests on many users, each with a separate interface and divided into households
//  // Users are assigned to households in alternating pattern, i.e.:
//  //   users              : 1 2 3 4 ...
//  //   household assigned : 1 2 1 2 ...
//  public void multiUserTest() throws InterruptedException {
//    // Can change as needed, but pay attention to asserts
//    int numHouseholds = 10;
//    int numUsers = 50;
//    int usersPerHousehold = numUsers / numHouseholds;
//
//    assert (numHouseholds >= 2); // At least 2 households
//    assert (numUsers >= 2 * numHouseholds); // At least 2 users per household
//    assert (numUsers % numHouseholds == 0); // Same number of users per household
//
//    List<UserModel> users = new ArrayList<>();
//    List<HouseholdModel> households = new ArrayList<>();
//    List<ModelInterface> models = new ArrayList<>();
//    List<CallbackChecker> checkers = new ArrayList<>();
//
//    // Create households and checkers
//    // All users within a household will have the same callback triggers
//    // So use the same checker for each (nicer for callback counting too)
//    for (int i = 0; i < numHouseholds; i++) {
//      households.add(new HouseholdModel("householdId_" + i)); // id currently ignored
//      checkers.add(new CallbackChecker());
//    }
//
//    // Create users and models interfaces, add to households
//    // Do basic init checks for each user/model interface
//    for (int i = 0; i < numUsers; i++) {
//      // Make user
//      UserModel userData = new UserModel("firebaseId_" + i, "fname " + i, "lname" + i);
//      users.add(userData);
//
//      // Make interface without user for now
//      ModelInterface model = new ModelInterface(mFirestore);
//      models.add(model);
//
//      HouseholdModel household = households.get(i % numHouseholds);
//      CallbackChecker checker = checkers.get(i % numHouseholds);
//
//      // Register checker with model
//      model.registerHouseholdCallback(checker);
//      model.registerTaskCallback(checker);
//      model.registerUserCallback(checker);
//
//      // Set user
//      checker.setUserWaiting();
//      model.setUser(userData.getFirebaseId());
//      checker.block();
//
//      // Update data
//      checker.setUserWaiting();
//      model.updateCurrentUser(userData);
//      checker.block();
//
//      assertNull("(Make sure to clear the database)", model.getHousehold());
//      assertNull(model.getTasks());
//      assertNull(model.getUsers());
//
//      // Set household
//      checker.setHouseholdWaiting();
//      checker.setUserWaiting(i / numHouseholds + 1); // Wait for each user to get an update
//      // If on the first occurrence of the household, make it
//      if (i < numHouseholds) {
//        model.makeHousehold(household);
//      } else {
//        model.setHousehold(household);
//      }
//      checker.block();
//
//      assertNotNull(model.getHousehold());
//      assertEquals(i / numHouseholds + 1, model.getUsers().size());
//    }
//
//    // Test tasks add and delete
//    int numTasks = 3;
//    // Add
//    for (int i = 0; i < numTasks; i++) {
//      for (int j = 0; j < numHouseholds; j++) {
//        ModelInterface model = models.get(j);
//        CallbackChecker checker = checkers.get(j);
//
//        checker.setTaskWaiting(usersPerHousehold);
//        model.addTaskToHousehold(new TaskModel("tname_" + i, "tdesc" + i, i));
//        checker.block();
//
//        assertEquals(i + 1, model.getTasks().size());
//      }
//    }
//    // Delete
//    for (int i = 0; i < numTasks; i++) {
//      for (int j = 0; j < numHouseholds; j++) {
//        ModelInterface model = models.get(j);
//        CallbackChecker checker = checkers.get(j);
//
//        checker.setTaskWaiting(usersPerHousehold);
//        model.removeTaskFromHousehold(model.getTasks().get(0));
//        checker.block();
//
//        assertEquals(numTasks - i - 1, model.getTasks().size());
//      }
//    }
//
//    // Delete a user from each household
//    for (int i = 0; i < numHouseholds; i++) {
//      ModelInterface model = models.get(i);
//      CallbackChecker checker = checkers.get(i);
//
//      Log.w("ModelInterface", "Here");
//      checker.setUserWaiting(usersPerHousehold);
//      checker.setHouseholdWaiting(); // Wait for clear data
//      model.removeUserFromHousehold();
//      checker.block();
//      Log.w("ModelInterface", "There");
//
//      assertNull(model.getHousehold());
//      assertNull(model.getTasks());
//      assertNull(model.getUsers());
//      assertEquals(usersPerHousehold - 1, models.get(i + numHouseholds).getUsers().size());
//    }
//    // Add them back
//    for (int i = 0; i < numHouseholds; i++) {
//      ModelInterface model = models.get(i);
//      CallbackChecker checker = checkers.get(i);
//
//      checker.setUserWaiting(usersPerHousehold);
//      checker.setHouseholdWaiting(); // Wait for household to be set
//      model.setHousehold(households.get(i));
//      checker.block();
//
//      assertEquals(usersPerHousehold, model.getUsers().size());
//    }
//
//    // Stress test, add numTasks per household
//    numTasks = 100;
//    for (CallbackChecker checker : checkers) {
//      checker.setTaskWaiting(numTasks * usersPerHousehold);
//    }
//    for (int i = 0; i < numTasks * numHouseholds; i++) {
//      ModelInterface model = models.get(i % numUsers);
//      model.addTaskToHousehold(new TaskModel("tname_" + i, "tdesc" + i, i % 10));
//    }
//    for (CallbackChecker checker : checkers) {
//      checker.block();
//    }
//    for (ModelInterface model : models) {
//      assertEquals(numTasks, model.getTasks().size());
//    }
//
//    // Always clean up and remove listeners
//    for (ModelInterface model : models) {
//      model.cleanUp();
//    }
//  }
//
//  // Constructs a basic setup based on the input params
//  // Household ignored if the user exists
//  private void basicSetup(
//      String userId,
//      ModelInterface model,
//      CallbackChecker checker,
//      HouseholdModel household,
//      boolean userExists)
//      throws InterruptedException {
//
//    model.registerHouseholdCallback(checker);
//    model.registerTaskCallback(checker);
//    model.registerUserCallback(checker);
//
//    Thread.sleep(100);
//
//    if (!userExists) {
//      // Wait for initial query to fail
//      checker.setUserWaiting();
//      model.setUser(userId); //  User should not exist, creates new blank user
//      checker.block();
//
//      // Initial add, no data should exist
//      assertNull("(Make sure to clear the database)", model.getHousehold());
//      assertNull(model.getTasks());
//      assertNull(model.getUsers());
//
//      // Give the user a household
//      checker.setHouseholdWaiting();
//      checker.setUserWaiting();
//      if (household == null) {
//        model.makeHousehold(new HouseholdModel());
//      } else {
//        checker.setTaskWaiting();
//        model.setHousehold(household);
//      }
//      checker.block();
//    } else {
//      // Should get a house and all the data
//      checker.setUserWaiting();
//      checker.setTaskWaiting();
//      checker.setHouseholdWaiting();
//      model.setUser(userId);
//      checker.block();
//    }
//
//    // A household with a user should now exist
//    assertNotNull(model.getHousehold());
//    assertNotNull(model.getTasks());
//    assertNotNull(model.getUsers());
//    assertNotNull(model.getCurrentUser());
//    assertTrue(model.getUsers().size() > 0);
//  }
//
//  private void basicSetup(
//      String userId, ModelInterface model, CallbackChecker checker, boolean userExists)
//      throws InterruptedException {
//    basicSetup(userId, model, checker, null, userExists);
//  }
//
//  private void basicSetup(
//      String userId, ModelInterface model, CallbackChecker checker, HouseholdModel household)
//      throws InterruptedException {
//    basicSetup(userId, model, checker, household, false);
//  }
//
//  private void basicSetup(String userId, ModelInterface model, CallbackChecker checker)
//      throws InterruptedException {
//    basicSetup(userId, model, checker, null, false);
//  }
// }
//
//// Simple class to keep track of callbacks and mark when the callbacks have succeeded
//// Uses callback counting, but does nothing to handle too many/few callbacks
////  - If the number of callbacks is greater than the wait amount, nothing will happen on excess
//// calls
////  - If it is less, block will never stop
// class CallbackChecker
//    implements HouseholdCallbackInterface, TaskCallbackInterface, UserCallbackInterface {
//  private int mHouseholdWaiting;
//  private int mTaskWaiting;
//  private int mUserWaiting;
//
//  public void block() throws InterruptedException {
//    while (isWaiting()) {
//      // Just wait... maybe add a really small sleep?
//      Thread.sleep(10);
//    }
//  }
//
//  public boolean isWaiting() {
//    return householdWaiting() || taskWaiting() || userWaiting();
//  }
//
//  private boolean householdWaiting() {
//    return mHouseholdWaiting > 0;
//  }
//
//  private boolean taskWaiting() {
//    return mTaskWaiting > 0;
//  }
//
//  private boolean userWaiting() {
//    return mUserWaiting > 0;
//  }
//
//  public void setHouseholdWaiting() {
//    setHouseholdWaiting(1);
//  }
//
//  public void setHouseholdWaiting(int setTo) {
//    mHouseholdWaiting = setTo;
//  }
//
//  public void setTaskWaiting() {
//    setTaskWaiting(1);
//  }
//
//  public void setTaskWaiting(int setTo) {
//    mTaskWaiting = setTo;
//  }
//
//  public void setUserWaiting() {
//    setUserWaiting(1);
//  }
//
//  public void setUserWaiting(int setTo) {
//    mUserWaiting = setTo;
//  }
//
//  @Override
//  public void householdCallback(HouseholdModel household) {
//    householdCallbackFailed(null);
//  }
//
//  @Override
//  public void householdCallbackFailed(String message) {
//    if (mHouseholdWaiting > 0) {
//      mHouseholdWaiting--;
//    }
//  }
//
//  @Override
//  public void taskCallback(List<TaskModel> users) {
//    taskCallbackFail(null);
//  }
//
//  @Override
//  public void taskCallbackFail(String message) {
//    if (mTaskWaiting > 0) {
//      mTaskWaiting--;
//    }
//  }
//
//  @Override
//  public void userCallback(List<UserModel> users) {
//    userCallbackFailed(null);
//  }
//
//  @Override
//  public void userCallbackFailed(String message) {
//    if (mUserWaiting > 0) {
//      mUserWaiting--;
//    }
//  }
// }
