package com.mtramin.fingerprintplayground;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RequestThread implements Runnable {
    public String validateUrl;
    public ArrayList<String> keyarry;
    public ArrayList<String> valuearry;
    public JSONArray mJSONArray;
    public RequestThread(String url, ArrayList<String> key, ArrayList<String> value)
    {
        //这里是你与服务器交互的地址
        validateUrl = url;
        keyarry=new ArrayList<String>(key);
        valuearry=new ArrayList<String>(value);
    }
    public RequestThread(String url)
    {
        //这里是你与服务器交互的地址
        validateUrl = url;
    }
    public void run() {

        boolean result = false;


        HttpClient httpClient = new DefaultHttpClient();

        //设置链接超时
        httpClient.getParams().setParameter(CoreConnectionPNames.
                CONNECTION_TIMEOUT, 5000);

        //设置读取超时
        httpClient.getParams().setParameter(
                CoreConnectionPNames.SO_TIMEOUT, 5000);

        HttpPost httpRequst = new HttpPost(validateUrl);

        //准备传输的数据
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        int size=0;
        if (keyarry!=null){
            size=keyarry.size();
        }
        for(int i=0;i<size;i++)
        {
            params.add(new BasicNameValuePair(keyarry.get(i), valuearry.get(i)));
        }

        try
        {
            //发送请求
            
            httpRequst.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            //得到响应
            HttpResponse response = httpClient.execute(httpRequst);


            //返回值如果为200的话则证明成功的得到了数据
            if(response.getStatusLine().getStatusCode() == 200)
            {
                StringBuilder builder = new StringBuilder();

                //将得到的数据进行解析
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                for(String s = buffer.readLine(); s!= null; s = buffer.readLine())
                {
                    builder.append(s);
                }

                System.out.println(builder.toString());
                //得到Json对象
                JSONObject mObject   = new JSONObject(builder.toString());
                mJSONArray=new JSONArray();
                mJSONArray.put(mObject);
                //mJSONArray= mObject.getJSONArray("result");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
        }