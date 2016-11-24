package no.nordicsemi.android.nrftoolbox.proximity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.proximity.ProximityService.ProximityBinder;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class ProximityActivity extends BleProfileServiceReadyActivity<ProximityBinder> {
    private static final String IMMEDIATE_ALERT_STATUS = "immediate_alert_status";
    public static final String PREFS_GATT_SERVER_ENABLED = "prefs_gatt_server_enabled";
    private static final String TAG = "ProximityActivity";
    private boolean isImmediateAlertOn;
    private Button mFindMeButton;
    private CheckBox mGattServerSwitch;
    private ImageView mLockImage;

    /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityActivity.1 */
    class C01261 implements OnCheckedChangeListener {
        final /* synthetic */ SharedPreferences val$preferences;

        C01261(SharedPreferences sharedPreferences) {
            this.val$preferences = sharedPreferences;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            this.val$preferences.edit().putBoolean(ProximityActivity.PREFS_GATT_SERVER_ENABLED, isChecked).commit();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityActivity.2 */
    class C01272 implements Runnable {
        C01272() {
        }

        public void run() {
            ProximityActivity.this.mFindMeButton.setText(C0063R.string.proximity_action_findme);
            ProximityActivity.this.mLockImage.setImageResource(C0063R.drawable.proximity_lock_closed);
        }
    }

    public ProximityActivity() {
        this.isImmediateAlertOn = false;
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_proximity);
        setGUI();
    }

    private void setGUI() {
        this.mFindMeButton = (Button) findViewById(C0063R.id.action_findme);
        this.mLockImage = (ImageView) findViewById(C0063R.id.imageLock);
        this.mGattServerSwitch = (CheckBox) findViewById(C0063R.id.option);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.mGattServerSwitch.setChecked(preferences.getBoolean(PREFS_GATT_SERVER_ENABLED, true));
        this.mGattServerSwitch.setOnCheckedChangeListener(new C01261(preferences));
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMMEDIATE_ALERT_STATUS, this.isImmediateAlertOn);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.isImmediateAlertOn = savedInstanceState.getBoolean(IMMEDIATE_ALERT_STATUS);
        if (isDeviceConnected()) {
            showOpenLock();
            if (this.isImmediateAlertOn) {
                showSilentMeOnButton();
            }
        }
    }

    protected int getLoggerProfileTitle() {
        return C0063R.string.proximity_feature_title;
    }

    protected void onServiceBinded(ProximityBinder binder) {
    }

    protected void onServiceUnbinded() {
    }

    protected Class<? extends BleProfileService> getServiceClass() {
        return ProximityService.class;
    }

    protected int getAboutTextId() {
        return C0063R.string.proximity_about_text;
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.proximity_default_name;
    }

    protected UUID getFilterUUID() {
        return ProximityManager.LINKLOSS_SERVICE_UUID;
    }

    public void onFindMeClicked(View view) {
        if (!isBLEEnabled()) {
            showBLEDialog();
        } else if (!isDeviceConnected()) {
        } else {
            if (this.isImmediateAlertOn) {
                showFindMeOnButton();
                ((ProximityBinder) getService()).stopImmediateAlert();
                this.isImmediateAlertOn = false;
                return;
            }
            showSilentMeOnButton();
            ((ProximityBinder) getService()).startImmediateAlert();
            this.isImmediateAlertOn = true;
        }
    }

    protected void setDefaultUI() {
        runOnUiThread(new C01272());
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    public void onDeviceConnected() {
        super.onDeviceConnected();
        showOpenLock();
        this.mGattServerSwitch.setEnabled(false);
    }

    public void onDeviceDisconnected() {
        super.onDeviceDisconnected();
        showClosedLock();
        this.mGattServerSwitch.setEnabled(true);
    }

    public void onBondingRequired() {
        showClosedLock();
    }

    public void onBonded() {
        showOpenLock();
    }

    public void onLinklossOccur() {
        super.onLinklossOccur();
        showClosedLock();
        resetForLinkloss();
        DebugLogger.m22w(TAG, "Linkloss occur");
        String deviceName = getDeviceName();
        if (deviceName == null) {
            deviceName = getString(C0063R.string.proximity_default_name);
        }
        showLinklossDialog(deviceName);
    }

    private void resetForLinkloss() {
        this.isImmediateAlertOn = false;
        setDefaultUI();
    }

    private void showFindMeOnButton() {
        this.mFindMeButton.setText(C0063R.string.proximity_action_findme);
    }

    private void showSilentMeOnButton() {
        this.mFindMeButton.setText(C0063R.string.proximity_action_silentme);
    }

    private void showOpenLock() {
        this.mFindMeButton.setEnabled(true);
        this.mLockImage.setImageResource(C0063R.drawable.proximity_lock_open);
    }

    private void showClosedLock() {
        this.mFindMeButton.setEnabled(false);
        this.mLockImage.setImageResource(C0063R.drawable.proximity_lock_closed);
    }

    private void showLinklossDialog(String name) {
        try {
            LinklossFragment.getInstance(name).show(getFragmentManager(), "scan_fragment");
        } catch (Exception e) {
        }
    }
}
