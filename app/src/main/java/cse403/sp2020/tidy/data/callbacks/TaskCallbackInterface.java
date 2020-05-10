package cse403.sp2020.tidy.data.callbacks;

import java.util.List;

import cse403.sp2020.tidy.data.model.TaskModel;

public interface TaskCallbackInterface {
  public void taskCallback(List<TaskModel> users);

  public void taskCallbackFail();
}
