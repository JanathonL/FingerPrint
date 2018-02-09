package com.mtramin.fingerprintplayground;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.id;

public class showaddress {
    public ArrayList<HashMap<String, String>> mArray = new ArrayList<HashMap<String,String>>();
    public showaddress(){

        String url="http://106.15.198.74/app/public/index.php/index/Index/showaddress";
        RequestThread t=new RequestThread(url);
        try {
            Thread T1=new Thread(t);
            T1.start();
            T1.join();
            JSONArray mJSONArray=t.mJSONArray;

            for(int i =  0 ; i < mJSONArray.length(); i++)
            {
                JSONObject jsonItem = mJSONArray.getJSONObject(i);
                String address = jsonItem.getString("location");
                String name = jsonItem.getString("a_username");
                String phone= jsonItem.getString("phone");
                HashMap map = new HashMap<String, String>();
                map.put("id", id+"");
                map.put("name", name);
                map.put("phone",phone);
                map.put("location",address);
                mArray.add(map);
            }
        } catch (InterruptedException | org.json.JSONException e) {
            e.printStackTrace();
        }
    }

}