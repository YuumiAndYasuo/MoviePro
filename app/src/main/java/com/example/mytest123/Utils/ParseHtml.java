package com.example.mytest123.Utils;

import com.example.mytest123.Base.PlayLink;
import com.example.mytest123.Base.VideoDetail;
import com.example.mytest123.Base.VideoInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ParseHtml {
    /**
     * 解析搜索结果
     * @param html
     * @return
     */
    public static ArrayList<VideoInfo> parsebaseinfo(String html){
        Document document= Jsoup.parse(html);
        Elements elements=document.getElementsByClass("xing_vb4");
        Elements videotype=document.getElementsByClass("xing_vb5");
        Elements updatetime=document.getElementsByClass("xing_vb6");


        ArrayList arrayList=new ArrayList();
        int i=0;
        for(Element element:videotype){
            arrayList.add(element.text());
//            System.out.println(arrayList.get(i));
            i++;
        }

        i=0;
        ArrayList<VideoInfo> searchresult=new ArrayList<VideoInfo>();
        for (Element element:elements){
            if(arrayList.get(i).toString().equals("福利片")){
                i++;
                continue;
            }
            Elements titleEle=element.select("a");
//            System.out.println("影片名称："+titleEle.text());
//            System.out.println("影片链接："+titleEle.attr("href"));
//            System.out.println("影片类型："+arrayList.get(i));
            VideoInfo videoInfo=new VideoInfo();
            videoInfo.setVideoname(titleEle.text());
            videoInfo.setVideourl(titleEle.attr("href"));
            videoInfo.setVideotype(arrayList.get(i).toString());
            searchresult.add(videoInfo);
//            System.out.println("*************************************");
            i++;
        }
//        System.out.println(searchresult.toString());
        return searchresult;
    }

    /**
     * 解析视频详细信息（播放链接。。。）
     * @param html
     * @return
     */
    public static VideoDetail parsedetailinfo(String html){
        Document document= Jsoup.parse(html);

        VideoDetail videoDetail=new VideoDetail();
        Elements videoname=document.select(".vodh h2");
//        System.out.println("影片名称："+videoname.text());
        Elements director=document.select(".vodinfobox li:nth-child(2) span");
//        System.out.println("影片导演："+director.text());
        Elements actors=document.select(".vodinfobox li:nth-child(3) span");
//        System.out.println("影片主演："+actors.text());
        Elements videotype=document.select(".vodinfobox li:nth-child(4) span");
//        System.out.println("影片类型："+videotype.text());
        Elements area=document.select(".vodinfobox li:nth-child(5) span");
//        System.out.println("影片地区："+area.text());
        Elements language=document.select(".vodinfobox li:nth-child(6) span");
//        System.out.println("影片语言："+language.text());
        Elements introduce=document.select(".vodinfobox li.cont span.more");
//        System.out.println("影片简介："+introduce.text());
        Elements coverimg=document.select(".warp div:nth-child(1) .vodImg img");
//        System.out.println("影片封面："+coverimg.attr("src"));

        //获取m3u8播放链接
        Elements elements=document.select("#play_1 ul li");
        for(Element element:elements){
            String[]content=element.text().split("\\$");
            videoDetail.getPlaylists_m3u8().add(new PlayLink(content[0],content[1]));
            System.out.println(element.text());
        }

        //获取mp4播放链接
        elements=document.select("#down_1 ul li");
        for(Element element:elements){
            String[]content=element.text().split("\\$");
            videoDetail.getPlaylists_mp4().add(new PlayLink(content[0],content[1]));
            System.out.println(element.text());
        }



        //为videodetail添加内容
        videoDetail.setVideoname(videoname.text());
        videoDetail.setCoverimage(coverimg.attr("src"));
        videoDetail.setDirector(director.text());
        videoDetail.setVideotype(videotype.text());
        videoDetail.setActors(actors.text());
        videoDetail.setArea(area.text());
        videoDetail.setLanguage(language.text());
        videoDetail.setIntroduce(introduce.text());


        return videoDetail;

    }
}
