package cse403.sp2020.tidy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.ui.profile.RecyclerAdapter;

public class ProfileActivity extends AppCompatActivity {

    List<String> exampleChores = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private LinearLayoutManager mLayoutManager;



    // TODO: iterate through list of chores and add each of them to the display,
    // TODO:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // initialize example chores for testing
        exampleChores.add("wash dishes");
        exampleChores.add("take out garbage");
        exampleChores.add("clean bathroom");
        exampleChores.add("vacuum");

        // this section makes the profile picture circular
        ImageView profilePic = (ImageView) findViewById(R.id.profile_picture);
        // might be able to replace R.drawable.example_user with user's selected image later on in the project
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.example_user);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        profilePic.setImageDrawable(roundedBitmapDrawable);

        recyclerView = findViewById(R.id.chore_preference_list);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new RecyclerAdapter(exampleChores);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int draggedPosition = dragged.getAdapterPosition();

                int targetPosition = target.getAdapterPosition();

                Collections.swap(exampleChores, draggedPosition, targetPosition);

                recyclerAdapter.notifyItemMoved(draggedPosition, targetPosition);

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });

        helper.attachToRecyclerView(recyclerView);
    }


}
