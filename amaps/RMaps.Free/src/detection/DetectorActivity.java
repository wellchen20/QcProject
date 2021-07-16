/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package detection;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

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
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.utils.BitmapUtils;
import com.mtkj.cnpc.activity.utils.FileUtils;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.ArrangeRecord;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.TimeUtil;
import com.robert.maps.applib.utils.Ut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import detection.customview.OverlayView;
import detection.env.BorderedText;
import detection.env.ImageUtils;
import detection.env.Logger;
import detection.tflite.Classifier;
import detection.tflite.TFLiteObjectDetectionAPIModel;
import detection.tracking.ArrangeBoxTracker;
import detection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static  int TF_OD_API_INPUT_SIZE = 224;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String[] TF_OD_API_MODEL_LIST = {"ssdlite_mobilenetv3.tflite","ssd_mobilenetv3_geo.tflite"};
    private static final String[] TF_OD_API_LABELS_LIST = {"file:///android_asset/quantized_model.txt","file:///android_asset/geo-detection.txt"};
    private static  String TF_OD_API_MODEL_FILE = "ssdlite_mobilenetv3.tflite";
    private static  String TF_OD_API_LABELS_FILE = "file:///android_asset/quantized_model.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

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
    private ArrangeBoxTracker boxTracker;

    private BorderedText borderedText;
    public MyHandler handler;
    Animation animation;
    private PointDBDao mPointDBDao;
    private DrillPoint drillPoint;
    private SharedPreferences mPreferences;
    //检波器
    File saveFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        mPointDBDao = new PointDBDao(this);
        saveFile= Ut.getRMapsProjectPrivateTasksOutputDir(this);
        getScreenSize();
        if (isRecord){
            getRecordPermission();
        }
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final long kaishiTime = SystemClock.uptimeMillis();
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        handler = new MyHandler();
        int cropSize = TF_OD_API_INPUT_SIZE;
        TF_OD_API_MODEL_FILE = TF_OD_API_MODEL_LIST[0];
        TF_OD_API_LABELS_FILE = TF_OD_API_LABELS_LIST[0];
        tracker = new MultiBoxTracker(this,handler);
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
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);


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
    }



    @Override
    protected void processImage() {//超哥看这里
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        readyForNextImage();
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        final long endTime = SystemClock.uptimeMillis();
                        lastProcessingTimeMs = endTime - startTime;
//                        Log.e("lastProcessingTimeMs", "lastProcessingTimeMs:"+lastProcessingTimeMs+"ms" );//打印后处理时长
                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Style.STROKE);
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
//                        if (SAVE_PREVIEW_BITMAP){
//                            //保存识别后的图片
//                        ImageUtils.saveBitmap(cropCopyBitmap);
//                        }

                        trackingOverlay.postInvalidate();//刷新UI 调用ondraw 这里就是画框

                        computingDetection = false;

                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
    RectF trackedPos;
    public class  MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 100:
                    new CaculateTask((List<Float>)msg.obj).execute();
                    break;
                case 101://hand
                    tv_hand.setTextColor(Color.parseColor("#1afa29"));
                    iv_hand.setImageResource(R.drawable.right);
                    animation = AnimationUtils.loadAnimation(DetectorActivity.this, R.anim.check_on);
                    tv_hand.startAnimation(animation);
                    iv_hand.startAnimation(animation);
                    break;
                case 102://hat
                    tv_hat.setTextColor(Color.parseColor("#1afa29"));
                    iv_hat.setImageResource(R.drawable.right);
                    animation = AnimationUtils.loadAnimation(DetectorActivity.this, R.anim.check_on);
                    tv_hat.startAnimation(animation);
                    iv_hat.startAnimation(animation);
                    break;
                case 103://plummet
                    //判断结束
                    if (count>=7){
                        shapeLoadingDialog.show();
                        tv_mCount.setTextColor(Color.parseColor("#1afa29"));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (shapeLoadingDialog!=null){
                                    shapeLoadingDialog.dismiss();
                                }
                                saveDrillSql();
                                Toast.makeText(DetectorActivity.this,"质检完成",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.putExtra("stationNo",stationNo);
                                intent.putExtra("videoPath",videoPath);
                                Log.e("realPath", "realPath: "+videoPath );
                                setResult(RESULT_OK,intent);
                                finish();
                            }
                        },2000);
                    }
                    break;
            }

        }
    }


    //<---------------------------------------drill---------------------------------------->
    //drill
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
        record.status = 1;

        mPointDBDao.insertDrillRecord(record);//插入钻井记录
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
            tv_mCount.setText("井深"+count+"米");
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
        File dir = Ut.getRMapsProjectPrivateTasksOutputDir(DetectorActivity.this);
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
    public synchronized void onDestroy() {
        if (mediaRecorder!=null){
            try{
                mediaRecorder.stop();
                mediaRecorder.release();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(DetectorActivity.this,"录屏发生错误",Toast.LENGTH_SHORT).show();
            }

        }
        super.onDestroy();
    }
}
