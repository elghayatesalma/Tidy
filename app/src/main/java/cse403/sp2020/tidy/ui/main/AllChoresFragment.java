package cse403.sp2020.tidy.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import cse403.sp2020.tidy.R;

public class AllChoresFragment extends Fragment {

  private static final String TAG = "ALL_CHORES";


  private TableLayout allChores;
  private HashMap<String, Map<String, Object>> allChoresMap;

  private static final String TITLE_VIEW_KEY = "task_title_view_reference";
  private static final String DESC_VIEW_KEY = "task_description_view_reference";

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.allchores_fragment, container, false);
    allChores = view.findViewById(R.id.all_chores_table);
    allChoresMap = new HashMap<>();
    return view;
  }

  public void addTask(String taskId, Map<String, Object> taskData) {
    // Extract the title and description strings from the task data
    String titleStr = (String) taskData.get("Title");
    String descStr = (String) taskData.get("Description");
    TextView title;
    TextView description;

    Log.w(TAG, "Got taskID to add: " + taskId);

    // Case where this task is already in the table
    if (allChoresMap.containsKey(taskId)
            && allChoresMap.get(taskId).containsKey(TITLE_VIEW_KEY)
            && allChoresMap.get(taskId).containsKey(DESC_VIEW_KEY)) {
      Log.w(TAG, taskId + " has already been seen - reusing TextViews");
      title = (TextView) allChoresMap.get(taskId).get(TITLE_VIEW_KEY);
      description = (TextView) allChoresMap.get(taskId).get(DESC_VIEW_KEY);
    } else {
      // Case where a new task should be added to the table gui
      Log.w("HERE", "adding new row for task " + taskId);
      // Case where this is a newly added task
      TableRow row = new TableRow(this.getContext());
      allChores.addView(row);

      title = new TextView(this.getContext());
      title.setPadding(10, 0, 20, 5);
      row.addView(title);

      description = new TextView(this.getContext());
      description.setPadding(10, 0, 10, 5);
      row.addView(description);
    }

    title.setText((CharSequence) titleStr);
    description.setText((CharSequence) descStr);


    taskData.put(TITLE_VIEW_KEY, title);
    taskData.put(DESC_VIEW_KEY, description);
    allChoresMap.put(taskId, taskData);
  }
}
