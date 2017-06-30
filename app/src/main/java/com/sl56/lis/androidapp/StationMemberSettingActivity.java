package com.sl56.lis.androidapp;

import android.content.Entity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.schedulers.Schedulers;

import static android.R.attr.action;
import static android.R.attr.dial;
import static com.nightonke.boommenu.Util.dp2px;


public class StationMemberSettingActivity extends AppCompatActivity {
    private JSONArray employeeList;
    private JSONArray stationList;
    private MaterialSpinner stationSpinner;
    private SwipeMenuListView stationMemberListView;
    private SwipeMenuListView employeeListView;
    private ArrayList<String> stationMemberData = new ArrayList<>();
    private ArrayList<String> employeeListData = new ArrayList<>();
    private Menu mMenu;//当前Activity菜单
    private int selectedIndex;
    private int groupId;//岗位Id
    private int createSwipMenuType=0;
    MaterialDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_member_setting);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        stationSpinner = (MaterialSpinner)findViewById(R.id.stationSpinner);
        stationSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selectedIndex=position;
                setStationMemberListView();
            }
        });
        stationMemberListView = (SwipeMenuListView)findViewById(R.id.station_member);
        employeeListView = (SwipeMenuListView)findViewById(R.id.employeelist);
        forceShowOverflowMenu();
        doAsync();
        selectedIndex=0;
    }

    /**
     * 获取当前选中的岗位下的成员，并填充ListView
     */
    private void setStationMemberListView(){
        Observable.just("GetPostGroupManipulators","GetManipulators")
                .map(new Func1<String, Map<String,JSONObject>>() {
                    @Override
                    public Map<String,JSONObject> call(String action) {
                        JSONObject params = new JSONObject();
                        Map<String,JSONObject> map = new Hashtable<String, JSONObject>();
                        try {
                            params.put("header",Global.getHeader());
                        if(action=="GetPostGroupManipulators"){
                            groupId=stationList.getJSONObject(selectedIndex).getInt("PostGroupId");
                            params.put("PostGroupId",groupId);
                        }
                        map.put(action,HttpHelper.getJSONObjectFromUrl(action,params));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return map;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, JSONObject>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Map<String, JSONObject> map) {
                        if(map.keySet().toArray()[0].toString()=="GetManipulators"){
                            try {
                                employeeList = ((JSONObject)(map.values().toArray())[0]).getJSONArray("Details");
                                employeeListData.clear();
                                for(int i=0;i<employeeList.length();i++) {
                                    JSONObject obj =  employeeList.getJSONObject(i);
                                    if(!stationMemberData.contains(obj.getString("ManipulatorName")))
                                        employeeListData.add(obj.getString("ManipulatorName"));
                                }
                                String[] data = employeeListData.toArray(new String[]{});
                                ArrayAdapter ad = new ArrayAdapter(StationMemberSettingActivity.this,android.R.layout.simple_list_item_1,data);
                                employeeListView.setAdapter(ad);
                                setListViewSwipeMenu(employeeListView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                JSONArray array = ((JSONObject)(map.values().toArray())[0]).getJSONArray("Details");
                                stationMemberData.clear();
                                for(int i=0;i<array.length();i++) {
                                    JSONObject obj =  array.getJSONObject(i);
                                    stationMemberData.add(obj.getString("ManipulatorName"));
                                }
                                String[] data = stationMemberData.toArray(new String[]{});
                                ArrayAdapter ad = new ArrayAdapter(StationMemberSettingActivity.this,android.R.layout.simple_list_item_1,data);
                                stationMemberListView.setAdapter(ad);
                                setListViewSwipeMenu(stationMemberListView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });




//        Observable.create(new Observable.OnSubscribe<JSONObject>() {
//            @Override
//            public void call(Subscriber<? super JSONObject> subscriber) {
//                JSONObject params = new JSONObject();
//                try {
//                    params.put("PostGroupId",stationList.getJSONObject(selectedIndex).getInt("PostGroupId"));
//                    params.put("header",Global.getHeader());
//                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPostGroupManipulators",params));
//                    subscriber.onCompleted();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        })
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe(new Observer<JSONObject>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//            }
//
//            @Override
//            public void onNext(JSONObject jsonObject) {
//                try {
//                    JSONArray array = jsonObject.getJSONArray("Details");
//                    stationMemberData.clear();
//                    for(int i=0;i<array.length();i++) {
//                        JSONObject obj =  array.getJSONObject(i);
//                        stationMemberData.add(obj.getString("ManipulatorName"));
//                    }
//                    String[] data = stationMemberData.toArray(new String[]{});
//                    ArrayAdapter ad = new ArrayAdapter(StationMemberSettingActivity.this,android.R.layout.simple_list_item_1,data);
//                    stationMemberListView.setAdapter(ad);
//                    setListViewSwipeMenu(stationMemberListView);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    /**
     * 获取岗位列表数据
     */
    private void doAsync(){

        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPostGrouplnfoList",params));
                subscriber.onCompleted();
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {
                    stationList=jsonObject.getJSONArray("PostGroupms");
                    ArrayList<String> data = new ArrayList<String>();
                    for(int i=0;i<stationList.length();i++) {
                        JSONObject obj =  stationList.getJSONObject(i);
                        data.add(obj.getString("PostGroupName"));
                    }
                    stationSpinner.setItems(data);
                    setStationMemberListView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 设置ListView项向左滑动时显示的菜单
     * @param view 需要显示菜单的ListView
     */
    private void setListViewSwipeMenu(SwipeMenuListView view){
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                //openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,0xCE)));
                // set item width
                //openItem.setWidth(dp2px(90));
                // set item title
                //openItem.setTitle("Open");
                // set item title fontsize
                //openItem.setTitleSize(18);
                // set item title font color
                //openItem.setTitleColor(Color.WHITE);
                // add to menu
                //menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem actionItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                actionItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                actionItem.setWidth(dp2px(90));
                // set a icon
                //deleteItem.setIcon(R.drawable.remove);
                if(createSwipMenuType==0)
                    actionItem.setTitle("移除");
                else
                    actionItem.setTitle("添加");
                actionItem.setTitleSize(18);
                actionItem.setTitleColor(R.color.white);
                // add to menu
                menu.addMenuItem(actionItem);
            }
        };
        view.setMenuCreator(creator);
        view.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        if(employeeListView.getVisibility()==View.GONE) {
                            employeeListData.add(stationMemberData.get(position));
                            stationMemberData.remove(position);

                            String[] data1 = stationMemberData.toArray(new String[]{});
                            ArrayAdapter ad1 = new ArrayAdapter(StationMemberSettingActivity.this,android.R.layout.simple_list_item_1,data1);
                            stationMemberListView.setAdapter(ad1);

                            String[] data2 = employeeListData.toArray(new String[]{});
                            ArrayAdapter ad2 = new ArrayAdapter(StationMemberSettingActivity.this, android.R.layout.simple_list_item_1, data2);
                            employeeListView.setAdapter(ad2);
                        }else{
                            stationMemberData.add(employeeListData.get(position));
                            employeeListData.remove(position);

                            String[] data1 = stationMemberData.toArray(new String[]{});
                            ArrayAdapter ad1 = new ArrayAdapter(StationMemberSettingActivity.this,android.R.layout.simple_list_item_1,data1);
                            stationMemberListView.setAdapter(ad1);

                            String[] data2 = employeeListData.toArray(new String[]{});
                            ArrayAdapter ad2 = new ArrayAdapter(StationMemberSettingActivity.this, android.R.layout.simple_list_item_1, data2);
                            employeeListView.setAdapter(ad2);
                        }
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    /**
     * 用动画过渡显示控件
     * @param view 需要显示的控件
     */
    private void showView(View view){
        //向上动画
//        TranslateAnimation showAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
//                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        //向下动画
        TranslateAnimation showAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f);
        showAction.setDuration(500);
        view.startAnimation(showAction);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * 用动画过渡隐藏控件
     * @param view 需要隐藏的控件
     */
    private void hideView(View view){
        TranslateAnimation hideAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f);
        hideAction.setDuration(500);
        view.startAnimation(hideAction);
        view.setVisibility(View.GONE);
    }

    /**
     * 选择菜单项时触发事件
     * @param item 点击的菜单项
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.add:
                stationMemberListView.setVisibility(View.GONE);
                showView(employeeListView);
                mMenu.clear();
                getMenuInflater().inflate(R.menu.editmenu,mMenu);
                createSwipMenuType=1;
                break;
            case R.id.done:
                employeeListView.setVisibility(View.GONE);
                showView(stationMemberListView);
                mMenu.clear();
                getMenuInflater().inflate(R.menu.stationsettingmenu,mMenu);
                createSwipMenuType=0;
                break;
            case R.id.save:
                dialog = new MaterialDialog.Builder(StationMemberSettingActivity.this)
                        .content("正在保存数据")
                        .progress(true,0)
                        .cancelable(false)
                        .show();
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设备存在实体菜单按键，需要屏蔽才可以显示OverFlowMenu
     */
    private void forceShowOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建菜单时触发事件
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stationsettingmenu, menu);
        mMenu=menu;
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 保存
     */
    private void save(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    JSONArray array = new JSONArray();
                    for(int i=0;i<employeeList.length();i++){
                        if(stationMemberData.contains(employeeList.getJSONObject(i).getString("ManipulatorName")))
                            array.put(employeeList.getJSONObject(i));
                    }
                    params.put("postGroupId",groupId);
                    params.put("Manipulators",array);
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("PostGroupMemberSave",params));
                subscriber.onCompleted();
            }
        })
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(new Subscriber<JSONObject>() {
             @Override
             public void onCompleted() {}

             @Override
             public void onError(Throwable e) {
                 e.printStackTrace();
             }

             @Override
             public void onNext(JSONObject array) {
                 dialog.dismiss();
                 try {
                     dialog = new MaterialDialog.Builder(StationMemberSettingActivity.this)
                             .content(array.getString("Contents"))
                             .cancelable(false)
                             .title(array.toString().contains("成功")?"保存成功":"保存失败")
                             .positiveText("确定")
                             .show();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });


    }
}
