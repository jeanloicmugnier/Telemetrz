package no.nordicsemi.android.nrftoolbox.csc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.widget.TextView;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.csc.CSCService.CSCBinder;
import no.nordicsemi.android.nrftoolbox.csc.settings.SettingsActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

public class CSCActivity extends BleProfileServiceReadyActivity<CSCBinder> {
    private BroadcastReceiver mBroadcastReceiver;
    private TextView mCadenceView;
    private TextView mDistanceUnitView;
    private TextView mDistanceView;
    private TextView mGearRatioView;
    private TextView mSpeedView;
    private TextView mTotalDistanceView;

    /* renamed from: no.nordicsemi.android.nrftoolbox.csc.CSCActivity.1 */
    class C00721 extends BroadcastReceiver {
        C00721() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CSCService.BROADCAST_WHEEL_DATA.equals(action)) {
                CSCActivity.this.onMeasurementReceived(intent.getFloatExtra(CSCService.EXTRA_SPEED, 0.0f), intent.getFloatExtra(CSCService.EXTRA_DISTANCE, -1.0f), intent.getFloatExtra(CSCService.EXTRA_TOTAL_DISTANCE, -1.0f));
            } else if (CSCService.BROADCAST_CRANK_DATA.equals(action)) {
                CSCActivity.this.onGearRatioUpdate(intent.getFloatExtra(CSCService.EXTRA_GEAR_RATIO, 0.0f), intent.getIntExtra(CSCService.EXTRA_CADENCE, 0));
            }
        }
    }

    public CSCActivity() {
        this.mBroadcastReceiver = new C00721();
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_csc);
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
        this.mTotalDistanceView = (TextView) findViewById(C0063R.id.distance_total);
        this.mGearRatioView = (TextView) findViewById(C0063R.id.ratio);
    }

    protected void setDefaultUI() {
        this.mSpeedView.setText(C0063R.string.not_available_value);
        this.mCadenceView.setText(C0063R.string.not_available_value);
        this.mDistanceView.setText(C0063R.string.not_available_value);
        this.mDistanceUnitView.setText(C0063R.string.csc_distance_unit_m);
        this.mTotalDistanceView.setText(C0063R.string.not_available_value);
        this.mGearRatioView.setText(C0063R.string.not_available_value);
    }

    protected int getLoggerProfileTitle() {
        return C0063R.string.csc_feature_title;
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.csc_default_name;
    }

    protected int getAboutTextId() {
        return C0063R.string.csc_about_text;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0063R.menu.csc_menu, menu);
        return true;
    }

    protected boolean onOptionsItemSelected(int itemId) {
        switch (itemId) {
            case C0063R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return true;
    }

    protected Class<? extends BleProfileService> getServiceClass() {
        return CSCService.class;
    }

    protected UUID getFilterUUID() {
        return CSCManager.CYCLING_SPEED_AND_CADENCE_SERVICE_UUID;
    }

    protected void onServiceBinded(CSCBinder binder) {
    }

    protected void onServiceUnbinded() {
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    private void onMeasurementReceived(float speed, float distance, float totalDistance) {
        this.mSpeedView.setText(String.format("%.1f", new Object[]{Float.valueOf(speed)}));
        if (distance < 1000.0f) {
            this.mDistanceView.setText(String.format("%.0f", new Object[]{Float.valueOf(distance)}));
            this.mDistanceUnitView.setText(C0063R.string.csc_distance_unit_m);
        } else {
            this.mDistanceView.setText(String.format("%.2f", new Object[]{Float.valueOf(distance / 1000.0f)}));
            this.mDistanceUnitView.setText(C0063R.string.csc_distance_unit_km);
        }
        this.mTotalDistanceView.setText(String.format("%.2f", new Object[]{Float.valueOf(totalDistance / 1000.0f)}));
    }

    private void onGearRatioUpdate(float ratio, int cadence) {
        this.mGearRatioView.setText(String.format("%.1f", new Object[]{Float.valueOf(ratio)}));
        this.mCadenceView.setText(String.format("%d", new Object[]{Integer.valueOf(cadence)}));
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CSCService.BROADCAST_WHEEL_DATA);
        intentFilter.addAction(CSCService.BROADCAST_CRANK_DATA);
        return intentFilter;
    }
}
