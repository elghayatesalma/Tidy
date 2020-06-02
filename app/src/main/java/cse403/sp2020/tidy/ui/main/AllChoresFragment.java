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

public class AllChoresFragment extends ChoresFragment {
  protected String TAG = "ALL_CHORES";

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    final View frag = inflater.inflate(R.layout.allchores_fragment, container, false);
    ListView allChoreListView = frag.findViewById(R.id.all_chores_list);
    addOnClick(frag.findViewById(R.id.all_chores_add));

    frag.findViewById(R.id.all_chores_add);
    choreList = new ChoreListArrayAdapter<>(getContext(), new ArrayList<>());
    allChoreListView.setAdapter(choreList);
    return frag;
  }

  @Override
  public void updateChoreList(List<TaskModel> tasks) {
    choreList.clear();
    choreList.addAll(tasks);
  }

  @Override
  public void addTask(TaskModel newTask) {
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
