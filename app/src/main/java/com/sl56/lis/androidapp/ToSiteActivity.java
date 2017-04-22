package com.sl56.lis.androidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ToSiteActivity extends AppCompatActivity {

    private EditText etReferenceNumber;
    private MaterialSpinner spinner;
    private ListView lvSendGoods;
    private Integer toSiteId;
    private Integer fromSiteId;
    private List<String > scanedNoList = new ArrayList<>();
    private List<Map.Entry<String,Integer>> sites;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_site);
        etReferenceNumber = (EditText)findViewById(R.id.et_referencenumber);
        etReferenceNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    String msg=null;
                    if(etReferenceNumber.getText().toString().toString().isEmpty())
                        msg="请扫描或输入单号";
                    else if(toSiteId==null||toSiteId<0)
                        msg="请选择出货目的地";
                    if(msg != null){
                        new MaterialDialog.Builder(ToSiteActivity.this)
                                .positiveText("确定")
                                .cancelable(false)
                                .content(msg)
                                .title("警告")
                                .show();
                        VibratorHelper.shock(ToSiteActivity.this);
                        etReferenceNumber.selectAll();
                        return false;
                    }
                    scan();
                    return true;
                }
                return false;
            }
        });
        spinner = (MaterialSpinner)findViewById(R.id.spinner_company);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                toSiteId = sites.get(position).getValue();
            }
        });
        lvSendGoods = (ListView)findViewById(R.id.lv_sendgoods);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        try {
            fromSiteId = Global.getHeader().getInt("SiteId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<String> siteNames = new ArrayList<>();
        sites = Global.getSites();
        for (Map.Entry<String,Integer> entry : sites)
            siteNames.add(entry.getKey());
        spinner.setItems(siteNames);
        toSiteId = sites.get(0).getValue();
    }
    private void scan(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("referenceNumber",etReferenceNumber.getText().toString().trim());
                    params.put("fromSiteId",fromSiteId);
                    params.put("toSiteId",toSiteId);
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("ToSiteScan",params));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {
                    boolean isSuccess = jsonObject.getBoolean("Success");
                    if(isSuccess)
                    {
                        scanedNoList.add(etReferenceNumber.getText().toString());
                        ArrayAdapter ad = new ArrayAdapter(ToSiteActivity.this,android.R.layout.simple_list_item_1,scanedNoList);
                        lvSendGoods.setAdapter(ad);
                        etReferenceNumber.setText("");
                    }else{
                        new MaterialDialog.Builder(ToSiteActivity.this)
                                .title("扫描失败")
                                .content(jsonObject.getString("Message"))
                                .cancelable(false)
                                .positiveText("确定")
                                .show();
                        VibratorHelper.shock(ToSiteActivity.this);
                        etReferenceNumber.selectAll();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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

}
