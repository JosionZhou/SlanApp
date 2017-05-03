package com.sl56.lis.androidapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by Josion on 2017/5/3.
 */

public class ExtendAdapter extends BaseAdapter {
    private List<Map.Entry<String,Boolean>> data;
    private Context context;
    public ExtendAdapter(Context context,List<Map.Entry<String,Boolean>> data){
        this.data=data;
        this.context=context;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater =  LayoutInflater.from(context);
        TextView tv = (TextView) inflater.inflate(android.R.layout.simple_list_item_1,null);
        Map.Entry<String,Boolean> entry = data.get(position);
        tv.setText(entry.getKey());
        if(entry.getValue())
            tv.setTextColor(Color.RED);
        return tv;
    }
}
