package no.nordicsemi.android.nrftoolbox.csc;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface CSCManagerCallbacks extends BleManagerCallbacks {
    public static final int NOT_AVAILABLE = -1;

    void onCrankMeasurementReceived(int i, int i2);

    void onWheelMeasurementReceived(int i, int i2);
}
