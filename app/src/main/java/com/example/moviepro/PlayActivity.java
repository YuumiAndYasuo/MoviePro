package com.example.moviepro;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.moviepro.Base.PlayLink;
import com.example.moviepro.Base.VideoDetail;
import com.example.moviepro.Utils.HttpUtil;
import com.example.moviepro.Utils.ParseHtml;

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

public class PlayActivity extends AppCompatActivity {

    //UI控件
    private IjkVideoView mVideoView;                //播放器
    private MyMediaController myMediaController;    //视频控制器
    private TableLayout mHudView;                   //视频详细信息
    private SurfaceRenderView mSurfaceRenderView;   //控制视频大小、旋转、好像是这样
    private ProgressBar mLoadVideoProgressbar;       //加载视频时显示该控件
    private Switch mRotationSwitch;                 //控制是否允许自动旋转
    private ActionBar mActionBar;                   //播放器标题栏
    private ImageView mToolbarBack;                 //返回
    private TextView mToolbarTitle;                 //视频标题

    //视频详细信息控件
    private Switch mPlaySourceSwitch;
    private ImageView coverimg;
    private TextView videotype;
    private TextView videoname;
    private TextView videodirector;
    private TextView videoactors;
    private TextView videoarea;
    private TextView videolanguage;
    private TextView videointroduce;
    private TextView playnum;

    //事件
    OrientationEventListener mOrientationEventListener;     //监听屏幕旋转角度

    //控制变量
    private GestureDetector gestureDetector;                //手势监听      监听步骤①
    private myGestureDetector myGestureDetector;            //具体的手势监听
    private int currentPosition=0;                          //当前播放位置
    private int screenHeight=0;                             //当前屏幕高度和宽度，使用前需初始化
    private int screenWidth=0;
    //    private boolean backpressed=false;                    //点击返回键
    private boolean enablerotation=false;                    //默认不允许旋转
    private boolean currentOriention_LANDSCAPE =false;      //当前是竖屏还是横屏
    private boolean enablem3u8=false;                        //默认播放mp4  false播放mp4
    private boolean switchPlaysourceStatus =true;              //是否首次加载切换播放源

    //视频播放比例宏定义
    private static final int SIZE_DEFAULT = 0;
    private static final int SIZE_4_3 = 1;
    private static final int SIZE_16_9 = 2;
    private int currentSize = SIZE_16_9;

    //数据变量
    VideoDetail videoDetail;                                //存放视频的播放链接以及其它详细信息


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //初始化
        init();

        //获取控件
        findView();

        //获取上个页面传递过来的视频详细链接
        Intent intent=getIntent();
        String type=intent.getStringExtra("type");
        String url=intent.getStringExtra("url");

