package com.example.myapplication.ui.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.ChartHelper;
import com.example.myapplication.MainActivity;
import com.example.myapplication.MqttHelp;
import com.example.myapplication.R;
import com.github.anastr.speedviewlib.TubeSpeedometer;
import com.github.mikephil.charting.charts.LineChart;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import java.util.Objects;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    String payload;
    JSONObject jsonMsg;
    MqttHelp mqttHelp;
    TubeSpeedometer tubeSpeedometer;
    TubeSpeedometer tubeSpeedometer2;
    ChartHelper mChart;
    LineChart chart;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
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
                Log.d("mqtt", payload);

                    tubeSpeedometer.speedTo(Float.parseFloat(jsonMsg.getString("alcool")),0);
                tubeSpeedometer2.speedTo(Float.parseFloat(jsonMsg.getString("gaz")),0);
                mChart.addEntry(Float.parseFloat(jsonMsg.getString("alcool")),Float.parseFloat(jsonMsg.getString("gaz")));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chart = Objects.requireNonNull(getActivity()).findViewById(R.id.chart);
        mChart = new ChartHelper(chart);
        tubeSpeedometer = Objects.requireNonNull(getActivity()).findViewById(R.id.tubeSpeedometer);
        tubeSpeedometer.setMaxSpeed(10000);
        tubeSpeedometer.speedTo(100,0);
        tubeSpeedometer2 = Objects.requireNonNull(getActivity()).findViewById(R.id.tubeSpeedometer1);
        tubeSpeedometer2.setMaxSpeed(10000);
        tubeSpeedometer2.speedTo(320,0);
    }
}