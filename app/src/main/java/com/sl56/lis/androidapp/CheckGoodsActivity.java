package com.sl56.lis.androidapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class CheckGoodsActivity extends AppCompatActivity {
    private EditText etReferencenumber;
    private TextView tvPriceName;
    private TextView tvCheckInfo;
    private MaterialDialog dialog;
    private SegmentTabLayout tabLayout;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<JSONArray> arrayList = new ArrayList<>();
    private final String[] tabTitles = {"报价规则", "其他规则", "问题", "备注"};
    private Button btnCheckGoods;
    private Button btnSave;
    private int receiveGoodsDetailId = 0;//收货Id
    private JSONArray priceRules;//查货返回的报价规则
    private JSONArray otherRules;//查货返回的其他规则
    private JSONArray problems;//查货返回的问题
    private String InspectionTips;//查货提示
    private Integer cellQuantity;//电池数
    private Boolean isChecked;//是否已经查货
    private int piece;//件数
    private SetPriceNameHandler handler = new SetPriceNameHandler();
    private String lastChanged;
    Subscriber<JSONObject> subscriber;
    Subscriber<Boolean> subscriberFailDialog;
    private Button btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_check_goods);
        //设置音频通道为多媒体，可以通过手机音量+/-键调节音量
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        tabLayout = (SegmentTabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabData(tabTitles);//设置选项卡标题
        tvPriceName = (TextView) findViewById(R.id.tv_pricename);
        tvCheckInfo = (TextView) findViewById(R.id.tv_checkinfo);
        etReferencenumber = (EditText) findViewById(R.id.etreferencenumber);
        //设置输入框弹出键盘默认为数字键盘
//        String digists = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        etReferencenumber.setKeyListener(DigitsKeyListener.getInstance(digists));
        //监听键盘回车键事件
        etReferencenumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    checkGoodsScan();
                    return true;
                }
                return false;
            }
        });
        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCheckGoods();
            }
        });
        btnCheckGoods = (Button) findViewById(R.id.btn_checkgoods);
        btnCheckGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGoodsScan();
            }
        });
        btnCamera=(Button) findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+File.separator+"camera").getAbsoluteFile();
                deleteDirWihtFile(dcimDir);
                if (receiveGoodsDetailId == 0) {
                    dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .title("请先查货")
                            .content("先查货再拍照上传")
                            .positiveText("确定")
                            .show();
                }
                else{
                    dispatchTakePictureIntent();
                }


            }
        });
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
    }


    private void dispatchTakePictureIntent() {
        String fileName = getPhotoFileName() + ".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        currentPhotoPath=path+fileName;
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.sl56.lis.androidapp.fileprovider",
                new File(currentPhotoPath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, 1);
    }

    String currentPhotoPath="";

    public void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete();
            else if (file.isDirectory())
                deleteDirWihtFile(file);
        }
        dir.delete();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }


        try {
            final String base64 = encodeBase64File(currentPhotoPath);
            Observable.create(new Observable.OnSubscribe<JSONObject>() {
                        @Override
                        public void call(Subscriber<? super JSONObject> subscriber) {
                            try {
                                JSONObject jsonParams = new JSONObject();
                                jsonParams.put("Name", currentPhotoPath);
                                jsonParams.put("Content", base64);
                                jsonParams.put("ReceiveGoodsDetailId",receiveGoodsDetailId);
                                jsonParams.put("CreateBy",Global.getHeader().getInt("UserId"));
                                //jsonParams.put("Content", "aasdfasdf");
                                subscriber.onNext(HttpHelper.getJSONObjectFromUrl1("InspectionUpload", jsonParams));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<JSONObject>() {
                        @Override
                        public void call(JSONObject jsonObject) {
                            boolean success= false;
                            String message="";
                            try {
                                success = jsonObject.getBoolean("Success");
                                message=jsonObject.getString("Message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(success==false) {
                                etReferencenumber.requestFocus();
                                new MaterialDialog.Builder(CheckGoodsActivity.this)
                                        .title("失败")
                                        .content("上传失败，请再次拍照重试,如果多次失败请联系系统管理员.错误信息:"+message)
                                        .positiveText("确定")
                                        .show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String path = Environment.getExternalStorageDirectory() +
            File.separator + Environment.DIRECTORY_DCIM + File.separator+"camera"+File.separator;
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "IMG_" + dateFormat.format(date);
    }
    public  String encodeBase64File(String path) throws Exception {
        File  file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        file.delete();
        return Base64.encodeToString(buffer,Base64.NO_WRAP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.groupmember:
                Intent intent2 = new Intent(CheckGoodsActivity.this, StationMemberSettingActivity.class);
                CheckGoodsActivity.this.startActivity(intent2);
                break;
            case R.id.hscode:
                if (receiveGoodsDetailId == 0) {
                    dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .title("请先查货")
                            .content("先查货再添加海关编码")
                            .positiveText("确定")
                            .show();
                    return false;
                }
                Intent intent3 = new Intent(CheckGoodsActivity.this, HSCodeListActivity.class);
                intent3.putExtra("receiveGoodsDetailId", receiveGoodsDetailId);
                CheckGoodsActivity.this.startActivity(intent3);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checkgoodsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void saveCheckGoods1() throws JSONException {
        final JSONObject jsonParams = new JSONObject();
        jsonParams.put("receiveGoodsDetailId", receiveGoodsDetailId);
        jsonParams.put("header", Global.getHeader());
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject result = HttpHelper.getJSONObjectFromUrl("InspectionSave1", jsonParams);
                subscriber.onNext(result);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        String msg = e.getMessage();
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            int code = jsonObject.getInt("Result");
                            if(code==1){
                                Message msg = handler.obtainMessage();
                                msg.arg1 = -1;
                                msg.arg2 = piece;
                                msg.sendToTarget();
                                ViewPager vpMaind = (ViewPager) findViewById(R.id.vpMain);
                                //保存查货后隐藏选项卡，避免误操作
                                vpMaind.setVisibility(View.GONE);
                            }
                            else {
                                etReferencenumber.requestFocus();
                                new MaterialDialog.Builder(CheckGoodsActivity.this)
                                        .title("保存失败")
                                        .content(jsonObject.getString("ErrorMessage"))
                                        .positiveText("确定")
                                        .show();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 保存查货
     */
    private void saveCheckGoods() {
        if (receiveGoodsDetailId == 0) {
            VibratorHelper.shock(CheckGoodsActivity.this);
            etReferencenumber.requestFocus();
            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                    .title("保存失败")
                    .content("请扫描单号成功后再保存")
                    .positiveText("确定")
                    .show();
            return;
        }
        JSONArray rules = new JSONArray();
        JSONArray changeProblems = new JSONArray();
        for (int i = 0; i < 3; i++) {
            ScrollViewFragment svf = (ScrollViewFragment) mFragments.get(i);
            //判断对应的选项卡是否被创建，没有创建的选项卡代表数据没有更改
            if (svf.getIsCreated()) {
                int checkBoxCount = svf.getCheckboxSourceArray().length();
                for (int j = 0; j < checkBoxCount; j++) {
                    try {
                        JSONObject tagObj = ((ScrollViewFragment) mFragments.get(i)).getCheckboxSourceArray().getJSONObject(j);
                        Boolean isCurrentCheck;
                        if (tagObj.isNull("IsChecked"))
                            isCurrentCheck = tagObj.getBoolean("NewIsChecked");
                        else
                            isCurrentCheck = tagObj.getBoolean("IsChecked");
                        //判断保存时复选框的状态和加载时是否一样
                        JSONObject json = null;
                        switch (i) {
                            case 0:
                                json = new JSONObject(tagObj.toString());
                                json.put("NewIsChecked", isCurrentCheck);
                                rules.put(json);
                                break;
                            case 1:
                                json = new JSONObject(tagObj.toString());
                                json.put("NewIsChecked", isCurrentCheck);
                                rules.put(json);
                                break;
                            case 2:
                                json = new JSONObject(tagObj.toString());
                                json.put("NewIsChecked", isCurrentCheck);
                                changeProblems.put(json);
                                break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {

                switch (i) {
                    case 0:
                        rules.put(priceRules);
                        break;
                    case 1:
                        rules.put(otherRules);
                        break;
                    case 2:
                        changeProblems.put(problems);
                        break;
                }
            }
        }
        final JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("receiveGoodsDetailId", receiveGoodsDetailId);
            jsonParams.put("rules", rules);
            jsonParams.put("problems", changeProblems);
            jsonParams.put("remark", ((ScrollViewFragment) mFragments.get(3)).getRemarkText());
            jsonParams.put("header", Global.getHeader());
            jsonParams.put("lastChanged", lastChanged);
            //把第一个选项卡和第二个选项卡的电池数相加
            //为了防止出现勾选了电池型号，但是电池数为0的问题（之前逻辑是取消勾选后将电池数直接设置为0，但是如果存在勾选了多个电池型号的情况下，将会出现勾选了电池型号，而电池数为0）
            int currentCellQuantity = (((ScrollViewFragment) mFragments.get(0)).getCellQuantity() + ((ScrollViewFragment) mFragments.get(1)).getCellQuantity());
            jsonParams.put("cellQuantity", currentCellQuantity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                .content("正在保存数据...")
                .progress(true, 0)
                .dismissListener(new DialogInterface.OnDismissListener() {//此对话框消失时
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        subscriber.unsubscribe();//取消订阅查货保存事件
                        subscriberFailDialog.unsubscribe();//取消订阅保存超时处理事件
                    }
                })
                .show();

        //查货保存网络访问(RXJava方式)
        Observable exeObj = Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject result = HttpHelper.getJSONObjectFromUrl("InspectionSave", jsonParams);
                subscriber.onNext(result);
            }
        });
        subscriber = new Subscriber<JSONObject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                VibratorHelper.shock(CheckGoodsActivity.this);
                etReferencenumber.requestFocus();
                new MaterialDialog.Builder(CheckGoodsActivity.this)
                        .title("保存操作失败")
                        .content(e.getMessage())
                        .positiveText("确定")
                        .show();
            }

            @Override
            public void onNext(JSONObject result) {
                int code = 0;
                try {
                    dialog.dismiss();
                    code = result.getInt("Result");
                    if (code == 0) {
                        String errorMessage = result.getString("ErrorMessage");
                        if (errorMessage.equals("当前无适合的成本价")) {
                            etReferencenumber.requestFocus();
                            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                                    .content("当前无适合的成本价，是否强制保存？")
                                    .cancelable(false)
                                    .positiveText("是")
                                    .negativeText("否")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            try {
                                                saveCheckGoods1();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .show();
                        } else
                            throw new Exception(result.getString("ErrorMessage"));
                    } else if (code == -1) {
                        throw new Exception(result.getString("ErrorMessage") + ".系统已刷新内价");
                    } else if (code == 1) {
                        Message msg = handler.obtainMessage();
                        msg.arg1 = -1;
                        msg.arg2 = piece;
                        msg.sendToTarget();
                        ViewPager vpMaind = (ViewPager) findViewById(R.id.vpMain);
                        //保存查货后隐藏选项卡，避免误操作
                        vpMaind.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    VibratorHelper.shock(CheckGoodsActivity.this);
                    etReferencenumber.requestFocus();
                    new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .title("提示")
                            .content(e.getMessage())
                            .positiveText("确定")
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (dialog.getContentView().getText().toString().contains("刷新内价")) {
                                        checkGoodsScan();
                                    }
                                }
                            })
                            .show();
                }
            }
        };
        exeObj.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
        subscriberFailDialog = new Subscriber<Boolean>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean isShowing) {
                if (isShowing) {
                    VibratorHelper.shock(CheckGoodsActivity.this);
                    dialog.dismiss();
                    dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .content("操作失败，是否重新获取数据？")
                            .cancelable(false)
                            .positiveText("是")
                            .negativeText("否")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    saveCheckGoods();
                                }
                            })
                            .show();
                }
            }
        };
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    Thread.sleep(10 * 1000);//休眠10秒
                    subscriber.onNext(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriberFailDialog);

//        task = new CheckGoodsTask("InspectionSave",jsonParams,1);
//        task.executeOnExecutor(cachedThreadPool);
    }

    /**
     * 查货扫描
     */
    private void checkGoodsScan() {
        etReferencenumber.selectAll();
        String referenceNumber = ((EditText) findViewById(R.id.etreferencenumber)).getText().toString();
        if (referenceNumber.trim().isEmpty()) {
            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                    .title("请输入单号")
                    .content("单号不能为空")
                    .positiveText("确定")
                    .show();
            VibratorHelper.shock(this);
        } else {
            final JSONObject params = new JSONObject();
            try {
                params.put("referenceNumber", referenceNumber);
                params.put("header", Global.getHeader());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                    .content("正在获取数据...")
                    .progress(true, 0)
                    .cancelable(true)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            subscriber.unsubscribe();
                            subscriberFailDialog.unsubscribe();
                        }
                    })
                    .show();
            subscriberFailDialog = new Subscriber<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                    VibratorHelper.shock(CheckGoodsActivity.this);
                    dialog.dismiss();
                    etReferencenumber.requestFocus();
                    dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .content("数据获取失败，是否重新获取？")
                            .cancelable(false)
                            .positiveText("是")
                            .negativeText("否")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    checkGoodsScan();
                                }
                            })
                            .show();
                }
            };
            //当获取数据的dialog显示时间超过10秒是，认为提交数据失败
            Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    try {
                        Thread.sleep(1 * 1000);
                        subscriber.onNext(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();

            //查货扫描网络访问(RXJava方式)
            Observable exeObj = Observable.create(new Observable.OnSubscribe<JSONObject>() {
                @Override
                public void call(Subscriber<? super JSONObject> subscriber) {
                    JSONObject result = HttpHelper.getJSONObjectFromUrl("InspectionScan", params);
                    if (result == null) {
                        subscriber.onError(new Exception("网络访问异常"));
                    }
                    subscriber.onNext(result);
                }
            });
            subscriber = new Subscriber<JSONObject>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    dialog.dismiss();
                    etReferencenumber.requestFocus();
                    new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .title("查货扫描失败")
                            .content(e.getMessage())
                            .positiveText("确定")
                            .show();
                }

                @Override
                public void onNext(JSONObject result) {
                    dialog.dismiss();
                    try {
                        Boolean isSuccess = result.getBoolean("Success");
                        if (!isSuccess) {
                            String alertMsg = result.getString("ErrorMessage");
                            throw new Exception(alertMsg);
                        }
                        lastChanged = result.getString("LastChanged");
                        priceRules = result.getJSONArray("PriceRules");
                        otherRules = result.getJSONArray("OhterRules");
                        problems = result.getJSONArray("Problems");
                        JSONArray remark = new JSONArray();
                        remark.put(result.getString("Remark"));
                        arrayList.clear();
                        arrayList.add(priceRules);//添加报价规则到第一个选项卡
                        arrayList.add(otherRules);//添加其他报价规则到第二个选项卡
                        arrayList.add(problems);//添加问题到第三个选项卡
                        arrayList.add(remark);//填写备注的选项卡显示已存在的备注内容
                        InspectionTips = result.getString("InspectionTips");
                        receiveGoodsDetailId = result.getInt("ReceiveGoodsDetailId");
                        cellQuantity = result.getInt("CellQuantity");
                        piece = result.getInt("Piece");
                        Message msg = handler.obtainMessage();
                        msg.obj = result.getString("PriceName");
                        msg.arg1 = result.getBoolean("IsInspection") ? 1 : 0;
                        msg.arg2 = piece;
                        msg.sendToTarget();
                        setTabAdapter(0);
                        etReferencenumber.selectAll();//全选输入框文本
                        ViewPager vpMaind = (ViewPager) findViewById(R.id.vpMain);
                        //查货扫描时显示选项卡
                        vpMaind.setVisibility(View.VISIBLE);
                        if (!InspectionTips.isEmpty()) {
                            etReferencenumber.requestFocus();
                            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                                    .title("查货提示")
                                    .content(InspectionTips)
                                    .positiveText("确定")
                                    .show();
                            VibratorHelper.shock(CheckGoodsActivity.this);
                        }
                    } catch (Exception e) {
                        etReferencenumber.requestFocus();
                        new MaterialDialog.Builder(CheckGoodsActivity.this)
                                .title("查货扫描失败")
                                .content(e.getMessage())
                                .positiveText("确定")
                                .show();
                    }
                }
            };
            exeObj.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
