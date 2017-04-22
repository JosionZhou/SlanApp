package com.sl56.lis.androidapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PrintLabelActivity extends AppCompatActivity {

    private MaterialDialog dialog;
    private EditText etReferenceNumber;
    private String errorMessage;
    private InputMethodManager imm;
    private int transportDocumentId;
    private JSONObject result;
    private Long lastPrintTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_label);
        etReferenceNumber = (EditText)findViewById(R.id.etreferencenumber);
        //设置输入框弹出键盘默认为数字键盘
//        String digists = "0123456789abcdefghigklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        etReferenceNumber.setKeyListener(DigitsKeyListener.getInstance(digists));
        etReferenceNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    printLabelScan();
                    return true;
                }
                return false;
            }
        });
        Button btnPrint = (Button)findViewById(R.id.btn_printlabels);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printLabelScan();
            }
        });
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etReferenceNumber.getWindowToken(), 0); //强制隐藏键盘

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
    }
    private void printLabelScan(){
        if(etReferenceNumber.getText().toString().trim().isEmpty()){
            dialog = new MaterialDialog.Builder(this)
                    .title("单号不能为空")
                    .content("请输入单号")
                    .positiveText("确定")
                    .show();
            VibratorHelper.shock(PrintLabelActivity.this);
        }
        else{
            imm.hideSoftInputFromWindow(etReferenceNumber.getWindowToken(), 0); //强制隐藏键盘
            dialog = new MaterialDialog.Builder(this)
                    .content("获取数据中...")
                    .progress(true,0)
                    .cancelable(false)
                    .show();
            JSONObject params = new JSONObject();
            try {
                params.put("referenceNumber",etReferenceNumber.getText().toString().trim());
                params.put("header",Global.getHeader());
                PrintLabelTask task = new PrintLabelTask(params,"PrintLabelScan");
                task.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListInfos() throws JSONException {
         ArrayList<String> list =getCommonInfos();
        if(result.getString("ModeOfTransportName").toUpperCase().contains("DHL")){
            list.add("转单号："+result.getString("TrackNumber"));
            list.add("Label："+(result.getBoolean("IsPrint")?"已打印":"未打印"));
        }else if(result.getString("ModeOfTransportName").toUpperCase().contains("FEDEX") || result.getString("ModeOfTransportName").toUpperCase().contains("UPS")){
            list.add("需要电池信："+(result.getBoolean("IsBatteryLetter")?"是":"否"));
            list.add("账号："+result.getString("AccountNumber"));
            list.add("PI966："+(result.getBoolean("IsPI966")?"是":"否"));
            list.add("PI967："+(result.getBoolean("IsPI967")?"是":"否"));
            list.add("PI969："+(result.getBoolean("IsPI969")?"是":"否"));
            list.add("PI970："+(result.getBoolean("IsPI970")?"是":"否"));
            list.add("干电池："+(result.getBoolean("IsDryBattery")?"是":"否"));
        }
        String[] data = list.toArray(new String[]{});
        ArrayAdapter ad = new ArrayAdapter(this,R.layout.listviewitems,data);
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setAdapter(ad);
    }
    private ArrayList getCommonInfos() throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        list.add("渠道："+result.getString("ModeOfTransportName"));
        list.add("国家："+result.getString("CountryName"));
        list.add("件数："+result.getInt("Piece"));
        list.add("入重："+result.getDouble("Weight"));
        list.add("需要转口证："+(result.getBoolean("IsReExport")?"是":"否"));
        list.add("单独报关："+(result.getBoolean("IsCustomsDeclaration")?"是":"否"));
        list.add("需要发票(打印)："+(result.getBoolean("IsInvoice")?"是":"否"));
        list.add("需要发票(其他)："+(result.getBoolean("IsRequeiredInvoice")?"是":"否"));
        list.add("随货资料："+(result.getBoolean("IsFollowDocument")?"是":"否"));
        return list;
    }
    private void checkIsPrinted() throws JSONException {
        Boolean isPrinted = result.getBoolean("IsPrint");
        if(isPrinted){
            new MaterialDialog.Builder(this)
                    .title("该票已打印")
                    .content("重复打印需要输入密码")
                    .cancelable(true)
                    .canceledOnTouchOutside(true)//点击提示框外的地方关闭提示框
                    .negativeText("取消")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            return;
                        }
                    })
                    .autoDismiss(false)
                    .input("请输入密码", "",false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            try {
                                if(!Global.getHeader().getString("Password").equals(input.toString())){
                                    Toast.makeText(PrintLabelActivity.this,"密码不正确",Toast.LENGTH_SHORT);
                                    new MaterialDialog.Builder(PrintLabelActivity.this)
                                            .title("密码错误")
                                            .content("请重新输入")
                                            .positiveText("确定")
                                            .show();
                                    return;
                                }else{
                                    if(checkTime())
                                        doPrint(true);
                                    else
                                        new MaterialDialog.Builder(PrintLabelActivity.this)
                                                .title("警告")
                                                .content("严格执行Label一出一贴原则，严禁多出多贴，违者重罚")
                                                .positiveText("确定")
                                                .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    }).show();
            VibratorHelper.shock(PrintLabelActivity.this);//设备震动提示
        }else {
            new MaterialDialog.Builder(this)
                    .title("确定要打印吗？")
                    .negativeText("取消")
                    .positiveText("确定")
                    .cancelable(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            try {
                                if(checkTime())
                                    doPrint(false);
                                else
                                    new MaterialDialog.Builder(PrintLabelActivity.this)
                                            .title("警告")
                                            .content("严格执行Label一出一贴原则，严禁多出多贴，违者重罚")
                                            .positiveText("确定")
                                            .show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .show();
        }

    }

    /**
     * 执行打印Label
     * @param isReprint 是否已经打印过Label
     * @throws JSONException
     */
    private void doPrint(Boolean isReprint) throws JSONException {
        JSONObject params = new JSONObject();
        params.put("transportDocumentId",result.getInt("TransportDocumentId"));
        params.put("transportName",result.getString("ModeOfTransportName"));
        params.put("receiveGoodsDetailId",result.getInt("ReceiveGoodsDetailId"));
        params.put("isRePrint",isReprint);
        params.put("header",Global.getHeader());
        PrintLabelTask task = new PrintLabelTask(params,"PrintLabel");
        task.execute();
    }
    private boolean checkTime() throws JSONException {
        boolean res = true;
        if(result.getString("ModeOfTransportName").toUpperCase().contains("DHL")){
            if(lastPrintTime!=null){
                long tempTickts = SystemClock.elapsedRealtime();
                long ticks = tempTickts - lastPrintTime;
                if (ticks < 12 * 1000)
                {

                    res = false;
                }
                else
                    lastPrintTime = tempTickts;
            }
            else
                lastPrintTime = SystemClock.elapsedRealtime();
        }
        return res;
    }
    private class PrintLabelTask extends AsyncTask<Void,Void,Boolean>{

        private JSONObject params;
        private String action;
        public PrintLabelTask(JSONObject params,String action){
            this.action=action;
            this.params = params;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            result = HttpHelper.getJSONObjectFromUrl(action,params);
            if(result==null || result.toString().trim().isEmpty()){
                errorMessage="网络访问异常";
                return false;
            }
            try {
                if(result.names().get(0).equals("Error")){
                    errorMessage = result.getString("Error");
                    return false;
                }
                if(!result.getString("ErrorMessage").isEmpty()){
                    errorMessage = result.getString("ErrorMessage");
                    return false;
                }
                transportDocumentId = result.getInt("TransportDocumentId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean isSuccess) {
            dialog.dismiss();
            if(isSuccess){
                try {
                    setListInfos();
                    checkIsPrinted();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                dialog = new MaterialDialog.Builder(PrintLabelActivity.this)
                        .title("操作失败")
                        .content(errorMessage)
                        .positiveText("确定")
                        .show();
                etReferenceNumber.selectAll();
                VibratorHelper.shock(PrintLabelActivity.this);
            }
        }
    }
}
