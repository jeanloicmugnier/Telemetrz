package no.nordicsemi.android.nrftoolbox.uart;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class UARTEditDialog extends DialogFragment implements OnClickListener, OnItemClickListener {
    private static final String ARG_INDEX = "index";
    private static final String TAG = "UARTEditDialog";
    private int mActiveIcon;
    private CheckBox mCheckBox;
    private EditText mField;
    private IconAdapter mIconAdapter;

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTEditDialog.1 */
    class C01451 implements OnCheckedChangeListener {
        final /* synthetic */ EditText val$field;
        final /* synthetic */ GridView val$grid;

        C01451(EditText editText, GridView gridView) {
            this.val$field = editText;
            this.val$grid = gridView;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            this.val$field.setEnabled(isChecked);
            this.val$grid.setEnabled(isChecked);
            if (UARTEditDialog.this.mIconAdapter != null) {
                UARTEditDialog.this.mIconAdapter.notifyDataSetChanged();
            }
        }
    }

    private class IconAdapter extends BaseAdapter {
        private final int SIZE;

        private IconAdapter() {
            this.SIZE = 20;
        }

        public int getCount() {
            return 20;
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            boolean z = false;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(UARTEditDialog.this.getActivity()).inflate(Integer.valueOf(C0063R.layout.feature_uart_dialog_edit_icon), parent, false);
            }
            ImageView image = (ImageView) view;
            image.setImageLevel(position);
            if (position == UARTEditDialog.this.mActiveIcon && UARTEditDialog.this.mCheckBox.isChecked()) {
                z = true;
            }
            image.setActivated(z);
            return view;
        }
    }

    public static UARTEditDialog getInstance(int index) {
        UARTEditDialog fragment = new UARTEditDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        int index = getArguments().getInt(ARG_INDEX);
        String command = preferences.getString(UARTButtonAdapter.PREFS_BUTTON_COMMAND + index, null);
        this.mActiveIcon = preferences.getInt(UARTButtonAdapter.PREFS_BUTTON_ICON + index, 0);
        View view = inflater.inflate(Integer.valueOf(C0063R.layout.feature_uart_dialog_edit), null);
        EditText field = (EditText) view.findViewById(Integer.valueOf(C0063R.id.field));
        this.mField = field;
        GridView grid = (GridView) view.findViewById(Integer.valueOf(C0063R.id.grid));
        CheckBox checkBox = (CheckBox) view.findViewById(Integer.valueOf(C0063R.id.active));
        this.mCheckBox = checkBox;
        checkBox.setOnCheckedChangeListener(new C01451(field, grid));
        field.setText(command);
        field.setEnabled(true);
        checkBox.setChecked(true);
        grid.setOnItemClickListener(this);
        grid.setEnabled(true);
        ListAdapter iconAdapter = new IconAdapter();
        this.mIconAdapter = (IconAdapter) iconAdapter;
        grid.setAdapter(iconAdapter);
        AlertDialog dialog = new Builder(getActivity()).setCancelable(false).setTitle(Integer.valueOf(C0063R.string.uart_edit_title)).setPositiveButton(Integer.valueOf(17039370), null).setNegativeButton(Integer.valueOf(17039360), null).setView(view).show();
        dialog.getButton(-1).setOnClickListener(this);
        return dialog;
    }

    public void onClick(View v) {
        boolean active = this.mCheckBox.isChecked();
        String command = this.mField.getText().toString();
        if (active && TextUtils.isEmpty(command)) {
            this.mField.setError(getString(Integer.valueOf(C0063R.string.uart_edit_command_error)));
            return;
        }
        this.mField.setError(null);
        int index = getArguments().getInt(ARG_INDEX);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(UARTButtonAdapter.PREFS_BUTTON_COMMAND + index, command);
        editor.putBoolean(UARTButtonAdapter.PREFS_BUTTON_ENABLED + index, active);
        editor.putInt(UARTButtonAdapter.PREFS_BUTTON_ICON + index, this.mActiveIcon);
        editor.commit();
        dismiss();
        ((UARTControlFragment) getParentFragment()).onConfigurationChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mActiveIcon = position;
        this.mIconAdapter.notifyDataSetChanged();
    }
}
