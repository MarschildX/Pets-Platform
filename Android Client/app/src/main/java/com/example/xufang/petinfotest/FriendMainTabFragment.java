/************************************************************************
 *                                                                      *
 *       由于前期设计考虑不周，这个类名起的不好，改了太麻烦，所以暂且这样吧        *
 *                                                                      *
 ************************************************************************/




package com.example.xufang.petinfotest;

import android.app.ActivityOptions;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FriendMainTabFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private ChattingAdapter mAdapter;
    private View view;
    private List<DataPack> mDataPacks;
    private MessageDealer_Chatting messageDealer_Chatting;

    public static final String HOST="10.0.2.2";
    public static final int PORT=2334;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //初始化mRecyclerview
        view=inflater.inflate(R.layout.tab02,container,false);
        mRecyclerView=(RecyclerView)view.findViewById(R.id.id_tab02_recyclerView);

        //初始化mDatas变量
        initMDataPacks();

        //设置适配器
        mAdapter=new ChattingAdapter(getContext(),mDataPacks);
        mRecyclerView.setAdapter(mAdapter);

        //设置布局管理
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //设置RecyclerView的每个Item之间的分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));

        //设置item的点击监听
        mAdapter.setmOnItemClickListener(new ChattingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getContext(),"click:"+position,Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(getActivity(), ThirdActivity.class);
                intent.putExtra("name",mDataPacks.get(position).getToWhoName());
                intent.putExtra("id",mDataPacks.get(position).getToWhoId());

                byte[] bitmap;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                //把bitmap以100%高质量压缩 到 output对象里
                mDataPacks.get(position).getImage().compress(Bitmap.CompressFormat.PNG, 100, output);
                //转换成功了  result就是一个bit的资源数组
                bitmap = output.toByteArray();
                //将图片传递进intent
                intent.putExtra("image",bitmap);
                startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(getContext(),"long click:"+position,Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    private void initMDataPacks(){
        mDataPacks=null;
        messageDealer_Chatting=new MessageDealer_Chatting(HOST,PORT);
        new Thread(messageDealer_Chatting).start();

        while(null==mDataPacks){
            //loop
        }
    }


    class MessageDealer_Chatting implements Runnable{
        private Socket socket;
        private int port;
        private String host;
        private BufferedInputStream bin;
        private BufferedOutputStream bout;

        public MessageDealer_Chatting(String host,int port){
            this.host=host;
            this.port=port;
        }

        @Override
        public void run() {
            socket = ((OnlyOneSocket) getActivity().getApplication()).getSocket();
            initFriendMessage();
        }

        /**该方法用于从OnlyOneSocket获取所有friendmessage*/
        private void initFriendMessage(){
            while(0==((OnlyOneSocket)getActivity().getApplication()).getFriendDataNum()){
                //nothing
            }
            mDataPacks=((OnlyOneSocket)getActivity().getApplication()).getFriendDatas();
        }
    }
}
