package com.bourne.httprequest.multiThreadDownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.bourne.httprequest.FileInfo;
import com.bourne.httprequest.R;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadActivity extends AppCompatActivity {

    private ListView listView;
    private List<FileInfo> mFileList;
    private FileAdapter mAdapter;

    private String urlone = "http://dldir1.qq.com/qqmi/TencentVideo_V5.5.2.11955_848.apk";
    private String urltwo = "http://file.ws.126.net/opencourse/netease_open_androidphone.apk";
    private String urlthree = "http://downloads.jianshu.io/apps/haruki/JianShu-2.2.3-17040111.apk";
    private String urlfour = "http://codown.youdao.com/note/youdaonote_android_5.9.1_youdaoweb.apk";


    private UIRecive mRecive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_thread);

        // 初始化控件
        listView = (ListView) findViewById(R.id.list_view);
        mFileList = new ArrayList<FileInfo>();

        // 初始化文件对象
        FileInfo fileInfo1 = new FileInfo(0, urlone, getfileName(urlone), 0, 0);
        FileInfo fileInfo2 = new FileInfo(1, urltwo, getfileName(urltwo), 0, 0);
        FileInfo fileInfo3 = new FileInfo(2, urlthree, getfileName(urlthree), 0, 0);
        FileInfo fileInfo4 = new FileInfo(3, urlfour, getfileName(urlfour), 0, 0);

        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mFileList.add(fileInfo4);

        mAdapter = new FileAdapter(this, mFileList);

        listView.setAdapter(mAdapter);

        mRecive = new UIRecive();

        //创建Receiver来接收下载进度，然后更新adapter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        intentFilter.addAction(DownloadService.ACTION_FINISHED);
        intentFilter.addAction(DownloadService.ACTION_START);
        registerReceiver(mRecive, intentFilter);
    }



    @Override
    protected void onDestroy() {
        unregisterReceiver(mRecive);
        super.onDestroy();
    }
    // 从URL地址中获取文件名，即/后面的字符
    private String getfileName(String url) {

        return url.substring(url.lastIndexOf("/") + 1);
    }

    // 从DownloadTadk中获取广播信息，更新进度条
    class UIRecive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                // 更新进度条的时候
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updataProgress(id, finished);

            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())){
                // 下载结束的时候
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                mAdapter.updataProgress(fileInfo.getId(), 0);
                Toast.makeText(MultiThreadActivity.this, mFileList.get(fileInfo.getId()).getFileName() + "下载完毕", Toast.LENGTH_SHORT).show();

            } else if (DownloadService.ACTION_START.equals(intent.getAction())){

            }
        }

    }

}
