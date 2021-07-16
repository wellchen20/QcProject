package com.mtkj.cnpc.protocol.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.andnav.osm.util.GeoPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.mtkj.cnpc.sqlite.PointDBDao;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

/**
 * 钻井下药图层
 * 
 * @author DRH
 *
 */
public class BulldozOverlay extends TileViewOverlay {
	private PointDBDao mPointDBDao;
	private int mTapId;
	private GeoPoint mLastMapCenter;
	private int mLastZoom;
	private BulldozListThread mThread;
	public static int NO_TAP = -9999;

	protected OnItemTapListener<GeoPoint> mOnItemTapListener;
	protected OnItemLongPressListener<GeoPoint> mOnItemLongPressListener;
	protected List<String> mBlocks = new ArrayList<String>();
	protected HashMap<String, List<GeoPoint>> mBlockList = new HashMap<String, List<GeoPoint>>();
	protected List<GeoPoint> mBulldozPointList = new ArrayList<GeoPoint>();

	/**
	 * 区块
	 */
	private Paint blockNodePaint = null;
	private Paint blockLinePaint = null;
	private Paint blockTextPaint = null;
	/**
	 * 下地口
	 */
	private Paint pointNodePaint = null;
	private Paint pointTextPaint = null;
	
	public int getTapIndex() {
		return mTapId;
	}

	public void setTapIndex(int mTapIndex) {
		this.mTapId = mTapIndex;
	}
	

	public BulldozOverlay(Context ctx, PointDBDao pointDBDao, OnItemTapListener<GeoPoint> onItemTapListener) {
		initPaint();
		mPointDBDao = pointDBDao;
		mBlocks = pointDBDao.selectBulldozBlock();
		mTapId = NO_TAP;

        this.mOnItemTapListener = onItemTapListener;

        mLastMapCenter = null;
        mLastZoom = -1;
        mThread = new BulldozListThread();// 获取范围内推土

	}
	
	public void initPaint() {
		blockNodePaint = new Paint();
		blockNodePaint.setColor(Color.YELLOW);
		blockNodePaint.setStyle(Paint.Style.STROKE);
		blockNodePaint.setStrokeWidth(8);
		blockLinePaint = new Paint();
		blockLinePaint.setColor(Color.GREEN);
		blockLinePaint.setStrokeWidth(5);
		blockTextPaint = new Paint();
		blockTextPaint.setColor(Color.RED);
		blockTextPaint.setTextAlign(Paint.Align.CENTER);
		blockTextPaint.setTextSize(48);
		
		pointNodePaint = new Paint();
		pointNodePaint.setColor(Color.RED);
		pointNodePaint.setStyle(Paint.Style.STROKE);
		pointNodePaint.setStrokeWidth(14);
		pointTextPaint = new Paint();
		pointTextPaint.setColor(Color.RED);
		pointTextPaint.setTextAlign(Paint.Align.CENTER);
		pointTextPaint.setTextSize(20);
	}
	
	public void clearPoiList() {
		if (mBlockList == null) {
			mBlockList = new HashMap<String, List<GeoPoint>>();
		} else {
			mBlockList.clear();
		}
		if (mBulldozPointList == null) {
			mBulldozPointList = new ArrayList<GeoPoint>();
		} else {
			mBulldozPointList.clear();
		}
	}

