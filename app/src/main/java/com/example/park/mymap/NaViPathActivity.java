package com.example.park.mymap;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.example.park.domain.EndInfo;
import com.example.park.domain.SearchInfo;
import com.example.park.domain.StartInfo;
import com.example.park.fragment.PublicPark;
import com.example.park.util.SystemStatusManager;

import java.io.File;
import java.util.ArrayList;
import com.example.park.R;

public class NaViPathActivity extends Activity implements android.view.View.OnClickListener{
	private String mSdcardPath=null;
	private static final String APP_FOLDER_NAME="mikyouPath";
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	private String authinfo = null;

	private TextView mStartTv;
	private TextView mEndTv;
	private SearchInfo info=null;
	private StartInfo sInfo=new StartInfo();
	private EndInfo eInfo=new EndInfo();
	//
	private TextView driveTextView;
	private TextView walkTextView;
	private TextView busTextView;
	private TextView rideTextView;

	private MapView pathMapView;
	private TextView startSearch;
	private RelativeLayout selectAddressRelativeLayout;
	//用来判定是否选择了初始地，如果没有选择，则使用当前位置
	private boolean chooseStart = false;
	//用来判定行走模式（驾车，步行，公交等）
	private int mode = 0;

	public static final int DRIVE_MODE = 0;
	public static final int RIDE_MODE = 3;
	public static final int WALK_MODE = 2;
	public static final int BUS_MODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTranslucentStatus();
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_na_vi_path);
		initSdcardPath();//先获得SD卡的路径
		initView();
	}
	private void initView() {
		registerAllViewId();
		registerAllViewEvent();
	}
	private void registerAllViewEvent() {
		mStartTv.setOnClickListener(this);
		mEndTv.setOnClickListener(this);
		startSearch.setOnClickListener(this);
		driveTextView.setOnClickListener(this);
		rideTextView.setOnClickListener(this);
		walkTextView.setOnClickListener(this);
		busTextView.setOnClickListener(this);
	}
	private void registerAllViewId() {
		mStartTv=(TextView) findViewById(R.id.start);
		mEndTv=(TextView) findViewById(R.id.end);
		startSearch=(TextView) findViewById(R.id.include_navi).findViewById(R.id.start_search);
		selectAddressRelativeLayout=(RelativeLayout) findViewById(R.id.relative_select_address);
		driveTextView = (TextView)findViewById(R.id.include_navi).findViewById(R.id.driveTextView);
		walkTextView = (TextView)findViewById(R.id.include_navi).findViewById(R.id.walkTextView);
		rideTextView = (TextView)findViewById(R.id.include_navi).findViewById(R.id.rideTextView);
		busTextView = (TextView)findViewById(R.id.include_navi).findViewById(R.id.busTextView);
	}

	private void initSdcardPath() {
		if (initDir()) {
			initNaviPath();
		}		
	}

	private boolean initDir() {//创建一个文件夹用于保存在路线导航过程中语音导航语音文件的缓存，防止用户再次开启同样的导航直接从缓存中读取即可
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			mSdcardPath= Environment.getExternalStorageDirectory().toString();
		}else{
			mSdcardPath=null;
		}
		if (mSdcardPath==null) {
			return false;
		}
		File file=new File(mSdcardPath,APP_FOLDER_NAME);
		if (!file.exists()) {
			try {
				file.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		Toast.makeText(NaViPathActivity.this, mSdcardPath, Toast.LENGTH_SHORT).show();
		return true;
	}

	private void initNaviPath() {//初始化导航路线的导航引擎
		BNOuterTTSPlayerCallback ttsCallback = null;
		BaiduNaviManager.getInstance().init(NaViPathActivity.this, mSdcardPath, APP_FOLDER_NAME, new NaviInitListener() {

			@Override
			public void onAuthResult(int status, String msg) {
				if (status==0) {
					authinfo = "key校验成功!";
				}else{
					authinfo = "key校验失败!"+msg;
				}
				NaViPathActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(NaViPathActivity.this, authinfo, Toast.LENGTH_LONG).show();
					}
				});
			}

			@Override
			public void initSuccess() {
				Toast.makeText(NaViPathActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_LONG).show();
			}

			@Override
			public void initStart() {
				Toast.makeText(NaViPathActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_LONG).show();
			}

			@Override
			public void initFailed() {
				Toast.makeText(NaViPathActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_LONG).show();
			}
		}, ttsCallback);
	}

	private void setTranslucentStatus() {
		if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
			Window win=getWindow();
			WindowManager.LayoutParams winParams=win.getAttributes();
			final int bits= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |=bits;
			win.setAttributes(winParams);
		}
		SystemStatusManager tintManager = new SystemStatusManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0);
		tintManager.setNavigationBarTintEnabled(true);		
	}

	@Override
	public void onClick(View v) {
		Intent intent=new Intent(NaViPathActivity.this, SelectAddressActivity.class);
		switch (v.getId()) {
		case R.id.start:
			chooseStart = true;
			startActivityForResult(intent, 0);//用于得到另外一个Activity返回起点地点信息
			break;
		case R.id.end:
			startActivityForResult(intent,1);//用于得到另外一个Activity返回终点地点信息
			break;
		case R.id.start_search:
			if (sInfo!=null&&eInfo!=null) {//如果起点和终点信息都不为空的时候开启路线规划得到最佳路径途径的所有节点信息
				switch (mode){
					case 0:
						//驾车导航
						initBNRoutePlan(sInfo,eInfo);
						break;
					case 1:
						System.out.println("公交前往");
						break;
					case 2:
						System.out.println("步行前往");
						break;
					case 3:
						System.out.println("骑车前往");
						break;
				}

			}
			break;
		case R.id.driveTextView:
			driveTextView.setTextColor(getResources().getColor(R.color.fore_white));
			rideTextView.setTextColor(getResources().getColor(R.color.back_white));
			walkTextView.setTextColor(getResources().getColor(R.color.back_white));
			busTextView.setTextColor(getResources().getColor(R.color.back_white));
			mode = DRIVE_MODE;
			break;
		case R.id.rideTextView:
			driveTextView.setTextColor(getResources().getColor(R.color.back_white));
			rideTextView.setTextColor(getResources().getColor(R.color.fore_white));
			walkTextView.setTextColor(getResources().getColor(R.color.back_white));
			busTextView.setTextColor(getResources().getColor(R.color.back_white));
			mode = RIDE_MODE;
			break;
		case R.id.walkTextView:
			driveTextView.setTextColor(getResources().getColor(R.color.back_white));
			rideTextView.setTextColor(getResources().getColor(R.color.back_white));
			walkTextView.setTextColor(getResources().getColor(R.color.fore_white));
			busTextView.setTextColor(getResources().getColor(R.color.back_white));
			mode = WALK_MODE;
			break;
		case R.id.busTextView:
			driveTextView.setTextColor(getResources().getColor(R.color.back_white));
			rideTextView.setTextColor(getResources().getColor(R.color.back_white));
			walkTextView.setTextColor(getResources().getColor(R.color.back_white));
			busTextView.setTextColor(getResources().getColor(R.color.fore_white));
			mode = BUS_MODE;
			break;
		default:
			break;
		}		
	}
	private void initBNRoutePlan(StartInfo mySInfo,EndInfo myEndInfo) {
		BNRoutePlanNode startNode = null;
		if(chooseStart){
			startNode=new BNRoutePlanNode(mySInfo.getLongtiude(), mySInfo.getLatitude(), mySInfo.getDesname(), null, CoordinateType.BD09LL);//根据得到的起点的信息创建起点节点
		}
		else {
			startNode=new BNRoutePlanNode(PublicPark.longtitude, PublicPark.latitude, mySInfo.getDesname(), null, CoordinateType.BD09LL);//默认自己的位置
		}

		BNRoutePlanNode endNode=new BNRoutePlanNode(myEndInfo.getLongtiude(), myEndInfo.getLatitude(), myEndInfo.getDesname(),null, CoordinateType.BD09LL);//根据得到的终点的信息创建终点节点
		if (startNode!=null&&endNode!=null) {
			ArrayList<BNRoutePlanNode> list=new ArrayList<BNRoutePlanNode>();
			list.add(startNode);//将起点和终点加入节点集合中
			list.add(endNode);
			BaiduNaviManager.getInstance().launchNavigator(NaViPathActivity.this, list, 1, true, new MyRoutePlanListener(list) );
		}
	}
	class MyRoutePlanListener implements RoutePlanListener {//路线规划监听器接口类
		private ArrayList<BNRoutePlanNode> mList=null;

		public MyRoutePlanListener(ArrayList<BNRoutePlanNode> list) {
			mList = list;
		}

		@Override
		public void onJumpToNavigator() {
			Intent intent=new Intent(NaViPathActivity.this, PathGuideActivity.class);
			intent.putExtra(ROUTE_PLAN_NODE, mList);//将得到所有的节点集合传入到导航的Activity中去
		   startActivity(intent);
		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub

		}

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==0&&resultCode==0) {//设置起点
			SearchInfo myInfo=	(SearchInfo) data.getSerializableExtra("info");
			sInfo.setAddress(myInfo.getAddress());
			sInfo.setDesname(myInfo.getDesname());
			sInfo.setLatitude(myInfo.getLatitude());
			sInfo.setLongtiude(myInfo.getLongtiude());
			sInfo.setStreetId(myInfo.getStreetId());
			sInfo.setUid(myInfo.getUid());
			mStartTv.setText(myInfo.getDesname());
		}
		if (requestCode==1&&resultCode==0) {//设置终点
			SearchInfo myInfo=	(SearchInfo) data.getSerializableExtra("info");
			eInfo.setAddress(myInfo.getAddress());
			eInfo.setDesname(myInfo.getDesname());
			eInfo.setLatitude(myInfo.getLatitude());
			eInfo.setLongtiude(myInfo.getLongtiude());
			eInfo.setStreetId(myInfo.getStreetId());
			eInfo.setUid(myInfo.getUid());
			mEndTv.setText(myInfo.getDesname());
		}
	}
}
