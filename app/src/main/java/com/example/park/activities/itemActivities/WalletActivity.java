package com.example.park.activities.itemActivities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.park.R;
import com.example.park.activities.BaseActivity;
import com.example.park.ui.MyToolbar;

public class WalletActivity extends BaseActivity {

    MyToolbar toolbar;
    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_wallet);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.black));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("我的钱包");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
