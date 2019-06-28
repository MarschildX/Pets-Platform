package com.example.xufang.petinfotest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class SecondActivity extends Activity{
    private ImageView imageView;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果MainActivity继承的是AppcompatActivity，本句无用，本语句是用来出去界面顶端的actionbar的
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_second);

        //这是分解进入动画
        getWindow().setEnterTransition(new Explode().setDuration(600));
        getWindow().setExitTransition(new Explode().setDuration(500));
        //这是滑动进入动画
        /*getWindow().setEnterTransition(new Slide().setDuration(500));
        getWindow().setExitTransition(new Slide().setDuration(500));*/

        init();
        show();
    }

    private void init(){
        imageView=(ImageView)findViewById(R.id.id_ats_iv);
        textView=(TextView)findViewById(R.id.id_ats_tv);
    }

    private void show(){
        Bitmap bitmap;

        byte[] imagedata=getIntent().getByteArrayExtra("image");
        String string=getIntent().getStringExtra("string");

        bitmap=getPicFromBytes(imagedata,null);

        imageView.setImageBitmap(bitmap);
        textView.setText(string);
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
}
