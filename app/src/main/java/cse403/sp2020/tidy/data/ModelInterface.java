package cse403.sp2020.tidy.data;

import java.util.List;
import java.util.ArrayList;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.*;

import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

/*
 * IMPORTANT NOTE: ID fields are the only required fields for data models
 * All other fields are arbitrary, and can be added or removed as needed
 *
 * Firestore Notes:
 *  - Firestore uses a local cache, so not every query goes to Cloud Firestore DB
 *  - ALL db changes should be done directly on the db (will affect local cache and remote)
 *  - Operation speed for any lookup/change will likely be very quick, especially if data is cached
 *  - Add event listeners for realtime updates, use get with success/fail/complete for one-time
 *  - Documents can be create by / used to create objects (See helper methods and add methods)
 *
 * TODO List:
 *  - Add checks for moving users between households
 *     (i.e. should check first if a user is part of a household and remove them)
 *  - Go through other TODOs
 *
 * Dev info:
 *  Public Methods
 *   Interface Methods:
 *    - Allow for retrieval and modification of database entities
 *    - DO NOT cache/store any data retrieved from these methods, query on every usage
 *      OR better yet, use callbacks to get updates automatically
 *
 *   Callback Register Methods:
 *    - Allows for registering classes that implement the callback interfaces
 *    - These methods will be called on listener updates for respective type
 *
 *  Private Methods
 *   Query Methods:
 *    - Private methods for querying data and setting up listeners
 *    - Ideally only called once per household setup (initial and change of household),
 *       Event listeners will handle the rest if used with callbacks
 *    - Querying of a new household will cascade to other queries for tasks and users
 *
 *   Callback Methods
 *    - Called on changes to respective data (Household, Users, Tasks),
 *       with differentiation of success/failure
 *    - Also called in some special cases, like
 *
 *   Helper Methods:
 *   - Build methods used to construct the model objects from query data
 *   - ClearData used to clear all current household data and listeners
 */

/*** Interface Methods ***
 *
 *  cleanUp()
 *   - removes all local data (not firestore cache) and disables listeners
 *   - removes callbacks as well
 *
 *  getCurrentUser()
 *   - Returns a UserModel object that represents the current user, or null if no user
 *
 *  updateCurrentUser(UserModel)
 *   - If the current user exists (either unassigned or in a household), metadata will be updated
 *   - Ignores the firebaseId in the provided model, in favor of the current user firebaseId
 *   - On failure, triggers user failure callback
 *   - On success, triggers user callback (potentially null if not part of a household)
 *
 *  setUser(String)
 *   - Attempts to set the user with the following id
 *      If the user is part of a household, user metadata and all household data will be gathered
 *      If the user is unassigned, no household is set but user metadata is recovered
 *      If the user does not exists, a new (empty) user will be created and set in unassigned
 *   - On failure, triggers callbackUsers fail callback
 *   - On success with no household, triggers user fail callback
 *   - On success with household, triggers all callbacks with up to date info for the user
 *
 *  makeHousehold(HouseholdModel)
 *   - Creates a new household entry with the info in HouseholdModel param
 *      If the ID field is null, a new ID will be autogenerated (this is recommended for now)
 *   - On failure, triggers household fail callback
 *   - On success, continues to setHousehold(HouseModel) (See below)
 *
 *  setHousehold(HouseModel)
 *   - Puts the current user (if one exists) into the household
 *   - On failure, triggers household fail callback
 *   - On success, triggers all callbacks with up to date info
 *
 *  removeUserFromHousehold()
 *   - Removes the current user from the current household (if both the user and teh household exist)
 *   - On failure, triggers household fail callback
 *   - On success, triggers household callback
 *     (NOTE: all local data is cleared, household will be null)
 *
 *  addTaskToHousehold(TaskModel)
 *   - Adds the provided task to the current household, if household exists
 *   - Tasks include ID fields, but this is set by the backend (so don't worry about it)
 *   - On failure, triggers task fail callback
 *   - On success, triggers task callback with updated task list
 *
 *  updateTask(TaskModel)
 *   - If the task exists, updates it
 *   - On failure, triggers task fail callback
 *   - On success, triggers task callback with updated task list
 *
 *  removeTaskToHousehold(TaskModel)
 *   - Removes the provided task from the current household, if both exists
 *   - On failure, triggers task fail callback
 *   - On success, triggers task callback with updated task list
 *
 *  registerTYPECallback(TYPECallbackInterface)
 *   - Takes in an object that implements the particular callback interface
 *   - After registering, all new callbacks will use the success/failure callbacks in the object
 *   - NOTE: Only one callback object can be registered per type of callback
 *
 *  getTYPE()
 *   - Returns a copy of the most up to date info for TYPE
 *   - Not needed in most cases, use the callbacks instead
 */

