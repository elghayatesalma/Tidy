package cse403.sp2020.tidy.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.FirebaseFirestore;

import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.UserModel;

public class ModelInterfaceTest {
  private FirebaseFirestore mFirestore;

  @Before
  public void setUp() throws Exception {
    // 10.0.2.2 is the special IP address to connect to the 'localhost' of
    // the host computer from an Android emulator.
    FirebaseFirestoreSettings settings =
        new FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build();

    mFirestore = FirebaseFirestore.getInstance();
    mFirestore.setFirestoreSettings(settings);
  }

  @Test
  // Runs through a basic setup with creating a household and adding a user
  public void setupTest() throws InterruptedException {
    String userId = "test_user_id";
    UserModel user = new UserModel(userId, "Test", "Name");
    ModelInterface mi = new ModelInterface(mFirestore, user);
    Thread.sleep(100);

    // Nothing is in the database yet, should be totally empty
    assertNull(mi.getHousehold());
    assertNull(mi.getTasks());
    assertNull(mi.getUsers());
    HouseholdModel household = new HouseholdModel(1);
    mi.makeHousehold(household);
    Thread.sleep(50);
    mi.setHousehold(household);
    Thread.sleep(50);

    // Added a household, show be set
    assertNotNull(mi.getHousehold());
    assertFalse(mi.getUsers().isEmpty());

    // Always clean up and remove listeners
    mi.cleanUp();
  }

  @After
  public void tearDown() throws Exception {
    mFirestore.terminate();
  }
}
