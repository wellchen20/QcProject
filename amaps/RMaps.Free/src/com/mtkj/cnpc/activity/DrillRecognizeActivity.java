package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mingle.widget.ShapeLoadingDialog;
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.interfaces.HttpUtil;
import com.mtkj.cnpc.activity.interfaces.RetrofitService;
import com.mtkj.cnpc.activity.utils.CycleView;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.view.numberprogressbar.NumberProgressBar;
import com.mtkj.utils.entity.PictureEntity;
import com.mtkj.utils.entity.UploadCheckInfo;
import com.obs.services.ObsClient;
import com.obs.services.model.ProgressListener;
import com.obs.services.model.ProgressStatus;
import com.obs.services.model.PutObjectRequest;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.TimeUtil;
import com.robert.maps.applib.utils.Ut;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import detection.customview.OverlayView;
import detection.env.BorderedText;
import detection.env.ImageUtils;
import detection.tflite.Classifier;
import detection.tflite.TFLiteObjectDetectionAPIModel;
import detection.tracking.MultiBoxTracker;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Subscriber;

public class DrillRecognizeActivity extends Activity implements Camera.PreviewCallback, SurfaceHolder.Callback, ImageReader.OnImageAvailableListener{
    SurfaceView surfaceView;
    private SurfaceHolder holder = null;
    private Camera mCamera = null;

    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private static final int TF_OD_API_INPUT_SIZE = 224;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "ssdlite_mobilenetv3.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/quantized_model.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = true;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;
    public MyHandler handler;
    public Handler run_handler;
    private static final float TEXT_SIZE_DIP = 10;

    protected int previewWidth = 0;
    protected int previewHeight = 0;
    int width = 640;
    int height = 480;
    private boolean debug = false;
    private long timestamp = 0;

