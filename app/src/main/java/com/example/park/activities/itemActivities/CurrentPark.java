package com.example.park.activities.itemActivities;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.BaseActivity;
import com.example.park.activities.DeviceListActivity;
import com.example.park.listeners.HandleResponse;
import com.example.park.ui.ConnectDialog;
import com.example.park.ui.MyToolbar;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CurrentPark extends BaseActivity implements View.OnClickListener{
    MyToolbar toolbar;
    ImageView iv_bluetooth;
    ImageView iv_tips;
    ImageView iv_switch;
    TextView tv_connectLock;
    TextView tv_payback;
    EditText et_password;
    LinearLayout tipsLayout;
    int switchIndex = 0;
    int tipsVisible = 0;
    ConnectDialog connectDialog;
    ConnectDialog disconnectDialog;

    private HandleResponse handleResponse;

    private BluetoothDevice _device = null;     //蓝牙设备
    private BluetoothSocket _socket = null;      //蓝牙通信socket
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();  //获取本地蓝牙适配器，即蓝牙设备
    InputStream in;
    OutputStream os;

    Handler handler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what){
                case 0:
                    connectDialog.dismiss();
                    //iv_bluetooth.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.bluetooth));
                    Toast.makeText(CurrentPark.this,"连接失败，请检查蓝牙是否连接",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    disconnectDialog.dismiss();
                    iv_bluetooth.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.bluetooth_disconnect));
                    Toast.makeText(CurrentPark.this,"抱歉，您未停车",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_current_park);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
        initListener();
    }

    public void initView(){
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("当前订单");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iv_bluetooth = findView(R.id.bluetooth);
        iv_switch = findView(R.id.openOrClose);
        iv_tips = findView(R.id.tips);
        tv_connectLock = findView(R.id.connectLock);
        tv_payback = findView(R.id.disconnectLock);
        et_password = findView(R.id.et_password);
        tipsLayout = findView(R.id.instructionLayout);

        handleResponse = new HandleResponse() {
            @Override
            public void getResponse(String response) {

            }
        };

        //打开蓝牙
        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }
            }
        }.start();
        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(CurrentPark.this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            return;
        }
    }

    public void initListener(){
        iv_bluetooth.setOnClickListener(this);
        iv_switch.setOnClickListener(this);
        iv_tips.setOnClickListener(this);
        tv_connectLock.setOnClickListener(this);
        tv_payback.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Log.d("CurrentPark","MAC address-------->"+address);
                    try {
                        // 得到蓝牙设备句柄
                        _device = _bluetooth.getRemoteDevice(address);
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        _socket.connect();
                        Toast.makeText(CurrentPark.this,"连接成功",Toast.LENGTH_SHORT).show();
                        iv_bluetooth.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.bluetooth));
                        Constant.is_connectBT = true;
                    }catch (IOException e) {
                        Toast.makeText(CurrentPark.this,"连接失败",Toast.LENGTH_SHORT).show();
                    }
                    try {
                        in = _socket.getInputStream();
                        os = _socket.getOutputStream();
                    }catch (IOException e){
                        System.out.println("-------获取通道失败");
                    }
                }
                else {
                    Toast.makeText(CurrentPark.this,"蓝牙连接失败，请重试！",Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bluetooth:
                break;
            case R.id.openOrClose:
                if(Constant.is_connectBT){
                    if(switchIndex==0){
                        try {
                            os.write((et_password.getText().toString()+"**").getBytes());
                        } catch (IOException e) {
                            Toast.makeText(CurrentPark.this,"打开车位失败，请重试！",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            return;
                        }
                        iv_switch.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.button_on));
                        switchIndex = 1;
                    }else{
                        try {
                            os.write("+START OF**".getBytes());
                        } catch (IOException e) {
                            Toast.makeText(CurrentPark.this,"关闭车位失败，请重试！",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            return;
                        }
                        iv_switch.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.button_off));
                        switchIndex = 0;
                    }
                }else{
                    Toast.makeText(CurrentPark.this,"尚未连接地锁,请连接后重试",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tips:
                if(tipsVisible == 0){
                    tipsLayout.setVisibility(View.VISIBLE);
                    tipsVisible = 1;
                }else{
                    tipsLayout.setVisibility(View.GONE);
                    tipsVisible = 0;
                }
                break;
            case R.id.connectLock:
                /*connectDialog = new ConnectDialog(this,R.style.dialog);
                connectDialog.setTips(R.string.bluetooth_connect);
                connectDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            handler.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();*/
                connect();

                break;
            case R.id.disconnectLock:
              /*  disconnectDialog = new ConnectDialog(this,R.style.dialog);
                disconnectDialog.setTips(R.string.bluetooth_disconnect);
                disconnectDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            handler.sendEmptyMessage(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();*/
                if(Constant.is_connectBT){
                    try {
                        os.write("+START OF**".getBytes());
                    } catch (IOException e) {
                        Toast.makeText(CurrentPark.this,"关闭车位失败，请重试！",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }
                    iv_switch.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.button_off));
                    switchIndex = 0;
                    List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
                    paramsList.add(new BasicNameValuePair("value", "1"));
                    new ConnectServer(paramsList, Constant.submitOrderUrl,handleResponse).execute();
                }else{
                    Toast.makeText(CurrentPark.this,"尚未开始停车",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.et_password:
                break;
        }
    }


    public void connect(){
        if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
            Toast.makeText(CurrentPark.this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            return;
        }
        if(_socket==null){
            Intent serverIntent = new Intent(CurrentPark.this, DeviceListActivity.class); //跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
        }
        else {
            Toast.makeText(CurrentPark.this,"socket连接出错，请重新连接或重启程序！",Toast.LENGTH_SHORT);
        }
    }


}
