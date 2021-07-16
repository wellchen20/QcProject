/**
 * Copyright (C) 2019 XYLink Android SDK Source Project
 *
 * Created by wanghui on 2019/1/22.
 */
package com.xylink.sdk.sample.view;

import android.content.Context;
import android.util.AttributeSet;

import com.ainemo.sdk.otf.VideoInfo;

import java.util.List;

/**
 * 演讲模式视频布局视图类（1大屏+N小屏）
 */
public class SpeakerVideoView extends VideoCellGroup {
    public SpeakerVideoView(Context context) {
        super(context);
    }

    public SpeakerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeakerVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createLocalCell(boolean isUvc) {
        mLocalVideoCell = new VideoCell(isUvc,false, getContext(), this);
        mLocalVideoCell.setId(VideoCell.LOCAL_VIEW_ID);
        mLocalVideoCell.bringToFront();
        mLocalVideoCell.setLayoutInfo(mLocalVideoInfo);

        addView(mLocalVideoCell);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void setRemoteVideoInfos(List<VideoInfo> infos) {

    }
}