    public TextView tv_mCount;
    public TextView tv_title;
    //    public TextView tv_hat;
//    public TextView tv_hand;
//    public ImageView iv_hat;
//    public ImageView iv_hand;
    public LinearLayout ll_reg_status;
    public ImageView iv_reg_status;
    public TextView tv_reg_status;
    private Dialog dialog;
    int is_wear = 0;
    int is_dangerous = 0;
    int drill_type = 15;
    public boolean is_drilling = false;
    LinearLayout ll_record;
    LinearLayout ll_progress;
    LinearLayout ll_caculate;
    ImageView iv_record;
    TextView tv_record;
    Timer mTimer;
    TimerTask mTimerTask;
    String timeCount;
    long totaltimeCount;
    Animation animation;
    public ShapeLoadingDialog shapeLoadingDialog;//58 loading
    private PointDBDao mPointDBDao;
    private DrillPoint drillPoint;
    private SharedPreferences mPreferences;
    String stationNo;
    File saveFile;
    public boolean isRecord = false;
    int distance  = 0;
    NumberProgressBar progressBar;
    private enum DetectorMode {
        TF_OD_API;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_recognize);
//        StatusBarUtil.setTranslucentStatus(this);
//        getFenBianLv();
        mPointDBDao = new PointDBDao(this);
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        saveFile= Ut.getRMapsProjectPrivateTasksOutputDir(this);
        stationNo = getIntent().getStringExtra("stationNo");
        isRecord = getIntent().getBooleanExtra("isRecord",false);
        distance = getIntent().getIntExtra("distance",0);
        getScreenSize();
        setViews();
        if (isRecord){
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

    private void setViews() {
        tv_mCount = findViewById(R.id.tv_mCount);
        tv_mCount = findViewById(R.id.tv_mCount);
        tv_title = findViewById(R.id.tv_title);
//        tv_hat = findViewById(R.id.tv_hat);
//        tv_hand = findViewById(R.id.tv_hand);
//        iv_hat = findViewById(R.id.iv_hat);
//        iv_hand = findViewById(R.id.iv_hand);
        ll_caculate = findViewById(R.id.ll_caculate);
        ll_reg_status = findViewById(R.id.ll_reg_status);
        ll_progress = findViewById(R.id.ll_progress);
        tv_reg_status = findViewById(R.id.tv_reg_status);
        iv_reg_status = findViewById(R.id.iv_reg_status);
        ll_record = findViewById(R.id.ll_record);
        iv_record = (ImageView) findViewById(R.id.iv_record);
        tv_record = findViewById(R.id.tv_record);
        surfaceView = findViewById(R.id.surfaceView);
        progressBar = findViewById(R.id.progressBar);
        shapeLoadingDialog = new ShapeLoadingDialog(DrillRecognizeActivity.this);
        shapeLoadingDialog.setLoadingText("正在上传数据...");
        shapeLoadingDialog.setCanceledOnTouchOutside(false);
        holder = surfaceView.getHolder();
        holder.setFixedSize(640, 480);
        holder.addCallback(this);

        iv_reg_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_drilling==false){
                    iv_reg_status.setImageResource(R.drawable.stop_reg);
                    tv_reg_status.setText("停止");
                    tv_reg_status.setTextColor(Color.RED);
                    is_drilling = true;
                    tracker.setDrillStatus(is_drilling);
                }else if (is_drilling){
                    iv_reg_status.setImageResource(R.drawable.start_reg);
                    tv_reg_status.setText("已结束");
                    tracker.setDrillStatus(false);
                    showWearDialog();
                }
            }
        });
    }
    private long baseTimer;
    private void startTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (0 == baseTimer) {
                    baseTimer = SystemClock.elapsedRealtime();
                }
                totaltimeCount = (int) ((SystemClock.elapsedRealtime() - baseTimer) / 1000);
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

    public void getFenBianLv() {
        //获取手机实际的物理分辨率
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
//        Log.e("width", "width: " + outMetrics.widthPixels);
//        Log.e("height", "height: " + outMetrics.heightPixels);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (isProcessingFrame) {
//            LOGGER.w("Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
//            LOGGER.e(e, "Exception!");
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
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

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        final long startTime = SystemClock.uptimeMillis();
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
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
                                    previewWidth,
                                    previewHeight,
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
//            LOGGER.e(e, "Exception!");
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
//                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
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
                mCamera.setDisplayOrientation(90);
            } catch (Exception e) {
                e.printStackTrace();
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
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

    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final long kaishiTime = SystemClock.uptimeMillis();
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        handler = new MyHandler();
        run_handler = new Handler();
        tracker = new MultiBoxTracker(this,handler);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
//            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
//        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

//        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback( Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
        final long preTimes = SystemClock.uptimeMillis() - kaishiTime;
//        Log.e("preTimes", "preTimes:"+preTimes+"ms" );//
    }

    protected void processImage() {
        //超哥看这里
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        final long kaishiTime = SystemClock.uptimeMillis();
        computingDetection = true;
//        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        long pixels = SystemClock.uptimeMillis();
        long pixelsMs = pixels - kaishiTime;
//        Log.e("pixelsMs", "pixelsMs:"+pixelsMs+"ms" );//打印后处理时长
        readyForNextImage();
        long readyTime = SystemClock.uptimeMillis();
        long readyTimeMs = readyTime - pixels;
//        Log.e("readyTimeMs", "readyTimeMs:"+readyTimeMs+"ms" );//打印后处理时长
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            //保存preview
//      ImageUtils.saveRecognizeBitmap(croppedBitmap);
        }
        long canvasMs = SystemClock.uptimeMillis() - readyTime;
//        Log.e("canvasMs", "canvasMs:"+canvasMs+"ms" );//打印后处理时长
        long processImageTimeMs = SystemClock.uptimeMillis() - kaishiTime;
//        Log.e("processImage", "processImage:"+processImageTimeMs+"ms" );//打印后处理时长

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
//                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        final long endTime = SystemClock.uptimeMillis();
//                        lastProcessingTimeMs = endTime - startTime;
//                        Log.e("lastProcessingTimeMs", "lastProcessingTimeMs:"+lastProcessingTimeMs+"ms" );//打印后处理时长
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
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {//计算坐标
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                canvas.drawRect(location, paint);
                                cropToFrameTransform.mapRect(location);
                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }
                        tracker.trackResults(mappedRecognitions, currTimestamp);//传递识别图片后的坐标
                        if (SAVE_PREVIEW_BITMAP){
                            //保存识别后的图片
//              ImageUtils.saveRecognizeBitmap(cropCopyBitmap);
                        }

                        trackingOverlay.postInvalidate();//刷新UI 调用ondraw 这里就是画框

                        computingDetection = false;
                        final long zuobiao = SystemClock.uptimeMillis() - endTime;;
                        final long totalTimes = SystemClock.uptimeMillis() - kaishiTime;
