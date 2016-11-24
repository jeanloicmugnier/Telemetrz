package no.nordicsemi.android.nrftoolbox.rsc;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface RSCManagerCallbacks extends BleManagerCallbacks {
    public static final int ACTIVITY_RUNNING = 1;
    public static final int ACTIVITY_WALKING = 0;
    public static final int NOT_AVAILABLE = -1;

    void onMeasurementReceived(float f, int i, float f2, float f3, int i2);
}
