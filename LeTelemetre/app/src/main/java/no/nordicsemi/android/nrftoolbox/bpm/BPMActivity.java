package no.nordicsemi.android.nrftoolbox.bpm;

import android.os.Bundle;
import android.widget.TextView;
import java.util.Calendar;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;

public class BPMActivity extends BleProfileActivity implements BPMManagerCallbacks {
    private static final String TAG = "BPMActivity";
    private TextView mDiastolicUnitView;
    private TextView mDiastolicView;
    private TextView mMeanAPUnitView;
    private TextView mMeanAPView;
    private TextView mPulseView;
    private TextView mSystolicUnitView;
    private TextView mSystolicView;
    private TextView mTimestampView;

    /* renamed from: no.nordicsemi.android.nrftoolbox.bpm.BPMActivity.1 */
    class C00661 implements Runnable {
        final /* synthetic */ float val$diastolic;
        final /* synthetic */ float val$meanArterialPressure;
        final /* synthetic */ float val$systolic;
        final /* synthetic */ int val$unit;

        C00661(float f, float f2, float f3, int i) {
            this.val$systolic = f;
            this.val$diastolic = f2;
            this.val$meanArterialPressure = f3;
            this.val$unit = i;
        }

        public void run() {
            int i;
            int i2 = C0063R.string.bpm_unit_mmhg;
            BPMActivity.this.mSystolicView.setText(Float.toString(this.val$systolic));
            BPMActivity.this.mDiastolicView.setText(Float.toString(this.val$diastolic));
            BPMActivity.this.mMeanAPView.setText(Float.toString(this.val$meanArterialPressure));
            TextView access$300 = BPMActivity.this.mSystolicUnitView;
            if (this.val$unit == 0) {
                i = C0063R.string.bpm_unit_mmhg;
            } else {
                i = C0063R.string.bpm_unit_kpa;
            }
            access$300.setText(i);
            access$300 = BPMActivity.this.mDiastolicUnitView;
            if (this.val$unit == 0) {
                i = C0063R.string.bpm_unit_mmhg;
            } else {
                i = C0063R.string.bpm_unit_kpa;
            }
            access$300.setText(i);
            TextView access$500 = BPMActivity.this.mMeanAPUnitView;
            if (this.val$unit != 0) {
                i2 = C0063R.string.bpm_unit_kpa;
            }
            access$500.setText(i2);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.bpm.BPMActivity.2 */
    class C00672 implements Runnable {
        final /* synthetic */ float val$cuffPressure;
        final /* synthetic */ int val$unit;

        C00672(float f, int i) {
            this.val$cuffPressure = f;
            this.val$unit = i;
        }

        public void run() {
            BPMActivity.this.mSystolicView.setText(Float.toString(this.val$cuffPressure));
            BPMActivity.this.mDiastolicView.setText(C0063R.string.not_available_value);
            BPMActivity.this.mMeanAPView.setText(C0063R.string.not_available_value);
            BPMActivity.this.mSystolicUnitView.setText(this.val$unit == 0 ? C0063R.string.bpm_unit_mmhg : C0063R.string.bpm_unit_kpa);
            BPMActivity.this.mDiastolicUnitView.setText(null);
            BPMActivity.this.mMeanAPUnitView.setText(null);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.bpm.BPMActivity.3 */
    class C00683 implements Runnable {
        final /* synthetic */ float val$pulseRate;

        C00683(float f) {
            this.val$pulseRate = f;
        }

        public void run() {
            if (this.val$pulseRate >= 0.0f) {
                BPMActivity.this.mPulseView.setText(Float.toString(this.val$pulseRate));
            } else {
                BPMActivity.this.mPulseView.setText(C0063R.string.not_available_value);
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.bpm.BPMActivity.4 */
    class C00694 implements Runnable {
        final /* synthetic */ Calendar val$calendar;

        C00694(Calendar calendar) {
            this.val$calendar = calendar;
        }

        public void run() {
            if (this.val$calendar != null) {
                BPMActivity.this.mTimestampView.setText(BPMActivity.this.getString(C0063R.string.bpm_timestamp, new Object[]{this.val$calendar}));
                return;
            }
            BPMActivity.this.mTimestampView.setText(C0063R.string.not_available);
        }
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_bpm);
        setGUI();
    }

    private void setGUI() {
        this.mSystolicView = (TextView) findViewById(C0063R.id.systolic);
        this.mSystolicUnitView = (TextView) findViewById(C0063R.id.systolic_unit);
        this.mDiastolicView = (TextView) findViewById(C0063R.id.diastolic);
        this.mDiastolicUnitView = (TextView) findViewById(C0063R.id.diastolic_unit);
        this.mMeanAPView = (TextView) findViewById(C0063R.id.mean_ap);
        this.mMeanAPUnitView = (TextView) findViewById(C0063R.id.mean_ap_unit);
        this.mPulseView = (TextView) findViewById(C0063R.id.pulse);
        this.mTimestampView = (TextView) findViewById(C0063R.id.timestamp);
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.bpm_default_name;
    }

    protected int getAboutTextId() {
        return C0063R.string.bpm_about_text;
    }

    protected UUID getFilterUUID() {
        return BPMManager.BP_SERVICE_UUID;
    }

    protected BleManager<BPMManagerCallbacks> initializeManager() {
        BPMManager manager = BPMManager.getBPMManager();
        manager.setGattCallbacks((BPMManagerCallbacks) this);
        return manager;
    }

    protected void setDefaultUI() {
        this.mSystolicView.setText(C0063R.string.not_available_value);
        this.mSystolicUnitView.setText(null);
        this.mDiastolicView.setText(C0063R.string.not_available_value);
        this.mDiastolicUnitView.setText(null);
        this.mMeanAPView.setText(C0063R.string.not_available_value);
        this.mMeanAPUnitView.setText(null);
        this.mPulseView.setText(C0063R.string.not_available_value);
        this.mTimestampView.setText(C0063R.string.not_available);
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    public void onBloodPressureMeasurementIndicationsEnabled() {
    }

    public void onIntermediateCuffPressureNotificationEnabled() {
    }

    public void onBloodPressureMeasurmentRead(float systolic, float diastolic, float meanArterialPressure, int unit) {
        runOnUiThread(new C00661(systolic, diastolic, meanArterialPressure, unit));
    }

    public void onIntermediateCuffPressureRead(float cuffPressure, int unit) {
        runOnUiThread(new C00672(cuffPressure, unit));
    }

    public void onPulseRateRead(float pulseRate) {
        runOnUiThread(new C00683(pulseRate));
    }

    public void onTimestampRead(Calendar calendar) {
        runOnUiThread(new C00694(calendar));
    }
}
