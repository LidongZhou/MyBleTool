package com.htc.mybletool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ListView;



public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_CODE = 1;

    private final static String TAG = "MainActivity";
    private ListView  bleListView;

    private BluetoothLeClass mBluetoothClass;
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

                case 1:
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, BleUartActivity.class);
                    Log.e(TAG, msg.getData().getString("address"));

                    intent.putExtra("address", msg.getData().getString("address"));
                    startActivityForResult(intent, REQUEST_CODE);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Init
        bleListView=(ListView)this.findViewById(R.id.blescanlistview);

        mBluetoothClass = new BluetoothLeClass(MainActivity.this, mHandler);
        mBluetoothClass.initBleScanList(bleListView);
        //


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                mBluetoothClass.statScan();
            }
        });


        //Enable bluetooth
        mBluetoothClass.enableBluetooth();

        //ChemPermission
        CheckPermission checkPermission = new CheckPermission(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
