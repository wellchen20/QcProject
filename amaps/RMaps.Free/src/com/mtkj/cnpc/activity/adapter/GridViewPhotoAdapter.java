package com.mtkj.cnpc.activity.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.mtkj.cnpc.R;

public class GridViewPhotoAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<String> photoList = new LinkedList<String>();
	private BitmapFactory.Options options;
	private IPhotoBack mPhotoBack;

	public GridViewPhotoAdapter(Context context, List<String> strings, IPhotoBack photoBack) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		photoList = strings;
		mPhotoBack = photoBack;
		
		options = new BitmapFactory.Options();
		options.inSampleSize = 8;
	}

	public void addPhoto(List<String> strings) {
		if (strings != null && strings.size() > 0) {
			photoList.clear();
			for (String string : strings) {
				photoList.add(string);
			}
		}
		update();
	}

	public void update() {
		this.notifyDataSetChanged();
	}
	
	public List<String> getPhotoList() {
		return photoList;
	}

	@Override
	public int getCount() {
		if (photoList == null) {
			return 1;
		} else if (photoList.size() == 3) {
			return 3;
		} else {
			return photoList.size() + 1;
		}
	}

	@Override
	public Object getItem(int arg0) {
		return photoList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View contertView, ViewGroup arg2) {
		HoldView holdView;
		if (contertView == null) {
			contertView = mInflater.inflate(R.layout.gridview_photo_item, null);
			holdView = new HoldView();
			holdView.iv_image = (ImageView) contertView.findViewById(R.id.iv_image);
			holdView.photo_del = (ImageView) contertView.findViewById(R.id.photo_del);
			contertView.setTag(holdView);
		} else {
			holdView = (HoldView) contertView.getTag();
		}
		if (position == photoList.size()) {
			holdView.photo_del.setVisibility(View.GONE);
			holdView.iv_image.setImageResource(R.drawable.compose_pic_add_highlighted);
			holdView.iv_image.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					mPhotoBack.addPhoto();
				}
			});
		} else {
			holdView.photo_del.setVisibility(View.VISIBLE);
			
			Bitmap bitmap = BitmapFactory.decodeFile(photoList.get(position), options);
			holdView.iv_image.setImageBitmap(bitmap);
			
			holdView.iv_image.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
//					Intent intent = new Intent(mContext, ShowBigImage.class);
//					intent.putExtra("imagepath", photoList.get(position));
//					mContext.startActivity(intent);
				}
			});

			holdView.photo_del.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					photoList.remove(position);
					update();
				}
			});
		}
		return contertView;
	}

	class HoldView {
		ImageView iv_image;
		ImageView photo_del;
	}

	public interface IPhotoBack {
		public void addPhoto();
	}
	
	private void destoryBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}
}
