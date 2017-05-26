package com.sl56.lis.androidapp;

import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CheckGoodsActivity extends AppCompatActivity {
    private EditText etReferencenumber;
    private TextView tvPriceName;
    private TextView tvCheckInfo;
    private CheckGoodsTask task;
    private MaterialDialog dialog;
    private SegmentTabLayout tabLayout;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<JSONArray> arrayList = new ArrayList<>();
    private final String[] tabTitles = {"报价规则","其他规则","问题","备注"};
    private Button btnCheckGoods;
    private Button btnSave;
    private int receiveGoodsDetialId=0;//收货Id
    private JSONArray priceRules;//查货返回的报价规则
    private JSONArray otherRules;//查货返回的其他规则
    private JSONArray problems;//查货返回的问题
    private String InspectionTips;//查货提示
    private Integer cellQuantity;//电池数
    private Boolean isChecked;//是否已经查货
    private SetPriceNameHandler handler = new SetPriceNameHandler();
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
        tvPriceName = (TextView)findViewById(R.id.tv_pricename);
        tvCheckInfo = (TextView)findViewById(R.id.tv_checkinfo);
        etReferencenumber = (EditText) findViewById(R.id.etreferencenumber);
        //设置输入框弹出键盘默认为数字键盘
        String digists = "0123456789abcdefghigklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        etReferencenumber.setKeyListener(DigitsKeyListener.getInstance(digists));
        //监听键盘回车键事件
        etReferencenumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    final String referenceNumber = ((EditText) findViewById(R.id.etreferencenumber)).getText().toString();
                    checkGoodsScan(referenceNumber);
                    return true;
                }
                return false;
            }
        });
        btnSave = (Button)findViewById(R.id.btn_save);
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
                final String referenceNumber = ((EditText) findViewById(R.id.etreferencenumber)).getText().toString();
                checkGoodsScan(referenceNumber);
            }
        });


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.groupmember:
                Intent intent2 = new Intent(CheckGoodsActivity.this, StationMemberSettingActivity.class);
                CheckGoodsActivity.this.startActivity(intent2);
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

    /**
     * 保存查货
     */
    private void saveCheckGoods(){
        if(receiveGoodsDetialId==0){
            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                    .title("保存失败")
                    .content("请扫描单号成功后再保存")
                    .positiveText("确定")
                    .show();
            return;
        }
        JSONArray changeRules = new JSONArray();
        JSONArray changeProblems = new JSONArray();
        for(int i=0;i<3;i++) {
            int checkBoxCount =((ScrollViewFragment) mFragments.get(i)).getCheckboxSourceArray().length();
            for(int j=0;j<checkBoxCount;j++){
                try {
                    JSONObject tagObj = ((ScrollViewFragment) mFragments.get(i)).getCheckboxSourceArray().getJSONObject(j);
                    Boolean isCurrentCheck = tagObj.getBoolean("IsChecked");
                    //判断保存时复选框的状态和加载时是否一样
                    switch(i) {
                        case 0:
                        if (priceRules.getJSONObject(j).getBoolean("IsChecked") != isCurrentCheck) {
                            JSONObject json = new JSONObject(tagObj.toString());
                            if (isCurrentCheck)
                                json.put("ChangeType", 0);
                            else
                                json.put("ChangeType", 1);
                                changeRules.put(json);
                        }
                        break;
                        case 1:
                            if (otherRules.getJSONObject(j).getBoolean("IsChecked") != isCurrentCheck) {
                                JSONObject json = new JSONObject(tagObj.toString());
                                if (isCurrentCheck)
                                    json.put("ChangeType", 0);
                                else
                                    json.put("ChangeType", 1);
                                changeRules.put(json);
                            }
                            break;
                        case 2:
                            if (problems.getJSONObject(j).getBoolean("IsChecked") != isCurrentCheck) {
                                JSONObject json = new JSONObject(tagObj.toString());
                                if (isCurrentCheck)
                                    json.put("ChangeType", 0);
                                else
                                    json.put("ChangeType", 1);
                                    changeProblems.put(json);
                            }
                            break;
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("receiveGoodsDetailId",receiveGoodsDetialId);
            jsonParams.put("changedRules",changeRules);
            jsonParams.put("problems",changeProblems);
            jsonParams.put("remark",((ScrollViewFragment)mFragments.get(1)).getRemarkText()+"；"+((ScrollViewFragment)mFragments.get(2)).getRemarkText());
            jsonParams.put("header",Global.getHeader());
            //若是已查货，并且上一次查货存在电池数，在此次操作中没有直接保存的话，直接取上次查货的电池数
            //若此次查货讲电池数更改为0，则getCellQuantity()会得到0，而不是null
            if(isChecked && ((ScrollViewFragment) mFragments.get(0)).getCellQuantity()==null)
                jsonParams.put("cellQuantity",cellQuantity);
            else
                jsonParams.put("cellQuantity",((ScrollViewFragment) mFragments.get(0)).getCellQuantity());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dialog =  new MaterialDialog.Builder(CheckGoodsActivity.this)
                .content("正在保存数据...")
                .progress(true,0)
                .show();
//        dialog.setTitleText("正在保存数据...");
//        dialog.show();
        task = new CheckGoodsTask("InspectionSave",jsonParams,1);
        task.execute();
    }

    /**
     * 查货扫描
     * @param referenceNumber 扫描的原单号
     */
    private void checkGoodsScan(String referenceNumber) {
        if(referenceNumber.trim().isEmpty()) {
            dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                    .title("请输入单号")
                    .content("单号不能为空")
                    .positiveText("确定")
                    .show();
            VibratorHelper.shock(this);
        }else{
            JSONObject params = new JSONObject();
            try {
                params.put("referenceNumber",referenceNumber);
                params.put("header",Global.getHeader());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog =  new MaterialDialog.Builder(CheckGoodsActivity.this)
                    .content("正在获取数据...")
                    .progress(true,0)
                    .show();
            task=new CheckGoodsTask("InspectionScan",params,0);
            task.execute();
        }
    }

    /**
     * 设置选项卡的适配器
     * @param type 0:查货扫描 1:查货保存
     */
    private void setTabAdapter(int type){
        FragmentManager fm = getSupportFragmentManager();
        MyPagerAdapter mpa = new MyPagerAdapter(fm,mFragments);
        if(type==0) {//若是查货扫描，则重新生成CheckBox
            ArrayList<Fragment> newFragments = new ArrayList<>();
            for (JSONArray ja : arrayList) {
                newFragments.add(new ScrollViewFragment(ja, arrayList.indexOf(ja),receiveGoodsDetialId,CheckGoodsActivity.this));
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
    public class CheckGoodsTask extends AsyncTask<Void, Void, Boolean> {
        private String referenceNumber;
        private String alertMsg;
        private JSONObject params;
        private  String action;
        private int status;//查货保存返回的状态值 -1：重新计算报价 0：失败 1：成功
        private int type;

        /**
         *
         * @param action 要执行的动作
         * @param params 要传递的参数
         * @param type 0:查货扫描  1:查货保存
         */
        public CheckGoodsTask(String action,JSONObject params,int type){
            this.params = params;
            this.action=action;
            this.type=type;
            this.referenceNumber=referenceNumber;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONObject result = HttpHelper.getJSONObjectFromUrl(action,params);
                if(result==null){
                    if(type==1) return true;
                    alertMsg="网络访问异常";
                    return false;
                }
                switch (type) {
                    case 0://处理查货扫描返回的结果
                        Boolean isSuccess = result.getBoolean("Success");
                        if(!isSuccess){
                            alertMsg=result.getString("ErrorMessage");
                            return false;
                        }
                        priceRules = result.getJSONArray("PriceRules");
                        otherRules = result.getJSONArray("OhterRules");
                        problems = result.getJSONArray("Problems");
                        arrayList.clear();
                        arrayList.add(priceRules);//添加报价规则到第一个选项卡
                        arrayList.add(otherRules);//添加其他报价规则到第二个选项卡
                        arrayList.add(problems);//添加问题到第三个选项卡
                        arrayList.add(null);//填写备注的选项卡为null
                        InspectionTips = result.getString("InspectionTips");
                        receiveGoodsDetialId = result.getInt("ReceiveGoodsDetailId");
                        cellQuantity = result.getInt("CellQuantity");
                        Message msg = handler.obtainMessage();
                        msg.obj = result.getString("PriceName");
                        msg.arg1 = result.getBoolean("IsInspection")?1:0;
                        msg.arg2 = result.getInt("Piece");
                        msg.sendToTarget();
                        break;
                    case 1://处理查货保存返回的结果
                        status = result.getInt("Result");
                        if(status==0) {
                            alertMsg = result.getString("ErrorMessage");
                            return false;
                        }
                        else if(status==-1){
                            alertMsg=result.getString("ErrorMessage")+".系统已刷新内价";
                            return false;
                        }
                        break;
                }
            } catch (JSONException e) {
                alertMsg = e.getMessage();
                return false;
            }
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            etReferencenumber.selectAll();//全选输入框文本
            dialog.dismiss();//释放dialog
            MaterialDialog.Builder builder = new MaterialDialog.Builder(CheckGoodsActivity.this);
            if(!isSuccess) {
                //音效提示
                //AudioHelper.playAudioByMP(CheckGoodsActivity.this,R.raw.pc_delete_device);
                //震动提示
                VibratorHelper.shock(CheckGoodsActivity.this);
                switch(type) {
                    case 0:
                    builder.title("查货扫描失败");
                        break;
                    case 1:
                        builder.title("查货保存失败");
                        break;
                }
                builder.content(alertMsg);
                builder.positiveText("确定");
                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(status==-1)
                            checkGoodsScan(etReferencenumber.getText().toString());
                    }
                });
                dialog = builder.show();
            }else{
                if(type==1)
                    Toast.makeText(CheckGoodsActivity.this,"保存成功",Toast.LENGTH_SHORT);
                if(!InspectionTips.isEmpty()){
                    dialog = new MaterialDialog.Builder(CheckGoodsActivity.this)
                            .title("查货提示")
                            .content(InspectionTips)
                            .positiveText("确定")
                            .show();
                    VibratorHelper.shock(CheckGoodsActivity.this);
                }
                setTabAdapter(type);
            }
        }
    }
    private class MyPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager fm;
        private ArrayList<Fragment> fragments;
        public MyPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments) {
            super(fm);
            this.fm=fm;
            this.fragments = fragments;
        }

        /**
         * FragmentManager会缓存Fragment，在更新Fragment前，要移除旧的Fragment，再更新fragmentlist
         * @param fragments 更新后的fragmentlist
         */
        public void setFragments(ArrayList fragments) {
            if(this.fragments != null){
                FragmentTransaction ft = fm.beginTransaction();
                for(Fragment f:this.fragments){
                    ft.remove(f);
                }
                ft.commit();
                ft=null;
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
    private class SetPriceNameHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            tvPriceName.setText(msg.obj.toString());
            tvCheckInfo.setText("共 "+msg.arg2+" 件； "+"是否查货："+(msg.arg1==0?"否":"是"));
            isChecked = !(msg.arg1==0);
        }
    }
}