//            task=new CheckGoodsTask("InspectionScan",params,0);
//            task.executeOnExecutor(cachedThreadPool);
        }
    }

    /**
     * 设置选项卡的适配器
     *
     * @param type 0:查货扫描 1:查货保存
     */
    private void setTabAdapter(int type) {
        FragmentManager fm = getSupportFragmentManager();
        MyPagerAdapter mpa = new MyPagerAdapter(fm, mFragments);
        if (type == 0) {//若是查货扫描，则重新生成CheckBox
            ArrayList<Fragment> newFragments = new ArrayList<>();
            for (JSONArray ja : arrayList) {
                newFragments.add(new ScrollViewFragment(ja, arrayList.indexOf(ja), receiveGoodsDetailId, cellQuantity, CheckGoodsActivity.this));
            }
            mpa.setFragments(newFragments);
            mFragments.clear();
            mFragments = newFragments;
        }
        final ViewPager vpMain = (ViewPager) findViewById(R.id.vpMain);
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        vpMain.setAdapter(mpa);

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

        public MyPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fm = fm;
            this.fragments = fragments;
        }

        /**
         * FragmentManager会缓存Fragment，在更新Fragment前，要移除旧的Fragment，再更新fragmentlist
         *
         * @param fragments 更新后的fragmentlist
         */
        public void setFragments(ArrayList fragments) {
            if (this.fragments != null) {
                FragmentTransaction ft = fm.beginTransaction();
                for (Fragment f : this.fragments) {
                    ft.remove(f);
                }
                ft.commit();
                ft = null;
                fm.executePendingTransactions();
            }
            this.fragments = fragments;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    private class SetPriceNameHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null)
                tvPriceName.setText(msg.obj.toString());
            tvCheckInfo.setText("共 " + msg.arg2 + " 件； " + "是否查货：" + (msg.arg1 == 0 ? "否" : "是"));
            isChecked = !(msg.arg1 == 0);
            //保存后清空单号信息
            if (msg.arg1 == -1) {
                tvPriceName.setText("");
                tvCheckInfo.setText("");
                etReferencenumber.setText("");
                receiveGoodsDetailId = 0;
            }
        }
    }
}
