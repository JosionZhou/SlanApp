package com.baoyz.swipemenulistview;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/* loaded from: D:\Downloads\dex-tools-2.1-20150601.060031-26\dex2jar-2.1-SNAPSHOT\classes.dex */
public class SwipeMenu {
    private Context mContext;
    private List<SwipeMenuItem> mItems = new ArrayList();
    private int mViewType;

    public SwipeMenu(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void addMenuItem(SwipeMenuItem item) {
        this.mItems.add(item);
    }

    public void removeMenuItem(SwipeMenuItem item) {
        this.mItems.remove(item);
    }

    public List<SwipeMenuItem> getMenuItems() {
        return this.mItems;
    }

    public SwipeMenuItem getMenuItem(int index) {
        return this.mItems.get(index);
    }

    public int getViewType() {
        return this.mViewType;
    }

    public void setViewType(int viewType) {
        this.mViewType = viewType;
    }
}