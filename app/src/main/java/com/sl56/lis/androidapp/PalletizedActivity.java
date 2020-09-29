package com.sl56.lis.androidapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PalletizedActivity extends AppCompatActivity {
    //region 变量声明
    private EditText startDate = null;
    private EditText endDate = null;
    private ListView listView = null;

    private ArrayList<PalletItem> palletList = new ArrayList<PalletItem>();
    private ArrayList<String> tagList = new ArrayList<String>();
    private ArrayList<String> selectedTagList = new ArrayList<String>();
    private ArrayList<Integer> selectedPalletList = new ArrayList<Integer>();
    private PalletListAdapter adapter;
    private AlertDialog tagDialog;

    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palletized);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);

        //region 在视图中查找控件
        startDate = (EditText) findViewById(R.id.editTextDate1);
        endDate = (EditText) findViewById(R.id.editTextDate2);
        listView = (ListView) findViewById(R.id.lvPalletList);
        //endregion

        //region 控件初始值
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(new Date());
        startDate.setText(dateStr);
        endDate.setText(dateStr);
        //endregion

        //region 控件事件绑定
        startDate.setOnClickListener(DateClickListener);
        endDate.setOnClickListener(DateClickListener);
        listView.setOnItemClickListener(PalletItemClickListener);
        //endregion

        //region 加载初始数据
        GetPalletList();
        GetPalletizedTagInfoList();
        //endregion

    }


    //region 获取打板数据
    private void GetPalletList() {
        selectedPalletList.clear();
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateString1 = startDate.getText().toString();
                    String dateString2 = endDate.getText().toString();
                    Date date1 = sdf.parse(dateString1);
                    Date date2 = sdf.parse(dateString2);
                    params.put("startDate", "/Date(" + date1.getTime() + "+0800)/");
                    params.put("endDate", "/Date(" + date2.getTime() + "+0800)/");
                    params.put("header", Global.getHeader());
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPalletInfoList1", params));
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

                            palletList.clear();
                            //TODO 获取板号列表
                            JSONArray array = jsonObject.getJSONArray("Details");
                            for (int i = 0; i < array.length(); i++) {
                                PalletItem item = new PalletItem();
                                item.Id = array.getJSONObject(i).getInt("Id");
                                item.No = array.getJSONObject(i).getString("No");
                                item.IsCustoms = array.getJSONObject(i).getBoolean("IsCustoms");
                                item.IsBattery = array.getJSONObject(i).getBoolean("IsBattery");
                                item.IsEPM = array.getJSONObject(i).getBoolean("IsEPM");
                                palletList.add(item);
                            }
                            adapter = new PalletListAdapter(palletList, PalletizedActivity.this);
                            listView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    //endregion

    //region 获取Tag列表
    private void GetPalletizedTagInfoList() {
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("header", Global.getHeader());
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetPalletizedTagInfoList", params));
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

                            palletList.clear();
                            //TODO 获取板号列表
                            JSONArray array = jsonObject.getJSONArray("Tags");
                            for (int i = 0; i < array.length(); i++) {

                                String val = array.getString(i);
                                tagList.add(val);

                            }
                            adapter = new PalletListAdapter(palletList, PalletizedActivity.this);
                            listView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    //endregion

    //region 选择板号事件处理
    private AdapterView.OnItemClickListener PalletItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            PalletListAdapter.ViewHolder holder = (PalletListAdapter.ViewHolder) view.getTag();
            final PalletItem selectedItem = palletList.get(position);
            if (selectedItem.IsChecked == true) {
                selectedItem.IsChecked = false;
                selectedPalletList.remove((Integer) selectedItem.Id);
            } else {
                selectedItem.IsChecked = true;
                selectedPalletList.add(selectedItem.Id);
            }
            adapter.notifyDataSetChanged();
        }
    };
    //endregion

    //region 日期选择事件处理
    private View.OnClickListener DateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Calendar d = Calendar.getInstance(Locale.CHINA);
            Date myDate = new Date();
            d.setTime(myDate);
            final EditText editText = (EditText) view;
            int year = d.get(Calendar.YEAR);
            int month = d.get(Calendar.MONTH);
            int day = d.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(PalletizedActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    String dateString = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    try {
                        date = sdf.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    editText.setText(sdf.format(date));
                    GetPalletList();
                }
            }, year, month, day);
            datePickerDialog.setMessage("请选择日期");
            datePickerDialog.show();
        }
    };
    //endregion

    //region ToolBar相关
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "打印").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 2, 0, "历史").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 1:
                if(selectedPalletList.size()==0){
                    Toast.makeText(PalletizedActivity.this,
                           "至少选择一个板号", Toast.LENGTH_SHORT).show();
                }
                else{
                    ShowTagDialog();
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    //endregion

    //region 显示标签对话框
    public void ShowTagDialog() {
        selectedTagList.clear();
        tagDialog = new AlertDialog.Builder(this)
                .setMultiChoiceItems(tagList.toArray(new String[tagList.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b) {
                            selectedTagList.add(tagList.get(i));
                        } else {
                            selectedTagList.remove(tagList.get(i));
                        }
                    }
                })
                .setTitle("请选择备注")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                })
                .setNegativeButton("取消", null)
                .create();
        tagDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = tagDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (selectedTagList.size() > 0) {
                            SaveAndPrint();
                        } else {
                            Toast.makeText(PalletizedActivity.this,
                                    "你还真随便,怎么也要选一个吧", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        tagDialog.show();
    }

    public String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }
    //endregion

    //region 保存并打印标签
    public void SaveAndPrint() {
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    String remark = listToString(selectedTagList, ',');
                    String idList=listToString(selectedPalletList,',');
                    params.put("palletIdListString", idList);
                    params.put("remark", remark);
                    params.put("header", Global.getHeader());
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("PalletizedSave", params));
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
                            boolean res = jsonObject.getBoolean("Success");
                            if(res){
                                GetPalletList();
                                tagDialog.dismiss();
                            }
                            else{
                                Toast.makeText(PalletizedActivity.this,
                                        jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    //endregion
}