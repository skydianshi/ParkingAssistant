package com.example.park.activities.itemActivities;

import android.view.View;

import com.example.park.R;
import com.example.park.activities.BaseActivity;
import com.example.park.ui.MyToolbar;

public class HistoryOrders extends BaseActivity {
    MyToolbar toolbar;
    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_history_orders);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
    }

    public void initView(){
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("历史订单");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
