package cse403.sp2020.tidy.data;

import java.util.List;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.*;

import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

/*
 * Important Notes:
 *  - Firestore uses a local cache, so not every query goes to Firestore DB
 *  - ALL db changes should be done directly on the db (will affect local cache and remote)
 *  - Operation speed for any lookup/change will likely be very quick, especially if data is cached
 *  - Add event listeners for realtime updates, use get with success/fail/complete for one-time
 *  - Documents can be create by / used to create objects (See helper methods and add methods)
 *
 * TODO List:
 *  - Finish the remaining interface functions (mostly updaters)
 *  - Consider adding locks on object lists
 *  - Possibly add Auth listener?
 *  - Go through other TODOs
 */

/* ModelInterface info
 *  Interface Methods:
 *   - Public methods that allow retrieval and modification of database entities
 *   - DO NOT cache/store any data retrieved from these methods, query on every usage
 *
 *  Query Methods:
 *   - Private methods for querying data and setting up listeners
 *   - Ideally only called once per household setup (initial and change of household)
 *   - Querying of a new household will cascade to other queries for tasks and users
 *
 *  Helper Methods:
 *  - Used to construct the model objects from query data
 */

public class ModelInterface {
  // Log info
  private static final String TAG = "Model Interface";

  // Firestore Constants
  private static final String HOUSEHOLD_COLLECTION_NAME = "Households";
  private static final String HOUSEHOLD_ID_FIELD = "householdNum";
  private static final String TASK_COLLECTION_NAME = "Tasks";
  private static final String USERS_COLLECTION_NAME = "Users";
  private static final String USER_ID_FIELD = "firebaseId";

  // Household objects
  private HouseholdModel mHousehold;
  private List<TaskModel> mTasks;
  private List<UserModel> mUsers;

  // Firestore Listeners
  private ListenerRegistration mHouseholdListener;
  private ListenerRegistration mTasksListener;
  private ListenerRegistration mUsersListener;

  // Collection references
  private CollectionReference mTaskCollection;
  private CollectionReference mUserCollection;

  // Firestore Database instance
  private FirebaseFirestore mFirestore;

  // User
  private String mFirebaseUserID;

  // Constructor
  // Takes a Firestore instance and the current user
  public ModelInterface(FirebaseFirestore firestore, String userID) {
    mFirestore = firestore;

    // Initialize data
    mHousehold = null;
    mTasks = new ArrayList<TaskModel>();
    mUsers = new ArrayList<UserModel>();

    // Initialize Listeners
    mHouseholdListener = null;
    mTasksListener = null;
    mUsersListener = null;

    // Do initial queries if a user is present
    mFirebaseUserID = userID;
    if (mFirebaseUserID != null) {
      queryHouseholdIdByUser();
    } else {
      Log.w(TAG, "No user on init, skipping initial queries");
    }
  }

  /* Interface Functions */

  // Takes in the new user id to set, ignores if it is the same user
  // If a new user is set, removes all current data and restarts the queries
  public void setUser(String newUser) {
    if (newUser != null && newUser.equals(mFirebaseUserID) || newUser == mFirebaseUserID) {
      Log.w(TAG, "No change to user, ignoring update");
      return;
    }

    Log.w(TAG, "Change in user, clearing local data and stopping listeners");
    clearData();

    if (newUser == null) {
      Log.w(TAG, "Current user is null, avoiding new queries");

    } else {
      Log.w(TAG, "New user set, starting queries");

      // Set user and restart queries
      mFirebaseUserID = newUser;
      queryHouseholdIdByUser();
    }
  }

  // Takes in a household and sets it to the current user
  // Returns true if the ID is valid (but not necessarily existing)
  public boolean setHousehold(HouseholdModel household) {
    // Make sure household ID is new
    if (mHousehold == null || !mHousehold.equals(household)) {
      queryHouseholdIdByNum(household.getHouseholdNum());
      // TODO: Set user to household
      return true;
    }
    return false;
  }

  // Returns a (potentially empty) list of users if household exists
  // Returns null if household doesn't exist
  public List<UserModel> getUsers() {
    if (mHousehold != null) {
      return mUsers; // Create copy?
    }
    return null;
  }

  // Returns a (potentially empty) list of tasks if household exists
  // Returns null if household doesn't exist
  public List<TaskModel> getTasks(HouseholdModel household) {
    if (mHousehold != null) {
      return mTasks; // Create copy?
    }
    return null;
  }

  // Returns true if user is successfully added to the household
  // Returns false otherwise
  public boolean addUserToHouse(UserModel user) {
    if (mUserCollection != null) {
      // Store the user object
      mUserCollection.document(user.getFirebaseId()).set(user);
      return true;
    }
    return false;
  }

  // Returns true if user is successfully removed from the household
  // Returns false otherwise
  public boolean removeUserFromHouse(UserModel user) {
    // TODO
    return false;
  }

  // Returns true if tasks is successfully added to the household
  // Returns false otherwise
  public boolean addTaskToHouse(TaskModel task) {
    if (mUserCollection != null) {
      // Store the task object
      mUserCollection.document(task.getName()).set(task);
      return true;
    }
    return false;
  }

  // Returns true if tasks is successfully removed from the household
  // Returns false otherwise
  public boolean removeTaskFromHouse(TaskModel task) {
    // TODO
    return false;
  }