//                        Log.e("zuobiao", "zuobiao:"+zuobiao+"ms" );//打印处理一帧图片花费的总时长
//                        Log.e("totalTimes", "totalTimes:"+totalTimes+"ms" );//打印处理一帧图片花费的总时长
                        lastProcessingTimeMs = totalTimes;
                    }
                });
    }



    protected synchronized void runInBackground(final Runnable r) {
        if (run_handler != null) {
            run_handler.post(r);
        }
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

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }



    public class  MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 100:
                    new CaculateTask((List<Float>)msg.obj).execute();
                    break;
//                case 101://hand
//                    tv_hand.setTextColor(Color.parseColor("#1afa29"));
//                    iv_hand.setImageResource(R.drawable.right);
//                    animation = AnimationUtils.loadAnimation(DrillRecognizeActivity.this, R.anim.check_on);
//                    tv_hand.startAnimation(animation);
//                    iv_hand.startAnimation(animation);
//                    break;
//                case 102://hat
//                    tv_hat.setTextColor(Color.parseColor("#1afa29"));
//                    iv_hat.setImageResource(R.drawable.right);
//                    animation = AnimationUtils.loadAnimation(DrillRecognizeActivity.this, R.anim.check_on);
//                    tv_hat.startAnimation(animation);
//                    iv_hat.startAnimation(animation);
//                    break;
//                case 103://plummet
                //判断结束
