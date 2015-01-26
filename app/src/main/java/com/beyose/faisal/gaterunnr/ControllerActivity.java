package com.beyose.faisal.gaterunnr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class ControllerActivity extends ActionBarActivity {

    protected BluetoothAdapter ba;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket sock;
    private OutputStream bt_stream;
    private BluetoothDevice device_to_connect;

    private byte[] OPEN_SIGNAL;
    private byte[] CLOSE_SIGNAL;
    private byte[] STOP_SIGNAL;

    int REQUEST_ENABLE_BT = 0;

    boolean bluetooth_denied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        device_to_connect = null;
        bt_stream = null;
        sock = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        getSupportActionBar().hide();
        Typeface roboto_thin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        Button open_button = (Button)findViewById(R.id.open_button);
        Button close_button = (Button)findViewById(R.id.close_button);
        Button stop_button = (Button)findViewById(R.id.stop);

        open_button.setTypeface(roboto_thin);
        close_button.setTypeface(roboto_thin);
        stop_button.setTypeface(roboto_thin);

        TextView header = (TextView)findViewById(R.id.header);
        TextView sub_header = (TextView)findViewById(R.id.sub_header);
        Typeface pirulen = Typeface.createFromAsset(getAssets(), "fonts/pirulen.ttf");
        header.setTypeface(pirulen);
        sub_header.setTypeface(pirulen);


        OPEN_SIGNAL = "o".getBytes();
        CLOSE_SIGNAL = "c".getBytes();
        STOP_SIGNAL = "s".getBytes();


    }


    @Override
    public void onResume() {
        super.onResume();
        ba = BluetoothAdapter.getDefaultAdapter();

        if (ba == null) {
            //Device not support bluetooth
            Toast.makeText(getApplication(), "Device not support bluetooth", Toast.LENGTH_LONG).show();
        }

        if (!ba.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if (!bluetooth_denied)
                setupConnection();

        }

    }

    public void setupConnection() {

        pairedDevices = ba.getBondedDevices();
        for(BluetoothDevice bt: pairedDevices) {
            if(bt.getName().equals("linvor")) {
                device_to_connect = bt;
                System.out.println("Linvor found");
                break;

            }
        }
        if (device_to_connect != null) {
            try {
                sock = device_to_connect.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Toast.makeText(getApplication(), "Socket cannot create", Toast.LENGTH_LONG).show();
                System.out.println("Socket creation failed");
                sock = null;
                //sock cannot create
            }
            if (sock != null) {
                ba.cancelDiscovery();
                try {
                    sock.connect();
                    System.out.println("Socket connection established");
                    bt_stream = sock.getOutputStream();
                } catch (IOException e) {
                    System.out.println("Socket connection cannot create");
                    Toast.makeText(getApplication(), "blue tooth connot connect", Toast.LENGTH_LONG).show();
                    System.out.println(e.toString());
                    //hello
                }

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {

                setupConnection();

            }
            if(resultCode == RESULT_CANCELED) {
                bluetooth_denied = true;
                Toast.makeText(getApplication(), "Please enable bluetooth", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    @Override
    public void onPause() {
        super.onStop();
        if (sock != null) {
            if (sock.isConnected()) {
                try {
                    sock.close();

                } catch (IOException e) {
                    System.out.println("Socket cannot close " + e.toString());
                }
            }
            bt_stream = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (sock != null) {
            if (sock.isConnected()) {
                try {
                    sock.close();

                } catch (IOException e) {
                    System.out.println("Socket cannot close " + e.toString());
                }
            }
            bt_stream = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controller, menu);
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

    public void openClicked(View v) {
        if (sock != null) {
            if(sock.isConnected()) {
                try {
                    bt_stream.write(OPEN_SIGNAL);
                    System.out.println("Open signal send");

                } catch (IOException e) {
                    System.out.println("IO error in socket " + e.toString());

                }
            }
        }
        //Toast.makeText(getApplication(), "Open Clicked", Toast.LENGTH_LONG).show();

    }

    public void closeClicked(View v) {
        if (sock != null) {
            if(sock.isConnected()) {
                try {
                    bt_stream.write(CLOSE_SIGNAL);

                } catch (IOException e) {
                    System.out.println("IO error in socket " + e.toString());

                }
            }
        }
    }

    public void onStopSignal(View v) {
        if (sock != null) {
            if(sock.isConnected()) {
                try {
                    bt_stream.write(STOP_SIGNAL);

                } catch (IOException e) {
                    System.out.println("IO error in socket " + e.toString());

                }
            }
        }
    }

}
