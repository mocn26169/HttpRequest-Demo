package com.bourne.httprequest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bourne.httprequest.singleThreadDownload.FileInfo;
import com.bourne.httprequest.singleThreadDownload.SingleThreadDownloadActivity;

public class MainActivity extends AppCompatActivity {

    private String urlstr = "http://www.imooc.com/mobile/imooc.apk";
    private FileInfo fileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SingleThreadDownload(null);

    }

    public void simpleHttp(View view) {
        Intent intent = new Intent(this, SimpleHttpActivity.class);
        startActivity(intent);
    }
    public void SingleThreadDownload(View view) {
        Intent intent = new Intent(this, SingleThreadDownloadActivity.class);
        startActivity(intent);
    }


}
