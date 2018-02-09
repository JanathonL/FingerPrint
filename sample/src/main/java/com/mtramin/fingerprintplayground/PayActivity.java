package com.mtramin.fingerprintplayground;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.id;

@TargetApi(18)
public class PayActivity{
    private final String targetDeviceName = "shop"; //服务端蓝牙名
    private BluetoothAdapter mBluetoothAdapter;
    private List<String> bluetoothDevices = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private final UUID MY_UUID = UUID
            .fromString("abcd1234-ab12-ab12-ab12-abcdef123456");//随便定义一个UUID
    private BluetoothSocket clientSocket;   //客户端socket
    private BluetoothGatt gatt;     //客户端gatt
    private int rssi;   //客户端rssi
    private BluetoothDevice device; //目标服务端设备

    private AcceptThread acceptThread;  //服务端监听线程
    private final String NAME = "Bluetooth_Socket"; //服务名
    private BluetoothServerSocket serverSocket; //服务端监听socket
//    private OutputStream os;//输出流

    private final String TAG="BT:";
    private Context context;
    private Activity activity;
    private int isGoods = 0;

    private InputStream is;//输入流
    private OutputStream os; //输出流

    public PayActivity(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;

        //动态蓝牙授权
        requestBluetoothPermission();

        //开启蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();

        //获取已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice curDevice : pairedDevices) {
                //对于每一个发现的蓝牙设备，检查是否为目标蓝牙
                if(targetDeviceName.equals(curDevice.getName())){
                    device = curDevice;
                }
            }
        }

        // 设置广播信息过滤
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//每搜索到一个设备就会发送一个该广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//当全部搜索完后发送该广播
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //注册开始发现广播
        filter.setPriority(Integer.MAX_VALUE);//设置优先级
        // 注册蓝牙搜索广播接收者，接收并处理搜索结果
        activity.registerReceiver(receiver, filter);

//        //绑定按钮1模拟客户端
//        Button bt1 = (Button) findViewById(R.id.beacon_1);
//        bt1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //如果配对列表中已经存在，则直接配对无需搜索
//                if(device != null){
//                    bondBlueTooth();
//                }
//                else {
//                    //如果当前在搜索，就先取消搜索
//                    if (mBluetoothAdapter.isDiscovering()) {
//                        mBluetoothAdapter.cancelDiscovery();
//                    }
//                    //开启搜索
//                    mBluetoothAdapter.startDiscovery();
//                }
//            }
//        });

