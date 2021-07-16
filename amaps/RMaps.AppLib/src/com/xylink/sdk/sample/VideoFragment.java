package com.xylink.sdk.sample;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.log.L;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.ainemo.module.call.data.Enums;
import com.ainemo.module.call.data.FECCCommand;
import com.ainemo.module.call.data.NewStatisticsInfo;
import com.ainemo.module.call.data.SDKLayoutInfo;
import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.NemoSDKListener;
import com.ainemo.sdk.otf.RecordCallback;
import com.ainemo.sdk.otf.VideoInfo;
import com.ainemo.shared.MediaSourceID;
import com.ainemo.shared.UserActionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robert.maps.applib.R;
import com.xylink.sdk.sample.face.FaceContract;
import com.xylink.sdk.sample.face.FacePresenter;
import com.xylink.sdk.sample.face.FaceView;
import com.xylink.sdk.sample.utils.AlertUtil;
import com.xylink.sdk.sample.utils.CommonTime;
import com.xylink.sdk.sample.utils.GalleryLayoutBuilder;
import com.xylink.sdk.sample.utils.LayoutMode;
import com.xylink.sdk.sample.utils.SpeakerLayoutBuilder;
import com.xylink.sdk.sample.utils.VolumeManager;
import com.xylink.sdk.sample.view.CallRosterView;
import com.xylink.sdk.sample.view.CallStatisticsView;
import com.xylink.sdk.sample.view.Dtmf;
import com.xylink.sdk.sample.view.GalleryVideoView;
import com.xylink.sdk.sample.view.StatisticsRender;
import com.xylink.sdk.sample.view.VideoCell;
import com.xylink.sdk.sample.view.VideoCellLayout;
import com.xylink.sdk.sample.view.VideoGroupView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


