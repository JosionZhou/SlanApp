package com.sl56.lis.androidapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.nightonke.boommenu.Util.dp2px;

public class HSCodeListActivity extends AppCompatActivity {
    MaterialDialog dialog;
    int receiveGoodsDetailId=0;
    List<ListViewItem> data=null;
    SwipeMenuListView listView;
    boolean isProgressDialogShowing=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hscode_list);
        listView =(SwipeMenuListView)findViewById(R.id.lvhscode);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
         receiveGoodsDetailId = this.getIntent().getIntExtra("receiveGoodsDetailId",0);
        if(receiveGoodsDetailId<=0){
            dialog = new MaterialDialog.Builder(HSCodeListActivity.this)
                    .title("异常问题")
                    .content("无收货ID")
                    .positiveText("确定")
                    .show();
            finish();
        }else{
            getCurrentHSCodeList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hscodemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.newhscode:
                Intent it = new Intent(HSCodeListActivity.this,AddHSCodeActivity.class);
                this.startActivityForResult(it,0);
                break;
            case R.id.savehscode:
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save(){
        dialog = new MaterialDialog.Builder(HSCodeListActivity.this)
                .progress(true,0)
                .title("数据保存中")
                .content("请稍后...")
                .cancelable(false)
                .show();
        isProgressDialogShowing=true;

        //当获取数据的dialog显示时间超过10秒是，认为提交数据失败
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    Thread.sleep(10*1000);
                    subscriber.onNext(isProgressDialogShowing);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Boolean>() {
            @Override
            public void call(final Boolean isShowing) {
                if(isShowing){
                    VibratorHelper.shock(HSCodeListActivity.this);
                    dialog.dismiss();
                    dialog =  new MaterialDialog.Builder(HSCodeListActivity.this)
                            .content("保存失败，是否重新保存")
                            .cancelable(false)
                            .positiveText("是")
                            .negativeText("否")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    save();
                                }
                            })
                            .show();
                }
            }
        });
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    String ids = "";
                    for(ListViewItem item : data){
                        ids+=item.getId();
                        if(data.indexOf(item)!=data.size()-1)
                            ids+=",";
                    }
                    params.put("codeIdList",ids);
                    params.put("receiveGoodsDetailId",receiveGoodsDetailId);
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("SaveCommodityHSCode",params));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {
                    dialog.dismiss();
                    isProgressDialogShowing=false;
                    boolean success = jsonObject.getBoolean("Success");
                    if(!success){
                        String message = jsonObject.getString("Message");
                        dialog = new MaterialDialog.Builder(HSCodeListActivity.this)
                                .title("保存失败")
                                .content(message)
                                .cancelable(false)
                                .positiveText("确定")
                                .show();
                        VibratorHelper.shock(HSCodeListActivity.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        if(intentData==null)return;
        String[] result = (String[])intentData.getCharSequenceArrayExtra("selectObj");
        if(data == null)
            data = new ArrayList<ListViewItem>();
        ListViewItem lvi = new ListViewItem();
        lvi.setId(Integer.parseInt(result[0]));
        lvi.setTitle(result[1]);
        lvi.setDescription(result[2]);
        data.add(lvi);
        listView.setAdapter(new TwoLineListviewAdapter(HSCodeListActivity.this,data));
    }

    private void getCurrentHSCodeList(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("receiveGoodsDetailId",receiveGoodsDetailId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetCommodityHSCode",params));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {
                    JSONArray array = jsonObject.getJSONArray("HSCodeList");
                    data = new ArrayList<ListViewItem>();
                    for (int i =0;i<array.length();i++){
                        ListViewItem lvi = new ListViewItem();
                        lvi.setId(array.getJSONObject(i).getInt("ObjectId"));
                        lvi.setTitle(array.getJSONObject(i).getString("ObjectNo"));
                        lvi.setDescription(array.getJSONObject(i).getString("Description"));
                        data.add(lvi);
                    }
                    listView.setAdapter(new TwoLineListviewAdapter(HSCodeListActivity.this,data));
                    setListViewSwipeMenu(listView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void setListViewSwipeMenu(SwipeMenuListView view){
        SwipeMenuCreator creator = new SwipeMenuCreator(){
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());

                SwipeMenuItem actionItem = new SwipeMenuItem(
                        getApplicationContext());
                actionItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                actionItem.setWidth(dp2px(90));
                    actionItem.setTitle("移除");
                actionItem.setTitleSize(18);
                actionItem.setTitleColor(R.color.white);
                menu.addMenuItem(actionItem);
            }
        };
        view.setMenuCreator(creator);
        view.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                data.remove(position);
                listView.setAdapter(new TwoLineListviewAdapter(HSCodeListActivity.this,data));
                return true;
            }
        });
    }
}
