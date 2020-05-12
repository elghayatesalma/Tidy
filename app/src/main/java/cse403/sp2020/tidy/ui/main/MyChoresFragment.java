package cse403.sp2020.tidy.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.model.TaskModel;

public class MyChoresFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
    final View frag = inflater.inflate(R.layout.mychores_fragment, container, false);
    ListView myChoreListView = frag.findViewById(R.id.my_chores_list);
    Button addChore = frag.findViewById(R.id.my_chores_add);
    addChore.setOnClickListener(view -> {
      //Model add task
      final Dialog dialog = new Dialog(getContext());
      dialog.setContentView(R.layout.add_chore_dialog);
      dialog.show();
      dialog.findViewById(R.id.add_chore_dialog_cancel).setOnClickListener(view1 -> dialog.dismiss());
      dialog.findViewById(R.id.add_chore_dialog_submit).setOnClickListener(view12 -> {
        String name = ((EditText)dialog.findViewById(R.id.add_chore_dialog_name)).getText().toString();
        String description = ((EditText)dialog.findViewById(R.id.add_chore_dialog_description)).getText().toString();
        String priorityStr = ((EditText)dialog.findViewById(R.id.add_chore_dialog_priority)).getText().toString();
        boolean valid = !name.isEmpty();
        valid &= !description.isEmpty();
        valid &= !priorityStr.isEmpty();
        int priority;
        try{
          priority = Integer.parseInt(priorityStr, 10);
        }catch (NumberFormatException ex){
          valid = false;
        }
        if(valid){
          //TODO model add task();
        }else{
          Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
        }
      });
    });
    ArrayList<TaskModel> choreList = new ArrayList<>();
    //TODO choreList.addAll(model.getAllTasks(id));
    ChoreListArrayAdapter<TaskModel> choreListAdapter = new ChoreListArrayAdapter<>(getContext(), choreList);
    myChoreListView.setAdapter(choreListAdapter);
    return frag;
  }
}
