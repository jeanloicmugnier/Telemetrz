package no.nordicsemi.android.nrftoolbox.uart;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class UARTButtonAdapter extends BaseAdapter {
    public static final String PREFS_BUTTON_COMMAND = "prefs_uart_command_";
    public static final String PREFS_BUTTON_ENABLED = "prefs_uart_enabled_";
    public static final String PREFS_BUTTON_ICON = "prefs_uart_icon_";
    private boolean mEditMode;
    private final boolean[] mEnableFlags;
    private final int[] mIcons;
    private final SharedPreferences mPreferences;

    public UARTButtonAdapter(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mIcons = new int[9];
        this.mEnableFlags = new boolean[9];
    }

    public void setEditMode(boolean editMode) {
        this.mEditMode = editMode;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        SharedPreferences preferences = this.mPreferences;
        for (int i = 0; i < this.mIcons.length; i++) {
            this.mIcons[i] = preferences.getInt(PREFS_BUTTON_ICON + i, -1);
            this.mEnableFlags[i] = preferences.getBoolean(PREFS_BUTTON_ENABLED + i, false);
        }
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return this.mIcons.length;
    }

    public Object getItem(int position) {
        return Integer.valueOf(this.mIcons[position]);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return this.mEditMode || this.mEnableFlags[position];
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(C0063R.layout.feature_uart_button, parent, false);
        }
        view.setEnabled(isEnabled(position));
        view.setActivated(this.mEditMode);
        ImageView image = (ImageView) view;
        int icon = this.mIcons[position];
        if (!this.mEnableFlags[position] || icon == -1) {
            image.setImageDrawable(null);
        } else {
            image.setImageResource(C0063R.drawable.uart_button);
            image.setImageLevel(icon);
        }
        return view;
    }
}
