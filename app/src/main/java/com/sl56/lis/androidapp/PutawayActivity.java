package com.sl56.lis.androidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PutawayActivity extends AppCompatActivity {
    //region 变量声明
    private EditText etCargoContainer;
    private EditText etReceiveGoodsDetailNo;
    private TextView tvCargoContainer;
    private TextView tvResult;
    private static final String[] m={"正常","转移"};
    private MaterialSpinner actionTypeSpinner;

    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_putaway);

        //region 工具栏设置
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        //endregion

        //region 视图控件初始化
        etCargoContainer = (EditText) findViewById(R.id.etCargoContainer);
        etReceiveGoodsDetailNo = (EditText) findViewById(R.id.etReceiveGoodsDetailNo);
        tvCargoContainer = (TextView) findViewById(R.id.tvCargoContainer);
        tvResult = (TextView) findViewById(R.id.tvResult);

        actionTypeSpinner = (MaterialSpinner) findViewById(R.id.spActionType);
        //endregion

        etCargoContainer.setOnKeyListener(CargoContainerOnKeyListener);
        etReceiveGoodsDetailNo.setOnKeyListener(ReceiveGoodsDetailOnKeyListener);

        actionTypeSpinner.setItems(m);
    }
    private View.OnKeyListener ReceiveGoodsDetailOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                final String cargoContainerNumber = etReceiveGoodsDetailNo.getText().toString();
                Object tag=etCargoContainer.getTag();
                final int actionType=actionTypeSpinner.getSelectedIndex();
                if(tag==null)
                {
                    etReceiveGoodsDetailNo.setText("");
                    showDialog("错误","请先扫描容器条码");
                    etCargoContainer.requestFocus();
                    return false;
                }
                final int cargoContainerId=(int)tag;
                tvResult.setText("");
                Observable.create(new Observable.OnSubscribe<JSONObject>() {
                    @Override
                    public void call(Subscriber<? super JSONObject> subscriber) {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("cargoContainerId", cargoContainerId);
                            params.put("referenceNumber", cargoContainerNumber);
                            params.put("actionType", actionType);
                            params.put("header", Global.getHeader());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext(HttpHelper.getJSONObjectFromUrl("BindingCargoToContainer", params));
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<JSONObject>() {
                            @Override
                            public void call(JSONObject jsonObject) {
                                try {
                                    boolean result = jsonObject.getBoolean("Success");
                                    if (result) {
                                        tvResult.setText("操作已成功");
                                        etReceiveGoodsDetailNo.setText("");
                                        etReceiveGoodsDetailNo.requestFocus();

                                    } else {
                                        String message = jsonObject.getString("Message");
                                        showDialog("操作失败", message);
                                        etReceiveGoodsDetailNo.selectAll();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                return true;
            }
            return false;
        }
    };
    private View.OnKeyListener CargoContainerOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                final String cargoContainerNumber = etCargoContainer.getText().toString();
                if(cargoContainerNumber==null|| cargoContainerNumber==""){
                    showDialog("错误","容器编码为空，请重试");
                    return false;
                }
                Observable.create(new Observable.OnSubscribe<JSONObject>() {
                    @Override
                    public void call(Subscriber<? super JSONObject> subscriber) {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("cargoContainerNo", cargoContainerNumber);
                            params.put("header", Global.getHeader());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext(HttpHelper.getJSONObjectFromUrl("ScanCargoContainer", params));
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<JSONObject>() {
                            @Override
                            public void call(JSONObject jsonObject) {
                                try {
                                    boolean result = jsonObject.getBoolean("Success");
                                    if (result) {
                                        tvCargoContainer.setText("当前容器:" + etCargoContainer.getText());
                                        etCargoContainer.setText("");
                                        etReceiveGoodsDetailNo.requestFocus();
                                        etCargoContainer.setTag(jsonObject.getInt("CargoContainerId"));
                                    } else {
                                        String message = jsonObject.getString("Message");
                                        showDialog("操作失败", message);
                                        etCargoContainer.selectAll();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                return true;
            }
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(String title, String content) {
        new MaterialDialog.Builder(PutawayActivity.this)
                .positiveText("确定")
                .title(title)
                .content(content)
                .cancelable(false)
                .show();
        VibratorHelper.shock(PutawayActivity.this);
    }
}