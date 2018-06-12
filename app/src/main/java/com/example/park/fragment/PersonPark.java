package com.example.park.fragment;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.park.R;
import com.example.park.activities.MainActivity;
import com.example.park.activities.ReferParkPlaceActivity;
import com.example.park.adapter.HorizontalAdapter;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.listeners.HandleResponse;
import com.example.park.mymap.overlayutil.DrivingRouteOverlay;
import com.example.park.sensor.MyOrientationListener;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;
import com.example.park.util.LocationUtils;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.pedant.SweetAlert.widget.SweetAlertDialog;

public class PersonPark extends Fragment implements View.OnClickListener ,BaiduMap.OnMarkerClickListener,BaiduMap.OnMapClickListener{

    private String TAG = "PersonPark";

    //进行定位服务，同时可以远程寻车等
    private TextureMapView myMapView;
    private BaiduMap myBaiduMap;
    private float current;
    private View defaultBaiduMapScaleButton=null;
    private View defaultBaiduMapLogo=null;
    private View defaultBaiduMapScaleUnit=null;
    private ImageView addScale,lowScale;
    private ImageView myLoaction;
    private ImageView fullScreen;
    private LocationClient myLocationClient;
    private MyLocationListener myListener;
    private boolean isFirstIn=true;
    public static double latitude,longtitude;//定义的经度和纬度
    private BitmapDescriptor myBitmapLocation;
    private MyOrientationListener myOrientationListener;
    private float myCurrentX;

