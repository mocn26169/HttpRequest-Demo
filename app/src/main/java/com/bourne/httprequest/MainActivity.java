package com.bourne.httprequest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bourne.httprequest.multiThreadDownload.MultiThreadActivity;
import com.bourne.httprequest.singleThreadDownload.SingleThreadDownloadActivity;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiThreadDownload(null);

    }

    public void simpleHttp(View view) {
        Intent intent = new Intent(this, SimpleHttpActivity.class);
        startActivity(intent);
    }
    public void SingleThreadDownload(View view) {
        Intent intent = new Intent(this, SingleThreadDownloadActivity.class);
        startActivity(intent);
    }
    public void MultiThreadDownload(View view) {
        Intent intent = new Intent(this, MultiThreadActivity.class);
        startActivity(intent);
    }

}
