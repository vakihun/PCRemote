package com.vaki.pcremoteclient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import mehdi.sakout.fancybuttons.FancyButton;

public class VolumeControlFragment extends Fragment implements View.OnClickListener {
    View rootView;
    FancyButton btn_up,btn_down,btn_mute;

    public VolumeControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_volume_control, container, false);
        btn_up = rootView.findViewById(R.id.btn_up);
        btn_up.setOnClickListener(this);
        btn_down = rootView.findViewById(R.id.btn_down);
        btn_down.setOnClickListener(this);
        btn_mute =  rootView.findViewById(R.id.btn_mute);
        btn_mute.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View ve) {
        FancyButton v = (FancyButton) ve;
        JSONObject obj = new JSONObject();
        try {
            switch (v.getId())
            {
                case R.id.btn_up:
                    obj.put("a", "vu");
                    ((MainActivity) getActivity()).mTcpClient.sendMessage(obj.toString());
                    break;
                case R.id.btn_down:
                    obj.put("a", "vd");
                    ((MainActivity) getActivity()).mTcpClient.sendMessage(obj.toString());
                    break;
                case R.id.btn_mute:
                    obj.put("a", "vm");
                    ((MainActivity) getActivity()).mTcpClient.sendMessage(obj.toString());
                    break;
            }
        } catch (JSONException e) {
        }
    }
}