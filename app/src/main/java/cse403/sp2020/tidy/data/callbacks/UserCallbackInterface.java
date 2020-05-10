package cse403.sp2020.tidy.data.callbacks;

import java.util.List;

import cse403.sp2020.tidy.data.model.UserModel;

public interface UserCallbackInterface {
  public void userCallback(List<UserModel> users);

  public void userCallbackFailed();
}
