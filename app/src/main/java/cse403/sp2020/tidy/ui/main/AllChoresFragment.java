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
import java.util.List;
import java.util.Objects;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.ModelInterface;
import cse403.sp2020.tidy.data.callbacks.TaskCallbackInterface;
import cse403.sp2020.tidy.data.model.TaskModel;
import cse403.sp2020.tidy.ui.MainActivity;

public class AllChoresFragment extends Fragment {
  private ModelInterface model;
  private String userId;
  private ArrayList<TaskModel> choreList;
  private ChoreListArrayAdapter<TaskModel> allChoreAdapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    model = ((MainActivity) Objects.requireNonNull(getActivity())).getModelInterface();
    if(savedInstanceState != null)
      userId = savedInstanceState.getString("tidy_user_id", null);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View frag = inflater.inflate(R.layout.allchores_fragment, container, false);
    ListView allChoreListView = frag.findViewById(R.id.all_chores_list);
    Button addChore = frag.findViewById(R.id.all_chores_add);
    addChore.setOnClickListener(view -> {
      //Model add task
      final Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
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
        int priority = -1;//Dummy value that will never be used
        try{
          priority = Integer.parseInt(priorityStr, 10);
        }catch (NumberFormatException ex){
          valid = false;
        }
        if(valid){
          model.addTaskToHousehold(new TaskModel(name, description, priority));
          dialog.dismiss();
        }else{
          Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
        }
      });
    });
    choreList = new ArrayList<>();
    allChoreAdapter = new ChoreListArrayAdapter<>(getContext(), choreList);
    allChoreListView.setAdapter(allChoreAdapter);
    setModelCallBacks();
    model.setUser(userId);//Initiates data collection callbacks to initialize tasks
    return frag;
  }

  @Override
  public void onPause() {
    super.onPause();
    model.cleanUp();
  }

  @Override
  public void onDestroy(){
    super.onDestroy();
    model.cleanUp();
  }

  private void setModelCallBacks(){
    model.registerTaskCallback(new TaskCallbackInterface() {
      @Override
      public void taskCallback(List<TaskModel> users) {
        choreList.clear();
        choreList.addAll(users);
        allChoreAdapter.notifyDataSetChanged();
      }

      @Override
      public void taskCallbackFail(String message) { }
    });
  }
}
