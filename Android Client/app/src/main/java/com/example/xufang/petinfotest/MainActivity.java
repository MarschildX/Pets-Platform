package com.example.xufang.petinfotest;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
/**
 * 因为引用了V4的包，所以得继承FragmentActivity
 *
 * @author FangXu
 * @date 2018/06/xx
 */
public class MainActivity extends FragmentActivity {
    /**滑动效果的实现依附于viewPager，fragment用了v4的包之后，viewpager也用v4的包*/
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment>mDatas;
    private TextView mChatTextView;
    private TextView mFindTextView;
    private TextView mContactTextView;
    private LinearLayout mChatTextViewLL;
    private LinearLayout mFindTextViewLL;
    private LinearLayout mContactTextViewLL;
    private ChatMainTabFragment tab01;
    private FriendMainTabFragment tab02;
    private ContactMainTabFragment tab03;

    private ImageView mTabline;
    private int mScreen1_3;
    private int mCurrentPageIndex;


    public Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果MainActivity继承的是AppcompatActivity，本句无用，本语句是用来出去界面顶端的actionbar的
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initTabline();
        initView();
    }

    private void initView(){
        mViewPager=(ViewPager)findViewById(R.id.id_viewpager);
        mChatTextView=(TextView)findViewById(R.id.id_tv_chat);
        mFindTextView=(TextView)findViewById(R.id.id_tv_find);
        mContactTextView=(TextView)findViewById(R.id.id_tv_contact);
        mChatTextViewLL=(LinearLayout)findViewById(R.id.id_ll_chat);
        mFindTextViewLL=(LinearLayout)findViewById(R.id.id_ll_find);
        mContactTextViewLL=(LinearLayout)findViewById(R.id.id_ll_contact);

        //mChatLinearLayout=findViewById(R.id.id_ll_chat);
        mDatas=new ArrayList<Fragment>();
        tab01=new ChatMainTabFragment();
        tab02=new FriendMainTabFragment();
        tab03=new ContactMainTabFragment();

        mDatas.add(tab01);
        mDatas.add(tab02);
        mDatas.add(tab03);

        //给viewpager创建一个fragment的adapter
        mAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                //返回一个一个的Fragment，这个返回的fragment就是用于显示的
                return mDatas.get(position);
            }

            @Override
            public int getCount() {                 //该方法返回有效视图的数量
                return mDatas.size();
            }
        };

        //给viewpager设置适配器
        mViewPager.setAdapter(mAdapter);

        //让ViewPager缓存4个页面，即后台最多保存四个fragment不销毁
        mViewPager.setOffscreenPageLimit(4);

        //这个方法是用于页面改变时其他部件要做的相应变化的设置，比如切换页面tab字体颜色变，上方滑动条滑动
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e("TAG",position+","+positionOffset+","+positionOffsetPixels);

                LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabline.getLayoutParams();
                //0->1
                if(mCurrentPageIndex==0&&position==0){
                    lp.leftMargin=(int)(positionOffset*mScreen1_3+mCurrentPageIndex*mScreen1_3);
                }
                //1->0
                else if(mCurrentPageIndex==1&&position==0){
                    lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(positionOffset-1)*mScreen1_3);
                }
                //1->2
                else if(mCurrentPageIndex==1&&position==1){
                    lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+positionOffset*mScreen1_3);
                }
                //2->1
                else if(mCurrentPageIndex==2&&position==1){
                    lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(positionOffset-1)*mScreen1_3);
                }
                mTabline.setLayoutParams(lp);
            }

            @Override
            public void onPageSelected(int position) {
                resetTextView();
                switch (position){
                    case 0:
                        mChatTextView.setTextColor(Color.parseColor("#FB3383"));
                        break;
                    case 1:
                        mFindTextView.setTextColor(Color.parseColor("#FB3383"));
                        break;
                    case 2:
                        mContactTextView.setTextColor(Color.parseColor("#FB3383"));
                        break;
                }
                mCurrentPageIndex=position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setTVListener();  //设置点击事件，切换页面
    }

    private void setTVListener() {
        mChatTextViewLL.setOnClickListener(new View.OnClickListener() {
            LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabline.getLayoutParams();

            @Override
            public void onClick(View v) {
                //直接调用mViewPager的se'tCurrentItem()方法就可以把界面切换的各个过程都自动实现，真方便
                mViewPager.setCurrentItem(0);
            }
        });

        mFindTextViewLL.setOnClickListener(new View.OnClickListener() {
            LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabline.getLayoutParams();
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });

        mContactTextViewLL.setOnClickListener(new View.OnClickListener() {
            LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabline.getLayoutParams();
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(2);
            }
        });
    }
    protected void resetTextView(){    //切换页面时统一将顶部tab字体都换成黑的先
        mChatTextView.setTextColor(Color.BLACK);
        mFindTextView.setTextColor(Color.BLACK);
        mContactTextView.setTextColor(Color.BLACK);
    }
    private void initTabline(){
        mTabline=(ImageView)findViewById(R.id.id_iv_tabline);

        /**这三行代码是获得窗口高度与宽度的*/
        Display display=getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics=new DisplayMetrics();
        display.getMetrics(outMetrics);

        mScreen1_3=outMetrics.widthPixels / 3;
        //这里是取得tabline的layoutparameter，tabline本来是在LinearLayout中的，但这里viewGroup的也可以
        ViewGroup.LayoutParams Ip=mTabline.getLayoutParams();
        Ip.width=mScreen1_3;
        mTabline.setLayoutParams(Ip);
    }

    public Handler getHandler(){
        return this.handler;
    }
}
