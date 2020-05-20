package cse403.sp2020.tidy.ui.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.MainActivity;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = "LoginActivity";
  private static final int RC_SIGN_IN = 9001;

  private GoogleSignInClient mGoogleSignInClient;
  private TextView mStatusTextView;
  private TextView mHouseholdShareTextView;
  private TextView mFirebaseStatusTextView;
  private ImageView mProfileImageView;
  private FirebaseAuth mAuth;
  private String mSharedHouseHold;

  /**
   * Called at the start of the activity's lifecycle. Loads the ui layout, initializes the buttons,
   * and configures the google sign-in parameters.
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Views
    mHouseholdShareTextView = findViewById(R.id.sharedHouseholdID);
    mStatusTextView = findViewById(R.id.status);
    mFirebaseStatusTextView = findViewById(R.id.firebaseStatus);
    mProfileImageView = findViewById(R.id.userProfileImage);

    // Button listeners
    findViewById(R.id.sign_in_button).setOnClickListener(this);
    findViewById(R.id.sign_out_button).setOnClickListener(this);
    findViewById(R.id.disconnect_button).setOnClickListener(this);
    findViewById(R.id.go_to_main_button).setOnClickListener(this);

    // [START configure_signin]
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            // besides requestIdToken, the rest of these requests are probably not needed.
            .requestId()
            .requestProfile()
            .requestEmail()
            .build();
    // [END configure_signin]

    // [START build_client]
    // Build a GoogleSignInClient with the options specified by gso.
    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    // [END build_client]

    // Initialize Firebase Auth
    mAuth = FirebaseAuth.getInstance();

    // [START customize_button]
    // Set the dimensions of the sign-in button.
    SignInButton signInButton = findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_STANDARD);
    signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
    // [END customize_button]
  }

  /**
   * Called on the start portion of the activity lifecycle and refreshes the last account to be
   * signed in.
   */
  @Override
  public void onStart() {
    super.onStart();

    // [START on_start_sign_in]
    // Check for existing Google Sign In account, if the user is already signed in
    // the GoogleSignInAccount will be non-null.
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    FirebaseUser currentUser = mAuth.getCurrentUser();
    // Only show google sign in information if firebase logged in as well
    if (currentUser != null) {
      updateGoogleSignInUI(account);
    } else {
      updateGoogleSignInUI(null);
    }
    updateFireBaseSignInUI(currentUser);

    FirebaseDynamicLinks.getInstance()
        .getDynamicLink(getIntent())
        .addOnSuccessListener(
            this,
            new OnSuccessListener<PendingDynamicLinkData>() {
              @Override
              public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                // Get deep link from result (may be null if no link is found)
                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                  deepLink = pendingDynamicLinkData.getLink();
                }

                if (deepLink != null) {
                  Log.d("DYNAMIC_LINK", "path: " + deepLink.toString());
                  mSharedHouseHold = deepLink.getLastPathSegment();
                  mHouseholdShareTextView.setText(
                      "Firebase Dynamic Link Household ID: " + mSharedHouseHold);
                }
              }
            })
        .addOnFailureListener(
            this,
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "getDynamicLink:onFailure", e);
              }
            });
    // [END on_start_sign_in]
  }

  /**
   * Handles the return from started activities. When requestCode = RC_SING_IN retrieves the signed
   * google account.
   * @param requestCode the code that started the new activity
   * @param resultCode a code returned from the activity when it finished
   * @param data the data returned from the activity
   */
  // [START onActivityResult]
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        // Google Sign In was successful then authenticate with Firebase
        GoogleSignInAccount account = task.getResult(ApiException.class);
        if (account != null) {
          Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
          // Use the id token from google sign-in
          firebaseAuthWithGoogle(account.getIdToken());
        } else {
          Log.w(TAG, "Google Sign-in Account is null");
          throw new ApiException(Status.RESULT_INTERNAL_ERROR);
        }
      } catch (ApiException e) {
        // Google Sign In failed, update UI appropriately
        Log.w(TAG, "Sign in failed", e);
      }
      handleSignInResult(task);
    }
  }
  // [END onActivityResult]

  /**
   * Collects the signed in google account or logs an error. Then updates the UI.
   * @param completedTask Finished sign in task
   */
  // [START handleSignInResult]
  private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
    try {
      GoogleSignInAccount account = completedTask.getResult(ApiException.class);

      // Signed in successfully, show authenticated UI.
      updateGoogleSignInUI(account);
    } catch (ApiException e) {
      // The ApiException status code indicates the detailed failure reason.
      // Please refer to the GoogleSignInStatusCodes class reference for more information.
      Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
      updateGoogleSignInUI(null);
    }
  }
  // [END handleSignInResult]

  /**
   * Sets google sign in intent and starts the activity for a result.
   */
  // [START signInGoogle]
  private void signInGoogle() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }
  // [END signInGoogle]

  /**
   * Initiates google sign out operation and registers a callback to update the UI when sign out
   * is complete.
   */
  // [START signOutGoogle]
  private void signOutGoogle() {
    mGoogleSignInClient
        .signOut()
        .addOnCompleteListener(
            this,
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                // [START_EXCLUDE]
                updateGoogleSignInUI(null);
                // [END_EXCLUDE]
              }
            });
  }
  // [END signOutGoogle]

  /**
   * Initiates google revoke access operation and registers a callback to update the UI when revoke
   * access is complete.
   */
  // [START revokeAccessGoogle]
  private void revokeAccessGoogle() {
    mGoogleSignInClient
        .revokeAccess()
        .addOnCompleteListener(
            this,
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                // [START_EXCLUDE]
                updateGoogleSignInUI(null);
                // [END_EXCLUDE]
              }
            });
  }
  // [END revokeAccessGoogle]

  /**
   * Changes the UI to display the profile image, hide the sign in button and show the sign
   * out button when account is not null.
   * @param account the google account that is signed in or null
   */
  private void updateGoogleSignInUI(@Nullable GoogleSignInAccount account) {
    if (account != null) {
      mProfileImageView.setImageURI(account.getPhotoUrl());
      mStatusTextView.setText(
          getString(
              R.string.signed_in_fmt,
              account.getDisplayName(),
              account.getEmail(),
              account.getGivenName(),
              account.getFamilyName()));

      findViewById(R.id.sign_in_button).setVisibility(View.GONE);
      findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
      mProfileImageView.setVisibility(View.VISIBLE);
    } else {
      mStatusTextView.setText(R.string.signed_out);
      mProfileImageView.setVisibility(View.GONE);

      findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
      findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
    }
  }

  /**
   * Changes the UI to display the account information when user is not null.
   * @param  user the firebase account to display
   */
  private void updateFireBaseSignInUI(@Nullable FirebaseUser user) {
    if (user != null) {
      String displayName = "no display name";
      String email = "no email";
      String photoURL = "no photo";
      String phone = "no phone number";
      if (user.getDisplayName() != null) {
        displayName = user.getDisplayName();
      }
      if (user.getPhotoUrl() != null) {
        photoURL = user.getPhotoUrl().toString();
      }
      if (user.getEmail() != null) {
        email = user.getEmail();
      }
      mFirebaseStatusTextView.setText(
          getString(R.string.firebase_status_fmt, displayName, email, photoURL, user.getUid()));
      findViewById(R.id.go_to_main_button).setVisibility(View.VISIBLE);
    } else {
      mFirebaseStatusTextView.setText(getString(R.string.firebase_disconnected));
      findViewById(R.id.go_to_main_button).setVisibility(View.GONE);
    }
  }

  /**
   * Creates a toast and displays it.
   * @param text the message to display
   */
  private void toast(CharSequence text) {
    int duration = Toast.LENGTH_LONG;
    Context context = getApplicationContext();
    Toast toast = Toast.makeText(context, text, duration);
    toast.show();
  }

  /**
   * Authenticates firebase using google credentials and sets a callback that sets the firebase user
   * when complete and successful or displays and logs an error message.
   * @param idToken the id for google authentication
   */
  private void firebaseAuthWithGoogle(String idToken) {
    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
    mAuth
        .signInWithCredential(credential)
        .addOnCompleteListener(
            this,
            new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                  // Sign in success, update UI with the signed-in user's information
                  Log.d(TAG, "signInWithCredential:success");
                  toast("Firebase signed in successfully");
                  FirebaseUser user = mAuth.getCurrentUser();
                  updateFireBaseSignInUI(user);
                } else {
                  // If sign in fails, display a message to the user.
                  Log.w(TAG, "signInWithCredential:failure", task.getException());
                  toast("Firebase failed to sign in!");
                  updateFireBaseSignInUI(null);
                }

                // ...
              }
            });
  }

  /**
   * Handles the click UI action for all the buttons.
   * @param v The view that is clicked
   */
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.go_to_main_button:
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.putExtra("tidy_user_id", mAuth.getUid());
        // TODO Remove this when setup activity is completed
        // Start temporary household registry
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        ModelInterface model = new ModelInterface(mFirestore);
        model.registerUserCallback(
            new UserCallbackInterface() {
              @Override
              public void userCallback(List<UserModel> users) {
                if (mSharedHouseHold != null) {
                  Log.d("DYNAMIC_LINK", "using shared dynamic link household: " + mSharedHouseHold);
                  model.makeHousehold(new HouseholdModel(mSharedHouseHold));
                } else {
                  Log.d("DYNAMIC_LINK", "not using shared dynamic link household");
                  model.makeHousehold(new HouseholdModel(mAuth.getUid() + "_house"));
                }
              }

              @Override
              public void userCallbackFailed(String message) {}
            });
        model.registerHouseholdCallback(
            new HouseholdCallbackInterface() {
              @Override
              public void householdCallback(HouseholdModel household) {
                startActivity(mainActivityIntent);
                model.cleanUp();
              }

              @Override
              public void householdCallbackFailed(String message) {}
            });
        model.setUser(mAuth.getUid());
        // End temporary household registry
        // startActivity(mainActivityIntent);
        break;
      case R.id.sign_in_button:
        signInGoogle();
        break;
      case R.id.sign_out_button:
        signOutGoogle();
        mAuth.signOut();
        updateFireBaseSignInUI(null);
        break;
      case R.id.disconnect_button:
        revokeAccessGoogle();
        updateFireBaseSignInUI(null);
        break;
    }
  }
}
