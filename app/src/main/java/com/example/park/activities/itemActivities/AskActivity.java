package com.example.park.activities.itemActivities;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.BaseActivity;
import com.example.park.ui.ConnectDialog;
import com.example.park.ui.MyToolbar;

public class AskActivity extends BaseActivity {
    MyToolbar toolbar;
    TextView tv_ask;
    ConnectDialog connectDialog;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    connectDialog.dismiss();
                    Toast.makeText(AskActivity.this,"提交成功，我们将尽快为您处理，请耐心等待",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_ask);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("提问");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_ask = findView(R.id.askTextView);
        tv_ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectDialog = new ConnectDialog(AskActivity.this,R.style.dialog);
                connectDialog.setTips(R.string.submit_question);
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
        });
    }

}
