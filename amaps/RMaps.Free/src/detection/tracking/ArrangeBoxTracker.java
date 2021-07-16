/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package detection.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import detection.env.BorderedText;
import detection.env.ImageUtils;
import detection.env.Logger;
import detection.tflite.Classifier;

/** A tracker that handles non-max suppression and matches existing objects to new detections. */
public class ArrangeBoxTracker {
  private static final float TEXT_SIZE_DIP = 18;
  private static final float MIN_SIZE = 16.0f;
  private static final int[] COLORS = {
          Color.BLUE,
          Color.RED,
          Color.GREEN,
          Color.YELLOW,
          Color.CYAN,
          Color.MAGENTA,
          Color.WHITE,
          Color.parseColor("#55FF55"),
          Color.parseColor("#FFA500"),
          Color.parseColor("#FF8888"),
          Color.parseColor("#AAAAFF"),
          Color.parseColor("#FFFFAA"),
          Color.parseColor("#55AAAA"),
          Color.parseColor("#AA33AA"),
          Color.parseColor("#0D0068")
  };
  final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
  private final Logger logger = new Logger();
  private final Queue<Integer> availableColors = new LinkedList<Integer>();
  private final List<TrackedRecognition> trackedObjects = new LinkedList<TrackedRecognition>();
  private final Paint boxPaint = new Paint();
  private final float textSizePx;
  private final BorderedText borderedText;
  private Matrix frameToCanvasMatrix;
  private int frameWidth;
  private int frameHeight;
  private int sensorOrientation;
  Handler handler;
  private int index;
  String fileName;
  public ArrangeBoxTracker(final Context context, Handler handler) {
    this.handler = handler;
    for (final int color : COLORS) {
      availableColors.add(color);
    }

    boxPaint.setColor(Color.RED);
    boxPaint.setStyle(Style.STROKE);
    boxPaint.setStrokeWidth(10.0f);
    boxPaint.setStrokeCap(Cap.ROUND);
    boxPaint.setStrokeJoin(Join.ROUND);
    boxPaint.setStrokeMiter(100);

    textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
  }

  public synchronized void setFrameConfiguration(
          final int width, final int height, final int sensorOrientation) {
    frameWidth = width;
    frameHeight = height;
    this.sensorOrientation = sensorOrientation;
  }

  public synchronized void drawDebug(final Canvas canvas) {
    final Paint textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setTextSize(60.0f);

    final Paint boxPaint = new Paint();
    boxPaint.setColor(Color.RED);
    boxPaint.setAlpha(200);
    boxPaint.setStyle(Style.STROKE);

    for (final Pair<Float, RectF> detection : screenRects) {
      final RectF rect = detection.second;
      canvas.drawRect(rect, boxPaint);
      canvas.drawText("" + detection.first, rect.left, rect.top, textPaint);
      borderedText.drawText(canvas, rect.centerX(), rect.centerY(), "" + detection.first);
    }
  }

  public synchronized void trackResults(final List<Classifier.Recognition> results, final long timestamp) {
    logger.i("Processing %d results from %d", results.size(), timestamp);
    processResults(results);
  }

  private Matrix getFrameToCanvasMatrix() {
    return frameToCanvasMatrix;
  }

