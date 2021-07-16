package com.robert.maps.applib.view;

import java.util.List;

import org.andnav.osm.util.GeoPoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.robert.maps.applib.R;
import com.robert.maps.applib.tileprovider.TileSource;
import com.robert.maps.applib.utils.ScaleBarDrawable;

public class MapView extends RelativeLayout {
	/** 是否引导中 **/
	public static boolean isGuiding = false;
	
	public static final int ZOOM_CONTROL_HIDE = 0;
	public static final int ZOOM_CONTROL_TOP = 1;
	public static final int ZOOM_CONTROL_BOTTOM = 2;
	public static final String MAPNAME = "MapName";
	
	private final TileView mTileView;
	private final MapController mController;
	private IMoveListener mMoveListener;
	private boolean mStopBearing = false;
	private boolean mUseVolumeControl;
	private ScaleBarDrawable mScaleBarDrawable;

	public MapView(Context context, int sideInOutButtons, int scaleBarVisible) {
		super(context);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		mUseVolumeControl = pref.getBoolean("pref_use_volume_controls", true);
		mController = new MapController();
		mTileView = new TileView(context);
		mMoveListener = null;
		addView(mTileView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		if (scaleBarVisible == 1) {
	        final ImageView ivScaleBar = new ImageView(getContext());
			mScaleBarDrawable = new ScaleBarDrawable(context, this, Integer.parseInt(pref.getString("pref_units","0")));
			ivScaleBar.setImageDrawable(mScaleBarDrawable);
			ivScaleBar.setId(R.id.scale_bar);
			final RelativeLayout.LayoutParams scaleParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if(sideInOutButtons == 1)
				scaleParams.addRule(RelativeLayout.RIGHT_OF, R.id.whatsnew);
			else
				scaleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			scaleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			addView(ivScaleBar, scaleParams);
		}

		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	public MapView(Context context) {
		this(context, 1, 1);

	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MapView);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		mController = new MapController();
		mTileView = new TileView(context);
		addView(mTileView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		if (a.getInt(R.styleable.MapView_SideInOutButtons, 0) == 1) {
	        final ImageView ivScaleBar = new ImageView(getContext());
			mScaleBarDrawable = new ScaleBarDrawable(context, this, Integer.parseInt(pref.getString("pref_units","0")));
			ivScaleBar.setImageDrawable(mScaleBarDrawable);
			final RelativeLayout.LayoutParams scaleParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			scaleParams.addRule(RelativeLayout.RIGHT_OF, R.id.whatsnew);
			scaleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			addView(ivScaleBar, scaleParams);
		}
		if(a.getBoolean(R.styleable.MapView_DisableControl, false))
			mTileView.setDisableControl(true);
		

        a.recycle();
	}
	
	public TileView getTileView() {
		return mTileView;
	}

	public class MapController {
		public void setCenter(GeoPoint point) {
			mTileView.setMapCenter(point);
		}
		
		public void setZoom(int zoom) {
			mTileView.setZoomLevel(zoom);
		}
		
		public void zoomOut() {
			mTileView.setZoomLevel(mTileView.getZoomLevel() - 1);
		}

		public void zoomIn() {
			mTileView.setZoomLevel(mTileView.getZoomLevel() + 1);
		}
}
	
	public MapController getController() {
		return mController;
	}
	
	public void setTileSource(TileSource tilesource) {
		mTileView.setTileSource(tilesource);
		if(mScaleBarDrawable != null && tilesource != null)
			mScaleBarDrawable.correctScale(tilesource.MAPTILE_SIZE_FACTOR, tilesource.GOOGLESCALE_SIZE_FACTOR);
	}
	
	public TileSource getTileSource() {
		return mTileView.getTileSource();
	}
	
	public int getZoomLevel() {
		return mTileView.getZoomLevel();
	}
	
	public double getZoomLevelScaled() {
		return mTileView.getZoomLevelScaled();
	}
	
	public GeoPoint getMapCenter() {
		return mTileView.getMapCenter();
	}
	
	public List<TileViewOverlay> getOverlays() {
		return mTileView.getOverlays();
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		if (mTileView.mDrillMenuInfo.EventGeoPoint != null) {
			return mTileView.mDrillMenuInfo;
		} else if (mTileView.mShotMenuInfo.EventGeoPoint != null) {
			return mTileView.mShotMenuInfo;
		} else if (mTileView.mPoiMenuInfo.EventGeoPoint != null) {
			return mTileView.mPoiMenuInfo;
		}else if (mTileView.mArrangeMenuInfo.EventGeoPoint != null){
			return mTileView.mArrangeMenuInfo;
		}else {
			return mTileView.mPoiMenuInfo;
		}
	}

	public void setBearing(float bearing) {
		if(!mStopBearing)
			mTileView.setBearing(bearing);
	}

	public void setMoveListener(IMoveListener moveListener) {
		mMoveListener = moveListener;
		mTileView.setMoveListener(moveListener);
	}

	public double getTouchScale() {
		return mTileView.mTouchScale;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean stopPropagation = false;
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if(mUseVolumeControl) {
					stopPropagation = true;
					getController().zoomOut();
				}
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:
				if(mUseVolumeControl) {
					stopPropagation = true;
					getController().zoomIn();
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				stopPropagation = true;
				getController().zoomOut();
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				stopPropagation = true;
				getController().zoomIn();
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				if(mStopBearing) 
					mStopBearing = false;
				else {
					setBearing(0);
					mStopBearing = true;
				}
				stopPropagation = true;
				break;
			default:
				break;
		}
		
	    if(stopPropagation) return true;
	    
	    return super.onKeyDown(keyCode, event);
	}
	
	public void Refresh() {
		mTileView.invalidate();
	}
}
