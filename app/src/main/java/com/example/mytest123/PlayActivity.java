package com.example.mytest123;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.mytest123.Base.PlayLink;
import com.example.mytest123.Base.VideoDetail;
import com.example.mytest123.Utils.HttpUtil;
import com.example.mytest123.Utils.ParseHtml;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.example.widget.media.MyMediaController;
import tv.danmaku.ijk.media.example.widget.media.SurfaceRenderView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayActivity extends Activity {

    //UI控件
    private IjkVideoView mVideoView;                //播放器
    private MyMediaController myMediaController;    //视频控制器
    private TableLayout mHudView;                   //视频详细信息
    private SurfaceRenderView mSurfaceRenderView;   //控制视频大小、旋转、好像是这样


    private ImageView coverimg;
    private TextView videotype;
    private TextView videoname;
    private TextView videodirector;
    private TextView videoactors;
    private TextView videoarea;
    private TextView videolanguage;
    private TextView videointroduce;
    private TextView playnum;

    //控制变量
    private boolean screen_vertical=true;                //当前是竖屏还是横屏
    private GestureDetector gestureDetector;        //手势监听      监听步骤①
    private myGestureDetector myGestureDetector;    //具体的手势监听
    private int currentPosition=0;                  //当前播放位置
    private int screenHeight=0;                     //当前屏幕高度和宽度，使用前需初始化
    private int screenWidth=0;

    //视频播放比例宏定义
    private static final int SIZE_DEFAULT = 0;
    private static final int SIZE_4_3 = 1;
    private static final int SIZE_16_9 = 2;
    private int currentSize = SIZE_16_9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        init();
        findView();


        Intent intent=getIntent();
        final String videoDetailUrl=intent.getStringExtra("url");

        HttpUtil.get(videoDetailUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlayActivity.this,"加载视频失败，请刷新重试",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String html=response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //获取视频详细信息
                        final VideoDetail videoDetail= ParseHtml.parsedetailinfo(html);
                        //获取各个控件
                        coverimg=(ImageView)findViewById(R.id.videocover);
                        videotype=findViewById(R.id.videotype);
                        videoname=findViewById(R.id.videoname);
                        videoactors=findViewById(R.id.videoactors);
                        videodirector=findViewById(R.id.videodirector);
                        videoarea=findViewById(R.id.videoarea);
                        videolanguage=findViewById(R.id.videolanguage);
                        videointroduce=findViewById(R.id.videointroduce);
                        playnum=findViewById(R.id.playnum);

                        //设置播放列表
                        final GridLayout gridLayout=(GridLayout)findViewById(R.id.gridlayout);
                        GridLayout.LayoutParams params = null;
                        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
                        final ArrayList<PlayLink> tmp=videoDetail.getPlaylists_mp4();
                        int length=tmp.size();
                        int i,j,k=0;
                        for(i=0;i<length/4+1;i++){
                            for(j=0;j<4;j++){
                                if(k>=length){
                                    break;
                                }
                                final Button button=new Button(PlayActivity.this);
                                button.setText(tmp.get(k).getVideonum());
                                button.setWidth((screenWidth-60)/4);
                                if(k==0){
                                    button.setBackgroundColor(Color.parseColor("#fb7299"));
                                }
//                                button.
                                //设置行
                                GridLayout.Spec rowSpec=GridLayout.spec(i);
                                //设置列
                                GridLayout.Spec columnSpec=GridLayout.spec(j);
                                params=new GridLayout.LayoutParams(rowSpec,columnSpec);
                                params.setMargins(5, 5, 5, 5);
                                final int index=k;

                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mVideoView.setVideoPath(tmp.get(index).getVideourl());
                                        for (int i = 0; i < gridLayout.getChildCount(); i++) {//清空所有背景色
                                            Button b = (Button) gridLayout.getChildAt(i);
                                            b.setBackgroundColor(Color.parseColor("#d6d7d7"));
                                        }
                                        //设置选中的按钮背景色
                                        button.setBackgroundColor(Color.parseColor("#fb7299"));
                                        playnum.setText(tmp.get(index).getVideonum());
                                        Toast.makeText(PlayActivity.this,"正在加载"+tmp.get(index).getVideonum(),Toast.LENGTH_SHORT).show();

                                    }
                                });
                                gridLayout.addView(button,params);
                                k++;
                            }
                        }
                        //设置各控件内容
                        Glide.with(PlayActivity.this).load(videoDetail.getCoverimage()).into(coverimg);
                        mVideoView.setVideoPath(videoDetail.getPlaylists_mp4().get(0).getVideourl());

                        videotype.setText(videoDetail.getVideotype());
                        videoname.setText(videoDetail.getVideoname());
                        videodirector.setText(videoDetail.getDirector());
                        videoactors.setText(videoDetail.getActors());
                        videoarea.setText(videoDetail.getArea());
                        videolanguage.setText(videoDetail.getLanguage());
                        videointroduce.setText("\u3000\u3000" +videoDetail.getIntroduce());


//                        mVideoView.setMediaController(myMediaController);
                        Toast.makeText(PlayActivity.this,"正在加载，请稍后",Toast.LENGTH_LONG).show();
