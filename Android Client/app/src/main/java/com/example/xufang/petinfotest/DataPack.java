package com.example.xufang.petinfotest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * 重载了克隆接口
 *
 * @author FangXu
 * @date 2018/06/xx
 *
 */
public class DataPack implements Cloneable{
    private String myname;
    private String myid;
    private String towhoid;
    private String towhoname;
    private String message;
    private boolean isMeSend;
    private Bitmap image;
    private String shortMessage;

    public DataPack(){
        myname=new String();
        towhoname=new String();
        message=new String();
        image= BitmapFactory.decodeStream(null);
        shortMessage=new String();
    }

    public DataPack( String myname, String chatMessage , boolean isMeSend,Bitmap bitmap){
        this.myname = myname;
        this.message = chatMessage;
        this.isMeSend = isMeSend;
        this.image=bitmap;
    }

    @Override
    public Object clone(){
        Object o=null;
        try{
            o=(DataPack)super.clone();
        }
        catch (CloneNotSupportedException ex){
            ex.printStackTrace();
        }
        return o;
    }

    public String getMyName() {
        return myname;
    }

    public void setMyName(String name) {
        this.myname = name;
    }

    public String getToWhoName(){
        return towhoname;
    }

    public void setTowhoName(String name){
        towhoname=name;
    }

    public String getMyId(){
        return myid;
    }

    public void setMyId(String id){
        myid=id;
    }

    public String getToWhoId(){
        return towhoid;
    }

    public void setTowhoId(String id){
        towhoid=id;
    }

    public Bitmap getImage(){
        return image;
    }

    public void setImage(Bitmap bitmap){
        image=bitmap;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMeSend() {
        return isMeSend;
    }

    public void setMeSend(boolean meSend) {
        isMeSend = meSend;
    }

    public String getShortMessage(){
        return shortMessage;
    }

    public void setShortMessage(String shortMessage){
        this.shortMessage=shortMessage;
    }
}
