package betterbluetoothle.tests;

import betterbluetoothle.async.AsyncBluetoothLeScan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import android.bluetooth.BluetoothAdapter;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class AsyncBluetoothLeScanTest {

    @Test
    public void test_start_starts_scan() throws Exception {
    	BluetoothAdapter adapter = mock(BluetoothAdapter.class);

        AsyncBluetoothLeScan scanner = new AsyncBluetoothLeScan(adapter);

        scanner.start();
    }
}
