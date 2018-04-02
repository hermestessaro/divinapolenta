package br.com.httpfluidobjects.appdivinapolenta;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hermestessaro on 02/04/2018.
 */

public class BluetoothManager extends Application {

    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice mmDevice;
    public BluetoothSocket mmSocket;
    public OutputStream mmOutputStream;
    public InputStream mmInputStream;

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    public BluetoothDevice getMmDevice() {
        return mmDevice;
    }

    public void setMmDevice(BluetoothDevice mmDevice) {
        this.mmDevice = mmDevice;
    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }

    public void setMmSocket(BluetoothSocket mmSocket) {
        this.mmSocket = mmSocket;
    }

    public OutputStream getMmOutputStream() {
        return mmOutputStream;
    }

    public void setMmOutputStream(OutputStream mmOutputStream) {
        this.mmOutputStream = mmOutputStream;
    }

    public InputStream getMmInputStream() {
        return mmInputStream;
    }

    public void setMmInputStream(InputStream mmInputStream) {
        this.mmInputStream = mmInputStream;
    }
}