	@Override
	public void onDraw(Canvas c, TileView mapView) {
		final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = mapView.getProjection();

		boolean looseCenter = false;
		GeoPoint center = mapView.getMapCenter();
		GeoPoint lefttop = pj.fromPixels(0, 0);
		double deltaX = Math.abs(center.getLongitude() - lefttop.getLongitude());
		double deltaY = Math.abs(center.getLatitude() - lefttop.getLatitude());
		
		if (mLastMapCenter == null || mLastZoom != mapView.getZoomLevel())
			looseCenter = true;
		else if(0.7 * deltaX < Math.abs(center.getLongitude() - mLastMapCenter.getLongitude()) || 0.7 * deltaY < Math.abs(center.getLatitude() - mLastMapCenter.getLatitude()))
			looseCenter = true;

		if(looseCenter){
			mLastMapCenter = center;
			mLastZoom = mapView.getZoomLevel();

			mThread.setParams(1.5 * deltaX, 1.5 * deltaY);
			mThread.run();
		}

		// 画区块
		if (this.mBlockList != null && this.mBlockList.size() > 0) {

			/*
			 * Draw in backward cycle, so the items with the least index are on
			 * the front.
			 */
			Set<String> keys = mBlockList.keySet();
			for (String key : keys) {
				List<GeoPoint> itemList = mBlockList.get(key);
				List<Point> points = new ArrayList<Point>();
				for (int i = itemList.size() - 1; i >= 0; i--) {
					GeoPoint item = itemList.get(i);
					Point curScreenCoords = new Point();
					pj.toPixels(item, curScreenCoords);

					points.add(curScreenCoords);
				}
				
				onDrawLine(c, key, points);
				
				c.save();
				c.restore();
			}
		}
		
		// 画下地口
		if (this.mBulldozPointList != null && this.mBulldozPointList.size() > 0) {
			List<Point> points = new ArrayList<Point>();
			for (int j = 0; j < this.mBulldozPointList.size(); j++) {
				GeoPoint item = this.mBulldozPointList.get(j);
				Point curScreenCoords = new Point();
				pj.toPixels(item, curScreenCoords);

				points.add(curScreenCoords);
			}
			
			onDrawPoint(c, points);
			c.save();
			c.restore();
		}
	}


	public int getMarkerAtPoint(final int eventX, final int eventY, TileView mapView){

		return NO_TAP;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event, TileView mapView) {
		final int id = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (id > NO_TAP)
//			if (onTap(id))
				return true;

		return super.onSingleTapUp(event, mapView);
	}

	@Override
	public int onLongPress(MotionEvent event, TileView mapView) {
//		final int id = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
//		mapView.mDrillMenuInfo.MarkerIndex = id;
//		mapView.mDrillMenuInfo.EventGeoPoint = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY(), mapView.getBearing());
//		if (id > NO_TAP)
//			return 1;
//			//if (onLongLongPress(id))
//
		return super.onLongPress(event, mapView);
	}
	
	public void onDrawLine(Canvas canvas,String block, List<Point> mlstPoints) {
		drawLine2(canvas, mlstPoints);
		drawNode(canvas, mlstPoints);
		drawLineText(canvas, block, mlstPoints);
	}
	
	public void onDrawPoint(Canvas canvas, List<Point> blockPoints) {
		drawPointNode(canvas, blockPoints);
		drawPointText(canvas, blockPoints);
	}

	/**
	 * 区块点
	 * 
	 * @param canvas
	 * @param mlstPoints
	 */
	public void drawNode(Canvas canvas,List<Point> mlstPoints) {
		if (mlstPoints.size() > 0) {
			float[] pts = new float[mlstPoints.size() * 2];
			int j = 0;
			for (int i = 0; i < mlstPoints.size(); i++) {
				pts[j] = mlstPoints.get(i).x;
				pts[++j] = mlstPoints.get(i).y;
				j++;
			}
			canvas.drawPoints(pts, blockNodePaint);
		}
	}

	/**
	 * 不封闭的连线
	 * 
	 * @param canvas
	 * @param mlstPoints
	 */
	public void drawLine(Canvas canvas, List<Point> mlstPoints) {
		if (mlstPoints.size() > 1) {
			float[] pts = new float[mlstPoints.size() * 4];
			int j = 0;
			for (int i = 0; i < mlstPoints.size() - 1; i++) {
				pts[j] = mlstPoints.get(i).x;
				pts[++j] = mlstPoints.get(i).y;
				pts[++j] = mlstPoints.get(i + 1).x;
				pts[++j] = mlstPoints.get(i + 1).y;
				j++;
			}
			canvas.drawLines(pts, blockLinePaint);
		}
	}
	
	/**
	 * 画点
	 * 
	 * @param canvas
	 * @param mlstPoints
	 */
	public void drawLine1(Canvas canvas,List<Point> mlstPoints) {
		if (mlstPoints.size() == 2) {
			canvas.drawLine(mlstPoints.get(0).x, mlstPoints.get(0).y, mlstPoints.get(1).x, mlstPoints.get(1).y, blockLinePaint);
		}
	}

