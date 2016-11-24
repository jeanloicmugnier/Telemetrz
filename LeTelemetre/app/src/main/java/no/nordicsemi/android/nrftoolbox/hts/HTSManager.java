package no.nordicsemi.android.nrftoolbox.hts;

import android.Manifest;
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
import android.support.annotation.RequiresPermission;
import android.support.annotation.UiThread;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class HTSManager implements BleManager<HTSManagerCallbacks> {
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC;
    private static final UUID BATTERY_SERVICE;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final int FIRST_BIT_MASK = 1;
    private static final UUID HT_MEASUREMENT_CHARACTERISTIC_UUID;
    public static final UUID HT_SERVICE_UUID;
    private static HTSManager managerInstance;
    private final int GET_BIT24;
    private final int HIDE_MSB_8BITS_OUT_OF_16BITS;
    private final int HIDE_MSB_8BITS_OUT_OF_32BITS;
    private final int SHIFT_LEFT_16BITS;
    private final int SHIFT_LEFT_8BITS;
    private final String TAG;
    private BluetoothGattCharacteristic mBatteryCharacteritsic;
    private BluetoothGatt mBluetoothGatt;
    private BroadcastReceiver mBondingBroadcastReceiver;
    private HTSManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGattCharacteristic mHTCharacteristic;

    /* renamed from: no.nordicsemi.android.nrftoolbox.hts.HTSManager.1 */
    class C01031 extends BroadcastReceiver {
        C01031() {
        }

        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
            DebugLogger.m18d("HTSManager", "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1));
            if (device.getAddress().equals(HTSManager.this.mBluetoothGatt.getDevice().getAddress()) && bondState == 12) {
                if (HTSManager.this.mHTCharacteristic != null) {
                    HTSManager.this.enableHTIndication();
                }
                HTSManager.this.mContext.unregisterReceiver(this);
                HTSManager.this.mCallbacks.onBonded();
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.hts.HTSManager.2 */
    class C01042 extends BluetoothGattCallback {
        C01042() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                HTSManager.this.mCallbacks.onError(HTSManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == 2) {
                DebugLogger.m18d("HTSManager", "Device connected");
                HTSManager.this.mBluetoothGatt.discoverServices();
                HTSManager.this.mCallbacks.onDeviceConnected();
            } else if (newState == 0) {
                DebugLogger.m18d("HTSManager", "Device disconnected");
                HTSManager.this.mCallbacks.onDeviceDisconnected();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().equals(HTSManager.HT_SERVICE_UUID)) {
                        HTSManager.this.mHTCharacteristic = service.getCharacteristic(HTSManager.HT_MEASUREMENT_CHARACTERISTIC_UUID);
                    } else if (service.getUuid().equals(HTSManager.BATTERY_SERVICE)) {
                        HTSManager.this.mBatteryCharacteritsic = service.getCharacteristic(HTSManager.BATTERY_LEVEL_CHARACTERISTIC);
                    }
                }
                if (HTSManager.this.mHTCharacteristic != null) {
                    HTSManager.this.mCallbacks.onServicesDiscovered(false);
                    if (HTSManager.this.mBatteryCharacteritsic != null) {
                        HTSManager.this.readBatteryLevel();
                        return;
                    } else {
                        HTSManager.this.enableHTIndication();
                        return;
                    }
                }
                HTSManager.this.mCallbacks.onDeviceNotSupported();
                gatt.disconnect();
                return;
            }
            HTSManager.this.mCallbacks.onError(HTSManager.ERROR_DISCOVERY_SERVICE, status);
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH)
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (characteristic.getUuid().equals(HTSManager.BATTERY_LEVEL_CHARACTERISTIC)) {
                    HTSManager.this.mCallbacks.onBatteryValueReceived(characteristic.getValue()[0]);
                    HTSManager.this.enableHTIndication();
                }
            } else if (status != 5) {
                HTSManager.this.mCallbacks.onError(HTSManager.ERROR_READ_CHARACTERISTIC, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w("HTSManager", HTSManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                HTSManager.this.mCallbacks.onError(HTSManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(HTSManager.HT_MEASUREMENT_CHARACTERISTIC_UUID)) {
                try {
                    HTSManager.this.mCallbacks.onHTValueReceived(HTSManager.this.decodeTemperature(characteristic.getValue()));
                } catch (Exception e) {
                    DebugLogger.m19e("HTSManager", "invalid temperature value");
                }
            }
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH)
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status != 0) {
                if (status != 5) {
                    DebugLogger.m19e("HTSManager", "Error on writing descriptor (" + status + ")");
                    HTSManager.this.mCallbacks.onError(HTSManager.ERROR_WRITE_DESCRIPTOR, status);
                } else if (gatt.getDevice().getBondState() == 10) {
                    HTSManager.this.mCallbacks.onBondingRequired();
                    HTSManager.this.mContext.registerReceiver(HTSManager.this.mBondingBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
                } else {
                    DebugLogger.m22w("HTSManager", HTSManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                    HTSManager.this.mCallbacks.onError(HTSManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            }
        }
    }

    public HTSManager() {
        this.TAG = "HTSManager";
        this.HIDE_MSB_8BITS_OUT_OF_32BITS = ViewCompat.MEASURED_SIZE_MASK;
        this.HIDE_MSB_8BITS_OUT_OF_16BITS = MotionEventCompat.ACTION_MASK;
        this.SHIFT_LEFT_8BITS = 8;
        this.SHIFT_LEFT_16BITS = 16;
        this.GET_BIT24 = 4194304;
        this.mBondingBroadcastReceiver = new C01031();
        this.mGattCallback = new C01042();
    }

    static {
        HT_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
        HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
        managerInstance = null;
    }

    public static synchronized HTSManager getHTSManager() {
        HTSManager hTSManager;
        synchronized (HTSManager.class) {
            if (managerInstance == null) {
                managerInstance = new HTSManager();
            }
            hTSManager = managerInstance;
        }
        return hTSManager;
    }

    public void setGattCallbacks(HTSManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void connect(Context context, BluetoothDevice device) {
        this.mBluetoothGatt = device.connectGatt(context, false, this.mGattCallback);
        this.mContext = context;
    }

    public void disconnect() {
        DebugLogger.m18d("HTSManager", "Disconnecting device");
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        }
    }

    public void readBatteryLevel() {
        if (this.mBatteryCharacteritsic != null) {
            this.mBluetoothGatt.readCharacteristic(this.mBatteryCharacteritsic);
        } else {
            DebugLogger.m19e("HTSManager", "Battery Level Characteristic is null");
        }
    }

    private void enableHTIndication() {
        this.mBluetoothGatt.setCharacteristicNotification(this.mHTCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mHTCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        this.mBluetoothGatt.writeDescriptor(descriptor);
    }

    private double decodeTemperature(byte[] data) throws Exception {
        byte flag = data[0];
        byte exponential = data[4];
        double temperatureValue = ((double) getTwosComplimentOfNegativeMantissa((((convertNegativeByteToPositiveShort(data[3]) << 16) | (convertNegativeByteToPositiveShort(data[2]) << 8)) | convertNegativeByteToPositiveShort(data[FIRST_BIT_MASK])) & ViewCompat.MEASURED_SIZE_MASK)) * Math.pow(10.0d, (double) exponential);
        if ((flag & FIRST_BIT_MASK) != 0) {
            return (double) ((float) (((98.6d * temperatureValue) - 32.0d) * 0.5555555555555556d));
        }
        return temperatureValue;
    }

    private short convertNegativeByteToPositiveShort(byte octet) {
        if (octet < 0) {
            return (short) (octet & MotionEventCompat.ACTION_MASK);
        }
        return (short) octet;
    }

    private int getTwosComplimentOfNegativeMantissa(int mantissa) {
        if ((4194304 & mantissa) != 0) {
            return (((mantissa ^ -1) & ViewCompat.MEASURED_SIZE_MASK) + FIRST_BIT_MASK) * -1;
        }
        return mantissa;
    }

    public void closeBluetoothGatt() {
        try {
            this.mContext.unregisterReceiver(this.mBondingBroadcastReceiver);
        } catch (Exception e) {
        }
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
            this.mBatteryCharacteritsic = null;
            this.mHTCharacteristic = null;
        }
    }
}
