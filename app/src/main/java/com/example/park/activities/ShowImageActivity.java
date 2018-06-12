package com.example.park.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.example.park.R;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.ui.MyToolbar;
import com.example.park.util.Constant;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShowImageActivity extends BaseActivity{
    int position;
    ParkPlaceInfo currentParkPlaceInfo;
    ImageView photo;
    Bitmap bitmap;
    MyToolbar toolbar;

    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    photo.setImageBitmap(bitmap);
                    break;
            }
        }
    };
    @Override
    protected View getContentView() {
        Intent mIntent = getIntent();
        //传递item位置信息
        position = mIntent.getIntExtra("position",-1);
        currentParkPlaceInfo = MainActivity.publishParkPlaceinfos.get(position);
        return inflateView(R.layout.activity_showimage);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initialView();
    }

    public void initialView(){
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("车位照片");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        photo = (ImageView)findViewById(R.id.photo);
        if(position<4){
            photo.setImageBitmap(MainActivity.publishParkPlaceinfos.get(position).getBitmap());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                setImage(Constant.imageUrl+MainActivity.publishParkPlaceinfos.get(position).getImagePath()+"_clear.png");
            }
        }).start();
    }

    public void setImage(String url){
        URL myFileURL;
        try{
            myFileURL = new URL(url);
            //获得连接
            HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(6000);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(false);
            //这句可有可无，没有影响
            //conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            handler.sendEmptyMessage(0);
            //关闭数据流
            is.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}

