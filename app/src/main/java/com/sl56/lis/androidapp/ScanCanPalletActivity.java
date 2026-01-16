package com.sl56.lis.androidapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ScanCanPalletActivity extends AppCompatActivity {

    private EditText etPackageNumber;
    private TextView tvClearanceInfo;
    private MaterialDialog pDialog;
    public final OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_can_pallet);
        tvClearanceInfo = (TextView)findViewById(R.id.tv_clearanceinfo);
        etPackageNumber = (EditText)findViewById(R.id.et_innerPackageNumber);
        etPackageNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    ScanRgd();
                    return true;
                }
                return false;
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
     * 是否可以打板扫描
     */
    private void ScanRgd(){
        final String packageNumber = etPackageNumber.getText().toString();
        if(packageNumber.isEmpty()){
            showAlertMessage("提示","单号不能为空");
            return;
        }
        pDialog = new MaterialDialog.Builder(this)
                .content("数据查询中...")
                .cancelable(false)
                .progress(true,0)
                .show();
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
                    @Override
                    public void call(Subscriber<? super JSONObject> subscriber) {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("packageNumber",packageNumber);
                            params.put("header",Global.getHeader());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext(HttpHelper.getJSONObjectFromUrl("CanPalletize",params));
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
                                    showAlertMessage("网络访问异常",errorMsg);
                                } else {
                                    Boolean isSuccess = jsonObject.getBoolean("Success");
                                    String message = jsonObject.getString("Message");
                                    if (!isSuccess) {
                                        showAlertMessage("警告⚠️",message);
                                        etPackageNumber.requestFocus();
                                        etPackageNumber.selectAll();
                                    }else{
                                        showAlertMessage("提示","可以打板扫描");
                                    }
                                }
                            }catch (Exception e){
                                showAlertMessage("系统异常",e.getMessage());
                            }
                        }
                        etPackageNumber.selectAll();
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

    private void showAlertMessage(final String title, final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(ScanCanPalletActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                etPackageNumber.setText("");
                            }
                        })
                        .show();
                VibratorHelper.shock(ScanCanPalletActivity.this);
            }
        });
    }
}
