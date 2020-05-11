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

public class AllChoresArrayAdapter<E> extends ArrayAdapter {

    private final LayoutInflater inflater;
    AllChoresArrayAdapter(Context context, List<E> objects){
        super(context, 0, objects);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getViewTypeCount(){
        return 1;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        Object chore = (Object) getItem(position);
        ChoreHolder choreHolder;
        if(convertView == null || convertView.findViewById(R.id.all_chores_chore_name) == null){
            convertView = inflater.inflate(R.layout.allchores_chore_view, parent, false);
            choreHolder = new ChoreHolder();
            choreHolder.chore_name = convertView.findViewById(R.id.all_chores_chore_name);
            choreHolder.chore_date = convertView.findViewById(R.id.all_chores_chore_date);
            choreHolder.assigned_roommate = convertView.findViewById(R.id.all_chores_chore_roommate);
            convertView.setTag(choreHolder);
        }else{
            choreHolder = (ChoreHolder) convertView.getTag();
        }
        assert chore != null;
        choreHolder.chore_name.setText("");
        choreHolder.chore_date.setText("");
        choreHolder.assigned_roommate.setText("");
        return convertView;
    }

    private class ChoreHolder{
        TextView chore_name;
        TextView chore_date;
        TextView assigned_roommate;
    }
}
