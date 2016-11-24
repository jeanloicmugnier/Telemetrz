package no.nordicsemi.android.nrftoolbox.rsc;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.UUID;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class RSCManager implements BleManager<RSCManagerCallbacks> {
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID;
    private static final UUID BATTERY_SERVICE_UUID;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
    private static final String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
    private static final byte INSTANTANEOUS_STRIDE_LENGTH_PRESENT = (byte) 1;
    private static final UUID RSC_MEASUREMENT_CHARACTERISTIC_UUID;
    public static final UUID RUNNING_SPEED_AND_CADENCE_SERVICE_UUID;
    private static final String TAG = "RSCManager";
    private static final byte TOTAL_DISTANCE_PRESENT = (byte) 2;
    private static final byte WALKING_OR_RUNNING_STATUS_BITS = (byte) 4;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BroadcastReceiver mBondingBroadcastReceiver;
    private RSCManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private ILogSession mLogSession;
    private BluetoothGattCharacteristic mRSCMeasurementCharacteristic;

    /* renamed from: no.nordicsemi.android.nrftoolbox.rsc.RSCManager.1 */
    class C01341 extends BluetoothGattCallback {
        C01341() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                RSCManager.this.mCallbacks.onError(RSCManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == 2) {
                DebugLogger.m18d(RSCManager.TAG, "Device connected");
                RSCManager.this.mBluetoothGatt.discoverServices();
                RSCManager.this.mCallbacks.onDeviceConnected();
            } else if (newState == 0) {
                DebugLogger.m18d(RSCManager.TAG, "Device disconnected");
                RSCManager.this.mCallbacks.onDeviceDisconnected();
                RSCManager.this.closeBluetoothGatt();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().equals(RSCManager.RUNNING_SPEED_AND_CADENCE_SERVICE_UUID)) {
                        DebugLogger.m18d(RSCManager.TAG, "Running Speed and Cadence service is found");
                        RSCManager.this.mRSCMeasurementCharacteristic = service.getCharacteristic(RSCManager.RSC_MEASUREMENT_CHARACTERISTIC_UUID);
                    } else if (service.getUuid().equals(RSCManager.BATTERY_SERVICE_UUID)) {
                        DebugLogger.m18d(RSCManager.TAG, "Battery service is found");
                        RSCManager.this.mBatteryCharacteristic = service.getCharacteristic(RSCManager.BATTERY_LEVEL_CHARACTERISTIC_UUID);
                    }
                }
                if (RSCManager.this.mRSCMeasurementCharacteristic == null) {
                    RSCManager.this.mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                    return;
                }
                RSCManager.this.mCallbacks.onServicesDiscovered(false);
                if (RSCManager.this.mBatteryCharacteristic != null) {
                    RSCManager.this.readBatteryLevel(gatt);
                    return;
                } else {
                    RSCManager.this.enableRSCMeasurementNotification(gatt);
                    return;
                }
            }
            RSCManager.this.mCallbacks.onError(RSCManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (characteristic.getUuid().equals(RSCManager.BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                    RSCManager.this.mCallbacks.onBatteryValueReceived(characteristic.getValue()[0]);
                    RSCManager.this.enableRSCMeasurementNotification(gatt);
                }
            } else if (status != 5) {
                RSCManager.this.mCallbacks.onError(RSCManager.ERROR_READ_CHARACTERISTIC, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w(RSCManager.TAG, RSCManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                RSCManager.this.mCallbacks.onError(RSCManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            int flags = characteristic.getValue()[0];
            int offset = 0 + 1;
            boolean islmPresent = (flags & 1) > 0;
            boolean tdPreset = (flags & 2) > 0;
            boolean running = (flags & 4) > 0;
            float instantaneousSpeed = (((float) characteristic.getIntValue(18, offset).intValue()) / 256.0f) * 3.6f;
            offset += 2;
            int instantaneousCadence = characteristic.getIntValue(17, offset).intValue();
            offset++;
            float instantaneousStrideLength = -1.0f;
            if (islmPresent) {
                instantaneousStrideLength = (float) characteristic.getIntValue(18, offset).intValue();
                offset += 2;
            }
            float totalDistance = -1.0f;
            if (tdPreset) {
                totalDistance = ((float) characteristic.getIntValue(20, offset).intValue()) / 10.0f;
                offset += 4;
            }
            RSCManager.this.mCallbacks.onMeasurementReceived(instantaneousSpeed, instantaneousCadence, totalDistance, instantaneousStrideLength, running ? 1 : 0);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.rsc.RSCManager.2 */
    class C01352 extends BroadcastReceiver {
        C01352() {
        }

        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
            int previousBondState = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1);
            if (device.getAddress().equals(RSCManager.this.mBluetoothGatt.getDevice().getAddress())) {
                DebugLogger.m20i(RSCManager.TAG, "Bond state changed for: " + device.getName() + " new state: " + bondState + " previous: " + previousBondState);
                if (bondState == 11) {
                    RSCManager.this.mCallbacks.onBondingRequired();
                } else if (bondState == 12) {
                    RSCManager.this.mCallbacks.onBonded();
                }
            }
        }
    }

    static {
        RUNNING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
        RSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public RSCManager(Context context) {
        this.mGattCallback = new C01341();
        this.mBondingBroadcastReceiver = new C01352();
        context.registerReceiver(this.mBondingBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    public void setGattCallbacks(RSCManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void setLogger(ILogSession session) {
        this.mLogSession = session;
    }

    public void connect(Context context, BluetoothDevice device) {
        this.mContext = context;
        Logger.m13i(this.mLogSession, "[RSC] Gatt server started");
        if (this.mBluetoothGatt == null) {
            this.mBluetoothGatt = device.connectGatt(this.mContext, true, this.mGattCallback);
        } else {
            this.mBluetoothGatt.connect();
        }
    }

    public void disconnect() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        }
    }

    public void readBatteryLevel() {
        readBatteryLevel(this.mBluetoothGatt);
    }

    private void readBatteryLevel(BluetoothGatt gatt) {
        if (this.mBatteryCharacteristic != null) {
            DebugLogger.m18d(TAG, "reading battery characteristic");
            gatt.readCharacteristic(this.mBatteryCharacteristic);
            return;
        }
        DebugLogger.m22w(TAG, "Battery Level Characteristic is null");
    }

    private void enableRSCMeasurementNotification(BluetoothGatt gatt) {
        DebugLogger.m18d(TAG, "enableIntermediateCuffPressureNotification()");
        gatt.setCharacteristicNotification(this.mRSCMeasurementCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mRSCMeasurementCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
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
        this.mCallbacks = null;
        this.mLogSession = null;
    }
}
