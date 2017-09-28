package com.sl56.lis.androidapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Created by Josion on 2017/2/24.
 */

public class Global {
    public static JSONObject getHeader() {
        return Header;
    }

    public static void setHeader(JSONObject header) throws JSONException {
        header.put("Platform","Android");
        Header = header;
    }

    private static JSONObject Header;
    private static List<Map.Entry<String,Integer>> CompanyList;
    public static List<Map.Entry<String,Integer>> getCompanyList(){return CompanyList;};
    public static void setCompanyList(List<Map.Entry<String,Integer>> companyList){CompanyList=companyList;}

    public static List<Map.Entry<String, Integer>> getSites() {
        return Sites;
    }

    public static void setSites(List<Map.Entry<String, Integer>> sites) {
        Sites = sites;
    }

    public static List<Map.Entry<String,Integer>> Sites;
    public static String getMD5(String pwd) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(pwd.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
    }

}
