package com.mtkj.cnpc.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mingle.widget.ShapeLoadingDialog;
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.interfaces.HttpUtil;
import com.mtkj.cnpc.activity.interfaces.RetrofitService;
import com.mtkj.cnpc.activity.utils.BitmapUtils;
import com.mtkj.cnpc.activity.utils.FileUtils;
import com.mtkj.cnpc.activity.utils.Syscofig;
import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.CheckRecord;
import com.mtkj.utils.entity.FilesEntity;
import com.mtkj.utils.entity.PictureEntity;
import com.mtkj.utils.entity.UploadCheckInfo;
import com.mtkj.utils.entity.UploadEntity;
import com.obs.services.ObsClient;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.Ut;

import org.json.JSONObject;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.TensorFlowMultiBoxDetector;
import org.tensorflow.demo.TensorFlowObjectDetectionAPIModel;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.env.Size;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.internal.util.ObserverSubscriber;

@SuppressLint("NewApi")
public class RecognizeActivity extends Activity implements Camera.PreviewCallback, SurfaceHolder.Callback, ImageReader.OnImageAvailableListener {
    private Camera mCamera = null;

    //???
    private int width = 1920;
    private int height = 1080;

    private int rotation;
    private boolean bfrontSwitch;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder = null;
    private Context context;
//    private TextView showTitle;
    private Myhandler handler;

    //下拉列表控件
    private Spinner spinner;
    private Spinner spinner2;
    private List<String> spinner_list;
    private List<String> spinner_list2;
    ArrayAdapter<String> adapter;
    private boolean detect_pause = false;
    boolean isRecord = false;
    int distance  = 0;
    ArrayAdapter<String> adapter2;
//    ArrayAdapter<String> adapter3;

    private List<String> TF_OD_API_MODEL_FILE_LIST = new ArrayList<String>();
    private List<String> TF_OD_API_LABELS_FILE_LIST = new ArrayList<String>();
    private List<Integer> CROPSIZE_LIST = new ArrayList<Integer>();

    int model_pos = 0; //选的模型索引
    int cropsize_pos = 0; //选的裁剪大小的索引

    //模型识别
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged multibox model.
    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
    private static final String MB_LOCATION_FILE = "file:///android_asset/multibox_location_priors.txt";

    private static final int TF_OD_API_INPUT_SIZE = 300;

    private enum DetectorMode {
        TF_OD_API, MULTIBOX, YOLO;
    }

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.8f;
    private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
    private static final float MINIMUM_CONFIDENCE_YOLO = 0.25f;

    private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.TF_OD_API;

    private static final boolean SAVE_PREVIEW_BITMAP = true;
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation = 90;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private byte[] luminanceCopy;

