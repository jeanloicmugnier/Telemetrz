package no.nordicsemi.android.nrftoolbox.uart;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class UARTManager implements BleManager<UARTManagerCallbacks> {
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final String TAG = "UARTManager";
    private static final UUID UART_RX_CHARACTERISTIC_UUID;
    private static final UUID UART_SERVICE_UUID;
    private static final UUID UART_TX_CHARACTERISTIC_UUID;
    private BluetoothGatt mBluetoothGatt;
    private UARTManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGattCharacteristic mRXCharacteristic;
    private BluetoothGattCharacteristic mTXCharacteristic;

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTManager.1 */
    class C01511 extends BluetoothGattCallback {
        C01511() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                UARTManager.this.mCallbacks.onError(UARTManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == 2) {
                DebugLogger.m18d(UARTManager.TAG, "Device connected");
                UARTManager.this.mBluetoothGatt.discoverServices();
                UARTManager.this.mCallbacks.onDeviceConnected();
            } else if (newState == 0) {
                DebugLogger.m18d(UARTManager.TAG, "Device disconnected");
                UARTManager.this.mCallbacks.onDeviceDisconnected();
                UARTManager.this.closeBluetoothGatt();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().equals(UARTManager.UART_SERVICE_UUID)) {
                        DebugLogger.m18d(UARTManager.TAG, "UART service is found");
                        UARTManager.this.mTXCharacteristic = service.getCharacteristic(UARTManager.UART_TX_CHARACTERISTIC_UUID);
                        UARTManager.this.mRXCharacteristic = service.getCharacteristic(UARTManager.UART_RX_CHARACTERISTIC_UUID);
                    }
                }
                if (UARTManager.this.mTXCharacteristic == null || UARTManager.this.mRXCharacteristic == null) {
                    UARTManager.this.mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                    return;
                }
                UARTManager.this.mCallbacks.onServicesDiscovered(false);
                UARTManager.this.enableRXNotification(gatt);
                return;
            }
            UARTManager.this.mCallbacks.onError(UARTManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status != 0) {
                UARTManager.this.mCallbacks.onError(UARTManager.ERROR_WRITE_DESCRIPTOR, status);
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                UARTManager.this.mCallbacks.onDataSent(characteristic.getStringValue(0));
                return;
            }
            UARTManager.this.mCallbacks.onError(UARTManager.ERROR_WRITE_DESCRIPTOR, status);
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UARTManager.this.mCallbacks.onDataReceived(characteristic.getStringValue(0));
        }
    }

    static {
        UART_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
        UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
        UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public UARTManager(Context context) {
        this.mGattCallback = new C01511();
        this.mContext = context;
    }

    public void setGattCallbacks(UARTManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void connect(Context context, BluetoothDevice device) {
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

    public void send(String text) {
        if (this.mTXCharacteristic != null) {
            this.mTXCharacteristic.setValue(text);
            this.mBluetoothGatt.writeCharacteristic(this.mTXCharacteristic);
        }
    }

    private void enableRXNotification(BluetoothGatt gatt) {
        gatt.setCharacteristicNotification(this.mRXCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mRXCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    public void closeBluetoothGatt() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
            this.mTXCharacteristic = null;
            this.mRXCharacteristic = null;
        }
        this.mCallbacks = null;
    }
}
