package com.bourne.httprequest.singleThreadDownload;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.bourne.httprequest.FileInfo;
import com.bourne.httprequest.ThreadInfo;
import com.bourne.httprequest.singleThreadDownload.db.ThreadDAO;
import com.bourne.httprequest.singleThreadDownload.db.ThreadDAOImple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DownloadTask {
    private Context mComtext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDao = null;
    private int mFinished = 0;
    public boolean mIsPause = false;

    public DownloadTask(Context comtext, FileInfo fileInfo) {
        super();
        this.mComtext = comtext;
        this.mFileInfo = fileInfo;
        this.mDao = new ThreadDAOImple(mComtext);
    }

    /**
     * 查询数据库上一次下载的信息，有则获取，没有则新建
     */
    public void download() {
        // 从数据库中获取到下载的信息
        List<ThreadInfo> list = mDao.queryThreads(mFileInfo.getUrl());
        ThreadInfo info = null;
        if (list.size() == 0) {
            info = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            info = list.get(0);

        }
        //开启下载任务
        new DownloadThread(info).start();
    }

    class DownloadThread extends Thread {
        private ThreadInfo threadInfo = null;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            Log.i("test", "开启一个下载任务");
            // 如果数据库不存在下载信息，添加下载信息
            if (!mDao.isExists(threadInfo.getUrl(), threadInfo.getId())) {
                mDao.insertThread(threadInfo);
            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5 * 1000);
                connection.setRequestMethod("GET");
                //开始位置为getFinished()开始上次结束的位置
                Log.i("test", "文件的长度"+mFileInfo.getLength()+"   上次结束的位置：" + threadInfo.getFinished());
                int start = threadInfo.getStart() + threadInfo.getFinished();
                // 设置下载文件开始到结束的位置（结束的位置也就是文件的长度）
                connection.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                //设置文件下载地址和文件名字
                File file = new File(SingleThreadDownloadActivity.DownloadPath, mFileInfo.getFileName());
                //设置输出文件路径
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                mFinished += threadInfo.getFinished();

                int code = connection.getResponseCode();
                if (code == HttpURLConnection.HTTP_PARTIAL) {
                    input = connection.getInputStream();
                    byte[] data = new byte[1024];
                    int count = -1;
                    // 定义UI刷新时间
                    long time = System.currentTimeMillis();
                    while ((count = input.read(data)) != -1) {
                        raf.write(data, 0, count);
                        //记录结束的位置
                        mFinished += count;
                        // 设置为500毫秒更新一次
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            //发送一个广播提示下载的进度
                            Intent intent = new Intent(SingleThreadDownloadActivity.ACTION_UPDATE);
                            //结束的位置/文件长度*100=下载进度百分比
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            Log.i("test", mFinished * 100 / mFileInfo.getLength() + "");
                            // 发送广播给Activity
                            mComtext.sendBroadcast(intent);
                        }
                        if (mIsPause) {
                            //如果状态为暂停，则跳出循环，并记录这次结束的位置的长度
                            mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), mFinished);
                            return;
                        }
                    }
                }

                // 下载完成后，刪除数据库信息
                mDao.deleteThread(threadInfo.getUrl(), threadInfo.getId());
                Intent intent = new Intent(SingleThreadDownloadActivity.ACTION_UPDATE);
                //结束的位置/文件长度*100=下载进度百分比
                intent.putExtra("finished", 100);
                // 发送广播给Activity
                mComtext.sendBroadcast(intent);
                Log.i("DownloadTask", "下载完毕");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("test", "关闭一个下载任务");
            }
            super.run();
        }
    }
}
