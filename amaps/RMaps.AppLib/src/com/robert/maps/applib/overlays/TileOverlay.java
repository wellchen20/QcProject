package com.robert.maps.applib.overlays;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.MyMath;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.Util;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import com.robert.maps.applib.tileprovider.MessageHandlerConstants;
import com.robert.maps.applib.tileprovider.TileSource;
import com.robert.maps.applib.utils.Ut;
import com.robert.maps.applib.view.IMoveListener;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

/**
 * 地图地图图层
 * 
 * @author DRH
 *
 */
public class TileOverlay extends TileViewOverlay implements OpenStreetMapConstants, OpenStreetMapViewConstants {
	private TileSource mTileSource;
	private double mOffsetLat, mOffsetLon;
	final Matrix mMatrixBearing = new Matrix();
	final Rect mRectDraw = new Rect();
	final Paint mPaint = new Paint();
	private TileMapHandler mTileMapHandler = new TileMapHandler();
	private TileView mTileView;
	private boolean mAsOverlay;
	private IMoveListener mMoveListener;
	
	public TileOverlay(TileView tileView, boolean asOverlay) {
		super();
		mPaint.setFilterBitmap(true);
		mPaint.setAntiAlias(true);
		
		mTileView = tileView;
		mAsOverlay = asOverlay;
		mMoveListener = null;
	}
	
	public void setTileSource(TileSource tileSource) {
		if(mTileSource != null)
			mTileSource.Free();
		
		mTileSource = tileSource;
		mTileSource.setHandler(mTileMapHandler);
		mOffsetLat = mTileSource.OFFSET_LAT;
		mOffsetLon = mTileSource.OFFSET_LON;
	}
	
	public TileSource getTileSource() {
		return mTileSource;
	}
	
	public void setMoveListener(IMoveListener moveListener) {
		mMoveListener = moveListener;
	}
	
	public void setOffset(double lat, double lon) {
		mOffsetLat = lat; 
		mOffsetLon = lon;
	}

	@Override
	public void Free() {
		if(mTileSource != null)
			mTileSource.Free();
		super.Free();
	}

	@Override
	protected void onDraw(Canvas c, TileView tileView) {
		if (mTileSource != null) {
			final int tileSizePxNotScale = mTileSource.getTileSizePx(tileView.getZoomLevel());
			final int tileSizePx = (int) (tileSizePxNotScale * tileView.mTouchScale);//根据当前比例尺获取像素点
			final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(tileView.mLatitudeE6 + (int)(1E6 * mOffsetLat), tileView.mLongitudeE6 + (int)(1E6 * mOffsetLon), tileView.getZoomLevel(), null, mTileSource.PROJECTION);
			
			final Point upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(tileView, 
					centerMapTileCoords, tileSizePx, mOffsetLat, mOffsetLon, null);
			final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile.x;
			final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile.y;

			final int mapTileUpperBound = mTileSource.getTileUpperBound(tileView.getZoomLevel());
			
			final int[] mapTileCoords = new int[] {
					centerMapTileCoords[LATITUDE],
					centerMapTileCoords[LONGITUDE] };
			
			boolean tileIn = true;
			int x = 0, y = 0, radius = 0, tilecnt = 0;
			mMatrixBearing.reset();
			mMatrixBearing.setRotate(360 - tileView.getBearing(), tileView.getWidth() / 2, tileView.getHeight() / 2);
			int a = 0;
			int b = 0;
			while (tileIn) {
				tileIn = false;
				
				for(x = -radius; x <= radius; x++) {
					for(y = -radius; y <= radius; y++) {
						if(x != -radius && x != radius && y != -radius && y != radius) continue;
						
						mapTileCoords[LATITUDE] = MyMath.mod(centerMapTileCoords[LATITUDE] + y, mapTileUpperBound);
						mapTileCoords[LONGITUDE] = MyMath.mod(centerMapTileCoords[LONGITUDE] + x, mapTileUpperBound);

						final int tileLeft = centerMapTileScreenLeft + (x * tileSizePx);
						final int tileTop = centerMapTileScreenTop + (y * tileSizePx);
						
						mRectDraw.set(tileLeft, tileTop, tileLeft + tileSizePx, tileTop + tileSizePx);

						float arr[] = {mRectDraw.left, mRectDraw.top, mRectDraw.right, mRectDraw.top, mRectDraw.right, mRectDraw.bottom, mRectDraw.left, mRectDraw.bottom, mRectDraw.left, mRectDraw.top};
						mMatrixBearing.mapPoints(arr);
						
//						String s = new StringBuffer().append("left: ").append(mRectDraw.left).append(" top: ").append(mRectDraw.top)
//								.append(" right: ").append(mRectDraw.right).append(" bottom: ").append(mRectDraw.bottom).toString();
//						System.out.println("x:" + x + " y:" + y + "  " + b++ + "  " + s + "  tileView.mTouchScale " + tileView.mTouchScale);
						
						// 判断是否在范围内
						if(Ut.Algorithm.isIntersected((int)(tileView.getWidth()*(1-tileView.mTouchScale)/2), (int)(tileView.getHeight()*(1-tileView.mTouchScale)/2), (int)(tileView.getWidth()*(1+tileView.mTouchScale)/2), (int)(tileView.getHeight()*(1+tileView.mTouchScale)/2), arr)) {
							tileIn = true;
							tilecnt++;
							
//							String string = new StringBuffer().append("left: ").append(mRectDraw.left).append(" top: ").append(mRectDraw.top)
//									.append(" right: ").append(mRectDraw.right).append(" bottom: ").append(mRectDraw.bottom).toString();
//							System.out.println(a++ + "" +string + "  tileView.mTouchScale " + tileView.mTouchScale);
							
							final Bitmap currentMapTile = this.mTileSource.getTile(mapTileCoords[LONGITUDE], mapTileCoords[LATITUDE], tileView.getZoomLevel());
							if (currentMapTile != null) {
								if (!currentMapTile.isRecycled())
									// 地图加载图片
									c.drawBitmap(currentMapTile, null, mRectDraw, mPaint);

								
								// 画加载地图瓦片格子和描述
//								if (tileView.mDrawTileGrid || OpenStreetMapViewConstants.DEBUGMODE) {
//									c.drawLine(tileLeft, tileTop, tileLeft + tileSizePx, tileTop, mPaint);
//									c.drawLine(tileLeft, tileTop, tileLeft, tileTop + tileSizePx, mPaint);
//									c.drawText("y x = " + mapTileCoords[LATITUDE] + " " + mapTileCoords[LONGITUDE] + " zoom " + tileView.getZoomLevel() + " ", tileLeft + 5,
//											tileTop + 15, mPaint);
//								}

							}
						}
					}
				}
				
				radius++;
			}
			
			mTileSource.getTileProvider().ResizeCashe(tilecnt);
			mTileSource.setReloadTileMode(false);
		}
	}

