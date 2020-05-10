package cse403.sp2020.tidy.data.callbacks;

import cse403.sp2020.tidy.data.model.HouseholdModel;

public interface HouseholdCallbackInterface {
  public void householdCallback(HouseholdModel household);
  public void householdCallbackFailed();
}