    private BorderedText borderedText;

    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;

    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    OverlayView trackingOverlay;
    private boolean hasGotToken = false;
    File saveFile;
    String stationNo;
    private int con_count = 0;//集线器
    private boolean ischecked_con = false;
    private String conFilename;
    private String conSaveName;
    private int cy_count = 0;//检波器
    private boolean ischecked_cy = false;
    private String cyFilename;
    private String cySaveName;
    private int sur_count = 0;//桩号
    private boolean ischecked_sur = false;
    private String surFilename;
    private String surSaveName;
    private boolean ischecked_final = false;
    protected PointDBDao mPointDBDao;
    private SharedPreferences mPreferences;
    private TaskPoint taskPoint;
    private List<String> mPhotoStrings = new LinkedList<String>();
    //    private ListView lv_check;
//    private List<String> array_check;
    private ShapeLoadingDialog shapeLoadingDialog;//58 loading
    TextView tv_cy;
    TextView tv_con;
    TextView tv_sur;
    ImageView iv_cy;
    ImageView iv_con;
    ImageView iv_sur;
    LinearLayout ll_record;
    ImageView iv_record;
    TextView tv_record;
    Timer mTimer;
    TimerTask mTimerTask;
    String timeCount;
    long totaltimeCount;
    ContentResolver cr;
    private MediaProjectionManager mediaProjectionManager;
    int REQUESTRECORD = 1;
    String videoPath;//录像路径
    int is_grass;//是否有杂草
    int is_line;//是否压耳线

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不显示顶部状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置主界面布局
        setContentView(R.layout.activity_recognize);
        initData();
        initViews();
        setAdapters();
        //全屏
/*        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
//        showTitle = (TextView) findViewById(R.id.showtile);
        holder = mSurfaceView.getHolder();
        holder.setFixedSize(width, height);
        holder.addCallback(this);
    }

    private void setAdapters() {
        adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinner_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                //避免第一次被动触发此函数时，摄像头还未初始化导致的崩溃
                if (mCamera == null) {
                    return;
                }

//                Toast.makeText(MainActivity.this, "你点击的是:"+spinner_list.get(pos), Toast.LENGTH_SHORT).show();

                model_pos = pos;
                reInitCameraAndDetector();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        adapter2 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinner_list2);

        spinner2.setAdapter(adapter2);
        /*spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                //避免第一次被动触发此函数时，摄像头还未初始化导致的崩溃
                if (mCamera == null) {
                    return;
                }

                cropsize_pos = pos;
                reInitCameraAndDetector();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });*/

//        adapter3 = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,array_check);
//        lv_check.setAdapter(adapter3);
    }

    private void initViews() {
        spinner = findViewById(R.id.spinner);
        spinner_list = new ArrayList<>();

        spinner_list.add("智能质控");
//        spinner_list.add("智能质控（新）");
        TF_OD_API_MODEL_FILE_LIST.add("file:///android_asset/frozen_inference_graph.pb");
        TF_OD_API_LABELS_FILE_LIST.add("file:///android_asset/object-detection.txt");
//        TF_OD_API_MODEL_FILE_LIST.add("file:///android_asset/jianboqi.pb");
//        TF_OD_API_LABELS_FILE_LIST.add("file:///android_asset/jianboqi.txt");

        //spinner2
        spinner2 = findViewById(R.id.spinner2);
        spinner_list2 = new ArrayList<>();
        spinner_list2.add("300x300");
        CROPSIZE_LIST.add(300);
        spinner_list2.add("400x400");
        CROPSIZE_LIST.add(400);
        spinner_list2.add("500x500");
        CROPSIZE_LIST.add(500);
        spinner_list2.add("600x600");
        CROPSIZE_LIST.add(600);
        spinner_list2.add("700x700");
        CROPSIZE_LIST.add(700);
        spinner_list2.add("800x800");
        CROPSIZE_LIST.add(800);
        spinner_list2.add("900x900");
        CROPSIZE_LIST.add(900);

//        lv_check = findViewById(R.id.lv_check);
//        array_check = new ArrayList<>();
        tv_cy = findViewById(R.id.tv_cy);
        tv_con = findViewById(R.id.tv_con);
        tv_sur = findViewById(R.id.tv_sur);
        iv_cy = findViewById(R.id.iv_cy);
        iv_con = findViewById(R.id.iv_con);
        iv_sur = findViewById(R.id.iv_sur);
        ll_record = findViewById(R.id.ll_record);
        iv_record = (ImageView) findViewById(R.id.iv_record);
        tv_record = findViewById(R.id.tv_record);

        if (isRecord) {
            getScreenSize();
            getRecordPermission();
            ll_record.setVisibility(View.VISIBLE);
            AnimationDrawable animationDrawable = (AnimationDrawable) iv_record.getBackground();
            //判断是否在运行
            if (!animationDrawable.isRunning()) {
                //开启帧动画
                animationDrawable.start();
            }
            startTimer();
        }
    }

    private long baseTimer;

    private void startTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (0 == RecognizeActivity.this.baseTimer) {
                    RecognizeActivity.this.baseTimer = SystemClock.elapsedRealtime();
                }
                totaltimeCount = (int) ((SystemClock.elapsedRealtime() - RecognizeActivity.this.baseTimer) / 1000);
                String hh = new DecimalFormat("00").format(totaltimeCount / 3600);
                String mm = new DecimalFormat("00").format(totaltimeCount % 3600 / 60);
                String ss = new DecimalFormat("00").format(totaltimeCount % 60);
                timeCount = new String(hh + ":" + mm + ":" + ss);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_record.setText(timeCount);
                    }
                });
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, 1000);
    }


    private void initData() {
        mPointDBDao = new PointDBDao(this);
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        stationNo = getIntent().getStringExtra("stationNo");
        isRecord = getIntent().getBooleanExtra("isRecord", false);
        distance = getIntent().getIntExtra("distance",0);
        if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_ARRANGE) {
            taskPoint = mPointDBDao.selectArrangePoint(stationNo);
        }

        initAccessTokenWithAkSk();//初始化匹对百度文字识别sdk
        getFenBianLv();
        Log.i("baichaoqun", "sd卡：" + getSDPath());
        handler = new Myhandler();
        context = this;
        saveFile = Ut.getRMapsProjectPrivateTasksOutputDir(this);
        cr = getContentResolver();

        shapeLoadingDialog = new ShapeLoadingDialog(RecognizeActivity.this);
        shapeLoadingDialog.setLoadingText("正在上传数据...");
        shapeLoadingDialog.setCanceledOnTouchOutside(false);

    }


    public void reInitCameraAndDetector() {
        detect_pause = true;

        //关闭原来打开的摄像头
        releaseCamera();

        //创建新的detector
        try {
            if (detector != null) {
                detector.close();
                detector = null;
            }

            int cropSize = CROPSIZE_LIST.get(cropsize_pos);
//            Toast.makeText(MainActivity.this, "cropsize: "+cropSize, Toast.LENGTH_SHORT).show();
            detector = TensorFlowObjectDetectionAPIModel.create(
                    getAssets(),
                    TF_OD_API_MODEL_FILE_LIST.get(model_pos),
                    TF_OD_API_LABELS_FILE_LIST.get(model_pos),
                    cropSize);

            //更新变换矩阵、缓存图片等
            rgbFrameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            width, height,
                            cropSize, cropSize,
                            sensorOrientation, MAINTAIN_ASPECT);

            cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);
        } catch (final IOException e) {
            LOGGER.e("Exception initializing classifier!", e);
            finish();
        }

        //重新初始化摄像头
        initCamera(holder);

        detect_pause = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();//相机资源释放
    }

    private void initCamera(SurfaceHolder holder) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
