package cse403.sp2020.tidy.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import cse403.sp2020.tidy.R;
import cse403.sp2020.tidy.data.model.TaskModel;

/**
 * Array Adapter for a chore list
 * @param <E> generic type
 */
public class ChoreListArrayAdapter<E> extends ArrayAdapter {

  private final LayoutInflater inflater;

  /**
   * Constructor that sets a layout inflater
   * @param context the active context of the app
   * @param objects the objects representing the elements of the array
   */
  ChoreListArrayAdapter(Context context, List<E> objects) {
    super(context, 0, objects);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  /**
   * Get the number of distinct views that appear in the list
   * @return the number of distinct views to be displayed
   */
  @Override
  public int getViewTypeCount() {
    return 1;
  }

  /**
   * Generates the view that corresponds with the appropriate element in the list.
   * @param position index in the list
   * @param convertView view from the pool that will be loaded next in the list
   * @param parent the viewgroup that contains the view to be returned
   * @return inflated view that corresponds to an element in the list
   */
  @Override
  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    TaskModel chore = (TaskModel) getItem(position);
    ChoreHolder choreHolder;
    if (convertView == null || convertView.findViewById(R.id.chore_list_element_name) == null) {
      convertView = inflater.inflate(R.layout.chore_list_element, parent, false);
      choreHolder = new ChoreHolder();
      choreHolder.chore_name = convertView.findViewById(R.id.chore_list_element_name);
      choreHolder.chore_description = convertView.findViewById(R.id.chore_list_element_description);
      choreHolder.assigned_roommate = convertView.findViewById(R.id.chore_list_element_roommate);
      convertView.setTag(choreHolder);
    } else {
      choreHolder = (ChoreHolder) convertView.getTag();
    }
    assert chore != null;
    choreHolder.chore_name.setText(chore.getName());
    choreHolder.chore_description.setText(chore.getDescription());
    choreHolder.assigned_roommate.setText(""); // TODO add assigned roommate
    return convertView;
  }

  /**
   * Holds the views of recycled element in the list so that they may be reload faster. Tagged onto
   * the view when it is recycled.
   */
  private class ChoreHolder {
    TextView chore_name;
    TextView chore_description;
    TextView assigned_roommate;
  }
}