  public synchronized void draw(final Canvas canvas) {
    final long startTime = SystemClock.uptimeMillis();
    final boolean rotated = sensorOrientation % 180 == 90;
    final float multiplier =
            Math.min(
                    canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                    canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
    frameToCanvasMatrix =
            ImageUtils.getTransformationMatrix(
                    frameWidth,
                    frameHeight,
                    (int) (multiplier * (rotated ? frameHeight : frameWidth)),
                    (int) (multiplier * (rotated ? frameWidth : frameHeight)),
                    sensorOrientation,
                    false);
    for (final TrackedRecognition recognition : trackedObjects) {
      final RectF trackedPos = new RectF(recognition.location);

      getFrameToCanvasMatrix().mapRect(trackedPos);
      final String labelString =
              !TextUtils.isEmpty(recognition.title)
                      ? String.format("%s %.2f", recognition.title, (100 * recognition.detectionConfidence))
                      : String.format("%.2f", (100 * recognition.detectionConfidence));
      boxPaint.setColor(recognition.color);

      float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
      canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint);
      Log.i("trackedPos1","trackedPos.left:"+trackedPos.left+", trackedPos.right:"+trackedPos.right+", trackedPos.top:"+trackedPos.top+", trackedPos.bottom:"+trackedPos.bottom);
      borderedText.drawText(
              canvas, trackedPos.left + cornerSize, trackedPos.top, labelString + "%", boxPaint);
      if(index==0)
      {
        new Thread(new Runnable() {
          @Override
          public void run() {
            seaveimg(cdata,frameWidth,frameHeight);

            if (labelString.contains("concentrator")) {
//              int v = cvUtils.getBTAState(cdata,frameWidth,frameHeight,Syscofig.widthPixels,Syscofig.heightPixels,(int)trackedPos.left,(int)trackedPos.top,(int)trackedPos.right,(int)trackedPos.bottom);
              String value = "集线器已识别";
              Log.e("concentrator","集线器已识别 ");
              Message msg=new Message();
              msg.obj=fileName;
              msg.what=200;
              handler.sendMessage(msg);

            }

            if (labelString.contains("geoCylinder")) {
//              int v = cvUtils.getBTAState(cdata,frameWidth,frameHeight,Syscofig.widthPixels,Syscofig.heightPixels,(int)trackedPos.left,(int)trackedPos.top,(int)trackedPos.right,(int)trackedPos.bottom);
              String value = "检波器已识别";
              Log.e("geoCylinder","检波器已识别 ");
              Message msg=new Message();
              msg.obj=fileName;
              msg.what=201;
              handler.sendMessage(msg);
            }

            if (labelString.contains("geoSquare")) {
//              int v = cvUtils.getBTAState(cdata,frameWidth,frameHeight,Syscofig.widthPixels,Syscofig.heightPixels,(int)trackedPos.left,(int)trackedPos.top,(int)trackedPos.right,(int)trackedPos.bottom);
              String value = "检波器已识别";
              Log.e("geoSquare","检波器已识别 ");
              Message msg=new Message();
              msg.obj=fileName;
              msg.what=201;
              handler.sendMessage(msg);
            }

            if (labelString.contains("surveyLine")) {
//              int v = cvUtils.getBTAState(cdata,frameWidth,frameHeight,Syscofig.widthPixels,Syscofig.heightPixels,(int)trackedPos.left,(int)trackedPos.top,(int)trackedPos.right,(int)trackedPos.bottom);
              String value = "开始识别并提取桩号信息";
              Log.e("surveyLine","桩号纸张已识别 ");
              Log.i("trackedPos2","trackedPos.left:"+trackedPos.left+", trackedPos.right:"+trackedPos.right+", trackedPos.top:"+trackedPos.top+", trackedPos.bottom:"+trackedPos.bottom);
              Message msg=new Message();
              Bundle bundle = new Bundle();
              bundle.putParcelable("trackedPos",trackedPos);
              bundle.putString("fileName",fileName);
              msg.obj=fileName;
              msg.setData(bundle);
              msg.what=202;
              handler.sendMessage(msg);
            }

            if (labelString.contains("redCloth")) {
//              int v = cvUtils.getBTAState(cdata,frameWidth,frameHeight,Syscofig.widthPixels,Syscofig.heightPixels,(int)trackedPos.left,(int)trackedPos.top,(int)trackedPos.right,(int)trackedPos.bottom);
              String value = "开始识别并提取桩号信息";
              Log.e("redCloth","手写桩号已识别 ");
              Message msg=new Message();
              Bundle bundle = new Bundle();
              bundle.putParcelable("trackedPos",trackedPos);
              bundle.putString("fileName",fileName);
              msg.obj=fileName;
              msg.setData(bundle);
              msg.what=203;
              handler.sendMessage(msg);
            }

          }
        }).start();

      }

      index++;
      if(index==10)
      {
        index=0;
      }
    }
    final long drawProcessTimes = SystemClock.uptimeMillis() - startTime;;
//    Log.e("drawProcessTimes", "drawProcessTimes:"+drawProcessTimes+"ms" );
  }

  private void processResults(final List<Classifier.Recognition> results) {
    final List<Pair<Float, Classifier.Recognition>> rectsToTrack = new LinkedList<Pair<Float, Classifier.Recognition>>();

    screenRects.clear();
    final Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());

    for (final Classifier.Recognition result : results) {
      if (result.getLocation() == null) {
        continue;
      }
      final RectF detectionFrameRect = new RectF(result.getLocation());

      final RectF detectionScreenRect = new RectF();
      rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);

      logger.v(
              "Result! Frame: " + result.getLocation() + " mapped to screen:" + detectionScreenRect);

      screenRects.add(new Pair<Float, RectF>(result.getConfidence(), detectionScreenRect));

      if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {
        logger.w("Degenerate rectangle! " + detectionFrameRect);
        continue;
      }

      rectsToTrack.add(new Pair<Float, Classifier.Recognition>(result.getConfidence(), result));
    }

    trackedObjects.clear();
    if (rectsToTrack.isEmpty()) {
      logger.v("Nothing to track, aborting.");
      return;
    }

    for (final Pair<Float, Classifier.Recognition> potential : rectsToTrack) {
      final TrackedRecognition trackedRecognition = new TrackedRecognition();
      trackedRecognition.detectionConfidence = potential.first;
      trackedRecognition.location = new RectF(potential.second.getLocation());
      trackedRecognition.title = potential.second.getTitle();
      trackedRecognition.color = COLORS[trackedObjects.size()];
      trackedObjects.add(trackedRecognition);
      if (trackedObjects.size() >= COLORS.length) {
        break;
      }
    }
  }

  private static class TrackedRecognition {
    RectF location;
    float detectionConfidence;
    int color;
    String title;
  }

  public void seaveimg(byte[] bytes,int width,int height)
  {
    fileName= Environment.getExternalStorageDirectory().toString()+"/"+
            "存储1"+".jpg";
    File file=new File(fileName);
    try {
      BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(file));
      YuvImage yuvimage=new YuvImage(bytes, ImageFormat.NV21, width, height, null);
      yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, bos);  //这里 80 是图片质量，取值范围 0-100，100为品质最高
    } catch (Exception e) {
      // TODO Auto-generated catch block
      Log.i("baichaoqun","存储失败"+e.toString());
    }
  }


  public byte[] getCdata() {
    return cdata;
  }

  public void setCdata(byte[] cdata) {
    this.cdata = cdata;
  }

  private  byte[] cdata;
}
