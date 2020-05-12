package cse403.sp2020.tidy.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.model.TaskModel;

public class AllChoresFragment extends Fragment {

  private ArrayList<TaskModel> choreList;
  private AllChoresArrayAdapter<TaskModel> allChoreAdapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View frag = inflater.inflate(R.layout.allchores_fragment, container, false);
    ListView allChoreListView = frag.findViewById(R.id.all_chores_list);
    Button addChore = frag.findViewById(R.id.all_chores_add);
    addChore.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //Model add task
      }
    });
    choreList = new ArrayList<>();
    //choreList.addAll(model.getAllTasks(id));
    allChoreAdapter = new AllChoresArrayAdapter<>(getContext(), choreList);
    allChoreListView.setAdapter(allChoreAdapter);
    return frag;
  }
}
