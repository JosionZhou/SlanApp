package com.sl56.lis.androidapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class PalletActivity extends AppCompatActivity {
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
    private MaterialDialog progressDialog;
    private List<PalletInfo> palletInfoList = new ArrayList<>();
    private ListView lvShipments;
    private CheckBox cbCustoms;
    private CheckBox cbBattery;
    private boolean isProgressDialogShowing=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pallet);
        TextView textView = (TextView)findViewById(R.id.tv_date);
        Date now = new Date();
        DateFormat d = DateFormat.getDateInstance();
        String dateStr = d.format(now);
        cbCustoms = (CheckBox)findViewById(R.id.cb_customs);
        cbBattery =(CheckBox)findViewById(R.id.cb_battery);
        lvShipments = (ListView)findViewById(R.id.lv_shipments);
        etBarCode = (EditText)findViewById(R.id.etBarCode);
        etBarCode.selectAll();
        etBarCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    pallet();
                    return true;
                }
                return false;
            }
        });
        textView.setText(dateStr);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                Intent intent = getIntent();
                intent.putExtra("action","add");
                overridePendingTransition(0, 0);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
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
                    params.put("parentId", 4);
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
                    JSONArray array = jsonObject.getJSONArray("PostGroupms");
                    for(int i =0;i<array.length();i++){
                        bindingStationList.put(array.getJSONObject(i).getString("PostGroupName"),array.getJSONObject(i).getInt("PostGroupId"));
                    }
                    View view = getLayoutInflater().inflate(R.layout.settingstationbinds,null);
                    MaterialSpinner spinner = (MaterialSpinner)view.findViewById(R.id.stationSpinner);
                    spinner.setItems(bindingStationList.keySet().toArray());
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
            dbHelper.Update(bindStationId.toString());
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
            palletInfoList.remove(pi);
            List<String> palletNoList = new ArrayList<>();
            for(PalletInfo item:palletInfoList)
                palletNoList.add(item.No);
            if(palletNoList.size()>0)
                palletnospinner.setItems(palletNoList);

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
    private void AddPallte(int id, String value,int palletCategoryId,Boolean isCustomer,Boolean isBattery)
    {
        Boolean isContains=false;
        for(PalletInfo pi : palletInfoList){
            if(pi.Id==id)isContains=true;
        }
        if (palletInfoList.size()==0 || !isContains)
        {
            PalletInfo newItem = new PalletInfo();
            newItem.Id = id;
            newItem.No = value;
            newItem.CategoryId = palletCategoryId;
            newItem.IsBattery = isBattery;
            newItem.IsCustoms = isCustomer;
            palletInfoList.add(newItem);
            List<String> palletNoList = new ArrayList<>();
            for(PalletInfo pi:palletInfoList)
                palletNoList.add(pi.No);
            palletnospinner.setItems(palletNoList);
            palletnospinner.setSelectedIndex(palletInfoList.indexOf(newItem));
//            bsPalletNoList.ResetBindings(false);
//            cbPlatteCategory.SelectedValue = palletCategoryId;
        }


//        cbPalletNoList.SelectedValue = id;
    }
    private void pallet(){
        etBarCode.selectAll();
        String alertMsg;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title("扫描失败").positiveText("确定");
        final String temp = etBarCode.getText().toString().trim();
        String no ="";
        if(temp.isEmpty()){
                   builder.content("单号不能为空")
                    .show();
            VibratorHelper.shock(this);
            return;
        }
        if(bindStationId==null){
            builder.content("请设置语音提示岗位")
                    .show();
            VibratorHelper.shock(this);
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
                new  MaterialDialog.Builder(this)
                        .title("错误")
                        .content("扫描的单号异常")
                        .positiveText("确定")
                        .show();
                VibratorHelper.shock(this);
                return;
        }
        etBarCode.setText(no);
        //Aramex 多件是同一个单号，不做检查
        if (no.length() != 11 && Find(no))
        {
            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("此票已扫描")
                    .positiveText("确定")
                    .show();
            etBarCode.setFocusable(true);
            etBarCode.selectAll();
            VibratorHelper.shock(this);
            return;
        }
        progressDialog =  new MaterialDialog.Builder(PalletActivity.this)
                .progress(true,0)
                .content("数据获取中...")
                .cancelable(false)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        isProgressDialogShowing=false;
                    }
                })
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
                    VibratorHelper.shock(PalletActivity.this);
                    progressDialog.dismiss();
                    progressDialog =  new MaterialDialog.Builder(PalletActivity.this)
                            .content("数据获取失败，是否重新获取？")
                            .cancelable(false)
                            .positiveText("是")
                            .negativeText("否")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    pallet();
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
                    progressDialog.dismiss();
                    String message = jsonObject.getString("Message");
                    if(!message.isEmpty()) {
                        VibratorHelper.shock(PalletActivity.this);
                        new MaterialDialog.Builder(PalletActivity.this)
                                .positiveText("确定")
                                .title("操作失败")
                                .content(message)
                                .show();
                        etBarCode.selectAll();
                    }else{
                        cbBattery.setEnabled(false);
                        cbCustoms.setEnabled(false);
                        addItem(temp);
                        String pieceInfo = String.format("共%s件，剩余%s件",jsonObject.getString("TotalPiece"),jsonObject.getString("ResiduePiece"));
                        ((TextView)findViewById(R.id.tv_pieceinfo)).setText(pieceInfo);
                        //新增的一个板
                        if(palletId==0){
                            palletId=jsonObject.getInt("PalletId");
                            palletNo=jsonObject.getString("PalletNo");
                            //移除临时板
                            RemovePallet(-1);
                            //将服务端生成的板添加到板列表
                            AddPallte(palletId, palletNo,palletCategoryId,cbCustoms.isChecked(),cbBattery.isChecked());
                        }
                        etBarCode.selectAll();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
                    params.put("dt","/Date("+(new Date()).getTime()+"+0800)/");
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
                    //TODO 获取板号列表
                    JSONArray array =  jsonObject.getJSONArray("Details");
                    List<String> palletNoList = new ArrayList<String>();
                    for(int i =0;i<array.length();i++){
                        PalletInfo pi = new PalletInfo();
                        pi.No = jsonObject.getJSONArray("Details").getJSONObject(i).getString("No");
                        pi.Id = jsonObject.getJSONArray("Details").getJSONObject(i).getInt("Id");
                        pi.IsCustoms = jsonObject.getJSONArray("Details").getJSONObject(i).getBoolean("IsCustoms");
                        pi.CategoryId = jsonObject.getJSONArray("Details").getJSONObject(i).getInt("CategoryId");
                        pi.IsBattery = jsonObject.getJSONArray("Details").getJSONObject(i).getBoolean("IsBattery");
                        palletInfoList.add(pi);
                        palletNoList.add(pi.No);
                    }
                    if(palletNoList.size()>0)
                        palletnospinner.setItems(palletNoList);
                    AddPallte(-1,"待生成",0,true,true);
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


    private class PalletInfo{

        public int Id;
        public String No;
        public int CategoryId;
        public Boolean IsBattery;
        public Boolean IsCustoms;
    }


    private class PalletTask extends AsyncTask<Void,Void,Boolean>{

        JSONObject params;
        String action;
        public PalletTask(String action,JSONObject params){
            this.params = params;
            this.action=action;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            JSONObject result = HttpHelper.getJSONObjectFromUrl(action,params);
            try {
                JSONArray categories = result.getJSONArray("Categories");
                for (int i=0;i<categories.length();i++){
                    palletCategories.add(new AbstractMap.SimpleEntry(categories.getJSONObject(i).getString("Value"),categories.getJSONObject(i).getInt("Key")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if(isSuccess) {
                ArrayList<String> list = new ArrayList<>();
                for (Map.Entry<String, Integer> item : palletCategories) {
                    list.add(item.getKey());
                }
                palletCategoriesspinner.setItems(list.toArray(new String[0]));
                //获取打板类别时，默认选中第一个打板类别
                palletCategoryId = palletCategories.get(0).getValue();
            }
        }
    }
}
