package betterbluetoothle.tests;

import betterbluetoothle.async.AsyncBluetoothLeScan;
import betterbluetoothle.async.AsyncBluetoothLeScan.ScanResult;

import java.util.ArrayList;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import android.bluetooth.*;
import org.jdeferred.*;

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
}
