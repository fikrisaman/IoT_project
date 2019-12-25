package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.MainActivity;
import com.example.myapplication.MqttHelp;
import com.example.myapplication.R;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private EditText editPasswd;
    private EditText editLogin;
    private Button login;
    private Button cancel;
    private MqttHelp mqttHelp;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mqttHelp = ((MainActivity)getContext()).getMQTT();
        login = (Button) root.findViewById(R.id.login);
        cancel = (Button) root.findViewById(R.id.cancel);
        editPasswd = (EditText) root.findViewById(R.id.editpasswd);
        editLogin = (EditText) root.findViewById(R.id.edituser);
        login.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return root;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                Log.d("MQTT", "Cancel");
                editLogin.setText("");
                editPasswd.setText("");
                break;
            case R.id.login:
                Log.d("MQTT", "Login");
                mqttHelp.setLogin(editLogin.getText().toString(),editPasswd.getText().toString());
                try {
                    mqttHelp.connect();
                }
                catch (Exception e){
                    Log.d("MQTT","FAIL");
                }

                break;
        }
    }
}