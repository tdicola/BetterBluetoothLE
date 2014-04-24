package betterbluetoothle.services;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.UUID;

import betterbluetoothle.async.AsyncBluetoothGatt;

public class UART {

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private AsyncBluetoothGatt gatt;
    private BluetoothGattCharacteristic rx;
    private BluetoothGattCharacteristic tx;
    // TODO: This deque of bytes is not efficient for storing the buffer of received data.
    private ArrayDeque<Byte> received;

    public UART(BluetoothDevice device, Context context, boolean autoConnect, boolean runOnUiThread) {
        gatt = new AsyncBluetoothGatt(device, context, autoConnect);
        received = new ArrayDeque<Byte>();
    }

    public static void findFirstDevice() {

    }

    // Return promise that will be resolved when the UART device is connected and ready for communication.
    public void connect(final Runnable onConnected, final Runnable onDisconnected, final Runnable onDataReceived) {
        // Connect to the device.
        gatt.connect().then(new DonePipe<Void, Void, Integer, Void>() {
            @Override
            public Promise<Void, Integer, Void> pipeDone(Void result) {
                // Connected, start service discovery.
                return gatt.discoverServices();
            }
        // Switch to promise for service discovery completion.
        }).then(new DonePipe<Void, BluetoothGattDescriptor, Integer, Void>() {
            @Override
            public Promise<BluetoothGattDescriptor, Integer, Void> pipeDone(Void result) {
                // Service discovery complete, grab reference to TX and RX services.
                rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
                tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
                // Notify that device is connected.
                if (onConnected != null) {
                    onConnected.run();
                }
                // Now setup notifications for RX characteristic changes.
                // First change the client descriptor to enable notifications and write it to the device.
                BluetoothGattDescriptor client = rx.getDescriptor(CLIENT_UUID);
                client.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                return gatt.writeDescriptor(client);
            }
            // Switch to promise for RX client descriptor update.
        }).then(new DonePipe<BluetoothGattDescriptor, Void, Void, BluetoothGattCharacteristic>() {
            @Override
            public Promise<Void, Void, BluetoothGattCharacteristic> pipeDone(BluetoothGattDescriptor result) {
                // Finally enable notifications on RX characteristic changes locally.
                return gatt.setCharacteristicNotification(rx, true);
            }
        // Switch to promise for RX characteristic updates (i.e. data received).
        }).progress(new ProgressCallback<BluetoothGattCharacteristic>() {
            @Override
            public void onProgress(BluetoothGattCharacteristic progress) {
                // Update buffer of received bytes.
                dataReceived(progress);
                // Notify data is available for reading.
                if (onDataReceived != null) {
                    onDataReceived.run();
                }
            }
        });
        gatt.disconnected().done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void result) {
                    if (onDisconnected != null) {
                        onDisconnected.run();
                    }
                }
            }
        );
    }

    private synchronized void dataReceived(BluetoothGattCharacteristic rx) {
        for (byte b : rx.getValue()) {
            received.push(b);
        }
    }

    public synchronized int available() {
        return received.size();
    }

    public void disconnect() {
        gatt.disconnect();
    }

    public void write(byte[] data) {
        if (tx == null) {
            return;
        }
        tx.setValue(data);
        gatt.writeCharacteristic(tx);
    }

    public void write(String data) {
        write(data.getBytes(Charset.forName("UTF-8")));
    }

    public synchronized byte[] read(int count) {
        int size = count < received.size() ? count : received.size();
        byte[] result = new byte[size];
        for (int i = 0; i < size; ++i) {
            result[i] = received.pop();
        }
        return result;
    }
}
