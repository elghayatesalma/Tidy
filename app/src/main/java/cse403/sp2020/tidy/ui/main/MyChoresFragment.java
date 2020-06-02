package cse403.sp2020.tidy.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.model.TaskModel;

public class MyChoresFragment extends ChoresFragment {
  protected String TAG = "MY_CHORES";

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    final View frag = inflater.inflate(R.layout.mychores_fragment, container, false);
    ListView allChoreListView = frag.findViewById(R.id.my_chores_list);
    addOnClick(frag.findViewById(R.id.my_chores_add));

    frag.findViewById(R.id.my_chores_add);
    choreList = new ChoreListArrayAdapter<>(getContext(), new ArrayList<>(), model, true);
    allChoreListView.setAdapter(choreList);
    return frag;
  }

  @Override
  public void updateChoreList(List<TaskModel> tasks) {
    choreList.clear();

    // Get all user related chores
    for (TaskModel task : tasks) {
      if (model.getCurrentUser().getFirebaseId() != null
          && model.getCurrentUser().getFirebaseId().equals(task.getAssignedTo())) {
        choreList.add(task);
      }
    }
  }

  @Override
  public void addTask(TaskModel newTask) {
    // Set user first
    newTask.setAssignedTo(model.getCurrentUser().getFirebaseId());
    model.addTask(
        newTask,
        task -> {
          if (task == null) {
            Log.e(TAG, "Failed to add a new task");
          } else {
            Log.d(
                TAG,
                "New task added -- "
                    + "Name: "
                    + task.getName()
                    + ", Desc:"
                    + task.getDescription()
                    + ", Priority: "
                    + task.getPriority());
          }
        });
  }
}
