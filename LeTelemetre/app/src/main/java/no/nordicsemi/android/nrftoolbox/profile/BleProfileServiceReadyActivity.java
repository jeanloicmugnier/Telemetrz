package no.nordicsemi.android.nrftoolbox.profile;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.UUID;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LocalLogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.AppHelpFragment;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;
import no.nordicsemi.android.nrftoolbox.rsc.RSCManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.OnDeviceSelectedListener;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import org.achartengine.tools.Zoom;

public abstract class BleProfileServiceReadyActivity<E extends LocalBinder> extends Activity implements OnDeviceSelectedListener {
    private static final String DEVICE_NAME = "device_name";
    private static final String LOG_URI = "log_uri";
    protected static final int REQUEST_ENABLE_BT = 2;
    private static final String TAG = "BleProfileServiceReadyActivity";
    private TextView mBatteryLevelView;
    private BroadcastReceiver mCommonBroadcastReceiver;
    private Button mConnectButton;
    private String mDeviceName;
    private TextView mDeviceNameView;
    private ILogSession mLogSession;
    private E mService;
    private ServiceConnection mServiceConnection;

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity.1 */
    class C01221 extends BroadcastReceiver {
        C01221() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleProfileService.BROADCAST_CONNECTION_STATE.equals(action)) {
                switch (intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, 0)) {
                    case RSCManagerCallbacks.NOT_AVAILABLE /*-1*/:
                        BleProfileServiceReadyActivity.this.onLinklossOccur();
                    case Zoom.ZOOM_AXIS_XY /*0*/:
                        BleProfileServiceReadyActivity.this.onDeviceDisconnected();
                        BleProfileServiceReadyActivity.this.mDeviceName = null;
                    case Zoom.ZOOM_AXIS_X /*1*/:
                        BleProfileServiceReadyActivity.this.mDeviceName = intent.getStringExtra(BleProfileService.EXTRA_DEVICE_NAME);
                        BleProfileServiceReadyActivity.this.onDeviceConnected();
                    default:
                }
            } else if (BleProfileService.BROADCAST_SERVICES_DISCOVERED.equals(action)) {
                boolean primaryService = intent.getBooleanExtra(BleProfileService.EXTRA_SERVICE_PRIMARY, false);
                boolean secondaryService = intent.getBooleanExtra(BleProfileService.EXTRA_SERVICE_SECONDARY, false);
                if (primaryService) {
                    BleProfileServiceReadyActivity.this.onServicesDiscovered(secondaryService);
                } else {
                    BleProfileServiceReadyActivity.this.onDeviceNotSupported();
                }
            } else if (BleProfileService.BROADCAST_BOND_STATE.equals(action)) {
                switch (intent.getIntExtra(BleProfileService.EXTRA_BOND_STATE, 10)) {
                    case 11:
                        BleProfileServiceReadyActivity.this.onBondingRequired();
                    case 12:
                        BleProfileServiceReadyActivity.this.onBonded();
                    default:
                }
            } else if (BleProfileService.BROADCAST_BATTERY_LEVEL.equals(action)) {
                int value = intent.getIntExtra(BleProfileService.EXTRA_BATTERY_LEVEL, -1);
                if (value > 0) {
                    BleProfileServiceReadyActivity.this.onBatteryValueReceived(value);
                }
            } else if (BleProfileService.BROADCAST_ERROR.equals(action)) {
                BleProfileServiceReadyActivity.this.onError(intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE), intent.getIntExtra(BleProfileService.EXTRA_ERROR_CODE, 0));
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity.2 */
    class C01232 implements ServiceConnection {
        C01232() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            E bleService =  (E) service;
            BleProfileServiceReadyActivity.this.mService =  bleService;
            BleProfileServiceReadyActivity.this.mLogSession = BleProfileServiceReadyActivity.this.mService.getLogSession();
            Logger.m9d(BleProfileServiceReadyActivity.this.mLogSession, "Activity binded to the service");
            BleProfileServiceReadyActivity.this.onServiceBinded(bleService);
            BleProfileServiceReadyActivity.this.mDeviceName = bleService.getDeviceName();
            BleProfileServiceReadyActivity.this.mDeviceNameView.setText(BleProfileServiceReadyActivity.this.mDeviceName);
            BleProfileServiceReadyActivity.this.mConnectButton.setText(String.valueOf(C0063R.string.action_disconnect));
            if (bleService.isConnected()) {
                BleProfileServiceReadyActivity.this.onDeviceConnected();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Logger.m9d(BleProfileServiceReadyActivity.this.mLogSession, "Activity disconnected from the service");
            BleProfileServiceReadyActivity.this.mDeviceNameView.setText(BleProfileServiceReadyActivity.this.getDefaultDeviceName());
            BleProfileServiceReadyActivity.this.mConnectButton.setText(String.valueOf(C0063R.string.action_connect));
            BleProfileServiceReadyActivity.this.mService = null;
            BleProfileServiceReadyActivity.this.mDeviceName = null;
            BleProfileServiceReadyActivity.this.mLogSession = null;
            BleProfileServiceReadyActivity.this.onServiceUnbinded();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity.3 */
    class C01243 implements Runnable {
        final /* synthetic */ String val$message;

        C01243(String str) {
            this.val$message = str;
        }

        public void run() {
            Toast.makeText(BleProfileServiceReadyActivity.this, this.val$message,Toast.LENGTH_LONG ).show();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity.4 */
    class C01254 implements Runnable {
        final /* synthetic */ int val$messageResId;

        C01254(int i) {
            this.val$messageResId = i;
        }

        public void run() {
            Toast.makeText(BleProfileServiceReadyActivity.this, this.val$messageResId, Toast.LENGTH_SHORT).show();
        }
    }

    protected abstract int getAboutTextId();

    protected abstract int getDefaultDeviceName();

    protected abstract UUID getFilterUUID();

    protected abstract Class<? extends BleProfileService> getServiceClass();

    protected abstract void onCreateView(Bundle bundle);

    protected abstract void onServiceBinded(E e);

    protected abstract void onServiceUnbinded();

    public abstract void onServicesDiscovered(boolean z);

    protected abstract void setDefaultUI();

    public BleProfileServiceReadyActivity() {
        this.mCommonBroadcastReceiver = new C01221();
        this.mServiceConnection = new C01232();
    }

    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensureBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        if (savedInstanceState != null) {
            this.mLogSession = Logger.openSession(getApplicationContext(), (Uri) savedInstanceState.getParcelable(LOG_URI));
        }
        onInitialize(savedInstanceState);
        onCreateView(savedInstanceState);
        onViewCreated(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mCommonBroadcastReceiver, makeIntentFilter());
    }

    protected void onStart() {
        super.onStart();
        if (bindService(new Intent(this, getServiceClass()), this.mServiceConnection, 0)) {
            Logger.m9d(this.mLogSession, "Binding to the service...");
        }
    }

    protected void onStop() {
        super.onStop();
        try {
            Logger.m9d(this.mLogSession, "Unbinding from the service...");
            unbindService(this.mServiceConnection);
            this.mService = null;
            Logger.m9d(this.mLogSession, "Activity unbinded from the service");
            onServiceUnbinded();
            this.mDeviceName = null;
            this.mLogSession = null;
        } catch (IllegalArgumentException e) {
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mCommonBroadcastReceiver);
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
        intentFilter.addAction(BleProfileService.BROADCAST_SERVICES_DISCOVERED);
        intentFilter.addAction(BleProfileService.BROADCAST_BOND_STATE);
        intentFilter.addAction(BleProfileService.BROADCAST_BATTERY_LEVEL);
        intentFilter.addAction(BleProfileService.BROADCAST_ERROR);
        return intentFilter;
    }

    protected LocalBinder getService() {
        return this.mService;
    }

    protected void onInitialize(Bundle savedInstanceState) {
    }

    protected final void onViewCreated(Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.mConnectButton = (Button) findViewById(Integer.valueOf(C0063R.id.action_connect));
        this.mDeviceNameView = (TextView) findViewById(Integer.valueOf(C0063R.id.device_name));
        this.mBatteryLevelView = (TextView) findViewById(Integer.valueOf(C0063R.id.battery));
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEVICE_NAME, this.mDeviceName);
        if (this.mLogSession != null) {
            outState.putParcelable(LOG_URI, this.mLogSession.getSessionUri());
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mDeviceName = savedInstanceState.getString(DEVICE_NAME);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(Integer.valueOf(C0063R.menu.help), menu);
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
        } else if (this.mService == null) {
            setDefaultUI();
            showDeviceScanningDialog(getFilterUUID(), isDiscoverableRequired());
        } else {
            Logger.m15v(this.mLogSession, "Disconnecting...");
            this.mService.disconnect();
        }
    }

    protected int getLoggerProfileTitle() {
        return 0;
    }

    protected Uri getLocalAuthorityLogger() {
        return null;
    }

    public void onDeviceSelected(BluetoothDevice device, String name) {
        int titleId = getLoggerProfileTitle();
        if (titleId > 0) {
            this.mLogSession = Logger.newSession(getApplicationContext(), getString(titleId), device.getAddress(), name);
            if (this.mLogSession == null && getLocalAuthorityLogger() != null) {
                this.mLogSession = LocalLogSession.newSession(getApplicationContext(), getLocalAuthorityLogger(), device.getAddress(), name);
            }
        }
        TextView textView = this.mDeviceNameView;
        this.mDeviceName = name;
        textView.setText(name);
        this.mConnectButton.setText(String.valueOf(C0063R.string.action_disconnect));
        Logger.m9d(this.mLogSession, "Creating service...");
        Intent service = new Intent(this, getServiceClass());
        service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, device.getAddress());
        if (this.mLogSession != null) {
            service.putExtra(BleProfileService.EXTRA_LOG_URI, this.mLogSession.getSessionUri());
        }
        startService(service);
        Logger.m9d(this.mLogSession, "Binding to the service...");
        bindService(service, this.mServiceConnection, 0);
    }

    public void onDialogCanceled() {
    }

    public void onDeviceConnected() {
        this.mDeviceNameView.setText(this.mDeviceName);
        this.mConnectButton.setText(String.valueOf(C0063R.string.action_disconnect));
    }

    public void onDeviceDisconnected() {
        this.mConnectButton.setText(String.valueOf(C0063R.string.action_connect));
        this.mDeviceNameView.setText(getDefaultDeviceName());
        if (this.mBatteryLevelView != null) {
            this.mBatteryLevelView.setText(String.valueOf(C0063R.string.not_available));
        }
        try {
            Logger.m9d(this.mLogSession, "Unbinding from the service...");
            unbindService(this.mServiceConnection);
            this.mService = null;
            Logger.m9d(this.mLogSession, "Activity unbinded from the service");
            onServiceUnbinded();
            this.mDeviceName = null;
            this.mLogSession = null;
        } catch (IllegalArgumentException e) {
        }
    }

    public void onLinklossOccur() {
        if (this.mBatteryLevelView != null) {
            this.mBatteryLevelView.setText(String.valueOf(C0063R.string.not_available));
        }
    }

    public void onBondingRequired() {
    }

    public void onBonded() {
    }

    public void onDeviceNotSupported() {
        showToast((int) C0063R.string.not_supported);
    }

    public void onBatteryValueReceived(int value) {
        if (this.mBatteryLevelView != null) {
            this.mBatteryLevelView.setText(String.valueOf(C0063R.string.battery).concat(" " + String.valueOf(value)));
        }
    }

    public void onError(String message, int errorCode) {
        DebugLogger.m19e(TAG, "Error occured: " + message + ",  error code: " + errorCode);
        showToast(message + " (" + errorCode + ")");
        onDeviceDisconnected();
    }

    protected void showToast(String message) {
        runOnUiThread(new C01243(message));
    }

    protected void showToast(int messageResId) {
        runOnUiThread(new C01254(messageResId));
    }

    protected boolean isDeviceConnected() {
        return this.mService != null;
    }

    protected String getDeviceName() {
        return this.mDeviceName;
    }

    protected boolean isDiscoverableRequired() {
        return true;
    }

    private void showDeviceScanningDialog(UUID filter, boolean discoverableRequired) {
        ScannerFragment.getInstance(this, filter, discoverableRequired).show(getFragmentManager(), "scan_fragment");
    }

    public ILogSession getLogSession() {
        return this.mLogSession;
    }

    private void ensureBLESupported() {
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, Integer.valueOf(C0063R.string.no_ble), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    protected boolean isBLEEnabled() {
        BluetoothAdapter adapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected void showBLEDialog() {
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
    }
}
