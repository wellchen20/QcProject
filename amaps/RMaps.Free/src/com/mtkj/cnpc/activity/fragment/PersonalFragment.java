package com.mtkj.cnpc.activity.fragment;


import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.activity.ImportTaskActivity;
import com.mtkj.cnpc.activity.LoginActivity;
import com.mtkj.cnpc.activity.MainActivity;
import com.mtkj.cnpc.activity.ReceiveTaskActivity;
import com.mtkj.cnpc.activity.ServerSettingActivity;
import com.mtkj.cnpc.activity.VideoListActivity;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.view.SwitchButton;
import com.robert.maps.applib.utils.DialogUtils;
import com.xylink.sdk.sample.HomeActivity;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * 个人中心
 */
public class PersonalFragment extends Fragment {

    ImageView btn_exit;
    View view;
    TextView tv_version;
    TextView tv_username;
    TextView tv_phone;
    LinearLayout ll_version;
    LinearLayout ll_settting;
    LinearLayout ll_input;
    LinearLayout ll_task;
    LinearLayout ll_clear;
    LinearLayout ll_xy;
    LinearLayout ll_video;
    LinearLayout ll_record;
    SwitchButton switch_video;
    boolean is_record = false;
//    LinearLayout ll_around;
    protected PointDBDao mPointDBDao;
    String ACTION_POINT_LOC = "action_point_loc";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_personal, container, false);
        setViews();
        setListeners();
        return view;
    }

    private void setViews() {
        mPointDBDao = new PointDBDao(getActivity());
        btn_exit = view.findViewById(R.id.btn_exit);
        tv_version = view.findViewById(R.id.tv_version);
        tv_username = view.findViewById(R.id.tv_username);
        tv_phone = view.findViewById(R.id.tv_phone);
        ll_version = view.findViewById(R.id.ll_version);
        ll_settting = view.findViewById(R.id.ll_settting);
        ll_input = view.findViewById(R.id.ll_input);
        ll_task = view.findViewById(R.id.ll_task);
        ll_clear = view.findViewById(R.id.ll_clear);
        ll_xy = view.findViewById(R.id.ll_xy);
        ll_video = view.findViewById(R.id.ll_video);
        ll_record = view.findViewById(R.id.ll_record);
        switch_video = view.findViewById(R.id.switch_video);
//        ll_around = view.findViewById(R.id.ll_around);
        tv_username.setText(((MainActivity)getActivity()).getData(SysContants.USERNAME, ""));
        tv_phone.setText(((MainActivity)getActivity()).getData(SysContants.TEL, ""));
        if (((MainActivity)getActivity()).getData(SysContants.WORK_TYPE, 0)!= SysConfig.WorkType.WORK_TYPE_DRILE){
            ll_video.setVisibility(View.GONE);
        }
        if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
            ll_record.setVisibility(View.GONE);
            ((MainActivity)getActivity()).setData(SysContants.RECORD,true);
        }
        is_record = ((MainActivity)getActivity()).getData(SysContants.RECORD, false);
        switch_video.setChecked(is_record);
        // 获取packagemanager的实例
        PackageManager packageManager = getActivity().getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        try{
            PackageInfo packInfo = packageManager.getPackageInfo(getActivity().getPackageName(),0);
            String version = packInfo.versionName;
            tv_version.setText(version);
        }catch (Exception e){

        };

    }

    private void setListeners() {
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        ll_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"当前为最新版本",Toast.LENGTH_SHORT).show();
            }
        });

        ll_settting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ServerSettingActivity.class));
            }
        });

        ll_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ImportTaskActivity.class));
            }
        });

        ll_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),ReceiveTaskActivity.class);
                startActivityForResult(intent,200);
            }
        });

        ll_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showClearDataDialog();
            }
        });

        ll_xy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HomeActivity.class));
            }
        });

        ll_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), VideoListActivity.class));
            }
        });

        switch_video.setmOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void OnCheckedChanged(boolean isChecked) {
                if (isChecked){
                    ((MainActivity)getActivity()).setData(SysContants.RECORD,true);
                }else {
                    ((MainActivity)getActivity()).setData(SysContants.RECORD,false);
                }
            }
        });

       /* ll_around.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).closeMenu();
                ((MainActivity)getActivity()).startSearchAround();
            }
        });*/

    }

    /**
     * 注销用户
     */
    public void logout() {
        SysConfig.workType = SysConfig.WorkType.WORK_TYPE_NONE;
        ((MainActivity)getActivity()).setData(SysContants.WORK_TYPE, SysConfig.workType);
        ((MainActivity)getActivity()).setData(SysContants.ISLOGIN, false);
        ((MainActivity)getActivity()).setData(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType);
        DataProcess.GetInstance().stopConn();
        startActivity(new Intent(getActivity(),LoginActivity.class));
        getActivity().finish();
    }

    private Dialog dialog;
    public void showClearDataDialog() {
        dialog = DialogUtils.Alert(getActivity(), "提示", "是否清空当前数据？",
                new String[]{getActivity().getString(R.string.ok), getActivity().getString(R.string.cancel)},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        mPointDBDao.deleteAllArrange();
                        mPointDBDao.deleteAllCheckRecord();
                        mPointDBDao.deleteAllDrillPoint();
                        mPointDBDao.deleteAllDrillRecord();
                        dialog.dismiss();
                    }
                },
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }
                });
        dialog.show();
    }
}
