package com.example.park.activities.itemActivities;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.BaseActivity;
import com.example.park.ui.MyToolbar;

public class QuestionActivity extends BaseActivity {
    MyToolbar toolbar;
    LinearLayout askItem;
    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_question);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("在线答疑");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        askItem = findView(R.id.askItem);
        askItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionActivity.this,AskActivity.class);
                startActivity(intent);
            }
        });
    }

}
