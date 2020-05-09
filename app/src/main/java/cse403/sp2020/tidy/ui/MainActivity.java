package cse403.sp2020.tidy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.ui.login.LoginActivity;
import cse403.sp2020.tidy.ui.main.AllChoresFragment;
import cse403.sp2020.tidy.ui.main.MyChoresFragment;
import cse403.sp2020.tidy.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final String HOUSEHOLDS = "Households";

  private SectionsPagerAdapter sectionsPagerAdapter;

  private ViewPager viewPager;
  private FirebaseUser currentUser;
  private FirebaseAuth mAuth;
  private FirebaseFirestore db;
  private Query houseHoldIDQuery;
  private String houseHoldID = null;
  private CollectionReference AllTasks;
  private CollectionReference AllUsers;
  private TableLayout allChoresTable;
  private AllChoresFragment mAllChoresFragment;

  // Listens to Firebase Auth state to detect if a user is signed out
  private FirebaseAuth.AuthStateListener authListener =
      new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
          currentUser = firebaseAuth.getCurrentUser();
          // If user is not signed in getCurrentUser method returns null
          if (currentUser != null) {
            initFirestore();
            getHouseHoldID(currentUser);
          } else {
            // User is signed out
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
          }
        }
      };

  private void getUsers() {
    Query UserQuery = db.collection(HOUSEHOLDS).document(houseHoldID).collection("Users");
    UserQuery.addSnapshotListener(
        new EventListener<QuerySnapshot>() {
          @Override
          public void onEvent(
              @Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
              Log.w(TAG, "Users Collection Listen failed.", e);
              return;
            }

            if (snapshot != null) {
              if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                Log.d(TAG, "Users Collection: empty");
              } else {
                Log.d(
                    TAG, "Users Collection has " + snapshot.getDocuments().size() + " documents ");
                for (DocumentSnapshot d : snapshot.getDocuments()) {
                  Log.d(TAG, "User Collection Document: " + d.getId() + ", data: " + d.getData());
                }
              }
            } else {
              Log.d(TAG, "Users Collection data: null");
              houseHoldID = null;
            }
          }
        });
  }

  private void getAllTasks() {
    AllTasks = db.collection(HOUSEHOLDS).document(houseHoldID).collection("Tasks");
    AllTasks.addSnapshotListener(
        new EventListener<QuerySnapshot>() {
          @Override
          public void onEvent(
              @Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
              Log.w(TAG, "Tasks Collection Listen failed.", e);
              return;
            }

            if (snapshot != null) {
              if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                Log.d(TAG, "Tasks Collection: empty");
              } else {
                Log.d(
                    TAG, "Tasks Collection has " + snapshot.getDocuments().size() + " documents ");
                for (DocumentSnapshot d : snapshot.getDocuments()) {
                  Log.d(
                      TAG,
                      "Found Task Collection Document: " + d.getId() + ", data: " + d.getData());
                  getTask(d.getId());
                }
              }
            } else {
              Log.d(TAG, "Tasks Collection data: null");
            }
          }
        });
  }

  private void getTask(final String documentID) {
    final DocumentReference task = AllTasks.document(documentID);
    task.addSnapshotListener(
        new EventListener<DocumentSnapshot>() {
          @Override
          public void onEvent(
              @Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
              Log.w(TAG, "List for task: " + documentID + " failed");
              return;
            }

            if (documentSnapshot != null) {
              Map<String, Object> taskData = documentSnapshot.getData();
              Log.w(TAG, "Retrieved task " + documentID + " with data " + taskData);
              if (!taskData.isEmpty()) {
                mAllChoresFragment.addTask(documentID, taskData);
              }
            } else {
              Log.w(TAG, "List for task: " + documentID + " returned null");
            }
          }
        });
  }

  private void getHouseHoldID(FirebaseUser user) {
    Log.w(TAG, "Firebase user id: " + user.getUid());
    houseHoldIDQuery = db.collection("Households").whereArrayContains("UserIDs", user.getUid());
    houseHoldIDQuery.addSnapshotListener(
        new EventListener<QuerySnapshot>() {
          @Override
          public void onEvent(
              @Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
              Log.w(TAG, "Listen failed.", e);
              return;
            }

            if (snapshot != null) {
              if (snapshot.isEmpty() && snapshot.getDocuments().isEmpty()) {
                Log.d(TAG, "Current data: empty");
                houseHoldID = null;
              } else {
                Log.d(TAG, "Current data has " + snapshot.getDocuments().size() + " documents ");
                houseHoldID = snapshot.getDocuments().get(0).getId();
                for (DocumentSnapshot d : snapshot.getDocuments()) {
                  Log.d(TAG, "DocumentID: " + d.getId() + ", data: " + d.getData());
                }
                getUsers();
                getAllTasks();
              }
            } else {
              Log.d(TAG, "Current data: null");
              houseHoldID = null;
            }
          }
        });
  }

  private void initFirestore() {
    db = FirebaseFirestore.getInstance();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(authListener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mAuth.removeAuthStateListener(authListener);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mAuth = FirebaseAuth.getInstance();
    sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    viewPager = (ViewPager) findViewById(R.id.view_pager);
    setupViewPager(viewPager);

    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);
  }

  private void setupViewPager(ViewPager viewPager) {
    SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
    mAllChoresFragment = new AllChoresFragment();
    adapter.addFragment(mAllChoresFragment, "All Chores");
    adapter.addFragment(new MyChoresFragment(), "My Chores");
    viewPager.setAdapter(adapter);
  }
}
