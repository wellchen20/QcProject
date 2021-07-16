package com.xylink.sdk.sample.view;

import android.content.Context;
import android.log.L;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ainemo.sdk.otf.VideoInfo;

import com.robert.maps.applib.R;
import com.xylink.sdk.sample.view.VideoCellLayout.OnVideoCellListener;
import com.xylink.sdk.sample.view.VideoCell.OnCellEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class VideoCellGroup extends ViewGroup implements VideoCellLayout, OnCellEventListener {

    private static final String TAG = "VideoCellGroup";

    private OnVideoCellListener onVideoCellListener;

    private static final int RENDER_FRAME_RATE = 15;

    /**
     * 本地视频信息
     */
    protected VideoInfo mLocalVideoInfo;
    /**
     * 本地视频单元（camera preview）
     */
    protected volatile VideoCell mLocalVideoCell;
    /**
     * 远端视频信息
     */
    protected volatile List<VideoInfo> mRemoteVideoInfos;
    /**
     * 远端视频单元(content & video)
     */
    protected volatile List<VideoCell> mRemoteVideoCells;

    protected int mWidth;

    protected int mHeight;

    protected int mCellPadding;

    protected Runnable mRenderRunnabler = new Runnable() {
        @Override
        public void run() {
            mLocalVideoCell.requestRender();
            for (VideoCell cell : mRemoteVideoCells) {
                cell.requestRender();
            }
            requestRender(true);
        }
    };

    private void requestRender(boolean isRendering) {
        removeCallbacks(mRenderRunnabler);
        if (isRendering) {
            if (getVisibility() == VISIBLE) {
                postDelayed(mRenderRunnabler, 1000/RENDER_FRAME_RATE);
            }
        }
    }

    public VideoCellGroup(Context context) {
        this(context, null);
    }

    public VideoCellGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoCellGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //设置背景色
        setBackgroundColor(getResources().getColor(R.color.black));

        mCellPadding = (int) getResources().getDimension(R.dimen.local_cell_pandding);

        //创建本地视频
        createLocalCell(false);

        mRemoteVideoCells = new CopyOnWriteArrayList<VideoCell>();
    }

    protected abstract void createLocalCell(boolean isUvc);


    @Override
    public void setMuteLocalAudio(boolean mute) {
        mLocalVideoCell.setMuteAudio(mute);
    }

    @Override
    public void setMuteLocalVideo(boolean mute, String reason) {
        mLocalVideoCell.setMuteVideo(mute,reason);
    }

    @Override
    public void setAudioOnlyMode(boolean flag) {
        mLocalVideoCell.setAudioOnly(flag);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        L.i(TAG, "onSizeChanged, mWidth : " + mWidth + ", mHeight = " + mHeight);

        requestLayout();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        requestRender(visibility == VISIBLE);
        super.onVisibilityChanged(changedView, visibility);
    }

    /**
     * 开始渲染
     */
    @Override
    public void startRender() {
        mLocalVideoCell.onResume();

        for (VideoCell cell : mRemoteVideoCells) {
            cell.onResume();
        }

        requestRender(true);
    }

    /**
     * 暂停渲染
     */
    @Override
    public void pauseRender() {
        mLocalVideoCell.onPause();

        for (VideoCell cell : mRemoteVideoCells) {
            cell.onPause();
        }

        requestRender(false);
    }

    /**
     * 销毁资源
     */
    @Override
    public void destroy() {
        mRemoteVideoCells.clear();
        requestRender(false);
    }

    /**
     * 设置本地视频信息
     * @param localViewInfo
     */
    @Override
    public void setLocalVideoInfo(VideoInfo localViewInfo) {
        mLocalVideoInfo = localViewInfo;
        if(mLocalVideoCell != null) {
            mLocalVideoCell.setLayoutInfo(mLocalVideoInfo);
            L.i(TAG, "setLocalVideoInfo, mLocalVideoInfo " + mLocalVideoInfo);
        }
    }

    /**
     * 更换usb camera
     * @param isUvc
     */
    @Override
    public void updateCamera(boolean isUvc) {
        if(mLocalVideoCell != null){
            mLocalVideoCell.updateCamrea(isUvc);
        }
    }

    public void setOnVideoCellListener(OnVideoCellListener listener) {
        onVideoCellListener = listener;
    }

    @Override
    public void onLongPress(MotionEvent e, VideoCell cell) {
        if(onVideoCellListener != null) {
            onVideoCellListener.onLongPress(e, cell);
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, VideoCell cell) {
        if(onVideoCellListener != null) {
            return onVideoCellListener.onDoubleTap(e, cell);
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
        if(onVideoCellListener != null) {
            return onVideoCellListener.onSingleTapConfirmed(e, cell);
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell) {
        if(onVideoCellListener != null) {
            return onVideoCellListener.onScroll(e1, e2, distanceX, distanceY, cell);
        }
        return false;
    }

    @Override
    public void onShakeDone(VideoCell cell) {
        if(onVideoCellListener != null) {
            onVideoCellListener.onShakeDone(cell);
        }
    }

    @Override
    public void onCancelAddother(VideoCell cell) {
        if(onVideoCellListener != null) {
            onVideoCellListener.onCancelAddother(cell);
        }
    }
}
