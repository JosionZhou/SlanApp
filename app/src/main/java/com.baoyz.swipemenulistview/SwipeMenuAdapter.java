package com.baoyz.swipemenulistview;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuView;

/* loaded from: D:\Downloads\dex-tools-2.1-20150601.060031-26\dex2jar-2.1-SNAPSHOT\classes.dex */
public class SwipeMenuAdapter implements WrapperListAdapter, SwipeMenuView.OnSwipeItemClickListener {
    private ListAdapter mAdapter;
    private Context mContext;
    private SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener;

    public SwipeMenuAdapter(Context context, ListAdapter adapter) {
        this.mAdapter = adapter;
        this.mContext = context;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mAdapter.getCount();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.mAdapter.getItem(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return this.mAdapter.getItemId(position);
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            View contentView = this.mAdapter.getView(position, convertView, parent);
            SwipeMenu menu = new SwipeMenu(this.mContext);
            menu.setViewType(this.mAdapter.getItemViewType(position));
            createMenu(menu);
            SwipeMenuView menuView = new SwipeMenuView(menu, (SwipeMenuListView) parent);
            menuView.setOnSwipeItemClickListener(this);
            SwipeMenuListView listView = (SwipeMenuListView) parent;
            SwipeMenuLayout layout = new SwipeMenuLayout(contentView, menuView, listView.getCloseInterpolator(), listView.getOpenInterpolator());
            layout.setPosition(position);
            return layout;
        }
        SwipeMenuLayout layout2 = (SwipeMenuLayout) convertView;
        layout2.closeMenu();
        layout2.setPosition(position);
        this.mAdapter.getView(position, layout2.getContentView(), parent);
        return layout2;
    }

    public void createMenu(SwipeMenu menu) {
        SwipeMenuItem item = new SwipeMenuItem(this.mContext);
        item.setTitle("Item 1");
        item.setBackground(new ColorDrawable(-7829368));
        item.setWidth(300);
        menu.addMenuItem(item);
        SwipeMenuItem item2 = new SwipeMenuItem(this.mContext);
        item2.setTitle("Item 2");
        item2.setBackground(new ColorDrawable(-65536));
        item2.setWidth(300);
        menu.addMenuItem(item2);
    }

    @Override // com.baoyz.swipemenulistview.SwipeMenuView.OnSwipeItemClickListener
    public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
        SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener = this.onMenuItemClickListener;
        if (onMenuItemClickListener != null) {
            onMenuItemClickListener.onMenuItemClick(view.getPosition(), menu, index);
        }
    }

    public void setOnMenuItemClickListener(SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mAdapter.registerDataSetObserver(observer);
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mAdapter.unregisterDataSetObserver(observer);
    }

    @Override // android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return this.mAdapter.areAllItemsEnabled();
    }

    @Override // android.widget.ListAdapter
    public boolean isEnabled(int position) {
        return this.mAdapter.isEnabled(position);
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return this.mAdapter.hasStableIds();
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int position) {
        return this.mAdapter.getItemViewType(position);
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return this.mAdapter.getViewTypeCount();
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return this.mAdapter.isEmpty();
    }

    @Override // android.widget.WrapperListAdapter
    public ListAdapter getWrappedAdapter() {
        return this.mAdapter;
    }
}