package com.sl56.lis.androidapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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


public class BindFBAActivity extends AppCompatActivity {

    private EditText etPackageNumber;
    private EditText etFBANumber;
    private MaterialDialog pDialog;
    private Button btnReScan;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_fba);
        etPackageNumber = (EditText)findViewById(R.id.et_package_number);
        etFBANumber = (EditText)findViewById(R.id.et_fba_number);
        btnReScan = (Button)findViewById(R.id.btn_re_scan);
        etPackageNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    etFBANumber.requestFocus();
                    return true;
                }
                return false;
            }
        });
        etFBANumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    BindFBAToTDD();
                    return true;
                }
                return false;
            }
        });
        btnReScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etPackageNumber.requestFocus();
                etPackageNumber.setText("");
                etFBANumber.setText("");
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
     * 绑定FBA单号到子单号
     */
    private void BindFBAToTDD(){
        final String packageNumber = etPackageNumber.getText().toString();
        final String fbaNumber = etFBANumber.getText().toString();
        if(packageNumber.isEmpty()){
            showDialog("提示","子单号不能为空");
            return;
        }
        if(fbaNumber.isEmpty()){
            showDialog("提示","FBA号不能为空");
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
                    params.put("packageNumber",packageNumber);
                    params.put("fbaNumber",fbaNumber);
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("BindFBAToTransportDocumentDetail",params));
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
                            String message = jsonObject.getString("Message");
                            if (!isSuccess) {
                                showDialog("操作失败",message);
                                etPackageNumber.requestFocus();
                                etPackageNumber.selectAll();
                            }else{
                                etPackageNumber.requestFocus();
                                etPackageNumber.setText("");
                                etFBANumber.setText("");
                            }
                        }
                    }catch (Exception e){
                        showDialog("系统异常",e.getMessage());
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
    private void showDialogAndShock(String title,String content,boolean isShock){
        new MaterialDialog.Builder(BindFBAActivity.this)
                .positiveText("确定")
                .title(title)
                .content(content)
                .cancelable(false)
                .show();
        if(isShock)
            VibratorHelper.shock(BindFBAActivity.this);
    }
    private void showDialog(String title,String content){
        showDialogAndShock(title,content,true);
    }
}
