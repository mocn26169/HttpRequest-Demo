package com.bourne.httprequest.singleThreadDownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bourne.httprequest.R;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class SingleThreadDownloadActivity extends AppCompatActivity {

    //下载链接
    private String urlstr = "http://downloads.jianshu.io/apps/haruki/JianShu-2.2.3-17040111.apk";
    //进度条
    private ProgressBar progressBar;
    //显示的文件名
    private TextView fileName;
    //下载的文件的信息
    private FileInfo fileInfo;
    //开始
    public static final String ACTION_START = "ACTION_START";
    //暂停
    public static final String ACTION_STOP = "ACTION_STOP";
    //更新
    public static final String ACTION_UPDATE = "ACTION_UPDATE";

    // 文件下载地址
    public static final String DownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Download/";
    //handler消息标志位
    public static final int MSG_INIT = 0;
    //下载任务
    private DownloadTask mTask = null;
    //广播消息
    private UIRecive mRecive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_thread_download);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fileName = (TextView) findViewById(R.id.file_textview);

        // 初始化进度条
        progressBar.setMax(100);
        // 初始化文件对象
        fileInfo = new FileInfo(0, urlstr, getfileName(urlstr), 0, 0);
        //初始化广播
        mRecive = new UIRecive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE);
        registerReceiver(mRecive, intentFilter);
    }

    // 从DownloadTadk中获取广播信息，更新进度条
    class UIRecive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                progressBar.setProgress(finished);
            }
        }
    }

    /**
     * 点击开始
     *
     * @param view
     */
    public void onStartClick(View view) {

        // 开启
        fileName.setText(getfileName(urlstr));
        // 获得Activity传来的参数
        Log.i("test", "START" + fileInfo.toString());
        //开启一个下载任务
        new InitThread(fileInfo).start();
    }

    /**
     * 点击暂停
     *
     * @param view
     */
    public void onStopClick(View view) {
        Log.i("test", "STOP" + fileInfo.toString());
//        下载任务将会结束循环
        if (mTask != null) {
            mTask.mIsPause = true;
        }
    }

    // 从URL地址中获取文件名，即/后面的字符
    private String getfileName(String url) {
        return urlstr.substring(urlstr.lastIndexOf("/") + 1);
    }

    // 从InitThread线程中获取FileInfo信息，然后开始下载任务
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i("test", "INIT:" + fileInfo.toString());
//                     获取FileInfo對象，开始下载任务
                    mTask = new DownloadTask(SingleThreadDownloadActivity.this, fileInfo);
                    mTask.download();
                    break;
            }
        }

        ;
    };

    // 初始化下载线程，获得下载文件的信息
    class InitThread extends Thread {
        private FileInfo mFileInfo = null;

        public InitThread(FileInfo mFileInfo) {
            super();
            this.mFileInfo = mFileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5 * 1000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                int length = -1;
                if (code == HttpURLConnection.HTTP_OK) {
                    length = conn.getContentLength();
                }
                //如果文件长度为小于0，表示获取文件失败，直接返回
                if (length <= 0) {
                    return;
                }
                // 判断文件路径是否存在，不存在这创建
                File dir = new File(DownloadPath);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                // 创建本地文件
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                // 设置文件长度
                mFileInfo.setLength(length);
                // 将ileInfo对象传送给Handler
                Message msg = Message.obtain();
                msg.obj = mFileInfo;
                msg.what = MSG_INIT;
                mHandler.sendMessage(msg);
//				msg.setTarget(mHandler);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }
}
