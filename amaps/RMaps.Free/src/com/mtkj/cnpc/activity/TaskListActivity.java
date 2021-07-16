package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.mtkj.cnpc.activity.adapter.TaskAllAdapter;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.sqlite.PointDBHelper;
import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;
/**任务消息*/
public class TaskListActivity extends Activity {
    ListView pullToRefreshRV;
    ImageView iv_back;
    TaskAllAdapter allAdapter;
    private List<TaskEntity> mTask = new ArrayList<TaskEntity>();
    protected PointDBDao mPointDBDao;
    UpdateReceiver updateReceiver;
    String ACTION_GET_MESSAGE = "action_get_message";
    private ImageView iv_menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        setData();
        setViews();
        setAdapters();
        setListeners();
        registReceiver();
    }

    private void setData() {
        mPointDBDao = new PointDBDao(this);
        PointDBHelper pointDBHelper = PointDBHelper.getInstance(TaskListActivity.this);
        pointDBHelper.getReadableDatabase();
        mTask = mPointDBDao.getAllTask();
    }

    private void setAdapters() {
        allAdapter = new TaskAllAdapter(TaskListActivity.this,mTask);
        pullToRefreshRV.setAdapter(allAdapter);
    }

    private void setViews() {
        pullToRefreshRV = (ListView) findViewById(R.id.pullToRefreshRV);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
    }

    private void setListeners() {
        pullToRefreshRV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TaskListActivity.this,TaskDetailsActivity.class);
                intent.putExtra("entity",mTask.get(position));
                startActivity(intent);
            }
        });

        pullToRefreshRV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                List<TaskEntity> task = mPointDBDao.getAllTask();
                showClearDataDialog(task.get(position).getId(),0);
                return true;
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDataDialog(0,1);
            }
        });
    }

    private void registReceiver(){
        updateReceiver = new UpdateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_GET_MESSAGE);
        registerReceiver(updateReceiver, filter);
    }

    private Dialog dialog;
    public void showClearDataDialog(final int id, final int flag) {
        dialog = DialogUtils.Alert(TaskListActivity.this, "提示", "是否删除当前数据？",
                new String[]{TaskListActivity.this.getString(R.string.ok), TaskListActivity.this.getString(R.string.cancel)},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //
                        if (flag==0){
                            mPointDBDao.deleteTask(id);
                            allAdapter.refush(mPointDBDao.getAllTask());
                            dialog.dismiss();
                        }else if (flag==1){
                            mPointDBDao.deleteAllTask();
                            allAdapter.refush(mPointDBDao.getAllTask());
                            dialog.dismiss();
                        }

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

    class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_GET_MESSAGE)){
                allAdapter.refush(mPointDBDao.getAllTask());
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
    }
}
