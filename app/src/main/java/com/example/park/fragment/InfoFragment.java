package com.example.park.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.MainActivity;
import com.example.park.activities.itemActivities.CurrentPark;
import com.example.park.activities.itemActivities.HistoryOrders;
import com.example.park.activities.itemActivities.QuestionActivity;
import com.example.park.activities.itemActivities.VersionActivity;
import com.example.park.activities.itemActivities.WalletActivity;
import com.example.park.activities.LoginActivity;
import com.example.park.util.Constant;

public class InfoFragment extends Fragment implements View.OnClickListener{
    View view;
    LinearLayout item2,item4,item5,item6,item7,item8,cancelLoginItem;
    Button loginButton;
    TextView tv_loginSuccess;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info, container, false);
        initialView();
        return view;
    }

    public void initialView(){
        item2 = (LinearLayout)view.findViewById(R.id.item2);
        item4 = (LinearLayout)view.findViewById(R.id.item4);
        item5 = (LinearLayout)view.findViewById(R.id.item5);
        item6 = (LinearLayout)view.findViewById(R.id.item6);
        item7 = (LinearLayout)view.findViewById(R.id.item7);
        item8 = (LinearLayout)view.findViewById(R.id.item8);
        cancelLoginItem = (LinearLayout)view.findViewById(R.id.cancelLogin);
        loginButton = (Button)view.findViewById(R.id.login);
        tv_loginSuccess = (TextView)view.findViewById(R.id.loginsuccess_tv);
        item2.setOnClickListener(this);
        item4.setOnClickListener(this);
        item5.setOnClickListener(this);
        item6.setOnClickListener(this);
        item7.setOnClickListener(this);
        item8.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        cancelLoginItem.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar.setTitle("个人中心");
        if(Constant.is_login){
            tv_loginSuccess.setText("欢迎您，"+Constant.username);
            tv_loginSuccess.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            cancelLoginItem.setVisibility(View.VISIBLE);
        }else{
            tv_loginSuccess.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            cancelLoginItem.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                if(Constant.is_login){
                    tv_loginSuccess.setText("欢迎您，"+Constant.username);
                    tv_loginSuccess.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.GONE);
                    cancelLoginItem.setVisibility(View.VISIBLE);
                }else{
                    tv_loginSuccess.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                    cancelLoginItem.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.item2:
                Intent walletIntent = new Intent(getActivity(), WalletActivity.class);
                startActivity(walletIntent);
                break;
            case R.id.item4:
                Intent orderIntent = new Intent(getActivity(), CurrentPark.class);
                startActivity(orderIntent);
                break;
            case R.id.item5:
                Toast.makeText(getActivity(),Constant.username,Toast.LENGTH_SHORT).show();
                break;
            case R.id.item6:
                Intent historyIntent = new Intent(getActivity(), HistoryOrders.class);
                startActivity(historyIntent);
                break;
            case R.id.item7:
                Intent questionIntent = new Intent(getActivity(), QuestionActivity.class);
                startActivity(questionIntent);
                break;
            case R.id.item8:
                Intent versionIntent = new Intent(getActivity(), VersionActivity.class);
                startActivity(versionIntent);
                break;
            case R.id.login:
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent,0);
                break;
            case R.id.cancelLogin:
                MainActivity.publishParkPlaceinfos.clear();
                Constant.is_login = false;
                Constant.is_loadImage = false;
                tv_loginSuccess.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
                cancelLoginItem.setVisibility(View.GONE);
        }

    }
}