package no.nordicsemi.android.nrftoolbox.csc.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    public static final String SETTINGS_WHEEL_SIZE = "settings_wheel_size";
    public static final int SETTINGS_WHEEL_SIZE_DEFAULT = 2340;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(C0063R.xml.settings_csc);
        updateWheelSizeSummary();
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SETTINGS_WHEEL_SIZE.equals(key)) {
            updateWheelSizeSummary();
        }
    }

    private void updateWheelSizeSummary() {
        PreferenceScreen screen = getPreferenceScreen();
        String value = getPreferenceManager().getSharedPreferences().getString(SETTINGS_WHEEL_SIZE, String.valueOf(SETTINGS_WHEEL_SIZE_DEFAULT));
        screen.findPreference(SETTINGS_WHEEL_SIZE).setSummary(getString(C0063R.string.csc_settings_wheel_diameter_summary, new Object[]{value}));
    }
}
