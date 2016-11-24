package no.nordicsemi.android.nrftoolbox.hrs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class HRSManager implements BleManager<HRSManagerCallbacks> {
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC;
    private static final UUID BATTERY_SERVICE;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final int FIRST_BITMASK = 1;
    private static final UUID HR_CHARACTERISTIC_UUID;
    private static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID;
    public static final UUID HR_SERVICE_UUID;
    private static HRSManager managerInstance;
    private final String TAG;
    private BluetoothGattCharacteristic mBatteryCharacteritsic;
    private BluetoothGatt mBluetoothGatt;
    private HRSManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGattCharacteristic mHRCharacteristic;
    private BluetoothGattCharacteristic mHRLocationCharacteristic;

    /* renamed from: no.nordicsemi.android.nrftoolbox.hrs.HRSManager.1 */
    class C01011 extends BluetoothGattCallback {
        C01011() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                HRSManager.this.mCallbacks.onError(HRSManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == 2) {
                DebugLogger.m18d("HRSManager", "Device connected");
                HRSManager.this.mBluetoothGatt.discoverServices();
                HRSManager.this.mCallbacks.onDeviceConnected();
            } else if (newState == 0) {
                DebugLogger.m18d("HRSManager", "Device disconnected");
                HRSManager.this.mCallbacks.onDeviceDisconnected();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().equals(HRSManager.HR_SERVICE_UUID)) {
                        HRSManager.this.mHRCharacteristic = service.getCharacteristic(HRSManager.HR_CHARACTERISTIC_UUID);
                        HRSManager.this.mHRLocationCharacteristic = service.getCharacteristic(HRSManager.HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
                    } else if (service.getUuid().equals(HRSManager.BATTERY_SERVICE)) {
                        HRSManager.this.mBatteryCharacteritsic = service.getCharacteristic(HRSManager.BATTERY_LEVEL_CHARACTERISTIC);
                    }
                }
                if (HRSManager.this.mHRCharacteristic != null) {
                    HRSManager.this.mCallbacks.onServicesDiscovered(false);
                    HRSManager.this.readHRSensorLocation();
                    return;
                }
                HRSManager.this.mCallbacks.onDeviceNotSupported();
                gatt.disconnect();
                return;
            }
            HRSManager.this.mCallbacks.onError(HRSManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (characteristic.getUuid().equals(HRSManager.HR_SENSOR_LOCATION_CHARACTERISTIC_UUID)) {
                    HRSManager.this.mCallbacks.onHRSensorPositionFound(HRSManager.this.getBodySensorPosition(characteristic.getValue()[0]));
                    if (HRSManager.this.mBatteryCharacteritsic != null) {
                        HRSManager.this.readBatteryLevel();
                    } else {
                        HRSManager.this.enableHRNotification();
                    }
                }
                if (characteristic.getUuid().equals(HRSManager.BATTERY_LEVEL_CHARACTERISTIC)) {
                    HRSManager.this.mCallbacks.onBatteryValueReceived(characteristic.getValue()[0]);
                    HRSManager.this.enableHRNotification();
                    return;
                }
                return;
            }
            HRSManager.this.mCallbacks.onError(HRSManager.ERROR_READ_CHARACTERISTIC, status);
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(HRSManager.HR_CHARACTERISTIC_UUID)) {
                int hrValue;
                if (HRSManager.this.isHeartRateInUINT16(characteristic.getValue()[0])) {
                    hrValue = characteristic.getIntValue(18, HRSManager.FIRST_BITMASK).intValue();
                } else {
                    hrValue = characteristic.getIntValue(17, HRSManager.FIRST_BITMASK).intValue();
                }
                HRSManager.this.mCallbacks.onHRValueReceived(hrValue);
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == 0) {
                HRSManager.this.mCallbacks.onHRNotificationEnabled();
            } else {
                HRSManager.this.mCallbacks.onError(HRSManager.ERROR_WRITE_DESCRIPTOR, status);
            }
        }
    }

    public HRSManager() {
        this.TAG = "HRSManager";
        this.mGattCallback = new C01011();
    }

    static {
        HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
        HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
        HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
        managerInstance = null;
    }

    public static synchronized HRSManager getInstance(Context context) {
        HRSManager hRSManager;
        synchronized (HRSManager.class) {
            if (managerInstance == null) {
                managerInstance = new HRSManager();
            }
            managerInstance.mContext = context;
            hRSManager = managerInstance;
        }
        return hRSManager;
    }

    public void setGattCallbacks(HRSManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void connect(Context context, BluetoothDevice device) {
        DebugLogger.m18d("HRSManager", "Connecting to device...");
        this.mBluetoothGatt = device.connectGatt(context, false, this.mGattCallback);
    }

    public void disconnect() {
        DebugLogger.m18d("HRSManager", "Disconnecting device...");
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        }
    }

    private void readBatteryLevel() {
        if (this.mBatteryCharacteritsic != null) {
            this.mBluetoothGatt.readCharacteristic(this.mBatteryCharacteritsic);
        }
    }

    private void readHRSensorLocation() {
        if (this.mHRLocationCharacteristic != null) {
            this.mBluetoothGatt.readCharacteristic(this.mHRLocationCharacteristic);
        }
    }

    private String getBodySensorPosition(byte bodySensorPositionValue) {
        String[] locations = this.mContext.getResources().getStringArray(C0063R.array.hrs_locations);
        if (bodySensorPositionValue > locations.length) {
            return this.mContext.getString(C0063R.string.hrs_location_other);
        }
        return locations[bodySensorPositionValue];
    }

    private boolean isHeartRateInUINT16(byte value) {
        if ((value & FIRST_BITMASK) != 0) {
            return true;
        }
        return false;
    }

    private void enableHRNotification() {
        DebugLogger.m18d("HRSManager", "Enabling heart rate notifications");
        this.mBluetoothGatt.setCharacteristicNotification(this.mHRCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mHRCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void closeBluetoothGatt() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
    }
}
