package cse403.sp2020.tidy.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cse403.sp2020.tidy.R;

public class MyChoresFragment extends Fragment {
  private static final String TAG = "MyChoresFragment";

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.mychores_fragment, container, false);

    return view;
  }
}
