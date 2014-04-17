package betterbluetoothle.tests;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.UUID;

import betterbluetoothle.async.AsyncBluetoothGatt;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AsyncBluetoothGattTest {

    private UUID TEST_UUID1 = UUID.fromString("12345678-0000-1000-8000-00805f9b34fb");
    private UUID TEST_UUID2 = UUID.fromString("87654321-0000-1000-8000-00805f9b34fb");

    private int testRssi;
    private BluetoothGattCharacteristic testCh;

    @Before
    public void setup() {
        testRssi = 0;
        testCh = null;
    }

    @Test
    public void test_connect_immediate_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);

        when(device.connectGatt(context, false, gatt)).thenReturn(null);
        Promise<Void, Integer, Void> connected = gatt.connect();

        assertThat(connected.isPending()).isFalse();
        assertThat(connected.isRejected()).isTrue();
        assertThat(connected.isResolved()).isFalse();
    }

    @Test
    public void test_connect_is_pending_before_update() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);

        Promise<Void, Integer, Void> connected = gatt.connect();
        Promise<Void, Integer, Void> disconnected = gatt.disconnected();

        assertThat(connected.isPending()).isTrue();
        assertThat(connected.isRejected()).isFalse();
        assertThat(connected.isResolved()).isFalse();

        assertThat(disconnected.isPending()).isTrue();
        assertThat(disconnected.isRejected()).isFalse();
        assertThat(disconnected.isResolved()).isFalse();
    }

    @Test
    public void test_connect_success_resolves_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);

        Promise<Void, Integer, Void> connected = gatt.connect();
        Promise<Void, Integer, Void> disconnected = gatt.disconnected();
        gatt.onConnectionStateChange(mockGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_CONNECTED);

        assertThat(connected.isPending()).isFalse();
        assertThat(connected.isRejected()).isFalse();
        assertThat(connected.isResolved()).isTrue();

        assertThat(disconnected.isPending()).isTrue();
        assertThat(disconnected.isRejected()).isFalse();
        assertThat(disconnected.isResolved()).isFalse();
    }

    @Test
    public void test_connect_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);

        Promise<Void, Integer, Void> connected = gatt.connect();
        gatt.onConnectionStateChange(mockGatt, BluetoothGatt.GATT_FAILURE, BluetoothGatt.STATE_CONNECTED);

        assertThat(connected.isPending()).isFalse();
        assertThat(connected.isRejected()).isTrue();
        assertThat(connected.isResolved()).isFalse();
    }

    // TODO: Test reconnect.  Should reconnect close the previous connect/disconnect promise?

    @Test
    public void test_disconnect_resolves_disconnected_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);

        gatt.connect();
        Promise<Void, Integer, Void> disconnected = gatt.disconnected();
        gatt.disconnect();

        assertThat(disconnected.isPending()).isFalse();
        assertThat(disconnected.isRejected()).isFalse();
        assertThat(disconnected.isResolved()).isTrue();
    }

    @Test
    public void test_disconnect_event_resolves_disconnected_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);

        gatt.connect();
        Promise<Void, Integer, Void> disconnected = gatt.disconnected();
        gatt.onConnectionStateChange(mockGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_DISCONNECTED);

        assertThat(disconnected.isPending()).isFalse();
        assertThat(disconnected.isRejected()).isFalse();
        assertThat(disconnected.isResolved()).isTrue();
    }

    @Test
    public void test_close_causes_disconnect() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);

        gatt.connect();
        Promise<Void, Integer, Void> disconnected = gatt.disconnected();
        gatt.close();

        assertThat(disconnected.isPending()).isFalse();
        assertThat(disconnected.isRejected()).isFalse();
        assertThat(disconnected.isResolved()).isTrue();
    }

    @Test
    public void test_discover_service_immediate_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();

        when(mockGatt.discoverServices()).thenReturn(false);
        Promise<Void, Integer, Void> discovered = gatt.discoverServices();

        assertThat(discovered.isPending()).isFalse();
        assertThat(discovered.isRejected()).isTrue();
        assertThat(discovered.isResolved()).isFalse();
    }

    @Test
    public void test_discover_service_is_pending_before_update() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        when(mockGatt.discoverServices()).thenReturn(true);

        Promise<Void, Integer, Void> discovered = gatt.discoverServices();

        assertThat(discovered.isPending()).isTrue();
        assertThat(discovered.isRejected()).isFalse();
        assertThat(discovered.isResolved()).isFalse();
    }

    @Test
    public void test_discover_service_success_resolves_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        when(mockGatt.discoverServices()).thenReturn(true);

        Promise<Void, Integer, Void> discovered = gatt.discoverServices();
        gatt.onServicesDiscovered(mockGatt, BluetoothGatt.GATT_SUCCESS);

        assertThat(discovered.isPending()).isFalse();
        assertThat(discovered.isRejected()).isFalse();
        assertThat(discovered.isResolved()).isTrue();
    }

    @Test
    public void test_discover_service_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        when(mockGatt.discoverServices()).thenReturn(true);

        Promise<Void, Integer, Void> discovered = gatt.discoverServices();
        gatt.onServicesDiscovered(mockGatt, BluetoothGatt.GATT_FAILURE);

        assertThat(discovered.isPending()).isFalse();
        assertThat(discovered.isRejected()).isTrue();
        assertThat(discovered.isResolved()).isFalse();
    }

    @Test
    public void test_read_rssi_immediate_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();

        when(mockGatt.readRemoteRssi()).thenReturn(false);
        Promise<Integer, Integer, Void> readRssi = gatt.readRemoteRssi();

        assertThat(readRssi.isPending()).isFalse();
        assertThat(readRssi.isRejected()).isTrue();
        assertThat(readRssi.isResolved()).isFalse();
    }

    @Test
    public void test_read_rssi_is_pending_before_update() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        when(mockGatt.readRemoteRssi()).thenReturn(true);

        Promise<Integer, Integer, Void> readRssi = gatt.readRemoteRssi();

        assertThat(readRssi.isPending()).isTrue();
        assertThat(readRssi.isRejected()).isFalse();
        assertThat(readRssi.isResolved()).isFalse();
    }

    @Test
    public void test_read_rssi_success_resolves_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        when(mockGatt.readRemoteRssi()).thenReturn(true);

        Promise<Integer, Integer, Void> readRssi = gatt.readRemoteRssi();
        gatt.onReadRemoteRssi(mockGatt, 100, BluetoothGatt.GATT_SUCCESS);

        assertThat(readRssi.isPending()).isFalse();
        assertThat(readRssi.isRejected()).isFalse();
        assertThat(readRssi.isResolved()).isTrue();
        readRssi.done(new DoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                testRssi = result;
            }
        });
        assertThat(testRssi).isEqualTo(100);
    }

    @Test
    public void test_read_rssi_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        when(mockGatt.readRemoteRssi()).thenReturn(true);

        Promise<Integer, Integer, Void> readRssi = gatt.readRemoteRssi();
        gatt.onReadRemoteRssi(mockGatt, 0, BluetoothGatt.GATT_FAILURE);

        assertThat(readRssi.isPending()).isFalse();
        assertThat(readRssi.isRejected()).isTrue();
        assertThat(readRssi.isResolved()).isFalse();
    }

    @Test
    public void test_read_characteristic_immediate_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        BluetoothGattCharacteristic ch = mock(BluetoothGattCharacteristic.class);

        when(mockGatt.readCharacteristic(ch)).thenReturn(false);
        Promise<BluetoothGattCharacteristic, Integer, Void> readCh = gatt.readCharacteristic(ch);

        assertThat(readCh.isPending()).isFalse();
        assertThat(readCh.isRejected()).isTrue();
        assertThat(readCh.isResolved()).isFalse();
    }

    @Test
    public void test_read_characteristic_is_pending_before_update() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        BluetoothGattCharacteristic ch = mock(BluetoothGattCharacteristic.class);
        when(mockGatt.readCharacteristic(ch)).thenReturn(true);
        when(ch.getUuid()).thenReturn(TEST_UUID1);

        Promise<BluetoothGattCharacteristic, Integer, Void> readCh = gatt.readCharacteristic(ch);

        assertThat(readCh.isPending()).isTrue();
        assertThat(readCh.isRejected()).isFalse();
        assertThat(readCh.isResolved()).isFalse();
    }

    @Test
    public void test_read_characteristic_success_resolves_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        BluetoothGattCharacteristic ch = mock(BluetoothGattCharacteristic.class);
        when(mockGatt.readCharacteristic(ch)).thenReturn(true);
        when(ch.getUuid()).thenReturn(TEST_UUID1);

        Promise<BluetoothGattCharacteristic, Integer, Void> readCh = gatt.readCharacteristic(ch);
        gatt.onCharacteristicRead(mockGatt, ch, BluetoothGatt.GATT_SUCCESS);

        assertThat(readCh.isPending()).isFalse();
        assertThat(readCh.isRejected()).isFalse();
        assertThat(readCh.isResolved()).isTrue();
        readCh.done(new DoneCallback<BluetoothGattCharacteristic>() {
            @Override
            public void onDone(BluetoothGattCharacteristic result) {
                testCh = result;
            }
        });
        assertThat(testCh).isEqualTo(ch);
    }

    @Test
    public void test_read_characteristic_failure_rejects_promise() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();
        BluetoothGattCharacteristic ch = mock(BluetoothGattCharacteristic.class);
        when(mockGatt.readCharacteristic(ch)).thenReturn(true);
        when(ch.getUuid()).thenReturn(TEST_UUID1);

        Promise<BluetoothGattCharacteristic, Integer, Void> readCh = gatt.readCharacteristic(ch);
        gatt.onCharacteristicRead(mockGatt, ch, BluetoothGatt.GATT_FAILURE);

        assertThat(readCh.isPending()).isFalse();
        assertThat(readCh.isRejected()).isTrue();
        assertThat(readCh.isResolved()).isFalse();
    }

    @Test
    public void test_read_multiple_characteristics() throws Exception {
        BluetoothDevice device = mock(BluetoothDevice.class);
        Context context = mock(Context.class);
        BluetoothGatt mockGatt = mock(BluetoothGatt.class);
        AsyncBluetoothGatt gatt = new AsyncBluetoothGatt(device, context, false);
        when(device.connectGatt(context, false, gatt)).thenReturn(mockGatt);
        gatt.connect();

        BluetoothGattCharacteristic ch1 = mock(BluetoothGattCharacteristic.class);
        when(mockGatt.readCharacteristic(ch1)).thenReturn(true);
        when(ch1.getUuid()).thenReturn(TEST_UUID1);
        BluetoothGattCharacteristic ch2 = mock(BluetoothGattCharacteristic.class);
        when(mockGatt.readCharacteristic(ch2)).thenReturn(true);
        when(ch2.getUuid()).thenReturn(TEST_UUID2);
        Promise<BluetoothGattCharacteristic, Integer, Void> readCh1 = gatt.readCharacteristic(ch1);
        Promise<BluetoothGattCharacteristic, Integer, Void> readCh2 = gatt.readCharacteristic(ch2);
        gatt.onCharacteristicRead(mockGatt, ch2, BluetoothGatt.GATT_SUCCESS);

        assertThat(readCh1.isPending()).isTrue();
        assertThat(readCh1.isRejected()).isFalse();
        assertThat(readCh1.isResolved()).isFalse();
        assertThat(readCh2.isPending()).isFalse();
        assertThat(readCh2.isRejected()).isFalse();
        assertThat(readCh2.isResolved()).isTrue();
        readCh2.done(new DoneCallback<BluetoothGattCharacteristic>() {
            @Override
            public void onDone(BluetoothGattCharacteristic result) {
                testCh = result;
            }
        });
        assertThat(testCh).isEqualTo(ch2);
    }
}
