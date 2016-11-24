package no.nordicsemi.android.nrftoolbox.csc;

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

public class CSCManager implements BleManager<CSCManagerCallbacks> {
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID;
    private static final UUID BATTERY_SERVICE_UUID;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final byte CRANK_REVOLUTION_DATA_PRESENT = (byte) 2;
    private static final UUID CSC_MEASUREMENT_CHARACTERISTIC_UUID;
    public static final UUID CYCLING_SPEED_AND_CADENCE_SERVICE_UUID;
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final String TAG = "RSCManager";
    private static final byte WHEEL_REVOLUTIONS_DATA_PRESENT = (byte) 1;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    private boolean mBatteryLevelNotificationsEnabled;
    private BluetoothGatt mBluetoothGatt;
    private BroadcastReceiver mBondingBroadcastReceiver;
    private BluetoothGattCharacteristic mCSCMeasurementCharacteristic;
    private CSCManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private ILogSession mLogSession;

    /* renamed from: no.nordicsemi.android.nrftoolbox.csc.CSCManager.1 */
    class C00731 extends BluetoothGattCallback {
        C00731() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                CSCManager.this.mCallbacks.onError(CSCManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == 2) {
                DebugLogger.m18d(CSCManager.TAG, "Device connected");
                CSCManager.this.mBluetoothGatt.discoverServices();
                CSCManager.this.mCallbacks.onDeviceConnected();
            } else if (newState == 0) {
                DebugLogger.m18d(CSCManager.TAG, "Device disconnected");
                CSCManager.this.mCallbacks.onDeviceDisconnected();
                CSCManager.this.closeBluetoothGatt();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().equals(CSCManager.CYCLING_SPEED_AND_CADENCE_SERVICE_UUID)) {
                        DebugLogger.m18d(CSCManager.TAG, "Cycling Speed and Cadence service is found");
                        CSCManager.this.mCSCMeasurementCharacteristic = service.getCharacteristic(CSCManager.CSC_MEASUREMENT_CHARACTERISTIC_UUID);
                    } else if (service.getUuid().equals(CSCManager.BATTERY_SERVICE_UUID)) {
                        DebugLogger.m18d(CSCManager.TAG, "Battery service is found");
                        CSCManager.this.mBatteryCharacteristic = service.getCharacteristic(CSCManager.BATTERY_LEVEL_CHARACTERISTIC_UUID);
                    }
                }
                if (CSCManager.this.mCSCMeasurementCharacteristic == null) {
                    CSCManager.this.mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                    return;
                }
                CSCManager.this.mCallbacks.onServicesDiscovered(false);
                if (CSCManager.this.mBatteryCharacteristic == null) {
                    CSCManager.this.enableCSCMeasurementNotification(gatt);
                    return;
                } else if ((CSCManager.this.mBatteryCharacteristic.getProperties() & 2) > 0) {
                    CSCManager.this.readBatteryLevel(gatt);
                    return;
                } else if ((CSCManager.this.mBatteryCharacteristic.getProperties() & 16) > 0) {
                    CSCManager.this.enableBatteryLevelNotification(gatt);
                    return;
                } else {
                    CSCManager.this.enableCSCMeasurementNotification(gatt);
                    return;
                }
            }
            CSCManager.this.mCallbacks.onError(CSCManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (characteristic.getUuid().equals(CSCManager.BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                    CSCManager.this.mCallbacks.onBatteryValueReceived(characteristic.getValue()[0]);
                    if (!CSCManager.this.mBatteryLevelNotificationsEnabled) {
                        CSCManager.this.enableCSCMeasurementNotification(gatt);
                    }
                }
            } else if (status != 5) {
                CSCManager.this.mCallbacks.onError(CSCManager.ERROR_READ_CHARACTERISTIC, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w(CSCManager.TAG, CSCManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                CSCManager.this.mCallbacks.onError(CSCManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == 0) {
                if (descriptor.getCharacteristic().getUuid().equals(CSCManager.BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                    CSCManager.this.enableCSCMeasurementNotification(gatt);
                }
            } else if (status != 5) {
                CSCManager.this.mCallbacks.onError(CSCManager.ERROR_WRITE_DESCRIPTOR, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w(CSCManager.TAG, CSCManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                CSCManager.this.mCallbacks.onError(CSCManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            boolean wheelRevPresent;
            boolean crankRevPreset = true;
            int flags = characteristic.getValue()[0];
            int offset = 0 + 1;
            if ((flags & 1) > 0) {
                wheelRevPresent = true;
            } else {
                wheelRevPresent = false;
            }
            if ((flags & 2) <= 0) {
                crankRevPreset = false;
            }
            if (wheelRevPresent) {
                int wheelRevolutions = characteristic.getIntValue(20, offset).intValue();
                offset += 4;
                int lastWheelEventTime = characteristic.getIntValue(18, offset).intValue();
                offset += 2;
                CSCManager.this.mCallbacks.onWheelMeasurementReceived(wheelRevolutions, lastWheelEventTime);
            }
            if (crankRevPreset) {
                int crankRevolutions = characteristic.getIntValue(18, offset).intValue();
                offset += 2;
                int lastCrankEventTime = characteristic.getIntValue(18, offset).intValue();
                offset += 2;
                CSCManager.this.mCallbacks.onCrankMeasurementReceived(crankRevolutions, lastCrankEventTime);
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.csc.CSCManager.2 */
    class C00742 extends BroadcastReceiver {
        C00742() {
        }

        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
            int previousBondState = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1);
            if (device.getAddress().equals(CSCManager.this.mBluetoothGatt.getDevice().getAddress())) {
                DebugLogger.m20i(CSCManager.TAG, "Bond state changed for: " + device.getName() + " new state: " + bondState + " previous: " + previousBondState);
                if (bondState == 11) {
                    CSCManager.this.mCallbacks.onBondingRequired();
                } else if (bondState == 12) {
                    CSCManager.this.mCallbacks.onBonded();
                }
            }
        }
    }

    static {
        CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
        CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public CSCManager(Context context) {
        this.mGattCallback = new C00731();
        this.mBondingBroadcastReceiver = new C00742();
        context.registerReceiver(this.mBondingBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    public void setGattCallbacks(CSCManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void setLogger(ILogSession session) {
        this.mLogSession = session;
    }

    public void connect(Context context, BluetoothDevice device) {
        this.mContext = context;
        Logger.m13i(this.mLogSession, "[CSC] Gatt server started");
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
        if ((this.mBatteryCharacteristic.getProperties() & 2) > 0) {
            readBatteryLevel(this.mBluetoothGatt);
        }
    }

    private void readBatteryLevel(BluetoothGatt gatt) {
        if (this.mBatteryCharacteristic != null) {
            DebugLogger.m18d(TAG, "reading battery characteristic");
            gatt.readCharacteristic(this.mBatteryCharacteristic);
            return;
        }
        DebugLogger.m22w(TAG, "Battery Level Characteristic is null");
    }

    private void enableBatteryLevelNotification(BluetoothGatt gatt) {
        this.mBatteryLevelNotificationsEnabled = true;
        gatt.setCharacteristicNotification(this.mBatteryCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mBatteryCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableCSCMeasurementNotification(BluetoothGatt gatt) {
        gatt.setCharacteristicNotification(this.mCSCMeasurementCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mCSCMeasurementCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
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
        this.mBatteryLevelNotificationsEnabled = false;
        this.mCallbacks = null;
        this.mLogSession = null;
    }
}
