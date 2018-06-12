package com.example.park.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.itemActivities.VersionActivity;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.listeners.HandleResponse;
import com.example.park.ui.ConnectDialog;
import com.example.park.ui.MyToolbar;
import com.example.park.ui.SelectPicPopupWindow;
import com.example.park.ui.timeselector.TimeSelector;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;

import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class EditParkplaceActivity extends BaseActivity implements View.OnClickListener{
    private TextView chooseStartDate;
    private TextView chooseEndDate;
    private TextView startDate;
    private TextView endDate;
    private TextView chooseAddress;
    private TimeSelector startTimeSelector;
    private TimeSelector endTimeSelector;
    private MyToolbar toolbar;
    private Button savaButton;
    private Button deleteButton;
    private LinearLayout photoItem;
    private ImageView parkPhoto;
    private TextView addressTextView;
    private EditText detailAddressEditText;
    private EditText parkNameEditText;
    private EditText hostNameEditText;
    private EditText contactEditText;
    private EditText commentEditText;
    private Spinner priceSpinner;
    SelectPicPopupWindow menuWindow;
    private Bitmap head;//车位照片

    private double latitude;
    private double longitude;
    private int position;
    ParkPlaceInfo currentParkPlaceInfo;
    HandleResponse handleResponse;
    private static String TAG = "EditParkplaceActivity";

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    connectDialog.dismiss();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_edit_parkplace);
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
        currentParkPlaceInfo = MainActivity.privateParkPlaceInfos.get(position);
        toolbar = (MyToolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("编辑车位信息");
        toolbar.setNavigationIcon(R.mipmap.backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        photoItem = (LinearLayout)findViewById(R.id.photoItem);
        parkPhoto = (ImageView)findViewById(R.id.edit_parkphoto);
        savaButton = (Button)findViewById(R.id.save);
        deleteButton = (Button)findViewById(R.id.delete);
        chooseStartDate = (TextView)findViewById(R.id.chooseStartDate);
        chooseEndDate = (TextView)findViewById(R.id.chooseEndDate);
        chooseAddress = (TextView)findViewById(R.id.chooseAddress);
        parkNameEditText = (EditText)findViewById(R.id.parkName);
        contactEditText = (EditText)findViewById(R.id.contactEditText);
        commentEditText = (EditText)findViewById(R.id.commentEditText);
        hostNameEditText = (EditText)findViewById(R.id.hostNameEditText);
        priceSpinner = (Spinner)findViewById(R.id.priceSpinner);
        addressTextView = (TextView)findViewById(R.id.addressTextView);
        detailAddressEditText = (EditText)findViewById(R.id.detailAddressEditText);
        startTimeSelector = new TimeSelector(this, new TimeSelector.ResultHandler() {
            @Override
            public void handle(String time) {
                startDate.setText(time);
            }
        }, "2017-01-30 00:00", "2050-12-31 00:00");

        endTimeSelector = new TimeSelector(this, new TimeSelector.ResultHandler() {
            @Override
            public void handle(String time) {
                endDate.setText(time);
            }
        }, "2017-01-30 00:00", "2018-12-31 00:00");
        startDate = (TextView)findViewById(R.id.startDate);
        endDate = (TextView)findViewById(R.id.endDate);

        parkPhoto.setImageBitmap(currentParkPlaceInfo.getBitmap());
        parkNameEditText.setText(currentParkPlaceInfo.getParkName());
        startDate.setText("     "+currentParkPlaceInfo.getStartTime());//加五个空格用来下面substring
        endDate.setText("     "+currentParkPlaceInfo.getEndTime());
        addressTextView.setText(currentParkPlaceInfo.getAddress());
        detailAddressEditText.setText(currentParkPlaceInfo.getDetailAddress());
        hostNameEditText.setText(currentParkPlaceInfo.getHostName());
        contactEditText.setText(currentParkPlaceInfo.getHostContact());
        commentEditText.setText(currentParkPlaceInfo.getComment());
        longitude = currentParkPlaceInfo.getLongitude();
        latitude = currentParkPlaceInfo.getLatitude();
        saveBitMap = currentParkPlaceInfo.getClearBitmap();
    }

    public void addListener(){
        chooseStartDate.setOnClickListener(this);
        chooseEndDate.setOnClickListener(this);
        chooseAddress.setOnClickListener(this);
        savaButton.setOnClickListener(this);
        photoItem.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        handleResponse = new HandleResponse() {
            @Override
            public void getResponse(String response) {
                if("1".equals(response)){
                    Log.d(TAG,"更新数据库成功");
                }else{
                    Log.d(TAG,"更新数据库失败");
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chooseStartDate:
                startTimeSelector.show();
                break;
            case R.id.chooseEndDate:
                endTimeSelector.show();
                break;
            case R.id.chooseAddress:
                Intent intent = new Intent(EditParkplaceActivity.this,ChooseAddressActivity.class);
                startActivityForResult(intent,4);
                break;
            case R.id.save:
                if(Constant.is_loadImage){
                    saveInformation();
                }else{
                    Toast.makeText(EditParkplaceActivity.this,"当前网速较慢，车位未加载完成，请等待加载完成后再进行保存",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.delete:
                if(Constant.is_loadImage){
                    new AlertDialog.Builder(EditParkplaceActivity.this).setTitle("系统提示")//设置对话框标题
                            .setMessage("确定要删除车位信息？")//设置显示的内容
                            .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮
                                @Override
                                public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                    //删除数据库中车位
                                    List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
                                    String parameters = String.valueOf(currentParkPlaceInfo.getPri_index());
                                    paramsList.add(new BasicNameValuePair("MODE", "deleteParkPlace"));
                                    paramsList.add(new BasicNameValuePair("parameters", parameters));
                                    new ConnectServer(paramsList, Constant.handleRequestUrl,handleResponse).execute();
                                    MainActivity.privateParkPlaceInfos.remove(position);
                                    finish();
                                }
                            }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//响应事件

                        }

                    }).show();//在按键响应事件中显示此对话框
                }else{
                    Toast.makeText(EditParkplaceActivity.this,"当前网速较慢，车位未加载完成，请等待后重试",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.photoItem:
                menuWindow = new SelectPicPopupWindow(EditParkplaceActivity.this,itemsOnClick);
                menuWindow.showAtLocation(EditParkplaceActivity.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
                break;
            default:
                break;
        }

    }

    //选择照片获取方式的点击事件
    private View.OnClickListener itemsOnClick = new View.OnClickListener(){

        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_take_photo:
                    //最好用try/catch包裹一下，防止因为用户未给应用程序开启相机权限，而使程序崩溃
                    try {
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//开启相机应用程序获取并返回图片（capture：俘获）
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                                "login_head.jpg")));//指明存储图片或视频的地址URI
                        startActivityForResult(intent2, 2);//采用ForResult打开
                    } catch (Exception e) {
                        Toast.makeText(EditParkplaceActivity.this, "相机无法启动，请先开启相机权限", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.btn_pick_photo:
                    Intent intent1 = new Intent(Intent.ACTION_PICK, null);//返回被选中项的URI
                    intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//得到所有图片的URI
                    startActivityForResult(intent1, 1);
                    break;
                default:
                    break;
            }


        }

    };

    ConnectDialog connectDialog;
    Bitmap saveBitMap;
    public boolean saveInformation(){
        if(head!=null){
            saveBitMap = head;
        }
        String saveParkName = parkNameEditText.getText().toString();
        String savePrice = (String)priceSpinner.getSelectedItem();
        String saveStartTime = startDate.getText().toString();
        String saveEndTime = endDate.getText().toString();
        String saveAddress = addressTextView.getText().toString();
        String saveDetailAddress = detailAddressEditText.getText().toString();
        String saveHostName = hostNameEditText.getText().toString();
        String saveContact = contactEditText.getText().toString();
        String saveComment = commentEditText.getText().toString();
        if(saveBitMap==null||saveParkName==null||savePrice==null||saveStartTime==null||saveEndTime==null||saveAddress==null
                ||saveHostName==null||saveDetailAddress==null|saveContact==null||saveComment==null||latitude==0||longitude==0){
            new AlertDialog.Builder(EditParkplaceActivity.this).setTitle("系统提示")//设置对话框标题
                    .setMessage("请填写完成您的车位信息！")//设置显示的内容
                    .setPositiveButton("返回继续填写",new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件

                        }
                    }).setNegativeButton("取消保存",new DialogInterface.OnClickListener() {//添加返回按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {//响应事件
                    finish();
                }
            }).show();//在按键响应事件中显示此对话框
            return false;
        }
        currentParkPlaceInfo.setBitmap(saveBitMap);
        currentParkPlaceInfo.setImagePath(currentParkPlaceInfo.getPri_index()+"");
        currentParkPlaceInfo.setParkName(saveParkName);
        currentParkPlaceInfo.setPrice(savePrice);
        currentParkPlaceInfo.setStartTime(saveStartTime.substring(5));
        currentParkPlaceInfo.setEndTime(saveEndTime.substring(5));
        currentParkPlaceInfo.setAddress(saveAddress);
        currentParkPlaceInfo.setDetailAddress(saveDetailAddress);
        currentParkPlaceInfo.setHostName(saveHostName);
        currentParkPlaceInfo.setHostContact(saveContact);
        currentParkPlaceInfo.setComment(saveComment);
        currentParkPlaceInfo.setLatitude(latitude);
        currentParkPlaceInfo.setLongitude(longitude);
        //更新数据库
        List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
        String parameters = "park_name-price-start_time-end_time-address-detail_address-host_name-host_contact-comment-latitude-longitude-image_path-pri_index-"+
                currentParkPlaceInfo.getParkName()+"-"+currentParkPlaceInfo.getPrice()+"-"+currentParkPlaceInfo.getStartTime()+"-"+currentParkPlaceInfo.getEndTime()+"-"+
                currentParkPlaceInfo.getAddress()+"-"+currentParkPlaceInfo.getDetailAddress()+"-"+currentParkPlaceInfo.getHostName()+"-"+currentParkPlaceInfo.getHostContact()+
                "-"+currentParkPlaceInfo.getComment()+"-"+currentParkPlaceInfo.getLatitude()+"-"+currentParkPlaceInfo.getLongitude()+"-"+currentParkPlaceInfo.getImagePath()+"-"+currentParkPlaceInfo.getPri_index();
        paramsList.add(new BasicNameValuePair("MODE", "updateFigure"));
        paramsList.add(new BasicNameValuePair("parameters", parameters));
        paramsList.add(new BasicNameValuePair("imgStr_clear", Bitmap2StrByBase64(saveBitMap,100)));
        paramsList.add(new BasicNameValuePair("imgStr_vague", Bitmap2StrByBase64(saveBitMap,40)));
        new ConnectServer(paramsList, Constant.handleRequestUrl,handleResponse).execute();

        connectDialog = new ConnectDialog(EditParkplaceActivity.this,R.style.dialog);
        connectDialog.setTips(R.string.is_saving);
        connectDialog.show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // mMsgHandler.sendEmptyMessageDelayed(TYPE_CONNECT, 100);
                try {
                    Thread.sleep(1000);
                    handler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    //进行选取照片操作
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //从相册里面取相片的返回结果
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());//裁剪图片
                }
                break;
            //相机拍照后的返回结果
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/login_head.jpg");
                    cropPhoto(Uri.fromFile(temp));//裁剪图片
                }
                break;
            //调用系统裁剪图片后
            case 3:
                if (imageUri != null) {
                    head = decodeUriAsBitmap(imageUri);
                    parkPhoto.setImageBitmap(head);
                }
                break;
            case 4:
                try {
                    String address = data.getStringExtra("address");
                    String detailAddress = data.getStringExtra("detailAddress");
                    latitude = data.getDoubleExtra("latitude",0);
                    longitude = data.getDoubleExtra("longitude",0);
                    addressTextView.setText(address);
                    detailAddressEditText.setText(detailAddress);
                }catch (Exception e){
                    Log.d("EditParkplaceActivity","未修改地址");
                }

                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 通过Base32将Bitmap转换成Base64字符串
     * @param bit
     * @return
     */
    public String Bitmap2StrByBase64(Bitmap bit,int sharpness){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, sharpness, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private Uri imageUri;
    /**
     * 调用系统的裁剪
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        File sdDir = new File(Environment.getExternalStorageDirectory(),"temp2.jpg");
        imageUri = Uri.fromFile(sdDir);
        Intent intent = new Intent("com.android.camera.action.CROP");
        //找到指定URI对应的资源图片
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale",true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 2);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 400);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //进入系统裁剪图片的界面
        startActivityForResult(intent, 3);
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            // 先通过getContentResolver方法获得一个ContentResolver实例，
            // 调用openInputStream(Uri)方法获得uri关联的数据流stream
            // 把上一步获得的数据流解析成为bitmap
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
