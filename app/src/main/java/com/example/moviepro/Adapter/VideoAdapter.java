package com.example.moviepro.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.moviepro.Base.VideoInfo;
import com.example.moviepro.R;

import java.util.List;

public class VideoAdapter extends ArrayAdapter<VideoInfo> {
    private int resourceId;
    public VideoAdapter(@NonNull Context context, int resource, @NonNull List<VideoInfo> objects) {
        super(context, resource, objects);
        resourceId=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        VideoInfo videoInfo=getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.videoname=(TextView)view.findViewById(R.id.videoname);
            viewHolder.videotype=(TextView)view.findViewById(R.id.videotype);
            view.setTag(viewHolder);
        }else {
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.videoname.setText(videoInfo.getVideoname());
        viewHolder.videotype.setText(videoInfo.getVideotype());
        return view;
    }
    class ViewHolder{
        TextView videoname;
        TextView videotype;
    }
}
