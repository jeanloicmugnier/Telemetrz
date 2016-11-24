package no.nordicsemi.android.nrftoolbox.proximity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.UUID;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class ProximityManager implements BleManager<ProximityManagerCallbacks> {
    private static final UUID ALERT_LEVEL_CHARACTERISTIC_UUID;
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID;
    private static final UUID BATTERY_SERVICE_UUID;
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
    private static final String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
    private static final int HIGH_ALERT = 2;
    public static final UUID IMMEIDIATE_ALERT_SERVICE_UUID;
    public static final UUID LINKLOSS_SERVICE_UUID;
    private static final int NO_ALERT = 0;
    private final String TAG;
    private BluetoothGattCharacteristic mAlertLevelCharacteristic;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattServer mBluetoothGattServer;
    private BroadcastReceiver mBondingBroadcastReceiver;
    private ProximityManagerCallbacks mCallbacks;
    private Context mContext;
    private BluetoothDevice mDeviceToConnect;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGattServerCallback mGattServerCallbacks;
    private Handler mHandler;
    private BluetoothGattCharacteristic mLinklossCharacteristic;
    private ILogSession mLogSession;
    private Ringtone mRingtoneAlarm;
    private Ringtone mRingtoneNotification;
    private boolean userDisconnectedFlag;

    /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityManager.1 */
    class C01291 extends BluetoothGattServerCallback {

        /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityManager.1.1 */
        class C01281 implements Runnable {
            final /* synthetic */ BluetoothGattService val$service;

            C01281(BluetoothGattService bluetoothGattService) {
                this.val$service = bluetoothGattService;
            }

            public void run() {
                if (ProximityManager.IMMEIDIATE_ALERT_SERVICE_UUID.equals(this.val$service.getUuid())) {
                    ProximityManager.this.addLinklossService();
                    return;
                }
                DebugLogger.m18d("ProximityManager", "[Proximity Server] Gatt server started");
                Logger.m13i(ProximityManager.this.mLogSession, "[Proximity Server] Gatt server started");
                if (ProximityManager.this.mBluetoothGatt == null) {
                    ProximityManager.this.mBluetoothGatt = ProximityManager.this.mDeviceToConnect.connectGatt(ProximityManager.this.mContext, false, ProximityManager.this.mGattCallback);
                    ProximityManager.this.mDeviceToConnect = null;
                    return;
                }
                ProximityManager.this.mBluetoothGatt.connect();
            }
        }

        C01291() {
        }

        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onCharacteristicReadRequest " + device.getName());
        }

        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onCharacteristicWriteRequest " + device.getName());
            if (value[0] != 0) {
                Logger.m13i(ProximityManager.this.mLogSession, "[Proximity Server] Immediate alarm request received: ON");
                ProximityManager.this.playAlarm();
                return;
            }
            Logger.m13i(ProximityManager.this.mLogSession, "[Proximity Server] Immediate alarm request received: OFF");
            ProximityManager.this.stopAlarm();
        }

        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onConnectionStateChange " + device.getName() + " status: " + status + " new state: " + newState);
        }

        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onDescriptorReadRequest " + device.getName());
        }

        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onDescriptorWriteRequest " + device.getName());
        }

        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onExecuteWrite " + device.getName());
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            DebugLogger.m18d("ProximityManager", "[Proximity Server] onServiceAdded " + service.getUuid());
            ProximityManager.this.mHandler.post(new C01281(service));
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityManager.2 */
    class C01302 extends BluetoothGattCallback {
        C01302() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                ProximityManager.this.mCallbacks.onError(ProximityManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == ProximityManager.HIGH_ALERT) {
                DebugLogger.m18d("ProximityManager", "Device connected");
                ProximityManager.this.mBluetoothGatt.discoverServices();
                ProximityManager.this.mCallbacks.onDeviceConnected();
            } else if (newState == 0) {
                DebugLogger.m18d("ProximityManager", "Device disconnected");
                if (ProximityManager.this.userDisconnectedFlag) {
                    ProximityManager.this.mCallbacks.onDeviceDisconnected();
                    ProximityManager.this.userDisconnectedFlag = false;
                    return;
                }
                ProximityManager.this.playNotification();
                ProximityManager.this.mCallbacks.onLinklossOccur();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().equals(ProximityManager.IMMEIDIATE_ALERT_SERVICE_UUID)) {
                        DebugLogger.m18d("ProximityManager", "Immediate Alert service is found");
                        ProximityManager.this.mAlertLevelCharacteristic = service.getCharacteristic(ProximityManager.ALERT_LEVEL_CHARACTERISTIC_UUID);
                    } else if (service.getUuid().equals(ProximityManager.LINKLOSS_SERVICE_UUID)) {
                        DebugLogger.m18d("ProximityManager", "Linkloss service is found");
                        ProximityManager.this.mLinklossCharacteristic = service.getCharacteristic(ProximityManager.ALERT_LEVEL_CHARACTERISTIC_UUID);
                    } else if (service.getUuid().equals(ProximityManager.BATTERY_SERVICE_UUID)) {
                        DebugLogger.m18d("ProximityManager", "Battery service is found");
                        ProximityManager.this.mBatteryCharacteristic = service.getCharacteristic(ProximityManager.BATTERY_LEVEL_CHARACTERISTIC_UUID);
                    }
                }
                if (ProximityManager.this.mLinklossCharacteristic == null) {
                    ProximityManager.this.mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                    return;
                }
                ProximityManager.this.mCallbacks.onServicesDiscovered(ProximityManager.this.mAlertLevelCharacteristic != null);
                ProximityManager.this.writeLinklossAlertLevel(ProximityManager.HIGH_ALERT);
                return;
            }
            ProximityManager.this.mCallbacks.onError(ProximityManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (characteristic.getUuid().equals(ProximityManager.BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                    ProximityManager.this.mCallbacks.onBatteryValueReceived(characteristic.getValue()[0]);
                }
            } else if (status != 5) {
                ProximityManager.this.mCallbacks.onError(ProximityManager.ERROR_READ_CHARACTERISTIC, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w("ProximityManager", ProximityManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                ProximityManager.this.mCallbacks.onError(ProximityManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (characteristic.getUuid().equals(ProximityManager.ALERT_LEVEL_CHARACTERISTIC_UUID) && ProximityManager.this.mBatteryCharacteristic != null) {
                    ProximityManager.this.readBatteryLevel();
                }
            } else if (status != 5) {
                ProximityManager.this.mCallbacks.onError(ProximityManager.ERROR_WRITE_CHARACTERISTIC, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w("ProximityManager", ProximityManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                ProximityManager.this.mCallbacks.onError(ProximityManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityManager.3 */
    class C01313 extends BroadcastReceiver {
        C01313() {
        }

        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
            int previousBondState = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1);
            if (device.getAddress().equals(ProximityManager.this.mBluetoothGatt.getDevice().getAddress())) {
                DebugLogger.m20i("ProximityManager", "Bond state changed for: " + device.getName() + " new state: " + bondState + " previous: " + previousBondState);
                if (bondState == 11) {
                    ProximityManager.this.mCallbacks.onBondingRequired();
                } else if (bondState == 12) {
                    if (ProximityManager.this.mLinklossCharacteristic != null) {
                        ProximityManager.this.writeLinklossAlertLevel(ProximityManager.HIGH_ALERT);
                    }
                    ProximityManager.this.mCallbacks.onBonded();
                }
            }
        }
    }

    static {
        IMMEIDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
        LINKLOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
        ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
    }

    public ProximityManager(Context context) {
        this.TAG = "ProximityManager";
        this.userDisconnectedFlag = false;
        this.mGattServerCallbacks = new C01291();
        this.mGattCallback = new C01302();
        this.mBondingBroadcastReceiver = new C01313();
        initializeAlarm(context);
        this.mHandler = new Handler();
        context.registerReceiver(this.mBondingBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    private void openGattServer(Context context, BluetoothManager manager) {
        this.mBluetoothGattServer = manager.openGattServer(context, this.mGattServerCallbacks);
    }

    private void closeGattServer() {
        if (this.mBluetoothGattServer != null) {
            this.mBluetoothGattServer.close();
            this.mBluetoothGattServer = null;
        }
    }

    private void addImmediateAlertService() {
        BluetoothGattCharacteristic alertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, 4, 16);
        alertLevel.setValue(HIGH_ALERT, 17, 0);
        BluetoothGattService immediateAlertService = new BluetoothGattService(IMMEIDIATE_ALERT_SERVICE_UUID, 0);
        immediateAlertService.addCharacteristic(alertLevel);
        this.mBluetoothGattServer.addService(immediateAlertService);
    }

    private void addLinklossService() {
        BluetoothGattCharacteristic linklossAlertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, 10, 16);
        linklossAlertLevel.setValue(HIGH_ALERT, 17, 0);
        BluetoothGattService linklossService = new BluetoothGattService(LINKLOSS_SERVICE_UUID, 0);
        linklossService.addCharacteristic(linklossAlertLevel);
        this.mBluetoothGattServer.addService(linklossService);
    }

    public void setGattCallbacks(ProximityManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void setLogger(ILogSession logSession) {
        this.mLogSession = logSession;
    }

    public void connect(Context context, BluetoothDevice device) {
        this.mContext = context;
        this.mDeviceToConnect = device;
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ProximityActivity.PREFS_GATT_SERVER_ENABLED, true)) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService("bluetooth");
            try {
                DebugLogger.m18d("ProximityManager", "[Proximity Server] Starting Gatt server...");
                Logger.m15v(this.mLogSession, "[Proximity Server] Starting Gatt server...");
                openGattServer(context, bluetoothManager);
                addImmediateAlertService();
            } catch (Exception e) {
                Logger.m11e(this.mLogSession, "[Proximity Server] Gatt server failed to start");
                Log.e("ProximityManager", "Creating Gatt Server failed", e);
            }
        } else if (this.mBluetoothGatt == null) {
            this.mBluetoothGatt = this.mDeviceToConnect.connectGatt(context, false, this.mGattCallback);
            this.mDeviceToConnect = null;
        } else {
            this.mBluetoothGatt.connect();
        }
    }

    public void disconnect() {
        if (this.mBluetoothGatt != null) {
            this.userDisconnectedFlag = true;
            this.mBluetoothGatt.disconnect();
            stopAlarm();
            closeGattServer();
        }
    }

    private void initializeAlarm(Context context) {
        this.mRingtoneAlarm = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(4));
        this.mRingtoneAlarm.setStreamType(4);
        this.mRingtoneNotification = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(HIGH_ALERT));
    }

    private void playNotification() {
        DebugLogger.m18d("ProximityManager", "playNotification");
        this.mRingtoneNotification.play();
    }

    private void playAlarm() {
        DebugLogger.m18d("ProximityManager", "playAlarm");
        AudioManager am = (AudioManager) this.mContext.getSystemService("audio");
        am.setStreamVolume(4, am.getStreamMaxVolume(4), 8);
        this.mRingtoneAlarm.play();
    }

    private void stopAlarm() {
        DebugLogger.m18d("ProximityManager", "stopAlarm");
        this.mRingtoneAlarm.stop();
    }

    private void readBatteryLevel() {
        if (this.mBatteryCharacteristic != null) {
            DebugLogger.m18d("ProximityManager", "reading battery characteristic");
            this.mBluetoothGatt.readCharacteristic(this.mBatteryCharacteristic);
            return;
        }
        DebugLogger.m22w("ProximityManager", "Battery Level Characteristic is null");
    }

    private void readLinklossAlertLevel() {
        if (this.mLinklossCharacteristic != null) {
            DebugLogger.m18d("ProximityManager", "reading linkloss alert level characteristic");
            this.mBluetoothGatt.readCharacteristic(this.mLinklossCharacteristic);
            return;
        }
        DebugLogger.m22w("ProximityManager", "Linkloss Alert Level Characteristic is null");
    }

    private void writeLinklossAlertLevel(int alertLevel) {
        if (this.mLinklossCharacteristic != null) {
            DebugLogger.m18d("ProximityManager", "writing linkloss alert level characteristic");
            this.mLinklossCharacteristic.setValue(alertLevel, 17, 0);
            this.mBluetoothGatt.writeCharacteristic(this.mLinklossCharacteristic);
            return;
        }
        DebugLogger.m22w("ProximityManager", "Linkloss Alert Level Characteristic is not found");
    }

    public void writeImmediateAlertOn() {
        if (this.mAlertLevelCharacteristic != null) {
            DebugLogger.m18d("ProximityManager", "writing Immediate alert characteristic On");
            this.mAlertLevelCharacteristic.setWriteType(1);
            this.mAlertLevelCharacteristic.setValue(HIGH_ALERT, 17, 0);
            this.mBluetoothGatt.writeCharacteristic(this.mAlertLevelCharacteristic);
            return;
        }
        DebugLogger.m22w("ProximityManager", "Immediate Alert Level Characteristic is not found");
    }

    public void writeImmediateAlertOff() {
        if (this.mAlertLevelCharacteristic != null) {
            DebugLogger.m18d("ProximityManager", "writing Immediate alert characteristic Off");
            this.mAlertLevelCharacteristic.setWriteType(1);
            this.mAlertLevelCharacteristic.setValue(0, 17, 0);
            this.mBluetoothGatt.writeCharacteristic(this.mAlertLevelCharacteristic);
            return;
        }
        DebugLogger.m22w("ProximityManager", "Immediate Alert Level Characteristic is not found");
    }

    public void closeBluetoothGatt() {
        try {
            this.mContext.unregisterReceiver(this.mBondingBroadcastReceiver);
        } catch (Exception e) {
        }
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
        if (this.mBluetoothGattServer != null) {
            this.mBluetoothGattServer.close();
            this.mBluetoothGattServer = null;
        }
        this.mCallbacks = null;
        this.mLogSession = null;
        this.mRingtoneNotification = null;
        this.mRingtoneAlarm = null;
    }
}
