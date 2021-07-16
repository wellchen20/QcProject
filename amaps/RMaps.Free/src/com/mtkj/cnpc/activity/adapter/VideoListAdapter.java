package com.mtkj.cnpc.activity.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.DrillRecognizeActivity;
import com.mtkj.cnpc.activity.VideoActivity;
import com.mtkj.cnpc.activity.VideoListActivity;
import com.mtkj.cnpc.activity.interfaces.HttpUtil;
import com.mtkj.cnpc.activity.interfaces.RetrofitService;
import com.mtkj.cnpc.activity.utils.CycleView;
import com.mtkj.cnpc.activity.utils.FileRequestBody;
import com.mtkj.cnpc.activity.utils.RetrofitCallback;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.PictureEntity;
import com.obs.services.ObsClient;
import com.obs.services.model.ProgressListener;
import com.obs.services.model.ProgressStatus;
import com.obs.services.model.PutObjectRequest;
import com.robert.maps.applib.utils.DialogUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Handler;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Subscriber;


/**
 * Created by Administrator on 2017/6/29 0029.
 */

public class VideoListAdapter extends BaseAdapter {
    VideoListActivity context;
    ArrayList<String> data;
    ArrayList<String> pathArr;
    ArrayList<String> isuploadList;
    ArrayList<String> localStation;
    private PointDBDao mPointDBDao;
    public VideoListAdapter(VideoListActivity context, ArrayList<String> data, ArrayList<String>  pathArr, ArrayList<String> isuploadList,ArrayList<String> localStation){
        this.context = context;
        this.data = data;
        this.pathArr = pathArr;
        this.isuploadList = isuploadList;
        this.localStation = localStation;
        mPointDBDao = new PointDBDao(context);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder ;
        if(convertView==null){
            convertView = View.inflate(context, R.layout.item_system_list,null);
            viewHolder = new ViewHolder();
            viewHolder.tv_title = convertView.findViewById(R.id.tv_title);
            viewHolder.iv_paly = convertView.findViewById(R.id.iv_paly);
            viewHolder.iv_upload = convertView.findViewById(R.id.iv_upload);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if ("0".equals(isuploadList.get(position)) ||"2".equals(isuploadList.get(position))){
            viewHolder.iv_upload.setImageResource(R.drawable.upload_1);
        }
        viewHolder.tv_title.setText(data.get(position));
        viewHolder.iv_paly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("path",pathArr.get(position));
                context.startActivity(intent);
            }
        });
        viewHolder.iv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("1".equals(isuploadList.get(position))){
                    //上传视频
                    showUploadDialog(position);
                }else {
                    Toast.makeText(context,"视频不满足上传条件",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return convertView;
    }

    public void showUploadDialog(int position) {
        lDialog = DialogUtils.Alert(context, "提示", "确定上传此视频吗？",
                new String[]{"是","否"},
                new View.OnClickListener[]{new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lDialog.dismiss();
                        showProgress();
                        new Thread(){
                            @Override
                            public void run() {
                                uploadVideo(pathArr.get(position),position);
                            }
                        }.start();

                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                lDialog.dismiss();
                            }
                        }
                });
        lDialog.show();
    }

    public void uploadVideo(String videoPath,int position){
        String endPoint = "obs.cn-east-3.myhuaweicloud.com";
        String ak = "01PLBCK9KKM99TVKW7JV";
        String sk = "IMM9QWe5pZGmkKyXYwPOKrLsUyx4PWACwcejmVEV";
// 创建ObsClient实例
        ObsClient obsClient = new ObsClient(ak, sk, endPoint);
        String uuid = UUID.randomUUID().toString();
        Log.e("uuid", "uuid1: "+uuid);
        uuid = uuid.replaceAll("-","");
        Log.e("uuid", "uuid2: "+uuid);
        PutObjectRequest request = new PutObjectRequest("fengtest001", uuid);
        request.setFile(new File(videoPath)); // localfile为上传的本地文件路径，需要指定到具体的文件名
        String finalUuid = uuid;
        request.setProgressListener(new ProgressListener() {

            @Override
            public void progressChanged(ProgressStatus status) {
                // 获取上传平均速率
                Log.e("PutObject", "AverageSpeed:" + status.getAverageSpeed());
                // 获取上传进度百分比
                Log.e("PutObject", "TransferPercentage:" + status.getTransferPercentage());
                int count_pro = status.getTransferPercentage();
                cyl_credit.setProgress(count_pro);
                //此处进行进度更新
                if (count_pro==100){
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lDialog.dismiss();
                            context.refush(position);
                            Toast.makeText(context,"视频上传成功",Toast.LENGTH_SHORT).show();
                            uploadVideoPath(finalUuid,localStation.get(position));
                        }
                    });

                }
            }
        });
