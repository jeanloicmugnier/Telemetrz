package no.nordicsemi.android.nrftoolbox.dfu;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.OnDeviceSelectedListener;

public class DfuInitiatorActivity extends Activity implements OnDeviceSelectedListener {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().hasExtra(DfuBaseService.EXTRA_FILE_PATH)) {
            finish();
        }
        if (savedInstanceState == null) {
            ScannerFragment.getInstance(this, DfuService.DFU_SERVICE_UUID, true).show(getFragmentManager(), null);
        }
    }

    public void onDeviceSelected(BluetoothDevice device, String name) {
        String finalName;
        Intent intent = getIntent();
        String overwritenName = intent.getStringExtra(DfuBaseService.EXTRA_DEVICE_NAME);
        String path = intent.getStringExtra(DfuBaseService.EXTRA_FILE_PATH);
        String address = device.getAddress();
        int type = intent.getIntExtra(DfuBaseService.EXTRA_FILE_TYPE, 0);
        if (overwritenName == null) {
            finalName = name;
        } else {
            finalName = overwritenName;
        }
        Intent service = new Intent(this, DfuService.class);
        service.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, address);
        service.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, finalName);
        if (intent.hasExtra(DfuBaseService.EXTRA_FILE_TYPE)) {
            service.putExtra(DfuBaseService.EXTRA_FILE_TYPE, type);
        }
        service.putExtra(DfuBaseService.EXTRA_FILE_PATH, path);
        startService(service);
        finish();
    }

    public void onDialogCanceled() {
        finish();
    }
}
