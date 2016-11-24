package no.nordicsemi.android.nrftoolbox.rsc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.rsc.RSCService.RSCBinder;

public class RSCActivity extends BleProfileServiceReadyActivity<RSCBinder> {
    private TextView mActivityView;
    private BroadcastReceiver mBroadcastReceiver;
    private TextView mCadenceView;
    private TextView mDistanceUnitView;
    private TextView mDistanceView;
    private TextView mSpeedView;
    private TextView mStridesCountView;
    private TextView mTotalDistanceView;

    /* renamed from: no.nordicsemi.android.nrftoolbox.rsc.RSCActivity.1 */
    class C01331 extends BroadcastReceiver {
        C01331() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RSCService.BROADCAST_RSC_MEASUREMENT.equals(action)) {
                RSCActivity.this.onMeasurementReceived(intent.getFloatExtra(RSCService.EXTRA_SPEED, 0.0f), intent.getIntExtra(RSCService.EXTRA_CADENCE, 0), intent.getFloatExtra(RSCService.EXTRA_TOTAL_DISTANCE, -1.0f), intent.getIntExtra(RSCService.EXTRA_ACTIVITY, 0));
            } else if (RSCService.BROADCAST_STRIDES_UPDATE.equals(action)) {
                int strides = intent.getIntExtra(RSCService.EXTRA_STRIDES, 0);
                RSCActivity.this.onStripsesUpdate(intent.getFloatExtra(RSCService.EXTRA_DISTANCE, 0.0f), strides);
            }
        }
    }

    public RSCActivity() {
        this.mBroadcastReceiver = new C01331();
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_rsc);
        setGui();
    }

    protected void onInitialize(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mBroadcastReceiver, makeIntentFilter());
    }

    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mBroadcastReceiver);
    }

    private void setGui() {
        this.mSpeedView = (TextView) findViewById(C0063R.id.speed);
        this.mCadenceView = (TextView) findViewById(C0063R.id.cadence);
        this.mDistanceView = (TextView) findViewById(C0063R.id.distance);
        this.mDistanceUnitView = (TextView) findViewById(C0063R.id.distance_unit);
        this.mTotalDistanceView = (TextView) findViewById(C0063R.id.total_distance);
        this.mStridesCountView = (TextView) findViewById(C0063R.id.strides);
        this.mActivityView = (TextView) findViewById(C0063R.id.activity);
    }

    protected void setDefaultUI() {
        this.mSpeedView.setText(C0063R.string.not_available_value);
        this.mCadenceView.setText(C0063R.string.not_available_value);
        this.mDistanceView.setText(C0063R.string.not_available_value);
        this.mDistanceUnitView.setText(C0063R.string.rsc_distance_unit_m);
        this.mTotalDistanceView.setText(C0063R.string.not_available_value);
        this.mStridesCountView.setText(C0063R.string.not_available_value);
        this.mActivityView.setText(C0063R.string.not_available);
    }

    protected int getLoggerProfileTitle() {
        return C0063R.string.rsc_feature_title;
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.rsc_default_name;
    }

    protected int getAboutTextId() {
        return C0063R.string.rsc_about_text;
    }

    protected Class<? extends BleProfileService> getServiceClass() {
        return RSCService.class;
    }

    protected UUID getFilterUUID() {
        return RSCManager.RUNNING_SPEED_AND_CADENCE_SERVICE_UUID;
    }

    protected void onServiceBinded(RSCBinder binder) {
    }

    protected void onServiceUnbinded() {
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    private void onMeasurementReceived(float speed, int cadence, float totalDistance, int activity) {
        this.mSpeedView.setText(String.format("%.1f", new Object[]{Float.valueOf(speed)}));
        this.mCadenceView.setText(String.format("%d", new Object[]{Integer.valueOf(cadence)}));
        if (totalDistance == -1.0f) {
            this.mTotalDistanceView.setText(C0063R.string.not_available);
        } else {
            this.mTotalDistanceView.setText(String.format("%.2f", new Object[]{Float.valueOf(totalDistance / 10000.0f)}));
        }
        this.mActivityView.setText(activity == 1 ? C0063R.string.rsc_running : C0063R.string.rsc_walking);
    }

    private void onStripsesUpdate(float distance, int strides) {
        if (distance == -1.0f) {
            this.mDistanceView.setText(C0063R.string.not_available);
            this.mDistanceUnitView.setText(C0063R.string.rsc_distance_unit_m);
        } else if (distance < 100000.0f) {
            this.mDistanceView.setText(String.format("%.0f", new Object[]{Float.valueOf(distance / 100.0f)}));
            this.mDistanceUnitView.setText(C0063R.string.rsc_distance_unit_m);
        } else {
            this.mDistanceView.setText(String.format("%.2f", new Object[]{Float.valueOf(distance / 100000.0f)}));
            this.mDistanceUnitView.setText(C0063R.string.rsc_distance_unit_km);
        }
        this.mStridesCountView.setText(String.valueOf(strides));
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RSCService.BROADCAST_RSC_MEASUREMENT);
        intentFilter.addAction(RSCService.BROADCAST_STRIDES_UPDATE);
        return intentFilter;
    }
}
