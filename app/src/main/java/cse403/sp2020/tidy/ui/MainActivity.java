package cse403.sp2020.tidy.ui;

import android.os.Bundle;

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

  private SectionsPagerAdapter sectionsPagerAdapter;
  private ModelInterface model;

  private ViewPager viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    String userId = "test";
    if(savedInstanceState != null)
      userId = savedInstanceState.getString("tidy_user_id", "test");//TODO Update when login works
    model = new ModelInterface(FirebaseFirestore.getInstance());
    model.setUser(userId);

    sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
    viewPager = findViewById(R.id.view_pager);
    setupViewPager(viewPager, userId);

    TabLayout tabLayout = findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);

  }

  public ModelInterface getModelInterface(){
    return model;
  }

  private void setupViewPager(ViewPager viewPager, String userId) {
    SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
    Bundle bundle = new Bundle();
    bundle.putString("tidy_user_id", userId);

    AllChoresFragment allFrag = new AllChoresFragment();
    allFrag.setArguments(bundle);
    MyChoresFragment myFrag = new MyChoresFragment();
    myFrag.setArguments(bundle);

    adapter.addFragment(allFrag, "All Chores");
    adapter.addFragment(myFrag, "My Chores");
    viewPager.setAdapter(adapter);
  }
}