//                        videoView.seekTo(300);
                    }
                });


            }
        });

        setmVideoView();
        setListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPosition=mVideoView.getCurrentPosition();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {                //监听步骤③
        return gestureDetector.onTouchEvent(event);
    }

    //初始化
    private void init(){
        //初始化ijkplayer
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    //获取所有的全局控件
    private void findView(){
        mVideoView=findViewById(R.id.ijk_video_View);
        mHudView=findViewById(R.id.hud_view);
        myMediaController=new MyMediaController(this);
//        mSurfaceRenderView=new SurfaceRenderView(this);
    }

    //设置播放器
    private void setmVideoView(){
//        mVideoView.setVideoPath("http://download.xunleizuida.com/1904/喜羊羊与灰太狼之羊村守护者-06.mp4");
        mVideoView.setHudView(mHudView);
        mVideoView.setMediaController(myMediaController);
    }

    //添加监听事件
    private void setListener(){
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setScreenRate(currentSize);
                        Toast.makeText(PlayActivity.this,"加载完成",Toast.LENGTH_SHORT).show();
                    }
                });
                mp.start();
            }
        });

        //开始监听手势
        myGestureDetector=new myGestureDetector();                      //监听步骤②
        gestureDetector=new GestureDetector(getApplicationContext(),myGestureDetector);
    }

    //获取屏幕的宽高
    private void initScreenInfo(){
        screenWidth=((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        screenHeight= ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }

    //设置视频播放比例
    public void setScreenRate(int rate) {
        int width = 0;
        int height = 0;
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {// 横屏
            if (rate == SIZE_DEFAULT) {
                width = mVideoView.getmVideoWidth();
                height = mVideoView.getmVideoHeight();
            } else if (rate == SIZE_4_3) {
                width = screenHeight / 3 * 4;
                height = screenHeight;
            } else if (rate == SIZE_16_9) {
                width = screenHeight / 9 * 16;
                height = screenHeight;
            }
        } else { //竖屏
            if (rate == SIZE_DEFAULT) {
                width = mVideoView.getmVideoWidth();
                height = mVideoView.getmVideoHeight();
            } else if (rate == SIZE_4_3) {
                width = screenWidth;
                height = screenWidth * 3 / 4;
            } else if (rate == SIZE_16_9) {
                width = screenWidth;
                height = screenWidth * 9 / 16;
            }
        }
        if (width > 0 && height > 0) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mVideoView.getmRenderView().getView().getLayoutParams();
            lp.width = width;
            lp.height = height;
            mVideoView.getmRenderView().getView().setLayoutParams(lp);
        }
    }

    //屏幕方向切换
    private void fullChangeScreen() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {// 切换为竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override//全屏播放 根据旋转切换
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //重新获取屏幕的宽和高
        initScreenInfo();
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){//切换为横屏
            LinearLayout.LayoutParams layoutParams=(LinearLayout.LayoutParams)mVideoView.getLayoutParams();
            layoutParams.height=screenHeight;
            layoutParams.width=screenWidth;
            mVideoView.setLayoutParams(layoutParams);
        }else {
            LinearLayout.LayoutParams layoutParams=(LinearLayout.LayoutParams)mVideoView.getLayoutParams();
            layoutParams.height=screenWidth*9/16;
            layoutParams.width=screenWidth;
            mVideoView.setLayoutParams(layoutParams);
        }
        setScreenRate(currentSize);
    }

    /**
     * 内部类 播放器的控制
     * 双击控制         播放/暂停
     * 长按控制         全屏/正常
     * 左划右划         快退/快进
     * 左边上下         亮度调整
     * 右边上下         音量调整
     */
    public class myGestureDetector extends GestureDetector.SimpleOnGestureListener {
        AudioManager audioManager=(AudioManager)getSystemService(Service.AUDIO_SERVICE);
        float FLIP_DISTANCE = 50;               //高于此数值的滑动才触发滑动事件

        public myGestureDetector() {
            super();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {

//            getScreenOrientation();
            if(screen_vertical){//切换到全屏播放
                Toast.makeText(getApplicationContext(),"切换到全屏播放"+screen_vertical,Toast.LENGTH_SHORT).show();
                convertToLandScreen();
                screen_vertical=false;

            }else {//退出全屏播放
                Toast.makeText(getApplicationContext(),"退出全屏播放"+screen_vertical,Toast.LENGTH_SHORT).show();
                convertToPortScreen();
                screen_vertical=true;
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > FLIP_DISTANCE) {//快退
                Toast.makeText(getApplicationContext(),"快退"+(swipe_time(e1.getX(),e2.getX())/1000)+"秒",Toast.LENGTH_SHORT).show();
                int seektime=mVideoView.getCurrentPosition()-swipe_time(e1.getX(),e2.getX());
                if(seektime<0){
                    seektime=0;
                }
                mVideoView.seekTo(seektime);
                return true;
            }
            if (e2.getX() - e1.getX() > FLIP_DISTANCE) {//快进
                Toast.makeText(getApplicationContext(),"快进"+(swipe_time(e2.getX(),e1.getX())/1000)+"秒",Toast.LENGTH_SHORT).show();
                int seektime=mVideoView.getCurrentPosition()+swipe_time(e2.getX(),e1.getX());
                if(seektime>mVideoView.getDuration()){
                    seektime=mVideoView.getDuration();
                }
                mVideoView.seekTo(seektime);
                return true;
            }
            if (e1.getY() - e2.getY() > FLIP_DISTANCE) {//增加音量或亮度
                int width=getScreenWidth();
                int height=getScreenHeight();

                int currentvolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int changedvolume=currentvolume+(int) ((e1.getY()-e2.getY())/150);
                if(e1.getX()<width/2.0){    //左半屏   更改亮度
                    float brightness=(e1.getY()-e2.getY())/height;
                    if(brightness>1.0){
                        brightness=1.0f;
                    }else if(brightness<0.15){
                        brightness=0.15f;
                    }
                    Toast.makeText(getApplicationContext(),"当前亮度："+(int)(brightness*100)+"%",Toast.LENGTH_SHORT).show();
                    Window window=getWindow();
                    WindowManager.LayoutParams layoutParams=window.getAttributes();
                    layoutParams.screenBrightness=brightness;
                    window.setAttributes(layoutParams);
                }else{                      //右半屏   增加音量
                    if(changedvolume>audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)){
                        changedvolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    }
                    Toast.makeText(getApplicationContext(),"当前音量："+changedvolume,Toast.LENGTH_SHORT).show();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,changedvolume,AudioManager.FLAG_PLAY_SOUND);
                }
                return true;
            }
            if (e2.getY() - e1.getY() > FLIP_DISTANCE) {//减小音量或亮度
                int width=getScreenWidth();
                int height=getScreenHeight();

                int currentvolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int changedvolume=currentvolume-(int) ((e2.getY()-e1.getY())/150);
                if(e2.getX()<width/2.0){    //左半屏   更改亮度
                    float brightness=(e2.getY()-e1.getY())/height;
                    if(brightness>1.0){
                        brightness=1.0f;
                    }else if(brightness<0.15){
                        brightness=0.05f;
                    }
                    Toast.makeText(getApplicationContext(),"当前亮度："+(int)(brightness*100)+"%",Toast.LENGTH_SHORT).show();
                    Window window=getWindow();
                    WindowManager.LayoutParams layoutParams=window.getAttributes();
                    layoutParams.screenBrightness=brightness;
                    window.setAttributes(layoutParams);
                }else{                      //右半屏   减少音量
                    if(changedvolume<0){
                        changedvolume=0;
                    }
                    Toast.makeText(getApplicationContext(),"当前音量："+changedvolume,Toast.LENGTH_SHORT).show();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,changedvolume,AudioManager.FLAG_PLAY_SOUND);
                }
                return true;
            }

            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            Toast.makeText(BaseApplication.getContext(),"双击",Toast.LENGTH_SHORT).show();
            if(mVideoView.isPlaying()){
                Toast.makeText(getApplicationContext(),"暂停播放",Toast.LENGTH_SHORT).show();
                mVideoView.pause();
            }else {
                Toast.makeText(getApplicationContext(),"继续播放",Toast.LENGTH_SHORT).show();
                mVideoView.start();
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return super.onContextClick(e);
        }

        //返回快进或快退多少毫米
        int swipe_time(double x1,double x2){
            return (int) ((x1-x2)*100);
        }
        //获取屏幕宽度
        int getScreenWidth(){
            int width = 0;
            width=((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            return width;
        }

        //获取屏幕高度
        int getScreenHeight(){
            int height=0;
            height= ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
            return height;
        }

        //获取屏幕方向
        private int getScreenOrientation() {
            //rotation 0 1 2 3分别代表顺时针旋转0、90、180、270
            int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            int width = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            int height = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();

            Toast.makeText(getApplicationContext(),rotation+":"+width+":"+height,Toast.LENGTH_SHORT).show();

            int orientation;
            // if the device's natural orientation is portrait:
            if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
                    (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
                switch (rotation) {
                    case Surface.ROTATION_0:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_90:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_180:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                        break;
                    case Surface.ROTATION_270:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                    default:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                }
            }
            // if the device's natural orientation is landscape or if the device
            // is square:
            else {
                switch (rotation) {
                    case Surface.ROTATION_0:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_90:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_180:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                    case Surface.ROTATION_270:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                        break;
                    default:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                }
            }
            return orientation;
        }



        private int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        //正常播放
        private void convertToPortScreen() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            int height=getScreenHeight();
            int width=getScreenWidth();
            LinearLayout relativeLayout=findViewById(R.id.media_box);
            mHudView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(height,width*2/5);

            relativeLayout.setLayoutParams(params);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置videoView竖屏播放
        }

        //全屏播放
        private void convertToLandScreen() {
            int height=getScreenHeight();
            int width=getScreenWidth();
            LinearLayout relativeLayout=findViewById(R.id.media_box);
            mHudView.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(height,width);
            relativeLayout.setLayoutParams(layoutParams);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置videoView横屏播放
        }
    }
}
