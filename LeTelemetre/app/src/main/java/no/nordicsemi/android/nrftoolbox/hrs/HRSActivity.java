package no.nordicsemi.android.nrftoolbox.hrs;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.internal.view.SupportMenu;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;
import org.achartengine.GraphicalView;

public class HRSActivity extends BleProfileActivity implements HRSManagerCallbacks {
    private static final String GRAPH_COUNTER = "graph_counter";
    private static final String GRAPH_STATUS = "graph_status";
    private static final String HR_VALUE = "hr_value";
    private final int MAX_HR_VALUE;
    private final int MIN_POSITIVE_VALUE;
    private final String TAG;
    private boolean isGraphInProgress;
    private int mCounter;
    private GraphicalView mGraphView;
    private TextView mHRSPosition;
    private TextView mHRSValue;
    private Handler mHandler;
    private int mHrmValue;
    private int mInterval;
    private LineGraphView mLineGraph;
    private Runnable mRepeatTask;

    /* renamed from: no.nordicsemi.android.nrftoolbox.hrs.HRSActivity.1 */
    class C00971 implements Runnable {
        C00971() {
        }

        public void run() {
            if (HRSActivity.this.mHrmValue > 0) {
                HRSActivity.this.updateGraph(HRSActivity.this.mHrmValue);
            }
            if (HRSActivity.this.isGraphInProgress) {
                HRSActivity.this.mHandler.postDelayed(HRSActivity.this.mRepeatTask, (long) HRSActivity.this.mInterval);
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.hrs.HRSActivity.2 */
    class C00982 implements Runnable {
        final /* synthetic */ int val$value;

        C00982(int i) {
            this.val$value = i;
        }

        public void run() {
            if (this.val$value < 0 || this.val$value > SupportMenu.USER_MASK) {
                HRSActivity.this.mHRSValue.setText(C0063R.string.not_available_value);
            } else {
                HRSActivity.this.mHRSValue.setText(Integer.toString(this.val$value));
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.hrs.HRSActivity.3 */
    class C00993 implements Runnable {
        final /* synthetic */ String val$position;

        C00993(String str) {
            this.val$position = str;
        }

        public void run() {
            if (this.val$position != null) {
                HRSActivity.this.mHRSPosition.setText(this.val$position);
            } else {
                HRSActivity.this.mHRSPosition.setText(C0063R.string.not_available);
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.hrs.HRSActivity.4 */
    class C01004 implements Runnable {
        C01004() {
        }

        public void run() {
            HRSActivity.this.mHRSValue.setText(C0063R.string.not_available_value);
            HRSActivity.this.mHRSPosition.setText(C0063R.string.not_available);
            HRSActivity.this.stopShowGraph();
        }
    }

    public HRSActivity() {
        this.TAG = "HRSActivity";
        this.MAX_HR_VALUE = SupportMenu.USER_MASK;
        this.MIN_POSITIVE_VALUE = 0;
        this.mHandler = new Handler();
        this.isGraphInProgress = false;
        this.mInterval = 1000;
        this.mHrmValue = 0;
        this.mCounter = 0;
        this.mRepeatTask = new C00971();
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_hrs);
        setGUI();
    }

    private void setGUI() {
        this.mLineGraph = LineGraphView.getLineGraphView();
        this.mHRSValue = (TextView) findViewById(C0063R.id.text_hrs_value);
        this.mHRSPosition = (TextView) findViewById(C0063R.id.text_hrs_position);
        showGraph();
    }

    private void showGraph() {
        this.mGraphView = this.mLineGraph.getView(this);
        ((ViewGroup) findViewById(C0063R.id.graph_hrs)).addView(this.mGraphView);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            this.isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
            this.mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
            this.mHrmValue = savedInstanceState.getInt(HR_VALUE);
            if (this.isGraphInProgress) {
                startShowGraph();
            }
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(GRAPH_STATUS, this.isGraphInProgress);
        outState.putInt(GRAPH_COUNTER, this.mCounter);
        outState.putInt(HR_VALUE, this.mHrmValue);
    }

    protected void onDestroy() {
        super.onDestroy();
        stopShowGraph();
    }

    protected int getAboutTextId() {
        return C0063R.string.hrs_about_text;
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.hrs_default_name;
    }

    protected UUID getFilterUUID() {
        return HRSManager.HR_SERVICE_UUID;
    }

    private void updateGraph(int hrmValue) {
        this.mCounter++;
        this.mLineGraph.addValue(new Point(this.mCounter, hrmValue));
        this.mGraphView.repaint();
    }

    void startShowGraph() {
        this.isGraphInProgress = true;
        this.mRepeatTask.run();
    }

    void stopShowGraph() {
        this.isGraphInProgress = false;
        this.mHandler.removeCallbacks(this.mRepeatTask);
    }

    protected BleManager<HRSManagerCallbacks> initializeManager() {
        HRSManager manager = HRSManager.getInstance(this);
        manager.setGattCallbacks((HRSManagerCallbacks) this);
        return manager;
    }

    private void setHRSValueOnView(int value) {
        runOnUiThread(new C00982(value));
    }

    private void setHRSPositionOnView(String position) {
        runOnUiThread(new C00993(position));
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    public void onHRSensorPositionFound(String position) {
        setHRSPositionOnView(position);
    }

    public void onHRNotificationEnabled() {
        startShowGraph();
    }

    public void onHRValueReceived(int value) {
        this.mHrmValue = value;
        setHRSValueOnView(this.mHrmValue);
    }

    public void onDeviceDisconnected() {
        super.onDeviceDisconnected();
        runOnUiThread(new C01004());
    }

    protected void setDefaultUI() {
        this.mHRSValue.setText(C0063R.string.not_available_value);
        this.mHRSPosition.setText(C0063R.string.not_available);
        clearGraph();
    }

    private void clearGraph() {
        this.mLineGraph.clearGraph();
        this.mGraphView.repaint();
        this.mCounter = 0;
        this.mHrmValue = 0;
    }
}
