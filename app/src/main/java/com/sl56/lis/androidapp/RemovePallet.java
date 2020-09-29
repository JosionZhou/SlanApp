package com.sl56.lis.androidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RemovePallet extends AppCompatActivity {
    private ScannerInterface scanner;
    private EditText etNumber;
    private RadioGroup radioGroup;
    private Button doRemoveButton;
    private ArrayList<Boolean> radioButtonValue=new ArrayList<Boolean>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_pallet);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        etNumber = (EditText) findViewById(R.id.etNumber);
        radioGroup=(RadioGroup) findViewById(R.id.rgIsCharge);
        doRemoveButton=(Button) findViewById(R.id.btnRemove);
        etNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    String refNumber = etNumber.getText().toString();
                    if (refNumber.length() == 16 && refNumber.lastIndexOf("0430") == 12) {
                        refNumber = refNumber.substring(0, 12);
                    }
                    etNumber.setText(refNumber);
                    etNumber.selectAll();
                    return true;
                }
                return false;
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton=(RadioButton) RemovePallet.this.findViewById(radioGroup.getCheckedRadioButtonId());
                radioButtonValue.clear();
                if(radioButton!=null){
                    CharSequence text=radioButton.getText();
                    if(text.equals("是")) {
                        radioButtonValue.add(true);
                    }
                    else{
                        radioButtonValue.add(false);
                    }
                }



            }
        });
        doRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRemovePallet();
            }
        });


    }



    private void doRemovePallet() {
        doRemoveButton.setEnabled(false);
        String refNumber = etNumber.getText().toString();
        final String exeNumber;
        if (refNumber.isEmpty()) {
            showDialog("提示", "单号不能为空");
            etNumber.setFocusable(true);
            doRemoveButton.setEnabled(true);
            return;
        }
        if(radioButtonValue.size()!=1){
            showDialog("提示", "请选择是否收取拆板费用");
            doRemoveButton.setEnabled(true);
            return;
        }
        //联邦单号做特殊处理
        if (refNumber.length() == 16 && refNumber.lastIndexOf("0430") == 12) {
            refNumber = refNumber.substring(0, 12);
        }
        exeNumber = refNumber;
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("number", exeNumber);
                    params.put("isCharge", radioButtonValue.get(0));
                    params.put("header", Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("RemovePallet", params));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JSONObject>() {
                    @Override
                    public void call(JSONObject jsonObject) {
                        if (jsonObject != null && !jsonObject.toString().trim().isEmpty()) {
                            try {
                                if (jsonObject.toString().contains("Error")) {
                                    String errorMsg = jsonObject.getString("Error");
                                    showDialog("网络访问异常", errorMsg);
                                } else {
                                    Boolean isSuccess = jsonObject.getBoolean("Success");
                                    if (!isSuccess) {
                                        String errorMsg = jsonObject.getString("Message");
                                        showDialog("操作失败", errorMsg);
                                    } else {
                                        etNumber.setText("");
                                        radioButtonValue.clear();
                                        radioGroup.clearCheck();
                                        showDialog("提示", "拆板已成功");
                                    }
                                }
                            } catch (Exception e) {
                                showDialog("系统异常", e.getMessage());
                            }
                        }
                        etNumber.selectAll();
                        doRemoveButton.setEnabled(true);
                    }
                });
    }

    private void showDialog(String title, String content) {
        new MaterialDialog.Builder(RemovePallet.this)
                .positiveText("确定")
                .title(title)
                .content(content)
                .cancelable(false)
                .show();
        VibratorHelper.shock(RemovePallet.this);
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
}