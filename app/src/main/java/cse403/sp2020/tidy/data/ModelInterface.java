package cse403.sp2020.tidy.data;

import java.util.List;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

/*
 * Important Notes:
 *  - Firestore uses a local cache, so not every query goes to Cloud Firestore DB
 *  - ALL db changes should be done directly on the db (will affect local cache and remote)
 *  - Operation speed for any lookup/change will likely be very quick, especially if data is cached
 *  - Add event listeners for realtime updates, use get with success/fail/complete for one-time
 *  - Documents can be create by / used to create objects (See helper methods and add methods)
 *
 * TODO List:
 *  - Possibly add Auth listener?
 *  - On create household, add household with no id?
 *  - Add checks for moving users between households
 *  - Go through other TODOs
 */

/*** ModelInterface info ***
 * Public Methods
 *  Interface Methods:
 *   - Allow for retrieval and modification of database entities
 *   - DO NOT cache/store any data retrieved from these methods, query on every usage
 *      OR better yet, use callbacks to get updates automatically
 *
 *  Callback Register Methods:
 *   - Allows for registering classes that implement the callback interfaces
 *   - These methods will be called on listener updates for respective type
 *
 * Private Methods
 *  Query Methods:
 *   - Private methods for querying data and setting up listeners
 *   - Ideally only called once per household setup (initial and change of household),
 *      Event listeners will handle the rest if used with callbacks
 *   - Querying of a new household will cascade to other queries for tasks and users
 *
 *  Callback Methods
 *   - Called on changes to respective data (Household, Users, Tasks),
 *      with differentiation of success/failure
 *   - Also called in some special cases, like
 *
 *  Helper Methods:
 *  - Build methods used to construct the model objects from query data
 *  - ClearData used to clear all current household data and listeners
 */

public class ModelInterface {
  // Log info
  private static final String TAG = "ModelInterface";

  // Firestore Constants
  private static final String HOUSEHOLD_COLLECTION_NAME = "Households";
  private static final String TASK_COLLECTION_NAME = "Tasks";
  private static final String USERS_COLLECTION_NAME = "Users";
  private static final String USER_ID_FIELD = "firebaseId";

  // Callbacks
  private HouseholdCallbackInterface mHouseholdCallback;
  private TaskCallbackInterface mTaskCallback;
  private UserCallbackInterface mUserCallback;

  // Household objects
  private HouseholdModel mHousehold;
  private List<TaskModel> mTasks;
  private List<UserModel> mUsers;

  // Firestore Listeners
  private ListenerRegistration mHouseholdListener;
  private ListenerRegistration mTasksListener;
  private ListenerRegistration mUsersListener;

  // Firestore Database instance
  private FirebaseFirestore mFirestore;

  // User
  private UserModel mFirebaseUser;

  // Constructor
  // Takes a Firestore instance and the current user
  public ModelInterface(FirebaseFirestore firestore, UserModel firebaseUser) {
    Log.d(TAG, "Building ModelInterface");
    mFirestore = firestore;

    // Initialize data
    mHousehold = null;
    mTasks = new ArrayList<TaskModel>();
    mUsers = new ArrayList<UserModel>();

    // Initialize Listeners
    mHouseholdListener = null;
    mTasksListener = null;
    mUsersListener = null;

    // Initialize Callbacks
    mHouseholdCallback = null;
    mTaskCallback = null;
    mUserCallback = null;

    // Do initial queries if a user is present
    mFirebaseUser = firebaseUser;
    if (mFirebaseUser != null) {
      Log.d(TAG, "Doing initial query");
      queryHouseholdIdByUser();
    } else {
      Log.w(TAG, "No user on init, skipping initial queries");
    }
  }

  // Use to clear all data and stop listeners
  public void cleanUp() {
    clearData();
  }

  /* Interface Methods */

  // Takes in the new user to set, ignores if it is the same user
  // If a new user is set, removes all current data and restarts the queries
  public void setUser(UserModel newUser) {
    if (newUser != null && newUser.equals(mFirebaseUser) || newUser == mFirebaseUser) {
      Log.w(TAG, "No change to user, ignoring update");
      return;
    }

    Log.d(TAG, "Change in user, clearing local data and stopping listeners");
    clearData();

    if (newUser == null) {
      Log.w(TAG, "Current user is null, avoiding new queries");

    } else {
      Log.d(TAG, "New user set, starting queries");

      // Set user and restart queries
      mFirebaseUser = newUser;
      queryHouseholdIdByUser();
    }
  }

