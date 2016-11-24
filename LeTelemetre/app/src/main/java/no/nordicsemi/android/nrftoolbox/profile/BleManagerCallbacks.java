package no.nordicsemi.android.nrftoolbox.profile;

public interface BleManagerCallbacks {
    void onBatteryValueReceived(int i);

    void onBonded();

    void onBondingRequired();

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onDeviceNotSupported();

    void onError(String str, int i);

    void onLinklossOccur();

    void onServicesDiscovered(boolean z);
}
