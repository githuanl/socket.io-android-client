package com.centersoft.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import com.blankj.utilcode.util.FileUtils;
import com.centersoft.base.BaseActivity;
import com.centersoft.util.MyLog;
import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;

/**
 * Created by fengli on 2018/3/2.
 */

public class CameraActy extends BaseActivity {


    @BindView(R.id.jcameraview)
    JCameraView jCameraView;

    @Override
    protected int initResource() {
        return R.layout.acty_camera;
    }

    /*
   * 图片保存路径
   * */
    public  String imageSavePath() {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/images/";
        FileUtils.createOrExistsDir(path);
        return path;
    }

    /*
    * 音频保存路径
    * */
    public static String audioSavePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audios/";
        FileUtils.createOrExistsDir(path);
        return path;
    }

    public static String videoSavePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videos/";
        FileUtils.createOrExistsDir(path);
        return path;
    }

    @Override
    protected void initData() {

        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }

        jCameraView.setSaveVideoPath(videoSavePath());
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {

                MyLog.i("打开camera失败");
            }

            @Override
            public void AudioPermissionError() {

                MyLog.i("没有权限");
            }
        });

        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {

                sendImage(bitmap);
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {

                MyLog.i("获取到视频");
            }
        });

        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {

                goBack();
            }
        });

        jCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {

                MyLog.i("保存图片");
            }
        });

    }

    private void sendImage(Bitmap bitmap) {

        // 保存图片到本地
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] buffer = stream.toByteArray();

        String fileName = UUID.randomUUID() + ".jpg";

        String savePath = imageSavePath() + fileName;

        File file = new File(savePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.putExtra("imageName", fileName);
        intent.putExtra("imageWidth", bitmap.getWidth());
        intent.putExtra("imageHeight", bitmap.getHeight());
        intent.putExtra("imagePath", savePath);
        setResult(102, intent);
        goBack();
    }


}
