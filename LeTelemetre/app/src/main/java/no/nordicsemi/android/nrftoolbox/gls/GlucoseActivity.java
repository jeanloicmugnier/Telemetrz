package no.nordicsemi.android.nrftoolbox.gls;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileExpandableListActivity;

public class GlucoseActivity extends BleProfileExpandableListActivity implements OnMenuItemClickListener, GlucoseManagerCallbacks {
    private static final String TAG = "GlucoseActivity";
    private BaseExpandableListAdapter mAdapter;
    private View mControlPanelAbort;
    private View mControlPanelStd;
    private GlucoseManager mGlucoseManager;
    private TextView mUnitView;

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseActivity.1 */
    class C00881 implements OnClickListener {
        C00881() {
        }

        public void onClick(View v) {
            GlucoseActivity.this.mGlucoseManager.getLastRecord();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseActivity.2 */
    class C00892 implements OnClickListener {
        C00892() {
        }

        public void onClick(View v) {
            GlucoseActivity.this.mGlucoseManager.getAllRecords();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseActivity.3 */
    class C00903 implements OnClickListener {
        C00903() {
        }

        public void onClick(View v) {
            GlucoseActivity.this.mGlucoseManager.abort();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseActivity.4 */
    class C00914 implements OnClickListener {
        C00914() {
        }

        public void onClick(View v) {
            PopupMenu menu = new PopupMenu(GlucoseActivity.this, v);
            menu.setOnMenuItemClickListener(GlucoseActivity.this);
            menu.getMenuInflater().inflate(Integer.valueOf(C0063R.menu.gls_more), menu.getMenu());
            menu.show();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseActivity.5 */
    class C00925 implements Runnable {
        final /* synthetic */ boolean val$progress;

        C00925(boolean z) {
            this.val$progress = z;
        }

        public void run() {
            int i;
            int i2 = View.VISIBLE;
            GlucoseActivity.this.setProgressBarIndeterminateVisibility(this.val$progress);
            View access$100 = GlucoseActivity.this.mControlPanelStd;
            if (this.val$progress) {
                i = View.GONE;
            } else {
                i = View.VISIBLE;
            }
            access$100.setVisibility(i);
            View access$200 = GlucoseActivity.this.mControlPanelAbort;
            if (!this.val$progress) {
                i2 = View.GONE;
            }
            access$200.setVisibility(i2);
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseActivity.6 */
    class C00936 implements Runnable {
        C00936() {
        }

        public void run() {
            SparseArray<GlucoseRecord> records = GlucoseActivity.this.mGlucoseManager.getRecords();
            if (records.size() > 0) {
                int unit = ((GlucoseRecord) records.valueAt(0)).unit;
                GlucoseActivity.this.mUnitView.setVisibility(View.VISIBLE);
                int text;
                if(unit==0){
                    text = C0063R.string.gls_unit_mgpdl;
                }
                else{
                    text = C0063R.string.gls_unit_mmolpl;
                }
                GlucoseActivity.this.mUnitView.setText(text);
            } else {
                GlucoseActivity.this.mUnitView.setVisibility(View.GONE);
            }
            GlucoseActivity.this.mAdapter.notifyDataSetChanged();
        }
    }

    protected void onCreateView(Bundle savedInstanceState) {
        requestWindowFeature(5);
        setContentView(Integer.valueOf(C0063R.layout.activity_feature_gls));
        setGUI();
    }

    private void setGUI() {
        this.mUnitView = (TextView) findViewById(Integer.valueOf(C0063R.id.unit));
        this.mControlPanelStd = findViewById(Integer.valueOf(C0063R.id.gls_control_std));
        this.mControlPanelAbort = findViewById(Integer.valueOf(C0063R.id.gls_control_abort));
        findViewById(Integer.valueOf(C0063R.id.action_last)).setOnClickListener(new C00881());
        findViewById(Integer.valueOf(C0063R.id.action_all)).setOnClickListener(new C00892());
        findViewById(Integer.valueOf(C0063R.id.action_abort)).setOnClickListener(new C00903());
        findViewById(Integer.valueOf(C0063R.id.action_more)).setOnClickListener(new C00914());
        ExpandableListAdapter expandableRecordAdapter = new ExpandableRecordAdapter(this, this.mGlucoseManager);
        this.mAdapter =  (BaseExpandableListAdapter) expandableRecordAdapter;
        setListAdapter(expandableRecordAdapter);
    }

    protected BleManager<GlucoseManagerCallbacks> initializeManager() {
        GlucoseManager manager = GlucoseManager.getGlucoseManager();
        this.mGlucoseManager = manager;
        manager.setGattCallbacks((GlucoseManagerCallbacks) this);
        return manager;
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case C0063R.id.action_refresh:
                this.mGlucoseManager.refreshRecords();
                break;
            case C0063R.id.action_first:
                this.mGlucoseManager.getFirstRecord();
                break;
            case C0063R.id.action_clear:
                this.mGlucoseManager.clear();
                break;
            case C0063R.id.action_delete_all:
                this.mGlucoseManager.deleteAllRecords();
                break;
        }
        return true;
    }

    protected int getAboutTextId() {
        return C0063R.string.gls_about_text;
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.gls_default_name;
    }

    protected UUID getFilterUUID() {
        return GlucoseManager.GLS_SERVICE_UUID;
    }

    protected void setDefaultUI() {
        this.mGlucoseManager.clear();
    }

    private void setOperationInProgress(boolean progress) {
        runOnUiThread(new C00925(progress));
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    public void onGlucoseMeasurementNotificationEnabled() {
    }

    public void onGlucoseMeasurementContextNotificationEnabled() {
    }

    public void onRecordAccessControlPointIndicationsEnabled() {
    }

    public void onOperationStarted() {
        setOperationInProgress(true);
    }

    public void onOperationCompleted() {
        setOperationInProgress(false);
    }

    public void onOperationAborted() {
        setOperationInProgress(false);
    }

    public void onOperationNotSupported() {
        setOperationInProgress(false);
        showToast((int) C0063R.string.gls_operation_not_supported);
    }

    public void onOperationFailed() {
        setOperationInProgress(false);
        showToast((int) C0063R.string.gls_operation_failed);
    }

    public void onDatasetChanged() {
        runOnUiThread(new C00936());
    }

    public void onNumberOfRecordsRequested(int value) {
        showToast(getString(Integer.valueOf(C0063R.string.gls_progress), new Object[]{Integer.valueOf(value)}));
    }
}
