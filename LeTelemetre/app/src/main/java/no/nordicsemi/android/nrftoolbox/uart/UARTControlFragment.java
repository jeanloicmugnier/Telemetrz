package no.nordicsemi.android.nrftoolbox.uart;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class UARTControlFragment extends Fragment implements OnItemClickListener {
    private static final String SIS_EDIT_MODE = "sis_edit_mode";
    private static final String TAG = "UARTControlFragment";
    private UARTButtonAdapter mAdapter;
    private boolean mEditMode;
    private ControlFragmentListener mListener;
    private SharedPreferences mPreferences;

    public interface ControlFragmentListener {
        void setEditMode(boolean z);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (ControlFragmentListener) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, "The parten activity must implement EditModeListener");
        }
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (savedInstanceState != null) {
            this.mEditMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SIS_EDIT_MODE, this.mEditMode);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(Integer.valueOf(C0063R.layout.fragment_feature_uart_control), container, false);
        GridView grid = (GridView) view.findViewById(Integer.valueOf(C0063R.id.grid));
        ListAdapter uARTButtonAdapter = new UARTButtonAdapter(getActivity());
        this.mAdapter = (UARTButtonAdapter) uARTButtonAdapter;
        grid.setAdapter(uARTButtonAdapter);
        grid.setOnItemClickListener(this);
        this.mAdapter.setEditMode(this.mEditMode);
        setHasOptionsMenu(true);
        return view;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mEditMode) {
            UARTEditDialog.getInstance(position).show(getChildFragmentManager(), null);
        } else {
            ((UARTInterface) getActivity()).send(this.mPreferences.getString(UARTButtonAdapter.PREFS_BUTTON_COMMAND + position, ""));
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(this.mEditMode ? C0063R.menu.uart_menu_config : C0063R.menu.uart_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        switch (item.getItemId()) {
            case C0063R.id.action_configure:
                if (!this.mEditMode) {
                    z = true;
                }
                setEditMode(z);
                return true;
            default:
                return false;
        }
    }

    public void setEditMode(boolean editMode) {
        this.mEditMode = editMode;
        this.mAdapter.setEditMode(this.mEditMode);
        getActivity().invalidateOptionsMenu();
        this.mListener.setEditMode(this.mEditMode);
    }

    public void onConfigurationChanged() {
        this.mAdapter.notifyDataSetChanged();
    }
}
