package com.example.myapplication.ui.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

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
import android.widget.ArrayAdapter;
import android.widget.AdapterView;



public class GalleryFragment extends Fragment{

    private GalleryViewModel galleryViewModel;
    String payload;
    JSONObject jsonMsg;
    MqttHelp mqttHelp;
    TubeSpeedometer tubeSpeedometer;
    TubeSpeedometer tubeSpeedometer2;
    ChartHelper mChart;
    Spinner spinGaz;
    Spinner spinAlcool;
    String selectAl;
    String selectGaz;
    LineChart chart;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        mqttHelp = ((MainActivity)getContext()).getMQTT();
        mqttHelp.Subscribe("sensor/+",0);
        spinGaz = (Spinner) root.findViewById(R.id.spinGaz);
        spinAlcool = (Spinner) root.findViewById(R.id.spinAl);
        spinAlcool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        selectAl = "hydrogene";
                        break;

                    case 1:
                        selectAl = "ethanol";
                        break;

                    case 2:
                        selectAl = "butane";
                        break;

                    default:
                        selectAl = "hydrogene";
                        break;

                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinGaz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectGaz = "smoke";
                        break;

                    case 1:
                        selectGaz = "lgp";
                        break;

                    case 2:
                        selectGaz = "co";
                        break;

                    default:
                        selectGaz = "smoke";
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

                tubeSpeedometer.speedTo(Float.parseFloat(jsonMsg.getString(selectGaz)),0);
                tubeSpeedometer2.speedTo(Float.parseFloat(jsonMsg.getString(selectAl)),0);
                mChart.addEntry(Float.parseFloat(jsonMsg.getString(selectAl)),Float.parseFloat(jsonMsg.getString(selectGaz)));
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