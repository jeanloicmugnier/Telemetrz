package no.nordicsemi.android.nrftoolbox.profile;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.C0063R;

public abstract class BleProfileService extends Service implements BleManagerCallbacks {
    public static final String BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL";
    public static final String BROADCAST_BOND_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_BOND_STATE";
    public static final String BROADCAST_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_CONNECTION_STATE";
    public static final String BROADCAST_ERROR = "no.nordicsemi.android.nrftoolbox.BROADCAST_ERROR";
    public static final String BROADCAST_SERVICES_DISCOVERED = "no.nordicsemi.android.nrftoolbox.BROADCAST_SERVICES_DISCOVERED";
    public static final String EXTRA_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.EXTRA_BATTERY_LEVEL";
    public static final String EXTRA_BOND_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_BOND_STATE";
    public static final String EXTRA_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_CONNECTION_STATE";
    public static final String EXTRA_DEVICE_ADDRESS = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_ADDRESS";
    public static final String EXTRA_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_NAME";
    public static final String EXTRA_ERROR_CODE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_CODE";
    public static final String EXTRA_ERROR_MESSAGE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_LOG_URI = "no.nordicsemi.android.nrftoolbox.EXTRA_LOG_URI";
    public static final String EXTRA_SERVICE_PRIMARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_PRIMARY";
    public static final String EXTRA_SERVICE_SECONDARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_SECONDARY";
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_LINK_LOSS = -1;
    private static final String TAG = "BleProfileService";
    private BleManager<BleManagerCallbacks> mBleManager;
    private boolean mConnected;
    private String mDeviceAddress;
    private String mDeviceName;
    private Handler mHandler;
    private ILogSession mLogSession;

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileService.1 */
    class C01201 implements Runnable {
        final /* synthetic */ int val$messageResId;

        C01201(int i) {
            this.val$messageResId = i;
        }

        public void run() {
            Toast.makeText(BleProfileService.this, this.val$messageResId, BleProfileService.STATE_DISCONNECTED).show();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.profile.BleProfileService.2 */
    class C01212 implements Runnable {
        final /* synthetic */ String val$message;

        C01212(String str) {
            this.val$message = str;
        }

        public void run() {
            Toast.makeText(BleProfileService.this, this.val$message, BleProfileService.STATE_DISCONNECTED).show();
        }
    }

    public class LocalBinder extends Binder {
        public final void disconnect() {
            if (BleProfileService.this.mConnected) {
                Intent broadcast = new Intent(BleProfileService.BROADCAST_CONNECTION_STATE);
                broadcast.putExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTING);
                LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);
                BleProfileService.this.mBleManager.disconnect();
                return;
            }
            BleProfileService.this.onDeviceDisconnected();
        }

        public String getDeviceAddress() {
            return BleProfileService.this.mDeviceAddress;
        }

        public String getDeviceName() {
            return BleProfileService.this.mDeviceName;
        }

        public boolean isConnected() {
            return BleProfileService.this.mConnected;
        }

        protected ILogSession getLogSession() {
            return BleProfileService.this.mLogSession;
        }
    }

    protected abstract BleManager initializeManager();

    public IBinder onBind(Intent intent) {
        return getBinder();
    }

    protected LocalBinder getBinder() {
        return new LocalBinder();
    }

    public boolean onUnbind(Intent intent) {
        return true;
    }

    public void onCreate() {
        super.onCreate();
        this.mHandler = new Handler();
        this.mBleManager = initializeManager();
        this.mBleManager.setGattCallbacks(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !intent.hasExtra(EXTRA_DEVICE_ADDRESS)) {
            throw new UnsupportedOperationException("No device address at EXTRA_DEVICE_ADDRESS key");
        }
        this.mLogSession = Logger.openSession(getApplicationContext(), (Uri) intent.getParcelableExtra(EXTRA_LOG_URI));
        this.mDeviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        Logger.m13i(this.mLogSession, "Service started");
        Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
        BluetoothDevice device = ((BluetoothManager) getSystemService("bluetooth")).getAdapter().getRemoteDevice(this.mDeviceAddress);
        this.mDeviceName = device.getName();
        onServiceStarted();
        Logger.m15v(this.mLogSession, "Connecting...");
        this.mBleManager.connect(this, device);
        return STATE_DISCONNECTING;
    }

    protected void onServiceStarted() {
    }

    public void onDestroy() {
        super.onDestroy();
        this.mBleManager.closeBluetoothGatt();
        Logger.m13i(this.mLogSession, "Service destroyed");
        this.mBleManager = null;
        this.mDeviceAddress = null;
        this.mDeviceName = null;
        this.mConnected = false;
        this.mLogSession = null;
    }

    public void onDeviceConnected() {
        Logger.m13i(this.mLogSession, "Connected to " + this.mDeviceAddress);
        this.mConnected = true;
        Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED);
        broadcast.putExtra(EXTRA_DEVICE_ADDRESS, this.mDeviceAddress);
        broadcast.putExtra(EXTRA_DEVICE_NAME, this.mDeviceName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onDeviceDisconnected() {
        Logger.m13i(this.mLogSession, "Disconnected");
        this.mConnected = false;
        this.mDeviceAddress = null;
        this.mDeviceName = null;
        Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
        Logger.m15v(this.mLogSession, "Stopping service...");
        stopSelf();
    }

    public void onLinklossOccur() {
        Logger.m17w(this.mLogSession, "Connection lost");
        this.mConnected = false;
        Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
        Logger.m13i(this.mLogSession, "Services Discovered");
        Logger.m15v(this.mLogSession, "Primary service found");
        if (optionalServicesFound) {
            Logger.m15v(this.mLogSession, "Secondary service found");
        }
        Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onDeviceNotSupported() {
        Logger.m13i(this.mLogSession, "Services Discovered");
        Logger.m17w(this.mLogSession, "Device is not supported");
        Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onBatteryValueReceived(int value) {
        Logger.m13i(this.mLogSession, "Battery level received: " + value + "%");
        Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
        broadcast.putExtra(EXTRA_BATTERY_LEVEL, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onBondingRequired() {
        Logger.m15v(this.mLogSession, "Bond state: Bonding...");
        showToast((int) C0063R.string.bonding);
        Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_BOND_STATE, 11);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onBonded() {
        Logger.m13i(this.mLogSession, "Bond state: Bonded");
        showToast((int) C0063R.string.bonded);
        Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_BOND_STATE, 12);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onError(String message, int errorCode) {
        Logger.m11e(this.mLogSession, message + " (" + errorCode + ")");
        Intent broadcast = new Intent(BROADCAST_ERROR);
        broadcast.putExtra(EXTRA_ERROR_MESSAGE, message);
        broadcast.putExtra(EXTRA_ERROR_CODE, errorCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
        this.mBleManager.disconnect();
        stopSelf();
    }

    protected void showToast(int messageResId) {
        this.mHandler.post(new C01201(messageResId));
    }

    protected void showToast(String message) {
        this.mHandler.post(new C01212(message));
    }

    protected ILogSession getLogSession() {
        return this.mLogSession;
    }

    protected String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    protected String getDeviceName() {
        return this.mDeviceName;
    }

    protected boolean isConnected() {
        return this.mConnected;
    }
}
