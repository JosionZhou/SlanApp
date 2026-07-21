package com.sl56.lis.androidapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ArchiveScanActivity extends AppCompatActivity {

    private EditText etReferenceNumber;
    private MaterialSpinner palletizedSpinner;
    private ScannerInterface scanner;
    private int selectedPalletizedId;
    private final List<PalletizedItem> palletizedItems = new ArrayList<>();
    private final List<String> successfulScans = new ArrayList<>();
    private ArrayAdapter<String> successfulScanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_scan);

        palletizedSpinner = (MaterialSpinner) findViewById(R.id.spinner_palletized);
        etReferenceNumber = (EditText) findViewById(R.id.et_referencenumber);
        ListView successfulScanList = (ListView) findViewById(R.id.lv_successful_scans);
        successfulScanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, successfulScans);
        successfulScanList.setAdapter(successfulScanAdapter);

        palletizedSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                selectedPalletizedId = position > 0 ? palletizedItems.get(position - 1).id : 0;
            }
        });
        etReferenceNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    disableScanner();
                    archiveScan();
                    return true;
                }
                return false;
            }
        });

        initScanner();
        loadPalletizedList();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        forceShowOverflowMenu();
    }

    private void initScanner() {
        scanner = new ScannerInterface(this);
        scanner.setOutputMode(0);
        scanner.enableFailurePlayBeep(true);
    }

    private void loadPalletizedList() {
        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("header", Global.getHeader());
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("GetArchiveScanPalletizedList", params));
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
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
                    public void onError(Throwable throwable) {
                        showDialog("加载失败", getErrorMessage(throwable));
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            ensureSuccessfulResponse(jsonObject);
                            JSONArray details = jsonObject.getJSONArray("Details");
                            palletizedItems.clear();
                            List<String> palletizedNos = new ArrayList<>();
                            palletizedNos.add("请选择物理板号");
                            for (int i = 0; i < details.length(); i++) {
                                JSONObject detail = details.getJSONObject(i);
                                PalletizedItem item = new PalletizedItem(detail.getInt("Id"), detail.getString("No"));
                                palletizedItems.add(item);
                                palletizedNos.add(item.no);
                            }
                            selectedPalletizedId = 0;
                            palletizedSpinner.setItems(palletizedNos);
                        } catch (Exception ex) {
                            showDialog("加载失败", getErrorMessage(ex));
                        }
                    }
                });
    }

    private void forceShowOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void archiveScan() {
        String scannedNumber = etReferenceNumber.getText().toString().trim();
        if (selectedPalletizedId == 0) {
            enableScanner();
            showDialog("提示", "请先选择物理板号");
            return;
        }
        if (scannedNumber.isEmpty()) {
            enableScanner();
            showDialog("提示", "单号不能为空");
            return;
        }

        String requestNumber = scannedNumber;
        if (requestNumber.length() == 16 && requestNumber.lastIndexOf("0430") == 12) {
            requestNumber = requestNumber.substring(0, 12);
        }
        final String displayNumber = scannedNumber;
        final String trackNumber = requestNumber;
        final int palletizedId = selectedPalletizedId;

        Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("trackNumber", trackNumber);
                    params.put("palletizedId", palletizedId);
                    params.put("header", Global.getHeader());
                    subscriber.onNext(HttpHelper.getJSONObjectFromUrl("ArchiveScanByPallet", params));
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
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
                    public void onError(Throwable throwable) {
                        enableScanner();
                        etReferenceNumber.selectAll();
                        showDialog("网络访问异常", getErrorMessage(throwable));
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        enableScanner();
                        etReferenceNumber.selectAll();
                        try {
                            ensureSuccessfulResponse(jsonObject);
                            successfulScans.add(0, displayNumber + " 扫描底单成功");
                            successfulScanAdapter.notifyDataSetChanged();
                        } catch (Exception ex) {
                            showDialog("操作失败", getErrorMessage(ex));
                        }
                    }
                });
    }

    private void ensureSuccessfulResponse(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            throw new JSONException("服务器未返回数据");
        }
        if (jsonObject.has("Error")) {
            throw new JSONException(jsonObject.optString("Error", "网络访问异常"));
        }
        if (!jsonObject.has("Success")) {
            throw new JSONException(jsonObject.optString("Message", "服务器返回格式错误"));
        }
        if (!jsonObject.getBoolean("Success")) {
            throw new JSONException(jsonObject.optString("Message", "操作失败"));
        }
    }

    private String getErrorMessage(Throwable throwable) {
        String message = throwable == null ? null : throwable.getMessage();
        return message == null || message.trim().isEmpty() ? "未知错误" : message;
    }

    private void enableScanner() {
        etReferenceNumber.setEnabled(true);
        scanner.lockScanKey();
    }

    private void disableScanner() {
        etReferenceNumber.setEnabled(false);
        scanner.unlockScanKey();
    }

    private void showDialog(String title, String content) {
        new MaterialDialog.Builder(ArchiveScanActivity.this)
                .positiveText("确定")
                .title(title)
                .content(content)
                .cancelable(false)
                .show();
        VibratorHelper.shock(ArchiveScanActivity.this);
    }

    private static class PalletizedItem {
        private final int id;
        private final String no;

        private PalletizedItem(int id, String no) {
            this.id = id;
            this.no = no;
        }
    }
}
