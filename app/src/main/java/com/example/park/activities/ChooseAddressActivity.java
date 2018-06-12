package com.example.park.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.park.R;
import com.example.park.ui.MyToolbar;

public class ChooseAddressActivity extends BaseActivity {

    private BaiduMap myBaiduMap;
    private TextureMapView myMapView;
    private View defaultBaiduMapScaleButton=null;
    private LocationClient myLocationClient;
    private MyLocationListener myListener;
    private boolean isFirstIn=true;
    private double latitude,longtitude;//定义的经度和纬度
    public static MyToolbar toolbar;
    private TextView addressTextView;
    private Button confirmButton;
    private TextView detailAddress;
    private double centerLatitude,centerLongtitude;

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_choose_address);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initMapView();
        initMapLocation();
        changeInitialzeScaleView();
    }

    private void initMapView() {
        myMapView=(TextureMapView) findViewById(R.id.chooseAddress_map);
        toolbar = (MyToolbar) findViewById(R.id.addressToolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("选择车位地址");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        detailAddress = (TextView)findViewById(R.id.detailAddress);
        confirmButton = (Button)findViewById(R.id.saveButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent();
                mIntent.putExtra("address",addressTextView.getText());
                mIntent.putExtra("detailAddress",detailAddress.getText().toString());
                if(centerLatitude==0||centerLongtitude==0){
                    mIntent.putExtra("latitude",latitude);
                    mIntent.putExtra("longitude",longtitude);
                }else{
                    mIntent.putExtra("latitude",centerLatitude);
                    mIntent.putExtra("longitude",centerLongtitude);
                }
                ChooseAddressActivity.this.setResult(4,mIntent);
                finish();
            }
        });
        myBaiduMap=myMapView.getMap();//改变百度地图的放大比例,让首次加载地图就开始扩大到500米的距离
        addressTextView = (TextView)findViewById(R.id.address);

        myBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                int mapAction=motionEvent.getAction();
                //在点击手势ACTION_UP里面进行检索信息
                if (mapAction==MotionEvent.ACTION_UP) {
                    MapStatus mapStatus= myBaiduMap.getMapStatus();
                    LatLng lng= mapStatus.target;
                    GeoCoder mGeoCoder=GeoCoder.newInstance();
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(lng));
                    mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                        @Override
                        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
                            if (arg0 == null || arg0.error != SearchResult.ERRORNO.NO_ERROR) {

                            }else {
                                String addr=arg0.getAddress();
                                centerLatitude = arg0.getLocation().latitude;
                                centerLongtitude = arg0.getLocation().longitude;
                                if(addr!=null){
                                    addressTextView.setText(addr);
                                }
                            }
                        }
                        @Override
                        public void onGetGeoCodeResult(GeoCodeResult arg0) {
                        }
                    });
                    //在点击手势ACTION_MOVE里面进行提示信息，如定位中等等
                }else if (mapAction==MotionEvent.ACTION_MOVE) {
                    addressTextView.setText("定位中,请稍等");
                }
            }
        });
    }

    /**
     * @author zhongqihong
     * 初始化定位
     * */
    private void initMapLocation() {
        myLocationClient=new LocationClient(this);//创建一个定位客户端对象
        myListener=new MyLocationListener();//创建一个定位事件监听对象
        myLocationClient.registerLocationListener(myListener);//并给该定位客户端对象注册监听事件
        //对LocaitonClient进行一些必要的设置
        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");//设置坐标的类型
        option.setIsNeedAddress(true);//返回当前的位置信息，如果不设置为true，默认就为false，就无法获得位置的信息
        option.setOpenGps(true);//打开GPS
        option.setScanSpan(1000);//表示1s中进行一次定位请求
        myLocationClient.setLocOption(option);
    }

    /**
     * @author zhongqihong
     * 修改百度地图默认开始初始化加载地图比例大小
     * */
    private void changeInitialzeScaleView() {
        MapStatusUpdate factory=MapStatusUpdateFactory.zoomTo(15.0f);
        myBaiduMap.animateMapStatus(factory);
        for (int i = 0; i < myMapView.getChildCount(); i++) {//遍历百度地图中的所有子View,找到这个扩大和缩放的按钮控件View，然后设置隐藏View即可
            View child=myMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                defaultBaiduMapScaleButton=child;//该defaultBaiduMapScaleButton子View是指百度地图默认产生的放大和缩小的按钮，得到这个View
                break;
            }
        }
        defaultBaiduMapScaleButton.setVisibility(View.GONE);//然后将该View的Visiblity设为不存在和不可见，即隐藏
    }

    private void getMyLatestLocation(double lat,double lng) {
        LatLng latLng=new LatLng(lat, lng);//创建一个经纬度对象，需要传入当前的经度和纬度两个整型值参数
        MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);//创建一个地图最新更新的状态对象，需要传入一个最新经纬度对象
        myBaiduMap.animateMapStatus(msu);//表示使用动画的效果传入，通过传入一个地图更新状态对象，然后利用百度地图对象来展现和还原那个地图更新状态，即此时的地图显示就为你现在的位置
    }

    public void onDestroy() {
        super.onDestroy();
        //在Activity执行onDestory时执行mapView(地图)生命周期管理
        myMapView.onDestroy();
    }
    @Override
    public void onStart() {//当Activity调用onStart方法，开启定位以及开启方向传感器，即将定位的服务、方向传感器和Activity生命周期绑定在一起
        myBaiduMap.setMyLocationEnabled(true);//开启允许定位
        if (!myLocationClient.isStarted()) {
            myLocationClient.start();//开启定位
        }
        super.onStart();
    }
    @Override
    public void onStop() {//当Activity调用onStop方法，关闭定位以及关闭方向传感器
        myBaiduMap.setMyLocationEnabled(false);
        myLocationClient.stop();//关闭定位
        super.onStop();
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //在Activity执行onResume是执行MapView(地图)生命周期管理
        myMapView.onResume();
    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        myMapView.onPause();
    }
    /**
     * @author zhongqihong
     * BDLocationListener的实现类:MyLocationListener
     * */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation
                                              location) {
            //得到一个MyLocationData对象，需要将BDLocation对象转换成MyLocationData对象
            MyLocationData data=new MyLocationData.Builder()
                    .accuracy(location.getRadius())//精度半径
                    .latitude(location.getLatitude())//经度
                    .longitude(location.getLongitude())//纬度
                    .build();
            myBaiduMap.setMyLocationData(data);
            latitude=location.getLatitude();//得到当前的经度
            longtitude=location.getLongitude();//得到当前的纬度
            //toast("经度："+latitude+"     纬度:"+longtitude);
            if (isFirstIn) {//表示用户第一次打开，就定位到用户当前位置，即此时只要将地图的中心点设置为用户此时的位置即可
                String address=""+location.getAddrStr();//这里得到地址必须需要在设置LocationOption的时候需要设置isNeedAddress为true;
                addressTextView.setText(address);
                getMyLatestLocation(latitude,longtitude);//获得最新定位的位置,并且地图的中心点设置为我的位置
                isFirstIn=false;//表示第一次才会去定位到中心点
            }
        }

    }
}
