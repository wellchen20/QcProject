package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.adapter.CheckAdapter;
import com.mtkj.cnpc.activity.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SeeCheckActivity extends Activity {
    List<Bitmap> bitmaps;
    ArrayList<String> paths;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_check);
        initData();
        ViewPager vp_check = findViewById(R.id.vp_check);
        if (bitmaps.size()!=0){
            vp_check.setAdapter(new CheckAdapter(this,bitmaps));
        }

    }

    private void initData() {
        bitmaps = new ArrayList<>();
        paths = getIntent().getStringArrayListExtra("paths");
        Log.e("paths", "paths: "+paths.size() );
        for (int i=0;i<paths.size();i++){
            Uri uri = FileUtils.getUriForFile(SeeCheckActivity.this,new File(paths.get(i)));
            Log.e("uri", "uri: "+uri+"" );
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().
                        openInputStream(uri));
                bitmaps.add(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(SeeCheckActivity.this,"本地图片不存在",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
