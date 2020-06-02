package cse403.sp2020.tidy.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.ui.MainActivity;

public class UserSetup extends AppCompatActivity {
  private static final String TAG = "USER_SETUP";
  private Button mJoinHouseholdButton;
  private Button mCreateHouseholdButton;
  private TextView mSharingLinkEdit;
  private FirebaseFirestore mFirestore;
  private FirebaseAuth mAuth;

  @Override
  public void onBackPressed() {
    // code here to show dialog
    Intent loginActivityIntent = new Intent(this, LoginActivity.class);
    startActivity(loginActivityIntent);
  }

    @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFirestore = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    setContentView(R.layout.activity_user_setup);

    mJoinHouseholdButton = findViewById(R.id.joinHouseButton);
    mCreateHouseholdButton = findViewById(R.id.newHouseButton);
    mSharingLinkEdit = findViewById(R.id.sharingLink);

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
                  mSharingLinkEdit.setText(deepLink.toString());
                  Log.d("DYNAMIC_LINK", "path: " + deepLink.toString());
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

    mJoinHouseholdButton.setOnClickListener(
        v -> {
          String str_link = this.mSharingLinkEdit.getText().toString();
          // In case the entire link was manually pasted into the app
          if (str_link.startsWith("https://tidy403.page.link")) {
            String[] split = str_link.split("&link=");
            if (split.length > 1) {
              try {
                str_link = URLDecoder.decode(split[1], "UTF-8");
              } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "failed to decode firebase sharing link");
              }
            }
          }
          if (str_link.startsWith("https://tidy.household/")) {
            Uri deepLink = Uri.parse(str_link);
            String householdId = deepLink.getLastPathSegment();
            ModelInterface model = new ModelInterface(mFirestore);

            model.setCurrentUser(
                mAuth.getUid(),
                u -> {
                  if (u != null) {
                    // Update the user display name
                    String displayName = mAuth.getCurrentUser().getDisplayName();
                    if (displayName.contains(" ")) {
                      String name[] = displayName.split(" ");
                      u.setFirstName(name[0]);
                      u.setLastName(name[name.length - 1]);
                    } else {
                      u.setFirstName(displayName);
                    }
                    model.setCurrentHousehold(
                        householdId,
                        h -> {
                          // Household was created successfully
                          if (h != null) {
                            model.updateCurrentUser(
                                u,
                                nu -> {
                                  if (nu == null) {
                                    Log.d(TAG, "Failed to update user name");
                                  } else {
                                    Log.d(TAG, "Updated user name");
                                  }
                                });
                            Intent mainActivityIntent = new Intent(this, MainActivity.class);
                            startActivity(mainActivityIntent);
                          } else {
                            CharSequence text =
                                "Unable to join shared household - double check the link";
                            Toast toast =
                                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                            toast.show();
                          }
                        });
                  } else {
                    CharSequence text = "Unable to login to tidy - please try again";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                    toast.show();
                  }
                });
          } else {
            CharSequence text = "Please enter a valid Tidy sharing link";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.show();
          }
        });

    mCreateHouseholdButton.setOnClickListener(
        v -> {
          ModelInterface model = new ModelInterface(mFirestore);
          model.setCurrentUser(
              mAuth.getUid(),
              u -> {
                if (u != null) {
                  String displayName = mAuth.getCurrentUser().getDisplayName();
                  if (displayName != null && displayName.contains(" ")) {
                    String name[] = displayName.split(" ");
                    u.setFirstName(name[0]);
                    u.setLastName(name[name.length - 1]);
                  } else if (displayName != null) {
                    u.setFirstName(displayName);
                  } else {
                    u.setFirstName("No");
                    u.setLastName("Name");
                  }
                  model.createHousehold(
                      new HouseholdModel(),
                      h -> {
                        // Household was created successfully
                        if (h != null) {
                          model.updateCurrentUser(
                              u,
                              nu -> {
                                if (nu == null) {
                                  Log.d(TAG, "Failed to update user name");
                                } else {
                                  Log.d(TAG, "Updated user name");
                                }
                              });
                          Intent mainActivityIntent = new Intent(this, MainActivity.class);
                          startActivity(mainActivityIntent);
                        } else {
                          CharSequence text = "Unable to create new household - please try again";
                          Toast toast =
                              Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                          toast.show();
                        }
                      });
                } else {
                  CharSequence text = "Unable to login to tidy - please try again";
                  Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                  toast.show();
                }
              });
        });
  }
}
