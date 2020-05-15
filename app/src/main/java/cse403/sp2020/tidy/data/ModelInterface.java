package cse403.sp2020.tidy.data;

import java.util.List;
import java.util.ArrayList;

import android.util.Log;

import com.google.firebase.firestore.*;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

/**
 * ModelInterface provides controlled access to the Firestore database. Functional Details: - It is
 * user-centric, meaning no operations will work before setting a user. - The majority of operations
 * also require the user to be in a household. - Aside from get*, all methods will use a callback
 * interface to return the result.
 *
 * <p>Database Details: - Callbacks leverage Firestore's real-time capabilities, which allows for
 * live updates. - Callbacks are asynchronous, but will usually happen very quickly due caching. -
 * Caching also significantly reduces network usage, even when there are a lot of changes.
 */
public class ModelInterface {
  // Log info
  private static final String TAG = "ModelInterface";

  // Firestore Constants
  private static final String HOUSEHOLD_COLLECTION_NAME = "Households";
  private static final String TASK_COLLECTION_NAME = "Tasks";
  private static final String USERS_COLLECTION_NAME = "Users";
  private static final String USER_ID_FIELD = "firebaseId";
  private static final String UNASSIGNED_USER_COLLECTION_NAME = "Unassigned";

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

    // Initialize user
    mFirebaseUser = null;
  }

  /* Utility methods */

  /** Removes all listeners Clears all local data in the interface */
  public void cleanUp() {
    clearData();
  }

  /* Single callback Methods */

  /**
   * Attempts to find the user by id, creates the user if not found. If the user is part of a
   * household, will set this as well. Clears all current data and replaces with new user. Returns
   * the newly set user model via callback, or null on failure.
   *
   * @param firebaseId Firebase Id string of the currently authenticated user.
   * @param callback Interface that accepts a {@link UserModel}.
   */
  public void setCurrentUser(final String firebaseId, final CallbackInterface<UserModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- setCurrentUser");
      return;
    }
    if (firebaseId == null) {
      Log.w(TAG, "Trying to set user with a null id");
      callback.callback(null);
      return;
    }

    opFindOrCreateUser(firebaseId, callback);
  }

  /**
   * Attempts to set the household of the current user. Requires that current user is set. Returns
   * the new household via callback, or null on failure.
   *
   * @param householdId Id string of the intended household to join.
   * @param callback Interface that accepts a {@link HouseholdModel}.
   */
  public void setCurrentHousehold(
      String householdId, final CallbackInterface<HouseholdModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- setCurrentHousehold");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot set household");
      callback.callback(null);
      return;
    }
    if (householdId == null) {
      Log.w(TAG, "Household Id is null, cannot set household");
      callback.callback(null);
      return;
    }

    opFindAndSetHousehold(householdId, callback);
  }

  /**
   * Attempts to create a household with the provided metadata. Requires that the current user is
   * set AND not in a household. Returns the new household object via callback, or null on failure.
   *
   * @param household Object containing metadata for a new household. Id field is ignored, an new
   *     one is auto-generated.
   * @param callback Interface that accepts a {@link HouseholdModel}.
   */
  public void createHousehold(
      final HouseholdModel household, final CallbackInterface<HouseholdModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- createHousehold");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot create household");
      callback.callback(null);
      return;
    }
    if (mHousehold != null) {
      Log.w(TAG, "User is in a household, cannot create a new one");
      callback.callback(null);
      return;
    }
    if (household == null) {
      Log.w(TAG, "Create data is null, cannot update household");
      callback.callback(null);
      return;
    }

    // Create a new household object with cleared Id
    HouseholdModel newHousehold = new HouseholdModel(household);
    newHousehold.setHouseholdId(null);

    opCreateHousehold(newHousehold, callback);
  }

  /**
   * Attempts to update the household of the current user with the provided metadata Requires that
   * the current user is set AND is in a household Returns the updated household via callback, or
   * null on failure
   *
   * @param household Object containing metadata to update current household with. Id field is
   *     ignored, the current household's Id will be used.
   * @param callback Interface that accepts a {@link HouseholdModel}
   */
  public void updateHousehold(
      final HouseholdModel household, final CallbackInterface<HouseholdModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- updateHousehold");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot update household");
      callback.callback(null);
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot update household");
      callback.callback(null);
      return;
    }
    if (household == null) {
      Log.w(TAG, "Update data is null, cannot update household");
      callback.callback(null);
      return;
    }

    opUpdateHousehold(household, callback);
  }

  /**
   * Attempts to update the current user with the provided metadata. Requires that the current user
   * is set. Returns the updated user via callback, or null on failure.
   *
   * @param user Object containing metadata to update current user with. Id field is ignored, the
   *     current users's Id will be used.
   * @param callback Interface that accepts a {@link UserModel}.
   */
  public void updateCurrentUser(final UserModel user, final CallbackInterface<UserModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- updateCurrentUser");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot update user");
      callback.callback(null);
      return;
    }
    if (user == null) {
      Log.w(TAG, "Update data is null, cannot update user");
      callback.callback(null);
      return;
    }

    // Make sure id is not overridden
    user.setFirebaseId(mFirebaseUser.getFirebaseId());

    opUpdateUser(user, callback);
  }

  /**
   * Attempts to remove the user from the current household. Requires that the current user is set
   * AND is in a household. Returns the current user via callback, or null on failure.
   *
   * @param callback Interface that accepts a {@link UserModel}.
   */
  public void removeUserFromHousehold(final CallbackInterface<UserModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- removeUserFromHousehold");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot remove user from household");
      callback.callback(null);
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot remove user from household");
      callback.callback(null);
      return;
    }
    if (getUserCollection() == null) {
      Log.w(TAG, "No users collection, cannot remove user from household");
      callback.callback(null);
      return;
    }

    opRemoveUserFromHousehold(callback);
  }

  /**
   * Attempts to add the provided task to the household. Ignores the id in the provided object and
   * auto-generates a new one. Requires that the current user is set AND is in a household. Returns
   * the task object via callback, or null on failure.
   *
   * @param task Task object with metadata to create task with. Id field is ignored, a new one is
   *     auto-generated.
   * @param callback Interface that accepts a {@link TaskModel}.
   */
  public void addTask(final TaskModel task, final CallbackInterface<TaskModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- addTask");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot add task to household");
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot add task to household");
      callback.callback(null);
      return;
    }
    if (getTaskCollection() == null) {
      Log.w(TAG, "No tasks collection, cannot add task to household");
      callback.callback(null);
      return;
    }
    if (task == null) {
      Log.w(TAG, "Task data is null, cannot add task to household");
      callback.callback(null);
      return;
    }

    opAddTask(task, callback);
  }

  /**
   * Attempts to update the task in the current household. Requires that the current user is set AND
   * is in a household. Returns the task object via callback, or null on failure.
   *
   * @param task Task object with metadata to update task with. Uses Id field to find task.
   * @param callback Interface that accepts a {@link TaskModel}.
   */
  public void updateTask(final TaskModel task, final CallbackInterface<TaskModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- updateTask");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot update task");
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot update task");
      callback.callback(null);
      return;
    }
    if (getTaskCollection() == null) {
      Log.w(TAG, "No tasks collection, cannot update task");
      callback.callback(null);
      return;
    }
    if (task == null || task.getTaskId() == null) {
      Log.w(TAG, "Task or task id is null, cannot update task");
      callback.callback(null);
      return;
    }

    opUpdateTask(task, callback);
  }

  /**
   * Attempts to remove the task from the current household. Requires that the current user is set
   * AND is in a household. Returns the deleted task object via callback, or null on failure.
   *
   * @param task Task object to delete, ignores metadata fields. Uses Id field to find task.
   * @param callback Interface that accepts a {@link TaskModel}.
   */
  public void removeTask(final TaskModel task, final CallbackInterface<TaskModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- addTask");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot remove task from household");
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot remove task from household");
      callback.callback(null);
      return;
    }
    if (getTaskCollection() == null) {
      Log.w(TAG, "No tasks collection, cannot remove task from household");
      callback.callback(null);
      return;
    }
    if (task == null || task.getTaskId() == null) {
      Log.w(TAG, "Task or task id is null, cannot remove task from household");
      callback.callback(null);
      return;
    }

    opRemoveTask(task, callback);
  }

  /* Get (No Callback) Methods */

  /**
   * If the user is not set, all other operations will not work
   *
   * @return returns the current user, or null if there isn't one.
   */
  public UserModel getCurrentUser() {
    if (mFirebaseUser != null) return new UserModel(mFirebaseUser);
    return null;
  }

  /**
   * The current household represents if the user is part of a household. If it is null, the user
   * has no household (or isn't set). If there isn't an a active listener for this type, data may be
   * inaccurate.
   *
   * @return returns the current household, or null if there isn't one.
   */
  public HouseholdModel getHousehold() {
    if (mHousehold != null) return new HouseholdModel(mHousehold);
    return null;
  }

  /**
   * The current list is dependent on an active listener for users. If there isn't an a active
   * listener for this type, data may be inaccurate. Recommendation is to simply use the listener
   * callback.
   *
   * @return returns a potentially empty {@link List} of {@link UserModel}s. returns null if there
   *     isn't a household set.
   */
  public List<UserModel> getUsers() {
    if (mHousehold != null) return new ArrayList<>(mUsers);
    return null;
  }

  /**
   * The current list is dependent on an active listener for tasks. If there isn't an a active
   * listener for this type, data may be inaccurate. Recommendation is to simply use the listener
   * callback.
   *
   * @return returns a potentially empty {@link List} of {@link TaskModel}s. returns null if there
   *     isn't a household set.
   */
  public List<TaskModel> getTasks() {
    if (mHousehold != null) return new ArrayList<>(mTasks);
    return null;
  }

  /* Event Listener (Repeat Callback) Methods */

  /**
   * Sets an event listener on the household document in the database. Removes any previous listener
   * for the household. Sets any updates to local household object. Requires the current user to
   * exist AND be in household.
   *
   * @param callback Interface that accepts a {@link HouseholdModel}. Will be called initially with
   *     current data. Will be called repeatedly with updates.
   */
  public void setHouseholdListener(final CallbackInterface<HouseholdModel> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- setHouseholdListener");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot listen on household");
      callback.callback(null);
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot listen on household");
      callback.callback(null);
      return;
    }

    opSetHouseholdListener(callback);
  }

  /**
   * Sets an event listener on the Users collection of the current household. Removes any previous
   * listener for the Users collection. Sets and updates the local list of users. Requires the
   * current user to exist AND be in household.
   *
   * @param callback Interface that accepts a {@link List} of {@link UserModel}s. Will be called
   *     initially with current data. Will be called repeatedly with updates.
   */
  public void setUsersListener(final CallbackInterface<List<UserModel>> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- setUsersListener");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot listen on users");
      callback.callback(null);
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot listen on users");
      callback.callback(null);
      return;
    }
    if (getUserCollection() == null) {
      Log.w(TAG, "User collection is null, cannot listen on users");
      callback.callback(null);
    }

    opSetUsersListener(callback);
  }

  /**
   * Sets an event listener on the Tasks collection of the current household. Removes any previous
   * listener for the Tasks collection. Sets and updates the local list of tasks. Requires the
   * current user to exist AND be in household.
   *
   * @param callback Interface that accepts a {@link List} of {@link TaskModel}s. Will be called
   *     initially with current data. Will be called repeatedly with updates.
   */
  public void setTasksListener(final CallbackInterface<List<TaskModel>> callback) {
    if (callback == null) {
      Log.w(TAG, "Callback is null -- setUsersListener");
      return;
    }
    if (mFirebaseUser == null) {
      Log.w(TAG, "User is null, cannot listen on tasks");
      callback.callback(null);
      return;
    }
    if (mHousehold == null) {
      Log.w(TAG, "User is not in household, cannot listen on tasks");
      callback.callback(null);
      return;
    }
    if (getTaskCollection() == null) {
      Log.w(TAG, "Task collection is null, cannot listen on tasks");
      callback.callback(null);
      return;
    }

    opSetTasksListener(callback);
  }

  /* Operations */

  private void opCreateHousehold(
      final HouseholdModel household, final CallbackInterface<HouseholdModel> callback) {
    // Generate a new household doc and Id
    DocumentReference householdDoc = mFirestore.collection(HOUSEHOLD_COLLECTION_NAME).document();
    Log.d(TAG, "Making new household: " + householdDoc.getId());
    household.setHouseholdId(householdDoc.getId());

    // Doc to move user to
    DocumentReference newDoc =
        householdDoc.collection(USERS_COLLECTION_NAME).document(mFirebaseUser.getFirebaseId());

    // Doc to remove user from
    DocumentReference oldDoc =
        mFirestore
            .collection(UNASSIGNED_USER_COLLECTION_NAME)
            .document(mFirebaseUser.getFirebaseId());

    // Create a batch write to move the user from unassigned to the new household
    WriteBatch batch = mFirestore.batch();
    batch
        .set(householdDoc, household)
        .set(newDoc, mFirebaseUser)
        .delete(oldDoc)
        .commit()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "Household has been created and user has been assigned");
                mHousehold = household;
                callback.callback(getHousehold());
              } else {
                Log.w(TAG, "Failed to create a new household: " + task.getException());
                callback.callback(null);
              }
            });
  }

  private void opFindOrCreateUser(
      final String firebaseId, final CallbackInterface<UserModel> callback) {
    // First, search all households for the user
    mFirestore
        .collectionGroup(USERS_COLLECTION_NAME)
        .whereEqualTo(USER_ID_FIELD, firebaseId)
        .get()
        .addOnSuccessListener(
            snapshot -> {
              if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                Log.d(TAG, "User not found to be part of a household, checking unassigned");

                // Search unassigned for the user
                mFirestore
                    .collection(UNASSIGNED_USER_COLLECTION_NAME)
                    .get()
                    .addOnCompleteListener(
                        task -> {
                          if (task.isSuccessful()) {
                            QuerySnapshot uSnapshot = task.getResult();
                            if (uSnapshot.isEmpty()) {
                              Log.d(TAG, "User not found in unassigned, creating new");
                              // User does not exist, create one
                              UserModel newUser = new UserModel();
                              newUser.setFirebaseId(firebaseId);
                              mFirestore
                                  .collection(UNASSIGNED_USER_COLLECTION_NAME)
                                  .document(firebaseId)
                                  .set(newUser)
                                  .addOnCompleteListener(
                                      utask -> {
                                        if (utask.isSuccessful()) {
                                          Log.d(TAG, "Created new user");
                                          // Clear existing data before setting current user
                                          clearData();
                                          mFirebaseUser = newUser;
                                          callback.callback(getCurrentUser());
                                        } else {
                                          Log.w(
                                              TAG, "Failed to create user: ", utask.getException());
                                          callback.callback(null);
                                        }
                                      });

                            } else {
                              // Found the user, callback with it
                              logExcess("Unassigned User", task.getResult());
                              Log.w(TAG, "User found in unassigned");
                              // Clear existing data before setting current user
                              clearData();
                              mFirebaseUser = buildUser(task.getResult().getDocuments().get(0));
                              callback.callback(getCurrentUser());
                            }

                          } else {
                            Log.w(TAG, "Failed to search unassigned: ", task.getException());
                            callback.callback(null);
                          }
                        });
              } else {
                logExcess("User", snapshot);

                // Get the household the user belongs to first, then use callback
                DocumentSnapshot userDoc = snapshot.getDocuments().get(0);
                DocumentReference household = userDoc.getReference().getParent().getParent();
                household
                    .get()
                    .addOnCompleteListener(
                        task -> {
                          if (task.isSuccessful()) {
                            Log.w(TAG, "Got user's household: " + household.getId());
                            // Clear existing data before setting current user
                            clearData();
                            mHousehold = buildHousehold(task.getResult());
                            mFirebaseUser = buildUser(userDoc);
                            callback.callback(getCurrentUser());
                          } else {
                            Log.w(TAG, "Failed to get household ", task.getException());
                            callback.callback(buildUser(null));
                          }
                        });
              }
            })
        .addOnFailureListener(
            e -> {
              Log.w(TAG, "Failed to find user in household by id: ", e);
              callback.callback(null);
            });
  }

  private void opFindAndSetHousehold(
      String householdId, final CallbackInterface<HouseholdModel> callback) {

    Log.d(TAG, "Attempting to set current household");
    // Check that household exists first
    mFirestore
        .collection(HOUSEHOLD_COLLECTION_NAME)
        .document(householdId)
        .get()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                  Log.d(TAG, "Household found, proceeding to set");
                  // Get the household object
                  HouseholdModel household = buildHousehold(task.getResult());

                  // Create a batch to move a user from unassigned to the household
                  WriteBatch batch = mFirestore.batch();
                  DocumentReference moveToDoc =
                      mFirestore
                          .collection(HOUSEHOLD_COLLECTION_NAME)
                          .document(householdId)
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

                              // Set a new household and send it back
                              mHousehold = household;
                              callback.callback(getHousehold());
                            } else {
                              Log.w(
                                  TAG,
                                  "Failed to assign user to household: " + task1.getException());
                              callback.callback(null);
                            }
                          });
                } else {
                  Log.w(TAG, "No household found with Id");
                  callback.callback(null);
                }
              } else {
                Log.w(TAG, "Failed to find household: " + task.getException());
                callback.callback(null);
              }
            });
  }

  private void opUpdateHousehold(
      final HouseholdModel household, final CallbackInterface<HouseholdModel> callback) {
    mFirestore
        .collection(HOUSEHOLD_COLLECTION_NAME)
        .document(mHousehold.getHouseholdId())
        .set(household)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.w(TAG, "Household updated successfully");
                mHousehold = household;
                callback.callback(getHousehold());
              } else {
                Log.w(TAG, "Failed to update household: " + task.getException());
                callback.callback(null);
              }
            });
  }

  private void opUpdateUser(final UserModel user, final CallbackInterface<UserModel> callback) {
    Log.w(TAG, "Updating current user");
    mFirestore
        .collection(USERS_COLLECTION_NAME)
        .document(mFirebaseUser.getFirebaseId())
        .set(user)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "User updated successfully");
                UserModel newUser = new UserModel(user);
                newUser.setFirebaseId(mFirebaseUser.getFirebaseId());
                mFirebaseUser = newUser;
                callback.callback(getCurrentUser());
              } else {
                Log.w(TAG, "Failed to update user", task.getException());
                callback.callback(null);
              }
            });
  }

  private void opRemoveUserFromHousehold(final CallbackInterface<UserModel> callback) {
    Log.d(TAG, "Removing user from household");

    // Move user from current household to unassigned
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
                clearListeners(); // Important, user should not get updates from old household
                clearHousehold(); // Data from current household is no longer needed
                callback.callback(getCurrentUser());
              } else {
                Log.w(TAG, "Failed to remove user: " + task.getException());
                callback.callback(null);
              }
            });
  }

  private void opSetHouseholdListener(final CallbackInterface<HouseholdModel> callback) {
    Log.w(TAG, "Setting listener on household");

    if (mHouseholdListener != null) {
      mHouseholdListener.remove();
    }

    mHouseholdListener =
        mFirestore
            .collection(HOUSEHOLD_COLLECTION_NAME)
            .document(mHousehold.getHouseholdId())
            .addSnapshotListener(
                (snapshot, e) -> {
                  if (e != null || snapshot == null) {
                    Log.w(TAG, "Listen failed on household", e);
                    callback.callback(null);
                    return;
                  }

                  if (snapshot.exists()) {
                    Log.d(TAG, "Sending household update");
                    mHousehold = buildHousehold(snapshot);
                    callback.callback(buildHousehold(snapshot));
                  } else {
                    callback.callback(null);
                  }
                });
  }

  private void opSetUsersListener(final CallbackInterface<List<UserModel>> callback) {
    Log.w(TAG, "Setting listener on users");

    if (mUsersListener != null) {
      mUsersListener.remove();
    }

    mUsersListener =
        getUserCollection()
            .addSnapshotListener(
                (snapshot, e) -> {
                  if (e != null || snapshot == null) {
                    Log.w(TAG, "Listen failed on users", e);
                    callback.callback(null);
                    return;
                  }

                  Log.d(TAG, "Sending users list update");
                  mUsers.clear();
                  for (DocumentSnapshot doc : snapshot) {
                    mUsers.add(buildUser(doc));
                  }
                  callback.callback(getUsers());
                });
  }

  private void opSetTasksListener(final CallbackInterface<List<TaskModel>> callback) {
    Log.w(TAG, "Setting listener on tasks");

    if (mTasksListener != null) {
      mTasksListener.remove();
    }

    mTasksListener =
        getTaskCollection()
            .addSnapshotListener(
                (snapshot, e) -> {
                  if (e != null || snapshot == null) {
                    Log.w(TAG, "Listen failed on taskss", e);
                    callback.callback(null);
                    return;
                  }

                  Log.d(TAG, "Sending task list update");
                  mTasks.clear();
                  for (DocumentSnapshot doc : snapshot) {
                    mTasks.add(buildTask(doc));
                  }
                  callback.callback(getTasks());
                });
  }

  private void opAddTask(final TaskModel taskData, final CallbackInterface<TaskModel> callback) {
    final DocumentReference taskDoc = getTaskCollection().document();
    taskData.setTaskId(taskDoc.getId());

    taskDoc
        .set(taskData)
        .addOnCompleteListener(
            task1 -> {
              if (task1.isSuccessful()) {
                Log.w(TAG, "Task added successfully");
                callback.callback(taskData);
              } else {
                Log.w(TAG, "Failed to add task: " + task1.getException());
                callback.callback(null);
              }
            });
  }

  private void opUpdateTask(final TaskModel taskData, final CallbackInterface<TaskModel> callback) {
    final DocumentReference taskDoc = getTaskCollection().document(taskData.getTaskId());

    taskDoc
        .get()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                  taskDoc
                      .set(taskData)
                      .addOnCompleteListener(
                          task1 -> {
                            if (task1.isSuccessful()) {
                              Log.w(TAG, "Task updated successfully");
                              callback.callback(taskData);
                            } else {
                              Log.w(TAG, "Failed to add task: " + task1.getException());
                              callback.callback(null);
                            }
                          });
                } else {
                  Log.w(TAG, "Task not found, not updating task");
                  callback.callback(null);
                }
              } else {
                Log.w(TAG, "Task lookup failed during update: " + task.getException());
                callback.callback(null);
              }
            });
  }

  private void opRemoveTask(final TaskModel taskData, final CallbackInterface<TaskModel> callback) {
    final DocumentReference taskDoc = getTaskCollection().document(taskData.getTaskId());

    taskDoc
        .get()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                  final TaskModel deletedTask = buildTask(task.getResult());
                  taskDoc
                      .delete()
                      .addOnCompleteListener(
                          task1 -> {
                            if (task1.isSuccessful()) {
                              Log.w(TAG, "Task deleted successfully");
                              callback.callback(deletedTask);
                            } else {
                              Log.w(TAG, "Failed to add task: " + task1.getException());
                              callback.callback(null);
                            }
                          });
                } else {
                  Log.w(TAG, "Task not found, not deleting task");
                  callback.callback(null);
                }
              } else {
                Log.w(TAG, "Task lookup failed during delete: " + task.getException());
                callback.callback(null);
              }
            });
  }

  /* Helper methods */

  // Builds and returns a household object from the provided document snapshot
  private HouseholdModel buildHousehold(DocumentSnapshot householdData) {
    if (householdData != null) return householdData.toObject(HouseholdModel.class);
    return null;
  }

  // Builds and returns a task object from document
  private TaskModel buildTask(DocumentSnapshot taskData) {
    if (taskData != null) return taskData.toObject(TaskModel.class);
    return null;
  }

  // Builds and returns a user object from document
  private UserModel buildUser(DocumentSnapshot userData) {
    if (userData != null) return userData.toObject(UserModel.class);
    return null;
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

  // Remove listeners if they exist
  private void clearListeners() {
    Log.d(TAG, "Clearing listeners");
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
  }

  private void clearHousehold() {
    Log.d(TAG, "Clearing household data");
    mHousehold = null;
    mUsers.clear();
    mTasks.clear();
  }

  // Removes all local data and references for the current household
  private void clearData() {
    Log.d(TAG, "Clearing all data");

    // Stop any active listeners
    clearListeners();

    // Remove household data
    clearHousehold();

    // Remove user reference
    mFirebaseUser = null;
  }

  // Logs excess results when only one is expected
  private void logExcess(String name, QuerySnapshot snapshot) {
    Log.d(TAG, "Found " + snapshot.getDocuments().size() + " " + name + "(s)");
    if (snapshot.getDocuments().size() > 1) {
      Log.w(TAG, "Multiple entries found, logging them");
      for (DocumentSnapshot d : snapshot.getDocuments()) {
        Log.w(TAG, "ID: " + d.getId() + ", data: " + d.getData());
      }
    }
  }
}
