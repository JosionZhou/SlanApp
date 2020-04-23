package com.sl56.lis.androidapp;

import android.renderscript.Sampler;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.execchain.RetryExec;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;

import org.apache.http.client.methods.HttpPost;

/**
 * Created by Josion on 2017/2/18.
 */

public class HttpHelper {

    public  static JSONObject getJSONObjectFromUrl(String action, JSONObject data){
        String responseContent=execPost(action,data);
        if(responseContent==null || responseContent.trim().isEmpty())return null;
        JSONObject res= null;
        try {
            res =new JSONObject(responseContent);
            if(res.has("Message") && res.getString("Message").equals("null"))
                res.put("Message","");
            if(res.has("ErrorMessage") && res.getString("ErrorMessage").equals("null"))
                res.put("ErrorMessage","");

        } catch (JSONException e) {
            //如果不能转换为JsonObject尝试转换为JsonArray
            try {
                if(new JSONArray(responseContent)!=null){
                    res = new JSONObject("{\"Details\":"+responseContent+"}");
                }
            } catch (JSONException e1) {
                try {
                    return new JSONObject("{\"Contents\":"+responseContent+"}");
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
            return res;
        }
        return  res;
    }
    /*
    * 推荐使用HttpURLConnection来进行网络通信
    * */
    private static String execPost(String action, JSONObject data) {
        String urlString="http://app2.sl56.com/MobileServiceV2.svc/http/"+action;
        //String urlString="http://192.168.0.58:8011/MobileServiceV2.svc/http/"+action;
        StringBuilder sb=new StringBuilder();
        URL url=null;
        HttpURLConnection conn = null ;
        String lines = null;//读取请求结果
        try{
            url = new URL(urlString);
            conn= (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");// 提交模式
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            //设置连接超时30秒
            conn.setConnectTimeout(20*1000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setReadTimeout(20*1000);
            conn.connect();
            OutputStreamWriter outputStreamWriter=null;
            outputStreamWriter=new OutputStreamWriter(conn.getOutputStream());
            outputStreamWriter.write(data.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            lines = reader.readLine();
            reader.close();
            conn.disconnect();
            if(lines ==null || lines.isEmpty())
                return null;
        }catch (Exception e) {
            String s= String.format("{'Message':%s","'"+e.toString()+"'}");
            return s;
        }
        return lines;
    }


    static JSONObject getVersion() throws Exception {
       // String urlString = "http://192.168.0.58:8070/Update.ashx?Action=CheckVersion&AppKey=Android";
        String urlString = "http://update2.sl56.com/update.ashx?Action=CheckVersion&AppKey=Android";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");// 提交模式
        conn.setReadTimeout(20*1000);
        InputStream stream = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String result = reader.readLine().replace("[","").replace("]","");
        if(result.trim().isEmpty())
            return  null;
        return new JSONObject(result);
    }
    static InputStream getUpdateFile(String fileName) throws Exception {
        //String urlString = "http://192.168.0.58:8070/Update.ashx?Action=Download&AppKey=Android&FileName="+fileName;
        String urlString = "http://update2.sl56.com/update.ashx?Action=Download&AppKey=Android&FileName="+fileName;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");// 提交模式
        return conn.getInputStream();
    }

}
