package com.sl56.lis.androidapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigmercu.cBox.CheckBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Josion on 2017/3/2.
 * 自定义Fragment
 */

public class ScrollViewFragment extends Fragment {

    private Integer cellQuantity;//电池个数
    private JSONArray array;
    private String remarkText="";
    private int fragmentIndex;
    private Context context;
    private Boolean isRulesChanged;

    /**
     *
     * @param array 生成checkbox的列表
     * @param fragmentIndex 选项卡index
     * @param receiveGoodsDetailId 收货明细id
     * @param context
     */
    public ScrollViewFragment(JSONArray array,int fragmentIndex,int receiveGoodsDetailId,Context context){
        if(array!=null) {
            isRulesChanged=false;
            this.fragmentIndex= fragmentIndex;
            this.array = new JSONArray();
            this.context=context;
            for (int i = 0; i < array.length(); i++) {
                try {
                    this.array.put(new JSONObject(array.getJSONObject(i).toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 返回自定义View作为Fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view ;//根据传入的JsonArray生成复选框，若JsonArray为null则生成备注输入框
        if(array==null){
            view = inflater.inflate(R.layout.checkgoodsremark,null);
            ((EditText)view).addTextChangedListener(new EditChangedListener());
        }else{
            view = inflater.inflate(R.layout.scroolviewmain,null);
            org.apmem.tools.layouts.FlowLayout parentLL = (org.apmem.tools.layouts.FlowLayout) view.findViewById(R.id.activity_check_goods_remarks);
            for(int i=0;i<array.length();i++){
                final CheckBox v = (CheckBox)inflater.inflate(R.layout.checkboxtemplate, null);
                try {
                    String text="";
                    try {
                        text = array.getJSONObject(i).getString("Description");
                    }catch(JSONException ex){
                        text=array.getJSONObject(i).getString("Name");
                    }
                    Boolean isChecked;
                    if(!array.getJSONObject(i).isNull("OldIsChecked")) {
                        isChecked = array.getJSONObject(i).getBoolean("OldIsChecked");
                        //初始化时，NewIsChecked等于OldIsChecked
                        if(!isRulesChanged)
                            array.getJSONObject(i).put("NewIsChecked",isChecked);
                        else
                            isChecked = array.getJSONObject(i).getBoolean("NewIsChecked");
                    }
                    else
                        isChecked = array.getJSONObject(i).getBoolean("IsChecked");
                    v.setText(text);
                    v.setChecked(isChecked);
                    if(isChecked && fragmentIndex==2)//问题移除功能未做好，若wcf端已完善移除问题的功能，则去除此代码即可
                            v.setEnabled(false);//已保存的问题项，暂不允许移除
                    v.setTag(array.getJSONObject(i));
                    //为复选框绑定事件
                    v.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                        //只对复选框为电池和人工扣货的做特殊处理
                        @Override
                        public void onChange(final boolean checked) {
                            try {
                                final JSONObject tagObj =(JSONObject) v.getTag();
                                if(tagObj.isNull("IsChecked")) {
                                    tagObj.put("NewIsChecked", checked);
                                    isRulesChanged=true;
                                }
                                else
                                    tagObj.put("IsChecked",checked);
                                //若是选择了电池，则弹出电池数量输入框
                                if((tagObj.getString("Name").contains("PI")||tagObj.getString("Name").contains("电池"))) {
                                    if(checked) {
                                        String prefill = cellQuantity == null ? "" : cellQuantity.toString();
                                        int preSelectedIndex=-1;
                                        if(cellQuantity!=null){
                                            if(cellQuantity>4)
                                                preSelectedIndex=0;
                                            else
                                                preSelectedIndex=1;
                                        }
                                        new MaterialDialog.Builder(context)
                                                .title("请选择电池个数")
                                                .items("大于2个","小于等于2个")
                                                .cancelable(false)
                                                .alwaysCallSingleChoiceCallback()//每次选择单选框都触发回调事件
                                                .itemsCallbackSingleChoice(preSelectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                                                    @Override
                                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                        switch(which){
                                                            case 0:
                                                                cellQuantity=5;
                                                                break;
                                                            case 1:
                                                                cellQuantity=2;
                                                                break;
                                                        }
                                                        return true;
                                                    }
                                                })
                                                .positiveText("确定")
                                                .show();
                                    }else cellQuantity=0;
                                }
                                //若是人工扣货
                                else if((tagObj.getString("Name").contains("人工扣货"))){
                                    if(checked) {
                                        new MaterialDialog.Builder(context)
                                                .title("请输入扣货备注")
                                                .cancelable(false)
                                                //.negativeText("取消")
                                                .inputType(InputType.TYPE_CLASS_TEXT)
                                                .input("扣货备注", "", false, new MaterialDialog.InputCallback() {
                                                    @Override
                                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                        if(input!=null && !input.toString().trim().isEmpty())
                                                            remarkText += ("；"+input.toString());
                                                    }
                                                })
                                                .show();
                                    }
                                }
                                //选择有添加费用的报价规则时弹出提示框(因服务器端已禁止子对象公式运算，此功能暂时注释)
//                                if(checked && fragmentIndex==0){
//                                    JSONObject params = new JSONObject();
//                                    params.put("receiveGoodsDetailId",receiveGoodsDetailId);
//                                    params.put("ruleId",((JSONObject)(v.getTag())).getInt("Id"));
//                                    params.put("header",Global.getHeader());
//                                    GetChangeRuleExpressTask task = new GetChangeRuleExpressTask(params);
//                                    task.execute();
//                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    String regEx = "[\\u4e00-\\u9fa5]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(text);
                    int count=0;
                    while (m.find()) {
                        count ++;
                    }
                    int width = (40-(text.length()-1)*2)*count+(text.length()-count)*20;
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,50);
                    v.setLayoutParams(params);
                }catch(Exception e){
                    e.printStackTrace();
                }
                view.setTag(array);
                parentLL.addView(v);
            }
        }
        return view;
    }
    public JSONArray getCheckboxSourceArray(){
        return array;
    }
    public String getRemarkText(){
        return remarkText;
    }
    public Integer getCellQuantity(){
        return cellQuantity;
    }
    public class EditChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            remarkText=editable.toString();
        }
    }
    public class GetChangeRuleExpressTask extends AsyncTask<Void,Void,Boolean> {
        JSONObject params;
        public GetChangeRuleExpressTask(JSONObject params){
            this.params = params;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            JSONObject execResult = HttpHelper.getJSONObjectFromUrl("InspectionRuleCheckedStateChanged",params);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                new MaterialDialog.Builder(context)
                        .title("加收费用")
                        .show();
            }
            super.onPostExecute(success);
        }
    }
}
