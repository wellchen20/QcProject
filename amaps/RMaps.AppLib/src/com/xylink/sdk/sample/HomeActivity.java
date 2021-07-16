package com.xylink.sdk.sample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.robert.maps.applib.R;


/**
 * 主界面
 * @author zhangyazhou
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    Intent intent;
    String name = "";
    String password = "";
    int type = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button threePartyAccountNumber = findViewById(R.id.bt_ThreeParty_Account_Number);
        Button xylinkAccountNumber = findViewById(R.id.bt_XYLink_Account_Number);

        threePartyAccountNumber.setOnClickListener(this);
        xylinkAccountNumber.setOnClickListener(this);

        if (getDatas()){
            switch (type){
                case 0:
                    Intent mainIntent =new Intent(HomeActivity.this, MainActivity.class);
                    mainIntent.putExtra("name",name);
                    mainIntent.putExtra("password",password);
                    mainIntent.putExtra("islogin",true);
                    startActivity(mainIntent);
                    break;
                case 1:
                    Intent loginIntent =new Intent(HomeActivity.this, LoginActivity.class);
                    loginIntent.putExtra("name",name);
                    loginIntent.putExtra("password",password);
                    loginIntent.putExtra("islogin",true);
                    startActivity(loginIntent);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.bt_ThreeParty_Account_Number) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("islogin",false);
            startActivity(intent);
        } else if (i == R.id.bt_XYLink_Account_Number) {
            intent = new Intent(this, LoginActivity.class);
            intent.putExtra("islogin",false);
            startActivity(intent);
        }
    }

    private boolean getDatas() {
        SharedPreferences sp=getSharedPreferences("xytest", Context.MODE_PRIVATE);
        //第一个参数是键名，第二个是默认值
        name=sp.getString("name", "");
        password = sp.getString("password","");
        type = sp.getInt("type",-1);
        boolean flag = sp.getBoolean("flag",false);
        return flag;
    }
}
