package com.mtkj.cnpc.activity.interfaces;

import com.ainemo.sdk.model.User;
import com.mtkj.utils.entity.PictureEntity;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public interface RetrofitService {
   /* @GET("/bijia/car/info/searchCarByUserId")//get
    Call<CarEntity> getInfoData(@Query("userId") int userId);
    @POST("/asd/asd")//post
    Call<CarEntity> getExample(@Body CarEntity carEntity);

    @GET("/bijia/car/info/searchCarByUserId")//get

    Observable<CarEntity> getRxData(@Query("userId") int userId);
    @POST("doAllCardRecon")//身份证，驾驶证，行驶证，银行卡，车牌号上传
    Observable<ResponseBody> uploadjson(@Body RequestBody jsonBody);*/
   /*测试*/

/*

   */
/*上传工单和图片*//*

   @Multipart
   @POST("/api/order")
   Observable<ResponseBody> uploadOrder(@Part(value = "order") RequestBody jsonBody, @Part List<MultipartBody.Part> file, @Part(value = "pic") RequestBody entity);

   */



   //上传工单
   @POST("/api/uploadQcInfo")
   Observable<ResponseBody> upQcInfo(@Body RequestBody jsonBody);

   //   上传工单
 /*  @Multipart
   @POST("/api/uploadQcInfo")
   Observable<ResponseBody> upQcInfo(@Part(value = "data") RequestBody jsonBody);*/


//问题上报

   @Multipart
   @POST("/api/question")
   Observable<ResponseBody> uploadQuestion(@Part(value = "question") RequestBody body, @Part List<MultipartBody.Part> file, @Part(value = "pic") RequestBody jsonBody);

/*   //工单列表
   @GET("/api/orders/user/{user}")
   Observable<CheckListEntity> getCheckList(@Path("user") String user);

   //工单详情
   @GET("/api/getorder/{workOrderId}")
   Observable<CheckContentEntity> getCheckContent(@Path("workOrderId") String workOrderId);

   //获取用户信息
   @GET("/api/getaccessstationinfo/user/{user}")
   Observable<Userbean> getUserInfo(@Path("user") String user);*/

   /*进出站状态*/
//   @Multipart
//   @POST("/api/setaccessstationstatus")
//   Observable<ResponseBody> Submitjc(@Part(value = "data") RequestBody body, @Part List<MultipartBody.Part> file, @Part(value = "pic") RequestBody jsonBody);

   //接收任务
   @GET("/task/toObtain")
   Observable<ResponseBody> toObtain(@Query("processType") int processType);

   //   上传质检任务
   @POST("/since/saveTaskDetails")
   Observable<ResponseBody> saveTaskDetails(@Body RequestBody jsonBody);

   //   上传检波器图片
   @Multipart
   @POST("/front/file")
   Observable<ResponseBody> uploadImages( @Part List<MultipartBody.Part> file);

   //   上传文件
   @POST("/front/fileVideo")
   Observable<PictureEntity> uploadFile(@Query("objectName") String objectName,@Query("pileNo") String pileNo);
}
