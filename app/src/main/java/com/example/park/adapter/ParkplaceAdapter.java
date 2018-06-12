package com.example.park.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.R;
import com.example.park.activities.EditParkplaceActivity;
import com.example.park.domain.ParkPlaceInfo;
import com.example.park.listeners.HandleResponse;
import com.example.park.util.ConnectServer;
import com.example.park.util.Constant;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 张海逢 on 2017/11/5.
 */

public class ParkplaceAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private HandleResponse handleResponse;
    private Context context;

    private ArrayList<ParkPlaceInfo> parkPlaceInformations;


    public ParkplaceAdapter(final Context context, ArrayList<ParkPlaceInfo> parkPlaceInformations){
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.parkPlaceInformations = parkPlaceInformations;

        handleResponse = new HandleResponse() {
            @Override
            public void getResponse(String response) {
                if(response.equals("1")){
                    Toast.makeText(context,"操作成功",Toast.LENGTH_SHORT).show();
                }else if(response.equals("0")){
                    Toast.makeText(context,"操作数据库失败",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }


    @Override
    public int getCount() {
        return parkPlaceInformations.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = mInflater.inflate(R.layout.parkplace_item,null);
        }
        ImageView parkView = (ImageView)view.findViewById(R.id.parkImage);
        TextView nameView = (TextView)view.findViewById(R.id.name);
        TextView costView = (TextView)view.findViewById(R.id.cost);
        TextView addressView = (TextView)view.findViewById(R.id.address);
        TextView timeView = (TextView)view.findViewById(R.id.time);
        Button editButton = (Button)view.findViewById(R.id.edit);
        final Button publishButton = (Button)view.findViewById(R.id.publish);
        parkView.setImageBitmap(parkPlaceInformations.get(i).getBitmap());
        nameView.setText(parkPlaceInformations.get(i).getParkName());
        costView.setText(parkPlaceInformations.get(i).getPrice()+"元每小时");
        addressView.setText(parkPlaceInformations.get(i).getAddress());
        timeView.setText(timeFormat(parkPlaceInformations.get(i).getStartTime())+"  --  "+timeFormat(parkPlaceInformations.get(i).getEndTime()));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditParkplaceActivity.class);
                intent.putExtra("position",i);
                context.startActivity(intent);
            }
        });
        if(parkPlaceInformations.get(i).isPublished()==true){
            publishButton.setText("取消发布");
        }else{
            publishButton.setText("发布");
        }
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = publishButton.getText().toString();
                List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
                String parameters;
                if(text.equals("发布")){
                    publishButton.setText("取消发布");
                    parkPlaceInformations.get(i).setPublished(true);
                    parameters = "is_published-pri_index-1-"+parkPlaceInformations.get(i).getPri_index();
                }else {
                    publishButton.setText("发布");
                    parkPlaceInformations.get(i).setPublished(false);
                    parameters = "is_published-pri_index-0-"+parkPlaceInformations.get(i).getPri_index();
                }
                paramsList.add(new BasicNameValuePair("MODE", "updatePublish"));
                paramsList.add(new BasicNameValuePair("parameters", parameters));
                new ConnectServer(paramsList, Constant.handleRequestUrl,handleResponse).execute();
            }
        });
        return view;
    }

    public String timeFormat(String time){
        return time.substring(0,2)+"-"+time.substring(2,4)+" "+time.substring(4,6)+":"+time.substring(6,8);
    }
}
