package com.sl56.lis.androidapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class PalletListAdapter extends BaseAdapter {
    List<PalletItem> list;
    Context context;
    public PalletListAdapter(List<PalletItem> list, Context context) {
        // TODO Auto-generated constructor stub
        this.list=list;
        this.context=context;


    }
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder viewHolder;
        if(convertView==null){
            convertView=View.inflate(context, R.layout.listview_item, null);
            viewHolder=new ViewHolder();
            viewHolder.textView=(TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.checkBox=(CheckBox) convertView.findViewById(R.id.cbCheckBox);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(list.get(position).No);

        //显示checkBox
        viewHolder.checkBox.setChecked(list.get(position).IsChecked);

        return convertView;

    }
    class ViewHolder{
        TextView textView;
        CheckBox checkBox;
    }
}
