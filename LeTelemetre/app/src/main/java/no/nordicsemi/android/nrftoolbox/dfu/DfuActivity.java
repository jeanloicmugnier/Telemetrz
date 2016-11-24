package no.nordicsemi.android.nrftoolbox.dfu;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresPermission;
import android.support.annotation.UiThread;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.error.GattError;
import no.nordicsemi.android.log.LogContract.Session.Content;
import no.nordicsemi.android.nrftoolbox.AppHelpFragment;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.dfu.adapter.FileBrowserAppsAdapter;
import no.nordicsemi.android.nrftoolbox.dfu.fragment.UploadCancelFragment;
import no.nordicsemi.android.nrftoolbox.dfu.fragment.UploadCancelFragment.CancelFragmentListener;
import no.nordicsemi.android.nrftoolbox.dfu.fragment.ZipInfoFragment;
import no.nordicsemi.android.nrftoolbox.dfu.settings.SettingsActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.rsc.RSCManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment.OnDeviceSelectedListener;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import org.achartengine.tools.Zoom;

public class DfuActivity extends Activity implements LoaderCallbacks<Cursor>, OnDeviceSelectedListener, CancelFragmentListener {
    private static final String DATA_DEVICE = "device";
    private static final String DATA_FILE_PATH = "file_path";
    private static final String DATA_FILE_STREAM = "file_stream";
    private static final String DATA_FILE_TYPE = "file_type";
    private static final String DATA_FILE_TYPE_TMP = "file_type_tmp";
    private static final String DATA_STATUS = "status";
    private static final String EXTRA_URI = "uri";
    private static final String PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME";
    private static final String PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME";
    private static final String PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE";
    private static final String PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE";
    static final int REQUEST_ENABLE_BT = 2;
    private static final int SELECT_FILE_REQ = 1;
    private static final String TAG = "DfuActivity";
    private Button mConnectButton;
    private TextView mDeviceNameView;
    private final BroadcastReceiver mDfuUpdateReceiver;
    private TextView mFileNameView;
    private String mFilePath;
    private TextView mFileSizeView;
    private TextView mFileStatusView;
    private Uri mFileStreamUri;
    private int mFileType;
    private int mFileTypeTmp;
    private TextView mFileTypeView;
    private ProgressBar mProgressBar;
    private Button mSelectFileButton;
    private BluetoothDevice mSelectedDevice;
    private boolean mStatusOk;
    private TextView mTextPercentage;
    private TextView mTextUploading;
    private Button mUploadButton;

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.1 */
    class C00771 extends BroadcastReceiver {

        /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.1.1 */
        class C00761 implements Runnable {
            C00761() {
            }

            public void run() {
                ((NotificationManager) DfuActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
            }
        }

