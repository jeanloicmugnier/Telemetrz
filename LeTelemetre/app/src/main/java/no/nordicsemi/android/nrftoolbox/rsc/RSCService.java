package no.nordicsemi.android.nrftoolbox.rsc;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;

public class RSCService extends BleProfileService implements RSCManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.rsc.ACTION_DISCONNECT";
    public static final String BROADCAST_RSC_MEASUREMENT = "no.nordicsemi.android.nrftoolbox.rsc.BROADCAST_RSC_MEASUREMENT";
    public static final String BROADCAST_STRIDES_UPDATE = "no.nordicsemi.android.nrftoolbox.rsc.BROADCAST_STRIDES_UPDATE";
    private static final int DISCONNECT_REQ = 1;
    public static final String EXTRA_ACTIVITY = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_ACTIVITY";
    public static final String EXTRA_CADENCE = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_CADENCE";
    public static final String EXTRA_DISTANCE = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_DISTANCE";
    public static final String EXTRA_SPEED = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_SPEED";
    public static final String EXTRA_STRIDES = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_STRIDES";
    public static final String EXTRA_TOTAL_DISTANCE = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_TOTAL_DISTANCE";
    private static final int NOTIFICATION_ID = 200;
    private static final int OPEN_ACTIVITY_REQ = 0;
    private static final String TAG = "RSCService";
    private boolean mBinded;
    private final LocalBinder mBinder;
    private float mCadence;
    private BroadcastReceiver mDisconnectActionBroadcastReceiver;
    private float mDistance;
    private Handler mHandler;
    private RSCManager mManager;
    private int mStepsNumber;
    private float mStrideLength;
    private boolean mTaskInProgress;
    private Runnable mUpdateStridesTask;

    /* renamed from: no.nordicsemi.android.nrftoolbox.rsc.RSCService.1 */
    class C01361 implements Runnable {
        C01361() {
        }

        public void run() {
            if (RSCService.this.isConnected()) {
                RSCService.this.mStepsNumber = RSCService.this.mStepsNumber + RSCService.DISCONNECT_REQ;
                RSCService.access$216(RSCService.this, RSCService.this.mStrideLength);
                Intent broadcast = new Intent(RSCService.BROADCAST_STRIDES_UPDATE);
                broadcast.putExtra(RSCService.EXTRA_STRIDES, RSCService.this.mStepsNumber);
                broadcast.putExtra(RSCService.EXTRA_DISTANCE, RSCService.this.mDistance);
                LocalBroadcastManager.getInstance(RSCService.this).sendBroadcast(broadcast);
                if (RSCService.this.mCadence > 0.0f) {
                    RSCService.this.mHandler.postDelayed(RSCService.this.mUpdateStridesTask, (long) (65000.0f / RSCService.this.mCadence));
                    return;
                }
                RSCService.this.mTaskInProgress = false;
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.rsc.RSCService.2 */
    class C01372 extends BroadcastReceiver {
        C01372() {
        }

        public void onReceive(Context context, Intent intent) {
            Logger.m13i(RSCService.this.getLogSession(), "[RSC] Disconnect action pressed");
            if (RSCService.this.isConnected()) {
                RSCService.this.getBinder().disconnect();
            } else {
                RSCService.this.stopSelf();
            }
        }
    }

    public class RSCBinder extends LocalBinder {
        public RSCBinder() {
            super();
        }
    }

    public RSCService() {
        this.mHandler = new Handler();
        this.mBinder = new RSCBinder();
        this.mUpdateStridesTask = new C01361();
        this.mDisconnectActionBroadcastReceiver = new C01372();
    }

    static /* synthetic */ float access$216(RSCService x0, float x1) {
        float f = x0.mDistance + x1;
        x0.mDistance = f;
        return f;
    }

    protected LocalBinder getBinder() {
        return this.mBinder;
    }

    protected BleManager<RSCManagerCallbacks> initializeManager() {
        BleManager rSCManager = new RSCManager(this);
        this.mManager = (RSCManager) rSCManager;
        return rSCManager;
    }

    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISCONNECT);
        registerReceiver(this.mDisconnectActionBroadcastReceiver, filter);
    }

    public void onDestroy() {
        cancelNotification();
        unregisterReceiver(this.mDisconnectActionBroadcastReceiver);
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        this.mBinded = true;
        return super.onBind(intent);
    }

    public void onRebind(Intent intent) {
        this.mBinded = true;
        cancelNotification();
        if (isConnected()) {
            this.mManager.readBatteryLevel();
        }
    }

    public boolean onUnbind(Intent intent) {
        this.mBinded = false;
        createNotifcation(C0063R.string.rsc_notification_connected_message, OPEN_ACTIVITY_REQ);
        return super.onUnbind(intent);
    }

    protected void onServiceStarted() {
        this.mManager.setLogger(getLogSession());
    }

    public void onMeasurementReceived(float speed, int cadence, float totalDistance, float strideLen, int activity) {
        Intent broadcast = new Intent(BROADCAST_RSC_MEASUREMENT);
        broadcast.putExtra(EXTRA_SPEED, speed);
        broadcast.putExtra(EXTRA_CADENCE, cadence);
        broadcast.putExtra(EXTRA_TOTAL_DISTANCE, totalDistance);
        broadcast.putExtra(EXTRA_ACTIVITY, activity);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
        this.mCadence = (float) cadence;
        this.mStrideLength = strideLen;
        if (!this.mTaskInProgress && cadence > 0) {
            this.mTaskInProgress = true;
            this.mHandler.postDelayed(this.mUpdateStridesTask, (long) (65000.0f / this.mCadence));
        }
    }

    private void createNotifcation(int messageResId, int defaults) {
        boolean z = false;
        Intent parentIntent=new Intent(this, FeaturesActivity.class).addFlags(268435456);
        Intent targetIntent = new Intent(this, RSCActivity.class);
        PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, new Intent(ACTION_DISCONNECT), PendingIntent.FLAG_CANCEL_CURRENT);
        Builder builder = new Builder(this).setContentIntent(PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, 134217728));
        Builder contentTitle = builder.setContentTitle(getString(Integer.valueOf(C0063R.string.app_name)));
        Object[] objArr = new Object[DISCONNECT_REQ];
        objArr[OPEN_ACTIVITY_REQ] = getDeviceName();
        contentTitle.setContentText(getString(messageResId, objArr));
        builder.setSmallIcon(Integer.valueOf(C0063R.drawable.ic_stat_notify_rsc));
        if (defaults != 0) {
            z = true;
        }
        builder.setShowWhen(z).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(C0063R.drawable.ic_action_bluetooth, getString(Integer.valueOf(C0063R.string.rsc_notification_action_disconnect)), disconnectAction);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
