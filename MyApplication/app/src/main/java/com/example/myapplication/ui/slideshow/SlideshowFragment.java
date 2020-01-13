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
import android.widget.ImageView;
import android.widget.Switch;
import android.util.Log;
import android.util.Base64;
import android.widget.Toast;
import android.graphics.BitmapFactory;
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
    String payload;
    JSONObject jsonMsg;
    private Button left;
    private Button right;
    private Button forward;
    private Button stop;
    private Switch auto;
    private Switch flash;
    private ImageView image;

    private MqttHelp mqttHelp;



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
        flash = (Switch) root.findViewById(R.id.flash);
        image = (ImageView) root.findViewById(R.id.imageView);
        forward.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        stop.setOnClickListener(this);
        flash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("MQTT","flash 1");
                    mqttHelp.publish("robot/move","flash 1");
                }
                else {
                    Log.d("MQTT","flash 0");
                    mqttHelp.publish("robot/move","flash 0");

                }
            }
        });
        auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("MQTT","check");
                    mqttHelp.publish("robot/move","manuel 1");
                }
                else {
                    Log.d("MQTT","uncheck");
                    mqttHelp.publish("robot/move","manuel 0");

                }
            }
        });
        mqttHelp = ((MainActivity)getContext()).getMQTT();
        mqttHelp.Subscribe("sensor/+",0);
        mqttHelp.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                payload = new String(mqttMessage.getPayload());
                jsonMsg = new JSONObject(payload);
                byte[] decodedString = Base64.decode(jsonMsg.getString("image"), Base64.DEFAULT);
                image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        return root;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.forward:
                Log.d("MQTT","forward");
                mqttHelp.publish("robot/move","Move 1");
                break;
            case R.id.left:
                Log.d("MQTT","left");
                mqttHelp.publish("robot/move","Move 3");
                break;
            case R.id.right:
                Log.d("MQTT","right");
                mqttHelp.publish("robot/move","Move 2");
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


    }


    @Override
    public void onStop() {
        super.onStop();

        auto.setChecked(true);
    }

    }