/*
                Camera.Parameters mParameters = mCamera.getParameters();
                List<Camera.Size> previewSizes = mParameters.getSupportedPreviewSizes();
                int length = previewSizes.size();

                for (int i = 0; i < length; i++) {
                    Log.i("baichaoqun","SupportedPreviewSizes : " + previewSizes.get(i).width + "x" + previewSizes.get(i).height);
                }
                mCamera.setParameters(mParameters);*/
                mCamera.setDisplayOrientation(90);
            } catch (Exception e) {
                e.printStackTrace();
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
//                Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(width, height); //摄像头预览大小
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //对焦模式，可改为点击对焦
//        parameters.setPreviewFormat(ImageFormat.YUV_420_888); //默认即是这种格式
//        parameters.setPreviewFrameRate(15); //设置帧率
        mCamera.setPreviewCallback(this);
        parameters.setRotation(90);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public byte[] newbytes;

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {

        if (detect_pause) {
            LOGGER.w("detection is pause!");
            return;
        }

        newbytes = bytes;

        //人工模型读取
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }

        // Initialize the storage bitmaps once when the resolution is known.
        if (rgbBytes == null) {
            rgbBytes = new int[width * height];
            onPreviewSizeChosen(new Size(width, height), 90, bytes);
        }

        //???
        tracker.setCdata(bytes);
        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = width;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, width, height, rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        isProcessingFrame = false;
                    }
                };
        processImage();
    }

    public byte[] Nv21ToI420(byte[] data, byte[] dstData, int w, int h) {

        int size = w * h;

        System.arraycopy(data, 0, dstData, 0, size);

        for (int i = 0; i < size / 4; i++) {
            dstData[size + size / 4 + i] = data[size + i * 2 + 1];//V
        }

        return dstData;
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {

        if (width == 0 || height == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[width * height];
        }
        try {
            final Image image = imageReader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    width,
                                    height,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };
            processImage();
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();

    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public Bitmap getXyRotation(Bitmap bitmap) {
        Matrix matrix = new Matrix(); //旋转图片 动作
        matrix.setRotate(90);//旋转角度
        width = bitmap.getWidth();
        height = bitmap.getHeight(); // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public void showGrassDialog() {
        dialog = DialogUtils.Alert(this, "提示", "请检查确认杂草已除尽？",
                new String[]{"确定", "杂草未除"},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        dialog.dismiss();
                        is_grass = 0;
                        showLineDialog();
                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                is_grass = 1;
                                showLineDialog();
                            }
                        }
                });
        dialog.show();
    }

    public void showLineDialog() {
        dialog = DialogUtils.Alert(this, "提示", "请检查确认耳线已压实！",
                new String[]{getString(R.string.ok), "耳线未压"},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        is_line = 0;
                        submitInfo(station_content);
                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                is_line = 1;
                                submitInfo(station_content);
                            }
                        }
                });
        dialog.show();
    }

    boolean isFirst = true;
    public void checkComplete() {
        if (ischecked_con && ischecked_cy && ischecked_sur && ischecked_final && isFirst) {//全部识别过了
            isFirst = false;
            showGrassDialog();

        }
    }

    int arrange_type = 14;
    private void submitInfo(String content) {
        shapeLoadingDialog.show();
        mPhotoStrings.clear();
        mPhotoStrings.add(conSaveName);
        mPhotoStrings.add(cySaveName);
        mPhotoStrings.add(surSaveName);
        String name = mPreferences.getString(SysContants.USERNAME, "");
        String tel = mPreferences.getString(SysContants.TEL, "");
        String time = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分").format(new Date());
        String lon = taskPoint.geoPoint.getLongitude() + "";
        String lat = taskPoint.geoPoint.getLatitude() + "";
        int status = 0;
        CheckRecord record = new CheckRecord();
        if (is_grass == 1){
            status = 1;
            record.setRemark("杂草未除尽");
        }else if (is_line == 1){
            status = 1;
            record.setRemark("耳线未压");
        }else if (is_grass==1 && is_line==1){
            status = 1;
            record.setRemark("杂草未除尽,耳线未压");
        }
        if (mPhotoStrings.size() == 3) {
            record.setImage1(mPhotoStrings.get(0));
            record.setImage2(mPhotoStrings.get(1));
            record.setImage3(mPhotoStrings.get(2));
        } else if (mPhotoStrings.size() == 2) {
            record.setImage1(mPhotoStrings.get(0));
            record.setImage2(mPhotoStrings.get(1));
        } else if (mPhotoStrings.size() == 1) {
            record.setImage1(mPhotoStrings.get(0));
        }
        record.setName(name);
        record.setTel(tel);
        record.setStation(stationNo);
        record.setTime(time);
        record.setLon(lon);
        record.setLat(lat);
        record.setStatus(status);
        if (isRecord){
            record.setVideo(videoPath);//录像路径
        }
        Log.e("record", "record: "+record.toString() );
        mPointDBDao.insertCheckRecord(record);
        uploadImage(record,content);
    }

    public void uploadFormInfo(CheckRecord record,String content,String msg){
        UploadCheckInfo info = new UploadCheckInfo();
        info.setPileNo(record.getStation());
        info.setProcessType(arrange_type);
        info.setStatus(record.getStatus());
        info.setSinceTheCardName(record.getName());
        info.setSinceTheCardPhone(record.getTel());
        info.setSinceTheCardTime(new Date().getTime());
        info.setPileNoContent(content);
        info.setIsGrass(is_grass);
        info.setIsLine(is_line);
        info.setImg(msg);
        info.setDistance(distance);
        String json = new Gson().toJson(info);
        Log.e("json", "json: "+json );
        RequestBody body= RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        HttpUtil.init(HttpUtil.getService(RetrofitService.class).saveTaskDetails(body), new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e("Throwable",e.getMessage());
                shapeLoadingDialog.dismiss();
                Toast.makeText(RecognizeActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String result = responseBody.string();
                    Log.e("result",result);
                    PictureEntity pictureEntity = new Gson().fromJson(result,PictureEntity.class);
                    if(pictureEntity.getCode()==0){
                        shapeLoadingDialog.dismiss();
                        Toast.makeText(RecognizeActivity.this, "质检完成", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("stationNo", stationNo);
                        if (isRecord){
                            intent.putExtra("videoPath",videoPath);
                        }
                        setResult(RESULT_OK, intent);
                        finish();
                    }else {
                        Toast.makeText(RecognizeActivity.this, "质检上传失败", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
    }
    public void uploadImage(CheckRecord record,String content){
        new Thread(){
            @Override
            public void run() {
                ArrayList<String> uuidList = new ArrayList<>();
                for (int i = 0; i < mPhotoStrings.size(); i++) {
                    String endPoint = "obs.cn-east-3.myhuaweicloud.com";
                    String ak = "01PLBCK9KKM99TVKW7JV";
                    String sk = "IMM9QWe5pZGmkKyXYwPOKrLsUyx4PWACwcejmVEV";
                    // 创建ObsClient实例
                    String uuid = UUID.randomUUID().toString();
                    uuid = uuid.replaceAll("-","");
                    uuidList.add(uuid);
                    ObsClient obsClient = new ObsClient(ak, sk, endPoint);
                    try{
                        obsClient.putObject("fengtest001", uuid, new File(mPhotoStrings.get(i))); // localfile为待上传的本地文件路径，需要指定到具体的文件名
                    }catch (Exception e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RecognizeActivity.this,"图片上传失败",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }

                }
                if (uuidList.size()==3){
                    String msg = uuidList.get(0)+"@"+uuidList.get(1)+"@"+uuidList.get(2);
                    uploadFormInfo(record,content,msg);
                }
            }
        }.start();

    }
    /*public void uploadImage(CheckRecord record,String content) {
        List<MultipartBody.Part> totalParts = new ArrayList<>();
        for (int i = 0; i < mPhotoStrings.size(); i++) {
            File file = null;
            if (Build.VERSION.SDK_INT >= 24) {
                Log.e("mPhotoStrings", "mPhotoStrings: " + mPhotoStrings);
                Uri uri = Uri.parse(mPhotoStrings.get(i));
                String realPath = FileUtils.getRealFilePath(this, uri);
                Log.e("realPath", "realPath: " + realPath);
                file = new File(realPath);
            } else {
                file = new File(mPhotoStrings.get(i));
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part mBody = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            totalParts.add(mBody);
        }

        HttpUtil.init(HttpUtil.getService(RetrofitService.class).uploadImages(totalParts), new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onStart() {
                shapeLoadingDialog.show();
            }

            @Override
            public void onError(Throwable e) {
                Log.e("onErrorPic", e.toString());
                shapeLoadingDialog.dismiss();
                isFirst = true;
                Toast.makeText(RecognizeActivity.this, "图片上传失败，尝试重新上传", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadImage(record,content);
                    }
                },5000);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String result = responseBody.string();
                    Log.e("img", "img: "+result);
                    PictureEntity pictureEntity = new Gson().fromJson(result,PictureEntity.class);
                    if(pictureEntity.getCode()==0){
                        String msg = pictureEntity.getMsg();
                        uploadFormInfo(record,content,msg);
                    }else {
                        shapeLoadingDialog.dismiss();
                        isFirst = true;
                        Toast.makeText(RecognizeActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
               *//*  ArrayList<String> values = new ArrayList<>();
               try {
                    String xml = responseBody.string();
                    Log.e("xml", xml);
                    FilesEntity entity = new Gson().fromJson(xml, FilesEntity.class);
                    Log.e("size", "size: " + entity.getFileId().size());
                    for (int i = 0; i < entity.getFileId().size(); i++) {
                        JsonElement labels = entity.getFileId().get(i);
                        Log.e("JsonElement", "JsonElement: " + labels.toString());
                        JSONObject json = new JSONObject(labels.toString());
                        Iterator<String> it = json.keys();//使用迭代器
                        while (it.hasNext()) {
                            String key = it.next();//获取key
                            String value = json.getString(key);//获取value
                            values.add(value);
                            Log.e("key-value", "key=" + key + " value=" + value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*//*
//                uploadQcInfo(content, checkRecord, values);

            }
        });

    }*/

    public void uploadQcInfo(String content, CheckRecord checkRecord, ArrayList<String> values) {
        Log.e("uploadQcInfo", "uploadQcInfo: ");
        UploadEntity entity = new UploadEntity();
        entity.setTel(checkRecord.getTel());
        entity.setName(checkRecord.getName());
        entity.setStation(checkRecord.getStation());
        entity.setStation_content(content);
        entity.setLat(Double.parseDouble(checkRecord.getLat()));
        entity.setLon(Double.parseDouble(checkRecord.getLon()));
        entity.setStatus("1");
        entity.setTime(new Date().getTime());
        List<UploadEntity.FilesBean> files = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            UploadEntity.FilesBean bean = new UploadEntity.FilesBean();
            bean.setFileId(values.get(i));
            files.add(bean);
        }
        entity.setFiles(files);
        String json = new Gson().toJson(entity);
        Log.e("json", "json: " + json);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        HttpUtil.init(HttpUtil.getService(RetrofitService.class).upQcInfo(body), new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onStart() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e("onErrorPic", e.toString());
                shapeLoadingDialog.dismiss();
                Toast.makeText(RecognizeActivity.this, "质检信息上传失败，请重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                String result = null;
                try {
                    String xml = responseBody.string();
                    Log.e("xml", xml);
                    Toast.makeText(RecognizeActivity.this, "质检完成", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("stationNo", stationNo);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                shapeLoadingDialog.dismiss();
            }
        });
    }

    RectF trackedPos;
    Animation animation;

    public class Myhandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100://集线器已识别
//                    showTitle.setText("集线器已识别");
                    if (!ischecked_con) {
                        //保存图片
                        conFilename = msg.obj.toString();
                        if (conFilename != null) {
                            conSaveName = saveFile.getPath() + "/" + stationNo + "jxq" + ".jpg";
//                            copyFile(conFilename,conSaveName);
                            Uri uri = FileUtils.getUriForFile(RecognizeActivity.this, new File(conFilename));
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapFactory.decodeStream(getContentResolver().
                                        openInputStream(uri));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            //------------------旋转图片--------------------
                            Bitmap resizedBitmap = getXyRotation(bitmap);
                            //压缩图片
//                            resizedBitmap = BitmapUtils.imageZoom(resizedBitmap, 100);
                            //------------------旋转图片--------------------
                            BitmapUtils.saveImage(resizedBitmap, conSaveName);
                        }
//                        array_check.add(showTitle.getText().toString());
//                        adapter3.notifyDataSetChanged();
                        tv_con.setTextColor(Color.parseColor("#1afa29"));
                        iv_con.setImageResource(R.drawable.right);
                        animation = AnimationUtils.loadAnimation(RecognizeActivity.this, R.anim.check_on);
                        tv_con.startAnimation(animation);
                        iv_con.startAnimation(animation);
                        ischecked_con = true;
                    }
                    checkComplete();
                    break;

                case 101://检波器已识别
//                    showTitle.setText("检波器已识别");
                    if (!ischecked_cy) {
                        //保存图片
                        cyFilename = msg.obj.toString();
                        if (cyFilename != null) {
                            cySaveName = saveFile.getPath() + "/" + stationNo + "jbq" + ".jpg";
//                            copyFile(cyFilename,cySaveName);
                            Uri uri = FileUtils.getUriForFile(RecognizeActivity.this, new File(cyFilename));
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapFactory.decodeStream(getContentResolver().
                                        openInputStream(uri));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            //------------------旋转图片--------------------
                            Bitmap resizedBitmap = getXyRotation(bitmap);
                            //压缩图片
//                            resizedBitmap = BitmapUtils.imageZoom(resizedBitmap, 100);
                            //------------------旋转图片--------------------
                            BitmapUtils.saveImage(resizedBitmap, cySaveName);
                        }
                        tv_cy.setTextColor(Color.parseColor("#1afa29"));
                        iv_cy.setImageResource(R.drawable.right);
                        animation = AnimationUtils.loadAnimation(RecognizeActivity.this, R.anim.check_on);
                        tv_cy.startAnimation(animation);
                        iv_cy.startAnimation(animation);
                        ischecked_cy = true;
                    }
                    checkComplete();
                    break;

                case 102://桩号
//                    showTitle.setText("正在提取桩号信息");
                    if (!ischecked_sur) {
                        //1.保存图片
                        ischecked_sur = true;
                        surFilename = msg.getData().getString("fileName");
                        trackedPos = msg.getData().getParcelable("trackedPos");
                        surSaveName = saveFile.getPath() + "/" + stationNo + "zh" + ".jpg";
                        Uri uri = FileUtils.getUriForFile(RecognizeActivity.this, new File(surFilename));
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().
                                    openInputStream(uri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        //------------------旋转图片--------------------
                        Bitmap resizedBitmap = getXyRotation(bitmap);
                        //----------------------------------------------
                        Bitmap final_bitmap = toRoundBitmap(resizedBitmap, trackedPos);

                        if (surFilename != null) {
//                            final_bitmap = BitmapUtils.imageZoom(final_bitmap, 100);
                            BitmapUtils.saveImage(final_bitmap, surSaveName);
//                            copyFile(surFilename,surSaveName);
                        }
                        //2.使用百度识别图片提取桩号 3.判断提取图片的桩号信息是否合格
//                        recognizePicture(surSaveName);//高精度
                        recGeneralBasic(surSaveName);
                        //4.如果合格保存图片和桩号/不合格重复以上步骤
                    }
                    break;
                case 103:
//                    showTitle.setText("正在提取桩号信息");
                    if (!ischecked_sur) {
                        //1.保存图片
                        ischecked_sur = true;
                        surFilename = msg.getData().getString("fileName");
                        trackedPos = msg.getData().getParcelable("trackedPos");
                        surSaveName = saveFile.getPath() + "/" + stationNo + "zh" + ".jpg";
                        Uri uri = FileUtils.getUriForFile(RecognizeActivity.this, new File(surFilename));
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().
                                    openInputStream(uri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        //------------------旋转图片--------------------
                        Bitmap resizedBitmap = getXyRotation(bitmap);
                        //----------------------------------------------
                        Bitmap final_bitmap = toHandBitmap(resizedBitmap, trackedPos);//裁剪图片

                        if (surFilename != null) {
//                            final_bitmap = BitmapUtils.imageZoom(final_bitmap, 300);//压缩图片到300kb
                            BitmapUtils.saveImage(final_bitmap, surSaveName);//保存旋转裁剪压缩过的图片
//                            copyFile(surFilename,surSaveName);
                        }
                        //2.使用百度识别图片提取桩号 3.判断提取图片的桩号信息是否合格
//                        recognizePicture(surSaveName);//高精度
                        recGeneralBasic(surSaveName);
                        //4.如果合格保存图片和桩号/不合格重复以上步骤
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //裁剪图片 全屏放大
    public static Bitmap toRoundBitmap(Bitmap bitmap, RectF trackedPos) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        Rect src = new Rect((int) trackedPos.left - 20, (int) trackedPos.top - 50, (int) trackedPos.right + 50, (int) trackedPos.bottom + 50);
//        Rect src = new Rect((int)trackedPos.top,(int)trackedPos.left,(int)trackedPos.bottom,(int)trackedPos.right);
        RectF dst = new RectF(0, 0, width, height);
        paint.setAntiAlias(true);
//        canvas.drawRect(trackedPos, paint);
//        总的来说：
//        src就是想要绘制图片的那一部分
//        dst是这个图片要绘制在View中的坐标
        canvas.drawBitmap(bitmap, src, dst, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return output;
    }

    //裁剪图片 横向放大 纵向基本保持
    public static Bitmap toHandBitmap(Bitmap bitmap, RectF trackedPos) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = (int) (trackedPos.bottom - trackedPos.top) + 120;
        Rect src = new Rect((int) trackedPos.left - 20, (int) trackedPos.top - 50, (int) trackedPos.right + 50, (int) trackedPos.bottom + 50);
//        Rect src = new Rect((int)trackedPos.top,(int)trackedPos.left,(int)trackedPos.bottom,(int)trackedPos.right);
        RectF dst = new RectF(0, 0, width, height);
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
//        canvas.drawRect(trackedPos, paint);
//        总的来说：
//        src就是想要绘制图片的那一部分
//        dst是这个图片要绘制在View中的坐标
        canvas.drawBitmap(bitmap, src, dst, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return output;
    }

    private Dialog dialog;
    private String station_content = "";

    public void showClearDataDialog(final String fileName, StringBuffer sb) {
        Log.e("StringBuffer", "sb: " + sb);
        station_content = sb.toString();
        dialog = DialogUtils.Alert(RecognizeActivity.this, "提取桩号是否正确？", sb.toString(),
                new String[]{"是", "否"},
                new View.OnClickListener[]{new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //上传
//                        showTitle.setText("桩号已识别");
                        tv_sur.setTextColor(Color.parseColor("#1afa29"));
                        iv_sur.setImageResource(R.drawable.right);
                        animation = AnimationUtils.loadAnimation(RecognizeActivity.this, R.anim.check_on);
                        tv_sur.startAnimation(animation);
                        iv_sur.startAnimation(animation);
                        ischecked_sur = true;
                        ischecked_final = true;
                        dialog.dismiss();
                        checkComplete();
                    }
                },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ischecked_sur = false;
                                ischecked_final = false;
                                dialog.dismiss();
                            }
                        }
                });
        dialog.show();
    }

    public String getFixName() {
        Random rd = new Random();
        String str = "";
        for (int i = 0; i < 16; i++) {

// 你想生成几个字符的，就把9改成几，如果改成１,那就生成一个随机字母．
            str = str + (char) (Math.random() * 26 + 'a');
        }
        return str;
    }

    public boolean copyFile(String oldPath, String newPath) {
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //旋转图片
    public void setPhtotRotation(String path) {
        try {
            Uri uri = FileUtils.getUriForFile(this, new File(path));
            Bitmap abitmap = BitmapUtils.getBitmap(cr, uri);
            Matrix matrix = new Matrix(); //旋转图片 动作
            matrix.setRotate(90);//旋转角度
            width = abitmap.getWidth();
            height = abitmap.getHeight(); // 创建新的图片
            Bitmap resizedBitmap = Bitmap.createBitmap(abitmap, 0, 0, width, height, matrix, true);
            BitmapUtils.saveImage(resizedBitmap, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPreviewSizeChosen(final Size size, final int rotation, final byte[] data) {

        //???
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this, handler);

        int cropSize = TF_OD_API_INPUT_SIZE;
//        if (MODE == DetectorMode.YOLO) {
//          detector =
//              TensorFlowYoloDetector.create(
//                  getAssets(),
//                  YOLO_MODEL_FILE,
//                  YOLO_INPUT_SIZE,
//                  YOLO_INPUT_NAME,
//                  YOLO_OUTPUT_NAMES,
//                  YOLO_BLOCK_SIZE);
//          cropSize = YOLO_INPUT_SIZE;
//        } else
        if (MODE == DetectorMode.MULTIBOX) {
            detector =
                    TensorFlowMultiBoxDetector.create(
                            getAssets(),
                            MB_MODEL_FILE,
                            MB_LOCATION_FILE,
                            MB_IMAGE_MEAN,
                            MB_IMAGE_STD,
                            MB_INPUT_NAME,
                            MB_OUTPUT_LOCATIONS_NAME,
                            MB_OUTPUT_SCORES_NAME);
            cropSize = MB_INPUT_SIZE;
        } else {
            try {
                cropSize = CROPSIZE_LIST.get(0);
                detector = TensorFlowObjectDetectionAPIModel.create(
                        getAssets(),
                        TF_OD_API_MODEL_FILE_LIST.get(0),
                        TF_OD_API_LABELS_FILE_LIST.get(0),
                        cropSize);
            } catch (final IOException e) {
                LOGGER.e("Exception initializing classifier!", e);
//                Toast toast =
//                        Toast.makeText(
//                                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
//                toast.show();
                finish();
            }
        }

        //???
        sensorOrientation = rotation - getScreenOrientation();
//        sensorOrientation = 0;
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

//        LOGGER.i("Initializing at size %dx%d", width, height);
        rgbFrameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        width, height,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }

                        //画出实际识别区域
                       /* final Paint boxPaint = new Paint();
                        boxPaint.setColor(Color.RED);
                        boxPaint.setStyle(Paint.Style.STROKE);
                        boxPaint.setStrokeWidth(3);

                        RectF rect = new RectF(0, 0, Syscofig.heightPixels, Syscofig.heightPixels);
                        canvas.drawRect(rect, boxPaint);*/
                    }
                });

        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        if (!isDebug()) {
                            return;
                        }
                        final Bitmap copy = cropCopyBitmap;
                        if (copy == null) {
                            return;
                        }

                        final int backgroundColor = Color.argb(100, 0, 0, 0);
                        canvas.drawColor(backgroundColor);

                        final Matrix matrix = new Matrix();
                        final float scaleFactor = 2;
                        matrix.postScale(scaleFactor, scaleFactor);
                        matrix.postTranslate(
                                canvas.getWidth() - copy.getWidth() * scaleFactor,
                                canvas.getHeight() - copy.getHeight() * scaleFactor);
                        canvas.drawBitmap(copy, matrix, new Paint());

                        final Vector<String> lines = new Vector<String>();
                        if (detector != null) {
                            final String statString = detector.getStatString();
                            final String[] statLines = statString.split("\n");
                            Log.i("baichaoqun", "statString   :" + statString);
                            for (final String line : statLines) {
                                lines.add(line);
                            }
                        }
                        lines.add("");

                        lines.add("Frame: " + width + "x" + height);
                        lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                        lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                        lines.add("Rotation: " + sensorOrientation);
                        lines.add("Inference time: " + lastProcessingTimeMs + "ms");
                        Log.e("Rotation", "Rotation: " + sensorOrientation);
                        borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
                    }
                });
    }


    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    public void addCallback(final OverlayView.DrawCallback callback) {
        final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.addCallback(callback);
        }
    }

    private boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    /**
     * 抽帧处理图片识别模型
     */
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        byte[] originalLuminance = getLuminance();
        tracker.onFrame(
                width,
                height,
                getLuminanceStride(),
                sensorOrientation,
                originalLuminance,
                timestamp,
                factWidth,
                factHeight);
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");
//
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, width, 0, 0, width, height);

        if (luminanceCopy == null) {
            luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        if (croppedBitmap == null) {
                            return;
                        }
                        //开始识别模型
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                            case MULTIBOX:
                                minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                                break;
//              case YOLO:
//                minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
//                break;
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                canvas.drawRect(location, paint);

                                cropToFrameTransform.mapRect(location);
                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }
                        //处理模型
                        tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                        trackingOverlay.postInvalidate();

                        requestRender();
                        computingDetection = false;
                    }
                }).start();
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }

    int factWidth;
    int factHeight;

    public void getFenBianLv() {
        //获取手机实际的物理分辨率
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        Syscofig.widthPixels = outMetrics.widthPixels;
        Syscofig.heightPixels = outMetrics.heightPixels;
        factHeight = outMetrics.widthPixels;
        factWidth = outMetrics.heightPixels;
        Log.e("width", "width: " + outMetrics.widthPixels);
        Log.e("height", "height: " + outMetrics.heightPixels);
    }


    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    private void save2file(byte[] data, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path, false);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
