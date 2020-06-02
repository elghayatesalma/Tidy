package cse403.sp2020.tidy.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Objects;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;
import cse403.sp2020.tidy.ui.MainActivity;

/** Generic chores fragment class, handles drawing and */
public abstract class ChoresFragment extends Fragment {
  protected String TAG = "CHORE_FRAGMENT";
  protected ModelInterface model;
  protected ChoreListArrayAdapter<TaskModel> choreList;
  protected List<UserModel> userList;

  /**
   * On fragment creation gets the firebase model interface and arguments from MainActivity
   *
   * @param savedInstanceState saved bundle that is passed by the system
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    model = ((MainActivity) Objects.requireNonNull(getActivity())).getModelInterface();
    Bundle b = getArguments(); // TODO: Needed?
    assert b != null;
  }

  /**
   * Inflates the AllChores UI, initializes onclick actions and sets the listview's array adapter.
   *
   * @param inflater creates the view from a layout resource
   * @param container viewgroup in which to place the new view
   * @param savedInstanceState saved bundle that is passed by the system
   * @return Inflated allchores fragment with all interactions initialized
   */
  @Nullable
  @Override
  public abstract View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState);

  /** Creates specific fragment views */
  protected void addOnClick(View viewById) {
    viewById.setOnClickListener(
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
                      // Build a new task and add it to the DB
                      TaskModel newTask = new TaskModel();
                      newTask.setName(name);
                      newTask.setDescription(description);
                      newTask.setPriority(priority);
                      addTask(newTask);

                      dialog.dismiss();
                    } else {
                      Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
        });
  }

  /** Handles all updates related to a new/updated list of tasks */
  public abstract void updateChoreList(List<TaskModel> tasks);

  /** Handles updates to new users */
  public abstract void updateUserList(List<UserModel> users);

  /** Handles updates to user id*/
  public abstract void updateUserID(String uid);

  /** Handles creating setting a new task */
  public abstract void addTask(TaskModel newTask);
}
