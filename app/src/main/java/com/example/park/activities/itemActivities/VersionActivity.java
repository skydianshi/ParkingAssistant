package com.example.park.activities.itemActivities;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.BaseActivity;
import com.example.park.ui.ConnectDialog;
import com.example.park.ui.MyToolbar;

public class VersionActivity extends BaseActivity {
    TextView update;
    ConnectDialog connectDialog;
    MyToolbar toolbar;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    connectDialog.dismiss();
                    Toast.makeText(VersionActivity.this,"当前版本为最新版本，请静待更新",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_version);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        update = findView(R.id.checkUpdate);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectDialog = new ConnectDialog(VersionActivity.this,R.style.dialog);
                connectDialog.setTips(R.string.check_update);
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

        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("关于我们");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
