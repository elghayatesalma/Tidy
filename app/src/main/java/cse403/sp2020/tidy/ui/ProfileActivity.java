package cse403.sp2020.tidy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import com.bumptech.glide.*;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.login.LoginActivity;
import cse403.sp2020.tidy.ui.profile.ChoreEntry;
import cse403.sp2020.tidy.ui.profile.RecyclerAdapter;

public class ProfileActivity extends AppCompatActivity {

  public List<ChoreEntry> choreList;
  public List<String> choreListIDs;
  private RecyclerView recyclerView;
  private RecyclerAdapter recyclerAdapter;
  private LinearLayoutManager mLayoutManager;
  private ModelInterface modelInterface;
  private UserModel user;
  private HouseholdModel household;
  private String username;
  private String houseName;
  private String mAuth;
  private List<TaskModel> taskList;

  private static final String TAG = "ProfileActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    Intent data = getIntent();
    final TextView nameView = (TextView) findViewById(R.id.profile_username);
    final TextView houseNameView = (TextView) findViewById(R.id.profile_household_name);
    mAuth = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // initialize firestore instance
    modelInterface = new ModelInterface(FirebaseFirestore.getInstance());

    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
    String photoURL = acct.getPhotoUrl().toString();

    RequestOptions requestOptions = new RequestOptions();
    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

    // import google+ profile picture
    Glide.with(getApplicationContext())
        .load(photoURL)
        .thumbnail(0.5f)
        .transition(withCrossFade())
        .apply(requestOptions)
        .into((ImageView) findViewById(R.id.profile_picture));

