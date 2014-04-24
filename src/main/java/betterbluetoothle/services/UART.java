package betterbluetoothle.services;

import android.bluetooth.BluetoothDevice;

public class UART {

    private BluetoothDevice device;

    public UART(BluetoothDevice device) {
        this.device = device;
    }

    public int available() {
        return 0;
    }

    public byte read() {
        return 0;
    }

    public void write(byte[] data) {

    }

}
