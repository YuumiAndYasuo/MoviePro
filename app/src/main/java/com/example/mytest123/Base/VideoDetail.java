package com.example.mytest123.Base;

import java.util.ArrayList;

public class VideoDetail {
    private String videoname;
    private String coverimage;
    private String director;
    private String videotype;
    private String actors;
    private String area;
    private String language;
    private String introduce;


    private ArrayList<PlayLink> playlists_m3u8=new ArrayList<PlayLink>();
    private ArrayList<PlayLink> playlists_mp4=new ArrayList<PlayLink>();

    public String getVideoname() {
        return videoname;
    }

    public void setVideoname(String videoname) {
        this.videoname = videoname;
    }

    public String getCoverimage() {
        return coverimage;
    }

    public void setCoverimage(String coverimage) {
        this.coverimage = coverimage;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getVideotype() {
        return videotype;
    }

    public void setVideotype(String videotype) {
        this.videotype = videotype;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public ArrayList<PlayLink> getPlaylists_m3u8() {
        return playlists_m3u8;
    }

    public void setPlaylists_m3u8(ArrayList<PlayLink> playlists_m3u8) {
        this.playlists_m3u8 = playlists_m3u8;
    }

    public ArrayList<PlayLink> getPlaylists_mp4() {
        return playlists_mp4;
    }

    public void setPlaylists_mp4(ArrayList<PlayLink> playlists_mp4) {
        this.playlists_mp4 = playlists_mp4;
    }

    @Override
    public String toString() {
        return "VideoDetail{" +
                "videoname='" + videoname + '\'' +
                ", coverimage='" + coverimage + '\'' +
                ", director='" + director + '\'' +
                ", videotype='" + videotype + '\'' +
                ", actors='" + actors + '\'' +
                ", area='" + area + '\'' +
                ", language='" + language + '\'' +
                ", introduce='" + introduce + '\'' +
                ", playlists_m3u8=" + playlists_m3u8 +
                ", playlists_mp4=" + playlists_mp4 +
                '}';
    }

}
