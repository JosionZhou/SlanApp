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

import java.io.IOException;
import java.lang.reflect.Field;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ScanRGDActivity extends AppCompatActivity {

    private EditText etInnerPackageNumber;
    private TextView tvClearanceInfo;
    private MaterialDialog pDialog;
    public final OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_rgd);
        tvClearanceInfo = (TextView)findViewById(R.id.tv_clearanceinfo);
        etInnerPackageNumber = (EditText)findViewById(R.id.et_innerPackageNumber);
        etInnerPackageNumber.setOnKeyListener(new View.OnKeyListener() {
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
     * 收货扫描
     */
    private void ScanRgd(){
        final String number = etInnerPackageNumber.getText().toString();
        if(number.isEmpty()){
            showAlertMessage("提示","单号不能为空");
            return;
        }
        pDialog = new MaterialDialog.Builder(this)
                .content("数据查询中...")
                .cancelable(false)
                .progress(true,0)
                .show();
        String urlString = "https://api.sl56.com/api/ReceiveGoodsDetailPrePrint/IsGoodsReceived?number="+number;
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 请求失败处理
                pDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 请求成功处理
                pDialog.dismiss();
                String responseData = response.body().string();
                if(responseData.equals("false")){
                    showAlertMessage("提示","未收货");
                }
                else if(responseData.equals("true")){
                    showAlertMessage("提示","已收货，可以过机测量");
                }
                else{
                    showAlertMessage("操作失败",responseData);
                }
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
                new AlertDialog.Builder(ScanRGDActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                etInnerPackageNumber.setText("");
                            }
                        })
                        .show();
                VibratorHelper.shock(ScanRGDActivity.this);
            }
        });
    }
}
