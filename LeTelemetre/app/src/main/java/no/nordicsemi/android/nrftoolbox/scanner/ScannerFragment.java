package no.nordicsemi.android.nrftoolbox.scanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresPermission;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Set;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class ScannerFragment extends DialogFragment {
    private static final boolean DEVICE_IS_BONDED = true;
    private static final boolean DEVICE_NOT_BONDED = false;
    private static final String DISCOVERABLE_REQUIRED = "discoverable_required";
    static final int NO_RSSI = -1000;
    private static final String PARAM_UUID = "param_uuid";
    private static final long SCAN_DURATION = 5000;
    private static final String TAG = "ScannerFragment";
    private DeviceListAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mDiscoverableRequired;
    private Handler mHandler;
    private boolean mIsScanning;
    private LeScanCallback mLEScanCallback;
    private OnDeviceSelectedListener mListener;
    private Button mScanButton;
    private UUID mUuid;

    /* renamed from: no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.1 */
    class C01391 implements OnItemClickListener {
        final /* synthetic */ AlertDialog val$dialog;

        C01391(AlertDialog alertDialog) {
            this.val$dialog = alertDialog;
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ScannerFragment.this.stopScan();
            this.val$dialog.dismiss();
            ExtendedBluetoothDevice d = (ExtendedBluetoothDevice) ScannerFragment.this.mAdapter.getItem(position);
            ScannerFragment.this.mListener.onDeviceSelected(d.device, d.name);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.2 */
    class C01402 implements OnClickListener {
        final /* synthetic */ AlertDialog val$dialog;

        C01402(AlertDialog alertDialog) {
            this.val$dialog = alertDialog;
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
        public void onClick(View v) {
            int i =  C0063R.id.action_cancel;
            if (v.getId() != i) {
                return;
            }
            if (ScannerFragment.this.mIsScanning) {
                this.val$dialog.cancel();
            } else {
                ScannerFragment.this.startScan();
            }
        }
    }
    /* renamed from: no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.3 */
    class C01413 implements Runnable {
        C01413() {
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
        public void run() {
            if (ScannerFragment.this.mIsScanning) {
                ScannerFragment.this.stopScan();
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.4 */
    class C01424 implements Runnable {
        final /* synthetic */ BluetoothDevice val$device;
        final /* synthetic */ boolean val$isBonded;
        final /* synthetic */ String val$name;
        final /* synthetic */ int val$rssi;

        C01424(BluetoothDevice bluetoothDevice, String str, int i, boolean z) {
            this.val$device = bluetoothDevice;
            this.val$name = str;
            this.val$rssi = i;
            this.val$isBonded = z;
        }

        public void run() {
            ScannerFragment.this.mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(this.val$device, this.val$name, this.val$rssi, this.val$isBonded));
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.5 */
    class C01435 implements Runnable {
        final /* synthetic */ BluetoothDevice val$device;
        final /* synthetic */ int val$rssi;

        C01435(BluetoothDevice bluetoothDevice, int i) {
            this.val$device = bluetoothDevice;
            this.val$rssi = i;
        }

        public void run() {
            ScannerFragment.this.mAdapter.updateRssiOfBondedDevice(this.val$device.getAddress(), this.val$rssi);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.6 */
    class C01446 implements LeScanCallback {
        C01446() {
        }

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null) {
                ScannerFragment.this.updateScannedDevice(device, rssi);
                try {
                    if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, ScannerFragment.this.mUuid, ScannerFragment.this.mDiscoverableRequired)) {
                        ScannerFragment.this.addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, ScannerFragment.DEVICE_NOT_BONDED);
                    }
                } catch (Exception e) {
                    DebugLogger.m19e(ScannerFragment.TAG, "Invalid data in Advertisement packet " + e.toString());
                }
            }
        }
    }

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(BluetoothDevice bluetoothDevice, String str);

        void onDialogCanceled();
    }

    public ScannerFragment() {
        this.mHandler = new Handler();
        this.mIsScanning = DEVICE_NOT_BONDED;
        this.mLEScanCallback = new C01446();
    }

    public static ScannerFragment getInstance(Context context, UUID uuid, boolean discoverableRequired) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle args = new Bundle();
        args.putParcelable(PARAM_UUID, new ParcelUuid(uuid));
        args.putBoolean(DISCOVERABLE_REQUIRED, discoverableRequired);
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnDeviceSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeviceSelectedListener");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey(PARAM_UUID)) {
            this.mUuid = ((ParcelUuid) args.getParcelable(PARAM_UUID)).getUuid();
        }
        this.mDiscoverableRequired = args.getBoolean(DISCOVERABLE_REQUIRED);
        this.mBluetoothAdapter = ((BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void onDestroyView() {
        stopScan();
        super.onDestroyView();
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        int selection = C0063R.layout.fragment_device_selection;
        View dialogView = LayoutInflater.from(getActivity()).inflate(selection, null);
        int id = 16908298;
        int id2 = 16908292;
        ListView listview = (ListView) dialogView.findViewById(Integer.valueOf(id));
        listview.setEmptyView(dialogView.findViewById(Integer.valueOf(id2)));
        ListAdapter deviceListAdapter = new DeviceListAdapter(getActivity());
        this.mAdapter = (DeviceListAdapter) deviceListAdapter;
        listview.setAdapter(deviceListAdapter);
        builder.setTitle(String.valueOf(C0063R.string.scanner_title));
        AlertDialog dialog = builder.setView(dialogView).create();
        listview.setOnItemClickListener(new C01391(dialog));
        int cancel = C0063R.id.action_cancel;
        this.mScanButton = (Button) dialogView.findViewById(cancel);
        this.mScanButton.setOnClickListener(new C01402(dialog));
        addBondedDevices();
        if (savedInstanceState == null) {
            startScan();
        }
        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.mListener.onDialogCanceled();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    private void startScan() {
        this.mAdapter.clearDevices();
        this.mScanButton.setText(String.valueOf(C0063R.string.scanner_action_cancel));
        this.mBluetoothAdapter.startLeScan(this.mLEScanCallback);
        this.mIsScanning = DEVICE_IS_BONDED;
        this.mHandler.postDelayed(new C01413(), SCAN_DURATION);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    private void stopScan() {
        if (this.mIsScanning) {
            this.mScanButton.setText(String.valueOf(C0063R.string.scanner_action_scan));
            this.mBluetoothAdapter.stopLeScan(this.mLEScanCallback);
            this.mIsScanning = DEVICE_NOT_BONDED;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    private void addBondedDevices() {
        for (BluetoothDevice device : this.mBluetoothAdapter.getBondedDevices()) {
            this.mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, device.getName(), NO_RSSI, DEVICE_IS_BONDED));
        }
    }

    private void addScannedDevice(BluetoothDevice device, String name, int rssi, boolean isBonded) {
        getActivity().runOnUiThread(new C01424(device, name, rssi, isBonded));
    }

    private void updateScannedDevice(BluetoothDevice device, int rssi) {
        getActivity().runOnUiThread(new C01435(device, rssi));
    }
}
