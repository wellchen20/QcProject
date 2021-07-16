package com.mtkj.cnpc.activity.interfaces;

import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/7/13 0013.
 * Retrofit+RxJava网络请求框架
 */
public class HttpUtil {

    private static HttpUtil instance;
    private Retrofit retrofit;
    private String url = "http://"+SysConfig.IP+":"+SysConfig.PORT;
    private static final OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(120, TimeUnit.SECONDS).
            readTimeout(120, TimeUnit.SECONDS).
            writeTimeout(120, TimeUnit.SECONDS).build();

    private HttpUtil() {
        this.instance = this;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private static HttpUtil getInstance() {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    return new HttpUtil();
                }
            }
        }
        return instance;
    }

    public static <T> T getService(Class<T> c) {
        return getInstance().retrofit.create(c);
    }

    public static <T> void init(Observable<T> observable, Subscriber<T> subscriber) {
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}
