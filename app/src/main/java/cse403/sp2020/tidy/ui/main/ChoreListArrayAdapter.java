package cse403.sp2020.tidy.ui.main;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Objects;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

public class ChoreListArrayAdapter<E> extends ArrayAdapter {

  private final LayoutInflater inflater;
  private ModelInterface model;
  private boolean all_toggleable;
  private List<UserModel> users;

  ChoreListArrayAdapter(
      Context context, List<E> objects, ModelInterface model, boolean toggleable) {
    super(context, 0, objects);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.model = model;
    this.all_toggleable = toggleable;
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    TaskModel chore = (TaskModel) getItem(position);
    ChoreHolder choreHolder;
    if (convertView == null || convertView.findViewById(R.id.chore_list_element_name) == null) {
      convertView = inflater.inflate(R.layout.chore_list_element, parent, false);
      choreHolder = new ChoreHolder();
      choreHolder.chore_name = convertView.findViewById(R.id.chore_list_element_name);
      choreHolder.chore_description = convertView.findViewById(R.id.chore_list_element_description);
      choreHolder.assigned_roommate = convertView.findViewById(R.id.chore_list_element_roommate);
      choreHolder.complete = convertView.findViewById(R.id.checkBox);
      convertView.setTag(choreHolder);
    } else {
      choreHolder = (ChoreHolder) convertView.getTag();
    }
    assert chore != null;
    choreHolder.chore_name.setText(chore.getName());
    choreHolder.chore_description.setText(chore.getDescription());
    String uid = chore.getAssignedTo();
    String assigned = "";
    boolean toggleable = false;
    if (model.getCurrentUser().getFirebaseId().equals(uid)) {
        assigned = "Mine";
        toggleable = true;
        choreHolder.assigned_roommate.setText(assigned);
        choreHolder.assigned_roommate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    } else if (users != null) {
      for (UserModel user : users) {
        if (user.getFirebaseId().equals(uid)) {
          assigned = user.getFirstName() + " " + user.getLastName();
          choreHolder.assigned_roommate.setText(assigned);
            choreHolder.assigned_roommate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        }
      }
    }

    choreHolder.complete.setChecked(chore.isCompleted());

    addEditView(convertView, chore);

    if (this.all_toggleable || toggleable) {
      View.OnClickListener li =
          l -> {
            // Toggle the checkbox if this is the choreholder view
            if (!(l instanceof CheckBox)) {
              choreHolder.complete.toggle();
            } // otherwise it was already toggled by clicking on it
            boolean completed = choreHolder.complete.isChecked();
            chore.setCompleted(completed);
            this.model.updateTask(
                chore,
                t -> {
                  if (t == null) {
                    Log.d("ChoreListAdapter", "failed to change completion status");
                    choreHolder.complete.toggle(); // failed to update
                  }
                });
          };

      convertView.setOnClickListener(li);
      choreHolder.complete.setEnabled(true);
      choreHolder.complete.setOnClickListener(li);
    } else {
      choreHolder.complete.setAlpha(0.4f);
      choreHolder.complete.setEnabled(false);
    }
    return convertView;
  }

  public void setUsers(List<UserModel> users) {
    this.users = users;
  }

  private void addEditView(View viewById, TaskModel task) {
    viewById.setOnLongClickListener(
        view -> {
          // Model add task
          final Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
          dialog.setContentView(R.layout.add_chore_dialog);
          dialog.show();
          dialog.findViewById(R.id.add_chore_dialog_delete).setVisibility(View.VISIBLE);
          dialog
              .findViewById(R.id.add_chore_dialog_delete)
              .setOnClickListener(
                  viewDelete -> {
                    model.removeTask(
                        task,
                        deletedTask -> {
                          if (deletedTask == null) {
                            Log.e("ChoreAdapter", "Task failed to delete");
                          } else {
                            Log.e("ChoreAdapter", "Task deleted successfully");
                          }
                        });
                    dialog.dismiss();
                  });
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
                      // Create a task and delete it
                      task.setName(name);
                      task.setDescription(description);
                      task.setPriority(priority);
                      model.updateTask(
                          task,
                          updatedTask -> {
                            if (updatedTask == null) {
                              Log.e("ChoreListAdapter", "Failed to update task");
                            } else {
                              Log.d("ChoreListAdapter", "Task updated");
                            }
                          });

                      dialog.dismiss();
                    } else {
                      Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
          return true;
        });
  }

  private void onClick(View v) {}

  private class ChoreHolder {
    TextView chore_name;
    TextView chore_description;
    TextView assigned_roommate;
    CheckBox complete;
  }
}
