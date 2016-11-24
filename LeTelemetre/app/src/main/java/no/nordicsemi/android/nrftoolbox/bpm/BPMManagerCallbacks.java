package no.nordicsemi.android.nrftoolbox.bpm;

import java.util.Calendar;
import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface BPMManagerCallbacks extends BleManagerCallbacks {
    public static final int UNIT_kPa = 1;
    public static final int UNIT_mmHG = 0;

    void onBloodPressureMeasurementIndicationsEnabled();

    void onBloodPressureMeasurmentRead(float f, float f2, float f3, int i);

    void onIntermediateCuffPressureNotificationEnabled();

    void onIntermediateCuffPressureRead(float f, int i);

    void onPulseRateRead(float f);

    void onTimestampRead(Calendar calendar);
}
