package com.bourne.httprequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



/**
 * HttpURLConnection分别实现图片，文本，文件的请求
 */
public class SimpleHttpActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG;

    private ImageView imageView;
    private TextView textView;

    private static final int IMAGE_MSG_NEW_PIC = 2;
    private static final int IMAGE_MSG_CACHE_PIC = 1;
    private static final int IMAGE_ERROR = 3;
    private static final int IMAGE_EXCEPTION = 4;

    protected static final int TEXT_SUCCESS = 5;
    protected static final int TEXT_ERROR = 6;
    protected static final int FILE_SUCCESS = 7;

    //1.在主线程里面声明消息处理器 handler
    private Handler handler = new Handler() {
        //处理消息的
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IMAGE_MSG_CACHE_PIC:
                    //3.处理消息 运行在主线程
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(SimpleHttpActivity.this, "使用缓存图", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGE_MSG_NEW_PIC:
                    Bitmap bitmap2 = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap2);
                    Toast.makeText(SimpleHttpActivity.this, "下载图片完毕", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGE_ERROR:
                    Toast.makeText(SimpleHttpActivity.this, "图片请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGE_EXCEPTION:
                    Toast.makeText(SimpleHttpActivity.this, "图片发生异常，请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_ERROR:
                    Toast.makeText(SimpleHttpActivity.this, "文本请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_SUCCESS:
                    String text = (String) msg.obj;
                    textView.setText(text);
                    Toast.makeText(SimpleHttpActivity.this, "文本请求成功", Toast.LENGTH_SHORT).show();
                    break;
                case FILE_SUCCESS:
                    Toast.makeText(SimpleHttpActivity.this, "文件下载完毕", Toast.LENGTH_SHORT).show();
                    break;
            }

            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_http);

        TAG = this.getLocalClassName();

        findViewById(R.id.btn_downloadImage).setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        findViewById(R.id.btn_downloadFile).setOnClickListener(this);
        findViewById(R.id.btn_downloadText).setOnClickListener(this);
        textView = (TextView) findViewById(R.id.textView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_downloadImage:
                downloadBitmap();
                break;
            case R.id.btn_downloadFile:
                downloadFile();
                break;
            case R.id.btn_downloadText:
                downloadText();
                break;
        }
    }

    /**
     * 下载文件
     */
    private void downloadFile() {

        new Thread() {
            @Override
            public void run() {
                String urlStr = "http://dl-cdn.coolapkmarket.com/down/apk_upload/2017/0312/13316bb0a1731665796e1800a5e9e0fa-for-110827-o_1bb14r5991llq19ng3kd145bptfq-uid-704307.apk?_upt=d8a58af21492100572";

                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage());
                    }
                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();
                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream("/sdcard/app.apk");
                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
//                        if (isCancelled()) {
//                            input.close();
//                            return null;
//                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            Log.e(TAG, "" + (int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                        Message msg = new Message();
                        msg.what = FILE_SUCCESS;
                        handler.sendMessage(msg);

                    } catch (IOException ignored) {
                    }
                    if (connection != null)
                        connection.disconnect();
                }

            }
        }.start();
    }

    /**
     * 下载图片
     */
    private void downloadBitmap() {

        final String path = "http://images.csdn.net/20170413/andr1_meitu_1.jpg";

        new Thread() {
            public void run() {

                File file = new File(getCacheDir(), Base64.encodeToString(
                        path.getBytes(), Base64.DEFAULT));

                if (file.exists() && file.length() > 0) {
                    Log.e(TAG, "图片存在，拿缓存");
                    Bitmap bitmap = BitmapFactory.decodeFile(file
                            .getAbsolutePath());

                    Message msg = new Message();//声明消息
                    msg.what = IMAGE_MSG_CACHE_PIC;
                    msg.obj = bitmap;//设置数据
                    handler.sendMessage(msg);//让handler帮我们发送数据
                } else {
                    Log.e(TAG, "图片不存在，获取数据生成缓存");
                    // 通过http请求把图片获取下来。
                    try {
                        // 1.声明访问的路径， url 网络资源 http ftp rtsp
                        URL url = new URL(path);
                        // 2.通过路径得到一个连接 http的连接
                        HttpURLConnection conn = (HttpURLConnection) url
                                .openConnection();
                        // 3.判断服务器给我们返回的状态信息。
                        // 200 成功 302 从定向 404资源没找到 5xx 服务器内部错误
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            InputStream is = conn.getInputStream();// png的图片
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len = -1;
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            is.close();
                            fos.close();
                            Bitmap bitmap = BitmapFactory.decodeFile(file
                                    .getAbsolutePath());
                            //更新ui ，不能写在子线程
                            Message msg = new Message();
                            msg.obj = bitmap;
                            msg.what = IMAGE_MSG_NEW_PIC;
                            handler.sendMessage(msg);

                        } else {
                            // 请求失败
                            //土司更新ui，不能写在子线程
                            //Toast.makeText(this, "请求失败", 0).show();
                            Message msg = new Message();
                            msg.what = IMAGE_ERROR;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //土司不能写在子线程
                        //Toast.makeText(this, "发生异常，请求失败", 0).show();
                        Message msg = new Message();
                        msg.what = IMAGE_EXCEPTION;
                        handler.sendMessage(msg);
                    }
                }
            }


        }.start();
    }

    /**
     * 下载文本
     */
    private void downloadText() {
        final String path = "http://blog.csdn.net/iromkoear";
        //访问网络，把html源文件下载下来
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");//声明请求方式 默认get
                    //conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.3.3; zh-cn; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1 MicroMessenger/6.0.0.57_r870003.501 NetType/internet");
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String result = readStream(is);

                        Message msg = Message.obtain();//减少消息创建的数量
                        msg.obj = result;
                        msg.what = TEXT_SUCCESS;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = Message.obtain();//减少消息创建的数量
                    msg.what = TEXT_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }

            ;
        }.start();
    }

    /**
     * 把输入流的内容转换成字符串
     *
     * @param is
     * @return null解析失败， string读取成功
     */
    public static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            String temptext = new String(baos.toByteArray());
            if (temptext.contains("charset=gb2312")) {//解析meta标签
                return new String(baos.toByteArray(), "gb2312");
            } else {
                return new String(baos.toByteArray(), "utf-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
