package com.sl56.lis.androidapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;

public class AddHSCodeActivity extends AppCompatActivity {
    String[] titles = new String[]{"选择模式","输入模式"};
    ArrayList<Fragment> fragments = new ArrayList<>();
    private SegmentTabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hscode);
        tabLayout = (SegmentTabLayout)findViewById(R.id.lyAddHSCodeTabTitle);
        tabLayout.setTabData(titles);//设置选项卡标题
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        setTabAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private  void setTabAdapter(){
        FragmentManager fm = getSupportFragmentManager();
        MyPagerAdapter adapter = new MyPagerAdapter(fm,fragments);
        for (int i=0;i<titles.length;i++){
            fragments.add(new AddHSCodeViewFragment(i,AddHSCodeActivity.this));
        }
        adapter.setFragments(fragments);
        final ViewPager vpMain = (ViewPager) findViewById(R.id.vpAddHSCodeTabMain);
        ListView lvSelectHSCode = (ListView) vpMain.findViewById(R.id.lv_hscode_select);
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        vpMain.setAdapter(adapter);

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vpMain.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vpMain.setCurrentItem(tabLayout.getCurrentTab());
    }
    private class MyPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager fm;
        private ArrayList<Fragment> fragments;
        public MyPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments) {
            super(fm);
            this.fm=fm;
            this.fragments = fragments;
        }

        /**
         * FragmentManager会缓存Fragment，在更新Fragment前，要移除旧的Fragment，再更新fragmentlist
         * @param fragments 更新后的fragmentlist
         */
        public void setFragments(ArrayList fragments) {
            if(this.fragments != null){
                FragmentTransaction ft = fm.beginTransaction();
                for(Fragment f:this.fragments){
                    ft.remove(f);
                }
                ft.commit();
                ft=null;
                fm.executePendingTransactions();
            }
            this.fragments = fragments;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
