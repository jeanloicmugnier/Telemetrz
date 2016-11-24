package no.nordicsemi.android.nrftoolbox.dfu.settings;

import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.widget.TextView;
import no.nordicsemi.android.dfu.DfuSettingsConstants;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class SettingsFragment extends PreferenceFragment implements DfuSettingsConstants, OnSharedPreferenceChangeListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(C0063R.xml.settings_dfu);
        updateNumberOfPacketsSummary();
        updateMBRSize();
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
        boolean disabled = true;
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        if (DfuSettingsConstants.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED.equals(key)) {
            if (preferences.getBoolean(DfuSettingsConstants.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, true)) {
                disabled = false;
            }
            if (disabled) {
                TextView view = (TextView) LayoutInflater.from(getActivity()).inflate(C0063R.layout.dialog_about_text, null);
                view.setText(C0063R.string.dfu_settings_dfu_number_of_packets_info);
                new Builder(getActivity()).setView(view).setTitle(C0063R.string.dfu_settings_dfu_information).setNeutralButton(17039370, null).show();
            }
        } else if (DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS.equals(key)) {
            updateNumberOfPacketsSummary();
        } else if (DfuSettingsConstants.SETTINGS_MBR_SIZE.equals(key)) {
            updateMBRSize();
        }
    }

    private void updateNumberOfPacketsSummary() {
        PreferenceScreen screen = getPreferenceScreen();
        String value = getPreferenceManager().getSharedPreferences().getString(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS, String.valueOf(10));
        screen.findPreference(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS).setSummary(value);
        if (Integer.parseInt(value) > 200) {
            TextView view = (TextView) LayoutInflater.from(getActivity()).inflate(C0063R.layout.dialog_about_text, null);
            view.setText(C0063R.string.dfu_settings_dfu_number_of_packets_info);
            new Builder(getActivity()).setView(view).setTitle(C0063R.string.dfu_settings_dfu_information).setNeutralButton(17039370, null).show();
        }
    }

    private void updateMBRSize() {
        PreferenceScreen screen = getPreferenceScreen();
        screen.findPreference(DfuSettingsConstants.SETTINGS_MBR_SIZE).setSummary(getPreferenceManager().getSharedPreferences().getString(DfuSettingsConstants.SETTINGS_MBR_SIZE, String.valueOf(DfuSettingsConstants.SETTINGS_DEFAULT_MBR_SIZE)));
    }
}
