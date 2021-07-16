package com.mtkj.utils.entity;

import android.content.Context;

import java.io.InputStream;

/**
 * Created by Administrator on 2019/1/2.
 */

public class FileUtils {
    /**读取本地JSON数据*/
    public static String readLocalJson(Context context, String fileName){
        String jsonString="";
        String resultString="";
        try {
            InputStream inputStream=context.getResources().getAssets().open(fileName);
            byte[] buffer=new byte[inputStream.available()];
            inputStream.read(buffer);
            resultString=new String(buffer,"utf-8");
        } catch (Exception e) {
            // TODO: handle exception
        }
        return resultString;
    }
}
