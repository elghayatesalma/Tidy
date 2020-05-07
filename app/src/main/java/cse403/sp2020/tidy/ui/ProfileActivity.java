package cse403.sp2020.tidy.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import cse403.sp2020.tidy.R;

public class ProfileActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }
}
