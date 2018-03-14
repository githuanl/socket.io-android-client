package com.centersoft.chat;

import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

import com.centersoft.base.BaseActivity;
import com.centersoft.webrtcclient.PeerConnectionParameters;
import com.centersoft.webrtcclient.WebRtcClient;

import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import butterknife.BindView;

//聊天界面
public class VideoActy extends BaseActivity implements WebRtcClient.RtcListener {


    @Override
    public int initResource() {
        return R.layout.acty_vedio;
    }


    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;

    @BindView(R.id.glview_call)
    GLSurfaceView glview_call;

    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private WebRtcClient client;



    @Override
    protected void initData() {


        glview_call.setPreserveEGLContextOnPause(true);
        glview_call.setKeepScreenOn(true);
        VideoRendererGui.setView(glview_call, new Runnable() {
            @Override
            public void run() {
                initWebRtc();
            }

        });

        // local and remote render
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

    }


    private void initWebRtc() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);

        Bundle bl = getIntent().getExtras();
        client = new WebRtcClient(this, bl,params, VideoRendererGui.getEGLContext());
    }


    @Override
    public void onPause() {
        super.onPause();
        glview_call.onPause();
        if (client != null) {
            client.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        glview_call.onResume();
        if (client != null) {
            client.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onCallReady(final String callId) {
        client.start();
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
//                scalingType);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
//        VideoRendererGui.update(remoteRender,
//                REMOTE_X, REMOTE_Y,
//                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);
//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
//                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
//                scalingType);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
//                scalingType);
    }

}
