package com.sl56.lis.androidapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class PackagingOptimizationActivity extends AppCompatActivity {

    private EditText etReferenceNumber;
    private MaterialDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.packaging_optimization);
        etReferenceNumber = (EditText)findViewById(R.id.et_referencenumber);
        etReferenceNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    PackagingOptimization(0);
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.btn_mark_PackagingOptimization).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackagingOptimization(0);
            }
        });
        findViewById(R.id.btn_cancel_PackagingOptimization).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackagingOptimization(1);
            }
        });

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        forceShowOverflowMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 标识/取消包装优化
     * actionType 0:标识包装优化  1:取消包装优化
     */
    private void PackagingOptimization(final int actionType){
        final String refNumber = etReferenceNumber.getText().toString();
        if(refNumber.isEmpty()){
            showDialog("提示","单号不能为空");
            return;
        }
        pDialog = new MaterialDialog.Builder(this)
                .content("数据同步中...")
                .cancelable(false)
                .progress(true,0)
                .show();
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("referenceNumber",refNumber);
                    params.put("actionType",actionType);
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("PackagingOptimization",params));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                pDialog.dismiss();
                if(jsonObject!=null&&!jsonObject.toString().trim().isEmpty()){
                    try {
                        if (jsonObject.toString().contains("Error")) {
                            String errorMsg = jsonObject.getString("Error");
                            showDialog("网络访问异常",errorMsg);
                        } else {
                            Boolean isSuccess = jsonObject.getBoolean("Success");
                            if (!isSuccess) {
                                String errorMsg = jsonObject.getString("Message");
                                showDialog("操作失败",errorMsg);
                            }else{
                                showDialogAndShock("操作成功",etReferenceNumber.getText().toString()+ (actionType==0?" 已标识包装优化":" 已取消包装优化"),false);
                                etReferenceNumber.setText("");
                                etReferenceNumber.requestFocus();
                            }
                        }
                    }catch (Exception e){
                        showDialog("系统异常",e.getMessage());
                    }
                }
                etReferenceNumber.selectAll();
            }
        });
    }
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
    private void showDialogAndShock(String title,String content,boolean isShock){
        new MaterialDialog.Builder(PackagingOptimizationActivity.this)
                .positiveText("确定")
                .title(title)
                .content(content)
                .cancelable(false)
                .show();
        if(isShock)
            VibratorHelper.shock(PackagingOptimizationActivity.this);
    }
    private void showDialog(String title,String content){
        showDialogAndShock(title,content,true);
    }
}
