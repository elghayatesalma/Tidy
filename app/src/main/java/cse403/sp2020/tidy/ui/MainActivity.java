package cse403.sp2020.tidy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.ui.main.AllChoresFragment;
import cse403.sp2020.tidy.ui.main.MyChoresFragment;
import cse403.sp2020.tidy.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private ModelInterface model;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Intent creationIntent = getIntent();
    final String userId = creationIntent.getStringExtra("tidy_user_id");
    Log.d("test", "main userid = " + userId);
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    model = new ModelInterface(mFirestore);
    model.setCurrentUser(userId, user -> {
      if (user == null) {
        Log.e(TAG, "Failed to set user in main activity");
      } else {
        Log.d(TAG, "User set");
        // Initiate fragments and tabs
        ViewPager viewPager = findViewById(R.id.main_view_pager);
        setupViewPager(viewPager, userId);
        TabLayout tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Enable navigation button to ProfileActivity
        findViewById(R.id.main_to_profile_button)
          .setOnClickListener(
            view -> {
              Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
              intent.putExtra("tidy_user_id", userId);
              startActivity(intent);
            });
      }
    });
  }

  // Enables fragments to share ModelInterface
  public ModelInterface getModelInterface() {
    return model;
  }

  private void setupViewPager(ViewPager viewPager, String userId) {
    // Set arguments for the fragments
    Bundle bundle = new Bundle();
    bundle.putString("tidy_user_id", userId);
    AllChoresFragment allFrag = new AllChoresFragment();
    allFrag.setArguments(bundle);
    MyChoresFragment myFrag = new MyChoresFragment();
    myFrag.setArguments(bundle);

    SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
    adapter.addFragment(allFrag, "All Chores");
    adapter.addFragment(myFrag, "My Chores");
    viewPager.setAdapter(adapter);
  }
}
