package com.example.park.activities;



import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.example.park.R;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.fragment.InfoFragment;
import com.example.park.fragment.ParkFragment;
import com.example.park.fragment.PublishFragment;
import com.example.park.ui.MyToolbar;

import java.util.ArrayList;


/**
 * @author yangyu
 *  功能描述：自定义TabHost
 */
public class MainActivity extends BaseActivity{
    public static ArrayList<ParkPlaceInfo> privateParkPlaceInfos = new ArrayList<>();
    public static ArrayList<ParkPlaceInfo> publishParkPlaceinfos = new ArrayList<>();
    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;
    public static MyToolbar toolbar;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class fragmentArray[] = {ParkFragment.class,PublishFragment.class,InfoFragment.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_search_btn,R.drawable.tab_message_btn,
            R.drawable.tab_selfinfo_btn};

    //Tab选项卡的文字
    private String mTextviewArray[] = {"找车位", "发布车位", "个人中心"};

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_main);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initView();
    }

    /**
     * 初始化组件
     */
    private void initView(){
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        //实例化布局对象
        layoutInflater = LayoutInflater.from(this);

        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        //得到fragment的个数
        int count = fragmentArray.length;

        for(int i = 0; i < count; i++){
            //为每一个Tab按钮设置图标、文字和内容
            TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index){
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);

        return view;
    }

}
