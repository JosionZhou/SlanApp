package com.sl56.lis.androidapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by Josion on 2017/8/9.
 */

public class TwoLineListviewAdapter extends BaseAdapter {

    private List<ListViewItem> data;
    private Context context;
    public TwoLineListviewAdapter(Context context,List<ListViewItem> data){
        this.context=context;
        this.data = data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater =  LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.twolinelistviewitem,null);
        TextView tvTitle = (TextView) view.findViewById(R.id.itemTitle);
        TextView tvDescription = (TextView) view.findViewById(R.id.itemText);
        ListViewItem lvi = data.get(position);
        tvTitle.setTag(lvi.getId());
        tvTitle.setText(lvi.getTitle());
        tvTitle.setTextColor(Color.RED);
        tvDescription.setText(lvi.getDescription());
        return view;
    }
}
