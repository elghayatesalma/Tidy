package cse403.sp2020.tidy.data;

import android.util.Log;
import android.view.Display;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.internal.$Gson$Preconditions;

import java.util.Random;

import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

/*
 * TODO: Test cases to add:
 *  - Full integration test
 *  - Multi user with task data tests
 *  - Switching between household with data
 */

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
  // Tests basic task operations
  public void taskTests() throws InterruptedException{
    ModelInterface model = new ModelInterface(mFirestore);
    final String userId = "taskTest_userId";
    final CallbackCounter counter = new CallbackCounter();
    final CallbackCounter listenerCounter = new CallbackCounter();

    // Build a basic one-user household
    basicSetup(model, counter, userId);

    // Set up a task listener
    model.setTasksListener(tasks -> {
      counter.decrement();
      // Check that tasks exist and are of proper size
      assertNotNull(tasks);
      assertEquals(listenerCounter.getCount(), tasks.size());
    });

    // Add a task
    TaskModel newTask = new TaskModel("Name", "Description", 1);
    counter.increment(2);  // two callbacks to wait for
    listenerCounter.increment();
    model.addTask(newTask, task -> {
      counter.decrement();
      assertNotNull(task);
      assertEquals(newTask.getTaskId(), task.getTaskId());
      assertEquals("Name", task.getName());
      assertEquals("Description", task.getDescription());
      assertEquals(1, task.getPriority());
    });
    counter.block();
    assertEquals(1, model.getTasks().size());

    // Update the task
    TaskModel updateTask = new TaskModel("Name2", "Description2", 2);
    updateTask.setTaskId(newTask.getTaskId());
    counter.increment(2);
    model.updateTask(updateTask, task -> {
      counter.decrement();
      assertNotNull(task);
      assertEquals(newTask.getTaskId(), task.getTaskId());
      assertEquals("Name2", task.getName());
      assertEquals("Description2", task.getDescription());
      assertEquals(2, task.getPriority());
    });
    counter.block();
    assertEquals(1, model.getTasks().size());

    // Delete the task
    TaskModel deleteTask = new TaskModel("ignore", "ignore", 3);
    updateTask.setTaskId(newTask.getTaskId());
    counter.increment(2);
    listenerCounter.decrement();
    model.removeTask(updateTask, task -> {
      counter.decrement();
      assertNotNull(task);
      // Should be same values as the update, not the delete task
      assertEquals(newTask.getTaskId(), task.getTaskId());
      assertEquals("Name2", task.getName());
      assertEquals("Description2", task.getDescription());
      assertEquals(2, task.getPriority());
    });
    counter.block();
    assertEquals(0, model.getTasks().size());

    // Update listener
    model.setTasksListener(tasks -> {
      counter.decrement();
      assertNotNull(tasks);
    });

    // Do a bunch of inserts without stopping
    int numOperations = 30;
    counter.increment(numOperations * 2);
    for (int i = 0; i < numOperations; i++) {
      final int currentInt = i;
      final TaskModel repeatTask = new TaskModel("Rname" + i, "Rdesc" + i, i);
      model.addTask(repeatTask, task -> {
        counter.decrement();
        assertNotNull(task);
        assertEquals("Rname" + currentInt, task.getName());
        assertEquals("Rdesc" + currentInt, task.getDescription());
        assertEquals(currentInt, task.getPriority());
      });
    }
    counter.block();
    assertEquals(numOperations, model.getTasks().size());

    // Do a bunch of updates without stopping
    counter.increment(numOperations * 2);
    int i = numOperations;
    for (TaskModel repeatTask : model.getTasks()) {
      final int currentInt = i;
      final TaskModel repeatTaskUpdate = new TaskModel("RnameU" + i, "RdescU" + i, i);
      repeatTaskUpdate.setTaskId(repeatTask.getTaskId());
      model.updateTask(repeatTaskUpdate, task -> {
        counter.decrement();
        assertNotNull(task);
        assertEquals("RnameU" + currentInt, task.getName());
        assertEquals("RdescU" + currentInt, task.getDescription());
        assertEquals(currentInt, task.getPriority());
      });
      i++;
    }
    counter.block();
    assertEquals(numOperations, model.getTasks().size());

    // Check that all updates went through
    for (TaskModel repeatTask : model.getTasks()) {
      assertNotNull(repeatTask);
      assertTrue(repeatTask.getPriority() >= numOperations);
    }

    // Delete everything
    counter.increment(numOperations * 2);
    i = numOperations;
    for (TaskModel repeatTask : model.getTasks()) {
      final int currentInt = i;
      final TaskModel repeatTaskUpdate = new TaskModel("RnameUD" + i, "RdescUD" + i, i);
      repeatTaskUpdate.setTaskId(repeatTask.getTaskId());
      model.removeTask(repeatTaskUpdate, task -> {
        counter.decrement();
        assertNotNull(task);
        // The passed in object is simply passed back
        assertEquals("RnameUD" + currentInt, task.getName());
        assertEquals("RdescUD" + currentInt, task.getDescription());
        assertEquals(currentInt, task.getPriority());
      });
      i++;
    }
    counter.block();
    assertEquals(0, model.getTasks().size());

    // Try operations on a nonexistent task (both bad and null id)
    final TaskModel nonexistentTask = new TaskModel("NOT", "THERE", 1);
    nonexistentTask.setTaskId("BAD_ID");
    counter.increment();
    model.updateTask(nonexistentTask, task -> {
      counter.decrement();
      assertNull(task);
    });
    counter.block();
    assertEquals(0, model.getTasks().size());
    counter.increment();
    model.removeTask(nonexistentTask, task -> {
      counter.decrement();
      assertNull(task);
    });
    counter.block();
    nonexistentTask.setTaskId(null);
    assertEquals(0, model.getTasks().size());
    counter.increment();
    model.updateTask(nonexistentTask, task -> {
      counter.decrement();
      assertNull(task);
    });
    counter.block();
    assertEquals(0, model.getTasks().size());
    assertEquals(0, model.getTasks().size());
    counter.increment();
    model.removeTask(nonexistentTask, task -> {
      counter.decrement();
      assertNull(task);
    });
    counter.block();

    model.cleanUp();
  }

  @Test
  // Tests basic user operations
  public void userTest() throws InterruptedException {
    ModelInterface model = new ModelInterface(mFirestore);
    final String userId = "userTest_userId";
    final String userId2 = "userTest_userId2";
    final CallbackCounter counter = new CallbackCounter();

    // Create different households with different users
    basicSetup(model, counter, userId);
    UserModel user1 = model.getCurrentUser();
    HouseholdModel house1 = model.getHousehold();
    basicSetup(model, counter, userId2);
    UserModel user2 = model.getCurrentUser();
    HouseholdModel house2 = model.getHousehold();

    // Get the first user back
    counter.increment();
    model.setCurrentUser(userId, user -> {
      assertNotNull(user);
      assertNotNull(model.getHousehold());
      assertNotNull(model.getCurrentUser());
      assertEquals(model.getCurrentUser().getFirebaseId(), user.getFirebaseId());
      assertNotEquals(user2.getFirebaseId(), user.getFirebaseId());
      counter.decrement();
    });
    counter.block();

    // Set up listener
    counter.increment();
    model.setUsersListener(users -> {
      assertNotNull(users);
      counter.decrement();
    });
    counter.block();

    assertEquals(1, model.getUsers().size());

    // Get the second user
    counter.increment();
    model.setCurrentUser(user2.getFirebaseId(), user -> {
      assertNotNull(user);
      assertNotNull(model.getHousehold());
      assertNotNull(model.getCurrentUser());
      assertEquals(model.getCurrentUser().getFirebaseId(), user.getFirebaseId());
      assertNotEquals(user1.getFirebaseId(), user.getFirebaseId());
      counter.decrement();
    });
    counter.block();

    // Leave the current household
    counter.increment();
    model.removeUserFromHousehold(user -> {
      assertNotNull(user);
      assertEquals(user2.getFirebaseId(), user.getFirebaseId());
      assertNull(model.getHousehold());
      counter.decrement();
    });
    counter.block();

    // Join the other household
    counter.increment(1);
    model.setCurrentHousehold(house1.getHouseholdId(), house -> {
      assertNotNull(house);
      assertEquals(model.getHousehold().getHouseholdId(), house.getHouseholdId());
      assertEquals(house1.getHouseholdId(), house.getHouseholdId());
      assertEquals(model.getCurrentUser().getFirebaseId(), user2.getFirebaseId());
      counter.decrement();
    });
    counter.block();

    // Set up listener
    counter.increment();
    model.setUsersListener(users -> {
      assertNotNull(users);
      counter.decrement();
    });
    counter.block();

    // Should be two users in the first household now
    assertEquals(2, model.getUsers().size());

    model.cleanUp();
  }

  @Test
  // Tests basic household operations
  public void householdTest() throws InterruptedException {
    ModelInterface model = new ModelInterface(mFirestore);
    final String userId = "householdTest_userId";
    final CallbackCounter counter = new CallbackCounter();

    // Test that a user can be added
    counter.increment();
    model.setCurrentUser(
        userId,
        user -> {
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
    model.updateCurrentUser(
        updatedUser,
        user -> {
          counter.decrement();
          assertNotNull(user);
          assertEquals(userId, user.getFirebaseId());
          assertEquals("fname", user.getFirstName());
          assertEquals("lname", user.getLastName());
        });
    counter.block();

    // Add the user to a new household
    counter.increment();
    model.createHousehold(
        new HouseholdModel("ignore"),
        household -> {
          counter.decrement();
          assertNotNull(household);
          assertNotEquals("ignore", household.getHouseholdId());
        });
    counter.block();

    // Grab the first household data
    HouseholdModel household1 = model.getHousehold();

    // Remove user
    counter.increment();
    model.removeUserFromHousehold(
        user -> {
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
    model.createHousehold(
        household1,
        household -> {
          counter.decrement();
          assertNotNull(household);
          assertNotEquals("ignore", household.getHouseholdId());
          assertNotEquals(household1.getHouseholdId(), household.getHouseholdId());
        });
    counter.block();

    // Again, remove user
    counter.increment();
    model.removeUserFromHousehold(
        user -> {
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
    model.setCurrentHousehold(
        household1.getHouseholdId(),
        household -> {
          counter.decrement();
          assertNotNull(household);
          assertNotEquals("ignore", household.getHouseholdId());
          assertEquals(household1.getHouseholdId(), household.getHouseholdId());
        });
    counter.block();

    model.cleanUp();
  }

  private void basicSetup(ModelInterface model, CallbackCounter counter, String userId)
      throws InterruptedException {
    // Add user
    counter.increment();
    model.setCurrentUser(
        userId,
        user -> {
          counter.decrement();
          assertNotNull(user);
          assertNull("(Make sure to clear the database)", model.getHousehold());
          assertNull(model.getUsers());
          assertNull(model.getTasks());
        });
    counter.block();

    // Make new household
    counter.increment();
    model.createHousehold(
        new HouseholdModel("ignore"),
        household -> {
          counter.decrement();
          assertNotNull(household);
          assertNotEquals("ignore", household.getHouseholdId());
        });
    counter.block();
  }
}

// Simple class to keep track of callbacks and block until they finish
class CallbackCounter {
  private int count;

  public int getCount() {
    return count;
  }

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
    increment(1);
  }

  public void increment(int amount) {
    count += amount;
  }

  public void decrement() {
    decrement(1);
  }

  public void decrement(int amount) {
    if (count >= amount) {
      count -= amount;
    } else {
      count = 0;
    }
  }
}

