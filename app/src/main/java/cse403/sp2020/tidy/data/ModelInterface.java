package cse403.sp2020.tidy.data;

import java.util.List;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.*;

import cse403.sp2020.tidy.data.model.Household;
import cse403.sp2020.tidy.data.model.Task;
import cse403.sp2020.tidy.data.model.User;

/*
 * Important Notes:
 *  - Firestore uses a local cache, so not every query goes to Firestore DB
 *  - ALL db changes should be done directly on the db (will affect local cache and remote)
 *  - Operation speed for any lookup/change will likely be very quick, especially if data is cached
 *  - Add event listeners for realtime updates, use get with success/fail/complete for one-time
 *
 * Open Questions:
 *  - Deactivate previous listeners if household object changes?
 *
 * TODO List:
 *  - Finish the remaining interface functions (mostly updaters)
 *  - Implement helper functions (needs a rework of Model objects)
 *  - Consider adding locks on object lists
 *  - Possibly add Auth listener?
 *  - Move Firestore instantiation out of constructor
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
  private static final String TASK_COLLECTION_NAME = "Tasks";
  private static final String USERS_COLLECTION_NAME = "Users";
  private static final String USER_ID_FIELD = "FirebaseID";

  // User
  private FirebaseUser mFirebaseUser;

  // Household objects
  private Household mHousehold;
  private List<Task> mTasks;
  private List<User> mUsers;

  // Firestore Listeners
  private ListenerRegistration mHouseholdListener;
  private ListenerRegistration mTasksListener;
  private ListenerRegistration mUsersListener;

  // Firestore Database instance
  private FirebaseFirestore db;

  // Constructor
  // Grabs the current Firestore database
  public ModelInterface() {
    // TODO
    // May need to move this out of the constructor for testing with a mock firestore db
    db = FirebaseFirestore.getInstance();

    // Initialize data
    mHousehold = null;
    mTasks = new ArrayList<Task>();
    mUsers = new ArrayList<User>();

    // Initialize Listeners
    mHouseholdListener = null;
    mTasksListener = null;
    mUsersListener = null;

    // Do initial queries if a user is present
    // TODO: Change this?
    mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    if (mFirebaseUser != null) {
      queryHouseholdID();
    } else {
      Log.w(TAG, "No user on init, skipping initial queries");
    }
  }

  /* interface Functions */

  // Tries to update the current user
  // Restarts the queries if a new user is set
  public void updateUser() {
    FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();

    // No current user
    if (newUser == null) {
      Log.w(TAG, "Current user is null, no user to set");
      mFirebaseUser = null;

      // Same user
    } else if (newUser.equals(mFirebaseUser)) {
      Log.w(TAG, "No change to user, ignoring update");
    } else {
      Log.w(TAG, "New user set, updating and restarting queries");
      queryHouseholdID();
    }
  }

  // Takes in a householdID and sets the corresponding household
  // Returns true if the ID is valid (but not necessarily existing)
  public boolean setHousehold(String householdID) {
    // Make sure household ID is new
    if (mHousehold == null || !mHousehold.getHouseId().equals(householdID)) {
      queryHousehold(householdID);
      return true;
    }
    return false;
  }

  // Returns a (potentially empty) list of users if household exists
  // Returns null if household doesn't exist
  public List<User> getUsers() {
    if (mHousehold != null) {
      return mUsers; // Create copy?
    }
    return null;
  }

  // Returns a (potentially empty) list of tasks of household exists
  // Returns null if household doesn't exist
  public List<Task> getTasks(Household household) {
    if (mHousehold != null) {
      return mTasks; // Create copy?
    }
    return null;
  }

  // Takes in user and household objects
  // Returns true if user is successfully added to the household
  // Returns false otherwise
  public boolean addUserToHouse(Household household, User user) {
    // TODO
    return false;
  }

  // Takes in user and household objects
  // Returns true if user is successfully removed from the household
  // Returns false otherwise
  public boolean removeUserFromHouse(Household household, User user) {
    // TODO
    return false;
  }

  // Takes in task and household objects
  // Returns true if tasks is successfully added to the household
  // Returns false otherwise
  public boolean addTaskToHouse(Household household, Task task) {
    // TODO
    return false;
  }

  // Takes in task and household objects
  // Returns true if tasks is successfully removed from the household
  // Returns false otherwise
  public boolean removeTaskFromHouse(Household household, Task task) {
    // TODO
    return false;
  }

  // Takes in no arguments (only current user can be removed)
  // Returns true if user is deleted
  // Returns false otherwise
  public boolean deleteUser() {
    // TODO
    return false;
  }

  // Takes in a household object
  // Returns true if household is deleted
  // Returns false otherwise
  public boolean deleteHousehold(Household household) {
    // TODO
    return false;
  }

  /* Query functions */

  // Finds the householdID corresponding to the current user
  // Starts query for household document if ID is found
  private void queryHouseholdID() {
    Log.w(TAG, "Getting household with user ID: " + mFirebaseUser.getUid());

    // Single-time query, no reason to get listener
    // Searches all Users collections for particular id
    db.collectionGroup(USERS_COLLECTION_NAME)
        .whereEqualTo(USER_ID_FIELD, mFirebaseUser.getUid())
        .get()
        .addOnSuccessListener(
            new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(QuerySnapshot snapshot) {
                if (snapshot != null) {
                  // TODO: are both needed?
                  if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                    Log.d(TAG, "User not found to be part of a household");
                    mHousehold = null;
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
                  mHousehold = null;
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

    DocumentReference householdDoc = db.collection(HOUSEHOLD_COLLECTION_NAME).document(householdID);

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

                    // Household has been found and set, so get the Users and Tasks
                    queryTasks();
                    queryUsers();

                  } else {
                    Log.d(TAG, "No household found");
                    mHousehold = null;
                  }
                } else {
                  Log.d(TAG, "Current data: null");
                  mHousehold = null;
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

    // Deactivate the previous listener if it exists
    if (mTasksListener != null) {
      mTasksListener.remove();
      mTasksListener = null;
    }

    CollectionReference taskCollection =
        db.collection(HOUSEHOLD_COLLECTION_NAME)
            .document(mHousehold.getHouseId())
            .collection(TASK_COLLECTION_NAME);

    mTasksListener =
        taskCollection.addSnapshotListener(
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
                    mHousehold = null;
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
                  mHousehold = null;
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

    // Deactivate the previous listener if it exists
    if (mUsersListener != null) {
      mUsersListener.remove();
      mUsersListener = null;
    }

    CollectionReference userCollection =
        db.collection(HOUSEHOLD_COLLECTION_NAME)
            .document(mHousehold.getHouseId())
            .collection(USERS_COLLECTION_NAME);

    mUsersListener =
        userCollection.addSnapshotListener(
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
                    mHousehold = null;
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
                  mHousehold = null;
                }
              }
            });
  }

  /* Helper functions */

  // Builds and returns a household object from the provided document snapshot
  private Household buildHousehold(DocumentSnapshot householdData) {
    return null;
  }

  // Builds and returns a task object from document
  private Task buildTask(DocumentSnapshot taskData) {
    return null;
  }

  // Builds and returns a user object from document
  private User buildUser(DocumentSnapshot userData) {
    return null;
  }
}