//                    shapeLoadingDialog.show();
//                    tv_mCount.setTextColor(Color.parseColor("#1afa29"));
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (shapeLoadingDialog!=null){
//                                shapeLoadingDialog.dismiss();
//                            }
//                            saveDrillSql();
//                            Toast.makeText(DrillRecognizeActivity.this,"质检完成",Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent();
//                            intent.putExtra("stationNo",stationNo);
//                            intent.putExtra("videoPath",videoPath);
//                            Log.e("realPath", "realPath: "+videoPath );
//                            setResult(RESULT_OK,intent);
//                            finish();
//                        }
//                    },2000);
//                    break;
            }

        }
    }

    int count = 0;//
    int i1 = 0;
    List<Integer> flag_list = new ArrayList<>();
    boolean isFive = true;
    class CaculateTask extends AsyncTask {
        List<Float> caculateList = new ArrayList<>();
        public CaculateTask(List<Float> caculateList){
            this.caculateList = caculateList;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            if (caculateList.size()>=4){
                float L1 = caculateList.get(caculateList.size() - 1) - caculateList.get(caculateList.size() - 2);
                float L2 = caculateList.get(caculateList.size() - 2) - caculateList.get(caculateList.size() - 3);
                float L3 = caculateList.get(caculateList.size() - 3) - caculateList.get(caculateList.size() - 4);
//                float L4 = caculateList.get(caculateList.size() - 4) - caculateList.get(caculateList.size() - 5);
//                Log.e("doInBackground", "L1:"+L1+" L2:"+L2+" L3:"+L3 );
                if (L1 > 0 && L2 > 0 && L3 > 0) {
                    flag_list.add(1);
                } else if (L1 < 0 && L2 < 0 && L3 < 0) {
                    flag_list.add(-1);
                }else {
                    flag_list.add(0);
                }
            }
            if (flag_list.size()>=3){
                for (int i=i1;i<flag_list.size();i++){
                    if (flag_list.get(i)==1){
                        int i2 = 0;
                        boolean isEx = false;
                        for (int j=i1;j<i;j++){
                            if (flag_list.get(j)==-1){
                                i2 = j;
                                isEx = true;
                            }
                        }
                        if (isEx){
                            boolean flag = true;
                            for (int k=i2;k<i;k++){
                                if (flag_list.get(k)==1){
                                    flag = false;
                                }
                            }
                            if (flag){
                                count++;
                            }
                        }
                        i1 = i;
                    }

                }
            }
            return count;
        }

        @Override
        protected void onPostExecute(Object o) {
            int cc = (int) o;
//            Log.e("count", "count: "+o);
           /* if (count==5 && isFive){
                for (int i=caculateList.size()-1;i>=4;i--){
                    Log.e("caculateList", "caculateList: "+caculateList.get(i));
                    Log.e("caculateList", "--------------"+i);
                }

                for (int i=0;i<flag_list.size();i++){
                    Log.e("flag_list", "flag_list: "+flag_list.get(i));
                    Log.e("doInBg", "-------------------"+i);
                }
                isFive = false;
            }*/
            tv_mCount.setText("井深"+count+"米");
//            caculateList.clear();
        }
    }


    //<----------------------------------录屏开始--------------------------------------->

    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionManager mediaProjectionManager;
    int REQUESTRECORD = 400;
    String videoPath;
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

    }


    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        scDpi = dm.densityDpi;
        scWidth = dm.widthPixels;
        scheight = dm.heightPixels;
        Log.e("size", "getScreenSize: "+scWidth+"|"+scheight );//agm 3x使用最大分辨率会报错 无法录屏
        scWidth = 1080;
        scheight = 1920;
    }


    private MediaRecorder getMediaRecorder() {
        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);    //音频载体
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);    //视频载体
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);   //输出格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);  //音频格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //视频格式
        mediaRecorder.setVideoSize(scWidth, scheight);  //size
        mediaRecorder.setVideoFrameRate(24);    //帧率
        mediaRecorder.setVideoEncodingBitRate(4 * 1024 * 1024); //比特率
        mediaRecorder.setOrientationHint(0);    //旋转角度

        //创建文件夹
        File dir = Ut.getRMapsProjectPrivateTasksOutputDir(DrillRecognizeActivity.this);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //创建文件名
        String fileName = stationNo + ".mp4";
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
    public synchronized void onDestroy() {
        if (mediaRecorder!=null){
            try{
                mediaRecorder.reset();
                mediaRecorder.release();
            }catch (Exception e){
                Log.e("Exception", e.getMessage()+"");
                Toast.makeText(DrillRecognizeActivity.this,"录屏发生错误",Toast.LENGTH_SHORT).show();
            }

        }
        super.onDestroy();
    }
    public void showWearDialog() {
        dialog = DialogUtils.Alert(this, "提示", "请检查所有人员穿戴安全帽和工服？",
                new String[]{"确定", "未穿戴"},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        dialog.dismiss();
                        is_wear = 0;
                        showDangerousDialog();
                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                is_wear = 1;
                                showDangerousDialog();
                            }
                        }
                });
        dialog.show();
    }

    private void showDangerousDialog() {
        dialog = DialogUtils.Alert(this, "提示", "请检查所有人员无违规行为？",
                new String[]{"确定", "有违规行为"},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        dialog.dismiss();
                        is_dangerous = 0;
                        uploadCheckInfo();
                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                is_dangerous = 1;
                                uploadCheckInfo();
                            }
                        }
                });
        dialog.show();
    }

    private void showVideoDialog(DrillRecord record) {
        dialog = DialogUtils.Alert(this, "提示", "是否立刻上传视频数据？",
                new String[]{"是", "否"},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        dialog.dismiss();
                        uploadVideo(record);
                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                uploadFormInfo(record,"");
                            }
                        }
                });
        dialog.show();
    }

    private void uploadCheckInfo() {
        tv_mCount.setTextColor(Color.parseColor("#1afa29"));
        saveDrillSql();
    }

    public void saveDrillSql(){
        drillPoint = mPointDBDao.selectDrillPoint(stationNo);
        final DrillRecord record = new DrillRecord();
        record.stationNo = drillPoint.stationNo;//桩号
        record.lineNo = drillPoint.lineNo;//线号
        record.spointNo = drillPoint.spointNo;//点号
        record.lon = String.valueOf(drillPoint.geoPoint.getLongitude());//经度
        record.lat = String.valueOf(drillPoint.geoPoint.getLatitude());//纬度
        record.drilldepth = count+"";//实际井深
        record.video = videoPath;//备注
        record.drilltime = TimeUtil.getCurrentTimeInString();//完成时间
        record.name = mPreferences.getString(SysContants.USERNAME, "");//执行人
        record.tel = mPreferences.getString(SysContants.TEL, "");//电话
        record.isupload = "1";
        record.status = 0;
        record.remark = "";
        if (is_wear == 1){
            record.status = 1;
            record.remark = "安全帽或工服未穿戴";
        } if (is_dangerous == 1){
            record.status = 1;
            record.remark = record.remark+"|员工在现场存在危险行为";
        } if (count<drillPoint.desWellDepth){
            record.status = 1;
            record.remark = record.remark+"|井深不达标";
        }
        Log.e("remark", "remark: "+record.remark );
        mPointDBDao.insertDrillRecord(record);//插入钻井记录
        showVideoDialog(record);
    }

    private void uploadVideo(DrillRecord record) {
        if (mediaRecorder!=null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder=null;
        }
        ll_caculate.setVisibility(View.GONE);
        showProgress();
        new Thread(){
            @Override
            public void run() {
                dealVideos(record);
            }
        }.start();
    }

    public void dealVideos(DrillRecord record){
        String endPoint = "obs.cn-east-3.myhuaweicloud.com";
        String ak = "01PLBCK9KKM99TVKW7JV";
        String sk = "IMM9QWe5pZGmkKyXYwPOKrLsUyx4PWACwcejmVEV";
// 创建ObsClient实例
        ObsClient obsClient = new ObsClient(ak, sk, endPoint);
        String uuid = UUID.randomUUID().toString();
        Log.e("uuid", "uuid1: "+uuid);
        uuid = uuid.replaceAll("-","");
        Log.e("uuid", "uuid2: "+uuid);
        PutObjectRequest request = new PutObjectRequest("fengtest001", uuid);
        request.setFile(new File(videoPath)); // localfile为上传的本地文件路径，需要指定到具体的文件名
        String finalUuid = uuid;
        request.setProgressListener(new ProgressListener() {

            @Override
            public void progressChanged(ProgressStatus status) {
                // 获取上传平均速率
                Log.e("PutObject", "AverageSpeed:" + status.getAverageSpeed());
                // 获取上传进度百分比
                Log.e("PutObject", "TransferPercentage:" + status.getTransferPercentage());
                int count_pro = status.getTransferPercentage();
                cyl_credit.setProgress(count_pro);
                if (count_pro==100){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            record.isupload = "0";
                            mPointDBDao.updateDrilRecord(record);
                            uploadVideoPath(record, finalUuid);//上传视频地址和桩号
                        }
                    });

                }
            }
        });
