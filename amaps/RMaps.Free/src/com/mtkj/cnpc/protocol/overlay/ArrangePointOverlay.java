package com.mtkj.cnpc.protocol.overlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.robert.maps.applib.R;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

import org.andnav.osm.util.GeoPoint;

/**
 * 查排列图层
 * 
 * @author cw
 *
 */
public class ArrangePointOverlay extends TileViewOverlay {
	private Context mCtx;
	private PointDBDao mPointDBDao;
	private int mTapId;
	private GeoPoint mLastMapCenter;
	private int mLastZoom;
	private PoiListThread mThread;
	private RelativeLayout mT;
	private boolean mNeedUpdateList = false;
	public static int NO_TAP = -9999;
	public Bitmap mBitmap_press;

	protected OnItemTapListener<ArrangePoint> mOnItemTapListener;
	protected OnItemLongPressListener<ArrangePoint> mOnItemLongPressListener;
	protected SparseArray<ArrangePoint> mItemList;
	protected final Point mMarkerHotSpot;
	protected final int mMarkerWidth, mMarkerHeight;
	private boolean mCanUpdateList = true;
	protected SparseArray<Drawable> mBtnMap;

	public int getTapIndex() {
		return mTapId;
	}

	public void setTapIndex(int mTapIndex) {
		this.mTapId = mTapIndex;
	}

	public void UpdateList() {
		mNeedUpdateList = true;
	}

