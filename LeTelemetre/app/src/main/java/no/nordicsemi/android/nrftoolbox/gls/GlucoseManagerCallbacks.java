package no.nordicsemi.android.nrftoolbox.gls;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface GlucoseManagerCallbacks extends BleManagerCallbacks {
    public static final int UNIT_kPa = 1;
    public static final int UNIT_mmHG = 0;

    void onDatasetChanged();

    void onGlucoseMeasurementContextNotificationEnabled();

    void onGlucoseMeasurementNotificationEnabled();

    void onNumberOfRecordsRequested(int i);

    void onOperationAborted();

    void onOperationCompleted();

    void onOperationFailed();

    void onOperationNotSupported();

    void onOperationStarted();

    void onRecordAccessControlPointIndicationsEnabled();
}
