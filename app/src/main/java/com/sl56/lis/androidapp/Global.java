package com.sl56.lis.androidapp;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Josion on 2017/2/24.
 */

public class Global {
    public static JSONObject getHeader() {
        return Header;
    }

    public static void setHeader(JSONObject header) {
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
}
