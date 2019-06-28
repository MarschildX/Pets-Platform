package com.example.xufang.petinfotest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{

    private List<DataPack> mDataPackList;
    private Context mContext;
    private LayoutInflater mInflater;


    public ChatAdapter(Context context,List<DataPack> datas){
        this.mContext=context;
        this.mDataPackList=datas;
        mInflater= LayoutInflater.from(context);
    }

    @Override
    public int getItemCount(){
        return mDataPackList.size();
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder,final int pos){
        /***调用左右两种视图的*/
        if(mDataPackList.get(pos).isMeSend()){
            holder.tv_right.setText(mDataPackList.get(pos).getMessage());
            holder.iv_right.setImageBitmap(mDataPackList.get(pos).getImage());
        }
        else{
            holder.tv_left.setText(mDataPackList.get(pos).getMessage());
            holder.iv_left.setImageBitmap(mDataPackList.get(pos).getImage());
        }
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup arg0,int itemtype){
        View view;
        //这个条件判断很关键，它决定了聊天消息的正确显示方向
        if(itemtype==1){
            view=mInflater.inflate(R.layout.item_right_chat,arg0,false);
        }
        else{
            view=mInflater.inflate(R.layout.item_left_chat,arg0,false);
        }

        //使用获得的view对viewHolder进行初始化，出过bug是因为这里参数写成arg0
        ChatViewHolder viewHolder=new ChatViewHolder(view);
        //返回holder对象，之前出过bug是因为这里返回值写了null
        return viewHolder;
    }

/*************************************************/
    /**该方法用于确定item用哪种layout*/
    @Override
    public int getItemViewType(int position){

        if(mDataPackList.get(position).isMeSend()){
            return 1;
        }
        else{
            return 0;
        }
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tv_left;
        ImageView iv_left;

        TextView tv_right;
        ImageView iv_right;

        public ChatViewHolder(View arg0){
            super(arg0);
            tv_left=(TextView)arg0.findViewById(R.id.id_ilc_tv);
            iv_left=(ImageView)arg0.findViewById(R.id.id_ilc_iv);
            tv_right=(TextView)arg0.findViewById(R.id.id_irc_tv);
            iv_right=(ImageView)arg0.findViewById(R.id.id_irc_iv);
        }
    }

}