    modelInterface.setCurrentUser(
        mAuth,
        setUser -> {
          if (setUser == null) {
            Toast.makeText(this, "An error has occurred", Toast.LENGTH_LONG).show();
          } else { // user has been found/created
            Log.d(TAG, "User set");
            user = setUser;
            username = user.getFirstName() + " " + user.getLastName();
            nameView.setText(username);
            if (modelInterface.getHousehold().getName() != null) {
              houseName = modelInterface.getHousehold().getName();
              houseNameView.setText(houseName);
            } else {
              houseName = "No house name found";
              houseNameView.setText(houseName);
            }

            houseNameView.setText(houseName);
            choreListIDs = setUser.getPreferences();
            modelInterface.setTasksListener(
                tasks -> {
                  if (tasks == null) {
                    taskList = new ArrayList<>();
                    Log.d(TAG, "Tasks is null");
                  } else {
                    taskList = new ArrayList<>(tasks);
                    Log.d(TAG, "Reached here");
                  }
                  if (choreListIDs
                      == null) { // don't set chore preferences in db until changed in recycler
                    // adapter
                    Log.d(TAG, "chore list is null");
                    choreListIDs = new ArrayList<>();
                    for (TaskModel t : taskList) {
                      choreListIDs.add(t.getTaskId());
                      Log.d(TAG, t.getName());
                    }
                  } else if (taskList.size()
                      != choreListIDs
                          .size()) { // if there is an update to task list, update chorelist
                    for (TaskModel t : taskList) {
                      if (!choreListIDs.contains(t.getTaskId())) {
                        choreListIDs.add(t.getTaskId());
                      }
                    }
                    // remove any deleted tasks and append any new tasks
                  }

                  // Build the combined chore preferences list
                  choreList = new ArrayList<>();
                  for (String taskID : choreListIDs) {
                    for (TaskModel t : taskList) {
                      if (t.getTaskId().equals(taskID)) {
                        choreList.add(new ChoreEntry(t.getTaskId(), t.getName()));
                      }
                    }
                  }

                  // Button for going back to main activity
                  ImageButton backToMain = (ImageButton) findViewById(R.id.profile_back);
                  backToMain.setOnClickListener(
                      new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                          finish();
                        }
                      });

                  // Button for going back to main activity
                  ImageButton profileSettings = (ImageButton) findViewById(R.id.settings_button);
                  profileSettings.setOnClickListener(
                      view -> {
                        // Model add task
                        UserModel u = modelInterface.getCurrentUser();
                        if (modelInterface != null && modelInterface.getHousehold() != null) {
                          houseName = modelInterface.getHousehold().getName();
                        }
                        final Dialog dialog =
                            new Dialog(Objects.requireNonNull(ProfileActivity.this));
                        dialog.setContentView(R.layout.edit_user_dialog);
                        ((EditText) dialog.findViewById(R.id.set_first_name))
                            .setText(u.getFirstName());
                        ((EditText) dialog.findViewById(R.id.set_last_name))
                            .setText(u.getLastName());
                        ((EditText) dialog.findViewById(R.id.set_house_name)).setText(houseName);
                        dialog.show();
                        dialog
                            .findViewById(R.id.leave_household)
                            .setOnClickListener(
                                v -> {
                                  new AlertDialog.Builder(this)
                                      .setTitle("Leave Household")
                                      .setMessage("Do you really want to leave the household?")
                                      .setIcon(android.R.drawable.ic_dialog_alert)
                                      .setPositiveButton(
                                          android.R.string.yes,
                                          (d, w) -> {
                                            modelInterface.removeUserFromHousehold(
                                                h -> {
                                                  if (h != null) {
                                                    Intent loginActivityIntent =
                                                        new Intent(
                                                            ProfileActivity.this,
                                                            LoginActivity.class);
                                                    startActivity(loginActivityIntent);
                                                  } else {
                                                    Toast.makeText(
                                                            ProfileActivity.this,
                                                            "Failed to delete user",
                                                            Toast.LENGTH_SHORT)
                                                        .show();
                                                  }
                                                });
                                          })
                                      .setNegativeButton(android.R.string.no, null)
                                      .show();
                                });
                        dialog
                            .findViewById(R.id.edit_user_dialog_cancel)
                            .setOnClickListener(view1 -> dialog.dismiss());
                        dialog
                            .findViewById(R.id.edit_user_dialog_submit)
                            .setOnClickListener(
                                view12 -> {
                                  String first_name =
                                      ((EditText) dialog.findViewById(R.id.set_first_name))
                                          .getText()
                                          .toString();
                                  String last_name =
                                      ((EditText) dialog.findViewById(R.id.set_last_name))
                                          .getText()
                                          .toString();
                                  String house_name =
                                      ((EditText) dialog.findViewById(R.id.set_house_name))
                                          .getText()
                                          .toString();
                                  boolean valid = !first_name.isEmpty();
                                  valid &= !last_name.isEmpty();
                                  valid &= !house_name.isEmpty();
                                  if (valid) {
                                    u.setFirstName(first_name);
                                    u.setLastName(last_name);
                                    modelInterface.updateCurrentUser(
                                        u,
                                        nu -> {
                                          if (nu == null) {
                                            Toast.makeText(
                                                    getBaseContext(),
                                                    "Failed to update user",
                                                    Toast.LENGTH_SHORT)
                                                .show();
                                          } else {
                                            nameView.setText(
                                                nu.getFirstName() + " " + nu.getLastName());
                                          }
                                        });
                                    HouseholdModel h = modelInterface.getHousehold();
                                    h.setName(house_name);
                                    modelInterface.updateHousehold(
                                        h,
                                        nh -> {
                                          if (nh == null) {
                                            Toast.makeText(
                                                    getBaseContext(),
                                                    "Failed to update household name",
                                                    Toast.LENGTH_SHORT)
                                                .show();
                                          } else {
                                            houseNameView.setText(house_name);
                                            dialog.dismiss();
                                          }
                                        });
                                  } else {
                                    Toast.makeText(
                                            getBaseContext(),
                                            "All fields must be filled",
                                            Toast.LENGTH_SHORT)
                                        .show();
                                  }
                                });
                      });

                  // Button for sharing household
                  ImageButton shareHouse = (ImageButton) findViewById(R.id.share_button);
                  shareHouse.setOnClickListener(
                      v -> {
                        String dynamicLink = modelInterface.getSharingLink().toString();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Share household with others");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, dynamicLink);
                        sendIntent.setType("text/plain");
                        Intent shareIntent =
                            Intent.createChooser(sendIntent, "Add users to household");
                        startActivity(shareIntent);
                      });

                  // Set up recycler view for drag and drop chore preference list
                  recyclerView = findViewById(R.id.chore_preference_list);
                  mLayoutManager = new LinearLayoutManager(this);
                  recyclerAdapter = new RecyclerAdapter(choreList);
                  recyclerView.setLayoutManager(mLayoutManager);
                  recyclerView.setAdapter(recyclerAdapter);

                  RecyclerView.ItemDecoration divider =
                      new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                  recyclerView.addItemDecoration(divider);

                  ItemTouchHelper helper =
                      new ItemTouchHelper(
                          new ItemTouchHelper.SimpleCallback(
                              ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                            @Override
                            public boolean onMove(
                                @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder dragged,
                                @NonNull RecyclerView.ViewHolder target) {
                              int draggedPosition = dragged.getAdapterPosition();

                              int targetPosition = target.getAdapterPosition();

                              Collections.swap(choreList, draggedPosition, targetPosition);

                              choreListIDs.clear();
                              for (ChoreEntry ce : choreList) {
                                choreListIDs.add(ce.taskId);
                              }
                              setUser.setPreferences(choreListIDs);

                              modelInterface.updateCurrentUser(
                                  setUser,
                                  updatedUser -> {
                                    // reset chore preference list if null
                                    if (updatedUser == null) {
                                      Toast.makeText(
                                              ProfileActivity.this,
                                              "Update failed.",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                    }
                                  });

                              recyclerAdapter.notifyItemMoved(draggedPosition, targetPosition);

                              return false;
                            }

                            @Override
                            public void onSwiped(
                                @NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
                          });

                  helper.attachToRecyclerView(recyclerView);
                });
          }
        });
  }
}
