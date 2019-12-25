package com.example.myapplication.ui.slideshow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.MainActivity;
import com.example.myapplication.MqttHelp;
import com.example.myapplication.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Timeline.Period;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Objects;

public class SlideshowFragment extends Fragment implements View.OnClickListener{

    private SlideshowViewModel slideshowViewModel;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private Button left;
    private Button right;
    private Button forward;
    private Button stop;
    private Switch auto;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private MqttHelp mqttHelp;
    private Timeline.Period mPeriod = new Period();
    private static final String DEFAULT_STREAM_URL =
            "https://storage.googleapis.com/testtopbox-public/video_content/bbb/master.m3u8";
    private static final String APP_LOG_TAG = "ImaDaiExample";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        forward = (Button) root.findViewById(R.id.forward);
        stop = (Button) root.findViewById(R.id.stop);
        right = (Button) root.findViewById(R.id.right);
        left = (Button) root.findViewById(R.id.left);
        auto = (Switch) root.findViewById(R.id.auto);
        forward.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        stop.setOnClickListener(this);
        auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("MQTT","check");
                    mqttHelp.publish("robot/move","Mode : 0");
                }
                else {
                    Log.d("MQTT","uncheck");
                    mqttHelp.publish("robot/move","Mode : 1");

                }
            }
        });
        mqttHelp = ((MainActivity)getContext()).getMQTT();
        //mqttHelp.Subscribe("move/+",0);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.forward:
                Log.d("MQTT","forward");
                mqttHelp.publish("robot/move","Move : 1");
                break;
            case R.id.left:
                Log.d("MQTT","left");
                mqttHelp.publish("robot/move","Move : 3");
                break;
            case R.id.right:
                Log.d("MQTT","right");
                mqttHelp.publish("robot/move","Move : 2");
                break;
            case R.id.stop:
                Log.d("MQTT","stop");
                mqttHelp.publish("robot/move","Move : 0");
                break;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerView = Objects.requireNonNull(getActivity()).findViewById(R.id.video_view);

    }

    private void initializePlayer() {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        DefaultTrackSelector.Parameters params =
                new DefaultTrackSelector.ParametersBuilder().setPreferredTextLanguage("en").build();
        trackSelector.setParameters(params);

        player = ExoPlayerFactory.newSimpleInstance(getContext(),
                trackSelector, new DefaultLoadControl());
        playerView.setPlayer(player);
        Uri uri = Uri.parse("https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8");
        MediaSource mediaSource = buildMediaSource(uri);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), "exoplayer-codelab");
        int type = Util.inferContentType(uri, null);
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        return mediaSource;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }


    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
        auto.setChecked(true);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    public long getCurrentPositionPeriod() {
        // Adjust position to be relative to start of period rather than window, to account for DVR
        // window.
        long position = player.getCurrentPosition();
        Timeline currentTimeline = player.getCurrentTimeline();
        if (!currentTimeline.isEmpty()) {
            position -= currentTimeline.getPeriod(player.getCurrentPeriodIndex(), mPeriod)
                    .getPositionInWindowMs();
        }
        return position;
    }

    public long getDuration() {
        return player.getDuration();
    }
    }