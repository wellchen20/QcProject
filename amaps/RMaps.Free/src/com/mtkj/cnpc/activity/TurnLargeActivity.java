package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mtkj.cnpc.R;

public class TurnLargeActivity extends Activity {

    ImageView iv_image;
    Uri uri;
    Bitmap mBitmap;
    String pathName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn_large);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        pathName = getIntent().getStringExtra("pathName");
        Log.e("pathName", pathName+"");
        mBitmap = BitmapFactory.decodeFile(pathName);
        iv_image.setImageBitmap(mBitmap);
        iv_image.setAdjustViewBounds(true);
        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