//        //绑定按钮2模拟服务端
//        Button bt2 = (Button) findViewById(R.id.kungfu);
//        bt2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    startService();
//                }
//            }
//        );
    }

    public void startService(int isGoods) {
        //如果当前在接收，就先停止线程
        this.isGoods = isGoods;
        if (acceptThread == null) {

            acceptThread = new AcceptThread();
            acceptThread.start();
            Toast.makeText(context, "已开启", Toast.LENGTH_LONG).show();
        }
    }

    private static final int REQUEST_BLUETOOTH_PERMISSION=10;
    //动态蓝牙授权
    private void requestBluetoothPermission(){
        //判断系统版本
        if (Build.VERSION.SDK_INT >= 23) {
            //检测当前app是否拥有某个权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            //判断这个权限是否已经授权过
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                //判断是否需要 向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(activity,"Need bluetooth permission.",
                            Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(activity ,new String[]
                        {Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH_PERMISSION);
                return;
            }else{
            }
        } else {
        }
    }

    /**
     * 定义广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice curDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (curDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    //对于每一个搜索到的蓝牙
                    //Toast.makeText(context, "name:"+curDevice.getName()+"\taddress:"+curDevice.getAddress(),Toast.LENGTH_LONG).show();
                    //获取目标蓝牙设备
                    String name = curDevice.getName();
                    if(targetDeviceName.equals(name)){
                        device = curDevice;
                        Toast.makeText(context, "一搜索到指定设备",Toast.LENGTH_LONG).show();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成，进行配对
                bondBlueTooth();
            }
        }
    };

    //客户端连接服务端
    private void bondBlueTooth(){
        if(device==null){
            Toast.makeText(context, "找不到指定设备",Toast.LENGTH_LONG).show();
            return;
        }
        String address=device.getAddress();
        try{
            if(clientSocket==null){
                //创建gatt连接
                gatt = device.connectGatt(context, true, new BluetoothGattCallback() {
                    public void onReadRemoteRssi (BluetoothGatt gatt, int r, int status){
                        //TODO:获取rssi
                        if(status == BluetoothGatt.GATT_SUCCESS){
                            rssi = r;
                            try{
                                if(os!=null){
                                    //往服务器端写信息
                                    //TODO: 在这里传输客户端信息
                                    os.write(String.valueOf(rssi).getBytes());
                                }
                            }
                            catch (Exception e){

                            }
                        }
                    }
                });
                //创建客户端蓝牙Socket
                clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                clientSocket.connect();
                Toast.makeText(context, "连接中...",Toast.LENGTH_LONG).show();
                //获得输出流（客户端指向服务端输出文本）
                os = clientSocket.getOutputStream();
            }
            //TODO:设置距离判定条件
            gatt.readRemoteRssi();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


//            String url="http://10.214.216.199/index.php/index/Index/newOrder";
            String username = String.valueOf(msg.obj);
            String data="";
            String isOk="0";
            if(msg.arg1==0){
                String url="http://106.15.198.74/app/public/index.php/index/Index/newOrder";
                ArrayList<HashMap<String, String>> mArray=ServerUtil.newOrder(String.valueOf(msg.obj),url,"1");


                if (mArray==null){
                    data="购买失败";
                }
                else{
                    isOk= mArray.get(0).get("isOk");
                    String id = mArray.get(0).get("id");
                    String name = mArray.get(0).get("name");
                    String sumPrice = mArray.get(0).get("sumPrice");
                    String date = mArray.get(0).get("date");
                    data= "购买成功\n"+"id:"+id+"\n客户名:"+name+"\n总价:"+sumPrice+"\n购买日期:"+date;

                }
            }
            else{
                String url="http://106.15.198.74/app/public/index.php/index/Index/goodsInfo";
                ArrayList<HashMap<String, String>> mArray=ServerUtil.getGoodsInfo(url);


                if (mArray==null){
                    data="购买失败";
                }
                else{
                    isOk= mArray.get(0).get("isOk");
                    String goodsid = mArray.get(0).get("goodsid");
                    String price = mArray.get(0).get("price");
                    String goodsname = mArray.get(0).get("goodsname");
                    String coverurl = mArray.get(0).get("coverurl");
                    String content = mArray.get(0).get("content");
                    data= "购买成功\n"+"商品ID:"+goodsid+"\n商品名称:"+goodsname+"\n商品价格:"+price+"\n商品封面:"+coverurl+"\n详情内容:"+content;

                }
            }
//            String address = mArray.get(0).get("address");
            String message = data;
            message = data;
            //TODO:得到消息后处理消息
            Toast.makeText(context, message,
                    Toast.LENGTH_LONG).show();
            try {
                data="isOk:"+isOk+"\ntype:"+msg.arg1+"\n"+data;
                os.write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };

    //服务端监听客户端的线程类
    private class AcceptThread extends Thread {
        private BluetoothSocket socket; //服务端socket
        public boolean flag;
        public AcceptThread() {
            try {
                flag = true;
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
            }
        }
        @Override
        public void run() {
            try {
                while(flag){
                    socket = serverSocket.accept();
                    Toast.makeText(context, "已连接",Toast.LENGTH_LONG).show();
                    new ServiceThread(socket).run();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //服务端与客户端的通信线程类
    private class ServiceThread extends Thread {
        private BluetoothSocket socket; //服务端socket

        public boolean flag;
        public ServiceThread(BluetoothSocket socket){
            this.socket = socket;

            try{
                is = this.socket.getInputStream();
                os = this.socket.getOutputStream();
                flag = true;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void run(){
            try{
                while(flag) {
                    //TODO:在这里实现服务端业务逻辑
                    byte[] buffer =new byte[1024];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    msg.arg1 = isGoods;
                    handler.sendMessage(msg);
                }
            }
            catch (IOException e){

            }
        }
    }

}

