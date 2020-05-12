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

public class ChoreListArrayAdapter<E> extends ArrayAdapter {

    private final LayoutInflater inflater;
    ChoreListArrayAdapter(Context context, List<E> objects){
        super(context, 0, objects);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getViewTypeCount(){
        return 1;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        TaskModel chore = (TaskModel) getItem(position);
        ChoreHolder choreHolder;
        if(convertView == null || convertView.findViewById(R.id.chore_list_element_name) == null){
            convertView = inflater.inflate(R.layout.chore_list_element, parent, false);
            choreHolder = new ChoreHolder();
            choreHolder.chore_name = convertView.findViewById(R.id.chore_list_element_name);
            choreHolder.chore_description = convertView.findViewById(R.id.chore_list_element_description);
            choreHolder.assigned_roommate = convertView.findViewById(R.id.chore_list_element_roommate);
            convertView.setTag(choreHolder);
        }else{
            choreHolder = (ChoreHolder) convertView.getTag();
        }
        assert chore != null;
        choreHolder.chore_name.setText(chore.getName());
        choreHolder.chore_description.setText(chore.getDescription());
        choreHolder.assigned_roommate.setText("");//TODO add assigned roommate
        return convertView;
    }

    private class ChoreHolder{
        TextView chore_name;
        TextView chore_description;
        TextView assigned_roommate;
    }
}
