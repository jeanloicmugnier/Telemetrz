package no.nordicsemi.android.nrftoolbox.hts;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface HTSManagerCallbacks extends BleManagerCallbacks {
    void onHTValueReceived(double d);
}