  // Returns true if user is deleted
  // Returns false otherwise
  public boolean deleteUser() {
    // TODO
    return false;
  }

  // Returns true if household is deleted
  // Returns false otherwise
  public boolean deleteHousehold() {
    // TODO
    return false;
  }

  /* Query functions */

  // Finds the householdID corresponding to the current user
  // Starts query for household document if ID is found
  private void queryHouseholdIdByNum(final long householdNum) {
    Log.w(TAG, "Getting household with num: " + householdNum);

    // Single-time query, no reason to get listener
    // Searches households for particular num
    mFirestore
        .collection(HOUSEHOLD_COLLECTION_NAME)
        .whereEqualTo(HOUSEHOLD_ID_FIELD, householdNum)
        .get()
        .addOnSuccessListener(
            new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(QuerySnapshot snapshot) {
                if (snapshot != null) {
                  // TODO: are both needed?
                  if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                    Log.d(TAG, "No household found with num");
                    clearData();
                  } else {
                    Log.d(TAG, "Found " + snapshot.getDocuments().size() + " households with num");

                    String householdID = snapshot.getDocuments().get(0).getReference().getId();
                    if (snapshot.getDocuments().size() > 1) {
                      Log.w(TAG, "Multiple households found, logging them");
                      for (DocumentSnapshot d : snapshot.getDocuments()) {
                        Log.d(TAG, "Household: " + d.getId() + ", data: " + d.getData());
                      }
                    }

                    // A household ID has been found, so get the corresponding household data
                    queryHousehold(householdID);
                  }
                } else {
                  Log.d(TAG, "Query result for households is null");
                  clearData();
                }
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to find household by num", e);
              }
            });
  }

  // Finds the householdID corresponding to the current user
  // Starts query for household document if ID is found
  private void queryHouseholdIdByUser() {
    Log.w(TAG, "Getting household with user ID: " + mFirebaseUserID);

    // Single-time query, no reason to get listener
    // Searches all Users collections for particular id
    mFirestore
        .collectionGroup(USERS_COLLECTION_NAME)
        .whereEqualTo(USER_ID_FIELD, mFirebaseUserID)
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
                      Log.w(TAG, "Multiple households found, logging them");
                      for (DocumentSnapshot d : snapshot.getDocuments()) {
                        Log.d(TAG, "Household: " + d.getId() + ", data: " + d.getData());
                      }
                    }

                    // A household ID has been found, so get the corresponding household data
                    queryHousehold(householdID);
                  }
                } else {
                  Log.d(TAG, "Query result for households is null");
                  clearData();
                }
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

                    // Set collection references
                    mTaskCollection =
                        mFirestore
                            .collection(HOUSEHOLD_COLLECTION_NAME)
                            .document(snapshot.getId())
                            .collection(TASK_COLLECTION_NAME);
                    mUserCollection =
                        mFirestore
                            .collection(HOUSEHOLD_COLLECTION_NAME)
                            .document(snapshot.getId())
                            .collection(TASK_COLLECTION_NAME);

                    mHousehold = buildHousehold(snapshot);

                    // Household has been found and set, so get the Users and Tasks
                    queryTasks();
                    queryUsers();

                  } else {
                    Log.d(TAG, "No household found");
                    clearData();
                  }
                } else {
                  Log.d(TAG, "Current data: null");
                  clearData();
                }
              }
            });
  }

  // Rebuilds the list of tasks to the current household
  private void queryTasks() {
    if (mHousehold == null) {
      Log.w(TAG, "Household is null, no tasks can be queried");
      return;
    }

    if (mTaskCollection == null) {
      Log.w(TAG, "No task collection available to query");
      return;
    }

    // Deactivate the previous listener if it exists
    if (mTasksListener != null) {
      mTasksListener.remove();
      mTasksListener = null;
    }

    mTasksListener =
        mTaskCollection.addSnapshotListener(
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
                  } else {
                    Log.d(TAG, "Found " + snapshot.getDocuments().size() + " tasks");

                    // Tasks have been found, so create a new list
                    mTasks.clear();
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                      // TODO: add a lock?
                      mTasks.add(buildTask(d));
                    }
                  }
                } else {
                  Log.d(TAG, "Query result for tasks is null");
                }
              }
            });
  }

  // Rebuilds the list of tasks to the current household
  private void queryUsers() {
    if (mHousehold == null) {
      Log.w(TAG, "Household is null, no users can be queried");
      return;
    }

    if (mUserCollection == null) {
      Log.w(TAG, "No user collection available to query");
      return;
    }

    // Deactivate the previous listener if it exists
    if (mUsersListener != null) {
      mUsersListener.remove();
      mUsersListener = null;
    }

    mUsersListener =
        mUserCollection.addSnapshotListener(
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
                  } else {
                    Log.d(TAG, "Found " + snapshot.getDocuments().size() + " users");

                    // Users have been found, so create a new list
                    mUsers.clear();
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                      // TODO: add a lock?
                      mUsers.add(buildUser(d));
                    }
                  }
                } else {
                  Log.d(TAG, "Query result for users is null");
                }
              }
            });
  }

  /* Helper functions */

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

  // Removes all local data and references for the current household
  private void clearData() {
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

    // Remove collection references
    mTaskCollection = null;
    mUserCollection = null;

    // Clear local data
    mHousehold = null;
    mUsers.clear();
    mTasks.clear();
  }
}
