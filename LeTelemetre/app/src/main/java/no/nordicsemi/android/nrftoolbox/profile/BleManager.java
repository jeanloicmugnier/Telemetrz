package no.nordicsemi.android.nrftoolbox.profile;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

public interface BleManager<E extends BleManagerCallbacks> {
    void closeBluetoothGatt();

    void connect(Context context, BluetoothDevice bluetoothDevice);

    void disconnect();

    void setGattCallbacks(E e);
}
