package com.mtramin.fingerprintplayground;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.id;

/**
 * Created by Student on 2017/7/12.
 */

public class ServerUtil {
//    public static ArrayList<HashMap<String, String>> mArray = new ArrayList<HashMap<String,String>>();
    public static ArrayList<HashMap<String, String>> newOrder(String user,String url,String type){
        ArrayList<HashMap<String, String>> mArray = new ArrayList<HashMap<String,String>>();
        ArrayList<String> key=new ArrayList<String>();
        ArrayList<String> value=new ArrayList<String>();
        key.add("username");
        value.add(user);
        key.add("goodsid");
        value.add("1");
        key.add("goodsnum");
        value.add("3");
        key.add("type");
        value.add(type);
        RequestThread t=new RequestThread(url,key,value);
        try {
            Thread T1=new Thread(t);
            T1.start();
            T1.join();
            JSONArray mJSONArray=t.mJSONArray;

            for(int i =  0 ; i < mJSONArray.length(); i++)
            {
                JSONObject jsonItem = mJSONArray.getJSONObject(i);
//                String address = jsonItem.getString("location");
                String isOk = jsonItem.getString("isOk");
                if (isOk.equals("0")){
                    return null;
                }
                String name = jsonItem.getJSONObject("data").getString("username");

                String sumPrice = jsonItem.getJSONObject("data").getString("sumprice");
                String date = jsonItem.getJSONObject("data").getString("date");
//                String phone= jsonItem.getString("phone");
                HashMap map = new HashMap<String, String>();
                map.put("id", id+"");
                map.put("name", name);
                map.put("isOk", isOk);
                map.put("sumPrice", sumPrice);
                map.put("date", date);
//                map.put("phone",phone);
//                map.put("location",address);
                mArray.add(map);
            }
            return mArray;
        } catch (InterruptedException | org.json.JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<HashMap<String, String>> getGoodsInfo(String url){
        ArrayList<HashMap<String, String>> mArray = new ArrayList<HashMap<String,String>>();
        ArrayList<String> key=new ArrayList<String>();
        ArrayList<String> value=new ArrayList<String>();
        key.add("goodsid");
        value.add("1");
        RequestThread t=new RequestThread(url,key,value);
        try {
            Thread T1=new Thread(t);
            T1.start();
            T1.join();
            JSONArray mJSONArray=t.mJSONArray;

            for(int i =  0 ; i < mJSONArray.length(); i++)
            {
                JSONObject jsonItem = mJSONArray.getJSONObject(i);
//                String address = jsonItem.getString("location");
                String isOk = jsonItem.getString("isOk");
                if (isOk.equals("0")){
                    return null;
                }
                String goodsid = jsonItem.getJSONObject("data").getString("goodsid");

                String price = jsonItem.getJSONObject("data").getString("price");
                String goodsname = jsonItem.getJSONObject("data").getString("goodsname");
                String coverurl = jsonItem.getJSONObject("data").getString("coverurl");
                String content = jsonItem.getJSONObject("data").getString("content");
//                String phone= jsonItem.getString("phone");
                HashMap map = new HashMap<String, String>();
                map.put("isOk",isOk);
                map.put("goodsid", goodsid);
                map.put("price", price);
                map.put("goodsname", goodsname);
                map.put("coverurl", coverurl);
                map.put("content", content);
//                map.put("phone",phone);
//                map.put("location",address);
                mArray.add(map);
            }
            return mArray;
        } catch (InterruptedException | org.json.JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
