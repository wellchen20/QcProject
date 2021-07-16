package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtkj.cnpc.R;

import java.util.List;

public class CheckAdapter extends PagerAdapter {
    List<Bitmap> bitmaps;
    Context context;
    public CheckAdapter(Context context,List<Bitmap> bitmaps){
        this.bitmaps = bitmaps;
        this.context = context;
    }
    @Override
    public int getCount() {
        return bitmaps.size();
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = View.inflate(context, R.layout.item_pager_check, null);
        TextView tv_number = view.findViewById(R.id.tv_number);
        ImageView iv_check = view.findViewById(R.id.iv_check);
        iv_check.setImageBitmap(bitmaps.get(position));
        int pos = position+1;
        iv_check.setScaleType(ImageView.ScaleType.FIT_XY);
        tv_number.setText(pos+"/3");
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container,position,object); 这一句要删除，否则报错
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
