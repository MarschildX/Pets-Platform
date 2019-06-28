package com.example.xufang.petinfotest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context mContext;
    private List<String> mDatas;
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;
    private List<DataPack> mDataPacks;


    public interface OnItemClickListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);
    }

    public void setmOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener=listener;
    }


    public SimpleAdapter(Context context,List<DataPack> mDataPacks){
        this.mContext=context;
        this.mDataPacks=mDataPacks;
        mInflater= LayoutInflater.from(context);
    }

    @Override
    public int getItemCount(){
        return mDataPacks.size();
        //return 20;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder,final int pos){
        holder.iv.setImageBitmap(mDataPacks.get(pos).getImage());
        holder.tv.setText(mDataPacks.get(pos).getShortMessage());

        if(mOnItemClickListener!=null) {
            //click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.itemView,pos);
                }
            });

            //longclick
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView,pos);
                    return false;
                }
            });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup arg0,int arg1){
        View view=mInflater.inflate(R.layout.item_single_textview,arg0,false);
        //使用获得的view对viewHolder进行初始化，出过bug是因为这里参数写成arg0
        MyViewHolder viewHolder=new MyViewHolder(view);
        //返回holder对象，之前出过bug是因为这里返回值写了null
        return viewHolder;
    }

}

class MyViewHolder extends RecyclerView.ViewHolder {
    TextView tv;
    ImageView iv;

    public MyViewHolder(View arg0){
        super(arg0);
        tv=(TextView)arg0.findViewById(R.id.id_item_single_tv);
        iv=(ImageView)arg0.findViewById(R.id.id_item_single_IV);
    }
}