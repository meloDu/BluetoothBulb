package com.android.smartmelo.bluetoothbulb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.btn_scan)
    Button mBtnScan;
    @Bind(R.id.btn_open)
    Button mBtnOpen;
    @Bind(R.id.btn_close)
    Button mBtnClose;

    BlueToothWrap instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        instance = BlueToothWrap.getInstance();
        boolean b = instance.initialize(this);
        if (b){
            instance.startScan();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }


    @OnClick({R.id.btn_scan, R.id.btn_open, R.id.btn_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                instance.startScan();
                break;
            case R.id.btn_open:
                instance.lightControl("fbf0fa");

                break;
            case R.id.btn_close:
                instance.lightControl("fb0ffa");
                break;
        }
    }




}