/** Front end interface for accessing Firebase data */
public class ModelInterface {
  // Log info
  private static final String TAG = "ModelInterface";

  // Firestore Constants
  private static final String HOUSEHOLD_COLLECTION_NAME = "Households";
  private static final String TASK_COLLECTION_NAME = "Tasks";
  private static final String USERS_COLLECTION_NAME = "Users";
  private static final String USER_ID_FIELD = "firebaseId";
  private static final String UNASSIGNED_USER_COLLECTION_NAME = "Unassigned";

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
  // Takes a Firestore instance
  public ModelInterface(FirebaseFirestore firestore) {
    Log.d(TAG, "Building ModelInterface");
    mFirestore = firestore;

    // Initialize data
    mHousehold = null;
    mTasks = new ArrayList<>();
    mUsers = new ArrayList<>();

    // Initialize Listeners
    mHouseholdListener = null;
    mTasksListener = null;
    mUsersListener = null;

    // Initialize Callbacks
    mHouseholdCallback = null;
    mTaskCallback = null;
    mUserCallback = null;

    // Initialize user
    mFirebaseUser = null;
  }

  // Use to clear all data and stop listeners
  // Also removes callbacks
  public void cleanUp() {
    clearData(true);
  }

  /* Interface Methods */

  // Returns the current user if it exists
  public UserModel getCurrentUser() {
    if (mFirebaseUser != null) {
      return new UserModel(mFirebaseUser);
    }
    return null;
  }