	public ArrangePointOverlay(Context ctx, PointDBDao pointDBDao,
                               OnItemTapListener<ArrangePoint> onItemTapListener, boolean hidepoi)
	{
		mCtx = ctx;
		mPointDBDao = pointDBDao;
		mCanUpdateList = !hidepoi;
		mTapId = NO_TAP;

		Drawable marker = ctx.getResources().getDrawable(R.drawable.point_undone);
		this.mMarkerWidth = marker.getIntrinsicWidth();
		this.mMarkerHeight = marker.getIntrinsicHeight();

		mBtnMap = new SparseArray<Drawable>();
		mBtnMap.put(Integer.valueOf(R.drawable.poi), marker);
		this.mMarkerHotSpot = new Point(0, mMarkerHeight);

        this.mOnItemTapListener = onItemTapListener;

        mLastMapCenter = null;
        mLastZoom = -1;
        mThread = new PoiListThread();// 获取范围内兴趣点

		this.mT = (RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.poi_descr, null);
		this.mT.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		this.mBitmap_press = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.poi_marker_pressed);
	}

	public void clearPoiList() {
		if(mItemList == null)
			mItemList = new SparseArray<ArrangePoint>();
		else
			mItemList.clear();
	}

	@Override
	public void onDraw(Canvas c, TileView mapView) {
		final TileView.OpenStreetMapViewProjection pj = mapView.getProjection();
		final Point curScreenCoords = new Point();

		if (mCanUpdateList){
			boolean looseCenter = false;
			GeoPoint center = mapView.getMapCenter();
			GeoPoint lefttop = pj.fromPixels(0, 0);
			double deltaX = Math.abs(center.getLongitude() - lefttop.getLongitude());
			double deltaY = Math.abs(center.getLatitude() - lefttop.getLatitude());

			if (mLastMapCenter == null || mLastZoom != mapView.getZoomLevel())
				looseCenter = true;
			else if(0.7 * deltaX < Math.abs(center.getLongitude() - mLastMapCenter.getLongitude()) || 0.7 * deltaY < Math.abs(center.getLatitude() - mLastMapCenter.getLatitude()))
				looseCenter = true;

			if(looseCenter || mNeedUpdateList){
				mLastMapCenter = center;
				mLastZoom = mapView.getZoomLevel();
				mNeedUpdateList = false;

				mThread.setParams(1.5 * deltaX, 1.5 * deltaY);
				mThread.run();
			}
		}

		if (this.mItemList != null) {

			/*
			 * Draw in backward cycle, so the items with the least index are on
			 * the front.
			 */
			int i = 0;
			while (i < mItemList.size()) {
				if (mLastZoom > 15) {
					ArrangePoint item = this.mItemList.valueAt(i);
					if (item.getId() != mTapId) {
						pj.toPixels(item.geoPoint, curScreenCoords);

						c.save();
						c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);

						onDrawItem(c, item, curScreenCoords);

						c.restore();
					}
					i = i + 1;
				} else if (mLastZoom > 13) {
					ArrangePoint item = this.mItemList.valueAt(i);
					if (item.getId() != mTapId) {
						pj.toPixels(item.geoPoint, curScreenCoords);

						c.save();
						c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);

						onDrawItem(c, item, curScreenCoords);

						c.restore();
					}
					i = i + (18 - mLastZoom) * 4;
				} else {
					ArrangePoint item = this.mItemList.valueAt(i);
					if (item.getId() != mTapId) {
						pj.toPixels(item.geoPoint, curScreenCoords);

						c.save();
						c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);

						onDrawItem(c, item, curScreenCoords);

						c.restore();
					}
					i = i + (18 - mLastZoom) * 10;
				}
			}

			if (mTapId > NO_TAP) {
				ArrangePoint item = this.mItemList.get(mTapId);
				if(item != null) {
					pj.toPixels(item.geoPoint, curScreenCoords);

					c.save();
					c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);

					onDrawItem(c, item, curScreenCoords);
					c.drawBitmap(mBitmap_press, curScreenCoords.x - (int)(mBitmap_press.getWidth() / 2), curScreenCoords.y - (int)(mBitmap_press.getHeight()), new Paint());
					c.restore();
				}
			}
		}
	}

	protected void onDrawItem(Canvas c, final ArrangePoint focusedItem, Point screenCoords) {

		final int left = screenCoords.x - (int) this.mMarkerWidth / 3;
		final int right = screenCoords.x + (int) this.mMarkerWidth / 3;
		final int top = screenCoords.y - (int) this.mMarkerHeight / 3;
		final int bottom = screenCoords.y + (int) this.mMarkerHeight / 3;

		Drawable marker = null;
		if(focusedItem.isDone) {
			marker = mCtx.getResources().getDrawable(R.drawable.point_isdone);
			mBtnMap.put(R.drawable.point_isdone, marker);
		} else {
			marker = mCtx.getResources().getDrawable(R.drawable.point_undone);
			mBtnMap.put(R.drawable.point_undone, marker);
		}
		if (mLastZoom > 11) {
			int level = 3 * (20 - mLastZoom);
			marker.setBounds(left , top, right - level, bottom - level);

			marker.draw(c);
		} else {
			marker.setBounds(left , top, right- 10, bottom - 10);

			marker.draw(c);
		}

		Paint p = new Paint();
		p.setColor(Color.GREEN);
		p.setStrokeWidth(6);
		p.setFakeBoldText(true);
		p.setTextSize(35);
		if (mLastZoom > 13 && mLastZoom <= 17 ) {
			c.drawText(focusedItem.lineNo, screenCoords.x + 3 * (mLastZoom - 16), screenCoords.y + 3 * (mLastZoom - 16), p);
		} else if (mLastZoom > 17) {
			c.drawText(focusedItem.stationNo, screenCoords.x + 3 * (mLastZoom - 16), screenCoords.y + 3 * (mLastZoom - 16), p);
		}
			// 画 叉
//		if(OpenStreetMapViewConstants.DEBUGMODE){
//			final int pxUp = 2;
//			final int left2 = (int)(screenCoords.x + mDensity*(5 - pxUp));
//			final int right2 = (int)(screenCoords.x + mDensity*(38 + pxUp));
//			final int top2 = (int)(screenCoords.y - this.mMarkerHotSpot.y - mDensity*(pxUp));
//			final int bottom2 = (int)(top2 + mDensity*(33 + pxUp));
//			Paint p = new Paint();
//			c.drawLine(left2, top2, right2, bottom2, p);
//			c.drawLine(right2, top2, left2, bottom2, p);
//
//			c.drawLine(screenCoords.x - 5, screenCoords.y - 5, screenCoords.x + 5, screenCoords.y + 5, p);
//			c.drawLine(screenCoords.x - 5, screenCoords.y + 5, screenCoords.x + 5, screenCoords.y - 5, p);
//		}
	}

	public ArrangePoint getArrangePoint(final int id){
		return this.mItemList.get(id);
	}

	public int getMarkerAtPoint(final int eventX, final int eventY, TileView mapView){
		if(this.mItemList != null){
			final TileView.OpenStreetMapViewProjection pj = mapView.getProjection();

			final Rect curMarkerBounds = new Rect();
			final Point mCurScreenCoords = new Point();

			 
			for(int i = 0; i < this.mItemList.size(); i++){
				final ArrangePoint mItem = this.mItemList.valueAt(i);
				pj.toPixels(mItem.geoPoint, mapView.getBearing(), mCurScreenCoords);

				final int left = (int)(mCurScreenCoords.x - (int) this.mMarkerWidth / 2);
				final int right = (int)(mCurScreenCoords.x + (int) this.mMarkerWidth / 2);
				final int top = (int)(mCurScreenCoords.y - (int) this.mMarkerHeight / 2);
				final int bottom = (int)(mCurScreenCoords.y + (int) this.mMarkerHeight / 2);

				curMarkerBounds.set(left, top, right, bottom);
				if(curMarkerBounds.contains(eventX, eventY))
					return mItem.getId();
			}
		}

		return NO_TAP;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event, TileView mapView) {
		final int id = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (id > NO_TAP) {
			mTapId = id;
			
			mapView.mArrangeMenuInfo.MarkerIndex = id;
			mapView.mArrangeMenuInfo.EventGeoPoint = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY(), mapView.getBearing());
			mapView.showContextMenu();
			
			return true;
		}

		return super.onSingleTapUp(event, mapView);
	}

	@Override
	public int onLongPress(MotionEvent event, TileView mapView) {
		final int id = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (id > NO_TAP) {
			mTapId = id;
			mapView.mArrangeMenuInfo.MarkerIndex = id;
			mapView.mArrangeMenuInfo.EventGeoPoint = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY(), mapView.getBearing());
			return 1;
		} else {
			mTapId = NO_TAP;
		}

		return super.onLongPress(event, mapView);
	}

	protected boolean onTap(int id) {
		if(mTapId == id)
			mTapId = NO_TAP;
		else
			mTapId = id;

		if(this.mOnItemTapListener != null)
			return this.mOnItemTapListener.onItemTap(id, this.mItemList.get(id));
		else
			return false;
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


	private class PoiListThread extends Thread {
		private double mdeltaX;
		private double mdeltaY;

		public void setParams(double deltaX, double deltaY){
			mdeltaX = deltaX;
			mdeltaY = deltaY;
		}

		@Override
		public void run() {
			mItemList = mPointDBDao.selectArrangeListNotHidden(mLastZoom, mLastMapCenter, mdeltaX, mdeltaY);

			super.run();
		}
	}
}