/**
 * 通话界面
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class VideoFragment extends Fragment implements CallActivity.CallListener,
        View.OnClickListener, VideoGroupView.ForceLayoutListener, VideoGroupView.BGCellLayoutInfoListener,
        VolumeManager.MuteCallback, FaceContract.View {

    private AtomicBoolean audioMuteStatus = new AtomicBoolean(false);
    private static final String TAG = "VideoFragment";
    private final static int REFRESH_STATISTICS_INFO_DELAYED = 2000;
    private VideoGroupView mVideoView;
    private GalleryVideoView mGalleryView;
    private ImageView mContent;
    private boolean foregroundCamera = true;
    private int cameraId = 1;
    private boolean audioMode = false;
    private boolean videoMute = false;
    private boolean speakerMode = true;
    private LinearLayout toolbottom;
    private RelativeLayout outgoingContainer;
    private LinearLayout videoFunctionHideShow;
    private RelativeLayout videoContainer;
    private LinearLayout videoHideShowContainer;
    private LinearLayout switchCameraLayout;
    private ImageButton mButtonCancel;
    private WebView confControlWebview;
    private ImageButton btnVideo;
    private TextView btnVideoText;
    private VideoInfo layoutInfo;
    private LayoutMode layoutMode = LayoutMode.MODE_SPEAKER;

    private ImageButton mSwitchCamera;
    private LinearLayout mWebViewContainer;
    private ImageButton mContolEnter;
    private View mViewContainer;
    private TextView mSwitchSpeakerText;
    private ImageButton mSwitchSpeakerMode;
    private RelativeLayout mLayoutDropCall;

    private RelativeLayout mMicContainer;
    private ImageButton mMuteMicBtn;
    private TextView mMuteMicLabel;
    private RelativeLayout mHandupContainer;
    private ImageButton mHandupBtn;
    private TextView mHandupLabel;
    private boolean isHandupNow;    // 本地状态
    private boolean isMuteDisable; // 会控强制静音

    //录制
    private String callNumber;//用户信息
    private ImageButton mRecordVideoBtn;//录制按钮
    private TextView mRecordVideoTxt;//录制文字显示
    private TextView mTimer;//录制计时


    private boolean isControlEnable = true;
    private LinearLayout mTimeLayout;
    private ImageView mFlashView;
    private final static int TIMER_DELAYED = 1000;
    private final static int FLASH_ICON_DELAYED = 500;
    long recordingDuration = 0;
    private boolean isMicphoneMuted = true;


    //语音模式
    private ImageButton mAudioOnlyBtn;
    private TextView mAudioOnlyText;


    private ImageView mNetworkState;
    private TextView networkStateTimer;
    private TextView tvCallNumber;
    private String mDisplayName;

    private CallRosterView callRosterView;
    private CallStatisticsView callStatisticsView;
    private Button mSaveDump;
    private Button mRoster;
    private Button mStats;
    private int mMuteBtnLongPress;
    private boolean isStopCount = false;

    private Handler mHandler = new Handler();
    private long timer = 0;
    private String timeStr = "";
    private List<VideoInfo> layoutInfos = new ArrayList<>();
    private StatisticsRender mStatisticsRender;

    //更多
    private ImageButton mMeetingMore;
    private LinearLayout moreDialog;
    private TextView keyboard;
    private TextView switchLayout;
    private RelativeLayout dtmfLayout;
    private Dtmf dtmf;
    private LinearLayout dtmfLay;

    //会控
    private final static String JS_PARTICIPANTID_ROSTER = "xylink:forcelayout?pid=";
    Random random = new Random();
    private int r = random.nextInt(1000);
    final String url = "https://devcdn.xylink.com/custom-host/index.html?cloudNo=918201507520&" +
            "extId=40260e9046bae2da238ac0b0c572326b91726a83&host=https://dev.xylink.com";

    /**
     * 人脸识别业务处理类
     */
    private FaceContract.Presenter facePresenter;

    //FECC
    private ImageButton mFeccLeftBtn;
    private ImageButton mFeccRightBtn;
    private ImageButton mFeccUpBtn;
    private ImageButton mFeccDownBtn;
    private LinearLayout mFeccControl;
    private ImageView mFeccControlBg;
    private ImageView mFeccControlBgLeft;
    private ImageView mFeccControlBgRight;
    private ImageView mFeccControlBgUp;
    private ImageView mFeccControlBgDown;
    private ImageView mFeccPanView;
    private ImageView zoomInAdd;
    private ImageView zoomInPlus;
    private boolean feccHorizontalControl = false;
    private boolean feccVerticalControl = false;
    private int mParticipantId;
    private int mFeccOri;
    private boolean feccDisable;
    private int lastFeccCommand = UserActionListener.USER_ACTION_FECC_STOP;
    private UserActionListener actionListener;
    private VideoInfo videoInfo = null;
    private boolean mVisible = true;
    Handler handler = new Handler();

    private boolean isSupportHorizontalFECC(int capability) {
        return (capability & 1 << 1) != 0;
    }

    private boolean isSupportVerticalFECC(int capability) {
        return (capability & 1 << 2) != 0;
    }

    private boolean isSupportZoomInOut(int capability) {
        return (capability & 1 << 4) != 0;
    }


    //锁屏
    private LinearLayout lockPeople;
    private boolean mLocalFullScreen;
    private boolean isLock = false;
    private boolean isWhiteBoardLock = false;


    private VolumeManager mVolumeManager;
    private int currentVolume = -1;

    public void setActionListener(UserActionListener actionListener) {
        this.actionListener = actionListener;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.call_fragment_layout, container, false);

    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        countTimer();
        initialization(view);
        refreshMuteMicBtn();
        super.onViewCreated(view, savedInstanceState);
        //NemoSDK.getInstance().setAudioMute(false);

    }


    //初始化控件
    private void initialization(View view) {

        mLayoutDropCall = (RelativeLayout) view.findViewById(R.id.ll_drop_call);
        //录制
        mTimer = (TextView) view.findViewById(R.id.video_recording_timer);
        mFlashView = (ImageView) view.findViewById(R.id.video_recording_icon);
        mRecordVideoBtn = (ImageButton) view.findViewById(R.id.start_record_video);
        mRecordVideoTxt = (TextView) view.findViewById(R.id.record_video_text);
        mTimeLayout = (LinearLayout) view.findViewById(R.id.conversation_recording_layout);
        mAudioOnlyBtn = (ImageButton) view.findViewById(R.id.audio_only_btn);
        mAudioOnlyText = (TextView) view.findViewById(R.id.audio_only_text);
        btnVideo = (ImageButton) view.findViewById(R.id.close_video);
        btnVideoText = (TextView) view.findViewById(R.id.video_mute_text);
        mSwitchSpeakerText = (TextView) view.findViewById(R.id.switch_speaker_text);
        mSwitchSpeakerMode = (ImageButton) view.findViewById(R.id.switch_speaker_mode);
        mViewContainer = view.findViewById(R.id.view_container);
        mWebViewContainer = (LinearLayout) view.findViewById(R.id.conf_webview_container);
        mContolEnter = (ImageButton) view.findViewById(R.id.contol_conf);
        confControlWebview = (WebView) view.findViewById(R.id.conf_control_webview);
        mMicContainer = (RelativeLayout) view.findViewById(R.id.mic_mute_container);
        mMuteMicLabel = (TextView) view.findViewById(R.id.mute_mic_btn_label);
        mHandupContainer = (RelativeLayout) view.findViewById(R.id.handup_view);
        mHandupBtn = (ImageButton) view.findViewById(R.id.handup_btn);
        mHandupLabel = (TextView) view.findViewById(R.id.handup_label);
        mMuteMicBtn = (ImageButton) view.findViewById(R.id.mute_mic_btn);

        mButtonCancel = (ImageButton) (view.findViewById(R.id.conn_mt_cancelcall_btn));
        mSwitchCamera = (ImageButton) view.findViewById(R.id.switch_camera);
        outgoingContainer = (RelativeLayout) view.findViewById(R.id.outgoing_container);
        videoContainer = (RelativeLayout) view.findViewById(R.id.video_container);
        mNetworkState = (ImageView) view.findViewById(R.id.network_state);
        mVideoView = (VideoGroupView) view.findViewById(R.id.remote_video_view);
        mGalleryView = (GalleryVideoView) view.findViewById(R.id.gallery_video_view);
        mContent = (ImageView) view.findViewById(R.id.shared_content);
        toolbottom = (LinearLayout) view.findViewById(R.id.ll_bottom);
        switchCameraLayout = (LinearLayout) view.findViewById(R.id.switch_camera_layout);
        networkStateTimer = (TextView) view.findViewById(R.id.network_state_timer);
        tvCallNumber = (TextView) view.findViewById(R.id.tv_call_number);
        videoHideShowContainer = (LinearLayout) view.findViewById(R.id.ll_top_hide_show);
        videoFunctionHideShow = (LinearLayout) view.findViewById(R.id.ll_video_function);
        tvCallNumber.setText(callNumber);

        //
        mSwitchCamera.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
        mMuteMicBtn.setOnClickListener(this);
        mHandupBtn.setOnClickListener(this);
        //会控
        WebSettings settings = confControlWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        mContolEnter.setOnClickListener(this);
        mViewContainer.setOnClickListener(this);
        mSwitchSpeakerMode.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        mLayoutDropCall.setOnClickListener(this);
        mVideoView.setFrameCellLongClickListener(videoFrameLongClickListener);
        //语音模式初始化
        mAudioOnlyBtn.setOnClickListener(this);
        mRecordVideoBtn.setOnClickListener(this);
        //用户看的统计
        mNetworkState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRefreshStatisticsInfo();
            }
        });
        mVideoView.init();

        callRosterView = (CallRosterView) view.findViewById(R.id.conversation_roster);
        callStatisticsView = (CallStatisticsView) view.findViewById(R.id.conversation_statics);
        mSaveDump = (Button) view.findViewById(R.id.save_dump);
        mRoster = (Button) view.findViewById(R.id.roster_btn);
        mStats = (Button) view.findViewById(R.id.stats_btn);
        callStatisticsView.setOnCloseListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callStatisticsView.setVisibility(GONE);
                stopRefreshMediaStatistics();
                callStatisticsView.clearData();
                Log.i(TAG, "callStatisticsView1");

            }
        });
        mSaveDump.setOnClickListener(this);
        mRoster.setOnClickListener(this);
        mStats.setOnClickListener(this);

        ViewStub stub = (ViewStub) view.findViewById(R.id.view_statistics_info);
        mStatisticsRender = new StatisticsRender(stub, new StatisticsRender.StatisticsOperationListener() {
            @Override
            public void stopStatisticsInfo() {
                stopRefreshStatisticsInfo();
            }
        });
        mVideoView.setLocalFullScreen(false, false);
        mVideoView.setOnHoldMode(false);
        TimeHide();
        //FECC
        mFeccLeftBtn = (ImageButton) view.findViewById(R.id.fecc_left);
        mFeccRightBtn = (ImageButton) view.findViewById(R.id.fecc_right);
        mFeccUpBtn = (ImageButton) view.findViewById(R.id.fecc_up);
        mFeccDownBtn = (ImageButton) view.findViewById(R.id.fecc_down);//圆盘指令
        mFeccControl = (LinearLayout) view.findViewById(R.id.fecc_control);
        mFeccControlBg = (ImageView) view.findViewById(R.id.fecc_control_bg);
        mFeccControlBgLeft = (ImageView) view.findViewById(R.id.fecc_control_bg_left);
        mFeccControlBgRight = (ImageView) view.findViewById(R.id.fecc_control_bg_right);
        mFeccControlBgUp = (ImageView) view.findViewById(R.id.fecc_control_bg_up);
        mFeccControlBgDown = (ImageView) view.findViewById(R.id.fecc_control_bg_down);
        mFeccPanView = (ImageView) view.findViewById(R.id.fecc_pan);
        zoomInAdd = (ImageView) view.findViewById(R.id.zoom_in_add);
        zoomInPlus = (ImageView) view.findViewById(R.id.zoom_out_plus);
        mVideoView.setBGCellLayoutInfoListener(this);
        mVideoView.setForceLayoutListener(this);
        initFeccEventListeners();
        mVideoView.setLocalLayoutInfo(buildLocalLayoutInfo());
        mGalleryView.setLocalVideoInfo(buildLocalLayoutInfo());
        NemoSDK.getInstance().releaseLayout();

        //FECC listeners
        FECCListeners();
        moreDialog = (LinearLayout) view.findViewById(R.id.more_layout_dialog);
        mMeetingMore = (ImageButton) view.findViewById(R.id.hold_meeting_more);
        mMeetingMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (moreDialog.getVisibility() == INVISIBLE) {
                    moreDialog.setVisibility(VISIBLE);
                    // shareDialog.setVisibility(GONE);
                } else hideMoreDialog(moreDialog);
            }
        });

        keyboard = (TextView) view.findViewById(R.id.keyboard);
        keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                L.i(TAG, "show keyboard true");
                TimeHide();
                showDtmfLayout();
                hideMoreDialog(moreDialog);
            }
        });
        switchLayout = (TextView) view.findViewById(R.id.switch_layout);
        switchLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(layoutMode == LayoutMode.MODE_SPEAKER) {
                    layoutMode = LayoutMode.MODE_GALLERY;
                } else {
                    layoutMode = LayoutMode.MODE_SPEAKER;
                }

                switchLayout();

                hideMoreDialog(moreDialog);
            }
        });
        dtmfLayout = (RelativeLayout) view.findViewById(R.id.dtmf_layout);
        dtmfLay = (LinearLayout) view.findViewById(R.id.dtmf);
        dtmf = new Dtmf(dtmfLay, new Dtmf.DtmfListener() {
            @Override
            public void onDtmfKey(String key) {
                L.i(TAG, "onDtmfKey key::" + key);
                if (layoutInfos != null) {
                    if (layoutInfos.size() > 0) {
                        L.i("sendDtmf 1：" + layoutInfos.get(0).getRemoteID() + ":key:" + key);
                        NemoSDK.getInstance().sendDtmf(layoutInfos.get(0).getRemoteID(), key);
                    }
                }

            }

        });


        lockPeople = (LinearLayout) view.findViewById(R.id.layout_lock_people);
        if (lockPeople != null) {

            lockPeople.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    L.i("setForceLayout onClick:" + mLocalFullScreen);
                    if (mLocalFullScreen) {
                        mVideoView.switchLocalViewToSmallCell();
                    } else {
                        NemoSDK.getInstance().forceLayout(0);
                    }
                    lockPeople.setVisibility(View.GONE);
                    isLock = false;
                    L.i("callActivity localFullScreen:" + mLocalFullScreen);
                }
            });
        }


        if (mMuteMicBtn != null) {
            mMuteMicBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mMuteBtnLongPress++;
                    if (mMuteBtnLongPress >= 3) {
                        displayDebugButton();
                        mMuteBtnLongPress = 0;
                    }
                    return true;
                }
            });

        }
        mVolumeManager = new VolumeManager(getActivity(), view.findViewById(R.id.operation_volume_brightness), AudioManager.STREAM_VOICE_CALL);
        mVolumeManager.setMuteCallback(this);
        currentVolume = mVolumeManager.getVolume();

        initFaceData();

        switchLayout();
    }

    private void initFaceData() {
        facePresenter = new FacePresenter(getActivity().getApplicationContext(), this);
    }


    public void hideMoreDialog(LinearLayout moreDialog) {
        if (moreDialog != null) {
            if (moreDialog.getVisibility() == VISIBLE) {
                moreDialog.setVisibility(INVISIBLE);
            }
        }
    }

    private void showDtmfLayout() {
        dtmfLayout.setVisibility(View.VISIBLE);
        //toolbar.setVisibility(View.INVISIBLE);
        if (dtmf != null) {
            dtmf.show();
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            recordingDuration += 1000;
            mTimer.setText(getResources().getString(R.string.recording_text) + " " + CommonTime.formatDuration(recordingDuration));
            mTimer.postDelayed(timerRunnable, TIMER_DELAYED);


        }
    };
    private Runnable flashingViewRunnable = new Runnable() {
        @Override
        public void run() {
            mFlashView.setVisibility(mFlashView.getVisibility() == VISIBLE ? INVISIBLE : VISIBLE);
            mFlashView.postDelayed(flashingViewRunnable, FLASH_ICON_DELAYED);
        }
    };

    private Runnable TimerRunnable = new Runnable() {

        @Override
        public void run() {
            if (!isStopCount) {
                timer += 1000;
                timeStr = CommonTime.formatDuration(timer);
                networkStateTimer.setText(timeStr);
            }
            countTimer();
        }
    };

    private Runnable refreshMSRunnable = new Runnable() {
        @Override
        public void run() {
            startRefreshMediaStatistics();
        }
    };
    private Runnable refreshStatisticsInfoRunnable = new Runnable() {
        @Override
        public void run() {
            startRefreshStatisticsInfo();
        }
    };

    private void countTimer() {
        mHandler.postDelayed(TimerRunnable, 1000);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //TimeHide();
            videoContainer.setVisibility(INVISIBLE);
            toolbottom.setVisibility(INVISIBLE);
            switchCameraLayout.setVisibility(INVISIBLE);
        }
    };

    private void StartToolbarVisibleTimer() {
        handler.removeCallbacks(runnable);
       // handler.postDelayed(runnable, 5000);
    }

    private void StopToolbarVisibleTimer() {
        handler.removeCallbacks(menuRunnable);
    }

    private Runnable menuRunnable = new Runnable() {
        @Override
        public void run() {
            videoContainer.setVisibility(INVISIBLE);
            toolbottom.setVisibility(INVISIBLE);
            switchCameraLayout.setVisibility(INVISIBLE);
        }
    };


    private void TimeHide() {
        if (videoContainer.getVisibility() == VISIBLE
                && toolbottom.getVisibility() == VISIBLE
                && switchCameraLayout.getVisibility() == VISIBLE) {
            StartToolbarVisibleTimer();
            setFECCButtonVisible(false);


        } else {
            videoContainer.setVisibility(VISIBLE);
            toolbottom.setVisibility(VISIBLE);
            switchCameraLayout.setVisibility(VISIBLE);
            StopToolbarVisibleTimer();
            setFECCButtonVisible(videoInfo != null
                    && !feccDisable
                    && audioMode != videoInfo.isAudioMute()
                    && !videoInfo.isVideoMute()
                    && (isSupportHorizontalFECC(videoInfo.getFeccOri()) || isSupportVerticalFECC(videoInfo.getFeccOri()))); // 左右或上下至少支持一种才行，否则不显示。
        }


    }

    private VideoCellLayout.SimpleVideoCellListener galleryVideoCellListener = new VideoCellLayout.SimpleVideoCellListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e, VideoCell cell) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            L.i(TAG, "onSingleTapConfirmed, cell.layoutInfo : " + cell.getLayoutInfo());
            if (videoContainer.getVisibility() == VISIBLE
                    && toolbottom.getVisibility() == VISIBLE
                    && switchCameraLayout.getVisibility() == VISIBLE) {
                videoContainer.setVisibility(INVISIBLE);
                toolbottom.setVisibility(INVISIBLE);
                switchCameraLayout.setVisibility(INVISIBLE);
                mVisible = false;
            } else {
                mVisible = true;

                videoContainer.setVisibility(VISIBLE);
                toolbottom.setVisibility(VISIBLE);
                switchCameraLayout.setVisibility(VISIBLE);
            }

            return true;
        }
    };


    private View.OnClickListener videoFrameCellClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "videoFrameCellClickListener==::" + mVisible);
            if (videoContainer.getVisibility() == VISIBLE
                    && toolbottom.getVisibility() == VISIBLE
                    && switchCameraLayout.getVisibility() == VISIBLE) {
                videoContainer.setVisibility(INVISIBLE);
                toolbottom.setVisibility(INVISIBLE);
                switchCameraLayout.setVisibility(INVISIBLE);
                mVisible = false;
                setFECCButtonVisible(false);
            } else {
                mVisible = true;
                TimeHide();
               // handler.postDelayed(menuRunnable, 5000);
            }
            if (moreDialog.getVisibility() == VISIBLE) {
                moreDialog.setVisibility(INVISIBLE);

            } else {
                moreDialog.setVisibility(INVISIBLE);
                if (dtmf != null) {
                    dtmf.hide();
                }
            }

            if (isLock) {
                lockPeople.setVisibility(VISIBLE);
                if (!isWhiteBoardLock) {
                    lockPeople.setVisibility(mVisible ? View.GONE : View.VISIBLE);
                }
            } else {
                lockPeople.setVisibility(GONE);
            }
        }
    };

    private View.OnLongClickListener videoFrameLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            mVideoView.moveThumbCellsToOriginal(); // 重置小窗口位置
            return false;
        }
    };

    public void setCallNumber(String callNumber) {
        Log.i(TAG, "onViewCreated setDisplayName=" + callNumber);
        this.callNumber = callNumber;

    }

    public void setDisplayName(String displayName) {

        Log.i(TAG, "setDisplayName=" + displayName);
        this.mDisplayName = displayName;

    }


    //录制
    public void setRecordVideo() {
        if (isControlEnable) {
            isControlEnable = false;
            mRecordVideoTxt.setText(R.string.button_text_stop);
            mTimeLayout.setVisibility(View.VISIBLE);
            mFlashView.postDelayed(flashingViewRunnable, FLASH_ICON_DELAYED);
            mRecordVideoBtn.setImageResource(R.mipmap.ic_toolbar_recording_ing);
            mTimer.setText(R.string.recording_text_preparing);
            NemoSDK.getInstance().startRecord(callNumber, new RecordCallback() {
                @Override
                public void onFailed(final int errorCode) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                }

                @Override
                public void onSuccess() {

                }
            });
            recordingDuration = 0;
            mTimer.postDelayed(timerRunnable, TIMER_DELAYED);
        } else {
            mRecordVideoTxt.setText(R.string.button_text_record);
            mRecordVideoBtn.setImageResource(R.drawable.ic_toolbar_recording);
            NemoSDK.getInstance().stopRecord();
            isControlEnable = !isControlEnable;
            mTimer.removeCallbacks(timerRunnable);
            mFlashView.removeCallbacks(flashingViewRunnable);
            String content = getString(R.string.third_conf_record_notice);
            AlertUtil.toastText(content);
            mTimeLayout.setVisibility(GONE);

        }
    }


    private void displayDebugButton() {
        mStats.setVisibility(VISIBLE);
//        mRoster.setVisibility(VISIBLE);
//        mSaveDump.setVisibility(VISIBLE);
    }

    //语音模式
    private void setSwitchCallState(boolean audioMode) {
        Log.i(TAG, "CellStateViewCSL setSwitchCallState=" + audioMode);
        mVideoView.setAudioOnlyMode(audioMode);
        mGalleryView.setAudioOnlyMode(audioMode);
        if (this.audioMode) {
            btnVideo.setEnabled(false);
            mAudioOnlyBtn.setImageResource(R.mipmap.ic_toolbar_audio_only_pressed);
            mAudioOnlyText.setText(R.string.close_switch_call_module);

        } else {
            btnVideo.setEnabled(true);

            mAudioOnlyText.setText(R.string.switch_call_module);
            mAudioOnlyBtn.setImageResource(R.mipmap.ic_toolbar_audio_only);

        }


    }

    //视频关闭或者开启
    private void setVideoState(boolean videoMute) {
        mVideoView.setMuteLocalVideo(videoMute, getActivity().getString(R.string.call_video_mute));
        mGalleryView.setMuteLocalVideo(videoMute, getActivity().getString(R.string.call_video_mute));
        if (this.videoMute) {
            mAudioOnlyBtn.setEnabled(false);
            btnVideo.setImageResource(R.mipmap.ic_toolbar_camera);
            btnVideoText.setText(getResources().getString(R.string.open_video));
        } else {
            mAudioOnlyBtn.setEnabled(true);
            btnVideo.setImageResource(R.mipmap.ic_toolbar_camera_muted);
            btnVideoText.setText(getResources().getString(R.string.close_video));
        }
    }

    /**
     * 是否静音
     *
     * @param isMicphoneMuted
     */
    public void setMicphoneMuted(boolean isMicphoneMuted) {
        if (mVideoView != null) {
            Log.i(TAG, "setMicphoneMuted1==::" + isMicphoneMuted);
            audioMuteStatus.set(isMicphoneMuted);
            mVideoView.setMuteLocalAudio(isMicphoneMuted);
            mGalleryView.setMuteLocalAudio(isMicphoneMuted);
            if (this.isMicphoneMuted) {
                Log.i(TAG, "setMicphoneMuted2==::" + this.isMicphoneMuted);
                mMuteMicBtn.setImageResource(R.mipmap.ic_toolbar_mic_muted);
                mMuteMicLabel.setText(R.string.mute_mic);
            } else {
                Log.i(TAG, "setMicphoneMuted3==::" + this.isMicphoneMuted);
                mMuteMicBtn.setImageResource(R.mipmap.ic_toolbar_mic);
                mMuteMicLabel.setText(R.string.mute_mic);
            }

        }

    }

    private void switchLayout() {
        L.i(TAG, "switchLayout, layoutMode : " + layoutMode);
        if(layoutMode == LayoutMode.MODE_SPEAKER) {
            mVideoView.setVisibility(VISIBLE);
            mGalleryView.setVisibility(GONE);

            lockPeople.setVisibility(isLock ? VISIBLE : GONE);

            NemoSDK.getInstance().setLayoutBuilder(new SpeakerLayoutBuilder());
        } else {
            setFECCButtonVisible(false);
            lockPeople.setVisibility(GONE);

            mVideoView.setVisibility(GONE);
            mGalleryView.setVisibility(VISIBLE);
            NemoSDK.getInstance().setLayoutBuilder(new GalleryLayoutBuilder());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean audioStatus = audioMuteStatus.get();
        Log.i(TAG, "onResume: audioStatus:" + audioStatus);
        setMicphoneMuted(audioStatus);
        NemoSDK.getInstance().enableMic(audioStatus, true);
        if (mVideoView != null) {
            mVideoView.setFrameCellClickListener(videoFrameCellClickListener);
        }
        if(mGalleryView != null) {
            mGalleryView.setOnVideoCellListener(galleryVideoCellListener);
        }
        if (currentVolume >= 0) {
            Log.i(TAG, "print onResume-->currentVolume=" + currentVolume);
            mVolumeManager.setVolume(0);
        }

    }


    @Override
    public void onContentStateChanged(NemoSDKListener.ContentState contentState) {
        if (NemoSDKListener.ContentState.ON_START == contentState) {
            mContent.setVisibility(VISIBLE);
        } else {
            mContent.setVisibility(GONE);
        }
    }

    @Override
    public void onStart() {
        mVideoView.onResume();
        mGalleryView.startRender();
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        mVideoView.onPause();
        mGalleryView.pauseRender();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (null == mVideoView) {
            return;
        }
        if (!hidden) {
            NemoSDK.getInstance().enableMic(false, true);
            mRecordVideoBtn.setEnabled(true);
            tvCallNumber.setText(callNumber);
        } else {
        }
    }

    public void refreshMuteMicBtn() {

        if (isMuteDisable) {
            mMicContainer.setVisibility(GONE);
            mHandupContainer.setVisibility(VISIBLE);

            isMicphoneMuted = NemoSDK.getInstance().isMicMuted();
            if (isMicphoneMuted) {
                if (isHandupNow) {
                    // 取消举手
                    mHandupBtn.setImageResource(R.mipmap.ic_toolbar_handdown);
                    mHandupLabel.setText(R.string.hand_down);
                } else {
                    // 举手发言
                    mHandupBtn.setImageResource(R.mipmap.ic_toolbar_hand_up);
                    mHandupLabel.setText(R.string.hand_up);
                }
            } else {
                // 结束发言
                mHandupBtn.setImageResource(R.mipmap.ic_toolbar_end_speech);
                mHandupLabel.setText(R.string.end_speech);
            }
        } else {
            mMicContainer.setVisibility(VISIBLE);
            mHandupContainer.setVisibility(GONE);

            isMicphoneMuted = !isMicphoneMuted;
            Log.i(TAG, "setMicphoneMuted==::" + isMicphoneMuted);
            setMicphoneMuted(isMicphoneMuted);


        }
    }


    /**
     * 本地网络质量提示
     *
     * @param level 1、2、3、4个等级,差-中-良-优
     */
    public void onNetworkIndicatorLevel(final int level) {
        L.i(TAG, "onNetworkIndicatorLevel" + level);
        if (mNetworkState != null) {
            switch (level) {
                case 4:
                    L.i(TAG, "onNetworkIndicatorLevel=" + level);
                    mNetworkState.setImageResource(R.drawable.network_state_four);
                    break;
                case 3:
                    L.i(TAG, "onNetworkIndicatorLevel=" + level);
                    mNetworkState.setImageResource(R.drawable.network_state_three);
                    break;
                case 2:
                    L.i(TAG, "onNetworkIndicatorLevel=" + level);
                    mNetworkState.setImageResource(R.drawable.network_state_two);
                    break;
                case 1:
                    L.i(TAG, "onNetworkIndicatorLevel=" + level);
                    mNetworkState.setImageResource(R.drawable.network_state_one);
                    break;
            }
        }
    }

    /**
     * 处理远程信息
     *
     * @param callIndex
     * @param isStart
     * @param displayName
     */
    public void onRecordStatusNotification(final int callIndex, final boolean isStart, String displayName) {

        //mainCallIndex = callIndex;
        Log.i(TAG, " csl onRecordStatusNotification" + isStart);
        if (isStart) {
            mRecordVideoBtn.setEnabled(false);
            mTimer.setText(displayName + "正在录制");
            mTimeLayout.setVisibility(View.VISIBLE);
            mFlashView.postDelayed(flashingViewRunnable, FLASH_ICON_DELAYED);
            mRecordVideoBtn.setImageResource(R.mipmap.ic_toolbar_recording_ing);
            mRecordVideoTxt.setText(R.string.button_text_stop);
        } else {
            mRecordVideoBtn.setEnabled(true);
            mFlashView.removeCallbacks(flashingViewRunnable);
            mTimeLayout.setVisibility(GONE);
            mRecordVideoTxt.setText(R.string.button_text_record);
            mRecordVideoBtn.setImageResource(R.drawable.ic_toolbar_recording);
        }


    }

    /**
     * 处理会控消息
     * 控制操作：静音、非静音
     * 控制状态：举手发言、取消举手、结束发言
     *
     * @param callIndex
     * @param opearation    操作：mute/unmute
     * @param isMuteDisable 是否为强制静音
     */
    public void onConfMgmtStateChanged(int callIndex, final String opearation, boolean isMuteDisable) {

        this.isMuteDisable = isMuteDisable;

        L.i(TAG, "onConfMgmtStateChanged called, callIndex=" + callIndex + ", opearation=" + opearation + ", isMuteDisable=" + isMuteDisable);

        if (opearation.equalsIgnoreCase("mute")) {
            NemoSDK.getInstance().enableMic(true, false);
            if (mVideoView != null) {
                mVideoView.setMuteLocalAudio(true);
            }
            if(mGalleryView != null) {
                mGalleryView.setMuteLocalAudio(true);
            }
        } else if (opearation.equalsIgnoreCase("unmute")) {
            NemoSDK.getInstance().enableMic(false, false);
            if (mVideoView != null) {
                mVideoView.setMuteLocalAudio(false);
            }
            if(mGalleryView != null) {
                mGalleryView.setMuteLocalAudio(false);
            }
        }

        isHandupNow = false;

        L.i(TAG, "isMicMutedNow=" + NemoSDK.getInstance().isMicMuted());

        refreshMuteMicBtn();
    }

    public void showOutgoingContainer() {
        if (outgoingContainer != null && videoHideShowContainer != null && videoFunctionHideShow != null) {
            outgoingContainer.setVisibility(VISIBLE);
            videoHideShowContainer.setVisibility(GONE);
            videoFunctionHideShow.setVisibility(GONE);
        }

    }

    public void showVideContainer() {
        if (outgoingContainer != null && videoHideShowContainer != null && videoFunctionHideShow != null) {
            outgoingContainer.setVisibility(GONE);
            videoHideShowContainer.setVisibility(VISIBLE);
            videoFunctionHideShow.setVisibility(VISIBLE);
        }

    }

    @Override
    public void onVideoDataSourceChange(List<VideoInfo> videoInfos) {
        if (mVideoView != null) {
            mVideoView.setLayoutInfo(videoInfos, false, false);
        }
        if(mGalleryView != null) {
            mGalleryView.setRemoteVideoInfos(videoInfos);
        }
        this.layoutInfos = videoInfos;
        for (int i = 0; i < videoInfos.size(); i++) {
            mParticipantId = videoInfos.get(i).getParticipantId();
            mFeccOri = videoInfos.get(i).getFeccOri();

        }
    }

    private void stopRefreshStatisticsInfo() {
        handler.removeCallbacks(refreshStatisticsInfoRunnable);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    //视频初始状态
    public void releaseResource() {
        confControlWebview = null;
//        NemoSDK.getInstance().enableMic(true, false);
//        if (mVideoView != null) {
//            mVideoView.setMuteLocalAudio(true);
//        }

        mVideoView.destroy();
        mGalleryView.destroy();
        if (videoMute) {
            videoMute = false;
            setVideoState(videoMute);
        }

        // NemoSDK.getInstance().setAudioMute(false);
    }

    //语音初始状态
    public void releaseSwitchResource() {
        //  mVideoView.stopRender();
        mVideoView.destroy();
        mGalleryView.destroy();
        if (audioMode) {
            audioMode = false;
            setSwitchCallState(audioMode);
        }
    }

    //录制初始状态
    public void RecordVideoResource() {
        isControlEnable = false;
        mTimer.removeCallbacks(timerRunnable);
        mFlashView.removeCallbacks(flashingViewRunnable);
        isControlEnable = !isControlEnable;
        mTimeLayout.setVisibility(GONE);
        mRecordVideoTxt.setText(R.string.button_text_record);
        mRecordVideoBtn.setImageResource(R.drawable.ic_toolbar_recording);
        NemoSDK.getInstance().stopRecord();
    }

    public void MicPhoneResource() {
        mVideoView.destroy();
        mGalleryView.destroy();
        Log.i(TAG, "isMicphoneMuted==4" + isMicphoneMuted);
        if (isMicphoneMuted) {
            isMicphoneMuted = false;
            setMicphoneMuted(isMicphoneMuted);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(TimerRunnable);
        StopToolbarVisibleTimer();

    }

    //视频、音频等统计
    private void startRefreshMediaStatistics() {
        Map<String, Object> map = NemoSDK.getInstance().getStatistics();
        Log.i(TAG, "====JSOn1" + map);
        callStatisticsView.updateStatistics(map);
        if (null == map) {
            return;
        }
        handler.removeCallbacks(refreshMSRunnable);
        handler.postDelayed(refreshMSRunnable, REFRESH_STATISTICS_INFO_DELAYED);
    }

    private void startRefreshStatisticsInfo() {
        NewStatisticsInfo newInfo = NemoSDK.getInstance().getStatisticsInfo();
        if (null == newInfo) {
            return;
        }
        mStatisticsRender.show();
        mStatisticsRender.onValue(newInfo);

        handler.removeCallbacks(refreshStatisticsInfoRunnable);
        handler.postDelayed(refreshStatisticsInfoRunnable, REFRESH_STATISTICS_INFO_DELAYED);
    }


    private void stopRefreshMediaStatistics() {

        handler.removeCallbacks(refreshMSRunnable);
    }


    //（String转成map）
    public static Map<String, Object> parseJsonToMap(String jsonStr) {

        Map<String, Object> map = new HashMap<String, Object>();
        Type type = TypeToken.get(map.getClass()).getType();
        map = new Gson().fromJson(jsonStr, type);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Log.i(TAG, "parseJsonToMapKey" + entry.getKey());
            Log.i(TAG, "parseJsonToMapValue" + entry.getValue());

        }
        return map;
    }

    //Fecc
    private void handleFECCControl(FECCCommand command) {
        if (videoInfo != null) {
            NemoSDK.getInstance().farEndHardwareControl(videoInfo.getParticipantId(), command, 10);
            Log.i(TAG, "user Fragment farEndHardwareControl22==" + videoInfo.getParticipantId() + "==command==" + command);
        }
    }

    //data
    private VideoInfo buildLocalLayoutInfo() {
        VideoInfo li = null;
        if (li == null) {
            li = new VideoInfo();
            li.setLayoutVideoState(Enums.LAYOUT_STATE_RECEIVED);
            li.setDataSourceID(MediaSourceID.SOURCE_ID_LOCAL_PREVIEW);
            li.setRemoteName(mDisplayName);
            Log.i(TAG, "getLayoutVideoState buildLocalLayoutInfo: " + li.toString());
            layoutInfo = li;

        }
        return li;
    }

    @Override
    public void onNewContentReceive(Bitmap bitmap) {
        mContent.setImageBitmap(bitmap);
    }


    //onClick事件
    @Override
    public void onClick(View view) {
        int i = view.getId();//语音模式
        if (i == R.id.switch_camera) {
            foregroundCamera = !foregroundCamera;
            cameraId = foregroundCamera ? 1 : 0;
            NemoSDK.getInstance().switchCamera(cameraId);  // 0：后置 1：前置
        } else if (i == R.id.conn_mt_cancelcall_btn) {
            NemoSDK.getInstance().hangup();
        } else if (i == R.id.mute_mic_btn) {
            mMuteBtnLongPress = 0;
            NemoSDK.getInstance().enableMic(!NemoSDK.getInstance().isMicMuted(), true);
            refreshMuteMicBtn();
        } else if (i == R.id.handup_btn) {
            if (NemoSDK.getInstance().isMicMuted()) {
                if (isHandupNow) {
                    // 取消举手
                    NemoSDK.getInstance().handDown();
                    isHandupNow = false;
                } else {
                    // 举手发言
                    NemoSDK.getInstance().handUp();
                    isHandupNow = true;
                }
            } else {
                // 结束发言
                NemoSDK.getInstance().endSpeech();
            }

            refreshMuteMicBtn();
        } else if (i == R.id.contol_conf) {
            if (mWebViewContainer.getVisibility() == GONE) {
                mWebViewContainer.setVisibility(VISIBLE);

                if (!TextUtils.isEmpty(url) && confControlWebview != null) {
                    confControlWebview.loadUrl(url);
                    confControlWebview.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            if (url != null) {
                                if (url.contains(JS_PARTICIPANTID_ROSTER)) {

                                    String participantId = url.substring(url.indexOf(JS_PARTICIPANTID_ROSTER)
                                            + JS_PARTICIPANTID_ROSTER.length(), url.length());
                                    Log.i("participantId", "拦截的ID===2==" + participantId);

                                    //传给下层
                                    NemoSDK.getInstance().forceLayout(Integer.parseInt(participantId));
                                } else {
                                    view.loadUrl(url);
                                }

                            }

                            return true;

                        }
                    });
                }

            } else {
                mWebViewContainer.setVisibility(GONE);
            }
        } else if (i == R.id.view_container) {
            mWebViewContainer.setVisibility(GONE);
        } else if (i == R.id.switch_speaker_mode) {
            speakerMode = !speakerMode;
            if (speakerMode) {
                mSwitchSpeakerText.setText(getResources().getString(R.string.close_switch_speaker_mode));
            } else {
                mSwitchSpeakerText.setText(getResources().getString(R.string.switch_speaker_mode));
            }
            NemoSDK.getInstance().switchSpeakerOnModle(speakerMode);
        } else if (i == R.id.close_video) {
            videoMute = !videoMute;
            setVideoState(videoMute);
            Log.i(TAG, "videoMute==1" + videoMute);
            NemoSDK.getInstance().setVideoMute(videoMute);
            mTimeLayout.setVisibility(GONE);
        } else if (i == R.id.ll_drop_call) {
            NemoSDK.getInstance().hangup();
            mStats.setVisibility(GONE);
        } else if (i == R.id.remote_video_view) {
        } else if (i == R.id.audio_only_btn) {
            audioMode = !audioMode;
            Log.i(TAG, "audio_only_btn==" + audioMode);
            setSwitchCallState(audioMode);
            NemoSDK.getInstance().switchCallMode(audioMode);
        } else if (i == R.id.start_record_video) {
            if (NemoSDK.getInstance().isAuthorize()) {
                setRecordVideo();
            } else {
                Toast.makeText(getActivity(), "端终号不可录制", Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.conversation_roster) {
        } else if (i == R.id.save_dump) {
            mVolumeManager.onVolumeDown();
        } else if (i == R.id.roster_btn) {
            mVolumeManager.onVolumeUp();
        } else if (i == R.id.stats_btn) {
            startRefreshMediaStatistics();
            callStatisticsView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onChanged(VideoInfo layoutInfo) {
        videoInfo = layoutInfo;
        if (videoInfo != null) {
            L.i(TAG, "main cell " + layoutInfo.getRemoteName() + ":layoutInfo:" + layoutInfo.toString());
        }

        setFECCButtonVisible(videoInfo != null
                && !feccDisable
                && audioMode != videoInfo.isAudioMute()
                && !videoInfo.isVideoMute()
                && (isSupportHorizontalFECC(videoInfo.getFeccOri()) || isSupportVerticalFECC(videoInfo.getFeccOri()))); // 左右或上下至少支持一种才行，否则不显示。
        setZoomInOutVisible(videoInfo != null && isSupportZoomInOut(videoInfo.getFeccOri()));
        setFeccTiltControl(videoInfo != null
                && isSupportHorizontalFECC(videoInfo.getFeccOri()), layoutInfos != null && isSupportVerticalFECC(videoInfo.getFeccOri()));
    }


    //FECC
    private float GetFeccBtnPositon(ImageButton feccButton) {
        float animator = 0f;

        if (feccButton == mFeccRightBtn) {
            animator = mFeccRightBtn.getRight() - mFeccPanView.getWidth() + 40;
        } else if (feccButton == mFeccLeftBtn) {
            animator = mFeccLeftBtn.getX();
        } else if (feccButton == mFeccUpBtn) {
            animator = mFeccUpBtn.getY();
        } else if (feccButton == mFeccDownBtn) {
            animator = mFeccDownBtn.getBottom() - mFeccPanView.getHeight() + 30;
        }
        return animator;
    }

    private void FeccPanTurnSide(final ImageButton feccButton) {
        float animator = GetFeccBtnPositon(feccButton);
        ObjectAnimator fadeIn = null;

        if (feccButton == mFeccLeftBtn) {
            mFeccControlBgLeft.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", animator);
        } else if (feccButton == mFeccRightBtn) {
            mFeccControlBgRight.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", animator);
        } else if (feccButton == mFeccUpBtn) {
            mFeccControlBgUp.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", animator);
        } else if (feccButton == mFeccDownBtn) {
            mFeccControlBgDown.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", animator);
        }

        fadeIn.setDuration(100);
        fadeIn.start();
        mFeccControlBg.setVisibility(View.VISIBLE);
    }

    private void FeccPanTurnPingPong(final ImageButton feccButton) {
        float animator = GetFeccBtnPositon(feccButton);
        ObjectAnimator fadeIn = null;
        if (feccButton == mFeccLeftBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", mFeccPanView.getLeft(), animator, mFeccPanView.getLeft());
            fadeIn.setDuration(200);
        } else if (feccButton == mFeccRightBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", mFeccPanView.getLeft(), animator, mFeccPanView.getLeft());
            fadeIn.setDuration(200);
        } else if (feccButton == mFeccUpBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", mFeccPanView.getTop(), animator, mFeccPanView.getTop());
            fadeIn.setDuration(200);
        } else if (feccButton == mFeccDownBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", mFeccPanView.getTop(), animator, mFeccPanView.getTop());
            fadeIn.setDuration(200);
        }


        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                if (feccButton == mFeccLeftBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgLeft.setVisibility(View.VISIBLE);
                } else if (feccButton == mFeccRightBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgRight.setVisibility(View.VISIBLE);
                } else if (feccButton == mFeccUpBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgUp.setVisibility(View.VISIBLE);
                } else if (feccButton == mFeccDownBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgDown.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mFeccControlBg.setVisibility(View.VISIBLE);
                if (feccButton == mFeccLeftBtn) {
                    mFeccControlBgLeft.setVisibility(GONE);
                } else if (feccButton == mFeccRightBtn) {
                    mFeccControlBgRight.setVisibility(GONE);
                } else if (feccButton == mFeccUpBtn) {
                    mFeccControlBgUp.setVisibility(GONE);
                } else if (feccButton == mFeccDownBtn) {
                    mFeccControlBgDown.setVisibility(GONE);
                }
            }
        });
        fadeIn.start();

    }

    private void FeccPanTurnOrigin() {
        float animatorx = 0f;
        float animatory = 0f;
        animatorx = mFeccPanView.getLeft();
        animatory = mFeccPanView.getTop();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", animatorx);
        fadeIn.setDuration(100);
        fadeIn.start();

        ObjectAnimator fadeIny = ObjectAnimator.ofFloat(mFeccPanView, "y", animatory);
        fadeIny.setDuration(100);
        fadeIny.start();

        mFeccControlBg.setVisibility(View.VISIBLE);
        mFeccControlBgUp.setVisibility(GONE);
        mFeccControlBgDown.setVisibility(GONE);
        mFeccControlBgRight.setVisibility(GONE);
        mFeccControlBgLeft.setVisibility(GONE);
    }

    private void initFeccEventListeners() {
        createFeccBtnGestureDetector(mFeccLeftBtn, UserActionListener.USER_ACTION_FECC_LEFT, UserActionListener.USER_ACTION_FECC_STEP_LEFT);
        createFeccBtnGestureDetector(mFeccRightBtn, UserActionListener.USER_ACTION_FECC_RIGHT, UserActionListener.USER_ACTION_FECC_STEP_RIGHT);
        createFeccBtnGestureDetector(mFeccUpBtn, UserActionListener.USER_ACTION_FECC_UP, UserActionListener.USER_ACTION_FECC_STEP_UP);
        createFeccBtnGestureDetector(mFeccDownBtn, UserActionListener.USER_ACTION_FECC_DOWN, UserActionListener.USER_ACTION_FECC_STEP_DOWN);
        if (zoomInAdd != null) {

            createZoomInGestureDetector(zoomInAdd);
        }
        if (zoomInPlus != null) {

            createZoomOutGestureDetector(zoomInPlus);
        }
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_LEFT, UserActionListener.USER_ACTION_FECC_STEP_LEFT);
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_RIGHT, UserActionListener.USER_ACTION_FECC_STEP_RIGHT);
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_UP, UserActionListener.USER_ACTION_FECC_STEP_UP);
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_DOWN, UserActionListener.USER_ACTION_FECC_STEP_DOWN);
    }

    private void createFeccBtnGestureDetector(final ImageButton feccButton, final int actionTurn, final int actionStep) {
        final GestureDetector mGestureDetector = new GestureDetector(feccButton.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                sendFeccCommand(actionTurn);
                FeccPanTurnSide(feccButton);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                sendFeccCommand(actionStep);
                FeccPanTurnPingPong(feccButton);
                return true;
            }

        });

        feccButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event))
                    return true;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        FeccPanTurnOrigin();
                        if (actionTurn == UserActionListener.USER_ACTION_FECC_LEFT
                                || actionTurn == UserActionListener.USER_ACTION_FECC_RIGHT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_LEFT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_RIGHT) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_STOP);
                        } else if (actionTurn == UserActionListener.USER_ACTION_FECC_UP
                                || actionTurn == UserActionListener.USER_ACTION_FECC_DOWN
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_UP
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_DOWN) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP);
                        }
                        return true;
                }
                return true;
            }
        });
    }

    private void createZoomInGestureDetector(ImageView zoomInAdd) {
        final GestureDetector zoomGestureDetector = new GestureDetector(zoomInAdd.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                L.i(TAG, "createZoomInGestureDetector onLongPress...");
                actionListener.onUserAction(UserActionListener.FECC_ZOOM_IN, null);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                L.i(TAG, "createZoomInGestureDetector onSingleTapConfirmed...");
                actionListener.onUserAction(UserActionListener.FECC_STEP_ZOOM_IN, null);
                return true;
            }
        });

        zoomInAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (zoomGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        L.i(TAG, "createZoomInGestureDetector ACTION_UP...");
                        actionListener.onUserAction(UserActionListener.FECC_ZOOM_TURN_STOP, null);
                        return true;
                }
                return true;
            }
        });
    }

    private void createZoomOutGestureDetector(ImageView zoomInPlus) {
        final GestureDetector zoomGestureDetector = new GestureDetector(zoomInPlus.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                L.i(TAG, "createZoomOutGestureDetector onLongPress...");
                actionListener.onUserAction(UserActionListener.FECC_ZOOM_OUT, null);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                L.i(TAG, "createZoomOutGestureDetector onSingleTapConfirmed...");
                actionListener.onUserAction(UserActionListener.FECC_STEP_ZOOM_OUT, null);
                return true;
            }
        });

        zoomInPlus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (zoomGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        L.i(TAG, "createZoomOutGestureDetector ACTION_UP...");
                        actionListener.onUserAction(UserActionListener.FECC_ZOOM_TURN_STOP, null);
                        return true;
                }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFeccPanGestureDetector(final ImageView feccBigCircle, final ImageView feccSmallCircle, final int actionTurn, final int actionStep) {
        feccBigCircle.setLongClickable(true);
        feccBigCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mFeccPanView.setImageResource(R.drawable.fecc_middle_icon);
                        FeccPanTurnOrigin();
                        sendFeccCommand(UserActionListener.USER_ACTION_FECC_STOP);
                        sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP);
                        if (actionTurn == UserActionListener.USER_ACTION_FECC_LEFT
                                || actionTurn == UserActionListener.USER_ACTION_FECC_RIGHT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_LEFT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_RIGHT) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_STOP);
                        } else if (actionTurn == UserActionListener.USER_ACTION_FECC_UP
                                || actionTurn == UserActionListener.USER_ACTION_FECC_DOWN
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_UP
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_DOWN) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() >= 1) {
                            mFeccPanView.setImageResource(R.drawable.fecc_middle_icon_press);
                            int bigR = feccBigCircle.getWidth() / 2;
                            int smallR = feccSmallCircle.getWidth() / 2;
                            int r = bigR - smallR;

                            float eventx = event.getX(0);
                            float eventy = event.getY(0);

                            float absRelX = Math.abs(eventx - bigR);
                            float absRelY = Math.abs(eventy - bigR);

                            if (eventx > bigR && absRelX > absRelY && feccHorizontalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgRight.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(GONE);
                                mFeccControlBgUp.setVisibility(GONE);
                                mFeccControlBgDown.setVisibility(GONE);
                                sendFeccCommand(UserActionListener.USER_ACTION_FECC_RIGHT);
                            } else if (eventx < bigR && absRelX > absRelY && feccHorizontalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(View.VISIBLE);
                                mFeccControlBgRight.setVisibility(GONE);
                                mFeccControlBgUp.setVisibility(GONE);
                                mFeccControlBgDown.setVisibility(GONE);
                                sendFeccCommand(UserActionListener.USER_ACTION_FECC_LEFT);
                            } else if (eventy > bigR && absRelY > absRelX && feccVerticalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(GONE);
                                mFeccControlBgRight.setVisibility(GONE);
                                mFeccControlBgUp.setVisibility(GONE);
                                mFeccControlBgDown.setVisibility(View.VISIBLE);
                                sendFeccCommand(UserActionListener.USER_ACTION_FECC_DOWN);
                            } else if (eventy < bigR && absRelY > absRelX && feccVerticalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(GONE);
                                mFeccControlBgRight.setVisibility(GONE);
                                mFeccControlBgDown.setVisibility(GONE);
                                mFeccControlBgUp.setVisibility(View.VISIBLE);
                                sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP);
                            }

                            double d = Math.sqrt((eventx - bigR) * (eventx - bigR) + (eventy - bigR) * (eventy - bigR));
                            r += 25; // critical pixel 包含小圆发光距离

                            if (d > r) { // moving out of the big circle
                                float fx = ((float) bigR + ((float) r) * (eventx - (float) bigR) / (float) d);
                                float fy = ((float) bigR + ((float) r) * (eventy - (float) bigR) / (float) d);

                                if (feccHorizontalControl) {
                                    feccSmallCircle.setX(fx - smallR + 15); // FIXME: 2017/10/18 temp fix
                                }
                                if (feccVerticalControl) {
                                    feccSmallCircle.setY(fy - smallR);
                                }
                            } else {  // moving inside of the big circle
                                if (feccHorizontalControl) {
                                    feccSmallCircle.setX(eventx - smallR);
                                }
                                if (feccVerticalControl) {
                                    feccSmallCircle.setY(eventy - smallR);
                                }
                            }

                            v.invalidate();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void sendFeccCommand(int command) {
        if (command == UserActionListener.USER_ACTION_FECC_LEFT || command == UserActionListener.USER_ACTION_FECC_RIGHT) {
            if (lastFeccCommand == UserActionListener.USER_ACTION_FECC_UP || lastFeccCommand == UserActionListener.USER_ACTION_FECC_DOWN) {
                actionListener.onUserAction(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP, null);
            }
        } else if (command == UserActionListener.USER_ACTION_FECC_UP || command == UserActionListener.USER_ACTION_FECC_DOWN) {
            if (lastFeccCommand == UserActionListener.USER_ACTION_FECC_LEFT || lastFeccCommand == UserActionListener.USER_ACTION_FECC_RIGHT) {
                actionListener.onUserAction(UserActionListener.USER_ACTION_FECC_STOP, null);
            }
        }
        lastFeccCommand = command;
        actionListener.onUserAction(command, null);
    }

    public void setFECCButtonVisible(final boolean visible) {
        Log.i(TAG, " cslName kunkka setFECCButtonVisible==" + visible);
        if (mFeccControl != null) {
            mFeccControl.setVisibility(visible ? VISIBLE : INVISIBLE);
        }
    }

    public void setZoomInOutVisible(boolean visible) {
        if (zoomInAdd != null && zoomInPlus != null) {
            zoomInPlus.setVisibility(visible ? VISIBLE : INVISIBLE);
            zoomInAdd.setVisibility(visible ? VISIBLE : INVISIBLE);
            if (mFeccUpBtn != null && mFeccDownBtn != null) {
                if (visible) {
                    mFeccUpBtn.setImageResource(R.drawable.fecc_up);
                    mFeccDownBtn.setImageResource(R.drawable.fecc_down);
                } else {
                    mFeccUpBtn.setImageResource(R.drawable.fecc_up_disabled);
                    mFeccDownBtn.setImageResource(R.drawable.fecc_down_disabled);
                }
            }
        }
    }

    public void setFeccTiltControl(final boolean horizontalStatus, final boolean verticalStatus) {

        feccHorizontalControl = horizontalStatus;
        feccVerticalControl = verticalStatus;

        if (mFeccControlBgLeft != null) {
            mFeccControlBgLeft.setImageResource(R.drawable.fecc_left_bg);
        }
        if (mFeccControlBgRight != null) {
            mFeccControlBgRight.setImageResource(R.drawable.fecc_right_bg);
        }
        if (mFeccControlBgUp != null) {
            mFeccControlBgUp.setImageResource(R.drawable.fecc_up_bg);
        }
        if (mFeccControlBgDown != null) {
            mFeccControlBgDown.setImageResource(R.drawable.fecc_down_bg);
        }

        if (feccHorizontalControl && !feccVerticalControl) {    // only support horizontal
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setImageResource(R.drawable.fecc_up_disabled);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setImageResource(R.drawable.fecc_down_disabled);
            }
            if (mFeccControlBg != null) {
                mFeccControlBg.setImageResource(R.drawable.bg_toolbar_fecc_pan);
            }
        } else {
            if (mFeccControlBg != null) {
                mFeccControlBg.setImageResource(R.drawable.bg_toolbar_fecc_pan);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setImageResource(R.drawable.fecc_down);
            }
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setImageResource(R.drawable.fecc_up);
            }
        }

        if (feccVerticalControl && !feccHorizontalControl) {     // only support vertical
            if (mFeccLeftBtn != null) {
                mFeccLeftBtn.setImageResource(R.drawable.fecc_left_disabled);
            }
            if (mFeccRightBtn != null) {
                mFeccRightBtn.setImageResource(R.drawable.fecc_right_disabled);
            }

        } else {
            if (mFeccLeftBtn != null) {
                mFeccLeftBtn.setImageResource(R.drawable.fecc_left);
            }
            if (mFeccRightBtn != null) {
                mFeccRightBtn.setImageResource(R.drawable.fecc_right);
            }
        }

        if (feccHorizontalControl) {
            if (mFeccLeftBtn != null) {
                mFeccLeftBtn.setVisibility(View.VISIBLE);
            }
            if (mFeccRightBtn != null) {
                mFeccRightBtn.setVisibility(View.VISIBLE);
            }
        }

        if (feccVerticalControl) {
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setVisibility(View.VISIBLE);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setVisibility(View.VISIBLE);
            }
        } else {
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setVisibility(View.VISIBLE);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setForceLayout(int participantId) {
        NemoSDK.getInstance().forceLayout(participantId);
    }

    @Override
    public void notificationLockPeople(boolean isLockClick, boolean mLocalFullScreen, boolean isMute) {
        this.mLocalFullScreen = mLocalFullScreen;
        if (isLockClick) {
            isLock = isLockClick;
            Log.i(TAG, "notificationLockPeople==::" + isLock);
            if (!isWhiteBoardLock) {
                lockPeople.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void notificationMute(boolean isRemoteVideo, boolean isMute) {

    }

    //FECC
    public void FECCListeners() {
        setActionListener(new UserActionListener() {
            @Override
            public void onUserAction(int nAction, Bundle args) {

                switch (nAction) {
                    case UserActionListener.USER_ACTION_FECC_LEFT:
                        handleFECCControl(FECCCommand.FECC_TURN_LEFT);
                        break;
                    case UserActionListener.USER_ACTION_FECC_RIGHT:
                        handleFECCControl(FECCCommand.FECC_TURN_RIGHT);
                        break;
                    case UserActionListener.USER_ACTION_FECC_STOP:
                        handleFECCControl(FECCCommand.FECC_TURN_STOP);
                        break;
                    case UserActionListener.USER_ACTION_FECC_STEP_LEFT:
                        handleFECCControl(FECCCommand.FECC_STEP_LEFT);
                        break;
                    case UserActionListener.USER_ACTION_FECC_STEP_RIGHT:
                        handleFECCControl(FECCCommand.FECC_STEP_RIGHT);
                        break;
                    case UserActionListener.USER_ACTION_FECC_UP:
                        handleFECCControl(FECCCommand.TILT_CAMERA_TURN_UP);
                        break;
                    case UserActionListener.USER_ACTION_FECC_DOWN:
                        handleFECCControl(FECCCommand.TILT_CAMERA_TURN_DOWN);
                        break;
                    case UserActionListener.USER_ACTION_FECC_STEP_UP:
                        handleFECCControl(FECCCommand.TILT_CAMERA_STEP_UP);
                        break;
                    case UserActionListener.USER_ACTION_FECC_STEP_DOWN:
                        handleFECCControl(FECCCommand.TILT_CAMERA_STEP_DOWN);
                        break;
                    case UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP:
                        handleFECCControl(FECCCommand.TILT_CAMERA_TURN_STOP);
                        break;
                    case FECC_ZOOM_IN:
                        handleFECCControl(FECCCommand.FECC_ZOOM_IN);
                        break;
                    case FECC_STEP_ZOOM_IN:
                        handleFECCControl(FECCCommand.FECC_STEP_ZOOM_IN);
                        break;
                    case FECC_ZOOM_OUT:
                        handleFECCControl(FECCCommand.FECC_ZOOM_OUT);
                        break;
                    case FECC_STEP_ZOOM_OUT:
                        handleFECCControl(FECCCommand.FECC_STEP_ZOOM_OUT);
                        break;
                    case FECC_ZOOM_TURN_STOP:
                        handleFECCControl(FECCCommand.FECC_ZOOM_TURN_STOP);
                        break;
                }


            }
        });
    }

    @Override
    public void muteChanged(boolean mute) {
        NemoSDK.getInstance().setSpeakerMute(mute);
    }

    @Override
    public void showFaceView(List<FaceView> faceViews) {
        mVideoView.showFaceView(faceViews);
    }

    @Override
    public Activity getCallActivity() {
        return getActivity();
    }

    @Override
    public int[] getMainCellSize() {
        return new int[]{mVideoView.getWidth(), mVideoView.getHeight()};
    }

    @Override
    public void setPresenter(FaceContract.Presenter presenter) {

    }

    public void handleAiFaceChanged(final AIParam aiParam, final boolean isLocal) {
        L.i(TAG, "aiParam:" + aiParam);
        if (aiParam == null || aiParam.getParticipantId() < 0) {
            return;
        }
            Observable.just(0)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            if (isLocal) {
                                facePresenter.dealLocalAiParam(aiParam, videoInfo != null
                                        && videoInfo.getParticipantId() == aiParam.getParticipantId());
                            } else {
                                facePresenter.dealAiParam(aiParam, videoInfo != null
                                        && videoInfo.getParticipantId() == aiParam.getParticipantId());
                            }
                        }
                    });
    }
}


