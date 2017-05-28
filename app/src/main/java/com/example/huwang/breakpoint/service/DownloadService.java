package com.example.huwang.breakpoint.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.huwang.breakpoint.MyApplication;
import com.example.huwang.breakpoint.domain.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwang on 2017/5/28.
 */

public class DownloadService extends Service {
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/downloads/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();
    public static final int MSG = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileinfo = (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("zhang", "start--" + fileinfo.toString());
            // 启动初始化线程
//            new InitThread(fileinfo).start();
            DownloadTask.sExecutorService.execute(new InitThread(fileinfo));
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileinfo = (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("zhang", "stop--" + fileinfo.toString());
            DownloadTask task = mTasks.get(fileinfo.getId());
            if (task != null) {
                task.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i("zhang", "init: " + fileInfo);
                    DownloadTask task = new DownloadTask(MyApplication.getContext(), fileInfo, 3);;
                    task.download();
                    mTasks.put(fileInfo.getId(), task);
                    break;
                default:
                    break;
            }
        }
    };


    class InitThread extends Thread {
        private FileInfo mFileInfo;

        public InitThread(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection urlConnection = null;
            RandomAccessFile raf = null;
            try {
                // 链接网络文件
                URL url = new URL(mFileInfo.getUrl());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(3000);
                int length = -1;
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // 获取文件长度
                    length = urlConnection.getContentLength();
                    Log.i("zhang", length + "");
                }
                if (length <= 0) {
                    return;
                }
                // 创建文件设置文件长度
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(DOWNLOAD_PATH, mFileInfo.getFileName());

                raf = new RandomAccessFile(file, "rwd");

//                if (raf != null) {
//                    Log.i("zhang",  mFileInfo.getFileName());
//                }
                // 设置文件长度
                raf.setLength(length);
                mFileInfo.setLength(length);
                Message msg = mHandler.obtainMessage(MSG, mFileInfo);
                mHandler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
                try {
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
