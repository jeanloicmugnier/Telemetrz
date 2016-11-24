package no.nordicsemi.android.nrftoolbox.profile;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.AppHelpFragment;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.OnDeviceSelectedListener;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public abstract class BleProfileActivity extends Activity implements BleManagerCallbacks, OnDeviceSelectedListener {
    private static final String CONNECTION_STATUS = "connection_status";
    private static final String DEVICE_NAME = "device_name";
    protected static final int REQUEST_ENABLE_BT = 2;
    private static final String TAG = "BaseProfileActivity";
    private TextView mBatteryLevelView;
    private BleManager<? extends BleManagerCallbacks> mBleManager;
    private Button mConnectButton;
    private boolean mDeviceConnected;
    private String mDeviceName;
    private TextView mDeviceNameView;

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.1 */
    class C01061 implements Runnable {
        C01061() {
        }

        public void run() {
            BleProfileActivity.this.mConnectButton.setText(C0063R.string.action_disconnect);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.2 */
    class C01072 implements Runnable {
        C01072() {
        }

        public void run() {
            BleProfileActivity.this.mConnectButton.setText(C0063R.string.action_connect);
            BleProfileActivity.this.mDeviceNameView.setText(BleProfileActivity.this.getDefaultDeviceName());
            BleProfileActivity.this.mBatteryLevelView.setText(C0063R.string.not_available);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.3 */
    class C01083 implements Runnable {
        C01083() {
        }

        public void run() {
            BleProfileActivity.this.mConnectButton.setText(C0063R.string.action_connect);
            BleProfileActivity.this.mDeviceNameView.setText(BleProfileActivity.this.getDefaultDeviceName());
            BleProfileActivity.this.mBatteryLevelView.setText(C0063R.string.not_available);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.4 */
    class C01094 implements Runnable {
        final /* synthetic */ int val$value;

        C01094(int i) {
            this.val$value = i;
        }

        public void run() {
            BleProfileActivity.this.mBatteryLevelView.setText(BleProfileActivity.this.getString(C0063R.string.battery, new Object[]{Integer.valueOf(this.val$value)}));
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.5 */
    class C01105 implements Runnable {
        final /* synthetic */ String val$message;

        C01105(String str) {
            this.val$message = str;
        }

        public void run() {
            Toast.makeText(BleProfileActivity.this, this.val$message, 0).show();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.6 */
    class C01116 implements Runnable {
        final /* synthetic */ int val$messageResId;

        C01116(int i) {
            this.val$messageResId = i;
        }

        public void run() {
            Toast.makeText(BleProfileActivity.this, this.val$messageResId, 0).show();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity.7 */
    class C01127 implements Runnable {
        final /* synthetic */ boolean val$discoverableRequired;
        final /* synthetic */ UUID val$filter;

        C01127(UUID uuid, boolean z) {
            this.val$filter = uuid;
            this.val$discoverableRequired = z;
        }

        public void run() {
            ScannerFragment.getInstance(BleProfileActivity.this, this.val$filter, this.val$discoverableRequired).show(BleProfileActivity.this.getFragmentManager(), "scan_fragment");
        }
    }

    protected abstract int getAboutTextId();

    protected abstract int getDefaultDeviceName();

    protected abstract UUID getFilterUUID();

    protected abstract BleManager<? extends BleManagerCallbacks> initializeManager();

    protected abstract void onCreateView(Bundle bundle);

    protected abstract void setDefaultUI();

    public BleProfileActivity() {
        this.mDeviceConnected = false;
    }

    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensureBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        this.mBleManager = initializeManager();
        onInitialize(savedInstanceState);
        onCreateView(savedInstanceState);
        onViewCreated(savedInstanceState);
    }

    protected void onInitialize(Bundle savedInstanceState) {
    }

    protected final void onViewCreated(Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.mConnectButton = (Button) findViewById(C0063R.id.action_connect);
        this.mDeviceNameView = (TextView) findViewById(C0063R.id.device_name);
        this.mBatteryLevelView = (TextView) findViewById(C0063R.id.battery);
    }

    public void onBackPressed() {
        this.mBleManager.disconnect();
        super.onBackPressed();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CONNECTION_STATUS, this.mDeviceConnected);
        outState.putString(DEVICE_NAME, this.mDeviceName);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mDeviceConnected = savedInstanceState.getBoolean(CONNECTION_STATUS);
        this.mDeviceName = savedInstanceState.getString(DEVICE_NAME);
        if (this.mDeviceConnected) {
            this.mConnectButton.setText(C0063R.string.action_disconnect);
        } else {
            this.mConnectButton.setText(C0063R.string.action_connect);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0063R.menu.help, menu);
        return true;
    }

    protected boolean onOptionsItemSelected(int itemId) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 16908332:
                onBackPressed();
                break;
            case C0063R.id.action_about:
                AppHelpFragment.getInstance(getAboutTextId()).show(getFragmentManager(), "help_fragment");
                break;
            default:
                return onOptionsItemSelected(id);
        }
        return true;
    }

    public void onConnectClicked(View view) {
        if (!isBLEEnabled()) {
            showBLEDialog();
        } else if (this.mDeviceConnected) {
            this.mBleManager.disconnect();
        } else {
            setDefaultUI();
            showDeviceScanningDialog(getFilterUUID(), isDiscoverableRequired());
        }
    }

    public void onDeviceSelected(BluetoothDevice device, String name) {
        TextView textView = this.mDeviceNameView;
        this.mDeviceName = name;
        textView.setText(name);
        this.mBleManager.connect(getApplicationContext(), device);
    }

    public void onDialogCanceled() {
    }

    public void onDeviceConnected() {
        this.mDeviceConnected = true;
        runOnUiThread(new C01061());
    }

    public void onDeviceDisconnected() {
        this.mDeviceConnected = false;
        this.mBleManager.closeBluetoothGatt();
        runOnUiThread(new C01072());
    }

    public void onLinklossOccur() {
        this.mDeviceConnected = false;
        runOnUiThread(new C01083());
    }

    public void onBatteryValueReceived(int value) {
        runOnUiThread(new C01094(value));
    }

    public void onBondingRequired() {
        showToast((int) C0063R.string.bonding);
    }

    public void onBonded() {
        showToast((int) C0063R.string.bonded);
    }

    public void onError(String message, int errorCode) {
        DebugLogger.m19e(TAG, "Error occured: " + message + ",  error code: " + errorCode);
        showToast(message + " (" + errorCode + ")");
        onDeviceDisconnected();
    }

    public void onDeviceNotSupported() {
        showToast((int) C0063R.string.not_supported);
    }

    protected void showToast(String message) {
        runOnUiThread(new C01105(message));
    }

    protected void showToast(int messageResId) {
        runOnUiThread(new C01116(messageResId));
    }

    protected boolean isDeviceConnected() {
        return this.mDeviceConnected;
    }

    protected String getDeviceName() {
        return this.mDeviceName;
    }

    protected boolean isDiscoverableRequired() {
        return true;
    }

    private void showDeviceScanningDialog(UUID filter, boolean discoverableRequired) {
        runOnUiThread(new C01127(filter, discoverableRequired));
    }

    private void ensureBLESupported() {
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, C0063R.string.no_ble, 1).show();
            finish();
        }
    }

    protected boolean isBLEEnabled() {
        BluetoothAdapter adapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected void showBLEDialog() {
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
    }
}
