package com.sl56.lis.androidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bigmercu.cBox.CheckBox;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class PalletActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private String errorMsg;
    private MaterialSpinner palletCategoriesspinner;
    private MaterialSpinner palletnospinner;
    private EditText etBarCode;
    private int palletId=0;
    private String palletNo;
    private Integer bindStationId = null;
    private Integer tempBindStationId=null;
    private Integer palletCategoryId=null;
    private ArrayList<String> shipments = new ArrayList<>();//已扫描的单号列表
    private ArrayList<Map.Entry<String,Integer>> palletCategories = new ArrayList<>();
    private Map<String,Integer> bindingStationList = new Hashtable<>();
    private DBHelper dbHelper = new DBHelper(this);
    //private MaterialDialog progressDialog;
    private List<PalletInfo> palletInfoList = new ArrayList<>();
    private List<String> palletNoList=new ArrayList<>() ;
    private ListView lvShipments;
    private CheckBox cbCustoms;
    private CheckBox cbBattery;
    private ScannerInterface scanner;
    BroadcastReceiver scanReceiver;
    private DatePickerDialog dpd;
    private TextView tvDate;

    private static final String RES_ACTION = "android.intent.action.SCANRESULT";//****重要
   // private boolean isProgressDialogShowing=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pallet);
        tvDate = (TextView)findViewById(R.id.tv_date);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String dateStr =sdf.format(new Date());
        tvDate.setText(dateStr);
        cbCustoms = (CheckBox)findViewById(R.id.cb_customs);
        cbBattery =(CheckBox)findViewById(R.id.cb_battery);
        lvShipments = (ListView)findViewById(R.id.lv_shipments);
        etBarCode = (EditText)findViewById(R.id.etBarCode);
        etBarCode.selectAll();
        /*etBarCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
              if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_F9&&keyEvent.getAction() == KeyEvent.ACTION_UP){
                    if( etBarCode.isEnabled()) {
                        Log.e("a","输入框可用");

                        scanner.lockScanKey();//锁定扫描键
                    }
                    else {
                        Log.e("a","输入框不可用");
                    }
                    return true;
                }
                else{
                  Log.e("a","不是扫描键");
              }
                return false;
            }
        });*/