// 每上传1MB数据反馈上传进度
        request.setProgressInterval(1024 * 1024L);
        obsClient.putObject(request);
    }

    private void uploadVideoPath(DrillRecord record,String uuid) {
        HttpUtil.init(HttpUtil.getService(RetrofitService.class).uploadFile(uuid, record.stationNo), new Subscriber<PictureEntity>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                uploadFormInfo(record,"");//上传质检结果
            }

            @Override
            public void onNext(PictureEntity pictureEntity) {
                int code = pictureEntity.getCode();
                Log.e("code", "code: "+code );
                uploadFormInfo(record,"");//上传质检结果
            }
        });
    }

    /*public void dealVideo(DrillRecord record){//本地视频上传带进度
        ll_caculate.setVisibility(View.GONE);
        showProgress();
        File file = new File(videoPath);
        RetrofitCallback<PictureEntity> callback = new RetrofitCallback<PictureEntity>() {
            @Override
            public void onSuccess(Call<PictureEntity> call, Response<PictureEntity> response) {
//                runOnUiThread(activity, response.body().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("onSuccess", "onSuccess: "+response.body().getMsg()+"|code = "+response.body().getCode());
                        String address = response.body().getMsg();
                        int code = response.body().getCode();
                        if (code==0){
//                            record.isupload = "0";
//                            mPointDBDao.updateDrilRecord(record);
//                            uploadFormInfo(record,address);//上传质检结果
                        }
                    }
                });

                //进度更新结束
            }
            @Override
            public void onFailure(Call<PictureEntity> call, Throwable t) {
//                runOnUIThread(activity, t.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("onFailure", "onFailure: "+t.getMessage() );
                        uploadFormInfo(record,"");//上传质检结果
                        Toast.makeText(DrillRecognizeActivity.this,"视频上传失败",Toast.LENGTH_SHORT).show();
                    }
                });

                //进度更新结束
            }
            @Override
            public void onLoading(long total, long progress) {
                super.onLoading(total, progress);
                BigDecimal currentCount = new BigDecimal(progress);
                BigDecimal totalCount = new BigDecimal(total);
                BigDecimal divide = currentCount.divide(totalCount,2, BigDecimal.ROUND_HALF_UP);
                long pro = divide.multiply(new BigDecimal(100)).longValue();
//                Log.e("onLoading", "total:"+total+"|progress:"+progress+"|pro"+pro );
                int count_pro = (int)pro;
                cyl_credit.setProgress(count_pro);
                if (count_pro==100){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            record.isupload = "0";
                            mPointDBDao.updateDrilRecord(record);
                            uploadFormInfo(record,"");//上传质检结果
                        }
                    });

                }


//                wave_progress_view_2.setProgress((int) pro);
                //此处进行进度更新
            }
        };
        RequestBody body1 = RequestBody.create(MediaType.parse("application/otcet-stream"), file);
        //通过该行代码将RequestBody转换成特定的FileRequestBody
        FileRequestBody body = new FileRequestBody(body1, callback);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
        Call<PictureEntity> call = HttpUtil.getService(RetrofitService.class).uploadFile(part,stationNo);
        call.enqueue(callback);
    }*/

    CycleView cyl_credit;
    Dialog lDialog;
    public void showProgress() {
        lDialog = new Dialog(this, R.style.MyDialogStyleFullText);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setCancelable(false);
        lDialog.setContentView(R.layout.layout_progess_upload);
        cyl_credit =  lDialog.findViewById(R.id.cyl_credit);
        lDialog.show();
    }

    public void uploadFormInfo(DrillRecord record,String address){
        if (lDialog==null || !lDialog.isShowing()){
            shapeLoadingDialog.show();
        }
        UploadCheckInfo info = new UploadCheckInfo();
        info.setPileNo(record.stationNo);
        info.setProcessType(drill_type);
        info.setStatus(record.status);
        info.setSinceTheCardName(record.name);
        info.setSinceTheCardPhone(record.tel);
        info.setSinceTheCardTime(new Date().getTime());
        info.setPileNoContent("");
        info.setDrillDepth(count+"");
        info.setMeasureDepth(count+"");
        info.setIsWearHat(is_wear);
        info.setIsWearClothes(is_wear);
        info.setIsViolation(is_dangerous);
        info.setDistance(distance);
//        info.setImg(address);
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
                if (lDialog!=null && lDialog.isShowing()){
                    lDialog.dismiss();
                }
                if (shapeLoadingDialog!=null){
                    shapeLoadingDialog.dismiss();
                }

                Toast.makeText(DrillRecognizeActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    if (lDialog!=null&&lDialog.isShowing()){
                        lDialog.dismiss();
                    }
                    if (shapeLoadingDialog!=null){
                        shapeLoadingDialog.dismiss();
                    }
                    String result = responseBody.string();
                    Log.e("result",result);
                    PictureEntity pictureEntity = new Gson().fromJson(result,PictureEntity.class);
                    if(pictureEntity.getCode()==0){
                        Toast.makeText(DrillRecognizeActivity.this, "质检完成", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("stationNo", stationNo);
                        if (isRecord){
                            intent.putExtra("videoPath",videoPath);
                        }
                        setResult(RESULT_OK, intent);
                        finish();
                    }else {
                        Toast.makeText(DrillRecognizeActivity.this, "质检上传失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
