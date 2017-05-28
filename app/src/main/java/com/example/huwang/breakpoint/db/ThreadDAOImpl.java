package com.example.huwang.breakpoint.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.huwang.breakpoint.domain.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwang on 2017/5/28.
 */

public class ThreadDAOImpl implements ThreadDAO {
    private DBHelper mHelper = null;

    public ThreadDAOImpl(Context context) {
        mHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo info) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id, url, start, end, finished) values(?,?,?,?,?)",
                new Object[] {info.getId(), info.getUrl(), info.getStart(), info.getEnd(), info.getFinished()});
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url=?",
                new Object[] {url});
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int threadId, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished=? where url=? and thread_id=?",
                new Object[] {finished, url, threadId});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url=?", new String[]{url});
        List<ThreadInfo> list = new ArrayList<>();
        while(cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo();

            info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            info.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            info.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));

            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExist(String url, int threadId) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url=? and thread_id= ?", new String[]{url, String.valueOf(threadId)});
        boolean result = cursor.moveToNext();
        cursor.close();
        db.close();
        return result;
    }
}
