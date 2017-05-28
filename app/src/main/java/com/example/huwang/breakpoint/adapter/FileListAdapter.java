package com.example.huwang.breakpoint.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.huwang.breakpoint.R;
import com.example.huwang.breakpoint.domain.FileInfo;
import com.example.huwang.breakpoint.service.DownloadService;

import java.util.List;

/**
 * Created by huwang on 2017/5/28.
 */

public class FileListAdapter extends BaseAdapter {
    private List<FileInfo> mFileInfos;
    private Context mContext;

    public FileListAdapter(List<FileInfo> fileInfos, Context context) {
        mFileInfos = fileInfos;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mFileInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FileInfo fileInfo = mFileInfos.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            // 获取控件
            holder = new ViewHolder();
            holder.mTvFileName = (TextView) convertView.findViewById(R.id.tv_filename);
            holder.mSeekBar = (SeekBar) convertView.findViewById(R.id.download_process);
            holder.mStartButton = (Button) convertView.findViewById(R.id.bt_start);
            holder.mStopButton = (Button) convertView.findViewById(R.id.bt_stop);
            holder.mTvFileName.setText(fileInfo.getFileName());
            holder.mSeekBar.setMax(100);
            holder.mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileinfo", fileInfo);
                    mContext.startService(intent);
                }
            });

            holder.mStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STOP);
                    intent.putExtra("fileinfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // 设置信息

        holder.mSeekBar.setProgress(fileInfo.getFinished());

        return convertView;
    }

    /**
     * 一个临时存储器，把每次getView中每次返回的view缓存起来，下次可直接使用，减少了findViewById查找控件的开销
     */
    static class ViewHolder {
        TextView mTvFileName;
        SeekBar mSeekBar;
        Button mStopButton;
        Button mStartButton;
    }

    public void updateProgress(int id, int progress) {
        FileInfo info = mFileInfos.get(id);
        info.setFinished(progress);
        notifyDataSetChanged();
    }

}
