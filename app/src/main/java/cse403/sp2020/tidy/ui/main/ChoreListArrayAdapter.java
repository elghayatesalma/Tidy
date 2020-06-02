package cse403.sp2020.tidy.ui.main;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.CallbackInterface;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.data.model.UserModel;

public class ChoreListArrayAdapter<E> extends ArrayAdapter {

  private final LayoutInflater inflater;
  private ModelInterface model;
  private boolean toggleable;
  private List<UserModel> users;

  ChoreListArrayAdapter(Context context, List<E> objects, ModelInterface model, boolean toggleable) {
    super(context, 0, objects);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.model = model;
    this.toggleable = toggleable;
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
    if (users != null) {
      for (UserModel user : users) {
        if (user.getFirebaseId().equals(uid)) {
          assigned = user.getFirstName() + " " + user.getLastName();
        }
      }
    }
    choreHolder.assigned_roommate.setText(assigned);
    choreHolder.complete.setChecked(chore.isCompleted());

    if (this.toggleable) {
      View.OnClickListener li = l -> {
        // Toggle the checkbox if this is the choreholder view
        if (!(l instanceof CheckBox)) {
          choreHolder.complete.toggle();
        } // otherwise it was already toggled by clicking on it
        boolean completed = choreHolder.complete.isChecked();
        chore.setCompleted(completed);
        this.model.updateTask(chore, t -> {
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
      choreHolder.complete.setEnabled(false);
    }
    return convertView;
  }

  public void setUsers(List<UserModel> users) {
    this.users = users;
  }

  private class ChoreHolder {
    TextView chore_name;
    TextView chore_description;
    TextView assigned_roommate;
    CheckBox complete;
  }
}
