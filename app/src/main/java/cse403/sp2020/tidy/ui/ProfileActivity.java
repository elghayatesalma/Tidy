package cse403.sp2020.tidy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.firebase.firestore.*;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.profile.RecyclerAdapter;

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
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    Intent data = getIntent();
    final TextView nameView = (TextView) findViewById(R.id.profile_username);
    userID = data.getStringExtra("tidy_user_id");

    // initialize firestore instance
    modelInterface = new ModelInterface(FirebaseFirestore.getInstance());
    setModelCallbacks();
    UserCallbackInterface c =
        new UserCallbackInterface() {
          @Override
          public void userCallback(List<UserModel> users) {
            user = modelInterface.getCurrentUser();
            username = user.getFirstName() + " " + user.getLastName();
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

    // Button for going back to main activity
    ImageButton backToMain = (ImageButton) findViewById(R.id.profile_back);
    backToMain.setOnClickListener(
        new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            v.getContext().startActivity(intent);
          }
        });

    // get username if user != null, otherwise fill in name with default
    if (user != null) {
      username = user.getFirstName() + " " + user.getLastName();
    } else {
      username = "No Name";
    }

    // set textView to username
    nameView.setText(username);

    // this section makes the profile picture circular
    ImageView profilePic = (ImageView) findViewById(R.id.profile_picture);
    // might be able to replace R.drawable.example_user with user's selected image later on in the
    // project
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.example_user);
    RoundedBitmapDrawable roundedBitmapDrawable =
        RoundedBitmapDrawableFactory.create(getResources(), bitmap);
    roundedBitmapDrawable.setCircular(true);
    profilePic.setImageDrawable(roundedBitmapDrawable);

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

  /*
  private List<String> compareList(List<String> original, List<String> updated) {
      List<String> returned = new ArrayList<>(original);
      for (int i = 0; i < updated.size(); i++) { // see new additions
          if (!original.contains(updated.get(i))) {
              returned.add(updated.get(i));
          }
      }

      for (int i = 0; i < original.size(); i++) { // see removals
          if (!updated.contains(original.get(i))) {
              returned.remove(original.get(i));
          }
      }
      return returned;
  }

   */
}
