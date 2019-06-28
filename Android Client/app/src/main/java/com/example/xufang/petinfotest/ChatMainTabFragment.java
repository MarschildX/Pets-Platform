package com.example.xufang.petinfotest;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * 该类是主activity里的第一个fragment，也是本程序的起点，OnlyOneSocket就在这里初始化
 *
 * @author FangXu
 * @date 2018/06/xx
 */
public class ChatMainTabFragment extends Fragment{
    public final static int PORT=2334;
    /**听说android虚拟机得用这个地址才能访问localhost*/
    public final static String HOST="10.0.2.2";
    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private SimpleAdapter mAdapter;
    private View view;
    private List<DataPack> mDataPacks;
    private MessageDealerFirst messageDealerFirst;
    private FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        //初始化mRecyclerview和floatingactionbutton
        view=inflater.inflate(R.layout.tab01,container,false);
        mRecyclerView=(RecyclerView)view.findViewById(R.id.id_recyclerView);
        floatingActionButton=(FloatingActionButton)view.findViewById(R.id.id_tab01_fab);
        init();
        //设置适配器
        mAdapter=new SimpleAdapter(getContext(),mDataPacks);
        mRecyclerView.setAdapter(mAdapter);

        //设置布局管理
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //设置item的点击监听
        mAdapter.setmOnItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getContext(),"click:"+position,Toast.LENGTH_SHORT).show();

                //这是共享动画，要用该动画时得把SecondActivity的动画效果注释掉
                //startActivity(new Intent(getActivity(),SecondActivity.class), ActivityOptions.makeSceneTransitionAnimation(getActivity(),view,"cover").toBundle());
                //这可以是分解或滑入动画
                Intent intent=new Intent(getActivity(),SecondActivity.class);
                byte[] bitmap;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                //把bitmap以100%高质量压缩 到 output对象里
                mDataPacks.get(position).getImage().compress(Bitmap.CompressFormat.PNG, 100, output);
                //转换成功了  result就是一个bit的资源数组
                bitmap = output.toByteArray();
                //将图片传递进intent
                intent.putExtra("image",bitmap);
                //将文本传递进intent
                String string=mDataPacks.get(position).getMessage();
                intent.putExtra("string",string);
                //启动跳转
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(getContext(),"long click:"+position,Toast.LENGTH_SHORT).show();
            }
        });

        //给浮动按钮设置事件监听
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FourthActivity.class),
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        return view;
    }

    //初始化各种东西
    private void init(){
        mDataPacks=null;
        messageDealerFirst=new MessageDealerFirst(HOST,PORT);
        new Thread(messageDealerFirst).start();

        while(mDataPacks==null){
            //loop;
        }

    }

    /**接收到更新数据后用于更新UI*/
    private void updateUI(){

    }


    /**
     * 该内部类很重要，因为它要初始化这个程序的Application，
     *
     * @author FangXu
     * @date 2018/06/19
     */
    private class MessageDealerFirst implements Runnable{
        private Socket socket;
        private int port;
        private String host;
        private BufferedInputStream bin;
        private BufferedOutputStream bout;

        public MessageDealerFirst(String host,int port){
            this.host=host;
            this.port=port;
        }

        @Override
        public void run(){
            try {
                socket = new Socket(this.host, this.port);
                /**对整个程序的唯一共享socket进行初始化，记住这个写法，真是神奇*/
                ((OnlyOneSocket)getActivity().getApplication()).setSocket(socket);
                initPetMessage();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

        /**该方法用于从OnlyOneSocket获取所有petmessage*/
        private void initPetMessage(){
            while(0==((OnlyOneSocket)getActivity().getApplication()).getPetDataNum()){
                //nothing
            }
            mDataPacks=((OnlyOneSocket)getActivity().getApplication()).getPetMessage();
        }
    }
}
