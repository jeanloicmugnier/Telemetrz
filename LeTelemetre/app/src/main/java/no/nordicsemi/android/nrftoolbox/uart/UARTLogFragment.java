package no.nordicsemi.android.nrftoolbox.uart;

import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.localprovider.*;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.uart.UARTService.UARTBinder;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import org.achartengine.tools.Zoom;

public class UARTLogFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private static final String[] LOG_PROJECTION;
    private static final int LOG_REQUEST_ID = 1;
    private static final int LOG_SCROLLED_TO_BOTTOM = -2;
    private static final int LOG_SCROLL_NULL = -1;
    private static final String SIS_LOG_SCROLL_POSITION = "sis_scroll_position";
    private BroadcastReceiver mCommonBroadcastReceiver;
    private EditText mField;
    private CursorAdapter mLogAdapter;
    private int mLogScrollPosition;
    private ILogSession mLogSession;
    private Button mSendButton;
    private ServiceConnection mServiceConnection;
    private UARTInterface mUARTInterface;

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTLogFragment.1 */
    class C01471 extends BroadcastReceiver {
        C01471() {
        }

        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, 0)) {
                case Zoom.ZOOM_AXIS_XY /*0*/:
                    UARTLogFragment.this.onDeviceDisconnected();
                case UARTLogFragment.LOG_REQUEST_ID /*1*/:
                    UARTLogFragment.this.onDeviceConnected();
                default:
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTLogFragment.2 */
    class C01482 implements ServiceConnection {
        C01482() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            UARTBinder bleService = (UARTBinder) service;
            UARTLogFragment.this.mUARTInterface = bleService;
            UARTLogFragment.this.mLogSession = bleService.getLogSession();
            if (UARTLogFragment.this.mLogSession != null) {
                UARTLogFragment.this.getLoaderManager().restartLoader(UARTLogFragment.LOG_REQUEST_ID, null, UARTLogFragment.this);
            }
            if (bleService.isConnected()) {
                UARTLogFragment.this.onDeviceConnected();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            UARTLogFragment.this.onDeviceDisconnected();
            UARTLogFragment.this.mUARTInterface = null;
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTLogFragment.3 */
    class C01493 implements OnEditorActionListener {
        C01493() {
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId != 4) {
                return false;
            }
            UARTLogFragment.this.onSendClicked();
            return true;
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTLogFragment.4 */
    class C01504 implements OnClickListener {
        C01504() {
        }

        public void onClick(View v) {
            UARTLogFragment.this.onSendClicked();
        }
    }

    public UARTLogFragment() {
        this.mCommonBroadcastReceiver = new C01471();
        this.mServiceConnection = new C01482();
    }

    static {
        LOG_PROJECTION = new String[]{"_id", LogContract.LogColumns.TIME, LogContract.LogColumns.LEVEL, LogContract.LogColumns.DATA};
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.mCommonBroadcastReceiver, makeIntentFilter());
        if (savedInstanceState != null) {
            this.mLogScrollPosition = savedInstanceState.getInt(SIS_LOG_SCROLL_POSITION);
        }
    }

    public void onStart() {
        super.onStart();
        getActivity().bindService(new Intent(getActivity(), UARTService.class), this.mServiceConnection, 0);
    }

    public void onStop() {
        super.onStop();
        try {
            getActivity().unbindService(this.mServiceConnection);
            this.mUARTInterface = null;
        } catch (IllegalArgumentException e) {
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ListView list = getListView();
        boolean scrolledToBottom = list.getCount() > 0 && list.getLastVisiblePosition() == list.getCount() + LOG_SCROLL_NULL;
        outState.putInt(SIS_LOG_SCROLL_POSITION, scrolledToBottom ? LOG_SCROLLED_TO_BOTTOM : list.getFirstVisiblePosition());
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.mCommonBroadcastReceiver);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(Integer.valueOf(C0063R.layout.fragment_feature_uart_log), container, false);
        EditText field = (EditText) view.findViewById(Integer.valueOf(C0063R.id.field));
        this.mField = field;
        field.setOnEditorActionListener(new C01493());
        Button sendButton = (Button) view.findViewById(Integer.valueOf(C0063R.id.action_send));
        this.mSendButton = sendButton;
        sendButton.setOnClickListener(new C01504());
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mLogAdapter = new UARTLogAdapter(getActivity());
        setListAdapter(this.mLogAdapter);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOG_REQUEST_ID /*1*/:
                return new CursorLoader(getActivity(), this.mLogSession.getSessionEntriesUri(), LOG_PROJECTION, null, null, LogContract.LogColumns.TIME);
            default:
                return null;
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean scrolledToBottom;
        ListView list = getListView();
        int position = this.mLogScrollPosition;
        if (position == LOG_SCROLLED_TO_BOTTOM || (list.getCount() > 0 && list.getLastVisiblePosition() == list.getCount() + LOG_SCROLL_NULL)) {
            scrolledToBottom = true;
        } else {
            scrolledToBottom = false;
        }
        this.mLogAdapter.swapCursor(data);
        if (position > LOG_SCROLL_NULL) {
            list.setSelectionFromTop(position, 0);
        } else if (scrolledToBottom) {
            list.setSelection(list.getCount() + LOG_SCROLL_NULL);
        }
        this.mLogScrollPosition = LOG_SCROLL_NULL;
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        this.mLogAdapter.swapCursor(null);
    }

    private void onSendClicked() {
        this.mUARTInterface.send(this.mField.getText().toString());
        this.mField.setText(null);
        this.mField.requestFocus();
    }

    public void onServiceStarted() {
        getActivity().bindService(new Intent(getActivity(), UARTService.class), this.mServiceConnection, 0);
    }

    public void onFragmentHidden() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mField.getWindowToken(), 0);
    }

    protected void onDeviceConnected() {
        this.mField.setEnabled(true);
        this.mSendButton.setEnabled(true);
    }

    protected void onDeviceDisconnected() {
        this.mField.setEnabled(false);
        this.mSendButton.setEnabled(false);
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
        return intentFilter;
    }
}