        if(type.equals("live")){//播放直播
            mVideoView.setVideoPath(url);
            mVideoView.setMediaController(myMediaController);
            mVideoView.setHudView(mHudView);
            mVideoView.start();
            mPlaySourceSwitch.setVisibility(View.INVISIBLE);
            findViewById(R.id.gridlayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.videodetaillayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.playinginfo).setVisibility(View.INVISIBLE);
//            findViewById(R.id.rotationSwitch).setVisibility(View.INVISIBLE);
//            findViewById(R.id.autorotation_tips).setVisibility(View.INVISIBLE);

            setListener();

        }else if(type.equals("video")){
            //获取视频并进行相关处理
            getVideodata(url);
            //设置播放器
            setmVideoView();
            //设置监听
            setListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(backpressed&& !currentOriention_LANDSCAPE){
//            mVideoView.stopBackgroundPlay();
//            mVideoView.stopPlayback();
//            this.finish();
//        }else {//暂停播放 保存播放进度
        currentPosition=mVideoView.getCurrentPosition();
        mVideoView.pause();
//        }
//        backpressed=false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        myMediaController.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {                //监听步骤③
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
//        backpressed=true;
        if(!currentOriention_LANDSCAPE){
//            Toast.makeText(PlayActivity.this,"退出本activity#######",Toast.LENGTH_SHORT).show();
            mVideoView.stopPlayback();
            this.finish();
        }else{
//            Toast.makeText(PlayActivity.this,"退出全屏@@@@@@@@",Toast.LENGTH_SHORT).show();
            convertToPortScreen();
        }
    }

    //初始化
    private void init(){
        //初始化ijkplayer
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");


    }

    //获取所有的全局控件
    private void findView(){
        //播放器部分
        mVideoView=findViewById(R.id.ijk_video_View);
        mHudView=findViewById(R.id.hud_view);
        mLoadVideoProgressbar =findViewById(R.id.loadVideoProgress);
        myMediaController=new MyMediaController(this);

        //初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarBack=findViewById(R.id.toolbar_back);
        mToolbarTitle=findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        myMediaController.setSupportActionBar(mActionBar);

        //其它文字信息
        //获取各个控件
        mPlaySourceSwitch=findViewById(R.id.playsourceSwitch);
        coverimg=(ImageView)findViewById(R.id.videocover);
        videotype=findViewById(R.id.videotype);
        videoname=findViewById(R.id.videoname);
        videoactors=findViewById(R.id.videoactors);
        videodirector=findViewById(R.id.videodirector);
        videoarea=findViewById(R.id.videoarea);
        videolanguage=findViewById(R.id.videolanguage);
        videointroduce=findViewById(R.id.videointroduce);
        playnum=findViewById(R.id.playnum);

    }

    //请求视频播放链接等数据、并进行相关设置
    private void getVideodata(String videoDetailUrl){
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
                //获得网页返回的数据
                final String html=response.body().string();

                /**
                 * 开一个线程进行界面的设置，否则报错
                 * android.view.ViewRootImpl$CalledFromWrongThreadException:
                 * Only the original thread that created a view hierarchy can touch its views.
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    //获取视频详细信息
                    videoDetail= ParseHtml.parsedetailinfo(html);
                    //设置播放列表（选集列表）
                    setPlayList(videoDetail);
                    //设置详细信息
                    setDetailInfo(videoDetail);
                    //提示信息
                    Toast.makeText(PlayActivity.this,"正在加载，请稍后",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    //设置播放器
    private void setmVideoView(){
//        mVideoView.setVideoPath("http://download.xunleizuida.com/1904/喜羊羊与灰太狼之羊村守护者-06.mp4");
        mVideoView.setHudView(mHudView);
        mVideoView.setMediaController(myMediaController);
    }

    //添加监听事件
    private void setListener(){

        //添加屏幕旋转监听
        mOrientationEventListener=new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if(enablerotation){
                    if((orientation>150&&orientation<210)||(orientation>330||orientation<30)){//竖屏 自适应
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    }else if((orientation>240&&orientation<300)||(orientation>60&&orientation<120)){//横屏 自适应
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    }
                }
            }
        };
        if(enablerotation) {
            mOrientationEventListener.enable();     //启用该监听
        }


        //监听视频准备完成事件
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlayActivity.this,"加载完成",Toast.LENGTH_SHORT).show();
                    }
                });
                //加载成功，进度条不可见
                mLoadVideoProgressbar.setVisibility(View.INVISIBLE);
                //开始播放视频
                mp.start();


                //以下的监听需要视频加载出来才可以使用，故放在setOnPreparedListener中
                //监听全屏按钮
                myMediaController.setFullScreenListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!currentOriention_LANDSCAPE){//切换到全屏播放
                            Toast.makeText(getApplicationContext(),"切换到全屏播放"+ currentOriention_LANDSCAPE,Toast.LENGTH_SHORT).show();
                            convertToLandScreen();
                        }else {//退出全屏播放
                            Toast.makeText(getApplicationContext(),"退出全屏播放"+ currentOriention_LANDSCAPE,Toast.LENGTH_SHORT).show();
                            convertToPortScreen();
                        }
                    }
                });

                //监听自动旋转
                myMediaController.setRotationSwitchListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            mOrientationEventListener.enable();     //启用该监听
                        }else {
                            mOrientationEventListener.disable();    //禁用
                        }
                    }
                });

            }
        });

        //监听toolbar的返回键
        mToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertToPortScreen();
            }
        });

        //监听切换播放源开关
        mPlaySourceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //切换为mp4
                enablem3u8= !isChecked;
                switchPlaylist();
                //显示加载进度条
                mLoadVideoProgressbar.setVisibility(View.VISIBLE);
            }
        });
        //开始监听手势
        myGestureDetector=new myGestureDetector();                      //监听步骤②
        gestureDetector=new GestureDetector(getApplicationContext(),myGestureDetector);
    }

    //设置视频的选集列表
    private void setPlayList(VideoDetail videoDetail){
        final GridLayout gridLayout=(GridLayout)findViewById(R.id.gridlayout);
        GridLayout.LayoutParams params = null;
        //移除该布局上的所有控件
        gridLayout.removeAllViews();
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        final ArrayList<PlayLink> tmp;
        if(enablem3u8){
            tmp=videoDetail.getPlaylists_m3u8();
        }else {
            tmp=videoDetail.getPlaylists_mp4();
        }
//      final ArrayList<PlayLink> tmp=videoDetail.getPlaylists_mp4();

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
    }

    //设置视频的详细信息，并添加播放链接
    private void setDetailInfo(VideoDetail videoDetail){
        Glide.with(PlayActivity.this).load(videoDetail.getCoverimage()).into(coverimg);

        if(enablem3u8){
            mVideoView.setVideoPath(videoDetail.getPlaylists_m3u8().get(0).getVideourl());
            mToolbarTitle.setText(videoDetail.getVideoname()+" "+videoDetail.getPlaylists_m3u8().get(0).getVideonum());
            playnum.setText(videoDetail.getPlaylists_m3u8().get(0).getVideonum());
        }else {
            mVideoView.setVideoPath(videoDetail.getPlaylists_mp4().get(0).getVideourl());
            mToolbarTitle.setText(videoDetail.getVideoname()+" "+videoDetail.getPlaylists_mp4().get(0).getVideonum());
            playnum.setText(videoDetail.getPlaylists_mp4().get(0).getVideonum());
        }

        //首次执行时加载
        if(switchPlaysourceStatus){
            videotype.setText(videoDetail.getVideotype());
            videoname.setText(videoDetail.getVideoname());
            videodirector.setText(videoDetail.getDirector());
            videoactors.setText(videoDetail.getActors());
            videoarea.setText(videoDetail.getArea());
            videolanguage.setText(videoDetail.getLanguage());
            videointroduce.setText("\u3000\u3000" +videoDetail.getIntroduce());
        }
    }

    //设置视频的选集列表和详细信息，并添加播放链接
    private void switchPlaylist(){
        setPlayList(videoDetail);
        setDetailInfo(videoDetail);
        switchPlaysourceStatus =false;
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
//        Toast.makeText(this,"屏幕旋转！！！",Toast.LENGTH_SHORT).show();
        initScreenInfo();
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){//切换为横屏
            convertToLandScreen();
//            currentOriention_LANDSCAPE =true;
        }else {
            convertToPortScreen();
//            currentOriention_LANDSCAPE =false;
        }
        //设置纵横比
//        setScreenRate(currentSize);
    }

    //正常播放
    private void convertToPortScreen() {
        currentOriention_LANDSCAPE =false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置videoView竖屏播放
        initScreenInfo();
        int height=screenHeight;
        int width=screenWidth;
        FrameLayout relativeLayout=findViewById(R.id.media_box);
        mHudView.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(width,width*9/16);
        relativeLayout.setLayoutParams(params);

    }

    //全屏播放
    private void convertToLandScreen() {
        currentOriention_LANDSCAPE =true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置videoView横屏播放
        initScreenInfo();
        int height=screenHeight;
        int width=screenWidth;
        FrameLayout relativeLayout=findViewById(R.id.media_box);
        mHudView.setVisibility(View.INVISIBLE);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(width,height);
        relativeLayout.setLayoutParams(layoutParams);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    /**
     * 内部类 播放器的手势控制
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
            if(!currentOriention_LANDSCAPE){//切换到全屏播放
//                currentOriention_LANDSCAPE =true;
//                Toast.makeText(getApplicationContext(),"切换到全屏播放"+ currentOriention_LANDSCAPE,Toast.LENGTH_SHORT).show();
                convertToLandScreen();


            }else {//退出全屏播放
//                currentOriention_LANDSCAPE =false;
//                Toast.makeText(getApplicationContext(),"退出全屏播放"+ currentOriention_LANDSCAPE,Toast.LENGTH_SHORT).show();
                convertToPortScreen();
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
//                Toast.makeText(getApplicationContext(),"暂停播放",Toast.LENGTH_SHORT).show();
                mVideoView.pause();
            }else {
//                Toast.makeText(getApplicationContext(),"继续播放",Toast.LENGTH_SHORT).show();
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

//            Toast.makeText(getApplicationContext(),rotation+":"+width+":"+height,Toast.LENGTH_SHORT).show();

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

//        //正常播放
//        private void convertToPortScreen() {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            int height=getScreenHeight();
//            int width=getScreenWidth();
//            FrameLayout relativeLayout=findViewById(R.id.media_box);
//            mHudView.setVisibility(View.VISIBLE);
//            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(height,width*2/5);
//
//            relativeLayout.setLayoutParams(params);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置videoView竖屏播放
//        }
//
//        //全屏播放
//        private void convertToLandScreen() {
//            int height=getScreenHeight();
//            int width=getScreenWidth();
//            FrameLayout relativeLayout=findViewById(R.id.media_box);
//            mHudView.setVisibility(View.INVISIBLE);
//            FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(height,width);
//            relativeLayout.setLayoutParams(layoutParams);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置videoView横屏播放
//        }
    }
}