	@Override
	protected void onDrawFinished(Canvas c, TileView tileView) {
	}

	private static final int LATITUDE = 0;
	private static final int LONGITUDE = 1;

	public Point getUpperLeftCornerOfCenterMapTileInScreen(TileView tileView, final int[] centerMapTileCoords, final int tileSizePx, final Point reuse) {
		return getUpperLeftCornerOfCenterMapTileInScreen(tileView, centerMapTileCoords, tileSizePx, 0, 0, reuse);
	}

	/**
	 * 得到中心地图平铺在屏幕的左上角
	 * 
	 * @param tileView
	 * @param centerMapTileCoords
	 * @param tileSizePx
	 * @param offsetLat
	 * @param offsetLon
	 * @param reuse
	 * @return
	 */
	public Point getUpperLeftCornerOfCenterMapTileInScreen(TileView tileView, final int[] centerMapTileCoords, final int tileSizePx, final double offsetLat, final double offsetLon, final Point reuse) {
		final Point out = (reuse != null) ? reuse : new Point();

		final int viewWidth = tileView.getWidth();
		final int viewWidth_2 = viewWidth / 2;
		final int viewHeight = tileView.getHeight();
		final int viewHeight_2 = viewHeight / 2;

		final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(centerMapTileCoords,
				tileView.getZoomLevel(), mTileSource.PROJECTION);
		final float[] relativePositionInCenterMapTile = bb
				.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
						tileView.mLatitudeE6 + (int)(1E6 * offsetLat), tileView.mLongitudeE6 + (int)(1E6 * offsetLon), null);

		final int centerMapTileScreenLeft = viewWidth_2
				- (int) (0.5f + (relativePositionInCenterMapTile[LONGITUDE] * tileSizePx));
		final int centerMapTileScreenTop = viewHeight_2
				- (int) (0.5f + (relativePositionInCenterMapTile[LATITUDE] * tileSizePx));

		out.set(centerMapTileScreenLeft, centerMapTileScreenTop);
		return out;
	}



	private class TileMapHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case MessageHandlerConstants.MAPTILEFSLOADER_SUCCESS_ID:
				mTileView.invalidate();
				break;
			case MessageHandlerConstants.MAPTILEFSLOADER_INDEXIND_SUCCESS_ID:
				mTileSource.postIndex();
				
				if(!mAsOverlay) {
					mTileView.setZoomLevel(mTileView.getZoomLevel());
					if(mMoveListener != null)
						mMoveListener.onZoomDetected();
				}
				break;
			}
		}
	}

}
