package com.xylink.sdk.sample.view;

import android.view.MotionEvent;

import com.ainemo.sdk.otf.VideoInfo;

import java.util.List;

public interface VideoCellLayout {

    /**
     * 本地静音
     * @param mute
     */
    void setMuteLocalAudio(boolean mute);

    /**
     * 关闭视频
     * @param mute
     * @param reason
     */
    void setMuteLocalVideo(boolean mute, String reason);

    /**
     * 语音模式
     * @param flag
     */
    void setAudioOnlyMode(boolean flag);

    /**
     * 设置本地视频信息
     * @param localViewInfo
     */
    void setLocalVideoInfo(VideoInfo localViewInfo);

    /**
     * 设置远端视频信息
     * @param infos
     */
    void setRemoteVideoInfos(List<VideoInfo> infos);

    /**
     * 更新usb camera
     * @param isUvc
     */
    void updateCamera(boolean isUvc);

    /**
     * 开始渲染
     */
    void startRender();

    /**
     * 暂停渲染
     */
    void pauseRender();

    /**
     * 销毁资源
     */
    void destroy();

    /**
     * 视频单元格监听器
     */
    interface OnVideoCellListener {

        void onLongPress(MotionEvent e, VideoCell cell);

        boolean onDoubleTap(MotionEvent e, VideoCell cell);

        boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell);

        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell);

        void onShakeDone(VideoCell cell);

        void onCancelAddother(VideoCell cell);
    }

    /**
     * 简单实现，避免子类需要实现所有接口
     */
    public class SimpleVideoCellListener implements OnVideoCellListener {

        @Override
        public void onLongPress(MotionEvent e, VideoCell cell) {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e, VideoCell cell) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell) {
            return false;
        }

        @Override
        public void onShakeDone(VideoCell cell) {

        }

        @Override
        public void onCancelAddother(VideoCell cell) {

        }
    }
}