        C00771() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DfuBaseService.BROADCAST_PROGRESS.equals(action)) {
                DfuActivity.this.updateProgressBar(intent.getIntExtra(DfuBaseService.EXTRA_DATA, 0), intent.getIntExtra(DfuBaseService.EXTRA_PART_CURRENT, DfuActivity.SELECT_FILE_REQ), intent.getIntExtra(DfuBaseService.EXTRA_PARTS_TOTAL, DfuActivity.SELECT_FILE_REQ), false);
            } else if (DfuBaseService.BROADCAST_ERROR.equals(action)) {
                DfuActivity.this.updateProgressBar(intent.getIntExtra(DfuBaseService.EXTRA_DATA, 0), 0, 0, true);
                new Handler().postDelayed(new C00761(), 200);
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.2 */
    class C00782 implements OnClickListener {
        C00782() {
        }

        public void onClick(DialogInterface dialog, int which) {
            new ZipInfoFragment().show(DfuActivity.this.getFragmentManager(), "help_fragment");
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.3 */
    class C00793 implements OnClickListener {
        C00793() {
        }

        public void onClick(DialogInterface dialog, int which) {
            DfuActivity.this.openFileChooser();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.4 */
    class C00804 implements OnClickListener {
        C00804() {
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Zoom.ZOOM_AXIS_XY /*0*/:
                    DfuActivity.this.mFileTypeTmp = DfuActivity.SELECT_FILE_REQ;
                case DfuActivity.SELECT_FILE_REQ /*1*/:
                    DfuActivity.this.mFileTypeTmp = DfuActivity.REQUEST_ENABLE_BT;
                case DfuActivity.REQUEST_ENABLE_BT /*2*/:
                    DfuActivity.this.mFileTypeTmp = 4;
                case BleProfileService.STATE_DISCONNECTING /*3*/:
                    DfuActivity.this.mFileTypeTmp = 0;
                default:
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.5 */
    class C00815 implements OnClickListener {
        final /* synthetic */ ListView val$appsList;

        C00815(ListView listView) {
            this.val$appsList = listView;
        }

        public void onClick(DialogInterface dialog, int which) {
            int pos = this.val$appsList.getCheckedItemPosition();
            if (pos >= 0) {
                DfuActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(DfuActivity.this.getResources().getStringArray(Integer.valueOf(C0063R.array.dfu_app_file_browser_action))[pos])));
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.6 */
    class C00826 implements OnClickListener {
        C00826() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.7 */
    class C00837 implements Runnable {
        C00837() {
        }

        public void run() {
            DfuActivity.this.onTransferCompleted();
            ((NotificationManager) DfuActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.DfuActivity.8 */
    class C00848 implements Runnable {
        C00848() {
        }

        public void run() {
            DfuActivity.this.onUploadCanceled();
            ((NotificationManager) DfuActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
        }
    }

    public DfuActivity() {
        this.mDfuUpdateReceiver = new C00771();
    }

    @UiThread
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    protected void onCreate(Bundle savedInstanceState) {
        boolean z = true;
        super.onCreate(savedInstanceState);
        setContentView(Integer.valueOf(C0063R.layout.activity_feature_dfu));
        isBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        setGUI();
        ensureSamplesExist();
        this.mFileType = 4;
        if (savedInstanceState != null) {
            boolean z2;
            this.mFileType = savedInstanceState.getInt(DATA_FILE_TYPE);
            this.mFileTypeTmp = savedInstanceState.getInt(DATA_FILE_TYPE_TMP);
            this.mFilePath = savedInstanceState.getString(DATA_FILE_PATH);
            this.mFileStreamUri = (Uri) savedInstanceState.getParcelable(DATA_FILE_STREAM);
            this.mSelectedDevice = (BluetoothDevice) savedInstanceState.getParcelable(DATA_DEVICE);
            if (this.mStatusOk || savedInstanceState.getBoolean(DATA_STATUS)) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mStatusOk = z2;
            Button button = this.mUploadButton;
            if (this.mSelectedDevice == null || !this.mStatusOk) {
                z = false;
            }
            button.setEnabled(z);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DATA_FILE_TYPE, this.mFileType);
        outState.putInt(DATA_FILE_TYPE_TMP, this.mFileTypeTmp);
        outState.putString(DATA_FILE_PATH, this.mFilePath);
        outState.putParcelable(DATA_FILE_STREAM, this.mFileStreamUri);
        outState.putParcelable(DATA_DEVICE, this.mSelectedDevice);
        outState.putBoolean(DATA_STATUS, this.mStatusOk);
    }

    private void setGUI() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.mDeviceNameView = (TextView) findViewById(Integer.valueOf(C0063R.id.device_name));
        this.mFileNameView = (TextView) findViewById(Integer.valueOf(C0063R.id.file_name));
        this.mFileTypeView = (TextView) findViewById(Integer.valueOf(C0063R.id.file_type));
        this.mFileSizeView = (TextView) findViewById(Integer.valueOf(C0063R.id.file_size));
        this.mFileStatusView = (TextView) findViewById(Integer.valueOf(C0063R.id.file_status));
        this.mSelectFileButton = (Button) findViewById(Integer.valueOf(C0063R.id.action_select_file));
        this.mUploadButton = (Button) findViewById(Integer.valueOf(C0063R.id.action_upload));
        this.mConnectButton = (Button) findViewById(Integer.valueOf(C0063R.id.action_connect));
        this.mTextPercentage = (TextView) findViewById(Integer.valueOf(C0063R.id.textviewProgress));
        this.mTextUploading = (TextView) findViewById(Integer.valueOf(C0063R.id.textviewUploading));
        this.mProgressBar = (ProgressBar) findViewById(Integer.valueOf(C0063R.id.progressbar_file));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(DfuBaseService.DFU_IN_PROGRESS, false)) {
            this.mDeviceNameView.setText(preferences.getString(PREFS_DEVICE_NAME, ""));
            this.mFileNameView.setText(preferences.getString(PREFS_FILE_NAME, ""));
            this.mFileTypeView.setText(preferences.getString(PREFS_FILE_TYPE, ""));
            this.mFileSizeView.setText(preferences.getString(PREFS_FILE_SIZE, ""));
            this.mFileStatusView.setText(Integer.valueOf(C0063R.string.dfu_file_status_ok));
            this.mStatusOk = true;
            showProgressBar();
        }
    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mDfuUpdateReceiver, makeDfuUpdateIntentFilter());
    }

    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDfuUpdateReceiver);
    }

    private static IntentFilter makeDfuUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DfuBaseService.BROADCAST_PROGRESS);
        intentFilter.addAction(DfuBaseService.BROADCAST_ERROR);
        intentFilter.addAction(DfuBaseService.BROADCAST_LOG);
        return intentFilter;
    }

    private void isBLESupported() {
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            showToast((int) C0063R.string.no_ble);
            finish();
        }
    }

    @UiThread
    @RequiresPermission(BLUETOOTH_SERVICE)
    private boolean isBLEEnabled() {
        BluetoothAdapter adapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
    }

    private void showDeviceScanningDialog() {
        ScannerFragment.getInstance(this, null, true).show(getFragmentManager(), "scan_fragment");
    }

    private void ensureSamplesExist() {
        File folder = new File(Environment.getExternalStorageDirectory(), "Nordic Semiconductor");
        if (!folder.exists()) {
            folder.mkdir();
        }
        boolean oldCopied = false;
        boolean newCopied = false;
        File f = new File(folder, "ble_app_hrs_s110_v6_0_0.hex");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.ble_app_hrs_s110_v6_0_0, f);
            oldCopied = true;
        }
        f = new File(folder, "ble_app_rscs_s110_v6_0_0.hex");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.ble_app_rscs_s110_v6_0_0, f);
            oldCopied = true;
        }
        f = new File(folder, "ble_app_hrs_s110_v7_0_0.hex");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.ble_app_hrs_s110_v7_0_0, f);
            newCopied = true;
        }
        f = new File(folder, "ble_app_rscs_s110_v7_0_0.hex");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.ble_app_rscs_s110_v7_0_0, f);
            newCopied = true;
        }
        f = new File(folder, "blinky_arm_s110_v7_0_0.hex");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.blinky_arm_s110_v7_0_0, f);
            newCopied = true;
        }
        if (oldCopied) {
            Toast.makeText(this, Integer.valueOf(C0063R.string.dfu_example_files_created), Toast.LENGTH_LONG).show();
        } else if (newCopied) {
            Toast.makeText(this, Integer.valueOf(C0063R.string.dfu_example_new_files_created), Toast.LENGTH_LONG).show();
        }
        newCopied = false;
        f = new File(folder, "dfu_2_0.bat");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.dfu_win_2_0, f);
            newCopied = true;
        }
        f = new File(folder, "dfu_2_0.sh");
        if (!f.exists()) {
            copyRawResource(C0063R.raw.dfu_mac_2_0, f);
            newCopied = true;
        }
        f = new File(folder, "README.txt");
        if (newCopied) {
            copyRawResource(C0063R.raw.readme, f);
        }
        if (newCopied) {
            Toast.makeText(this, Integer.valueOf(C0063R.string.dfu_scripts_created), Toast.LENGTH_LONG).show();
        }
    }

    private void copyRawResource(int rawResId, File dest) {
        InputStream is;
        FileOutputStream fos;
        try {
            is = getResources().openRawResource(rawResId);
            fos = new FileOutputStream(dest);
            byte[] buf = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
            while (true) {
                int read = is.read(buf);
                if (read > 0) {
                    fos.write(buf, 0, read);
                } else {
                    is.close();
                    fos.close();
                    return;
                }
            }
        } catch (IOException e) {
            DebugLogger.m19e(TAG, "Error while copying HEX file " + e.toString());
        } catch (Throwable th) {
            DebugLogger.m19e(TAG, "throwing " + th.toString());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(Integer.valueOf(C0063R.menu.dfu_menu), menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
            case C0063R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case C0063R.id.action_about:
                AppHelpFragment.getInstance(C0063R.string.dfu_about_text).show(getFragmentManager(), "help_fragment");
                break;
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case SELECT_FILE_REQ /*1*/:
                    this.mFileType = this.mFileTypeTmp;
                    this.mFilePath = null;
                    this.mFileStreamUri = null;
                    Uri uri = data.getData();
                    if (uri.getScheme().equals("file")) {
                        String path = uri.getPath();
                        File file = new File(path);
                        this.mFilePath = path;
                        updateFileInfo(file.getName(), file.length(), this.mFileType);
                    } else if (uri.getScheme().equals(Content.CONTENT)) {
                        this.mFileStreamUri = uri;
                        Bundle extras = data.getExtras();
                        if (extras != null && extras.containsKey("android.intent.extra.STREAM")) {
                            this.mFileStreamUri = (Uri) extras.getParcelable("android.intent.extra.STREAM");
                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(EXTRA_URI, uri);
                        getLoaderManager().restartLoader(0, bundle, this);
                    }
                default:
            }
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, (Uri) args.getParcelable(EXTRA_URI), null, null, null, null);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        this.mFileNameView.setText(null);
        this.mFileTypeView.setText(null);
        this.mFileSizeView.setText(null);
        this.mFilePath = null;
        this.mFileStreamUri = null;
        this.mStatusOk = false;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToNext()) {
            this.mFileNameView.setText(null);
            this.mFileTypeView.setText(null);
            this.mFileSizeView.setText(null);
            this.mFilePath = null;
            this.mFileStreamUri = null;
            this.mFileStatusView.setText(Integer.valueOf(C0063R.string.dfu_file_status_error));
            this.mStatusOk = false;
            return;
        }
        String fileName = data.getString(data.getColumnIndex("_display_name"));
        int fileSize = data.getInt(data.getColumnIndex("_size"));
        String filePath = null;
        int dataIndex = data.getColumnIndex("_data");
        if (dataIndex != -1) {
            filePath = data.getString(dataIndex);
        }
        if (!TextUtils.isEmpty(filePath)) {
            this.mFilePath = filePath;
        }
        updateFileInfo(fileName, (long) fileSize, this.mFileType);
    }

    private void updateFileInfo(String fileName, long fileSize, int fileType) {
        boolean z;
        this.mFileNameView.setText(fileName);
        switch (fileType) {
            case Zoom.ZOOM_AXIS_XY /*0*/:
                this.mFileTypeView.setText(getResources().getStringArray(Integer.valueOf(C0063R.array.dfu_file_type))[3]);
                break;
            case SELECT_FILE_REQ /*1*/:
                this.mFileTypeView.setText(getResources().getStringArray(Integer.valueOf(C0063R.array.dfu_file_type))[0]);
                break;
            case REQUEST_ENABLE_BT /*2*/:
                this.mFileTypeView.setText(getResources().getStringArray(Integer.valueOf(C0063R.array.dfu_file_type))[SELECT_FILE_REQ]);
                break;
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                this.mFileTypeView.setText(getResources().getStringArray(Integer.valueOf(C0063R.array.dfu_file_type))[REQUEST_ENABLE_BT]);
                break;
        }
        TextView textView = this.mFileSizeView;
        Object[] objArr = new Object[SELECT_FILE_REQ];
        objArr[0] = Long.valueOf(fileSize);
        textView.setText(getString(Integer.valueOf(C0063R.string.dfu_file_size_text), objArr));
        boolean isHexFile = MimeTypeMap.getFileExtensionFromUrl(fileName).equalsIgnoreCase(this.mFileType == 0 ? "ZIP" : "HEX");
        this.mStatusOk = isHexFile;
        int text = isHexFile ? Integer.valueOf(C0063R.string.dfu_file_status_ok) : Integer.valueOf(C0063R.string.dfu_file_status_invalid);
        this.mFileStatusView.setText(String.valueOf(text));
        Button button = this.mUploadButton;
        if (this.mSelectedDevice == null || !isHexFile) {
            z = false;
        } else {
            z = true;
        }
        button.setEnabled(z);
    }

    public void onSelectFileHelpClicked(View view) {
        new Builder(this).setTitle(Integer.valueOf(C0063R.string.dfu_help_title)).setMessage(Integer.valueOf(C0063R.string.dfu_help_message)).setPositiveButton(Integer.valueOf(17039370), null).show();
    }

    public void onSelectFileClicked(View view) {
        this.mFileTypeTmp = this.mFileType;
        int index = REQUEST_ENABLE_BT;
        switch (this.mFileType) {
            case Zoom.ZOOM_AXIS_XY /*0*/:
                index = 3;
                break;
            case SELECT_FILE_REQ /*1*/:
                index = 0;
                break;
            case REQUEST_ENABLE_BT /*2*/:
                index = SELECT_FILE_REQ;
                break;
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                index = REQUEST_ENABLE_BT;
                break;
        }
        new Builder(this).setTitle(Integer.valueOf(C0063R.string.dfu_file_type_title)).setSingleChoiceItems(Integer.valueOf(C0063R.array.dfu_file_type), index, new C00804()).setPositiveButton(Integer.valueOf(17039370), new C00793()).setNeutralButton(Integer.valueOf(C0063R.string.dfu_file_info), new C00782()).setNegativeButton(Integer.valueOf(17039360), null).show();
    }

    private void openFileChooser() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType(this.mFileTypeTmp == 0 ? DfuBaseService.MIME_TYPE_ZIP : DfuBaseService.MIME_TYPE_HEX);
        intent.addCategory("android.intent.category.OPENABLE");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, SELECT_FILE_REQ);
            return;
        }
        View customView = getLayoutInflater().inflate(Integer.valueOf(C0063R.layout.app_file_browser), null);
        ListView appsList = (ListView) customView.findViewById(Integer.valueOf(16908298));
        appsList.setAdapter(new FileBrowserAppsAdapter(this));
        appsList.setChoiceMode(SELECT_FILE_REQ);
        appsList.setItemChecked(0, true);
        new Builder(this).setTitle(Integer.valueOf(C0063R.string.dfu_alert_no_filebrowser_title)).setView(customView).setNegativeButton(Integer.valueOf(17039369), new C00826()).setPositiveButton(Integer.valueOf(17039370), new C00815(appsList)).show();
    }

    @UiThread
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void onUploadClicked(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(DfuBaseService.DFU_IN_PROGRESS, false)) {
            showUploadCancelDialog();
        } else if (this.mStatusOk) {
            Editor editor = preferences.edit();
            editor.putString(PREFS_DEVICE_NAME, this.mSelectedDevice.getName());
            editor.putString(PREFS_FILE_NAME, this.mFileNameView.getText().toString());
            editor.putString(PREFS_FILE_TYPE, this.mFileTypeView.getText().toString());
            editor.putString(PREFS_FILE_SIZE, this.mFileSizeView.getText().toString());
            editor.commit();
            showProgressBar();
            Intent service = new Intent(this, DfuService.class);
            service.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, this.mSelectedDevice.getAddress());
            service.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, this.mSelectedDevice.getName());
            service.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, this.mFileType == 0 ? DfuBaseService.MIME_TYPE_ZIP : DfuBaseService.MIME_TYPE_HEX);
            service.putExtra(DfuBaseService.EXTRA_FILE_TYPE, this.mFileType);
            service.putExtra(DfuBaseService.EXTRA_FILE_PATH, this.mFilePath);
            service.putExtra(DfuBaseService.EXTRA_FILE_URI, this.mFileStreamUri);
            startService(service);
        } else {
            Toast.makeText(this, Integer.valueOf(C0063R.string.dfu_file_status_invalid_message), Toast.LENGTH_LONG).show();
        }
    }

    private void showUploadCancelDialog() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        Intent pauseAction = new Intent(DfuBaseService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, 0);
        manager.sendBroadcast(pauseAction);
        UploadCancelFragment.getInstance().show(getFragmentManager(), TAG);
    }

    @UiThread
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void onConnectClicked(View view) {
        if (isBLEEnabled()) {
            showDeviceScanningDialog();
        } else {
            showBLEDialog();
        }
    }

    public void onDeviceSelected(BluetoothDevice device, String name) {
        this.mSelectedDevice = device;
        this.mUploadButton.setEnabled(this.mStatusOk);
        this.mDeviceNameView.setText(name);
    }

    public void onDialogCanceled() {
    }

    private void updateProgressBar(int progress, int part, int total, boolean error) {
        switch (progress) {
            case DfuBaseService.PROGRESS_ABORTED /*-7*/:
                this.mTextPercentage.setText(Integer.valueOf(C0063R.string.dfu_status_aborted));
                new Handler().postDelayed(new C00848(), 200);
            case DfuBaseService.PROGRESS_COMPLETED /*-6*/:
                this.mTextPercentage.setText(Integer.valueOf(C0063R.string.dfu_status_completed));
                new Handler().postDelayed(new C00837(), 200);
            case DfuBaseService.PROGRESS_DISCONNECTING /*-5*/:
                this.mProgressBar.setIndeterminate(true);
                this.mTextPercentage.setText(Integer.valueOf(C0063R.string.dfu_status_disconnecting));
            case DfuBaseService.PROGRESS_VALIDATING /*-4*/:
                this.mProgressBar.setIndeterminate(true);
                this.mTextPercentage.setText(Integer.valueOf(C0063R.string.dfu_status_validating));
            case DfuBaseService.PROGRESS_STARTING /*-2*/:
                this.mProgressBar.setIndeterminate(true);
                this.mTextPercentage.setText(Integer.valueOf(C0063R.string.dfu_status_starting));
            case RSCManagerCallbacks.NOT_AVAILABLE /*-1*/:
                this.mProgressBar.setIndeterminate(true);
                this.mTextPercentage.setText(Integer.valueOf(C0063R.string.dfu_status_connecting));
            default:
                this.mProgressBar.setIndeterminate(false);
                if (error) {
                    showErrorMessage(progress);
                    return;
                }
                this.mProgressBar.setProgress(progress);
                TextView textView = this.mTextPercentage;
                Object[] objArr = new Object[SELECT_FILE_REQ];
                objArr[0] = Integer.valueOf(progress);
                textView.setText(getString(Integer.valueOf(C0063R.string.progress), objArr));
                if (total > SELECT_FILE_REQ) {
                    textView = this.mTextUploading;
                    objArr = new Object[REQUEST_ENABLE_BT];
                    objArr[0] = Integer.valueOf(part);
                    objArr[SELECT_FILE_REQ] = Integer.valueOf(total);
                    textView.setText(getString(Integer.valueOf(C0063R.string.dfu_status_uploading_part), objArr));
                    return;
                }
                this.mTextUploading.setText(Integer.valueOf(C0063R.string.dfu_status_uploading));
        }
    }

    private void showProgressBar() {
        this.mProgressBar.setVisibility(View.VISIBLE);
        this.mTextPercentage.setVisibility(View.VISIBLE);
        this.mTextPercentage.setText(null);
        this.mTextUploading.setText(Integer.valueOf(C0063R.string.dfu_status_uploading));
        this.mTextUploading.setVisibility(View.VISIBLE);
        this.mConnectButton.setEnabled(false);
        this.mSelectFileButton.setEnabled(false);
        this.mUploadButton.setEnabled(true);
        this.mUploadButton.setText(Integer.valueOf(C0063R.string.dfu_action_upload_cancel));
    }

    private void onTransferCompleted() {
        clearUI();
        showToast((int) C0063R.string.dfu_success);
    }

    public void onUploadCanceled() {
        clearUI();
        showToast((int) C0063R.string.dfu_aborted);
    }

    public void onCancelUpload() {
        this.mProgressBar.setIndeterminate(true);
        this.mTextUploading.setText(Integer.valueOf(C0063R.string.dfu_status_aborting));
        this.mTextPercentage.setText(null);
    }

    private void showErrorMessage(int code) {
        clearUI();
        showToast("Upload failed: " + GattError.parse(code) + " (" + (code & -12289) + ")");
    }

    private void clearUI() {
        this.mProgressBar.setVisibility(View.INVISIBLE);
        this.mTextPercentage.setVisibility(View.INVISIBLE);
        this.mTextUploading.setVisibility(View.INVISIBLE);
        this.mConnectButton.setEnabled(true);
        this.mSelectFileButton.setEnabled(true);
        this.mUploadButton.setEnabled(false);
        this.mSelectedDevice = null;
        this.mDeviceNameView.setText(Integer.valueOf(C0063R.string.dfu_default_name));
        this.mUploadButton.setText(Integer.valueOf(C0063R.string.dfu_action_upload));
    }

    private void showToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
