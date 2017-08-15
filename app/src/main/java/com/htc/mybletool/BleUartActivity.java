package com.htc.mybletool;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Lidong_Zhou on 2017/8/11.
 */
public class BleUartActivity extends AppCompatActivity{
    private final static String TAG = "BleUartActivity";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothGatt mBluetoothGatt;
    public String mBluetoothDeviceAddress;

    public static String UUID_HERATRATE = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String UUID_TEMPERATURE = "00002a1c-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int MSG_READ_FROM_REMOTE = 1;
    public static final int MSG_READ_REMOTE_RSSI = 2;
    public static final int MSG_SEND_TO_REMOTE = 3;

    public TextView textView;
    public TextView textConnectView;
    public EditText mEditText;

    private BluetoothGattCharacteristic mGattCharacteristic;

    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

                case MSG_READ_FROM_REMOTE:
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "HH:mm:ss ");
                    Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                    String TimeStr = formatter.format(curDate);

                    String DisplayStr = "[" + TimeStr + "][R] " + msg.getData().getString("read_msg")+"\n";
                    textView.append(DisplayStr);

                    break;

                case MSG_READ_REMOTE_RSSI:
                    int rssi = msg.getData().getInt("RSSI");
                 //   if(rssi <0)
                    //    getSupportActionBar().setTitle("RSSI:"+rssi+"");
                    textConnectView.setText("Connected "+"      RSSI:"+rssi);
                    mBluetoothGatt.readRemoteRssi();
                    break;
                case MSG_SEND_TO_REMOTE:
                    SimpleDateFormat formatter_send = new SimpleDateFormat(
                            "HH:mm:ss ");
                    Date curDate_send = new Date(System.currentTimeMillis());// 获取当前时间
                    String TimeStr_send = formatter_send.format(curDate_send);

                    String DisplayStr_send = "[" + TimeStr_send + "][S] " + msg.getData().getString("send_msg")+"\n";
                    textView.append(DisplayStr_send);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_uart);

        textConnectView = (TextView) findViewById(R.id.uart_show_connect_info);
        textConnectView.setTextColor(Color.RED);

        textView = (TextView) findViewById(R.id.uart_show_remote_info);
        textView.setTextColor(Color.GREEN);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String address = bundle.getString("address");
        connectBleDevice(address);

        mEditText = (EditText)findViewById(R.id.button_edit_text);
        Button mButton = (Button)findViewById(R.id.button_send);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mEditText.getText().toString().equals("")) {
                    mGattCharacteristic.setValue(mEditText.getText().toString() + "\n");
                    mBluetoothGatt.writeCharacteristic(mGattCharacteristic);
                    Message msg = new Message();
                    Bundle mBundler = new Bundle();
                    mBundler.putString("send_msg", mEditText.getText().toString());
                    msg.what = MSG_SEND_TO_REMOTE;
                    msg.setData(mBundler);
                    mHandler.sendMessage(msg);

                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    mEditText.setText("");
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /*************************************Connect remote device start**********************************/

    public boolean connectBleDevice(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e(TAG,
                    "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            try {
                Log.e(TAG, "Connected");
                mBluetoothGatt.discoverServices();
                Message msg = new Message();
                msg.what = MSG_READ_REMOTE_RSSI;
                mHandler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }



        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(TAG, "onServicesDiscovered");

            for( BluetoothGattService gattService  :gatt.getServices()){
                Log.e(TAG, gattService.getUuid().toString());
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                        .getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                    Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
                    if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR6)) {
                        mGattCharacteristic = gattCharacteristic;
                        mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                        BluetoothGattDescriptor descriptor = gattCharacteristic
                                .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                        descriptor
                                .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                    }

                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG, "onCharacteristicChanged    "+new String(characteristic.getValue()));
            Message msg = new Message();
            Bundle mBundler = new Bundle();
            mBundler.putString("read_msg",new String(characteristic.getValue()));
            msg.what = MSG_READ_FROM_REMOTE;
            msg.setData(mBundler);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e(TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e(TAG, "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.e(TAG, "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.e(TAG, "onReadRemoteRssi "+rssi);
            Message msg = new Message();
            msg.what = MSG_READ_REMOTE_RSSI;
            Bundle mBundle = new Bundle();
            mBundle.putInt("RSSI", rssi);
            msg.setData(mBundle);
            mHandler.sendMessageDelayed(msg,1000);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e(TAG, "onMtuChanged");
        }
    };
/*************************************Connect remote device end  **********************************/

}
