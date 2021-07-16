package com.mtkj.cnpc.protocol.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mtkj.utils.entity.SharePoint;
import com.robert.maps.applib.R;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * 位置分享图层
 *
 * @author CW
 *
 */
public class SharePosOverlay extends TileViewOverlay {

	private Paint mPaint = new Paint();
	private Paint mPaintText = new Paint();
	private Bitmap mBitmap_press;
	private Bitmap mPerson;
	private int mPressWidth,mPressHeight,mPersonWidth,mPersonHeight;
	private SharePoint sharePoint;
	private List<SharePoint> pointArrayList = new ArrayList<>();
	private LinearLayout msgbox = null;

	public SharePosOverlay(Context ctx, View bottomView){
		mPaint.setAntiAlias(true);
		mPaintText.setColor(Color.GREEN);
		mPaintText.setTextSize(ctx.getResources().getDimensionPixelSize(R.dimen.measuretool_label_size));
		mPaintText.setAntiAlias(true);
		this.mBitmap_press = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.point_online);
		this.mPerson = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.person_share);
		mPressWidth = mBitmap_press.getWidth();
		mPressHeight = mBitmap_press.getHeight();
		mPersonWidth = mPerson.getWidth();
		mPersonHeight = mPerson.getHeight();
		msgbox = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.measure_info_box, (ViewGroup) bottomView);
		msgbox.setVisibility(View.VISIBLE);
		((TextView) msgbox.findViewById(R.id.value)).setText("位置共享中...");
	}
	@Override
	protected void onDraw(Canvas c, TileView tileView) {
		/*final com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = tileView.getProjection();
		if (sharePoint!=null){
			Point p1 = pj.toPixels(sharePoint.Point,null);
			c.drawBitmap(mBitmap_press,p1.x - (int)(mPressWidth/2), p1.y - (int)(mPressHeight / 2), mPaint);
			c.drawBitmap(mPerson,p1.x - (int)(mPersonWidth/2), p1.y - (int)(mPersonHeight+mPressHeight)/3*2, mPaint);
			c.drawText(sharePoint.Name,p1.x+(int)(mPersonWidth/2),p1.y - (int)(mPersonHeight/2),mPaintText);
		}*/
		com.robert.maps.applib.view.TileView.OpenStreetMapViewProjection pj = tileView.getProjection();
		Point curScreenCoords = new Point();
		if (pointArrayList!=null && pointArrayList.size()!=0){
			for (int i=0;i<pointArrayList.size();i++){
				pj.toPixels(pointArrayList.get(i).getPoint(), curScreenCoords);
				c.save();
				c.rotate(tileView.getBearing(), curScreenCoords.x, curScreenCoords.y);
				onDrawItem(c, pointArrayList.get(i),curScreenCoords);
				c.restore();
			}
		}
	}


	@Override
	protected void onDrawFinished(Canvas c, TileView tileView) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event, TileView mapView) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			sharePoint=null;
			mapView.invalidate();
			return true;
		}

		return super.onKeyDown(keyCode, event, mapView);
	}



	public void addSharePoint(SharePoint point, TileView mapView){
		pointArrayList.add(point);
		mapView.invalidate();
	}

	public void onDrawItem(Canvas c,SharePoint point,Point p1){
		if (p1!=null && point!=null){
			c.drawBitmap(mBitmap_press,p1.x - (int)(mPressWidth/2), p1.y - (int)(mPressHeight / 2), mPaint);
			c.drawBitmap(mPerson,p1.x - (int)(mPersonWidth/2), p1.y - (int)(mPersonHeight+mPressHeight)/3*2, mPaint);
			c.drawText(point.getName(),p1.x+(int)(mPersonWidth/2),p1.y - (int)(mPersonHeight/2),mPaintText);
		}
	}
}