package com.robert.maps.applib.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.robert.maps.applib.R;
import com.robert.maps.applib.utils.CoordFormatter;
import com.robert.maps.applib.utils.DistanceFormatter;
import com.robert.maps.applib.utils.SimpleThreadFactory;
import com.robert.maps.applib.view.MapView;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

/**
 * 搜索展示图层
 * 
 * @author DRH
 *
 */
public class SearchResultOverlay extends TileViewOverlay {
	
	// 搜索结果显示
	public static List<GeoPoint> mSearchGeoPoints = new ArrayList<GeoPoint>();
	public static List<String> mSearchDescrs = new ArrayList<String>();
	private Bitmap mBitmap_press;
	private Bitmap mBitmap_unpress;
	
	protected final Paint mPaint = new Paint();
	protected final Paint mPaintLine;
	protected GeoPoint mLocation;
	protected GeoPoint mCurrLocation;
	protected String mDescr;
	private TextView mT;
	private CoordFormatter mCf;
	private DistanceFormatter mDf;
	//private RequestQueue mRequestQueue;
	private double mElevation;
	private MapView mMapView;
	private boolean mSearchBubble;
	
	private ExecutorService mThreadPool = Executors.newFixedThreadPool(2, new SimpleThreadFactory("SearchResultOverlay"));
	
	private final String LAT;
	private final String LON;
	private final String ALT;
	private final String DIST;
	private final String AZIMUT;
	
