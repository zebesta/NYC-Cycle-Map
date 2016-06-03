package com.example.chrissebesta.nyccyclemap.details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.chrissebesta.nyccyclemap.R;

import java.util.ArrayList;

/**
 * Created by chrissebesta on 5/26/16.
 * Used to populate a list view with information from the Details class
 */
public class DetailAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Detail> details;

    public DetailAdapter(Context context, ArrayList<Detail> details) {
        this.context = context;
        this.details = details;
    }
    @Override
    public int getCount() {
        return details.size();
    }

    @Override
    public Object getItem(int position) {
        return details.get(position);
    }

    @Override
    public long getItemId(int position) {
        //return 0;
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_detail, null);

        TextView text1 = (TextView) view.findViewById(R.id.key_text_view);
        TextView text2 = (TextView) view.findViewById(R.id.value_text_view);

        text1.setText(details.get(position).getKey());
        text2.setText(details.get(position).getValue());

        return view;
    }
}
