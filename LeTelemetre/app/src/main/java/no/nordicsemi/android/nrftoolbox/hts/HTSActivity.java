package no.nordicsemi.android.nrftoolbox.hts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.hts.HTSService.RSCBinder;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

public class HTSActivity extends BleProfileServiceReadyActivity<RSCBinder> {
    private final String TAG;
    private BroadcastReceiver mBroadcastReceiver;
    private TextView mHTSValue;

    /* renamed from: no.nordicsemi.android.nrftoolbox.hts.HTSActivity.1 */
    class C01021 extends BroadcastReceiver {
        C01021() {
        }

        public void onReceive(Context context, Intent intent) {
            if (HTSService.BROADCAST_HTS_MEASUREMENT.equals(intent.getAction())) {
                HTSActivity.this.setHTSValueOnView(intent.getDoubleExtra(HTSService.EXTRA_TEMPERATURE, 0.0d));
            }
        }
    }

    public HTSActivity() {
        this.TAG = "HTSActivity";
        this.mBroadcastReceiver = new C01021();
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_hts);
        setGUI();
    }

    protected void onInitialize(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mBroadcastReceiver, makeIntentFilter());
    }

    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mBroadcastReceiver);
    }

    private void setGUI() {
        this.mHTSValue = (TextView) findViewById(C0063R.id.text_hts_value);
    }

    protected void onServiceBinded(RSCBinder binder) {
    }

    protected void onServiceUnbinded() {
    }

    protected int getAboutTextId() {
        return C0063R.string.hts_about_text;
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.hts_default_name;
    }

    protected UUID getFilterUUID() {
        return HTSManager.HT_SERVICE_UUID;
    }

    protected Class<? extends BleProfileService> getServiceClass() {
        return HTSService.class;
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    private void setHTSValueOnView(double value) {
        this.mHTSValue.setText(new DecimalFormat("#0.00").format(value));
    }

    protected void setDefaultUI() {
        this.mHTSValue.setText(C0063R.string.not_available_value);
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HTSService.BROADCAST_HTS_MEASUREMENT);
        return intentFilter;
    }
}
