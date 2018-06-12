package com.example.park.fragment;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.AddParkPlaceActivity;
import com.example.park.activities.MainActivity;
import com.example.park.adapter.ParkplaceAdapter;
import com.example.park.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PublishFragment extends Fragment{



    private ViewPager viewPager;
    private RadioGroup group;
    private TextView addParkPlace;
    //图片资源
    private int[] imageIds = {R.mipmap.privatepark4,R.mipmap.privatepark3,R.mipmap.privatepark2,R.mipmap.privatepark1};
    //存放图片的数组
    private List<ImageView> mList;
    //当前索引位置以及上一个索引位置
    private int index = 0,preIndex = 0;
    //是否需要轮播标志
    private boolean isContinue = true;
    //定时器，用于实现轮播
    private Timer timer;
    private ListView mListView;
    private  ImageView backgroundView;
    public static Boolean isAdded = false;//防止重复添加模拟数据


    Handler mHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    index++;
                    viewPager.setCurrentItem(index);
            }
        }
    };

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_publish, null);
        initialView();
        initRadioButton(imageIds.length);

        if(Constant.is_login){
            if(MainActivity.privateParkPlaceInfos.size()==0){
                backgroundView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.bg_publish2));
            }else {
                backgroundView.setVisibility(View.INVISIBLE);
                mListView.setVisibility(View.VISIBLE);
            }
        }else{
            backgroundView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    private void initialView(){
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        group = (RadioGroup) view.findViewById(R.id.group);
        mList = new ArrayList<>();
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setOnTouchListener(onTouchListener);
        mListView = (ListView)view.findViewById(R.id.listView);
        addParkPlace = (TextView)view.findViewById(R.id.addParkplace);
        addParkPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(Constant.is_login){
                if(Constant.is_loadImage){
                Intent intent = new Intent(getActivity(), AddParkPlaceActivity.class);
                startActivity(intent);}
                else{
                    Toast.makeText(getActivity(),"当前网速较慢，车位未加载完成，请等待后重试",Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(getActivity(),"请先到个人中心登录后再添加车位，下图有测试账号！",Toast.LENGTH_SHORT).show();
            }
            }
        });
        backgroundView = (ImageView)view.findViewById(R.id.background);
    }

    /**
     * 根据图片个数初始化按钮
     * @param length
     */
    private void initRadioButton(int length) {
        for(int i = 0;i<length;i++){
            ImageView imageview = new ImageView(getActivity());
            imageview.setImageResource(R.drawable.rg_selector);//设置背景选择器
            imageview.setPadding(20,0,0,0);//设置每个按钮之间的间距
            //将按钮依次添加到RadioGroup中
            group.addView(imageview, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //默认选中第一个按钮，因为默认显示第一张图片
            group.getChildAt(0).setEnabled(false);
        }
    }

    /**
     * 根据当前触摸事件判断是否要轮播
     */
    View.OnTouchListener onTouchListener  = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                //手指按下和划动的时候停止图片的轮播
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    isContinue = false;
                    break;
                default:
                    isContinue = true;
            }
            return false;
        }
    };
    /**
     *根据当前选中的页面设置按钮的选中
     */
    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
        @Override
        public void onPageSelected(int position) {
            index = position;//当前位置赋值给索引
            setCurrentDot(index%imageIds.length);
        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 设置对应位置按钮的状态
     * @param i 当前位置
     */
    private void setCurrentDot(int i) {
        if(group.getChildAt(i)!=null){
            group.getChildAt(i).setEnabled(false);//当前按钮选中
        }
        if(group.getChildAt(preIndex)!=null){
            group.getChildAt(preIndex).setEnabled(true);//上一个取消选中
            preIndex = i;//当前位置变为上一个，继续下次轮播
        }
    }
    PagerAdapter pagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            //返回一个比较大的值，目的是为了实现无限轮播
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position%imageIds.length;
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(imageIds[position]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            container.addView(imageView);
            mList.add(imageView);
            return imageView;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));

        }
    };

    @Override
    public void onPause() {
        timer.cancel();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        //重置车位数量
        ParkplaceAdapter parkplaceAdapter = new ParkplaceAdapter(getContext(),MainActivity.privateParkPlaceInfos);
        mListView.setAdapter(parkplaceAdapter);
        MainActivity.toolbar.setTitle("发布车位");
        index = 0;//从头开始播放
        viewPager.setCurrentItem(0);
        timer = new Timer();//创建Timer对象
        //执行定时任务
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //首先判断是否需要轮播，是的话我们才发消息
                if (isContinue) {
                    mHandler.sendEmptyMessage(1);
                }
            }
        },6000000,6000000);//延迟2秒，每隔2秒发一次消息
        super.onResume();
    }
}