package com.htc.mybletool;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lidong_Zhou on 2017/8/11.
 */
public class BluetoothLeClass extends Activity {
    private final static String TAG = "BluetoothLeClass";
    private Activity  mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private SimpleAdapter adapter;
    private List<HashMap<String, Object>> data;
    private HashMap<String, Object> item;
    private ListView  mBleListView;

    public String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    private Handler mHandler;

    private final static int REQUEST_CODE = 1;

    public BluetoothLeClass(Activity mContext, Handler mHandler) {
        this.mContext = mContext;
        this.mHandler = mHandler;
    }

    public void initBluetoothLeClass(Activity mContext) {
        this.mContext = mContext;
    }

    public BluetoothAdapter getluetoothAdapter() {

        return  mBluetoothAdapter;
    }

    public  void initBleScanList(ListView  mBleListView){
        this.mBleListView = mBleListView;
        data = new ArrayList<HashMap<String,Object>>();
        adapter = new SimpleAdapter(mContext, data, R.layout.blescanlistviewitem,
                new String[]{"name", "address","uuid"}, new int[]{R.id.remote_name, R.id.remote_address,R.id.remote_uuid});
        mBleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG,  " "+data.get(i).get("name"));
                stopScan();
                Message msg = new Message();
                msg.what = 1;
                Bundle mBundle = new Bundle();
                mBundle.putString("address" ,data.get(i).get("address").toString());
                msg.setData(mBundle);
                mHandler.sendMessage(msg);
               // connectBleDevice(data.get(i).get("address").toString());
            }
        });
    }

    /************************************* enable bluetooth start    **********************************/
    public  void enableBluetooth() {

        if (!mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "你的手机不支持BLE", Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Log.e(TAG, "initialize Bluetooth, has BLE system");
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, "你的手机不支持BLE",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.e(TAG,  "mBluetoothAdapter = " + mBluetoothAdapter);
        }

        // 打开蓝牙
        mBluetoothAdapter.enable();
        Log.e(TAG,  "mBluetoothAdapter.enable");



    }
    /************************************* enable bluetooth end  **********************************/

    /************************************* start scan            **********************************/

    public void statScan() {
        if (mBluetoothAdapter != null) {

            BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner != null)
                mBluetoothLeScanner.startScan(mLecallback);
        }
    }
    public void stopScan() {
        if (mBluetoothAdapter != null) {

            BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner != null)
                mBluetoothLeScanner.stopScan(mLecallback);
        }
    }


    public ScanCallback mLecallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e(TAG, "onScanResult"+result.getDevice().getAddress()
                    +"  "+result.getDevice().getName()
                    +" "+result.getDevice().getUuids());
            for(HashMap<String, Object> mItem : data){
                if (mItem.get("address").equals(result.getDevice().getAddress()))
                    return;

            }

            item = new HashMap<String, Object>();
            item.put("name",result.getDevice().getName());
            item.put("address",result.getDevice().getAddress());
            item.put("uuid",result.getDevice().getUuids());
            data.add(item);
            mBleListView.setAdapter(adapter);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


/****************************************end  scan**********************************************/

}
