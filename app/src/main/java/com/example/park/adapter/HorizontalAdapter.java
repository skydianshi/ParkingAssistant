package com.example.park.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.park.R;
import com.example.park.domain.ParkPlaceInfo;

import java.util.ArrayList;

/**
 * Created by takusemba on 2017/08/03.
 */

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.ViewHolder> implements View.OnClickListener {

    private OnItemClickListener mOnItemClickListener;
    private ArrayList<ParkPlaceInfo> parkPlaceInformations;


    public HorizontalAdapter(ArrayList<ParkPlaceInfo> parkPlaceInformations){
        this.parkPlaceInformations = parkPlaceInformations;
    }

    @Override
    public HorizontalAdapter.ViewHolder onCreateViewHolder (ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_horizontal, viewGroup, false);
        view.setOnClickListener(this);
        return new HorizontalAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HorizontalAdapter.ViewHolder holder, int position) {
        String title = parkPlaceInformations.get(position).getParkName();
        if(title.length()>=10){
            title = title.substring(0,10) + "...";
        }
        holder.title.setText(title);
        String address = parkPlaceInformations.get(position).getAddress();
        String detailAddress = parkPlaceInformations.get(position).getDetailAddress();
        int distance = parkPlaceInformations.get(position).getDistance();
        if((address+detailAddress).length()>13){
            holder.address.setText("地址： "+(address+detailAddress).substring(0,14)+ "... | "+distance + "米");
        }else{
            holder.address.setText("地址： "+address+detailAddress+ " | 距离："+distance + "米");
        }
        String price = parkPlaceInformations.get(position).getPrice();
        holder.price.setText("价格： "+price+"元每小时");
        String time = timeFormat(parkPlaceInformations.get(position).getStartTime())+"  --  "+timeFormat(parkPlaceInformations.get(position).getEndTime());
        holder.time.setText("时间： "+time);
        Bitmap bitmap = parkPlaceInformations.get(position).getBitmap();
        holder.parkPhoto.setImageBitmap(bitmap);
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return parkPlaceInformations.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.OnItemClick(view,(int)view.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView address;
        private TextView price;
        private TextView time;
        private ImageView parkPhoto;

        ViewHolder(final View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.title);
            this.address = (TextView) itemView.findViewById(R.id.address);
            this.price = (TextView) itemView.findViewById(R.id.price);
            this.time = (TextView) itemView.findViewById(R.id.time);
            this.parkPhoto = (ImageView) itemView.findViewById(R.id.parkPhoto);
        }
    }

    public String timeFormat(String time){
        return time.substring(4,6)+":"+time.substring(6,8);
    }
}