  // Updates the metadata based on the info in the updated model
  // DOES NOT update the firebaseId
  public void updateCurrentUser(UserModel updateData) {
    if (mFirebaseUser == null) {
      Log.w(TAG, "Current user is null, cannot update");
      return;
    }

    if (updateData == null) {
      Log.w(TAG, "User update data is null, cannot update");
      return;
    }

    final boolean isUnassigned = getUserCollection() == null;
    final UserModel dataCopy = new UserModel(updateData);

    // Make sure firebase id is not overridden
    dataCopy.setFirebaseId(mFirebaseUser.getFirebaseId());

    // Get the doc with the current user
    DocumentReference userDoc;
    if (isUnassigned) {
      userDoc =
          mFirestore
              .collection(UNASSIGNED_USER_COLLECTION_NAME)
              .document(updateData.getFirebaseId());
    } else {
      userDoc = getUserCollection().document(updateData.getFirebaseId());
    }

    userDoc
        .set(updateData)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "User updated");
                mFirebaseUser = dataCopy;
                if (isUnassigned) {
                  // manually trigger a callback, since unassigned does not have a listener
                  callbackUsers(false);
                }
              } else {
                Log.w(TAG, "Failed to update user: " + task.getException());
                callbackUsers(true);
              }
            });
  }

  public Uri getSharingLink() {
    String houseHoldLink = "https://tidy.household/" + this.mHousehold.getHouseholdId();
    Log.d("DYNAMIC_LINK", "Creating dynamic link with this household link: " + houseHoldLink);
    DynamicLink dynamicLink =
        FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(houseHoldLink))
            .setDomainUriPrefix("https://tidy403.page.link")
            // Open links with this app on Android
            .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
            .buildDynamicLink();

    Uri dynamicUri = dynamicLink.getUri();
    Log.d("DYNAMIC_LINK", "Got this dynamic uri: " + dynamicUri.toString());
    return dynamicUri;
  }

  // Takes in the new user to set, ignores if it is the same user
  // If a new user is set, removes all current data and restarts the queries
  public void setUser(String firebaseId) {
    if (firebaseId == null) {
      Log.w(TAG, "Trying to set user with a null id");
      return;
    }
    if (mFirebaseUser != null && firebaseId.equals(mFirebaseUser.getFirebaseId())) {
      Log.w(TAG, "No change to user, ignoring update");
      return;
    }

    // Try to find the user, create an entry if not found
    Log.d(TAG, "Change in user, clearing local data and stopping listeners");
    clearData();
    queryUserById(firebaseId);
  }

  // Creates a household and assigns the current user to it
  // Uses any metadata store in the input household
  // If householdId is null, auto-generates one
  public void makeHousehold(final HouseholdModel household) {
    // MUST have a user to make a new household
    if (mFirebaseUser == null) {
      Log.w(TAG, "No user to create a household for");
      return;
    }

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
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "Household has been created");
                // Household created, assign user to it
                setHousehold(household);
              } else {
                Log.w(TAG, "Failed to create a new household: " + task.getException());
                callbackHousehold(true);
              }
            });
  }

  // Takes in a household object to put the user in
  public void setHousehold(final HouseholdModel household) {
    // MUST have a user to set household
    if (mFirebaseUser == null) {
      Log.w(TAG, "No user to put into a household");
      return;
    }

    Log.d(TAG, "Attempting to set household");
    if (household == null) {
      Log.w(TAG, "set household object is null");
      return;
    }

    // Check that household exists first
    mFirestore
        .collection(HOUSEHOLD_COLLECTION_NAME)
        .document(household.getHouseholdId())
        .get()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "Household found, proceeding to set");

                if (task.getResult().exists()) {
                  // Create a batch to move a user from the unassigned batch to the household
                  WriteBatch batch = mFirestore.batch();
                  DocumentReference moveToDoc =
                      mFirestore
                          .collection(HOUSEHOLD_COLLECTION_NAME)
                          .document(household.getHouseholdId())
                          .collection(USERS_COLLECTION_NAME)
                          .document(mFirebaseUser.getFirebaseId());
                  DocumentReference unassignedDoc =
                      mFirestore
                          .collection(UNASSIGNED_USER_COLLECTION_NAME)
                          .document(mFirebaseUser.getFirebaseId());
                  batch.delete(unassignedDoc);
                  batch.set(moveToDoc, mFirebaseUser);
                  batch
                      .commit()
                      .addOnCompleteListener(
                          task1 -> {
                            if (task1.isSuccessful()) {
                              Log.d(TAG, "User has been assigned to household");

                              // Set a new household and trigger callback
                              mHousehold = household;
                              callbackHousehold(false);

                              // User has been assigned a new household, so get the data
                              queryTasks();
                              queryUsers();

                            } else {
                              Log.w(
                                  TAG,
                                  "Failed to assign user to household: " + task1.getException());
                              callbackHousehold(false);
                            }
                          });
                } else {
                  // No household
                  callbackHousehold(true);
                }
              } else {
                Log.w(TAG, "Failed to find household: " + task.getException());
              }
            });
  }

  public void updateHousehold(HouseholdModel household) {
    if (mHousehold == null) {
      Log.w(TAG, "No household to update");
      callbackHousehold(true);
    } else {
      mFirestore
          .collection(HOUSEHOLD_COLLECTION_NAME)
          .document(mHousehold.getHouseholdId())
          .set(household)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Log.w(TAG, "Household updated successfully");
                } else {
                  Log.w(TAG, "Failed to update household: " + task.getException());
                  callbackTasks(true);
                }
              });
    }
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

    if (mHousehold == null) {
      Log.w(TAG, "User is not part of a household, ignoring");
      return;
    }

    if (getUserCollection() != null) {
      Log.d(TAG, "Moving user to unassigned collection: " + mFirebaseUser.getFirebaseId());

      // Create a batch to atomically remove the user from the household and add them to the
      // unassigned
      WriteBatch batch = mFirestore.batch();
      DocumentReference currentDoc = getUserCollection().document(mFirebaseUser.getFirebaseId());
      DocumentReference unassignedDoc =
          mFirestore
              .collection(UNASSIGNED_USER_COLLECTION_NAME)
              .document(mFirebaseUser.getFirebaseId());
      batch.delete(currentDoc);
      batch.set(unassignedDoc, mFirebaseUser);
      batch
          .commit()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Log.d(TAG, "User removed successfully");
                  // The user is gone from household, so household data is no longer relevant
                  clearData();
                  // Trigger household callback, data is now invalid
                  callbackHousehold(false);
                } else {
                  Log.w(TAG, "Failed to remove user: " + task.getException());
                  callbackHousehold(true);
                }
              });
    } else {
      Log.w(TAG, "No users collection, failed to remove user: " + mFirebaseUser.getFirebaseId());
    }
  }

  // Attempts to add the task to the household
  public void addTaskToHousehold(final TaskModel taskAdd) {
    if (mHousehold == null) {
      Log.w(TAG, "No household to add task to");
      return;
    }

    if (taskAdd == null) {
      Log.d(TAG, "Trying to add a null task, ignoring");
      return;
    }

    if (getTaskCollection() != null) {
      Log.w(TAG, "Attempting to add new task: " + taskAdd.getName());
      // Store the task object (but grab the id first)
      final DocumentReference taskDoc = getTaskCollection().document();
      taskAdd.setTaskId(taskDoc.getId());

      taskDoc
          .get()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  if (task.getResult().exists()) {
                    Log.w(TAG, "Task Found, ignoring add");
                    callbackTasks(true);
                  } else {
                    Log.d(TAG, "Adding task");
                    taskDoc
                        .set(taskAdd)
                        .addOnCompleteListener(
                            task1 -> {
                              if (task1.isSuccessful()) {
                                Log.w(TAG, "Task updated successfully");
                              } else {
                                Log.w(TAG, "Failed to update task: " + task1.getException());
                                callbackTasks(true);
                              }
                            });
                  }
                } else {
                  Log.w(TAG, "Task lookup failed: " + task.getException());
                  callbackTasks(true);
                }
              });

    } else {
      Log.w(TAG, "No tasks collection, failed to add task: " + taskAdd.getName());
      callbackTasks(true);
    }
  }

  public void updateTask(final TaskModel taskUpdate) {
    if (mHousehold == null) {
      Log.w(TAG, "No household to update chore in");
      return;
    }

    if (taskUpdate == null) {
      Log.d(TAG, "Trying to update a null task, ignoring");
      return;
    }

    if (getTaskCollection() != null) {
      Log.w(TAG, "Updating task: " + taskUpdate.getName());
      final DocumentReference taskDoc = getTaskCollection().document(taskUpdate.getTaskId());

      // See if task exists first, then update if it does
      taskDoc
          .get()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Log.w(TAG, "Task Found, updating");
                  taskDoc
                      .set(taskUpdate)
                      .addOnCompleteListener(
                          task1 -> {
                            if (task1.isSuccessful()) {
                              Log.w(TAG, "Task updated successfully");
                            } else {
                              Log.w(TAG, "Failed to update task: " + task1.getException());
                              callbackTasks(true);
                            }
                          });
                } else {
                  Log.w(TAG, "Failed to find task: " + task.getException());
                  callbackTasks(true);
                }
              });
    } else {
      Log.w(TAG, "No tasks collection, failed to update task: " + taskUpdate.getName());
      callbackTasks(true);
    }
  }

  // Attempts to remove the task from the household
  public void removeTaskFromHousehold(final TaskModel taskDelete) {
    if (mHousehold == null) {
      Log.w(TAG, "No household to remove task from");
      return;
    }

    if (taskDelete == null) {
      Log.w(TAG, "Trying to remove a null task, ignoring");
      return;
    }

    if (getTaskCollection() != null) {
      Log.w(TAG, "Removing task: " + taskDelete.getName());
      final DocumentReference taskDoc = getTaskCollection().document(taskDelete.getTaskId());

      // See if task exists first, then remove if it does
      taskDoc
          .get()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Log.d(TAG, "Tasks removes");
                  taskDoc
                      .delete()
                      .addOnCompleteListener(
                          task1 -> {
                            if (task1.isSuccessful()) {
                              // Note: Deleting non-existing documents DOES NOT fail, no way to
                              // tell either
                              Log.d(TAG, "Task removed successfully");
                            } else {
                              Log.w(TAG, "Failed to remove task: " + task1.getException());
                              callbackTasks(true);
                            }
                          });
                } else {
                  Log.w(TAG, "Failed to find task: " + task.getException());
                  callbackTasks(true);
                }
              });
    } else {
      Log.w(TAG, "No tasks collection, failed to remove task: " + taskDelete.getName());
      callbackTasks(true);
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
  private void callbackHousehold(boolean failed, String message) {
    if (mHouseholdCallback == null) {
      Log.d(TAG, "No household callback");
      return;
    }

    if (failed) {
      Log.d(TAG, "callback fail on Household");
      mHouseholdCallback.householdCallbackFailed(message);
    } else {
      Log.d(TAG, "callback on Household");
      mHouseholdCallback.householdCallback(getHousehold());
    }
  }

  private void callbackHousehold(boolean failed) {
    callbackHousehold(failed, null);
  }

  // Called when the tasks have been updated
  private void callbackTasks(boolean failed, String message) {
    if (mTaskCallback == null) {
      Log.d(TAG, "No task callback");
      return;
    }

    if (failed) {
      Log.d(TAG, "callback fail on Tasks");
      mTaskCallback.taskCallbackFail(message);
    } else {
      Log.d(TAG, "callback on Tasks");
      mTaskCallback.taskCallback(getTasks());
    }
  }

  private void callbackTasks(boolean failed) {
    callbackTasks(failed, null);
  }

  // Called when the users have been updated
  private void callbackUsers(boolean failed, String message) {
    if (mUserCallback == null) {
      Log.d(TAG, "No user callback");
      return;
    }

    if (failed) {
      Log.d(TAG, "callback fail on Users");
      mUserCallback.userCallbackFailed(message);
    } else {
      Log.d(TAG, "callback on Users");
      mUserCallback.userCallback(getUsers());
    }
  }

  private void callbackUsers(boolean failed) {
    callbackUsers(failed, null);
  }

  /* Query functions */

  // See if a user with the provided id exists in a household
  // Moves to checking unassigned if not found
  private void queryUserById(final String firebaseId) {
    if (firebaseId == null) {
      Log.d(TAG, "Provided user Id is null");
      return;
    }
    Log.d(TAG, "Checking households for user: " + firebaseId);

    mFirestore
        .collectionGroup(USERS_COLLECTION_NAME)
        .whereEqualTo(USER_ID_FIELD, firebaseId)
        .get()
        .addOnSuccessListener(
            snapshot -> {
              if (snapshot != null) {
                if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                  Log.d(TAG, "User not found to be part of a household");
                  // No user by this id in a household, check the unassigned collection
                  queryUserByIdUnassigned(firebaseId);
                  return;

                } else {
                  Log.d(TAG, "Found " + snapshot.getDocuments().size() + " user with that ID");
                  if (snapshot.getDocuments().size() > 1) {
                    Log.w(TAG, "Multiple entries of user found, logging them");
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                      Log.w(TAG, "UserID: " + d.getId() + ", data: " + d.getData());
                    }
                  }

                  // Get the (first) user entry, and create a user model from it
                  mFirebaseUser = buildUser(snapshot.getDocuments().get(0));
                  // callbackUsers(true);
                  // TODO: Is this needed? may leave a gap in user callbacks
                  //  Maybe as status? new user set?

                  // User found within a household, so get household data
                  queryHouseholdIdByUser();
                  return;
                }
              } else {
                Log.w(TAG, "Query result for user is null");
                clearData();
              }

              // Problems when trying to find a user
              callbackUsers(true);
            })
        .addOnFailureListener(
            e -> {
              Log.w(TAG, "Failed to find user in household by id: ", e);
              callbackUsers(true);
            });
  }

  private void queryUserByIdUnassigned(final String firebaseId) {
    if (firebaseId == null) {
      Log.w(TAG, "Trying to find unassigned user with a null id");
      return;
    }

    // Check the unassigned collection for an existing user
    mFirestore
        .collection(UNASSIGNED_USER_COLLECTION_NAME)
        .whereEqualTo(USER_ID_FIELD, firebaseId)
        .get()
        .addOnSuccessListener(
            snapshot -> {
              if (snapshot != null) {
                if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                  Log.d(TAG, "User not found in unassigned, creating new user");

                  // This user does not exist yet, create them without a household
                  UserModel newUser = new UserModel();
                  newUser.setFirebaseId(firebaseId);
                  mFirebaseUser = newUser;
                  mFirestore
                      .collection(UNASSIGNED_USER_COLLECTION_NAME)
                      .document(firebaseId)
                      .set(newUser);
                  callbackUsers(true);
                  return;
                } else {
                  Log.d(
                      TAG, "Found " + snapshot.getDocuments().size() + " households with user ID");
                  if (snapshot.getDocuments().size() > 1) {
                    Log.w(TAG, "Multiple entries of user found, logging them");
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                      Log.w(TAG, "UserID: " + d.getId() + ", data: " + d.getData());
                    }
                  }

                  // Get the (first) user entry, and create a user model from it
                  mFirebaseUser = buildUser(snapshot.getDocuments().get(0));

                  // No household for this user, so initiate a callback
                  callbackUsers(false);
                  return;
                }
              } else {
                Log.w(TAG, "Query result for user is null");
                clearData();
              }

              // No user was found
              callbackUsers(true);
            })
        .addOnFailureListener(e -> Log.w(TAG, "Failed to find household by user ID", e));
  }

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
            snapshot -> {
              if (snapshot != null) {
                // TODO: are both needed?
                if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                  Log.d(TAG, "User not found to be part of a household");
                  clearData();
                } else {
                  Log.d(
                      TAG, "Found " + snapshot.getDocuments().size() + " households with user ID");

                  // From the first document, get the parent (Users collection),
                  // then parent's parent (Household document)
                  String householdID =
                      snapshot.getDocuments().get(0).getReference().getParent().getParent().getId();
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
            })
        .addOnFailureListener(e -> Log.w(TAG, "Failed to find household by user ID", e));
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
            (snapshot, e) -> {
              if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
              }

              if (snapshot != null) {
                if (snapshot.exists()) {
                  Log.d(TAG, "Household found with id " + householdID);

                  mHousehold = buildHousehold(snapshot);

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
                (snapshot, e) -> {
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
                (snapshot, e) -> {
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
    if (mHousehold != null && mHousehold.getHouseholdId() != null) {
      return mFirestore
          .collection(HOUSEHOLD_COLLECTION_NAME)
          .document(mHousehold.getHouseholdId())
          .collection(TASK_COLLECTION_NAME);
    }
    return null;
  }

  private CollectionReference getUserCollection() {
    if (mHousehold != null && mHousehold.getHouseholdId() != null) {
      return mFirestore
          .collection(HOUSEHOLD_COLLECTION_NAME)
          .document(mHousehold.getHouseholdId())
          .collection(USERS_COLLECTION_NAME);
    }
    return null;
  }

  private void clearData() {
    clearData(false);
  }

  // Removes all local data and references for the current household
  private void clearData(boolean removeCallbacks) {
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

    // Remove the callbacks
    if (removeCallbacks) {
      mUserCallback = null;
      mTaskCallback = null;
      mHouseholdCallback = null;
    }

    // Clear local data
    mHousehold = null;
    mUsers.clear();
    mTasks.clear();
  }
}
