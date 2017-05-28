package com.example.huwang.breakpoint.service;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.example.huwang.breakpoint.db.ThreadDAO;
import com.example.huwang.breakpoint.db.ThreadDAOImpl;
import com.example.huwang.breakpoint.domain.FileInfo;
import com.example.huwang.breakpoint.domain.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huwang on 2017/5/28.
 */

public class DownloadTask {
    private FileInfo mFileInfo;
    private ThreadDAO mDAO;
    private Context mContext;
    private int mFinished;
    public boolean isPause;
    private int mThreadCount = 1;
    private List<DownloadThread> mDownloadThreads;
    public static ExecutorService sExecutorService = Executors.newCachedThreadPool();


    public DownloadTask(Context context, FileInfo fileInfo, int count) {
        mContext = context;
        mFileInfo = fileInfo;
        mThreadCount = count;
        mDAO = new ThreadDAOImpl(mContext);
    }

    public void download() {
        //读取数据库线程信息
        List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getUrl());
        if (threadInfos.size() == 0) {
            // 获取每个下载长度
            int length = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), length * i, (i+1)*length-1, 0);
                if (i == mThreadCount -1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threadInfos.add(threadInfo);
                // 向数据库插入线程信息
                    mDAO.insertThread(threadInfo);
            }
        }

        mDownloadThreads = new ArrayList<>();
        // 启动多个线程下载
        for (int i = 0; i < threadInfos.size(); i++) {
            DownloadThread thread = new DownloadThread(threadInfos.get(i));
//            thread.start();
            DownloadTask.sExecutorService.execute(thread);
            // 管理线程
            mDownloadThreads.add(thread);
        }
    }

    private synchronized void checkAllThreadFinished() {
        boolean allFinished = true;
        for (DownloadThread thread :
                mDownloadThreads) {
            if (!thread.isFinished) {
                allFinished = false;
                return;
            }
        }
        if (allFinished) {
            // 下载完成删除信息
            mDAO.deleteThread(mFileInfo.getUrl());
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileinfo", mFileInfo);
            mContext.sendBroadcast(intent);
        }
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        public boolean isFinished;
        public DownloadThread(ThreadInfo info) {
            mThreadInfo = info;
        }

        @Override
        public void run() {
            Log.i("zhang", "开始下载");

            // 设置下载位置
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes="+start + "-" + mThreadInfo.getEnd());
                // 文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());

                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);

                mFinished += mThreadInfo.getFinished();

                // 开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    // 读取数据
                    while((len = input.read(buffer)) != -1) {
                        // 写入文件
                        raf.write(buffer, 0, len);
                        // 下载暂停保存进度
                        // 整个文件的下载进度
                        mFinished += len;
                        // 累加每个线程的下载进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished()+len);
                        intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                        intent.putExtra("id", mFileInfo.getId());
                        SystemClock.sleep(1000);
                        mContext.sendBroadcast(intent);

                        if (isPause) {
                            mDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                            return;
                        }
                    }
                    // 标示线程执行完毕
                    isFinished = true;


                    // 检查下载完成
                    checkAllThreadFinished();
                }

                // 广播进度
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                try {
                    if (raf != null) {
                        raf.close();
                    }
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
