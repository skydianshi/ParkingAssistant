package com.example.park.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.List;

import com.example.park.R;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.listeners.HandleResponse;
import com.example.park.ui.ConnectDialog;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;
import com.example.park.util.HttpUtils;


public class LoginActivity extends BaseActivity {
    Button submitBtnPost = null;
    //Button submitBtnGet = null;
    EditText nameEdit = null;
    EditText codeEdit = null;
    android.support.design.widget.FloatingActionButton registerButton;
    private HandleResponse handleResponse;
    String name;
    String password;
    ConnectDialog connectDialog;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    connectDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    setResult(1,intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_login);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
        initListener();
    }

    public void initView(){
        submitBtnPost = findView(R.id.bt_go);
        //submitBtnGet = (Button)findViewById(R.id.btn_submit_get);//通过get方式进行与服务器端的通信，该button在xml中没有定义
        nameEdit = findView(R.id.et_username);
        codeEdit = findView(R.id.et_password);
        nameEdit.setText("test");
        codeEdit.setText("123");
        registerButton = findView(R.id.fab);

    }

    public void initListener(){
        submitBtnPost.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            name = nameEdit.getText().toString();
            password = codeEdit.getText().toString();
            String mode = "login";
            List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
            paramsList.add(new BasicNameValuePair("NAME", name));
            paramsList.add(new BasicNameValuePair("CODE", password));
            paramsList.add(new BasicNameValuePair("MODE", mode));
            new ConnectServer(paramsList,Constant.checkUserUrl,handleResponse).execute();
            }
        });
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(registerIntent);
            }
        });
        handleResponse = new HandleResponse() {
            @Override
            public void getResponse(String response) {
                String res = response.trim();
                if(res.equals("1")){
                    Toast.makeText(LoginActivity.this,"用户名或密码错误，请重新输入后重试！",Toast.LENGTH_SHORT).show();

              /*  Intent LoginIntent = new Intent(LoginActivity.this,LoginSuccessActivity.class);
                startActivity(LoginIntent);*/
                }else if(res.equals("-1")){
                    Toast.makeText(LoginActivity.this,"网络连接不成功或访问数据库出错",Toast.LENGTH_SHORT).show();
                }else if(res.length()==0){
                    Toast.makeText(LoginActivity.this,"登录失败，请检查网络是否连接",Toast.LENGTH_SHORT).show();
                }
                else{
                    MainActivity.privateParkPlaceInfos.clear();
                    initialFigures(response.substring(12));
                    Constant.is_login = true;
                    Constant.username = name;
                    Constant.password = password;
                    connectDialog = new ConnectDialog(LoginActivity.this,R.style.dialog);
                    connectDialog.show();
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            // mMsgHandler.sendEmptyMessageDelayed(TYPE_CONNECT, 100);
                            try {
                                Thread.sleep(1000);
                                handler.sendEmptyMessage(0);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        };
    }


    public void initialFigures(String result){
        if(!result.equals("")){
            String [] results = result.split("\\+");
            for(int i = 0;i<results.length;i++){
                String [] charactors = results[i].split("-");
                ParkPlaceInfo parkPlace = new ParkPlaceInfo(charactors[0],charactors[1],charactors[2],charactors[3],charactors[4],charactors[5],charactors[6],charactors[7],charactors[8],charactors[9],Double.valueOf(charactors[10]),Double.valueOf(charactors[11]),
                        charactors[12],Integer.valueOf(charactors[13]),Boolean.valueOf(charactors[14]),Integer.valueOf(charactors[15]),Integer.valueOf(charactors[16]));
                MainActivity.privateParkPlaceInfos.add(parkPlace);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(ParkPlaceInfo p :MainActivity.privateParkPlaceInfos){
                        p.setClearBitmap(HttpUtils.getBitmapFromServer(Constant.imageUrl+p.getImagePath()+"_clear.png"));
                    }
                    Constant.is_loadImage = true;
                }
            }).start();
        }

    }
}
