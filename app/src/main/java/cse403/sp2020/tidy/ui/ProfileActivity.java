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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.firebase.firestore.*;
import com.google.firebase.firestore.auth.User;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
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
    // userID = data.getStringExtra("tidy_user_id");
    userID = "test"; // test string

    // initialize firestore instance
      modelInterface = new ModelInterface(FirebaseFirestore.getInstance());
      setModelCallbacks();
      modelInterface.setUser(userID);

      user = modelInterface.getCurrentUser();
      household = modelInterface.getHousehold();

    // Button for going back to main activity
    ImageButton backToMain = (ImageButton) findViewById(R.id.profile_back);
    backToMain.setOnClickListener(new View.OnClickListener() {

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
    TextView nameView = (TextView) findViewById(R.id.profile_username);
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

                Collections.swap(choreList, draggedPosition, targetPosition); // probably work with backend on chore preference algo
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

  }
}