	/**
	 * 封闭的连线
	 * 
	 * @param canvas
	 * @param mlstPoints
	 */
	public void drawLine2(Canvas canvas,List<Point> mlstPoints) {
		if (mlstPoints.size() > 1) {
			float[] pts = new float[mlstPoints.size() * 4 + 4];
			int j = 0;
			for (int i = 0; i < mlstPoints.size() - 1; i++) {
				pts[j] = mlstPoints.get(i).x;
				pts[++j] = mlstPoints.get(i).y;
				pts[++j] = mlstPoints.get(i + 1).x;
				pts[++j] = mlstPoints.get(i + 1).y;
				j++;
			}
			pts[pts.length - 2] = mlstPoints.get(mlstPoints.size() - 1).x;
			pts[pts.length - 1] = mlstPoints.get(mlstPoints.size() - 1).y;
			pts[pts.length - 4] = mlstPoints.get(0).x;
			pts[pts.length - 3] = mlstPoints.get(0).y;
			canvas.drawLines(pts, blockLinePaint);
		}
	}

	/**
	 * 区块名称
	 * 
	 * @param canvas
	 * @param block
	 * @param mlstPoints
	 */
	public void drawLineText(Canvas canvas, String block, List<Point> mlstPoints) {
		if (mlstPoints != null && mlstPoints.size() > 1) {
			canvas.drawText(block, centerX(mlstPoints), centerY(mlstPoints), blockTextPaint);
		}
	}
	
	/**
	 * 组合中心x
	 */
	private float centerX(List<Point> points) {
		float sum = 0;
		if (points != null && points.size() > 0) {
			for (Point point : points) {
				sum = sum + point.x;
			}
			return sum / points.size();
		}
		return sum;
	}
	
	/**
	 * 组合中心x
	 */
	private float centerY(List<Point> points) {
		float sum = 0;
		if (points != null && points.size() > 0) {
			for (Point point : points) {
				sum = sum + point.y;
			}
			return sum / points.size();
		}
		return sum;
	}
	
	/**
	 * 下地口名称
	 * 
	 * @param canvas
	 * @param blockPoints
	 */
	private void drawPointText(Canvas canvas, List<Point> blockPoints) {
		if (blockPoints != null && blockPoints.size() > 1) {
			for (Point point : blockPoints) {
				canvas.drawText("下地口", point.x, point.y, pointTextPaint);
			}
		}
	}

	/**
	 * 下地口点
	 * 
	 * @param canvas
	 * @param blockPoints
	 */
	private void drawPointNode(Canvas canvas, List<Point> blockPoints) {
		if (blockPoints != null && blockPoints.size() > 0) {
			float[] pts = new float[blockPoints.size() * 2];
			int j = 0;
			for (int i = 0; i < blockPoints.size(); i++) {
				pts[j] = blockPoints.get(i).x;
				pts[++j] = blockPoints.get(i).y;
				j++;
			}
			canvas.drawPoints(pts, pointNodePaint);
		}
	}

	@SuppressWarnings("hiding")
	public static interface OnItemTapListener<DrillPoint>{
		public boolean onItemTap(final int aIndex, final DrillPoint aItem);
	}

	@SuppressWarnings("hiding")
	public static interface OnItemLongPressListener<DrillPoint>{
		public boolean onItemLongPress(final int aIndex, final DrillPoint aItem);
	}

	@Override
	protected void onDrawFinished(Canvas c, TileView osmv) {
	}


	private class BulldozListThread extends Thread {
		private double mdeltaX;
		private double mdeltaY;

		public void setParams(double deltaX, double deltaY){
			mdeltaX = deltaX;
			mdeltaY = deltaY;
		}

		@Override
		public void run() {
//			mBlockList = mPointDBDao.selectBulldozTaskListNotHidden(mBlocks, mLastZoom, mLastMapCenter, mdeltaX, mdeltaY);
//			mBulldozPointList = mPointDBDao.selectBulldozPointListNotHidden(mLastZoom, mLastMapCenter, mdeltaX, mdeltaY);
			mBlockList = mPointDBDao.selectBulldozTasks(mBlocks);
			mBulldozPointList = mPointDBDao.selectAllBulldozPoint();

			super.run();
		}
	}
}