//                Toast.makeText(RecognizeActivity.this,"AK，SK方式获取token失败"+error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("TAG", "onError: AK，SK方式获取token失败");
            }
        }, getApplicationContext(), "lHPSORIGoB4GXmOzR8ruyz0s", "ex5WM0txO5ApGgjGVav1zYlyWw5i21hX");
    }

    private StringBuffer sb;

    //高精度文字识别 每天只有500次
    private void recognizePicture(final String filePath) {
        //高精度
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setVertexesLocation(true);
        param.setRecognizeGranularity(GeneralParams.GRANULARITY_SMALL);
        param.setImageFile(new File(filePath));
        sb = new StringBuffer();

        // 调用高精度文字识别服务
        OCR.getInstance(this).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 调用成功，返回GeneralResult对象
                for (WordSimple wordSimple : result.getWordList()) {
                    // wordSimple不包含位置信息
                    WordSimple word = wordSimple;
                    sb.append(word.getWords());
                    sb.append("\n");
                }
                // json格式返回字符串
//                listener.onResult(result.getJsonRes());
                Log.e("WordSimple", "success: " + sb.toString());
                showClearDataDialog(filePath, sb);
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                Log.e("WordSimple", "faild: " + error.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecognizeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                ischecked_sur = false;
            }
        });
    }


    //文字识别普通版 日调用次数5w
    public void recGeneralBasic(String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        sb = new StringBuffer();
        OCR.getInstance(this).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                for (WordSimple wordSimple : result.getWordList()) {
                    WordSimple word = wordSimple;
                    sb.append(word.getWords());
                    sb.append("\n");
                }
                // json格式返回字符串
//                listener.onResult(result.getJsonRes());
                Log.e("WordSimple", "success: " + sb.toString());
                showClearDataDialog(filePath, sb);
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                Log.e("WordSimple", "faild: " + error.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecognizeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                ischecked_sur = false;
            }
        });
    }

    //识别手写体  50次/天免费
    public void recHandwriting(String filePath) {
        OcrRequestParams param = new OcrRequestParams();
        param.setImageFile(new File(filePath));
        sb = new StringBuffer();
        OCR.getInstance(this).recognizeHandwriting(param, new OnResultListener<OcrResponseResult>() {
            @Override
            public void onResult(OcrResponseResult result) {
                sb = sb.append(result.getJsonRes());
                Log.e("WordSimple", "success: " + sb.toString());
                showClearDataDialog(filePath, sb);
            }

            @Override
            public void onError(OCRError error) {
                Log.e("WordSimple", "faild: " + error.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecognizeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                ischecked_sur = false;
            }
        });
    }

    //<----------------------------------录屏开始--------------------------------------->

    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    int scDpi ;
    int scWidth;
    int scheight ;

    private void getRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent intent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUESTRECORD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode==REQUESTRECORD){
                MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                //获取mediaRecorder
                mediaRecorder = getMediaRecorder();
                virtualDisplay = mediaProjection.createVirtualDisplay("我的工作录屏",
                        scWidth, scheight, scDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mediaRecorder.getSurface(),
                        null, null);
            }
        }

        //开始录制
        try {
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

   /**
    * try {
    *                         mediaRecorder.stop();
    *                     } catch (IllegalStateException e) {
    *                         e.printStackTrace();
    *                     }
    * */

    }


    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        scDpi = dm.densityDpi;
        scWidth = dm.widthPixels;
        scheight = dm.heightPixels;
        scWidth = 1080;
        scheight = 1920;
    }


    private MediaRecorder getMediaRecorder() {
        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);    //音频载体
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);    //视频载体
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);   //输出格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);  //音频格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //视频格式
        mediaRecorder.setVideoSize(scWidth, scheight);  //size
        mediaRecorder.setVideoFrameRate(30);    //帧率
        mediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024); //比特率
        mediaRecorder.setOrientationHint(0);    //旋转角度

        //创建文件夹
        File dir = Ut.getRMapsProjectPrivateTasksOutputDir(RecognizeActivity.this);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //创建文件名
        String fileName = "录像作业"+new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".mp4";
        //设置文件位置
        videoPath = dir + "/" + fileName;
        mediaRecorder.setOutputFile(videoPath);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        return mediaRecorder;
    }

    //<----------------------------------录屏结束--------------------------------------->


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaRecorder!=null){
            mediaRecorder.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaRecorder!=null){
            mediaRecorder.stop();
        }
        super.onDestroy();
    }
}