  // Creates a household and assigns the current user to it
  // Uses any metadata store in the input household
  // If householdId is null, auto-generates one
  public void makeHousehold(final HouseholdModel household) {
    // Generate the householdId if one does not exist
    DocumentReference householdDoc;
    if (household.getHouseholdId() == null) {
      householdDoc = mFirestore.collection(HOUSEHOLD_COLLECTION_NAME).document();
      household.setHouseholdId(householdDoc.getId());
    } else {
      householdDoc =
          mFirestore.collection(HOUSEHOLD_COLLECTION_NAME).document(household.getHouseholdId());
    }
    Log.d(TAG, "Making new household: " + household.getHouseholdId());

    // Create the household
    householdDoc
        .set(household)
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                  Log.d(TAG, "Household has been created");
                  // Household created, assign user to it
                  setHousehold(household);
                } else {
                  Log.w(TAG, "Failed to create a new household: " + task.getException());
                }
              }
            });
  }

  // Takes in a household object to put the user in
  public void setHousehold(final HouseholdModel household) {
    Log.d(TAG, "Attempting to set household");
    if (household == null) {
      Log.w(TAG, "set household object is null");
      return;
    }

    // Put the user in the household
    mFirestore
        .collection(HOUSEHOLD_COLLECTION_NAME)
        .document(household.getHouseholdId())
        .collection(USERS_COLLECTION_NAME)
        .document(mFirebaseUser.getFirebaseId())
        .set(mFirebaseUser)
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                  Log.d(TAG, "User has been assigned to household");

                  // Set a new household and trigger callback
                  mHousehold = household;
                  callbackHousehold(false);

                  // User has been assigned a new household, so get the data
                  queryTasks();
                  queryUsers();

                } else {
                  Log.w(TAG, "Failed to assign user to household: " + task.getException());
                }
              }
            });
  }

  // Returns the current household (or null if there isn't one)
  public HouseholdModel getHousehold() {
    if (mHousehold == null) {
      return null;
    }
    return new HouseholdModel(mHousehold);
  }

  // Returns a (potentially empty) list of users if household exists
  // Returns null if household doesn't exist
  public List<UserModel> getUsers() {
    if (mHousehold != null) {
      return new ArrayList<>(mUsers);
    }
    return null;
  }

  // Returns a (potentially empty) list of tasks if household exists
  // Returns null if household doesn't exist
  public List<TaskModel> getTasks() {
    if (mHousehold != null) {
      return new ArrayList<>(mTasks);
    }
    return null;
  }

  // Attempts to remove the user from the household
  public void removeUserFromHousehold() {
    if (mFirebaseUser == null) {
      Log.w(TAG, "Trying to remove null user, ignoring");
      return;
    }

    if (getUserCollection() != null) {
      Log.d(TAG, "Removing user: " + mFirebaseUser.getFirebaseId());
      getUserCollection()
          .document(mFirebaseUser.getFirebaseId())
          .delete()
          .addOnCompleteListener(
              new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    Log.d(TAG, "User removed successfully");
                    // The user is gone, so household data is no longer relevant
                    clearData();
                    // Trigger household callback, data is now invalid
                    callbackHousehold(false);
                  } else {
                    Log.w(TAG, "Failed to remove user: " + task.getException());
                  }
                }
              });
    } else {
      Log.w(TAG, "No users collection, failed to remove user: " + mFirebaseUser.getFirebaseId());
    }
  }

  // Attempts to add the task to the household
  public void addTaskToHouse(TaskModel task) {
    if (task == null) {
      Log.d(TAG, "Trying to add a null task, ignoring");
      return;
    }

    if (getTaskCollection() != null) {
      Log.w(TAG, "Adding new task: " + task.getName());
      // Store the task object (but grab the id first)
      DocumentReference taskDoc = getTaskCollection().document();
      task.setTaskId(taskDoc.getId());

      taskDoc
          .set(task)
          .addOnCompleteListener(
              new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    Log.w(TAG, "Task added successfully");
                  } else {
                    Log.w(TAG, "Failed to add task: " + task.getException());
                  }
                }
              });
    } else {
      Log.w(TAG, "No tasks collection, failed to add task: " + task.getName());
    }
  }

  // Attempts to remove the task from the household
  public void removeTaskFromHouse(TaskModel task) {
    if (task == null) {
      Log.w(TAG, "Trying to remove a null task, ignoring");
      return;
    }

    if (getTaskCollection() != null) {
      Log.d(TAG, "Removing task: " + task.getName());
      getTaskCollection()
          .document(task.getTaskId())
          .delete()
          .addOnCompleteListener(
              new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    // Note: Deleting non-existing documents DOES NOT fail, no way to tell either
                    Log.d(TAG, "Task deleted successfully");
                  } else {
                    Log.w(TAG, "Failed to delete task: " + task.getException());
                  }
                }
              });
    } else {
      Log.w(TAG, "No tasks collection, failed to remove task: " + task.getName());
    }
  }

  /* Callback Register methods */

  public void registerHouseholdCallback(HouseholdCallbackInterface callback) {
    Log.d(TAG, "Registering callback for Household");
    mHouseholdCallback = callback;
  }

  public void registerTaskCallback(TaskCallbackInterface callback) {
    Log.d(TAG, "Registering callback for Tasks");
    mTaskCallback = callback;
  }

  public void registerUserCallback(UserCallbackInterface callback) {
    Log.d(TAG, "Registering callback for Users");
    mUserCallback = callback;
  }

  /* Callback methods */

  // Called when the current household has been updated
  private void callbackHousehold(boolean failed) {
    if (mHouseholdCallback == null) {
      Log.d(TAG, "No household callback");
      return;
    }

    if (failed) {
      Log.d(TAG, "callback fail on Household");
      mHouseholdCallback.householdCallbackFailed();
    } else {
      Log.d(TAG, "callback on Household");
      mHouseholdCallback.householdCallback(getHousehold());
    }
  }

  // Called when the tasks have been updated
  private void callbackTasks(boolean failed) {
    if (mTaskCallback == null) {
      Log.d(TAG, "No task callback");
      return;
    }

    if (failed) {
      Log.d(TAG, "callback fail on Tasks");
      mTaskCallback.taskCallbackFail();
    } else {
      Log.d(TAG, "callback on Tasks");
      mTaskCallback.taskCallback(getTasks());
    }
  }

  // Called when the users have been updated
  private void callbackUsers(boolean failed) {
    if (mUserCallback == null) {
      Log.d(TAG, "No user callback");
      return;
    }

    if (failed) {
      Log.d(TAG, "callback fail on Users");
      mUserCallback.userCallbackFailed();
    } else {
      Log.d(TAG, "callback on Users");
      mUserCallback.userCallback(getUsers());
    }
  }

  /* Query functions */

  // Finds the householdID corresponding to the current user
  // Starts query for household document if ID is found
  private void queryHouseholdIdByUser() {
    Log.d(TAG, "Getting household with user ID: " + mFirebaseUser.getFirebaseId());

    // Single-time query, no reason to get listener
    // Searches all Users collections for particular id
    mFirestore
        .collectionGroup(USERS_COLLECTION_NAME)
        .whereEqualTo(USER_ID_FIELD, mFirebaseUser.getFirebaseId())
        .get()
        .addOnSuccessListener(
            new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(QuerySnapshot snapshot) {
                if (snapshot != null) {
                  // TODO: are both needed?
                  if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                    Log.d(TAG, "User not found to be part of a household");
                    clearData();
                  } else {
                    Log.d(
                        TAG,
                        "Found " + snapshot.getDocuments().size() + " households with user ID");

                    // From the first document, get the parent (Users collection),
                    // then parent's parent (Household document)
                    String householdID =
                        snapshot
                            .getDocuments()
                            .get(0)
                            .getReference()
                            .getParent()
                            .getParent()
                            .getId();
                    if (snapshot.getDocuments().size() > 1) {
                      Log.w(TAG, "Multiple entries of user found, logging them");
                      for (DocumentSnapshot d : snapshot.getDocuments()) {
                        Log.w(TAG, "UserID: " + d.getId() + ", data: " + d.getData());
                      }
                    }

                    // A household ID has been found, so get the corresponding household data
                    queryHousehold(householdID);
                    return;
                  }
                } else {
                  Log.w(TAG, "Query result for households is null");
                  clearData();
                }

                // No household was found
                callbackHousehold(true);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to find household by user ID", e);
              }
            });
  }

  // Starts a query for a household with the provided household ID
  // If found, it will start queries for existing Tasks and Users
  private void queryHousehold(final String householdID) {
    Log.w(TAG, "Getting household document for household ID " + householdID);

    // Deactivate the previous listener if it exists
    if (mHouseholdListener != null) {
      mHouseholdListener.remove();
      mHouseholdListener = null;
    }

    DocumentReference householdDoc =
        mFirestore.collection(HOUSEHOLD_COLLECTION_NAME).document(householdID);

    mHouseholdListener =
        householdDoc.addSnapshotListener(
            new EventListener<DocumentSnapshot>() {
              @Override
              public void onEvent(
                  @Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                  Log.w(TAG, "Listen failed.", e);
                  return;
                }

                if (snapshot != null) {
                  if (snapshot.exists()) {
                    Log.d(TAG, "Household found with id " + householdID);

                    mHousehold = buildHousehold(snapshot);

                    // Set the user this household
                    if (mFirebaseUser != null) {
                      Log.d(TAG, "Setting user to new household");
                      mFirestore
                          .collection(HOUSEHOLD_COLLECTION_NAME)
                          .document(householdID)
                          .collection(USERS_COLLECTION_NAME)
                          .document(mFirebaseUser.getFirebaseId())
                          .set(mFirebaseUser);
                    } else {
                      Log.w(TAG, "User is null, unable to assign to household");
                    }

                    // Household has been found and set, so get the Users and Tasks
                    queryTasks();
                    queryUsers();

                    // Use the callback for success
                    callbackHousehold(false);
                    return;

                  } else {
                    Log.d(TAG, "No household found");
                    clearData();
                  }
                } else {
                  Log.w(TAG, "Household query is null");
                  clearData();
                }
                callbackHousehold(true);
              }
            });
  }

  // Rebuilds the list of tasks to the current household
  private void queryTasks() {
    if (mHousehold == null) {
      Log.w(TAG, "Household is null, no tasks can be queried");
      return;
    }

    if (getTaskCollection() == null) {
      Log.w(TAG, "No task collection available to query");
      return;
    }

    // Deactivate the previous listener if it exists
    if (mTasksListener != null) {
      mTasksListener.remove();
      mTasksListener = null;
    }

    mTasksListener =
        getTaskCollection()
            .addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                  @Override
                  public void onEvent(
                      @Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                      Log.w(TAG, "Tasks collection lookup failed:", e);
                      return;
                    }

                    if (snapshot != null) {
                      if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                        Log.d(TAG, "No tasks found");
                        // No tasks, but that's still valid
                        mTasks.clear();
                      } else {
                        Log.d(TAG, "Found " + snapshot.getDocuments().size() + " tasks");

                        // Tasks have been found, so create a new list
                        mTasks.clear();
                        for (DocumentSnapshot d : snapshot.getDocuments()) {
                          mTasks.add(buildTask(d));
                        }
                      }
                      callbackTasks(false);
                      return;
                    } else {
                      Log.w(TAG, "Query result for tasks is null");
                    }
                    callbackTasks(true);
                  }
                });
  }

  // Rebuilds the list of tasks to the current household
  private void queryUsers() {
    if (mHousehold == null) {
      Log.w(TAG, "Household is null, no users can be queried");
      return;
    }

    if (getUserCollection() == null) {
      Log.w(TAG, "No user collection available to query");
      return;
    }

    // Deactivate the previous listener if it exists
    if (mUsersListener != null) {
      mUsersListener.remove();
      mUsersListener = null;
    }

    mUsersListener =
        getUserCollection()
            .addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                  @Override
                  public void onEvent(
                      @Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                      Log.w(TAG, "User collection lookup failed:", e);
                      return;
                    }

                    if (snapshot != null) {
                      if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                        Log.d(TAG, "No users found");
                        // TODO: This is probably a really bad state...
                      } else {
                        Log.d(TAG, "Found " + snapshot.getDocuments().size() + " users");

                        // Users have been found, so create a new list
                        mUsers.clear();
                        for (DocumentSnapshot d : snapshot.getDocuments()) {
                          mUsers.add(buildUser(d));
                        }
                      }

                      // Use the callback
                      callbackUsers(false);
                      return;
                    } else {
                      Log.w(TAG, "Query result for users is null");
                    }
                    callbackUsers(true);
                  }
                });
  }

  /* Helper methods */

  // Builds and returns a household object from the provided document snapshot
  private HouseholdModel buildHousehold(DocumentSnapshot householdData) {
    return householdData.toObject(HouseholdModel.class);
  }

  // Builds and returns a task object from document
  private TaskModel buildTask(DocumentSnapshot taskData) {
    return taskData.toObject(TaskModel.class);
  }

  // Builds and returns a user object from document
  private UserModel buildUser(DocumentSnapshot userData) {
    return userData.toObject(UserModel.class);
  }

  private CollectionReference getTaskCollection() {
    if (mHousehold != null) {
      return mFirestore
          .collection(HOUSEHOLD_COLLECTION_NAME)
          .document(mHousehold.getHouseholdId())
          .collection(TASK_COLLECTION_NAME);
    }
    return null;
  }

  private CollectionReference getUserCollection() {
    if (mHousehold != null) {
      return mFirestore
          .collection(HOUSEHOLD_COLLECTION_NAME)
          .document(mHousehold.getHouseholdId())
          .collection(USERS_COLLECTION_NAME);
    }
    return null;
  }

  // Removes all local data and references for the current household
  private void clearData() {
    Log.d(TAG, "Clearing all data");

    // Remove listeners if they exist
    if (mHouseholdListener != null) {
      mHouseholdListener.remove();
      mHouseholdListener = null;
    }
    if (mTasksListener != null) {
      mTasksListener.remove();
      mTasksListener = null;
    }
    if (mUsersListener != null) {
      mUsersListener.remove();
      mUsersListener = null;
    }

    // Clear local data
    mHousehold = null;
    mUsers.clear();
    mTasks.clear();
  }
}