// 每上传1MB数据反馈上传进度
        request.setProgressInterval(1024 * 1024L);
        obsClient.putObject(request);
    }

    private void uploadVideoPath(String uuid,String stationNo) {
        HttpUtil.init(HttpUtil.getService(RetrofitService.class).uploadFile(uuid, stationNo), new Subscriber<PictureEntity>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(PictureEntity pictureEntity) {
                int code = pictureEntity.getCode();
                Log.e("code", "code: "+code );
            }
        });
    }
/*
    public void uploadVideo(String videoPath,int position){
        showProgress();
        File file = new File(videoPath);
        RetrofitCallback<PictureEntity> callback = new RetrofitCallback<PictureEntity>() {
            @Override
            public void onSuccess(Call<PictureEntity> call, Response<PictureEntity> response) {
//                runOnUiThread(activity, response.body().toString());
                Log.e("onSuccess", "onSuccess: "+response.body().getMsg()+"|code = "+response.body().getCode());
                lDialog.dismiss();
                int code = response.body().getCode();
                if (code==0){
//                    context.refush(position);
//                    Toast.makeText(context,"视频上传成功",Toast.LENGTH_SHORT).show();
                }else {
//                    Toast.makeText(context,"视频上传失败",Toast.LENGTH_SHORT).show();
                }

                //进度更新结束
            }
            @Override
            public void onFailure(Call<PictureEntity> call, Throwable t) {
                Log.e("onFailure", "onFailure: "+t.getMessage() );
                Toast.makeText(context,"视频上传失败",Toast.LENGTH_SHORT).show();
                lDialog.dismiss();
                //进度更新结束
            }
            @Override
            public void onLoading(long total, long progress) {
                super.onLoading(total, progress);
                BigDecimal currentCount = new BigDecimal(progress);
                BigDecimal totalCount = new BigDecimal(total);
                BigDecimal divide = currentCount.divide(totalCount,2, BigDecimal.ROUND_HALF_UP);
                long pro = divide.multiply(new BigDecimal(100)).longValue();
//                Log.e("onLoading", "total:"+total+"|progress:"+progress+"|pro"+pro );
                int count_pro = (int)pro;
                cyl_credit.setProgress(count_pro);
                //此处进行进度更新
                if (count_pro==100){
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lDialog.dismiss();
                            context.refush(position);
                            Toast.makeText(context,"视频上传成功",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        };
        RequestBody body1 = RequestBody.create(MediaType.parse("application/otcet-stream"), file);
        //通过该行代码将RequestBody转换成特定的FileRequestBody
        FileRequestBody body = new FileRequestBody(body1, callback);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
        Call<PictureEntity> call = HttpUtil.getService(RetrofitService.class).uploadFile(part,localStation.get(position));
        call.enqueue(callback);
    }
    */

    CycleView cyl_credit;
    Dialog lDialog;
    public void showProgress() {
        lDialog = new Dialog(context, R.style.MyDialogStyleFullText);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setCancelable(false);
        lDialog.setContentView(R.layout.layout_progess_upload);
        cyl_credit =  lDialog.findViewById(R.id.cyl_credit);
        lDialog.show();
    }

    class ViewHolder{
        TextView tv_title;
        ImageView iv_paly;
        ImageView iv_upload;
    }
}
