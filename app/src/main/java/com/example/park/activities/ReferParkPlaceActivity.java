package com.example.park.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.park.R;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.ui.MyToolbar;
import com.example.park.ui.SelectPicPopupWindow;
import com.example.park.ui.timeselector.TimeSelector;

public class ReferParkPlaceActivity extends BaseActivity implements View.OnClickListener{
    private TextView chooseStartDate;
    private TextView chooseEndDate;
    private TextView startDate;
    private TextView endDate;
    private TextView chooseAddress;
    private TimeSelector startTimeSelector;
    private TimeSelector endTimeSelector;
    private MyToolbar toolbar;
    private Button cancelButton;
    private Button goButton;
    private LinearLayout photoItem;
    private ImageView parkPhoto;
    private TextView addressTextView;
    private TextView detailAddressTextView;
    private TextView parkNameTextView;
    private TextView hostNameTextView;
    private TextView contactTextView;
    private TextView commentTextView;
    private TextView priceSpinner;
    LinearLayout contactLayout;
    SelectPicPopupWindow menuWindow;
    private Bitmap head;//车位照片

    private double latitude;
    private double longitude;
    private int position;
    ParkPlaceInfo currentParkPlaceInfo;


    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_refer_park_place);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        initialView();
        addListener();
    }

    public void initialView(){
        Intent mIntent = getIntent();
        //传递item位置信息
        position = mIntent.getIntExtra("position",-1);
        currentParkPlaceInfo = MainActivity.publishParkPlaceinfos.get(position);
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("查询车位信息");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        photoItem = (LinearLayout)findViewById(R.id.photoItem);
        parkPhoto = (ImageView)findViewById(R.id.edit_parkphoto);
        cancelButton = (Button)findViewById(R.id.cancel);
        goButton = (Button)findViewById(R.id.go);
        chooseStartDate = (TextView)findViewById(R.id.chooseStartDate);
        chooseEndDate = (TextView)findViewById(R.id.chooseEndDate);
        chooseAddress = (TextView)findViewById(R.id.chooseAddress);
        parkNameTextView = (TextView)findViewById(R.id.parkName);
        contactTextView = (TextView)findViewById(R.id.contactTextView);
        commentTextView = (TextView)findViewById(R.id.commentTextView);
        hostNameTextView = (TextView)findViewById(R.id.hostNameTextView);
        priceSpinner = (TextView)findViewById(R.id.priceSpinner);
        addressTextView = (TextView)findViewById(R.id.addressTextView);
        detailAddressTextView = (TextView)findViewById(R.id.detailAddressTextView);
        startDate = (TextView)findViewById(R.id.startDate);
        endDate = (TextView)findViewById(R.id.endDate);

        parkPhoto.setImageBitmap(currentParkPlaceInfo.getBitmap());
        parkNameTextView.setText(currentParkPlaceInfo.getParkName());
        startDate.setText(currentParkPlaceInfo.getStartTime());
        endDate.setText(currentParkPlaceInfo.getEndTime());
        addressTextView.setText(currentParkPlaceInfo.getAddress());
        detailAddressTextView.setText(currentParkPlaceInfo.getDetailAddress());
        hostNameTextView.setText(currentParkPlaceInfo.getHostName());
        contactTextView.setText(currentParkPlaceInfo.getHostContact());
        commentTextView.setText(currentParkPlaceInfo.getComment());
        longitude = currentParkPlaceInfo.getLongitude();
        latitude = currentParkPlaceInfo.getLatitude();
        priceSpinner.setText(currentParkPlaceInfo.getPrice());

        contactLayout = (LinearLayout)findViewById(R.id.contactLayout);
    }

    public void addListener(){
        cancelButton.setOnClickListener(this);
        goButton.setOnClickListener(this);
        contactLayout.setOnClickListener(this);
        photoItem.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancel:
                Intent intent = new Intent();
                intent.putExtra("info","notGo");
                ReferParkPlaceActivity.this.setResult(1,intent);
                finish();
                break;
            case R.id.go:
                Intent mIntent = new Intent();
                mIntent.putExtra("info","go");
                mIntent.putExtra("latitude",latitude);
                mIntent.putExtra("longitude",longitude);
                ReferParkPlaceActivity.this.setResult(1,mIntent);
                finish();
                break;
            case R.id.contactLayout:
                String number = contactTextView.getText().toString();
                //用intent启动拨打电话
                Intent callIntent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+number));
                startActivity(callIntent);
                break;
            case R.id.photoItem:
                Intent photoIntent = new Intent(ReferParkPlaceActivity.this,ShowImageActivity.class);
                photoIntent.putExtra("position",position);
                startActivity(photoIntent);
            default:
                break;
        }
    }
}

