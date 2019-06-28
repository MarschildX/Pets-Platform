package com.example.xufang.petinfotest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.transition.Explode;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.net.Socket;

public class FourthActivity extends Activity{
    private DataPack dataPack;
    private RelativeLayout relativeLayout;
    private TextView textView;
    private EditText editText;
    private ImageView imagePhoto;
    private ImageView imageShow;
    private Button button_release;
    private Button button_back;
    //这是压缩过的最终图像
    private Bitmap bitmap_compressed;
    private MessageDealer_fat messageDealer_fat;
    /**调用系统相册*/
    private static final int PHOTO = 1;
    public static final int PORT=2334;
    public static final String HOST="10.0.2.2";
    private static final String PET_MESSAGE="pet message";


    public Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            String result=(String)msg.obj;
            Toast.makeText(FourthActivity.this,result,Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //如果MainActivity继承的是AppcompatActivity，本句无用，本语句是用来出去界面顶端的actionbar的
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fourth);

        //设置启动该activity的动画，这是滑入动画
        getWindow().setEnterTransition(new Explode().setDuration(600));
        getWindow().setExitTransition(new Explode().setDuration(500));

        init();
        setActionListeners();
    }


    private void init(){
        button_release=(Button)findViewById(R.id.id_atf_bt);
        imageShow=(ImageView)findViewById(R.id.id_atf_iv_show);
        imagePhoto=(ImageView)findViewById(R.id.id_atf_iv_photo);
        relativeLayout=(RelativeLayout)findViewById(R.id.id_atf_rl);
        button_back=(Button)findViewById(R.id.id_atf_back);
        editText=(EditText)findViewById(R.id.id_atf_et);
    }

    /**设置各种监听器*/
    private void setActionListeners() {
        button_release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPack=new DataPack();
                dataPack.setImage(bitmap_compressed);
                String datastring=editText.getText().toString();
                dataPack.setMessage(datastring);
                messageDealer_fat=new MessageDealer_fat(HOST,PORT,dataPack);
                new Thread(messageDealer_fat).start();
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**调用android的图库*/
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PHOTO);
            }
        });
    }

    /**该方法是调用相册后系统自动回调的*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                Uri uri = data.getData();
                String[] pojo = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, pojo, null, null, null);
                if (cursor != null) {
                    ContentResolver cr = this.getContentResolver();
                    int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(colunm_index);
                    final File file = new File(path);
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    bitmap_compressed = BitmapOption.bitmapOption(bitmap, 10);
                    //将图片显示出来
                    imageShow.setImageBitmap(bitmap_compressed);
                    Log.e("TAG", "fiels11111 " + file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**该方法用来设置弹窗弹出和收入时的背景透明度*/
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //0.0-1.0
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    /**
     * 该类用于点击弹窗以外的区域后能在将弹窗收起来后自动把背景改回来
     * 该类是利用了别人的开源例子
     */
    class poponDismissListener implements PopupWindow.OnDismissListener{
        @Override
        public void onDismiss() {
            backgroundAlpha(1f);
        }
    }


    /**这个内部类用于处理数据网络传输*/
    class MessageDealer_fat implements Runnable{
        private Socket socket;
        private DataPack dataPack;
        private BufferedOutputStream bout;
        private int port;
        private String host;
        private DataOutputStream dout;
        private DataInputStream din;

        public MessageDealer_fat(String host,int port,DataPack dataPack){
            this.host=host;
            this.port=port;
            this.dataPack=dataPack;
        }

        @Override
        public void run() {
            try {
                Looper.prepare();
                socket=((OnlyOneSocket)getApplication()).getSocket();
                dout=new DataOutputStream(socket.getOutputStream());
                bout=new BufferedOutputStream(socket.getOutputStream());
                String headString = PET_MESSAGE + " from " + ((OnlyOneSocket) getApplication()).getId();
                dout.writeUTF(headString);
                dout.flush();
                sendImage(dout,bout);
                sendMessage(dout);

                Message message=new Message();
                String result="数据上传成功";
                message.obj=result;
                /**好像回默认传给本activity*/
                handler.sendMessage(message);
                Looper.loop();

            }catch(IOException ex){
                ex.printStackTrace();
                Message message=new Message();
                String result="数据上传失败";
                message.obj=result;
                handler.sendMessage(message);
                Looper.loop();
            }
            finally{
                try{
                    bout.close();
                    socket.close();
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        }

        private void sendImage(DataOutputStream dout,BufferedOutputStream bout){
            try {

                dout.flush();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                dataPack.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imagebyte = baos.toByteArray();
                int length = imagebyte.length;
                dout.writeUTF(String.valueOf(length));
                dout.flush();
                dout.write(imagebyte, 0, length);
                dout.flush();
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }

        private void sendMessage(DataOutputStream dout){
            String datastring = dataPack.getMessage();
            try {
                dout.writeUTF(datastring);
                dout.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * 这是个工具类，该类是网上开源的一个例子，主要进行图像压缩
 * @author i don't know
 */
class BitmapOption {
    private static final BitmapOption bitmapOption = new BitmapOption();

    private BitmapOption() { }
    public static BitmapOption getBitmapOption() {
        return bitmapOption;
    }

    public static Bitmap bitmapOption(Bitmap image, int size) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 85, out);
        float zoom = (float) Math.sqrt(size * 1024 / (float) out.toByteArray().length);
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);
        Bitmap result = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        while (out.toByteArray().length > size * 1024) {
            System.out.println(out.toByteArray().length);
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        }
        return result;
    }
}




