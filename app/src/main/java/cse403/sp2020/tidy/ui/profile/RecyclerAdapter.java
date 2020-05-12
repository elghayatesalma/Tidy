package cse403.sp2020.tidy.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cse403.sp2020.tidy.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

  private static final String TAG = "RecyclerAdapter";
  private List<String> chorePreferences;

  public RecyclerAdapter(List<String> chorePreferences) {
    this.chorePreferences = chorePreferences;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    // holder.rowCountTextView.setText(String.valueOf(position));
    holder.textViewChore.setText(chorePreferences.get(position));
  }

  @Override
  public int getItemCount() {
    return chorePreferences.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView textViewChore;

    public ViewHolder(View v) {
      super(v);
      textViewChore = (TextView) v.findViewById(R.id.chore_preference_name);
    }

    @Override
    public void onClick(View v) {
      Toast.makeText(v.getContext(), chorePreferences.get(getAdapterPosition()), Toast.LENGTH_SHORT)
          .show();
    }
  }
}
