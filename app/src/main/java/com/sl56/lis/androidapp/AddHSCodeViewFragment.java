package com.sl56.lis.androidapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Josion on 2017/8/9.
 */

public class AddHSCodeViewFragment extends Fragment {
    int type=0;
    View view=null;
    int classId;
    int chapterId;
    String currentFourDigit;
    ArrayList<SpinnerItem> allCategories = new ArrayList<>();
    ArrayList<SpinnerItem> allChapters = new ArrayList<>();
    ArrayList<SpinnerItem> allFourCodes = new ArrayList<>();
    Context context;
    ListView selectTypeListView;
    ListView inputTypeListView;
    public AddHSCodeViewFragment(int type,Context context){
        this.type = type;
        this.context = context;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(type==0){
            view = inflater.inflate(R.layout.add_hscode_select_mode,null);
            selectTypeListView = (ListView) view.findViewById(R.id.lv_hscode_select);
            getCategory();
        }else{
            view = inflater.inflate(R.layout.add_hscode_input_mode,null);
            inputTypeListView = (ListView) view.findViewById(R.id.lv_hscode_input);
            EditText etHSCode = (EditText) view.findViewById(R.id.etHSCode);
            etHSCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        Integer.parseInt(s.toString());
                        if(s.length()==4){
                            currentFourDigit=s.toString();
                            getHSCodes();
                        }
                        else
                            inputTypeListView.setAdapter(null);
                    }catch(Exception e){}
                }
            });
        }
        return view;
    }
    private void getCategory(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetHSCodeClass",new JSONObject()));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JSONObject>() {
                    @Override
                    public void call(JSONObject jsonObject) {
                        try {
                            JSONArray HSCodeClassList=jsonObject.getJSONArray("Categories");
                            MaterialSpinner spinner = (MaterialSpinner) view.findViewById(R.id.spinner_hscode_class);
                            SpinnerItem first = new SpinnerItem();
                            first.setId(-1);
                            first.setText("请选择类别");
                            allCategories.add(first);
                            for(int i =0;i<HSCodeClassList.length();i++){
                                SpinnerItem si = new SpinnerItem();
                                si.setId(HSCodeClassList.getJSONObject(i).getInt("Key"));
                                si.setText(HSCodeClassList.getJSONObject(i).getString("Value"));
                                allCategories.add(si);
                            }
                            spinner.setItems(allCategories);
                            spinner.setSelected(false);
                            spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                                    SpinnerItem selectedObj = (SpinnerItem) item;
                                    if(selectedObj.getId()!=classId)
                                        classId=selectedObj.getId();
                                    if(classId!=-1){
                                        getChapter();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    private void getChapter(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetHSCodeChapter",new JSONObject()));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JSONObject>() {
                    @Override
                    public void call(JSONObject jsonObject) {
                        try {
                            JSONArray HSCodeChapterList=jsonObject.getJSONArray("Chapters");
                            MaterialSpinner spinner = (MaterialSpinner) view.findViewById(R.id.spinner_hscode_chapter);
                            for(int i =0;i<HSCodeChapterList.length();i++){
                                int cId = HSCodeChapterList.getJSONObject(i).getInt("ClassId");
                                if(cId!=classId)continue;
                                SpinnerItem si = new SpinnerItem();
                                si.setId(HSCodeChapterList.getJSONObject(i).getInt("ObjectId"));
                                si.setText(HSCodeChapterList.getJSONObject(i).getString("ObjectName"));
                                allChapters.add(si);
                            }
                            spinner.setItems(allChapters);
                            chapterId=allChapters.get(0).getId();
                            getFourCodes();
                            spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                                    SpinnerItem selectedObj = (SpinnerItem) item;
                                    if(selectedObj.getId()!=chapterId) {
                                        chapterId = selectedObj.getId();
                                        getFourCodes();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    private void getFourCodes(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetHSFourCodeList",new JSONObject()));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JSONObject>() {
                    @Override
                    public void call(JSONObject jsonObject) {
                        try {
                            JSONArray HSCodeChapterList=jsonObject.getJSONArray("HSFourCodeList");
                            MaterialSpinner spinner = (MaterialSpinner) view.findViewById(R.id.spinner_hscode_prefourdigit);
                            for(int i =0;i<HSCodeChapterList.length();i++){
                                int cId = HSCodeChapterList.getJSONObject(i).getInt("ChapterId");
                                if(cId!=chapterId)continue;
                                SpinnerItem si = new SpinnerItem();
                                si.setId(HSCodeChapterList.getJSONObject(i).getInt("FourDigit"));
                                si.setText(HSCodeChapterList.getJSONObject(i).getString("FourDigitTitle"));
                                allFourCodes.add(si);
                            }
                            spinner.setItems(allFourCodes);
                            currentFourDigit = allFourCodes.get(0).getId()+"";
                            getHSCodes();
                            spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                                    SpinnerItem selectedObj = (SpinnerItem) item;
                                    if(selectedObj.getId()+""!=currentFourDigit){
                                        currentFourDigit=selectedObj.getId()+"";
                                        getHSCodes();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    private void getHSCodes(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("prefix",currentFourDigit);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetHSCodeList",params));
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
                        int i =1;
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            JSONArray HSCodeList=jsonObject.getJSONArray("HSCodeList");
                            ListView lvHSCodeList = null;
                            if(type==0)
                                lvHSCodeList = selectTypeListView;
                            else
                                lvHSCodeList = inputTypeListView;
                            ArrayList<ListViewItem> lvil = new ArrayList<ListViewItem>();
                            for(int i =0;i<HSCodeList.length();i++){
                                ListViewItem lvi = new ListViewItem();
                                lvi.setId(HSCodeList.getJSONObject(i).getInt("ObjectId"));
                                lvi.setTitle(HSCodeList.getJSONObject(i).getString("ObjectNo"));
                                lvi.setDescription(HSCodeList.getJSONObject(i).getString("Description"));
                                lvil.add(lvi);
                            }
                            TwoLineListviewAdapter adapter = new TwoLineListviewAdapter(context,lvil);
                            lvHSCodeList.setAdapter(adapter);
                            lvHSCodeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ListViewItem lvi = (ListViewItem) ((ListView) parent).getAdapter().getItem(position);
                                    Intent it = new Intent();
                                    it.putExtra("selectObj",new String[]{lvi.getId()+"",lvi.getTitle(),lvi.getDescription()});
                                    FragmentActivity activity = getActivity();
                                    activity.setResult(1,it);
                                    activity.finish();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
    class SpinnerItem{
        private int Id;

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public String getText() {
            return Text;
        }

        public void setText(String text) {
            Text = text;
        }

        private String Text;

        @Override
        public String toString() {
            return getText();
        }
    }
}
