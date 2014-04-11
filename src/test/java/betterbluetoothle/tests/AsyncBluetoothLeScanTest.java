package betterbluetoothle.tests;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import betterbluetoothle.async.AsyncBluetoothLeScan;
import betterbluetoothle.async.AsyncBluetoothLeScan.ScanResult;

import java.util.ArrayList;
import java.util.UUID;

import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class AsyncBluetoothLeScanTest {

	private ArrayList<ScanResult> found = new ArrayList<ScanResult>();

	@Before
	public void setup() {
		found.clear();
	}

    @Test
    public void test_start_no_parameters_detects_all_devices() throws Exception {
    	BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(true);
        BluetoothDevice d1 = mock(BluetoothDevice.class);
        BluetoothDevice d2 = mock(BluetoothDevice.class);

        Promise<Void, Void, ScanResult> promise = scanner.start();
        promise.progress(new ProgressCallback<ScanResult>() {
            @Override
            public void onProgress(ScanResult result) {
                found.add(result);
            }
        });
        scanner.onLeScan(d1, 0, new byte[]{ 0x02, 0x02, 0x01, 0x02 });
        scanner.onLeScan(d2, 0, new byte[]{ 0x02, 0x02, 0x03, 0x04 });

        assertThat(found.size()).isEqualTo(2);
        assertThat(found.get(0).device).isEqualTo(d1);
        assertThat(found.get(1).device).isEqualTo(d2);
        assertThat(promise.isPending()).isTrue();
        assertThat(promise.isRejected()).isFalse();
        assertThat(promise.isResolved()).isFalse();
    }

    @Test
    public void test_start_one_service_uuid_detected() throws Exception {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(true);
        BluetoothDevice d1 = mock(BluetoothDevice.class);
        BluetoothDevice d2 = mock(BluetoothDevice.class);

        Promise<Void, Void, ScanResult> promise = scanner.start(UUID.fromString("00000201-0000-1000-8000-00805f9b34fb"));
        promise.progress(new ProgressCallback<ScanResult>() {
            @Override
            public void onProgress(ScanResult result) {
                found.add(result);
            }
        });
        scanner.onLeScan(d1, 0, new byte[]{ 0x02, 0x02, 0x01, 0x02 });
        scanner.onLeScan(d2, 0, new byte[]{ 0x02, 0x02, 0x03, 0x04 });

        assertThat(found.size()).isEqualTo(1);
        assertThat(found.get(0).device).isEqualTo(d1);
        assertThat(promise.isPending()).isTrue();
        assertThat(promise.isRejected()).isFalse();
        assertThat(promise.isResolved()).isFalse();
    }

    @Test
    public void test_start_multiple_service_uuids_detected() throws Exception {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(true);
        BluetoothDevice d1 = mock(BluetoothDevice.class);
        BluetoothDevice d2 = mock(BluetoothDevice.class);
        BluetoothDevice d3 = mock(BluetoothDevice.class);

        UUID services[] = new UUID[] {
                UUID.fromString("00000201-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00000403-0000-1000-8000-00805f9b34fb")
        };
        Promise<Void, Void, ScanResult> promise = scanner.start(services);
        promise.progress(new ProgressCallback<ScanResult>() {
            @Override
            public void onProgress(ScanResult result) {
                found.add(result);
            }
        });
        scanner.onLeScan(d1, 0, new byte[]{ 0x02, 0x02, 0x01, 0x02 });
        scanner.onLeScan(d2, 0, new byte[]{ 0x02, 0x02, 0x03, 0x04 });
        scanner.onLeScan(d3, 0, new byte[]{ 0x02, 0x02, 0x05, 0x06 });

        assertThat(found.size()).isEqualTo(2);
        assertThat(found.get(0).device).isEqualTo(d1);
        assertThat(found.get(1).device).isEqualTo(d2);
        assertThat(promise.isPending()).isTrue();
        assertThat(promise.isRejected()).isFalse();
        assertThat(promise.isResolved()).isFalse();
    }

    @Test
    public void test_startlescan_failure_rejects_promise() throws Exception {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(false);

        Promise<Void, Void, ScanResult> promise = scanner.start();

        assertThat(promise.isPending()).isFalse();
        assertThat(promise.isRejected()).isTrue();
        assertThat(promise.isResolved()).isFalse();
    }

    @Test
    public void test_onlescan_passes_rssi_and_packet_to_progress() throws Exception {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(true);

        Promise<Void, Void, ScanResult> promise = scanner.start();
        promise.progress(new ProgressCallback<ScanResult>() {
            @Override
            public void onProgress(ScanResult result) {
                found.add(result);
            }
        });
        byte[] packet = new byte[]{ 0x02, 0x02, 0x01, 0x02 };
        scanner.onLeScan(mock(BluetoothDevice.class), 99, packet);

        assertThat(found.size()).isEqualTo(1);
        assertThat(found.get(0).rssi).isEqualTo(99);
        assertThat(found.get(0).bytes).isEqualTo(packet);
    }

    @Test
    public void test_stop_scan_resolves_promise() throws Exception {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(true);

        Promise<Void, Void, ScanResult> promise = scanner.start();
        scanner.stop();

        assertThat(promise.isPending()).isFalse();
        assertThat(promise.isRejected()).isFalse();
        assertThat(promise.isResolved()).isTrue();
    }

    @Test
    public void test_start_ends_previous_scan() throws Exception {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);
        when(adapter.startLeScan(scanner)).thenReturn(true);

        Promise<Void, Void, ScanResult> previous = scanner.start();
        Promise<Void, Void, ScanResult> current = scanner.start();

        assertThat(previous.isPending()).isFalse();
        assertThat(previous.isRejected()).isFalse();
        assertThat(previous.isResolved()).isTrue();
        assertThat(current.isPending()).isTrue();
        assertThat(current.isRejected()).isFalse();
        assertThat(current.isResolved()).isFalse();
    }
}