	public SearchResultOverlay(final Context ctx, MapView mapView) {
		this.mDescr = "";
		this.mPaint.setAntiAlias(true);
		this.mBitmap_press = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.poi_marker_pressed);
		this.mBitmap_unpress = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.poi_marker);
		this.mT = (TextView) LayoutInflater.from(ctx).inflate(R.layout.search_bubble, null); //new Button(ctx);
		this.mT.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mCf = new CoordFormatter(ctx);
		mDf = new DistanceFormatter(ctx);
		//mRequestQueue = Volley.newRequestQueue(ctx);
		mMapView = mapView;
	
		mPaintLine = new Paint();
		mPaintLine.setAntiAlias(true);
		mPaintLine.setStrokeWidth(2);
		mPaintLine.setStyle(Paint.Style.STROKE);
		mPaintLine.setColor(ctx.getResources().getColor(R.color.line_to_gps));
		
		LAT = ctx.getResources().getString(R.string.PoiLat);
		LON = ctx.getResources().getString(R.string.PoiLon);
		ALT = ctx.getResources().getString(R.string.PoiAlt);
		DIST = ctx.getResources().getString(R.string.dist);
		AZIMUT = ctx.getResources().getString(R.string.azimuth);
	}

	@Override
	public void Free() {
		mThreadPool.shutdown();
		super.Free();
	}

	public void setLocation(final Location loc){
		this.mCurrLocation = TypeConverter.locationToGeoPoint(loc);
		if(!mSearchBubble)
			setDescr();
	}

	public void setLocation(final GeoPoint geopoint, final String aDescr){
		this.mLocation = geopoint;
		this.mDescr = aDescr;
		mSearchBubble = true;
	}
	
	public static void setSearchResult(final List<GeoPoint> geoPoints, final List<String > strings){
		mSearchGeoPoints = geoPoints;
		mSearchDescrs = strings;
	}

	public void Clear(){
		if (this.mLocation != null) {
			this.mLocation = null;
		}
		this.mDescr = "";
	}

	@Override
	protected void onDraw(Canvas c, TileView osmv) {
		if(this.mLocation != null){
//			mT.setText(mDescr);
//			mT.measure(0, 0);
//			mT.layout(0, 0, mT.getMeasuredWidth(), mT.getMeasuredHeight());

			final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = osmv.getProjection();
			final Point screenCoords = new Point();
			pj.toPixels(this.mLocation, screenCoords);

//			if(!mSearchBubble && mCurrLocation != null) {
//				final Point currLocCoords = new Point();
//				pj.toPixels(this.mCurrLocation, currLocCoords);
//				c.drawLine(currLocCoords.x, currLocCoords.y, screenCoords.x, screenCoords.y, mPaintLine);
//			}

			c.drawBitmap(mBitmap_press, screenCoords.x - (int)(mBitmap_press.getWidth() / 2), screenCoords.y - (int)(mBitmap_press.getHeight()), mPaint);
			
			c.save();
			c.rotate(osmv.getBearing(), screenCoords.x, screenCoords.y);
			c.translate(screenCoords.x - mT.getMeasuredWidth() / 2, screenCoords.y - mT.getMeasuredHeight() + 2);
//			mT.draw(c);
			
			c.restore();
		}
		if (this.mSearchGeoPoints != null && this.mSearchGeoPoints.size() > 0) {
			for (int i = 0; i < this.mSearchGeoPoints.size(); i++) {
				final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = osmv.getProjection();
				final Point screenCoords = new Point();
				pj.toPixels(this.mSearchGeoPoints.get(i), screenCoords);
				
				if (isSelected == i) {
					c.drawBitmap(mBitmap_press, screenCoords.x - (int)(mBitmap_press.getWidth() / 2), screenCoords.y - (int)(mBitmap_press.getHeight()), mPaint);
					
					c.save();
					c.rotate(osmv.getBearing(), screenCoords.x, screenCoords.y);
					c.translate(screenCoords.x - mT.getMeasuredWidth() / 2, screenCoords.y - mT.getMeasuredHeight() - mBitmap_press.getHeight() / 2);
				} else {
					c.drawBitmap(mBitmap_unpress, screenCoords.x - (int)(mBitmap_unpress.getWidth() / 2), screenCoords.y - (int)(mBitmap_unpress.getHeight()), mPaint);
					
					c.save();
					c.rotate(osmv.getBearing(), screenCoords.x, screenCoords.y);
					c.translate(screenCoords.x - mT.getMeasuredWidth() / 2, screenCoords.y - mT.getMeasuredHeight() - mBitmap_unpress.getHeight() / 2);
				}
				
				c.restore();
				
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event,
			TileView mapView) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			if (mLocation != null || (this.mSearchGeoPoints != null && this.mSearchGeoPoints.size() > 0)) {
				
				Clear();
				this.mSearchGeoPoints.clear();
				this.mSearchDescrs.clear();
				
				mapView.invalidate();
				return false;
		}
		return super.onKeyDown(keyCode, event, mapView);
	}
	
	private int isSelected = 0;
	
	@Override
	public boolean onSingleTapUp(MotionEvent e, TileView mapView) {
//		if (mLocation != null) {
//			final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = mapView.getProjection();
//			final Point screenCoords = new Point();
//			pj.toPixels(this.mLocation, mapView.getBearing(), screenCoords);
//			final Rect rect = new Rect(screenCoords.x - mT.getMeasuredWidth() / 2, screenCoords.y - mT.getMeasuredHeight() + 5, screenCoords.x + mT.getMeasuredWidth() / 2, screenCoords.y - 20);
//			
//			if(rect.contains((int)e.getX(), (int)e.getY())) {
//				mapView.mPoiMenuInfo.EventGeoPoint = mLocation;
//				mapView.mPoiMenuInfo.MarkerIndex = PoiOverlay.NO_TAP;
//				mapView.mPoiMenuInfo.Elevation = mElevation;
//				mapView.showContextMenu();
//			}
//			
//			mLocation = null;
//			mapView.invalidate();
//			return true;
//		} else 
			if (this.mSearchGeoPoints != null && this.mSearchGeoPoints.size() > 0) {
			for (int i = 0; i < mSearchGeoPoints.size(); i++) {
				final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = mapView.getProjection();
				final Point screenCoords = new Point();
				pj.toPixels(this.mSearchGeoPoints.get(i), mapView.getBearing(), screenCoords);
//				final Rect rect = new Rect(screenCoords.x - mT.getMeasuredWidth() / 2, screenCoords.y - mT.getMeasuredHeight() + 5, screenCoords.x + mT.getMeasuredWidth() / 2, screenCoords.y - 20);
				final Rect rect = new Rect(screenCoords.x - mBitmap_unpress.getWidth() / 2, screenCoords.y - mBitmap_unpress.getHeight() + 5, screenCoords.x + mBitmap_unpress.getWidth() / 2, screenCoords.y + mBitmap_unpress.getHeight() / 2);
				if(rect.contains((int)e.getX(), (int)e.getY())) {
//					if (isSelected == i) {
//						mapView.mPoiMenuInfo.EventGeoPoint = this.mSearchGeoPoints.get(i);
//						mapView.mPoiMenuInfo.MarkerIndex = PoiOverlay.NO_TAP;
//						mapView.mPoiMenuInfo.Elevation = mElevation;
//						mapView.showContextMenu();
//						
//					} else {
//						isSelected = i;
//						mapView.setMapCenter(this.mSearchGeoPoints.get(i));
//					}
					
					isSelected = i;
					
					mapView.mPoiMenuInfo.EventGeoPoint = this.mSearchGeoPoints.get(i);
					mapView.mPoiMenuInfo.MarkerIndex = PoiOverlay.NO_TAP;
					mapView.mPoiMenuInfo.Elevation = mElevation;
					mapView.mPoiMenuInfo.mIndex = i;
					mapView.showContextMenu();
					
					mapView.setMapCenter(this.mSearchGeoPoints.get(i));
					
					Clear();
					break;
				}
			}
			
			mapView.invalidate();
			return true;
		}

		return super.onSingleTapUp(e, mapView);
	}

	@Override
	public int onLongPress(MotionEvent event, TileView mapView) {
		mLocation = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY(), mapView.getBearing());
		
		mapView.mPoiMenuInfo.EventGeoPoint = mLocation;
		mapView.mPoiMenuInfo.MarkerIndex = PoiOverlay.NO_TAP;
		mapView.mPoiMenuInfo.mIndex = -1;
		
		isSelected = -1;
		
//		setDescr();
		mMapView.Refresh();
		
		return super.onLongPress(event, mapView);
	}
	
	private void setDescr() {
		if(mLocation != null)
			mDescr = new StringBuilder()
			.append(LAT).append(": ")
			.append(mCf.convertLat(mLocation.getLatitude()))
			.append("\n").append(LON).append(": ")
			.append(mCf.convertLon(mLocation.getLongitude()))
			.append("\n").append(ALT).append(": ")
			.append(mElevation == 0.0 ? "n/a" : mDf.formatElevation(mElevation))
			.append(mCurrLocation == null ? "" : String.format(Locale.UK, "\n%s: %.1f°", AZIMUT, mCurrLocation.bearingTo360(mLocation))+"\n"+DIST+": "+mDf.formatDistance(mCurrLocation.distanceTo(mLocation)))
			.toString();				
	}

	@Override
	protected void onDrawFinished(Canvas c, TileView osmv) {

	}

	public void fromPref(SharedPreferences settings) {
		final String strlocation = settings.getString("SearchResultLocation", "");
		if(strlocation.length() > 0){
			mLocation = GeoPoint.fromDoubleString(strlocation);
			mDescr = settings.getString("SearchResultDescr", "");
		}
	}
	
	public void toPref(SharedPreferences.Editor editor){
		if(mLocation != null && mSearchBubble){
			editor.putString("SearchResultDescr", mDescr);
			editor.putString("SearchResultLocation", mLocation.toDoubleString());
		}else{
			editor.putString("SearchResultDescr", "");
			editor.putString("SearchResultLocation", "");
		}
	}

}
