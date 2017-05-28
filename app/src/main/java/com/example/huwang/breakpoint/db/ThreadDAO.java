package com.example.huwang.breakpoint.db;

import com.example.huwang.breakpoint.domain.ThreadInfo;

import java.util.List;

/**
 * Created by huwang on 2017/5/28.
 */

public interface ThreadDAO {
    public void insertThread(ThreadInfo info);
    public void deleteThread(String url);

    /**
     * 更新下载进度
     * @param url
     * @param threadId
     * @param finished
     */
    public void updateThread(String url, int threadId, int finished);

    /**
     * @param url
     * @return 查询文件的线程信息
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url
     * @param threadId
     * @return
     */
    public boolean isExist(String url, int threadId);
}
