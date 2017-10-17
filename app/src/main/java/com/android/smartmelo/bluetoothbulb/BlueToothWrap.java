package com.android.smartmelo.bluetoothbulb;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by duyanfeng on 2017/10/17.
 * 蓝牙帮助类
 */
public class BlueToothWrap {

    public static final String TAG = BlueToothWrap.class.getSimpleName();

    private static BlueToothWrap sBlueToothWrap;
    private Context mContext;

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;

    //特征值
    BluetoothGattCharacteristic characteristic = null;

    private int STOP_LESCAN = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    stopScan();
                    break;
            }
        }
    };

    private BlueToothWrap() {

    }

    public static BlueToothWrap getInstance() {
        if (sBlueToothWrap == null) {
            sBlueToothWrap = new BlueToothWrap();
        }

        return sBlueToothWrap;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initialize(Context context) {
        mContext = context;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }


    public void startScan() {
        Log.i("tag", "startScan()");
        mBluetoothAdapter.startLeScan(mLeScanCallback); //开始搜索
        mHandler.sendEmptyMessageDelayed(STOP_LESCAN, 20000);  //这个搜索2x0秒，如果搜索不到则停止搜

    }

    public void stopScan() {
        Log.i("tag", "stopScan()");
        mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜索
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            // //在这里可通过device这个对象来获取到搜索到的ble设备名称和一些相关信息
            Log.i("tag", "onLeScan() Address------>" + device.getAddress());
            device.getName();
            device.getAddress();

            if (device.getName() == null) {
                return;
            }
            if (device.getAddress().contains("12:57:14:22:16:9B")) {
                //判断是否搜索到你需要的ble设备
                Log.i(TAG, "onLeScan() DeviceAddress------>" + device.getAddress());
                mBluetoothDevice = device;   //获取到周边设备
                stopScan();
                //1、当找到对应的设备后，立即停止扫描；
                // 2、不要循环搜索设备，为每次搜索设置适合的时间限制。
                // 避免设备不在可用范围的时候持续不停扫描，消耗电量。
                connect();  //连接
            }
        }
    };

    //建立链接
    public boolean connect() {
        if (mBluetoothDevice == null) {
            Log.i(TAG, "BluetoothDevice is null.");
            return false;
        }

        //两个设备通过BLE通信，首先需要建立GATT连接。这里我们讲的是Android设备作为client端，连接GATT Server

        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);  //mGattCallback为回调接口

        if (mBluetoothGatt != null) {

            if (mBluetoothGatt.connect()) {
                Log.d(TAG, "Connect succeed.");
                return true;
            } else {
                Log.d(TAG, "Connect fail.");
                return false;
            }
        } else {
            Log.d(TAG, "BluetoothGatt null.");
            return false;
        }
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //蓝牙链接成功
                Log.i("tag", "链接成功");
                // 链接成功,寻找设备中的服务
                mBluetoothGatt.discoverServices();
                Log.i(TAG, "Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //连接失败
                if (mBluetoothDevice != null) {
                    Log.i(TAG, "重新连接");
                    connect();
                } else {
                    Log.i(TAG, "Disconnected from GATT server.");
                }
            }
        }



        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ///找到服务
                BluetoothGattService service = gatt.getService(UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb"));
                //得到特征值
                characteristic = service.getCharacteristic(UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb"));

            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            // we got response regarding our request to fetch characteristic value
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // and it success, so we can get the value
//                getCharacteristicValue(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            // characteristic's value was updated due to enabled notification, lets get this value
//            // the value itself will be reported to the UI inside getCharacteristicValue
//            getCharacteristicValue(characteristic);
//            // also, notify UI that notification are enabled for particular characteristic
//            mUiCallback.uiGotNotification(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            String deviceName = gatt.getDevice().getName();
//            String serviceName = BleNamesResolver.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
//            String charName = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
//            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + charName;
//
//            // we got response regarding our request to write new value to the characteristic
//            // let see if it failed or not
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                mUiCallback.uiSuccessfulWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description);
//            } else {
//                mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description + " STATUS = " + status);
//            }
        }

        ;

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // we got new value of RSSI of the connection, pass it to the UI
//                mUiCallback.uiNewRssiAvailable(mBluetoothGatt, mBluetoothDevice, rssi);
            }
        }

    };

    //写入命令
    public void writeDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) return;

        // first set it locally....
        ch.setValue(dataToWrite);
        // ... and then "commit" changes to the peripheral
        mBluetoothGatt.writeCharacteristic(ch);
    }

    //设备控制
    public void lightControl(String order) {
        if (characteristic!=null){
            //写入命令
            String newValue =  order.toLowerCase(Locale.getDefault());
            Log.i("tag",newValue);
            byte[] dataToWrite = parseHexStringToBytes(newValue);
            characteristic.setValue(dataToWrite);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    //转化为字节数组
    public byte[] parseHexStringToBytes(final String hex) {
//		String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        String tmp=hex;
        Log.i("tag",tmp );
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally

        String part = "";

        for(int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i*2, i*2+2);
            bytes[i] = Long.decode(part).byteValue();
        }

        return bytes;
    }
}

