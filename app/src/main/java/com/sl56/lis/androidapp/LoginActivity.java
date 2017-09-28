package com.sl56.lis.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextView mEmailView;
    private EditText mPasswordView;
    private MaterialDialog pDialog;
    private MaterialSpinner spinnerCompanyList;
    private MaterialSpinner spinnerSites;
    private String errorMsg;
    private Integer companyId;
    private Integer siteId;
    private String downloadFileName;
    private List<Map.Entry<String,Integer>> companyList;
    private List<Map.Entry<String,Integer>> sites;
    private final String SDPATH = Environment.getExternalStorageDirectory() + "/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        pDialog = new MaterialDialog.Builder(this)
                .content("正在检测版本")
                .progress(true,0)
                .cancelable(false)
                .show();
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                try {
                    subscriber.onNext(HttpHelper.getVersion());
                } catch (Exception e) {
                    subscriber.onError(e);
                }
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
                pDialog.dismiss();
                new MaterialDialog.Builder(LoginActivity.this)
                        .content(e.getMessage())
                        .positiveText("确定")
                        .show();
            }

            @Override
            public void onNext(JSONObject jsonObject) {
                try {
                    pDialog.dismiss();
                    if(jsonObject==null){
                        getCompanyList();
                        return;
                    }
                    String serverPublishDate = jsonObject.getString("PublishDate");
                    String localPublicshDate=null;
                    Boolean isUpdate=false;
                    DBHelper db = new DBHelper(LoginActivity.this);
                    Cursor cursor = db.Fetch("PublishDate");//从数据库获取更新日期
                    if(cursor.getCount()>0){
                        cursor.moveToFirst();
                        localPublicshDate = cursor.getString(1);
                    }
                    if(localPublicshDate==null){
                        isUpdate=true;
                        db.Inert("PublishDate",serverPublishDate);
                    }
                    else
                    {
                        Date localDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(localPublicshDate);
                        Date serverDate =new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(serverPublishDate);
                        if(serverDate.getTime()>localDate.getTime()) {
                            isUpdate = true;
                            db.Update("PublishDate",serverPublishDate);
                        }
                    }
                    if(isUpdate){
                        pDialog = new MaterialDialog.Builder(LoginActivity.this)
                                .content("正在下载更新...")
                                .progress(true,0)
                                .cancelable(false)
                                .show();
                        downloadFileName = jsonObject.getString("FileName");
                        Observable.create(new Observable.OnSubscribe<InputStream>() {
                            @Override
                            public void call(Subscriber<? super InputStream> subscriber) {
                                try {
                                    InputStream inputStream = HttpHelper.getUpdateFile(downloadFileName);
                                    File file = new File(SDPATH+"SL56");
                                    if(!file.exists())
                                        file.mkdirs();
                                    file = new File(file.getPath()+"/"+downloadFileName);
                                    if(file.exists())
                                        file.delete();
                                    file.createNewFile();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    byte buffer [] = new byte[4 * 1024];
                                    int count =0;
                                    while((count=inputStream.read(buffer))>=0){
                                        fos.write(buffer,0,count);
                                    }
                                    fos.flush();
                                    fos.close();
                                    inputStream.close();
                                    installApk(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<InputStream>() {
                            @Override
                            public void call(InputStream inputStream) {

                            }
                        });
                    }
                    else
                        getCompanyList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mEmailView = (TextView) findViewById(R.id.userNo);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        spinnerCompanyList = (MaterialSpinner) findViewById(R.id.spinner);
        spinnerCompanyList.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                companyId=companyList.get(position).getValue();
            }
        });
        spinnerSites = (MaterialSpinner)findViewById(R.id.spinner_sites);
        spinnerSites.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                siteId = sites.get(position).getValue();
            }
        });

    }

    /**
     * 安装apk
     * @param file
     */
    private void installApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    public int compareVersion(String version1, String version2) throws PackageManager.NameNotFoundException {
        if (version1.equals(version2)) {
            return 0;
        }

        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");

        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;

        while (index < minLen && (diff = Integer.parseInt(version1Array[index]) - Integer.parseInt(version2Array[index])) == 0) {
            index ++;
        }

        if (diff == 0) {
            for (int i = index; i < version1Array.length; i ++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i ++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }

            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
    /**
     * 获取操作点列表
     */
    private void getSites(){
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject params = new JSONObject();
                try {
                    params.put("companyId",companyId);
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetSites",params));
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
                    JSONArray array = jsonObject.getJSONArray("Sites");
                    sites = new ArrayList<Map.Entry<String, Integer>>();
                    for(int i =0;i<array.length();i++){
                        Map.Entry<String,Integer> entry = new AbstractMap.SimpleEntry<String, Integer>(array.getJSONObject(i).getString("Value"),array.getJSONObject(i).getInt("Key"));
                        sites.add(entry);
                    }
                    List<String> siteNames = new ArrayList<>();
                    for(Map.Entry<String,Integer> entry:sites)
                        siteNames.add(entry.getKey());
                    spinnerSites.setItems(siteNames);
                    Global.setSites(sites);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * 获取公司列表
     */
    private void getCompanyList(){
        if(Global.getCompanyList()==null) {
            pDialog = new MaterialDialog.Builder(LoginActivity.this)
                    .progress(true, 0)
                    .cancelable(false)
                    .content("获取公司名列表...")
                    .show();
        }
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetCompanyList",new JSONObject()));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<JSONObject>() {
            @Override
            public void call(JSONObject jsonObject) {
                try {
                    if(jsonObject.toString().contains("Error")){
                        pDialog.dismiss();
                        new MaterialDialog.Builder(LoginActivity.this)
                                .title("网络异常")
                                .content(jsonObject.getString("Error"))
                                .positiveText("确定")
                                .show();
                        VibratorHelper.shock(LoginActivity.this);
                        return;
                    }
                    JSONArray array = jsonObject.getJSONArray("Details");
                    companyList = new ArrayList<Map.Entry<String, Integer>>();
                    for(int i=0;i<array.length();i++){
                        JSONObject companyObj = array.getJSONObject(i);
                        Map.Entry entry = new AbstractMap.SimpleEntry(companyObj.getString("Name"),companyObj.getInt("Id"));
                        companyList.add(entry);
                    }
                    Global.setCompanyList(companyList);
                    List<String> companyNams = new ArrayList<>();
                    for(Map.Entry<String,Integer> entry:companyList)
                        companyNams.add(entry.getKey());
                    spinnerCompanyList.setItems(companyNams);
                    companyId=companyList.get(0).getValue();
                    getSites();
                    pDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String userNo = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(siteId==null || siteId<0 ){
            new MaterialDialog.Builder(this)
                    .title("请选择操作点")
                    .positiveText("确定")
                    .cancelable(false)
                    .show();
            return;
        }
        if (TextUtils.isEmpty(userNo)) {
            focusView = mEmailView;
            pDialog = new MaterialDialog.Builder(this)
                    .title("请输入用户名")
                    .content("用户名不能为空")
                    .positiveText("确定")
                    .show();
            cancel = true;
        }else if(TextUtils.isEmpty(password)){
            focusView = mPasswordView;
            pDialog = new MaterialDialog.Builder(this)
                    .title("请输入密码")
                    .content("密码不能为空")
                    .positiveText("确定")
                    .show();
            cancel = true;
        }

        if (cancel) {
            //失败后重新获取焦点
            focusView.requestFocus();
        } else {

            focusView = mEmailView;
            pDialog =new MaterialDialog.Builder(this)
                    .content("登录中...")
                    .cancelable(false)
                    .progress(true,0)
                    .show();
            mAuthTask = new UserLoginTask(userNo, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(LoginActivity.this,
//                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
//
//        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject para = new JSONObject();
            try {
                para.put("userNo", mUsername);
                para.put("password", mPassword);
                para.put("companyId", companyId);
                para.put("siteId",siteId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject response = HttpHelper.getJSONObjectFromUrl("Login", para);
            try {
                if(response==null) {
                    errorMsg = "用户名或密码不正确";
                    return false;
                }
                JSONArray array = response.names();
                if(array.length()==0) {
                    errorMsg = "用户名或密码不正确";
                    return false;
                }
                if(array.get(0)=="Error") {
                    errorMsg=response.getString("Error");
                    return false;
                }
                else Global.setHeader(response);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Intent it = new Intent(LoginActivity.this,MainActivity.class);
                LoginActivity.this.startActivity(it);
                pDialog.dismiss();//activity关闭前要释放dialog
                finish();
            } else {
                pDialog.dismiss();
                pDialog = new MaterialDialog.Builder(LoginActivity.this)
                        .title("登录失败")
                        .content(errorMsg)
                        .cancelable(false)
                        .positiveText("确定")
                        .show();
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

