package com.example.mytest123.Base;

public class PlayLink {//播放链接
    private String videonum;//第几集或者该集对应的名称      第1集/BD超清中字
    private String videourl;//对应播放链接                  https://you-cdn.maque-zuida.com/20200209/ycH6Japb/index.m3u8

    public PlayLink(String videonum, String videourl) {//构造方法
        this.videonum = videonum;
        this.videourl = videourl;
    }

    public String getVideonum() {
        return videonum;
    }

    public void setVideonum(String videonum) {
        this.videonum = videonum;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }
}
