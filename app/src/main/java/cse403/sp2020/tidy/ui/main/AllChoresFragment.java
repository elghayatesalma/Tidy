package cse403.sp2020.tidy.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.callbacks.HouseholdCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.callbacks.UserCallbackInterface;
import cse403.sp2020.tidy.data.model.HouseholdModel;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.MainActivity;

public class AllChoresFragment extends Fragment {
  private ModelInterface model;
  private String userId;
  private ArrayList<TaskModel> choreList;
  private ChoreListArrayAdapter<TaskModel> choreListAdapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    model = ((MainActivity) Objects.requireNonNull(getActivity())).getModelInterface();
    Bundle b = getArguments();
    assert b != null;
    userId = b.getString("tidy_user_id", "test");
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View frag = inflater.inflate(R.layout.allchores_fragment, container, false);
    ListView allChoreListView = frag.findViewById(R.id.all_chores_list);
    frag.findViewById(R.id.all_chores_add)
        .setOnClickListener(
            view -> {
              // Model add task
              final Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
              dialog.setContentView(R.layout.add_chore_dialog);
              dialog.show();
              dialog
                  .findViewById(R.id.add_chore_dialog_cancel)
                  .setOnClickListener(view1 -> dialog.dismiss());
              dialog
                  .findViewById(R.id.add_chore_dialog_submit)
                  .setOnClickListener(
                      view12 -> {
                        String name =
                            ((EditText) dialog.findViewById(R.id.add_chore_dialog_name))
                                .getText()
                                .toString();
                        String description =
                            ((EditText) dialog.findViewById(R.id.add_chore_dialog_description))
                                .getText()
                                .toString();
                        String priorityStr =
                            ((EditText) dialog.findViewById(R.id.add_chore_dialog_priority))
                                .getText()
                                .toString();
                        boolean valid = !name.isEmpty();
                        valid &= !description.isEmpty();
                        valid &= !priorityStr.isEmpty();
                        int priority = -1; // Dummy value that will never be used
                        try {
                          priority = Integer.parseInt(priorityStr, 10);
                        } catch (NumberFormatException ex) {
                          valid = false;
                        }
                        if (valid) {
                          model.addTaskToHousehold(new TaskModel(name, description, priority));
                          dialog.dismiss();
                        } else {
                          Toast.makeText(
                                  getContext(), "All fields must be filled", Toast.LENGTH_SHORT)
                              .show();
                        }
                      });
            });
    choreList = new ArrayList<>();
    choreListAdapter = new ChoreListArrayAdapter<>(getContext(), choreList);
    allChoreListView.setAdapter(choreListAdapter);
    setModelCallBacks();
    model.setUser(userId); // Initiates data collection callbacks to initialize tasks
    return frag;
  }

  @Override
  public void onPause() {
    super.onPause();
    model.cleanUp();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    model.cleanUp();
  }

  private void setModelCallBacks() {
    model.registerTaskCallback(
        new TaskCallbackInterface() {
          @Override
          public void taskCallback(List<TaskModel> users) {
            Log.d("test", "All Chore task callback success tasks == null = " + (users == null));
            choreList.clear();
            if (users != null) choreList.addAll(users);
            choreListAdapter.notifyDataSetChanged();
          }

          @Override
          public void taskCallbackFail(String message) {
            Log.d("test", "task callback fail message = " + message);
          }
        });

    model.registerHouseholdCallback(
        new HouseholdCallbackInterface() {
          @Override
          public void householdCallback(HouseholdModel household) {
            Log.d(
                "test",
                "All Chore house callback success household == null = " + (household == null));
          }

          @Override
          public void householdCallbackFailed(String message) {
            Log.d("test", "house callback fail message = " + message);
          }
        });

    model.registerUserCallback(
        new UserCallbackInterface() {
          @Override
          public void userCallback(List<UserModel> users) {
            Log.d("test", "All Chore user callback success users == null = " + (users == null));
          }

          @Override
          public void userCallbackFailed(String message) {
            Log.d("test", "user callback fail message = " + message);
          }
        });
  }
}