;
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
            /*
            It is recommended to always create a new instance whenever you need to show a Dialog.
            The sample app is reusing them because it is useful when looking for regressions
            during testing
             */
                if (dpd == null) {
                    dpd = DatePickerDialog.newInstance(
                            PalletActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                } else {
                    dpd.initialize(
                            PalletActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                }
                dpd.show( PalletActivity.this.getFragmentManager(), "Datepickerdialog");
            }
        });
        palletnospinner = (MaterialSpinner)findViewById(R.id.spinner_palletno);
        palletnospinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                setPalletDetails(position);
            }
        });
        palletCategoriesspinner = (MaterialSpinner) findViewById(R.id.spinner_palletcategory);
        palletCategoriesspinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                palletCategoryId = palletCategories.get(position).getValue();
            }
        });
        try {
            getPalletCagetories();
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        forceShowOverflowMenu();
        BindPalletNoList();
        initScanner();
    }
    private  void initScanner()
    {
        scanner=new ScannerInterface(this);
        //scanner.open();
        //scanner.resultScan();
        scanner.setOutputMode(1);
        scanner.enableFailurePlayBeep(true);//扫描失败蜂鸣反馈  ***测试扫描失败反馈接口，解码失败会出现错误提示
    }
    protected  void onResume()
    {
        super.onResume();
        scanReceiver = new ScannerResultReceiver();
        IntentFilter intentFilter = new IntentFilter(RES_ACTION);
        registerReceiver(scanReceiver, intentFilter);
    }
    @Override
    protected void onPause() {//��onPause��ע��㲥������
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(scanReceiver);
        //PDA内存是有限的，一款优秀的APP懂得在适当的时期释放自己所占的资源，如果不会收一般有两种结果，
        //一种是系统强行回收，二是内存溢出。
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
       // scanner.resultScan();//重置串口，恢复iScan默认设置。一般在退出整个程序时调用一次。
        //scanner.close();
    }

    /**
     * 根据选择的板代码获取当前板的明细
     * @param position 板代码在列表中的索引位置
     */
    private void setPalletDetails(int position){
        final PalletInfo selectObj = palletInfoList.get(position);
        if(selectObj!=null && selectObj.Id!=-1){
            palletId=selectObj.Id;
            palletNo=selectObj.No;
            cbBattery.setEnabled(false);
            cbCustoms.setEnabled(false);
            cbBattery.setChecked(selectObj.IsBattery);
            cbCustoms.setChecked(selectObj.IsCustoms);
            palletCategoriesspinner.setEnabled(false);
            Observable.create(new Observable.OnSubscribe<JSONObject>() {
                @Override
                public void call(Subscriber<? super JSONObject> subscriber) {
                    JSONObject params = new JSONObject();
                    try {
                        params.put("palletId",palletId);
                        params.put("header",Global.getHeader());
                        subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPalletDetilInfoList",params));
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
                            try {
                                shipments.clear();
                                JSONArray array = jsonObject.getJSONArray("Details");
                                shipments = new ArrayList<String>();
                                for(int i =0;i<array.length();i++){
                                    shipments.add(array.getJSONObject(i).getString("Value"));
                                }
                                reBindShipmentsListView();
                                for(Map.Entry<String,Integer> entry:palletCategories){
                                    if(entry.getValue()==selectObj.CategoryId){
                                        palletCategoriesspinner.setSelectedIndex(palletCategories.indexOf(entry));
                                    }
                                }
                                etBarCode.selectAll();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
            ;
        }else{
            //清空ListView
            lvShipments.setAdapter(null);
        }
    }

    /**
     * 刷新当前板下的单号列表
     */
    private void reBindShipmentsListView(){
        ArrayAdapter ad = new ArrayAdapter(PalletActivity.this,android.R.layout.simple_list_item_1,shipments);
        lvShipments.setAdapter(ad);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.add:
                palletId=0;
                palletCategoriesspinner.setEnabled(true);
                AddPallte(0,"待生成",0,false,false,true);
                if(palletNoList.size()>0){
                    palletnospinner.setItems(palletNoList);
                    palletnospinner.setSelectedIndex(0);
                }
                shipments.clear();
                cbBattery.setEnabled(true);
                cbCustoms.setEnabled(true);
                reBindShipmentsListView();
                break;
            case R.id.groupmember:
                Intent intent2 = new Intent(PalletActivity.this, StationMemberSettingActivity.class);
                PalletActivity.this.startActivity(intent2);
                break;
            case R.id.stationbinding:
                showStationBindingDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //弹出绑定岗位对话框
    public void showStationBindingDialog(){
        bindingStationList.clear();
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("header", Global.getHeader());
                    params.put("parentId", null);
                }catch(Exception e){
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPostGrouplnfoList",params));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<JSONObject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(JSONObject jsonObject) {
                try {
                    if(!jsonObject.getString("Message").isEmpty()) {
                        VibratorHelper.shock(PalletActivity.this);
                        new MaterialDialog.Builder(PalletActivity.this)
                                .positiveText("确定")
                                .title("操作失败")
                                .content(jsonObject.getString("Message"))
                                .cancelable(false)
                                .show();
                        return;
                    }
                    JSONArray array = jsonObject.getJSONArray("PostGroupms");
                    for(int i =0;i<array.length();i++){
                        bindingStationList.put(array.getJSONObject(i).getString("PostGroupName"),array.getJSONObject(i).getInt("PostGroupId"));
                    }
                    View view = getLayoutInflater().inflate(R.layout.settingstationbinds,null);
                    MaterialSpinner spinner = (MaterialSpinner)view.findViewById(R.id.stationSpinner);
                    spinner.setItems(bindingStationList.keySet().toArray());
                    if(bindStationId!=null) {
                        Integer[] ids = bindingStationList.values().toArray(new Integer[0]);
                        for(int i=0;i<ids.length;i++)
                        {
                            if(ids[i]==bindStationId)
                                spinner.setSelectedIndex(i);
                        }
                    }
                    spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                            tempBindStationId = (Integer) bindingStationList.values().toArray()[position];
                        }
                    });
                    TextView tv = (TextView)view.findViewById(R.id.tv_bindingremark);
                    tv.setText("扫描枪绑定对应岗位后，将在对应岗位的电脑上播放扫描结果语音提示");
                    new MaterialDialog.Builder(PalletActivity.this)
                            .title("绑定岗位")
                            .customView(view,false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    bindStationId=tempBindStationId;
                                    try {
                                        setStationId();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //创建右上角菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.barmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void init() throws Exception{
        //从数据库获取绑定的岗位Id
        Cursor result = dbHelper.Fetch("StationId");
        if(result.getCount()>0){
            result.moveToFirst();
            bindStationId = Integer.parseInt(result.getString(1));

        }
        cbCustoms.setText("");
        cbBattery.setText("");
    }
    //把stationId存入数据库
    private void setStationId() throws Exception{

        Cursor result = dbHelper.Fetch("StationId");
        if(result.getCount()>0)
            dbHelper.Update("StationId",bindStationId.toString());
        else
            dbHelper.Inert("StationId",bindStationId.toString());
    }

    /**移除板
     * 板ID
     * @param id
     */
    private void RemovePallet(int id)
    {
        PalletInfo pi = null;
        for(PalletInfo item : palletInfoList){
            if(item.Id==id)pi=item;
        }
        if (pi!=null)
        {
            palletNoList.remove(pi.No);
            palletInfoList.remove(pi);



        }

    }

    /**
     * 添加板
     * @param id 板ID
     * @param value 板No
     * @param palletCategoryId 板类别ID
     * @param isCustomer 是否单独报关
     * @param isBattery 是否电池板
     */
    private void AddPallte(int id, String value,int palletCategoryId,Boolean isCustomer,Boolean isBattery,boolean isInsert) {
        if(!palletNoList.contains(value)){
            PalletInfo newItem = new PalletInfo();
            newItem.Id = id;
            newItem.No = value;
            newItem.CategoryId = palletCategoryId;
            newItem.IsBattery = isBattery;
            newItem.IsCustoms = isCustomer;
            if(isInsert){
                palletInfoList.add(0,newItem);
                palletNoList.add(0,value);
            }
            else{
                palletInfoList.add(newItem);
                palletNoList.add(value);
            }
        }


            /*List<String> palletNoList = new ArrayList<>();
            for(PalletInfo pi:palletInfoList)
                palletNoList.add(0,pi.No);
            palletnospinner.setItems(palletNoList);
            palletnospinner.setSelectedIndex(palletInfoList.indexOf(newItem));*/


//        cbPalletNoList.SelectedValue = id;
    }
    private void pallet(){
        etBarCode.selectAll();
        String alertMsg;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title("扫描失败").positiveText("确定");
        final String temp = etBarCode.getText().toString().trim();
        String no ="";

        if(bindStationId==null){
            VibratorHelper.shock(this);
            builder.content("请设置语音提示岗位")
                    .positiveText("确定")
                    .canceledOnTouchOutside(false)//点击外部不取消对话框
                    .onAny(new MaterialDialog.SingleButtonCallback(){
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                enableScanner();
                                etBarCode.setFocusable(true);
                                etBarCode.selectAll();
                            }
                        }
                    })
                    .show();

            return;
        }
        switch (temp.length()){
            case 34://FedEx
                no=temp.substring(22);
                break;
            case 21://DHL
                no=temp;
                break;
            case 18:
                no=temp;
                break;
            case 11://Aramex
                no=temp;
                break;
            default:
                VibratorHelper.shock(this);
                new  MaterialDialog.Builder(this)
                        .title("错误")
                        .content("扫描的单号异常")
                        .canceledOnTouchOutside(false)//点击外部不取消对话框
                        .positiveText("确定")
                        .onAny(new MaterialDialog.SingleButtonCallback(){
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    enableScanner();
                                    etBarCode.setFocusable(true);
                                    etBarCode.selectAll();
                                }
                            }
                        })
                        .show();

                return;
        }
        etBarCode.setText(no);
        //Aramex 多件是同一个单号，不做检查
        if (no.length() != 11 && Find(no))
        {
            VibratorHelper.shock(this);
            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("此票已扫描")
                    .positiveText("确定")
                    .canceledOnTouchOutside(false)//点击外部不取消对话框
                    .onAny(new MaterialDialog.SingleButtonCallback(){
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                enableScanner();
                                etBarCode.setFocusable(true);
                                etBarCode.selectAll();
                            }
                        }
                    })
                    .show();
            return;
        }

        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("packageNumber",etBarCode.getText().toString());
                    params.put("palletId",palletId);
                    params.put("palletCategoryId",palletCategories.get(palletCategoriesspinner.getSelectedIndex()).getValue());
                    params.put("isCustoms",cbCustoms.isChecked());
                    params.put("isBattery",cbBattery.isChecked());
                    params.put("bindStationId",bindStationId);
                    params.put("header",Global.getHeader());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("PalletScan",params));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {
                    //progressDialog.dismiss();

                    String message = jsonObject.getString("Message");
                    if(!message.isEmpty()) {
                        VibratorHelper.shock(PalletActivity.this);
                        new MaterialDialog.Builder(PalletActivity.this)
                                .positiveText("确定")
                                .canceledOnTouchOutside(false)//点击外部不取消对话框
                                .title("操作失败")
                                .content(message)
                                .onAny(new MaterialDialog.SingleButtonCallback(){
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (which == DialogAction.POSITIVE) {
                                            enableScanner();
                                            etBarCode.selectAll();
                                        }
                                    }

                                })
                                .show();


                    }else{
                        cbBattery.setEnabled(false);
                        cbCustoms.setEnabled(false);
                        addItem(etBarCode.getText().toString());
                        String pieceInfo = String.format("共%s件，剩余%s件",jsonObject.getString("TotalPiece"),jsonObject.getString("ResiduePiece"));
                        ((TextView)findViewById(R.id.tv_pieceinfo)).setText(pieceInfo);
                        //新增的一个板
                        if(palletId==0){
                            palletId=jsonObject.getInt("PalletId");
                            palletNo=jsonObject.getString("PalletNo");
                            //移除临时板
                            RemovePallet(0);
                            //将服务端生成的板添加到板列表
                            AddPallte(palletId, palletNo,palletCategoryId,cbCustoms.isChecked(),cbBattery.isChecked(),true);
                            if(palletNoList.size()>0){
                                palletnospinner.setItems(palletNoList);
                            }
                        }
                        etBarCode.selectAll();
                        enableScanner();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        });
    }
    private void enableScanner(){
        etBarCode.setEnabled(true);
        scanner.lockScanKey();
       scanner.enableFailurePlayBeep(true);
    }

    /**
     * 根据日期获取板号列表
     */
    private void BindPalletNoList()
    {
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );
                    String dateString=tvDate.getText().toString();
                    Date date=sdf.parse(dateString) ;
                    params.put("dt","/Date("+date.getTime()+"+0800)/");
                    params.put("header",Global.getHeader());
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPalletInfoList",params));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {

                    palletInfoList.clear();
                    palletNoList.clear();
                    //TODO 获取板号列表
                    JSONArray array =  jsonObject.getJSONArray("Details");

                    for(int i =0;i<array.length();i++){

                        AddPallte(array.getJSONObject(i).getInt("Id"),array.getJSONObject(i).getString("No"),array.getJSONObject(i).getInt("CategoryId"),
                                array.getJSONObject(i).getBoolean("IsCustoms"), array.getJSONObject(i).getBoolean("IsBattery"),false);

                    }
                    if(palletNoList.size()>0){
                        palletnospinner.setItems(palletNoList);
                        setPalletDetails(0);
                    }
                    else{
                       Number index= palletnospinner.getSelectedIndex();
                        Number aa=index;

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        PalletInfoListResult result = client.GetPalletInfoList(dtpShipmentDate.Value, true, Global.Header);
//
//        List<PalletInfo> datasource = new List<PalletInfo>();
//        datasource.AddRange(result.Details);
//        bsPalletNoList.DataSource = datasource;
//        if (datasource.Count > 0)
//            cbPalletNoList.SelectedValue = datasource[0].Id;
    }

    /**
     * 添加到已扫描列表
     * @param referenceNumber
     */
    private void addItem(String referenceNumber){
        shipments.add(referenceNumber);
        reBindShipmentsListView();
        palletCategoriesspinner.setEnabled(shipments.size()<=0);
    }
    public Boolean Find(String trackNumber)
    {
        return shipments.contains(trackNumber);
    }
    private HashMap<String,Integer> getPalletCagetories() throws JSONException {
        JSONObject params = new JSONObject();
        params.put("header",Global.getHeader());
        PalletTask task = new PalletTask("GetPalletCategoryInfoList",params);
        task.execute();
        return null;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String dateString=year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date date  = new Date();
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvDate.setText(sdf.format(date));
        BindPalletNoList();
        dpd = null;
    }


    private class PalletInfo{

        public int Id;
        public String No;
        public int CategoryId;
        public Boolean IsBattery;
        public Boolean IsCustoms;
    }

    /**
     * 扫描结果广播接收
     */
    private class ScannerResultReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            //scanner.scan_stop();
            final String scanResult = intent.getStringExtra("value");//***重要 Extral参数
            /** 如果条码长度>0，解码成功。如果条码长度等于0解码失败。*/
            if (intent.getAction().equals(RES_ACTION)){//获取扫描结果   ****重要 Action
                if(scanResult.length()>0){
                    scanner.unlockScanKey();
                    etBarCode.setEnabled(false);
                    etBarCode.setText(scanResult);
                    pallet();
                }else{
                    //scanner.resultScan();
                   //
                    /**扫描失败提示使用有两个条件：
                     1，需要先将扫描失败提示接口打开只能在广播模式下使用，其他模式无法调用。
                     2，通过判断条码长度来判定是否解码成功，当长度等于0时表示解码失败。
                     * */
                   // Toast.makeText(getApplicationContext(), "解码失败!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private class PalletTask extends AsyncTask<Void,Void,AbstractMap.SimpleEntry<Boolean,String>>{

        JSONObject params;
        String action;
        public PalletTask(String action,JSONObject params){
            this.params = params;
            this.action=action;
        }
        @Override
        protected AbstractMap.SimpleEntry<Boolean,String> doInBackground(Void... voids) {
            JSONObject result = HttpHelper.getJSONObjectFromUrl(action,params);
            try {
                if(!result.getString("Message").isEmpty()){
                    return new AbstractMap.SimpleEntry<Boolean, String>(false,result.getString("Message"));
                }
                JSONArray categories = result.getJSONArray("Categories");
                for (int i=0;i<categories.length();i++){
                    palletCategories.add(new AbstractMap.SimpleEntry(categories.getJSONObject(i).getString("Value"),categories.getJSONObject(i).getInt("Key")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new AbstractMap.SimpleEntry<Boolean, String>(true,"");
        }

        @Override
        protected void onPostExecute(AbstractMap.SimpleEntry<Boolean,String> result) {
            if(result.getKey()) {
                ArrayList<String> list = new ArrayList<>();
                for (Map.Entry<String, Integer> item : palletCategories) {
                    list.add(item.getKey());
                }
                palletCategoriesspinner.setItems(list.toArray(new String[0]));
                //获取打板类别时，默认选中第一个打板类别
                palletCategoryId = palletCategories.get(0).getValue();
            }else{
                VibratorHelper.shock(PalletActivity.this);
                new MaterialDialog.Builder(PalletActivity.this)
                        .positiveText("确定")
                        .title("操作失败")
                        .content(result.getValue())
                        .cancelable(false)
                        .show();
            }
        }
    }
}
