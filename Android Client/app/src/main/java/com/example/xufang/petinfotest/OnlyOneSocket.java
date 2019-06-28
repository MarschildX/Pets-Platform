package com.example.xufang.petinfotest;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 * 该类目的是使多Activity共用同一个socket，所以继承了Application，Application是实体类，在mainfest中注册一下，
 * 整个程序只有一个Application，用于数据共享等，生命周期是整个程序运行期间。
 *
 * @author FangXu
 * @date 2018/06/23
 *
 */
public class OnlyOneSocket extends Application {
    private Socket socket=null;
    private String id="123456";
    private BufferedOutputStream bout;
    private BufferedInputStream bin;
    private List<DataPack> petDatas;
    private int petDataNum;
    private List<DataPack> friendDatas;
    private int friendDataNum;
    private List<DataPack> chatDatas;
    private DataInputStream din;
    private DataOutputStream dout;


    private static final String PET_MESSAGE="pet message";
    private static final String ASK_PET_MESSAGE="ask pet message";
    private static final String ASK_FRIEND_MESSAGE="ask friend message";
    private static final String CHAT_MESSAGE="chat message";



    /**将该方法当成本类的初始化方法*/
    public void setSocket(Socket socket){
        this.socket=socket;
        try{
            din=new DataInputStream(socket.getInputStream());
            dout=new DataOutputStream(socket.getOutputStream());
            dout.writeUTF(id);
            dout.flush();
            receiveOK();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        petDatas=new ArrayList<DataPack>();
        friendDatas=new ArrayList<DataPack>();
        chatDatas=new ArrayList<DataPack>();
        petDataNum=0;
        friendDataNum=0;

        /**初始化完成后就启动网络通信子线程*/
        receive();
    }


    private void receive(){
        /*new Thread(new Runnable() {
            @Override
            public void run() {*/

                initPetMessage();
                initFriendMessage();

                /*while(true){
                    receiveAndParse();
                }*/
            /*}
        }).start();*/
    }



    private void initPetMessage(){
        sendAskPetMessage();
        int length=-1;
        byte[] datahead=new byte[64];
        String petdatanumstring=null;
        try {
            petdatanumstring=din.readUTF();
            sendOK();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        petDataNum=Integer.parseInt(petdatanumstring);

        for(int i=0;i<petDataNum;i++){
            DataPack dataPack=new DataPack();
            receivePetMessage(dataPack);
        }
    }

    private void receivePetMessage(DataPack dataPack){
        acceptImageUsual(din,bin,dataPack);
        receivePetText(dataPack);
        petDatas.add(dataPack);
    }

    private void receivePetText(DataPack dataPack){
        String datamessage="";
        try {
            datamessage=din.readUTF();
            sendOK();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        dataPack.setMessage(datamessage);
        //被截断的字符串是第一个fragment中的item要显示的
        String shortmessage=null;
        if(datamessage.length()>25) {
            shortmessage = datamessage.substring(0, 25);
            shortmessage += "...";
        }
        else{
            shortmessage=datamessage;
        }
        dataPack.setShortMessage(shortmessage);
    }

    private void sendAskPetMessage(){
        String datasend=ASK_PET_MESSAGE+" "+"from "+id;
        try{
            dout.writeUTF(datasend);
            dout.flush();
            receiveOK();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void initFriendMessage(){
        Log.e("debugs","请求用户数量");
        sendAskFriendMessage();
        Log.e("debugs","已发送用户请求");
        String frienddatanumstring=new String("");
        try {
            frienddatanumstring=din.readUTF();
            sendOK();
            Log.e("debugs","接到用户数量");
            friendDataNum=Integer.parseInt(frienddatanumstring);
            Log.e("debugs","接到的friend数据数为:"+friendDataNum);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        for(int i=0;i<friendDataNum;i++){
            DataPack dataPack=new DataPack();
            receiveFriendMessage(dataPack);
        }
    }

    private void receiveFriendMessage(DataPack dataPack){
        receiveFriendText(dataPack);
        Log.e("debugs","接到一个文本");
        acceptImageUsual(din,bin,dataPack);
        Log.e("debugs","接到一张图片");
        friendDatas.add(dataPack);
    }

    private void receiveFriendText(DataPack dataPack){
        String datamessage="";
        try {
            datamessage=din.readUTF();
            sendOK();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

        int start,end;
        String friendname,friendid;
        start="name:".length();
        end=datamessage.indexOf("\r\n");
        friendname=datamessage.substring(start,end);
        start=end+"\r\n".length();
        datamessage=datamessage.substring(start);
        start="id:".length();
        friendid=datamessage.substring(start);
        dataPack.setTowhoId(friendid);
        dataPack.setTowhoName(friendname);
    }

    private void sendAskFriendMessage() {
        String datasend = ASK_FRIEND_MESSAGE + " " + "from " + id;
        try {
            dout.writeUTF(datasend);
            dout.flush();
            receiveOK();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }



    private void receiveAndParse(){
        try {
            String dataheadstring=din.readUTF();
            sendOK();
            if(dataheadstring.equals(CHAT_MESSAGE)){
                DataPack dataPack=new DataPack();
                receiveChatMessage(dataPack);
                chatDatas.add(dataPack);
            }
            else if(dataheadstring.equals(PET_MESSAGE)){
                //todo
            }
            /**以后有什么功能再加在这后面*/
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


    private void receiveChatMessage(DataPack dataPack){
        String chatdatastring=new String("");
        try {
            chatdatastring=din.readUTF();
            sendOK();
            /**开始解析数据*/
            int start,end;
            start=chatdatastring.indexOf("from ");
            start+="from ".length();
            end=chatdatastring.indexOf("\r\n");
            String fromwhoid=chatdatastring.substring(start,end);
            start=end+"\r\n".length();
            chatdatastring=chatdatastring.substring(start);
            dataPack.setTowhoId(fromwhoid);
            dataPack.setMessage(chatdatastring);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }


    private void acceptImageUsual(DataInputStream din,BufferedInputStream bin,DataPack dataPack){
        try {
            String lengthString=din.readUTF();
            Log.e("debugs","获取的长度为："+lengthString);
            sendOK();
            int length=Integer.parseInt(lengthString);
            byte[] imagebyte=new byte[length];
            int real_length_get=0;
            int start=real_length_get;
            while(real_length_get<length){
                int temp_length=din.read(imagebyte,start,length-real_length_get);
                real_length_get+=temp_length;
                start=real_length_get;
            }
            sendOK();
            Bitmap bitmap=BitmapFactory.decodeByteArray(imagebyte,0,length);
            dataPack.setImage(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  Socket getSocket(){
        return socket;
    }
    public String getId(){
        return  id;
    }
    public void setId(String id){
        this.id=id;
    }

    public int getPetDataNum(){
        int tempPetDataNum=petDataNum;
        /**及时清零*/
        petDataNum=0;
        return tempPetDataNum;
    }

    public int getFriendDataNum(){
        int tempFriendDataNum=friendDataNum;
        /**及时清零*/
        friendDataNum=0;
        return tempFriendDataNum;
    }

    public List<DataPack> getPetMessage(){
        List<DataPack> tempPetDatas=petDatas;
        /**及时将已被拿取的数据清除,不知道能不能成*/
        //petDatas.clear();
        return tempPetDatas;
    }

    public List<DataPack> getFriendDatas(){
        List<DataPack> tempFriendDatas=friendDatas;
        /**及时将已被拿取的数据清除，不知道能不能成*/
        //friendDatas.clear();
        return tempFriendDatas;
    }


    /**该方法检查是否有某个属于某个id的消息*/
    public boolean cheakIsHasMessage(String towhoid){
        for(int i=0;i<chatDatas.size();i++){
            if(chatDatas.get(i).getToWhoId().equals(towhoid)){
                return true;
            }
        }
        return false;
    }

    public DataPack getChatData(String towhoid){
        for(int i=0;i<chatDatas.size();i++){
            if(chatDatas.get(i).getToWhoId().equals(towhoid)){
                /**深层复制的方法获取对象的备份*/
                DataPack tempChatData=(DataPack)chatDatas.get(i).clone();
                /**及时清除已被拿取的数据*/
                chatDatas.remove(i);
                return tempChatData;
            }
        }
        /**理论上这个返回语句是不会触发的*/
        return null;
    }



    private void sendOK(){
        try{
            dout.writeUTF("OK");
            dout.flush();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void receiveOK(){
        try{
            String temp=din.readUTF();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
