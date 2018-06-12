package com.example.park.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.park.R;
import com.example.park.domain.StockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by takusemba on 2017/08/03.
 */

public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.MyViewHolder> implements View.OnClickListener {

    private List<StockEntity> dataList = new ArrayList<>();//recyclerView数据集
    private OnItemClickListener mOnItemClickListener = null;

    public VerticalAdapter(List<StockEntity> dataList){
        this.dataList = dataList;
    }

    public VerticalAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_verticl, viewGroup, false);
        view.setOnClickListener(this);
        return new VerticalAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyViewHolder myHolder = holder;
        myHolder.bindData(dataList.get(position));
        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return dataList.size();
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

    public static interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView address;
        ImageView trendFlagIv;
        TextView grossTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.address = (TextView) itemView.findViewById(R.id.address);
            this.trendFlagIv = (ImageView) itemView.findViewById(R.id.item_trend_flag);
            this.grossTv = (TextView) itemView.findViewById(R.id.item_gross);
        }

        public void bindData(StockEntity stockEntity) {
            name.setText(stockEntity.getName());
            if(stockEntity.getAddress().length()>18){
                address.setText(String.valueOf(stockEntity.getDistance())+"m | "+stockEntity.getAddress().substring(0,18)+"...");
            }else{
                address.setText(String.valueOf(stockEntity.getDistance())+"m | "+stockEntity.getAddress());
            }
        }
    }
}