package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mtkj.cnpc.activity.adapter.TaskDetailsAdapter;
import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.cnpc.R;

public class TaskDetailsActivity extends Activity {

    ListView pullToRefreshRV;
    TextView tv_start;
    TextView tv_end;
    TextView tv_type;
    ImageView iv_back;
    TaskEntity  taskEntity;
    TaskDetailsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        getMessages();
        setViews();
        setAdapters();
        setListeners();
    }

    private void getMessages() {
        taskEntity = (TaskEntity) getIntent().getSerializableExtra("entity");
    }

    private void setViews() {
        pullToRefreshRV = (ListView) findViewById(R.id.pullToRefreshRV);
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_end = (TextView) findViewById(R.id.tv_end);
        tv_type = (TextView) findViewById(R.id.tv_type);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_start.setText(taskEntity.getStart_time());
        tv_end.setText(taskEntity.getFinish_time());
        if (taskEntity.getTask_type()==1){
            tv_type.setText("放线任务");
        }else {
            tv_type.setText("井炮任务");
        }
    }

    private void setAdapters() {
        adapter = new TaskDetailsAdapter(TaskDetailsActivity.this,taskEntity);
        pullToRefreshRV.setAdapter(adapter);
    }

    private void setListeners() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
