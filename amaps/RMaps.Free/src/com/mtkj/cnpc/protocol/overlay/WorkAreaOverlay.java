package com.mtkj.cnpc.protocol.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mtkj.utils.entity.SharePoint;
import com.mtkj.utils.entity.WorkAreaPoint;
import com.robert.maps.applib.R;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

import org.andnav.osm.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 工区图层
 *
 * @author CW
 *
 */
public class WorkAreaOverlay extends TileViewOverlay {

	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	private List<WorkAreaPoint> pointArrayList = new ArrayList<>();

	public WorkAreaOverlay(Context ctx){
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.parseColor("#f4ea2a"));//为画笔设置颜色
		mPaint.setStrokeWidth(5);//为画笔设置粗细
		mPaint.setStyle(Paint.Style.STROKE);//设置空心
	}
	@Override
	protected void onDraw(Canvas c, TileView tileView) {
		TileView.OpenStreetMapViewProjection pj = tileView.getProjection();
		Point pointStart = new Point();
		Point pointEnd = new Point();
		if (pointArrayList != null && pointArrayList.size() != 0) {
			for (int i = 0; i < pointArrayList.size(); i++) {
				if (i > 0) {
					pj.toPixels(pointArrayList.get(i - 1).getPoint(), pointStart);
					pj.toPixels(pointArrayList.get(i).getPoint(), pointEnd);
					c.save();
					c.rotate(tileView.getBearing(), pointStart.x, pointStart.y);
					c.rotate(tileView.getBearing(), pointEnd.x, pointEnd.y);
					onDrawItem(c, pointStart, pointEnd);
					c.restore();
				}
				if (i == pointArrayList.size() - 1) {
					pj.toPixels(pointArrayList.get(i).getPoint(), pointStart);
					pj.toPixels(pointArrayList.get(0).getPoint(), pointEnd);
					c.save();
					c.rotate(tileView.getBearing(), pointStart.x, pointStart.y);
					c.rotate(tileView.getBearing(), pointEnd.x, pointEnd.y);
					onDrawItem(c, pointStart, pointEnd);
					c.restore();
				}

			}

			/*TileView.OpenStreetMapViewProjection pj = tileView.getProjection();
			Point curScreenCoords = new Point();
			if (pointArrayList!=null && pointArrayList.size()!=0){
				for (int i=0;i<pointArrayList.size();i++){
					pj.toPixels(pointArrayList.get(i).getPoint(), curScreenCoords);
					c.save();
					c.rotate(tileView.getBearing(), curScreenCoords.x, curScreenCoords.y);
					if (i==0){
						onDrawFirst(c,curScreenCoords);
					}else if (i==pointArrayList.size()-1){
						onDrawLast(c,curScreenCoords);
					}else {
						onDrawNormal(curScreenCoords);
					}
					c.restore();
				}

			}*/

		}
	}


	@Override
	protected void onDrawFinished(Canvas c, TileView tileView) {

	}

	public void addPoint(WorkAreaPoint point, TileView mapView){
		pointArrayList.add(point);
		mapView.invalidate();
	}


	public void onDrawItem(Canvas c,Point p1,Point p2){
		if (p1!=null && p2!=null){
			c.drawLine(p1.x,p1.y,p2.x,p2.y,mPaint);
		}
	}

	public void onDrawNormal(Point p1){
		if (p1!=null){
			mPath.lineTo(p1.x,p1.y);
		}
	}

	public void onDrawFirst(Canvas c,Point p1){
		if (p1!=null){
			mPath.moveTo(p1.x,p1.y);
		}
	}

	public void onDrawLast(Canvas c,Point p1){
		if (p1!=null){
			mPath.lineTo(p1.x,p1.y);
			mPath.close();//闭合图形
			//绘制多边形
			c.drawPath(mPath,mPaint);
//			Log.e("onDrawLast", "onDrawLast: ");
		}
	}
}