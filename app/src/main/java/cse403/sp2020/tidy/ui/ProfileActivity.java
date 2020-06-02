package cse403.sp2020.tidy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import cse403.sp2020.tidy.ui.profile.RecyclerAdapter;

public class ProfileActivity extends AppCompatActivity {

  public List<String> choreList;
  private RecyclerView recyclerView;
  private RecyclerAdapter recyclerAdapter;
  private LinearLayoutManager mLayoutManager;
  private ModelInterface modelInterface;
  private UserModel user;
  private HouseholdModel household;
  private String username;
  private String mAuth;
  private List<TaskModel> taskList;

  private static final String TAG = "ProfileActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    Intent data = getIntent();
    final TextView nameView = (TextView) findViewById(R.id.profile_username);
    mAuth = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // initialize firestore instance
    modelInterface = new ModelInterface(FirebaseFirestore.getInstance());

    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
    username = acct.getDisplayName();
    nameView.setText(username);
    String photoURL = acct.getPhotoUrl().toString();

    RequestOptions requestOptions = new RequestOptions();
    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

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
            choreList = setUser.getChorePreferences();
            Log.d(TAG, "Chore list is null: " + (choreList == null));
            modelInterface.setTasksListener(
                tasks -> {
                  if (tasks == null) {
                    taskList = new ArrayList<>();
                    Log.d(TAG, "Tasks is null");
                  } else {
                    taskList = new ArrayList<>(tasks);
                    Log.d(TAG, "Reached here");
                  }
                  if (choreList
                      == null) { // don't set chore preferences in db until changed in recycler
                                 // adapter
                    Log.d(TAG, "chore list is null");
                    choreList = new ArrayList<>();
                    for (TaskModel t : taskList) {
                      choreList.add(t.getName());
                      Log.d(TAG, t.getName());
                    }
                  } else if (taskList.size()
                      != choreList.size()) { // if there is an update to task list, update chorelist
                    // remove any deleted tasks and append any new tasks
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

                              setUser.setChorePreferences(choreList);

                              Log.e(TAG, setUser.getChorePreferences().toString());

                              Log.d(TAG, "Reached update chore prefs");

                              modelInterface.updateCurrentUser(
                                  setUser,
                                  updatedUser -> {
                                    if (updatedUser
                                        == null) { // reset chore preference list if null
                                      Toast.makeText(
                                              ProfileActivity.this,
                                              "Update failed.",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                    } else {
                                      if (updatedUser.getChorePreferences() == null) {
                                        Log.e(TAG, "Updated user prefs are null");
                                      } else {
                                        Log.e(TAG, updatedUser.getChorePreferences().toString());
                                      }
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
