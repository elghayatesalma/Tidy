package cse403.sp2020.tidy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.login.LoginActivity;
import cse403.sp2020.tidy.ui.main.AllChoresFragment;
import cse403.sp2020.tidy.ui.main.ChoresFragment;
import cse403.sp2020.tidy.ui.main.MyChoresFragment;
import cse403.sp2020.tidy.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private ModelInterface model;
  private ChoresFragment allFrag, myFrag;
  private boolean initialized = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final String userId = FirebaseAuth.getInstance().getUid();
    Log.d(TAG, "main userid = " + userId);
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    model = new ModelInterface(mFirestore);
    model.setCurrentUser(
        userId,
        user -> {
          if (user == null) {
            Log.e(TAG, "Failed to set user in main activity");
          } else {
            Log.d(TAG, "User set");
            model.setTasksListener(
                tasks -> {
                  if (tasks == null) {
                    Log.e(TAG, "Tasks returned null in listener callback");
                  } else {
                    if (!initialized) {
                      initialized = true;
                      // Initiate fragments and tabs
                      ViewPager viewPager = findViewById(R.id.main_view_pager);
                      setupViewPager(viewPager, userId);
                      TabLayout tabLayout = findViewById(R.id.main_tabs);
                      tabLayout.setupWithViewPager(viewPager);

                      // Enable navigation button to ProfileActivity
                      findViewById(R.id.main_to_profile_button)
                          .setOnClickListener(
                              view -> {
                                Intent intent =
                                    new Intent(getApplicationContext(), ProfileActivity.class);
                                intent.putExtra("tidy_user_id", userId);
                                startActivity(intent);
                              });
                    }
                    model.setUsersListener(
                        users -> {
                          if (users == null) {
                            Log.e(TAG, "No users found");
                          } else {
                            handleUsersUpdates(users);
                          }
                        });
                    Log.d(TAG, "Tasks updated");

                    handleTaskUpdates(tasks);
                  }
                });
          }
        });
  }

  // Enables fragments to share ModelInterface
  public ModelInterface getModelInterface() {
    return model;
  }

  /** Always called whenever the fragment is no longer being used */
  @Override
  public void onPause() {
    super.onPause();
    if (initialized) {
      model.removeListeners();
    }
  }

  @Override
  public void onBackPressed() {
    // code here to show dialog
    Intent loginActivityIntent = new Intent(this, LoginActivity.class);
    startActivity(loginActivityIntent);
  }

  /** Always called whenever the fragment starts being used */
  @Override
  public void onResume() {
    super.onResume();

    if (initialized) {
      // Add listener on tasks
      model.setTasksListener(
          tasks -> {
            if (tasks == null) {
              Log.e(TAG, "Tasks returned null in listener callback");
            } else {
              Log.d(TAG, "Tasks updated");
              handleTaskUpdates(tasks);
            }
          });

      model.setUsersListener(
          users -> {
            if (users == null) {
              Log.e(TAG, "No users found");
            } else {
              handleUsersUpdates(users);
            }
          });
    }
  }

  private void handleTaskUpdates(List<TaskModel> tasks) {
    allFrag.updateChoreList(tasks);
    myFrag.updateChoreList(tasks);
  }

  private void handleUsersUpdates(List<UserModel> users) {
    allFrag.updateUserList(users);
    myFrag.updateUserList(users);
  }

  private void setupViewPager(ViewPager viewPager, String userId) {
    // Set arguments for the fragments
    Bundle bundle = new Bundle();
    bundle.putString("tidy_user_id", userId);
    allFrag = new AllChoresFragment();
    allFrag.setArguments(bundle);
    myFrag = new MyChoresFragment();
    myFrag.setArguments(bundle);

    SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
    adapter.addFragment(allFrag, "all chores");
    adapter.addFragment(myFrag, "my chores");
    viewPager.setBackgroundResource(R.drawable.background);
    viewPager.setAdapter(adapter);
  }
}
