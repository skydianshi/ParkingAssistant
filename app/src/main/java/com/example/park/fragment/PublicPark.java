package com.example.park.fragment;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
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
import com.example.park.adapter.VegaLayoutManager;
import com.example.park.adapter.VerticalAdapter;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.domain.SearchInfo;
import com.example.park.domain.StockEntity;
import com.example.park.listeners.HandleResponse;
import com.example.park.sensor.MyOrientationListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.park.sensor.MyOrientationListener.onOrientationListener;

import cn.pedant.SweetAlert.widget.SweetAlertDialog;
import com.example.park.mymap.overlayutil.DrivingRouteOverlay;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;
import com.example.park.util.LocationUtils;

import org.apache.http.message.BasicNameValuePair;

public class PublicPark extends Fragment implements View.OnClickListener,BaiduMap.OnMarkerClickListener,BaiduMap.OnMapClickListener {
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
    //定位相关
    private PoiSearch poiSearch;
    private LocationClient myLocationClient;
    private MyLocationListener myListener;
    private boolean isFirstIn=true;
    public static double latitude,longtitude;//定义的经度和纬度
    private BitmapDescriptor myBitmapLocation;
    private MyOrientationListener myOrientationListener;
    private float myCurrentX;
    //覆盖物相关
    private BitmapDescriptor myMark;
    private BitmapDescriptor greenMark;//绿标
    private String currentAddress;
    //private LinearLayout markLayout;
    //线路导航
    //private ImageButton startGo;
    // 路径规划搜索接口
    private RoutePlanSearch routePlanSearch;
    //用来存储蓝色标记的信息，从而响应点击事件
    SearchInfo blueMarkerInfo = null;
    RecyclerView recyclerView;
    private List<StockEntity> dataList = new ArrayList<>();//recyclerView数据集
    LocationUtils locationUtils;
    VerticalAdapter adapter;
    private HandleResponse handleResponse;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplicationContext());
        view = inflater.inflate(R.layout.fragment_public_park, container, false);
        initMapView();
        changeDefaultBaiduMapView();
        initMapLocation();
        updateFigures();
        return view;
    }

    //提前根据自身发布车位的情况及时更新车位信息，防止加载来不及
    public void updateFigures(){
        MainActivity.publishParkPlaceinfos.clear();
        MainActivity.publishParkPlaceinfos.add(new ParkPlaceInfo(MainActivity.publishParkPlaceinfos.size(),"重庆市沙坪坝区沙正街174号","01311200","1","1号私人停车场",29.557353059389516 ,106.48431766268939,"张三","12345678","01311300"," ", BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.park1),"请注意保持卫生，否则后果自负！"));
        MainActivity.publishParkPlaceinfos.add(new ParkPlaceInfo(MainActivity.publishParkPlaceinfos.size(),"重庆市沙坪坝区沙正街174号","01311400","1","2号私人停车场",29.557353059389516 ,106.48431766268939,"李四","12345678","01310600"," ", BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.park2),"请按时停车，谢谢配合！"));
        MainActivity.publishParkPlaceinfos.add(new ParkPlaceInfo(MainActivity.publishParkPlaceinfos.size(),"重庆市沙坪坝区沙正街174号","01312200","1","3号私人停车场",29.557353059389516 ,106.48431766268939,"王五","12345678","02010800"," ", BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.park4),"交个朋友吧，亲！"));
        MainActivity.publishParkPlaceinfos.add(new ParkPlaceInfo(MainActivity.publishParkPlaceinfos.size(),"重庆市沙坪坝区沙正街174号","02010000","1","4号私人停车场",29.557353059389516 ,106.48431766268939,"丁六","12345678","02012200"," ", BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.park3),"欢迎下次再来，谢谢光临！"));

        List<BasicNameValuePair> paramsList = new ArrayList<>();
        paramsList.add(new BasicNameValuePair("MODE", "getpublish"));
        new ConnectServer(paramsList, Constant.handleRequestUrl,handleResponse).execute();
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
        //配置自定义的定位图标,需要在紧接着setMyLocationData后面设置
        //调用自定义定位图标
        changeLocationIcon();
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
        flag = true;
        super.onStop();
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //在Activity执行onResume是执行MapView(地图)生命周期管理
        isFirstIn = true;//再次进入界面的时候重新定位，重新扫描停车位
        myMapView.onResume();
        //myLoaction.performClick();
        getMyLatestLocation(latitude,longtitude);//获取最新的位置
    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        myMapView.onPause();
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
                if(response.length()!=0){
                    response = response.trim();
                    String [] results = response.split("\\+");
                    for(int i = 0;i<results.length;i++){
                        String [] charactors = results[i].split("-");
                        ParkPlaceInfo parkPlace = new ParkPlaceInfo(charactors[0],charactors[1],charactors[2],charactors[3],charactors[4],charactors[5],charactors[6],charactors[7],charactors[8],charactors[9],Double.valueOf(charactors[10]),Double.valueOf(charactors[11]),
                                charactors[12],Integer.valueOf(charactors[13]),Boolean.valueOf(charactors[14]),Integer.valueOf(charactors[15]),Integer.valueOf(charactors[16]));
                        MainActivity.publishParkPlaceinfos.add(parkPlace);
                    }
                }
            }
        };
    }

    //计算私人车位距离自己的distance
    public void setPersonParkplaceDistance(){
        LocationUtils locationUtils = new LocationUtils();
        MainActivity.publishParkPlaceinfos.get(0).setLatitude(latitude+0.005);
        MainActivity.publishParkPlaceinfos.get(0).setLongitude(longtitude+0.003);
        MainActivity.publishParkPlaceinfos.get(0).setAddress(currentAddress+"");
        MainActivity.publishParkPlaceinfos.get(0).setDetailAddress("东南角");
        MainActivity.publishParkPlaceinfos.get(1).setLatitude(latitude-0.01);
        MainActivity.publishParkPlaceinfos.get(1).setLongitude(longtitude-0.01);
        MainActivity.publishParkPlaceinfos.get(1).setAddress(currentAddress+"");
        MainActivity.publishParkPlaceinfos.get(1).setDetailAddress("西南角");
        MainActivity.publishParkPlaceinfos.get(2).setLatitude(latitude+0.007);
        MainActivity.publishParkPlaceinfos.get(2).setLongitude(longtitude-0.05);
        MainActivity.publishParkPlaceinfos.get(2).setAddress(currentAddress+"");
        MainActivity.publishParkPlaceinfos.get(2).setDetailAddress("西北角");
        MainActivity.publishParkPlaceinfos.get(3).setLatitude(latitude-0.001);
        MainActivity.publishParkPlaceinfos.get(3).setLongitude(longtitude+0.008);
        MainActivity.publishParkPlaceinfos.get(3).setAddress(currentAddress+"");
        MainActivity.publishParkPlaceinfos.get(3).setDetailAddress("东北角");
        for(int i = 0;i<MainActivity.publishParkPlaceinfos.size();i++){
            MainActivity.publishParkPlaceinfos.get(i).setDistance(locationUtils.getDistance(MainActivity.publishParkPlaceinfos.get(i).getLatitude(),MainActivity.publishParkPlaceinfos.get(i).getLongitude(), latitude,longtitude));
        }
        Iterator i = MainActivity.publishParkPlaceinfos.iterator();
        while (i.hasNext()){
            ParkPlaceInfo p = (ParkPlaceInfo)i.next();
            if(p.getDistance()>10000){
                if(!(latitude==0&&longtitude==0)){
                    i.remove();
                }
            }
        }
    }
    /**
     * @author zhongqihong
     * 注册所有View相关的id
     * */
    private void registerAllViewsId() {
        myMapView=(TextureMapView) view.findViewById(R.id.mymap_view);
        addScale=(ImageView) view.findViewById(R.id.add_scale);
        lowScale=(ImageView) view.findViewById(R.id.low_scale);
        myLoaction=(ImageView) view.findViewById(R.id.my_location);
        fullScreen = (ImageView)view.findViewById(R.id.fullScreen);
        //markLayout=(LinearLayout) view.findViewById(R.id.mark_layout);
        //startGo=(ImageButton) view.findViewById(R.id.start_go);

        recyclerView = (RecyclerView) view.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new VegaLayoutManager());
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
        //startGo.setOnClickListener(this);
    }
    /**
     * @author zhongqihong
     * 初始化定位
     * */
    private void initMapLocation() {
        locationUtils = new LocationUtils();
        poiSearch = PoiSearch.newInstance();
        // 设置检索监听器
        poiSearch.setOnGetPoiSearchResultListener(poiSearchListener);
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
        myOrientationListener.setMyOrientationListener(new onOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {//监听方向的改变，方向改变时，需要得到地图上方向图标的位置
                myCurrentX=x;
            }
        });
    }

    boolean flag = true;//标记私人停车位信息每次进入界面只更新一次
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
            latitude=location.getLatitude();//得到当前的经度
            longtitude=location.getLongitude();//得到当前的纬度
            if(latitude!=0&&longtitude!=0&&flag){//只有得到当前位置的时候才通知更新私人车位信息并且只更新一次
                currentAddress = location.getAddrStr();
                setPersonParkplaceDistance();
                flag = false;
            }
            //toast("经度："+latitude+"     纬度:"+longtitude);
            if (isFirstIn) {//表示用户第一次打开，就定位到用户当前位置，即此时只要将地图的中心点设置为用户此时的位置即可

                getMyLatestLocation(latitude,longtitude);//获得最新定位的位置,并且地图的中心点设置为我的位置
                isFirstIn=false;//表示第一次才会去定位到中心点
                //加载停车位
                nearbySearch(0);//第一次获取到位置之后将周围的停车场加入recyclerView和mapView，之后就不再允许修改
                String locationTextString=""+location.getAddrStr();//这里得到地址必须需要在设置LocationOption的时候需要设置isNeedAddress为true;
                toast(locationTextString);
            }
        }

    }
    /**
     * @author zhongqihong
     * 自定义定位图标
     * */
    private void changeLocationIcon() {
        myBitmapLocation=BitmapDescriptorFactory
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
                MapStatusUpdate
                        factory2=MapStatusUpdateFactory.zoomTo(15.0f+current);
                myBaiduMap.animateMapStatus(factory2);
                break;
            case R.id.my_location://定位功能，需要用到LocationClient进行定位
                //BDLocationListener
                //myBaiduMap.clear();
                //nearbySearch(0);
                getMyLatestLocation(latitude,longtitude);//获取最新的位置
                addMarker();
                break;
            case R.id.fullScreen:
                if(recyclerView.getVisibility()==View.VISIBLE){
                    recyclerView.setVisibility(View.GONE);
                    fullScreen.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.partscreen));
                }else{
                    recyclerView.setVisibility(View.VISIBLE);
                    fullScreen.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.fullscreen));
                }
                break;
          /*  case R.id.start_go:
                Intent intent2=new Intent(getActivity(), NaViPathActivity.class);
                startActivity(intent2);
                break;*/
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
            /*    Toast.makeText(getActivity(),
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


    /**
     * 附近检索停车场
     */
    private void nearbySearch(int page) {
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.location(new LatLng(latitude, longtitude));
        nearbySearchOption.keyword("停车场");
        nearbySearchOption.radius(3000);// 检索半径，单位是米
        nearbySearchOption.pageNum(page);
        nearbySearchOption.pageCapacity(30);//每页的容量
        poiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求

    }

    //POI检索的回调，将找到的结果返回
    OnGetPoiSearchResultListener poiSearchListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null
                    || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                Toast.makeText(getActivity(), "未找到结果",
                        Toast.LENGTH_LONG).show();
                return;
            }
            else{// 检索结果正常返回
                myBaiduMap.clear();
                dataList.clear();
                List<PoiInfo> locations = poiResult.getAllPoi();
                if(locations==null){
                    return;
                }
                Marker marker;
                OverlayOptions options;
                LatLng location = null;
                myMark=BitmapDescriptorFactory.fromResource(R.mipmap.mark);//引入自定义的覆盖物图标，将其转化成一个BitmapDescriptor对象
                greenMark=BitmapDescriptorFactory.fromResource(R.mipmap.mark_green);
                for(int i = 0;i<locations.size();i++){
                    location = locations.get(i).location;
                    String name = locations.get(i).name;
                    String address = locations.get(i).address;
                    double lLatitude = location.latitude;
                    double lLongitude = location.longitude;
                    int distance = (int)locationUtils.getDistance(latitude,longtitude,lLatitude,lLongitude);
                    StockEntity data = new StockEntity(name,address,lLatitude,lLongitude,distance);
                    dataList.add(data);

                    if(i%3==0){
                        options=new MarkerOptions().position(location).icon(greenMark).zIndex(6);
                        marker=(Marker) myBaiduMap.addOverlay(options);
                        Bundle bundle=new Bundle();
                        //bundle.putSerializable("mark", locations.get(i));
                        bundle.putString("address",locations.get(i).address);
                        bundle.putDouble("latitude",locations.get(i).location.latitude);
                        bundle.putDouble("longitude",locations.get(i).location.longitude);
                        bundle.putString("city",locations.get(i).city);
                        marker.setExtraInfo(bundle);
                    }else{
                        options=new MarkerOptions().position(location).icon(myMark).zIndex(6);
                        marker=(Marker) myBaiduMap.addOverlay(options);
                        Bundle bundle=new Bundle();
                        //bundle.putSerializable("mark", locations.get(i));
                        bundle.putString("address",locations.get(i).address);
                        bundle.putDouble("latitude",locations.get(i).location.latitude);
                        bundle.putDouble("longitude",locations.get(i).location.longitude);
                        bundle.putString("city",locations.get(i).city);
                        marker.setExtraInfo(bundle);
                    }
                }
                adapter = new VerticalAdapter(dataList);
                System.out.println(dataList.size());
                System.out.println(dataList.get(5));
                adapter.setOnItemClickListener(new VerticalAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        LatLng targetLatLng = new LatLng(dataList.get(position).getLatitude(),dataList.get(position).getLongitude());
                        planRoute();
                        driveSearch(targetLatLng);
                    }
                });
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();//这里不知道为啥不更新recyclerView，所以直接不让它更新了，下下策把添加覆盖物重新写了个方法
                /*MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(location);
                myBaiduMap.animateMapStatus(msu);*/
                //int totalPage = poiResult.getTotalPageNum();// 获取总分页数
                /*Toast.makeText(
                        getActivity(),
                        "总共查到" + poiResult.getTotalPoiNum() + "个兴趣点, 分为"
                                + totalPage + "页", Toast.LENGTH_SHORT).show();*/

            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity(), "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            } else {// 正常返回结果的时候，此处可以获得很多相关信息
                Toast.makeText(
                        getActivity(),
                        poiDetailResult.getName() + ": "
                                + poiDetailResult.getAddress(),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    //添加覆盖物
    public void addMarker(){
        if(dataList!=null){
            myBaiduMap.clear();
            Marker marker;
            OverlayOptions options;
            LatLng location = null;
            myMark=BitmapDescriptorFactory.fromResource(R.mipmap.mark);//引入自定义的覆盖物图标，将其转化成一个BitmapDescriptor对象
            greenMark=BitmapDescriptorFactory.fromResource(R.mipmap.mark_green);
            for(int i = 0;i < dataList.size();i++){
                location = new LatLng(dataList.get(i).getLatitude(),dataList.get(i).getLongitude());
                if(i%3==0){
                    options=new MarkerOptions().position(location).icon(greenMark).zIndex(6);
                }else{
                    options=new MarkerOptions().position(location).icon(myMark).zIndex(6);
                }
                marker=(Marker) myBaiduMap.addOverlay(options);
                Bundle bundle=new Bundle();
                //bundle.putSerializable("mark", locations.get(i));
                bundle.putString("address",dataList.get(i).getAddress());
                bundle.putDouble("latitude",dataList.get(i).getLatitude());
                bundle.putDouble("longitude",dataList.get(i).getLongitude());
                marker.setExtraInfo(bundle);
            }
        }

    }


    public void toast(String str){
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
    }
    /**
     * @author zhongqihong
     * 添加好的覆盖物的点击事件
     * */
    @Override
    public boolean onMarkerClick(Marker marker) {//显示已点击后的覆盖物的详情
        //红色标记物的点击事件
        if(marker.getIcon()==myMark) {
            Bundle bundle = marker.getExtraInfo();
            final String targetAddress = bundle.getString("address");
            final double targetLongitude = bundle.getDouble("longitude");
            final double targetLatitude = bundle.getDouble("latitude");
            final String targetCity = bundle.getString("city");
            Toast.makeText(getActivity(), targetAddress, Toast.LENGTH_SHORT).show();

            new SweetAlertDialog(view.getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("是否前往此地？")
                    .setConfirmText("确定")
                    .setCancelText("取消")
                    .showCancelButton(true)
                    .setContentText(targetAddress)
                    .setCustomImage(R.mipmap.route)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            LatLng targetLocation = new LatLng(targetLatitude, targetLongitude);
                            planRoute();
                            driveSearch(targetLocation);
                            sweetAlertDialog.dismiss();
                        }
                    })
                    .show();
        }
        else if(marker.getIcon()==greenMark) {
            Bundle bundle = marker.getExtraInfo();
            final String targetAddress = bundle.getString("address");
            final double targetLongitude = bundle.getDouble("longitude");
            final double targetLatitude = bundle.getDouble("latitude");
            final String targetCity = bundle.getString("city");
            Toast.makeText(getActivity(), targetAddress, Toast.LENGTH_SHORT).show();

            new SweetAlertDialog(view.getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("是否前往该充电桩？")
                    .setConfirmText("确定")
                    .setCancelText("取消")
                    .showCancelButton(true)
                    .setContentText(targetAddress)
                    .setCustomImage(R.mipmap.route)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            LatLng targetLocation = new LatLng(targetLatitude, targetLongitude);
                            planRoute();
                            driveSearch(targetLocation);
                            sweetAlertDialog.dismiss();
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
    public void onMapClick(LatLng arg0) {//表示点击地图其他的地方使得覆盖物的详情介绍的布局隐藏，但是点击已显示的覆盖物详情布局上，则不会消失，因为在详情布局上添加了Clickable=true
        //由于事件的传播机制，因为点击事件首先会在覆盖物布局的父布局(map)中,由于map是可以点击的，map则会把点击事件给消费掉，如果加上Clickable=true表示点击事件由详情布局自己处理，不由map来消费
        //markLayout.setVisibility(View.GONE);
        myBaiduMap.hideInfoWindow();//隐藏InfoWindow
    }

    @Override
    public boolean onMapPoiClick(MapPoi arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}