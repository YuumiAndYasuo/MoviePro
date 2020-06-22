package com.example.moviepro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moviepro.Adapter.VideoAdapter;
import com.example.moviepro.Base.VideoInfo;
import com.example.moviepro.Utils.HttpUtil;
import com.example.moviepro.Utils.ParseHtml;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    EditText mSearch_edit;
    Button mSearchButton;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findView();

        initlive();

        addListener();

    }

    private void initlive(){
        final ArrayList<VideoInfo> videolist=new ArrayList<>();

        videolist.add(new VideoInfo("cctv1  综合","直播","http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8"));
        videolist.add(new VideoInfo("cctv2  财经","直播","http://ivi.bupt.edu.cn/hls/cctv2hd.m3u8"));
        videolist.add(new VideoInfo("cctv3  综艺","直播","http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8"));
        videolist.add(new VideoInfo("cctv4  中文国际（亚）","直播","http://ivi.bupt.edu.cn/hls/cctv4hd.m3u8"));
        videolist.add(new VideoInfo("cctv5  体育","直播","http://120.241.133.167/outlivecloud-cdn.ysp.cctv.cn/cctv/2000205103.m3u8"));
        videolist.add(new VideoInfo("cctv5+ 体育赛事","直播","http://ivi.bupt.edu.cn/hls/cctv5phd.m3u8"));
        videolist.add(new VideoInfo("cctv6  电影","直播","http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8"));
        videolist.add(new VideoInfo("cctv7  国防军事","直播","http://ivi.bupt.edu.cn/hls/cctv7hd.m3u8"));
        videolist.add(new VideoInfo("cctv8  电视剧","直播","http://ivi.bupt.edu.cn/hls/cctv8hd.m3u8"));
        videolist.add(new VideoInfo("cctv9  记录","直播","http://ivi.bupt.edu.cn/hls/cctv9hd.m3u8"));
        videolist.add(new VideoInfo("cctv10 科教","直播","http://ivi.bupt.edu.cn/hls/cctv10hd.m3u8"));
        videolist.add(new VideoInfo("cctv11 戏曲","直播","http://120.241.133.167/outlivecloud-cdn.ysp.cctv.cn/cctv/2000204103.m3u8"));
        videolist.add(new VideoInfo("cctv12 社会与法","直播","http://ivi.bupt.edu.cn/hls/cctv12hd.m3u8"));
        videolist.add(new VideoInfo("cctv13 新闻","直播","http://120.241.133.167/outlivecloud-cdn.ysp.cctv.cn/cctv/2000204603.m3u8"));
        videolist.add(new VideoInfo("cctv14 少儿","直播","http://ivi.bupt.edu.cn/hls/cctv14hd.m3u8"));
        videolist.add(new VideoInfo("cctv15 音乐","直播","http://120.241.133.167/outlivecloud-cdn.ysp.cctv.cn/cctv/2000205003.m3u8"));
        videolist.add(new VideoInfo("cctv17 农业农村","直播","http://ivi.bupt.edu.cn/hls/cctv17hd.m3u8"));

        VideoAdapter videoAdapter=new VideoAdapter(SearchActivity.this,R.layout.videolist,videolist);

        listView.setAdapter(videoAdapter);

        //监听listview中的每一项
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfo videoInfo = videolist.get(position);
//                Toast.makeText(SearchActivity.this,videoInfo.getVideoname(),Toast.LENGTH_SHORT).show();
                //从当前页面跳转到播放页面，并将选中视频信息传递过去
                Intent intent=new Intent(SearchActivity.this,PlayActivity.class);
                intent.putExtra("url",videoInfo.getVideourl());
                intent.putExtra("type","live");
                startActivity(intent);
            }
        });
    }

    //获取控件
    private void findView(){
        mSearch_edit=(EditText) findViewById(R.id.search_edit);
        mSearchButton=findViewById(R.id.search_button);
        listView=(ListView)findViewById(R.id.videolist);
    }

    //添加监听
    private void addListener(){
        mSearch_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event!=null&& KeyEvent.KEYCODE_ENTER==event.getKeyCode()&&KeyEvent.ACTION_DOWN==event.getAction()){
                    search_video();
                    return true;
                }
                return false;
            }
        });
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_video();
            }
        });
    }


    //搜索视频
    private void search_video(){
        String keyword=mSearch_edit.getText().toString();
        Toast.makeText(SearchActivity.this,"搜索词："+keyword,Toast.LENGTH_SHORT).show();
        RequestBody requestBody=new FormBody.Builder()
                .add("m","vod-search")
                .add("wd",keyword)
                .add("submit","search")
                .build();
        HttpUtil.post("http://www.zuidazy5.com/index.php?m=vod-search", requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchActivity.this,"搜索失败，请重新搜索",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String html=response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<VideoInfo> videolist= ParseHtml.parsebaseinfo(html);
                        VideoAdapter videoAdapter=new VideoAdapter(SearchActivity.this,R.layout.videolist,videolist);
                        ListView listView=(ListView)findViewById(R.id.videolist);
                        listView.setAdapter(videoAdapter);

                        //监听listview中的每一项
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                VideoInfo videoInfo = videolist.get(position);
                                Toast.makeText(SearchActivity.this,videoInfo.getVideoname(),Toast.LENGTH_SHORT).show();
                                //从当前页面跳转到播放页面，并将选中视频信息传递过去
                                Intent intent=new Intent(SearchActivity.this,PlayActivity.class);
                                intent.putExtra("url","http://www.zuidazy5.com"+videoInfo.getVideourl());
                                intent.putExtra("type","video");
                                startActivity(intent);
                            }
                        });
//                                Toast.makeText(indexActivity.this,"成功"+html,Toast.LENGTH_LONG).show();
                    }
                });

//                        Log.d("tag", "onResponse: "+html);
            }
        });
    }


}
