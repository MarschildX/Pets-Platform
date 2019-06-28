package com.example.xufang.petinfotest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.View;
import android.view.Window;
import android.widget.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ThirdActivity extends Activity {
    public final static int PORT=2334;

    /**模拟器上访问localhost要用这个地址*/
    public final static String HOST="10.0.2.2";
    public final static String MY_ID="123456";
    public final static String TOWHO="222222";

    private MessageDealer messageDealer;
    private Button button;
    private ImageView imageView;
    private EditText editText;
    private DataPack mDataPack;
    private List<DataPack> dataList;
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textView;
    private String myid;
    private String towho;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**如果继承的是AppcompatActivity，本句无用，本语句是用来出去界面顶端的actionbar的*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_third);

        //这是滑动进入的动画
        getWindow().setEnterTransition(new Slide().setDuration(400));
        getWindow().setExitTransition(new Slide().setDuration(400));

        init();
        setButtonListener();
        setImageViewListener();
    }

    /**网络线程通过handler机制将数据传递给主线程*/
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                mDataPack=(DataPack)msg.obj;
                mDataPack.setImage(bitmap);
                dataList.add(mDataPack);
                adapter.notifyItemInserted(dataList.size());
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                /**终止Looper循环，这样才能继续接收消息*/
                getLooper().quitSafely();
            }
        }
    };

    private void init(){
        dataList=new ArrayList<DataPack>();
        button=(Button)findViewById(R.id.id_cb_bn);
        imageView=(ImageView)findViewById(R.id.id_ct_iv);
        editText=(EditText)findViewById(R.id.id_cb_et);
        adapter=new ChatAdapter(this,dataList);
        textView=(TextView)findViewById(R.id.id_ct_tv);
        recyclerView=(RecyclerView)findViewById(R.id.id_cc_rcv);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        messageDealer=new MessageDealer(HOST,PORT);
        new Thread(messageDealer).start();
        myid=MY_ID;
        String friendname=getIntent().getStringExtra("name");
        textView.setText(friendname);
        towho=getIntent().getStringExtra("id");

        byte[] imagedata=getIntent().getByteArrayExtra("image");
        bitmap=getPicFromBytes(imagedata,null);
    }

    /**下面的这个方法是将byte数组转化为Bitmap对象的一个方法*/
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null) {
            if (opts != null) {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            } else {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        }
        return null;
    }

    private void setButtonListener(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempcontent=editText.getText().toString();
                //更新界面
                mDataPack=new DataPack();
                mDataPack.setMessage(tempcontent);
                dataList.add(mDataPack);
                adapter.notifyItemInserted(dataList.size());
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                //发送消息
                messageDealer.send(tempcontent,myid,towho);
                editText.setText("");
            }
        });
    }

    private void setImageViewListener(){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**该类是一个数据处理器，包括了数据的发送，收取由OnlyOneSocket统一进行*/
    private class MessageDealer implements Runnable{
        private Socket socket;
        private BufferedOutputStream bout;
        private BufferedInputStream bin;
        private int port;
        private String host;
        private DataOutputStream dout;

        @Override
        public void run(){
            try{
                /**socket来自于本程序的唯一socket*/
                socket=((OnlyOneSocket)getApplication()).getSocket();
                /**貌似多线程下可以给一个socket开多个流*/
                dout=new DataOutputStream(socket.getOutputStream());
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
            while(true){
                getChatMessage();
            }
        }

        public MessageDealer(String host,int port){
            this.host=host;
            this.port=port;
        }

        public void send(final String msgcontent,final String myid,final String towho) {
            /**android因为不能在主线程进行网络操作，所以这里要建线程*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String datahead="chat message from "+((OnlyOneSocket)getApplication()).getId();
                    String from="from:"+myid+"\r\n";
                    String to="to:"+towho+"\r\n";
                    String datacomplete=from+to+msgcontent;
                    try {
                        /**感觉这里设计的不太好，用了两次传数据*/
                        dout.writeUTF(datahead);
                        dout.flush();
                        dout.writeUTF(datacomplete);
                        dout.flush();
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        }

        private void getChatMessage(){
            while(((OnlyOneSocket)getApplication()).cheakIsHasMessage(towho)){
                Looper.prepare();
                DataPack dataPack=((OnlyOneSocket)getApplication()).getChatData(towho);
                Message message=new Message();
                //1代表传递聊天信息
                message.what=1;
                message.obj=dataPack;
                handler.sendMessage(message);
                Looper.loop();
            }
        }

        public void closeSocket(){
            try{
                socket.close();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

}
