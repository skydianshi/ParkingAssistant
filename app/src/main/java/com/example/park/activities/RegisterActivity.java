package com.example.park.activities;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.listeners.HandleResponse;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends BaseActivity {

    EditText et_username;
    EditText et_password;
    EditText et_repeatPaaaword;
    TextView tv_register;
    String username;
    String password;
    String repeatPassword;
    private HandleResponse handleResponse;

    android.support.design.widget.FloatingActionButton closeButton;

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_register);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
        initListener();
        handleResponse = new HandleResponse() {
            @Override
            public void getResponse(String response) {
                if(response.equals("2")){
                    Toast.makeText(RegisterActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.equals("3")){
                    Toast.makeText(RegisterActivity.this,"注册失败！",Toast.LENGTH_SHORT).show();
                }else if(response.equals("4")){
                    Toast.makeText(RegisterActivity.this,"用户名已存在，请重新注册",Toast.LENGTH_SHORT).show();
                }
                else if(response.equals("-1")){
                    Toast.makeText(RegisterActivity.this,"数据库操作失败！",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(RegisterActivity.this,"网络连接失败！",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public void initView(){
        et_username = findView(R.id.et_username);
        et_password = findView(R.id.et_password);
        et_repeatPaaaword = findView(R.id.repeatpassword);
        tv_register = findView(R.id.bt_go);
        closeButton = findView(R.id.fab);

    }

    public void initListener(){
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = et_username.getText().toString();
                password = et_password.getText().toString();
                repeatPassword = et_repeatPaaaword.getText().toString();
                if(!username.isEmpty()&&!password.isEmpty()&&!repeatPassword.isEmpty()){
                    if(!password.equals(repeatPassword)){
                        Toast.makeText(RegisterActivity.this,"密码不一致，请重新输入！",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
                        paramsList.add(new BasicNameValuePair("NAME", username));
                        paramsList.add(new BasicNameValuePair("CODE", password));
                        paramsList.add(new BasicNameValuePair("MODE", "register"));
                        new ConnectServer(paramsList, Constant.checkUserUrl,handleResponse).execute();
                    }
                }
                else{
                    Toast.makeText(RegisterActivity.this,"请将信息填写完整之后再进行提交！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
