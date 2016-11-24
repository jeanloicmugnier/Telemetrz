package no.nordicsemi.android.nrftoolbox.bpm;

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
import java.util.Calendar;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class BPMManager implements BleManager<BPMManagerCallbacks> {
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC;
    public static final UUID BATTERY_SERVICE;
    private static final UUID BPM_CHARACTERISTIC_UUID;
    public static final UUID BP_SERVICE_UUID;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final UUID ICP_CHARACTERISTIC_UUID;
    private static BPMManager managerInstance;
    private final String TAG;
    private BluetoothGattCharacteristic mBPMCharacteristic;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BroadcastReceiver mBondingBroadcastReceiver;
    private BPMManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGattCharacteristic mICPCharacteristic;

    /* renamed from: no.nordicsemi.android.nrftoolbox.bpm.BPMManager.1 */
    class C00701 extends BroadcastReceiver {
        C00701() {
        }

        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
            DebugLogger.m18d("BPMManager", "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1));
            if (device.getAddress().equals(BPMManager.this.mBluetoothGatt.getDevice().getAddress()) && bondState == 12) {
                if (BPMManager.this.mICPCharacteristic != null) {
                    BPMManager.this.enableIntermediateCuffPressureNotification(BPMManager.this.mBluetoothGatt);
                } else {
                    BPMManager.this.enableBloodPressureMeasurementIndication(BPMManager.this.mBluetoothGatt);
                }
                BPMManager.this.mContext.unregisterReceiver(this);
                BPMManager.this.mCallbacks.onBonded();
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.bpm.BPMManager.2 */
    class C00712 extends BluetoothGattCallback {
        C00712() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                BPMManager.this.mCallbacks.onError(BPMManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == 2) {
                BPMManager.this.mCallbacks.onDeviceConnected();
                gatt.discoverServices();
            } else if (newState == 0) {
                BPMManager.this.mCallbacks.onDeviceDisconnected();
                gatt.close();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (BPMManager.BP_SERVICE_UUID.equals(service.getUuid())) {
                        BPMManager.this.mBPMCharacteristic = service.getCharacteristic(BPMManager.BPM_CHARACTERISTIC_UUID);
                        BPMManager.this.mICPCharacteristic = service.getCharacteristic(BPMManager.ICP_CHARACTERISTIC_UUID);
                    } else if (BPMManager.BATTERY_SERVICE.equals(service.getUuid())) {
                        BPMManager.this.mBatteryCharacteristic = service.getCharacteristic(BPMManager.BATTERY_LEVEL_CHARACTERISTIC);
                    }
                }
                if (BPMManager.this.mBPMCharacteristic == null) {
                    BPMManager.this.mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                    return;
                }
                BPMManager.this.mCallbacks.onServicesDiscovered(BPMManager.this.mICPCharacteristic != null);
                if (BPMManager.this.mBatteryCharacteristic != null) {
                    readBatteryLevel(gatt);
                    return;
                } else if (BPMManager.this.mICPCharacteristic != null) {
                    BPMManager.this.enableIntermediateCuffPressureNotification(gatt);
                    return;
                } else {
                    BPMManager.this.enableBloodPressureMeasurementIndication(gatt);
                    return;
                }
            }
            DebugLogger.m19e("BPMManager", "onServicesDiscovered error " + status);
            BPMManager.this.mCallbacks.onError(BPMManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != 0) {
                BPMManager.this.mCallbacks.onError(BPMManager.ERROR_DISCOVERY_SERVICE, status);
            } else if (BPMManager.BATTERY_LEVEL_CHARACTERISTIC.equals(characteristic.getUuid())) {
                BPMManager.this.mCallbacks.onBatteryValueReceived(characteristic.getIntValue(17, 0).intValue());
                if (BPMManager.this.mICPCharacteristic != null) {
                    BPMManager.this.enableIntermediateCuffPressureNotification(gatt);
                } else {
                    BPMManager.this.enableBloodPressureMeasurementIndication(gatt);
                }
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            int offset;
            int offset2 = 0 + 1;
            int flags = characteristic.getIntValue(17, 0).intValue();
            int unit = flags & 1;
            boolean timestampPresent = (flags & 2) > 0;
            boolean pulseRatePresent = (flags & 4) > 0;
            if (BPMManager.BPM_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                offset = offset2 + 6;
                BPMManager.this.mCallbacks.onBloodPressureMeasurmentRead(characteristic.getFloatValue(50, offset2).floatValue(), characteristic.getFloatValue(50, 3).floatValue(), characteristic.getFloatValue(50, 5).floatValue(), unit);
            } else if (BPMManager.ICP_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                offset = offset2 + 6;
                BPMManager.this.mCallbacks.onIntermediateCuffPressureRead(characteristic.getFloatValue(50, offset2).floatValue(), unit);
            } else {
                offset = offset2;
            }
            if (timestampPresent) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(1, characteristic.getIntValue(18, offset).intValue());
                calendar.set(2, characteristic.getIntValue(17, offset + 2).intValue());
                calendar.set(5, characteristic.getIntValue(17, offset + 3).intValue());
                calendar.set(11, characteristic.getIntValue(17, offset + 4).intValue());
                calendar.set(12, characteristic.getIntValue(17, offset + 5).intValue());
                calendar.set(13, characteristic.getIntValue(17, offset + 6).intValue());
                offset += 7;
                BPMManager.this.mCallbacks.onTimestampRead(calendar);
            } else {
                BPMManager.this.mCallbacks.onTimestampRead(null);
            }
            if (pulseRatePresent) {
                float pulseRate = characteristic.getFloatValue(50, offset).floatValue();
                offset += 2;
                BPMManager.this.mCallbacks.onPulseRateRead(pulseRate);
                return;
            }
            BPMManager.this.mCallbacks.onPulseRateRead(-1.0f);
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == 0) {
                if (BPMManager.BPM_CHARACTERISTIC_UUID.equals(descriptor.getCharacteristic().getUuid())) {
                    BPMManager.this.mCallbacks.onBloodPressureMeasurementIndicationsEnabled();
                }
                if (BPMManager.ICP_CHARACTERISTIC_UUID.equals(descriptor.getCharacteristic().getUuid())) {
                    BPMManager.this.mCallbacks.onIntermediateCuffPressureNotificationEnabled();
                    BPMManager.this.enableBloodPressureMeasurementIndication(gatt);
                }
            } else if (status != 5) {
                BPMManager.this.mCallbacks.onError(BPMManager.ERROR_WRITE_DESCRIPTOR, status);
            } else if (gatt.getDevice().getBondState() == 10) {
                BPMManager.this.mCallbacks.onBondingRequired();
                BPMManager.this.mContext.registerReceiver(BPMManager.this.mBondingBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
            } else {
                DebugLogger.m22w("BPMManager", BPMManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                BPMManager.this.mCallbacks.onError(BPMManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        private void readBatteryLevel(BluetoothGatt gatt) {
            DebugLogger.m18d("BPMManager", "readBatteryLevel()");
            gatt.readCharacteristic(BPMManager.this.mBatteryCharacteristic);
        }
    }

    public BPMManager() {
        this.TAG = "BPMManager";
        this.mBondingBroadcastReceiver = new C00701();
        this.mGattCallback = new C00712();
    }

    static {
        BP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");
        ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        managerInstance = null;
    }

    public static synchronized BPMManager getBPMManager() {
        BPMManager bPMManager;
        synchronized (BPMManager.class) {
            if (managerInstance == null) {
                managerInstance = new BPMManager();
            }
            bPMManager = managerInstance;
        }
        return bPMManager;
    }

    public void setGattCallbacks(BPMManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void connect(Context context, BluetoothDevice device) {
        this.mBluetoothGatt = device.connectGatt(context, false, this.mGattCallback);
        this.mContext = context;
    }

    public void disconnect() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        }
    }

    private void enableIntermediateCuffPressureNotification(BluetoothGatt gatt) {
        DebugLogger.m18d("BPMManager", "enableIntermediateCuffPressureNotification()");
        gatt.setCharacteristicNotification(this.mICPCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mICPCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableBloodPressureMeasurementIndication(BluetoothGatt gatt) {
        DebugLogger.m18d("BPMManager", "enableBloodPressureMeasurementIndication()");
        gatt.setCharacteristicNotification(this.mBPMCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mBPMCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    public void closeBluetoothGatt() {
        try {
            this.mContext.unregisterReceiver(this.mBondingBroadcastReceiver);
        } catch (Exception e) {
        }
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBPMCharacteristic = null;
            this.mBatteryCharacteristic = null;
            this.mBluetoothGatt = null;
        }
    }
}
