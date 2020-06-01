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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import com.bumptech.glide.*;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.profile.RecyclerAdapter;

/** Activity that allows users to manage their account and chore preferences */
public class ProfileActivity extends AppCompatActivity {

  private List<String> choreList = new ArrayList<>();
  private RecyclerView recyclerView;
  private RecyclerAdapter recyclerAdapter;
  private LinearLayoutManager mLayoutManager;
  private ModelInterface modelInterface;
  private UserModel user;
  private HouseholdModel household;
  private String username;
  private String userID;

  // TODO: receive chores list from the model and add to display
  /**
   * On activity creation gets the firebase model interface, the current user, and initializes the
   * UI including current profile image and preference list
   *
   * @param savedInstanceState saved bundle that is passed by the system
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    Intent data = getIntent();
    final TextView nameView = (TextView) findViewById(R.id.profile_username);
    userID = data.getStringExtra("tidy_user_id");

    // initialize firestore instance
    modelInterface = new ModelInterface(FirebaseFirestore.getInstance());

    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
    username = acct.getDisplayName();
    String photoURL = acct.getPhotoUrl().toString();

    RequestOptions requestOptions = new RequestOptions();
    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

    Glide.with(getApplicationContext())
        .load(photoURL)
        .thumbnail(0.5f)
        .transition(withCrossFade())
        .apply(requestOptions)
        .into((ImageView) findViewById(R.id.profile_picture));

    setModelCallbacks();
    UserCallbackInterface c =
        new UserCallbackInterface() {
          @Override
          public void userCallback(List<UserModel> users) {
            user = modelInterface.getCurrentUser();
            nameView.setText(username);
          }

          @Override
          public void userCallbackFailed(String message) {
            username = "No Name";
            nameView.setText(username);
          }
        };
    Log.w("PROFILE", userID);
    modelInterface.setUser(userID);

    modelInterface.registerUserCallback(c);
    user = modelInterface.getCurrentUser();
    household = modelInterface.getHousehold();

    Button share = (Button) findViewById(R.id.share_household_button);
    share.setOnClickListener(
        v -> {
          String dynamicLink = modelInterface.getSharingLink().toString();
          Intent sendIntent = new Intent();
          sendIntent.setAction(Intent.ACTION_SEND);
          //  sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Add users to household");
          sendIntent.putExtra(Intent.EXTRA_TEXT, dynamicLink);
          sendIntent.setType("text/plain");
          Intent shareIntent = Intent.createChooser(sendIntent, "Add users to household");
          startActivity(shareIntent);
        });

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
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
              @Override
              public boolean onMove(
                  @NonNull RecyclerView recyclerView,
                  @NonNull RecyclerView.ViewHolder dragged,
                  @NonNull RecyclerView.ViewHolder target) {
                int draggedPosition = dragged.getAdapterPosition();

                int targetPosition = target.getAdapterPosition();

                Collections.swap(
                    choreList,
                    draggedPosition,
                    targetPosition); // probably work with backend on chore preference algo
                // needs to update preference list
                recyclerAdapter.notifyItemMoved(draggedPosition, targetPosition);

                return false;
              }

              @Override
              public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
            });

    helper.attachToRecyclerView(recyclerView);
  }

  /** Registers all the callback methods for the model. */
  private void setModelCallbacks() {
    modelInterface.registerTaskCallback(
        new TaskCallbackInterface() {
          @Override
          public void taskCallback(List<TaskModel> users) {
            Log.d("test", "Profile task callback success tasks == null = " + (users == null));
            for (int i = 0; i < users.size(); i++) {
              choreList.add(users.get(i).getName());
              recyclerAdapter.notifyDataSetChanged();
            }
          }

          @Override
          public void taskCallbackFail(String message) {
            Log.d("test", "task callback fail message = " + message);
          }
        });

    modelInterface.registerHouseholdCallback(
        new HouseholdCallbackInterface() {
          @Override
          public void householdCallback(HouseholdModel household) {
            Log.d(
                "test",
                "Profile house callback success household == null = " + (household == null));
          }

          @Override
          public void householdCallbackFailed(String message) {
            Log.d("test", "house callback fail message = " + message);
          }
        });
  }
}
