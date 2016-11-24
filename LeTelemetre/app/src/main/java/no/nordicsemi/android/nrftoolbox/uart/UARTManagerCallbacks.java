package no.nordicsemi.android.nrftoolbox.uart;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface UARTManagerCallbacks extends BleManagerCallbacks {
    void onDataReceived(String str);

    void onDataSent(String str);
}
