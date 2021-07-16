package com.mtkj.cnpc.protocol.overlay;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;

import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.utils.DensityUtil;
import com.robert.maps.applib.R;
import com.robert.maps.applib.kml.PoiManager;
import com.robert.maps.applib.kml.constants.PoiConstants;
import com.robert.maps.applib.utils.SimpleThreadFactory;
import com.robert.maps.applib.utils.Ut;
import com.robert.maps.applib.view.MapView;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

import comjph.tts.usc.SpeechUtilOffline;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GuidingOverlay extends TileViewOverlay  {
	
	private static final int SCALE[] = {25000000,15000000,8000000,4000000,2000000,1000000,500000,200000,100000,50000,25000,15000,8000,4000,2000,1000,500,250,100,50,25,10,5};
	
	public boolean isLine = false;
	
	private Context mContext;
	private PoiManager mPoiManager;
	public static int mTrackId = PoiConstants.EMPTY_ID;
	public static boolean isFollowTrack = false;
	private int mLastZoom;
	private Path mPath;
	private Point mBaseCoords;
	private GeoPoint mBaseLocation;
	private com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection mProjection;
	private Handler mMainMapActivityCallbackHandler;
	private TrackThread mThread;
	private boolean mThreadRunned = false;
	protected ExecutorService mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("TrackOverlay"));
	
	/** 是否为任务点导航 **/
	public boolean isTaskPointGuiding = false;
	
	/** 任务导航点名称 **/
	public String taskName = "";

	/** 引导点名称 **/
	public String guidePointName = "";

	/** 终点 **/
	public GeoPoint endPoint2d = null;

	/** gps位置 **/
	public GeoPoint location = null;

	/** 终点 */
	private Bitmap guidePointBitmap;

	private Paint linePaint = null;
	private Paint circlePaint = null;
	
	private SpeechUtilOffline mSpeechUtilOffline;

	public GuidingOverlay(Context context, PoiManager poiManager, Handler aHandler) {
		mContext = context;
		mMainMapActivityCallbackHandler = aHandler;
		if (linePaint == null) {
			linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			linePaint.setStyle(Style.STROKE);
			linePaint.setColor(Color.RED);
			linePaint.setStrokeWidth(16);
		}
		if (circlePaint == null) {
			circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			circlePaint.setStyle(Paint.Style.STROKE);
			circlePaint.setColor(Color.GREEN);
			circlePaint.setStrokeWidth(8);
		}
		
		this.mPoiManager = poiManager;
//		mSpeechUtilOffline = new SpeechUtilOffline(context);
		guidePointBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.amap_end);
		
	}
	
	public void startGuid(String name, GeoPoint endPoint2d, int trackId) {
		this.endPoint2d = endPoint2d;
		this.guidePointName = name;
		MapView.isGuiding = true;
		
		mTrackId = trackId;
		mPath = null;
		mThreadRunned = false;
		mLastZoom = -1;
		
		mBaseCoords = new Point();
		mBaseLocation = new GeoPoint(0, 0);
		mThread = new TrackThread();
		mThread.setName("Track thread");
		mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("TrackOverlay"));
	}
	
	public void startGuid(String name, GeoPoint endPoint2d) {
		this.endPoint2d = endPoint2d;
		this.guidePointName = name;
		MapView.isGuiding = true;
		
		mBaseCoords = new Point();
		mBaseLocation = new GeoPoint(0, 0);
	}
	
	public void updataLocation(GeoPoint geoPoint) {
		this.location = geoPoint;
	}
	
	/**
	 * 结束引导
	 */
	public void stopGuiding() {
		endPoint2d = null;
		MapView.isGuiding = false;
		if (mTrackId != PoiConstants.EMPTY_ID) {
			mPoiManager.deleteTrack(mTrackId);
		}
		isFollowTrack = false;
		mTrackId = PoiConstants.EMPTY_ID;
		mPath = null;
		
		if(mPoiManager != null)
			mPoiManager.StopProcessing();
		if(mProjection != null)
			mProjection.StopProcessing();
		mThreadExecutor.shutdown();
		
//		mSpeechUtilOffline.release();
	}
	
	/***
	 * 是否引导中
	 * 
	 * @return
	 */
	public boolean isGuiding() {
		return MapView.isGuiding;
	}

	@Override
	protected void onDraw(Canvas canvas, TileView tileView) {
		final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = tileView.getProjection();
		final Point locationScreenCoords = new Point();
		final Point endScreenCoords = new Point();
		if (isGuiding() && endPoint2d != null) {
			pj.toPixels(location, locationScreenCoords);
			pj.toPixels(endPoint2d, endScreenCoords);
			if (isLine) {
				// 绘制线
				if (this.location != null && !isFollowTrack) {
					float[] linePoints = new float[4];
					linePoints[0] = (float) locationScreenCoords.x;
					linePoints[1] = (float) locationScreenCoords.y;
					linePoints[2] = (float) endScreenCoords.x;
					linePoints[3] = (float) endScreenCoords.y;
					canvas.drawLines(linePoints, linePaint);
				}
			} else {
				if (!mThreadRunned && (mTrackId == PoiConstants.EMPTY_ID || mLastZoom != tileView.getZoomLevel())) {
					mLastZoom = tileView.getZoomLevel();
					mThreadRunned = true;
					mProjection = tileView.getProjection();
					mThreadExecutor.execute(mThread);
					return;
				}
			}
			
			if (isFollowTrack) {
				String desc = mPoiManager.selectTrackPoint(mTrackId, this.location);
				mSpeechUtilOffline.play(desc);
			}
			// 绘制终点
			if (endPoint2d != null) {
				canvas.drawBitmap(guidePointBitmap,
						(float) endScreenCoords.x - guidePointBitmap.getWidth() / 2,
						(float) endScreenCoords.y - guidePointBitmap.getHeight(), linePaint);
				
				
				float value = (float) (1.00000 / SCALE[tileView.getZoomLevel()]
						* (SysConfig.ShotproMax * 100) );
				float pxValue = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_MM, value, mContext.getResources().getDisplayMetrics());
				float dpValue = DensityUtil.px2dip(mContext, pxValue);
				canvas.drawCircle(endScreenCoords.x, endScreenCoords.y, dpValue, circlePaint);
			}
			
			final Point screenCoords = new Point();
			pj.toPixels(mBaseLocation, screenCoords);
			canvas.save();
			// 移动时，轨迹移动
			if(screenCoords.x != mBaseCoords.x && screenCoords.y != mBaseCoords.y){
				canvas.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
				canvas.scale((float)tileView.mTouchScale, (float)tileView.mTouchScale, mBaseCoords.x, mBaseCoords.y);
			}
			if (mPath != null) {
				canvas.drawPath(mPath, linePaint);
			}
		}
	}

	@Override
	protected void onDrawFinished(Canvas c, TileView tileView) {
	}
	
	private class TrackThread extends Thread {

		@Override
		public void run() {
			try {
				mPath = new Path();
				mPath = mProjection.toPixelsTrackPoints(mPoiManager.getGeoDatabase().getTrackPoints(mTrackId), mBaseCoords, mBaseLocation);
				Message.obtain(mMainMapActivityCallbackHandler, Ut.MAPTILEFSLOADER_SUCCESS_ID).sendToTarget();
			} catch (Exception e) {
				mPath = null;
			}
			mThreadRunned = false;
		}
	}
}