    // 路径规划搜索接口
    private RoutePlanSearch routePlanSearch;
    RecyclerView firstRecyclerView;
    //覆盖物相关
    private BitmapDescriptor myMark;
    private HorizontalAdapter firstAdapter;//recycler布局适配器
    View view;
    boolean is_performClick = true;//标记是否resume的时候更新地图信息

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplicationContext());
        view = inflater.inflate(R.layout.fragment_person_park, container, false);
        initMapView();
        changeDefaultBaiduMapView();
        initMapLocation();
        return view;
    }



    /**
     * @author zhongqihong
     * 修改默认百度地图的View
     *
     * */
    private void changeDefaultBaiduMapView() {
        changeInitialzeScaleView();//改变默认百度地图初始加载的地图比例
        //设置隐藏缩放和扩大的百度地图的默认的比例按钮
        for (int i = 0; i < myMapView.getChildCount(); i++) {//遍历百度地图中的所有子View,找到这个扩大和缩放的按钮控件View，然后设置隐藏View即可
            View child=myMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                defaultBaiduMapScaleButton=child;//该defaultBaiduMapScaleButton子View是指百度地图默认产生的放大和缩小的按钮，得到这个View
                break;
            }
        }
        defaultBaiduMapScaleButton.setVisibility(View.GONE);//然后将该View的Visiblity设为不存在和不可见，即隐藏
        defaultBaiduMapLogo =myMapView.getChildAt(1);//该View是指百度地图中默认的百度地图的Logo,得到这个View
        defaultBaiduMapLogo.setPadding(300, -10, 100, 100);//设置该默认Logo View的位置，因为这个该View的位置会影响下面的刻度尺单位View显示的位置
        myMapView.removeViewAt(1);//最后移除默认百度地图的logo View
        defaultBaiduMapScaleUnit=myMapView.getChildAt(2);//得到百度地图的默认单位刻度的View
        defaultBaiduMapScaleUnit.setPadding(100, 0, 115,200);//最后设置调整百度地图的默认单位刻度View的位置
    }
    /**
     * @author zhongqihong
     * 修改百度地图默认开始初始化加载地图比例大小
     * */
    private void changeInitialzeScaleView() {
        myBaiduMap=myMapView.getMap();//改变百度地图的放大比例,让首次加载地图就开始扩大到500米的距离
        MapStatusUpdate factory=MapStatusUpdateFactory.zoomTo(15.0f);
        myBaiduMap.animateMapStatus(factory);
        myBaiduMap.setOnMarkerClickListener(this);
        myBaiduMap.setOnMapClickListener(this);//这两个监听器设置在这边是因为这两个监听器需要借助mybaidumap这个变量，要对它进行初始化
    }
    /**
     * @author zhongqihong
     * 初始化地图
     * */
    private void initMapView() {
        registerAllViewsId();
        registerAllViewsEvent();

        handleResponse = new HandleResponse() {
            @Override
            public void getResponse(String response) {
                new SweetAlertDialog(view.getContext(),SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("车位口令："+response)
                        .setConfirmText("立即前往")
                        .setCancelText("取消预约")
                        .showCancelButton(true)
                        .setContentText("请牢记车位口令，一小时内有效")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
                                paramsList.add(new BasicNameValuePair("value", "1"));
                                new ConnectServer(paramsList, Constant.submitOrderUrl,handleResponse2).execute();
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog2) {
                                LatLng targetLocation = new LatLng(targetLatitude, targetLongitude);
                                planRoute();
                                driveSearch(targetLocation);
                                sweetAlertDialog2.dismiss();
                            }
                        }).show();
            }
        };
        handleResponse2 = new HandleResponse() {
            @Override
            public void getResponse(String response) {

            }
        };
    }
    /**
     * @author zhongqihong
     * 注册所有View相关的id
     * */
    private void registerAllViewsId() {
        myMapView=(TextureMapView) view.findViewById(R.id.person_mapview);
        addScale=(ImageView) view.findViewById(R.id.add_scale);
        lowScale=(ImageView) view.findViewById(R.id.low_scale);
        myLoaction=(ImageView) view.findViewById(R.id.my_location);
        fullScreen = (ImageView)view.findViewById(R.id.fullScreen_person);
        firstRecyclerView = (RecyclerView)view.findViewById(R.id.first_recycler_view);


    }

    public void setAdapter(){


        firstAdapter = new HorizontalAdapter(MainActivity.publishParkPlaceinfos);
        firstAdapter.setOnItemClickListener(new HorizontalAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                is_performClick = false;
                Intent intent = new Intent(getContext(), ReferParkPlaceActivity.class);
                intent.putExtra("position",position);
                startActivityForResult(intent,1);
            }
        });
        LinearLayoutManager firstManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        firstRecyclerView.setLayoutManager(firstManager);
    }



    /**
     * @author zhongqihong
     * 注册所有View相关的事件
     * */
    private void registerAllViewsEvent() {
        addScale.setOnClickListener(this);
        lowScale.setOnClickListener(this);
        myLoaction.setOnClickListener(this);
        fullScreen.setOnClickListener(this);
    }
    /**
     * @author zhongqihong
     * 初始化定位
     * */
    private void initMapLocation() {
        myLocationClient=new LocationClient(view.getContext());//创建一个定位客户端对象
        myListener=new MyLocationListener();//创建一个定位事件监听对象
        myLocationClient.registerLocationListener(myListener);//并给该定位客户端对象注册监听事件
        //对LocaitonClient进行一些必要的设置
        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");//设置坐标的类型
        option.setIsNeedAddress(true);//返回当前的位置信息，如果不设置为true，默认就为false，就无法获得位置的信息
        option.setOpenGps(true);//打开GPS
        option.setScanSpan(1000);//表示1s中进行一次定位请求
        myLocationClient.setLocOption(option);
        useLocationOrientationListener();//调用方向传感器
    }
    /**
     * @author zhongqihong
     * 定位结合方向传感器，从而可以实时监测到X轴坐标的变化，从而就可以检测到
     * 定位图标方向变化，只需要将这个动态变化的X轴的坐标更新myCurrentX值，
     * 最后在MyLocationData data.driection(myCurrentX);
     * */
    private void useLocationOrientationListener() {
        myOrientationListener=new MyOrientationListener(getContext());
        myOrientationListener.setMyOrientationListener(new MyOrientationListener.onOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {//监听方向的改变，方向改变时，需要得到地图上方向图标的位置
                myCurrentX=x;
            }
        });
    }
    /**
     * @author zhongqihong
     * 获得最新定位的位置,并且地图的中心点设置为我的位置
     * */
    private void getMyLatestLocation(double lat,double lng) {
        LatLng latLng=new LatLng(lat, lng);//创建一个经纬度对象，需要传入当前的经度和纬度两个整型值参数
        MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);//创建一个地图最新更新的状态对象，需要传入一个最新经纬度对象
        myBaiduMap.animateMapStatus(msu);//表示使用动画的效果传入，通过传入一个地图更新状态对象，然后利用百度地图对象来展现和还原那个地图更新状态，即此时的地图显示就为你现在的位置
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
                    .direction(myCurrentX)//方向
                    .latitude(location.getLatitude())//经度
                    .longitude(location.getLongitude())//纬度
                    .build();
            myBaiduMap.setMyLocationData(data);
            //配置自定义的定位图标,需要在紧接着setMyLocationData后面设置
            //调用自定义定位图标
            changeLocationIcon();
            latitude=location.getLatitude();//得到当前的经度
            longtitude=location.getLongitude();//得到当前的纬度

            //toast("经度："+latitude+"     纬度:"+longtitude);
            if (isFirstIn&&is_performClick) {//表示用户第一次打开，就定位到用户当前位置，即此时只要将地图的中心点设置为用户此时的位置即可
                getMyLatestLocation(latitude,longtitude);//获得最新定位的位置,并且地图的中心点设置为我的位置
                isFirstIn=false;//表示第一次才会去定位到中心点
                is_performClick = false;
                //加载停车位
                myLoaction.performClick();
                System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk---------------------");
                String locationTextString=""+location.getAddrStr();//这里得到地址必须需要在设置LocationOption的时候需要设置isNeedAddress为true;
                toast(locationTextString);
            }else{
                is_performClick = true;
                isFirstIn = false;
            }
        }
    }

    //添加覆盖物
    public void addMarkers(){
        myBaiduMap.clear();
        Marker marker;
        OverlayOptions options;
        LatLng mlocation;

        myMark=BitmapDescriptorFactory.fromResource(R.mipmap.mark);//引入自定义的覆盖物图标，将其转化成一个BitmapDescriptor对象
        for(int i = 0;i<MainActivity.publishParkPlaceinfos.size();i++){
            double mLatitude = MainActivity.publishParkPlaceinfos.get(i).getLatitude();
            double mLongitude = MainActivity.publishParkPlaceinfos.get(i).getLongitude();
            mlocation = new LatLng(mLatitude,mLongitude);
            String address = MainActivity.publishParkPlaceinfos.get(i).getAddress();
            options=new MarkerOptions().position(mlocation).icon(myMark).zIndex(6);
            marker=(Marker) myBaiduMap.addOverlay(options);
            Bundle bundle=new Bundle();
            bundle.putString("address",address);
            bundle.putDouble("latitude",mLatitude);
            bundle.putDouble("longitude",mLongitude);
            marker.setExtraInfo(bundle);
        }
        firstRecyclerView.setAdapter(firstAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 1:
                String info = data.getStringExtra("info");
                if(info.equals("go")){
                    double targetLatitude = data.getDoubleExtra("latitude",29);
                    double targetLongitude = data.getDoubleExtra("longitude",120);
                    LatLng targetLocation = new LatLng(targetLatitude, targetLongitude);
                    planRoute();
                    driveSearch(targetLocation);
                }
                break;
            default:
                break;
        }
    }

    /**
     * @author zhongqihong
     * 自定义定位图标
     * */
    private void changeLocationIcon() {
        myBitmapLocation= BitmapDescriptorFactory
                .fromResource(R.mipmap.direction);//引入自己的图标,这边的图标似乎转向不准
        if (isFirstIn) {//表示第一次定位显示普通模式
            MyLocationConfiguration config=new
                    MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, myBitmapLocation);
            myBaiduMap.setMyLocationConfigeration(config);
        }
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.add_scale:
                current+=0.5f;
                MapStatusUpdate
                        factory=MapStatusUpdateFactory.zoomTo(15.0f+current);
                myBaiduMap.animateMapStatus(factory);//设置百度地图的缩放效果动画animateMapStatus
                break;
            case R.id.low_scale:
                current-=0.5f;
                MapStatusUpdate factory2=MapStatusUpdateFactory.zoomTo(15.0f+current);
                myBaiduMap.animateMapStatus(factory2);
                break;
            case R.id.my_location://定位功能，需要用到LocationClient进行定位
                myBaiduMap.clear();
                getMyLatestLocation(latitude,longtitude);//获取最新的位置
                setAdapter();
                addMarkers();
                break;
            case R.id.fullScreen_person:
                if(firstRecyclerView.getVisibility()==View.VISIBLE){
                    firstRecyclerView.setVisibility(View.GONE);
                    fullScreen.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.partscreen));
                }else{
                    firstRecyclerView.setVisibility(View.VISIBLE);
                    fullScreen.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.fullscreen));
                }
                break;
            default:
                break;
        }
    }

    /**
     * 路径规划
     */
    public void planRoute(){
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch
                .setOnGetRoutePlanResultListener(routePlanResultListener);
    }

    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {

        //步行路线回调
        @Override
        public void onGetWalkingRouteResult(
                WalkingRouteResult walkingRouteResult) {
        }
        // 换成路线结果回调

        @Override
        public void onGetTransitRouteResult(
                TransitRouteResult transitRouteResult) {

        }
        // 驾车路线结果回调 查询的结果可能包括多条驾车路线方案
        @Override
        public void onGetDrivingRouteResult(
                DrivingRouteResult drivingRouteResult) {
            myBaiduMap.clear();
            if (drivingRouteResult == null
                    || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity(), "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                        myBaiduMap);
                drivingRouteOverlay.setData(drivingRouteResult.getRouteLines()
                        .get(0));// 设置一条驾车路线方案
                myBaiduMap.setOnMarkerClickListener(drivingRouteOverlay);
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();
                int totalLine = drivingRouteResult.getRouteLines().size();
              /*  Toast.makeText(getActivity(),
                        "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_LONG).show();*/
            }
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };

    // 驾车路线查询
    private void driveSearch(LatLng targetLocation) {
        LatLng beginLat = new LatLng(latitude,longtitude);
        DrivingRoutePlanOption drivingRoutePlanOption = new DrivingRoutePlanOption();
        drivingRoutePlanOption.from(PlanNode.withLocation(beginLat));
        drivingRoutePlanOption.to(PlanNode.withLocation(targetLocation));
        routePlanSearch.drivingSearch(drivingRoutePlanOption);
    }

    public void toast(String str){
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
    }




    private HandleResponse handleResponse;
    private HandleResponse handleResponse2;
    private String targetAddress;
    private double targetLongitude;
    private double targetLatitude;
    /**
     * @author zhongqihong
     * 添加好的覆盖物的点击事件
     * */
    @Override
    public boolean onMarkerClick(Marker marker) {//显示已点击后的覆盖物的详情
        //红色标记物的点击事件
        if(marker.getIcon()==myMark) {
            Bundle bundle = marker.getExtraInfo();
            targetAddress = bundle.getString("address");
            targetLongitude = bundle.getDouble("longitude");
            targetLatitude = bundle.getDouble("latitude");
            Toast.makeText(getActivity(), targetAddress, Toast.LENGTH_SHORT).show();

            new SweetAlertDialog(view.getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("是否前往此地？")
                    .setConfirmText("提交订单")
                    .setCancelText("取消")
                    .showCancelButton(true)
                    .setContentText(targetAddress)
                    .setCustomImage(R.mipmap.route)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if(Constant.is_login){
                                List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
                                paramsList.add(new BasicNameValuePair("value", "0"));
                                new ConnectServer(paramsList, Constant.submitOrderUrl,handleResponse).execute();
                                sweetAlertDialog.dismiss();
                            }else{
                                Toast.makeText(getActivity(),"请先登录再提交订单",Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .show();
        }
        return true;
    }

    /**
     * @author zhongqihong
     * 给整个地图添加的点击事件
     * */
    @Override
    public void onMapClick(LatLng arg0) {
        myBaiduMap.hideInfoWindow();//隐藏InfoWindow
    }

    @Override
    public boolean onMapPoiClick(MapPoi arg0) {
        // TODO Auto-generated method stub
        return false;
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
        //开启方向传感器
        myOrientationListener.start();
        super.onStart();
    }
    @Override
    public void onStop() {//当Activity调用onStop方法，关闭定位以及关闭方向传感器
        myBaiduMap.setMyLocationEnabled(false);
        myLocationClient.stop();//关闭定位
        myOrientationListener.stop();//关闭方向传感器
        super.onStop();
    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MainActivity.toolbar.setTitle("私人车位");
        //在Activity执行onResume是执行MapView(地图)生命周期管理
        myMapView.onResume();
    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        myMapView.onPause();
        isFirstIn = true;
        //isFirstIn = true;
    }

}
