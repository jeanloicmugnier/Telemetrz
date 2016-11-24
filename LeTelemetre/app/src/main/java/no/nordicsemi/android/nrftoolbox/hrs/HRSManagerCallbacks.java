package no.nordicsemi.android.nrftoolbox.hrs;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface HRSManagerCallbacks extends BleManagerCallbacks {
    void onHRNotificationEnabled();

    void onHRSensorPositionFound(String str);

    void onHRValueReceived(int i);
}
