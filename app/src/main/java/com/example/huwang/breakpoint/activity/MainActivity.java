package com.example.huwang.breakpoint.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.huwang.breakpoint.MyApplication;
import com.example.huwang.breakpoint.R;
import com.example.huwang.breakpoint.adapter.FileListAdapter;
import com.example.huwang.breakpoint.domain.FileInfo;
import com.example.huwang.breakpoint.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //    private TextView mTvFileName;
//    private SeekBar mSeekBar;
//    private Button mStopButton;
//    private Button mStartButton;
    private ListView mListView;
    private List<FileInfo> mFileInfos;
    private FileListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        mFileInfos = new ArrayList<>();
        String url1 = "https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1495958337&di=7bda1335f7c490b06d6f5180ae019954&src=http://d.5857.com/xgs_150428/desk_005.jpg";
        String url2 = "https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1495960983&di=cf69a572ac170dcdb197cfa29048f2b9&src=http://pic1.win4000.com/pic/3/e9/72821378027.jpg";
        String url3 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1495971207767&di=a0d3e988cca7c89fcac0a8f76ac8e323&imgtype=0&src=http%3A%2F%2Fstar.xiziwang.net%2Fuploads%2Fallimg%2F140427%2F22_140427172810_4.jpg";
        String url4 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1495971207765&di=151b4e1bf02c1edf665bf205448458b2&imgtype=0&src=http%3A%2F%2Fwww.5djiaren.com%2Fuploads%2F2015-09%2F24-114331_533.jpg";


        final FileInfo fileInfo = new FileInfo(0, url1, "girls.jpg", 0, 0);
        final FileInfo fileInfo1 = new FileInfo(1, url2, "girls1.jpg", 0, 0);
        final FileInfo fileInfo2 = new FileInfo(2, url3, "girls2.jpg", 0, 0);
        final FileInfo fileInfo3 = new FileInfo(3, url4, "girls3.jpg", 0, 0);

        mFileInfos.add(fileInfo);
        mFileInfos.add(fileInfo1);
        mFileInfos.add(fileInfo2);
        mFileInfos.add(fileInfo3);

        mAdapter = new FileListAdapter(mFileInfos, MyApplication.getContext());
        mListView.setAdapter(mAdapter);


        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lv_File);
    }

    /**
     * 更新UI
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updateProgress(id, finished);
//                mSeekBar.setProgress(finished);
            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())){
                FileInfo info = (FileInfo) intent.getSerializableExtra("fileinfo");
                mAdapter.updateProgress(info.getId(), 0);
                Toast.makeText(MainActivity.this, mFileInfos.get(info.getId()).getFileName()+"下载完毕", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
