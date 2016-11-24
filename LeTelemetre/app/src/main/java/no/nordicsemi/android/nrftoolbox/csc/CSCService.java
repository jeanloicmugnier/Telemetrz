package no.nordicsemi.android.nrftoolbox.csc;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.internal.view.SupportMenu;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.csc.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;

public class CSCService extends BleProfileService implements CSCManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.csc.ACTION_DISCONNECT";
    public static final String BROADCAST_CRANK_DATA = "no.nordicsemi.android.nrftoolbox.csc.BROADCAST_CRANK_DATA";
    public static final String BROADCAST_WHEEL_DATA = "no.nordicsemi.android.nrftoolbox.csc.BROADCAST_WHEEL_DATA";
    private static final int DISCONNECT_REQ = 1;
    public static final String EXTRA_CADENCE = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_CADENCE";
    public static final String EXTRA_DISTANCE = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_DISTANCE";
    public static final String EXTRA_GEAR_RATIO = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_GEAR_RATIO";
    public static final String EXTRA_SPEED = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_SPEED";
    public static final String EXTRA_TOTAL_DISTANCE = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_TOTAL_DISTANCE";
    private static final int NOTIFICATION_ID = 200;
    private static final int OPEN_ACTIVITY_REQ = 0;
    private static final String TAG = "CSCService";
    private boolean mBinded;
    private final LocalBinder mBinder;
    private BroadcastReceiver mDisconnectActionBroadcastReceiver;
    private int mFirstWheelRevolutions;
    private int mLastCrankEventTime;
    private int mLastCrankRevolutions;
    private int mLastWheelEventTime;
    private int mLastWheelRevolutions;
    private CSCManager mManager;
    private float mWheelCadence;

    /* renamed from: no.nordicsemi.android.nrftoolbox.csc.CSCService.1 */
    class C00751 extends BroadcastReceiver {
        C00751() {
        }

        public void onReceive(Context context, Intent intent) {
            Logger.m13i(CSCService.this.getLogSession(), "[CSC] Disconnect action pressed");
            if (CSCService.this.isConnected()) {
                CSCService.this.getBinder().disconnect();
            } else {
                CSCService.this.stopSelf();
            }
        }
    }

    public class CSCBinder extends LocalBinder {
        public CSCBinder() {
            super();
        }
    }

    public CSCService() {
        this.mFirstWheelRevolutions = -1;
        this.mLastWheelRevolutions = -1;
        this.mLastWheelEventTime = -1;
        this.mWheelCadence = -1.0f;
        this.mLastCrankRevolutions = -1;
        this.mLastCrankEventTime = -1;
        this.mBinder = new CSCBinder();
        this.mDisconnectActionBroadcastReceiver = new C00751();
    }

    protected LocalBinder getBinder() {
        return this.mBinder;
    }

    protected BleManager<CSCManagerCallbacks> initializeManager() {
        BleManager cSCManager = new CSCManager(this);
        this.mManager = (CSCManager) cSCManager;
        return cSCManager;
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
        createNotifcation(C0063R.string.csc_notification_connected_message, OPEN_ACTIVITY_REQ);
        return super.onUnbind(intent);
    }

    protected void onServiceStarted() {
        this.mManager.setLogger(getLogSession());
    }

    public void onWheelMeasurementReceived(int wheelRevolutions, int lastWheelEventTime) {
        int circumference = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.SETTINGS_WHEEL_SIZE, String.valueOf(SettingsFragment.SETTINGS_WHEEL_SIZE_DEFAULT)));
        if (this.mFirstWheelRevolutions < 0) {
            this.mFirstWheelRevolutions = wheelRevolutions;
        }
        if (this.mLastWheelEventTime != lastWheelEventTime) {
            if (this.mLastWheelRevolutions >= 0) {
                float timeDifference;
                if (lastWheelEventTime < this.mLastWheelEventTime) {
                    timeDifference = ((float) ((SupportMenu.USER_MASK + lastWheelEventTime) - this.mLastWheelEventTime)) / 1024.0f;
                } else {
                    timeDifference = ((float) (lastWheelEventTime - this.mLastWheelEventTime)) / 1024.0f;
                }
                float totalDistance = (((float) wheelRevolutions) * ((float) circumference)) / 1000.0f;
                float distance = (((float) (wheelRevolutions - this.mFirstWheelRevolutions)) * ((float) circumference)) / 1000.0f;
                float speed = (((float) ((wheelRevolutions - this.mLastWheelRevolutions) * circumference)) / 1000.0f) / timeDifference;
                this.mWheelCadence = (((float) (wheelRevolutions - this.mLastWheelRevolutions)) * 60.0f) / timeDifference;
                Intent broadcast = new Intent(BROADCAST_WHEEL_DATA);
                broadcast.putExtra(EXTRA_SPEED, speed);
                broadcast.putExtra(EXTRA_DISTANCE, distance);
                broadcast.putExtra(EXTRA_TOTAL_DISTANCE, totalDistance);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
            }
            this.mLastWheelRevolutions = wheelRevolutions;
            this.mLastWheelEventTime = lastWheelEventTime;
        }
    }

    public void onCrankMeasurementReceived(int crankRevolutions, int lastCrankEventTime) {
        if (this.mLastCrankEventTime != lastCrankEventTime) {
            if (this.mLastCrankRevolutions >= 0) {
                float timeDifference;
                if (lastCrankEventTime < this.mLastCrankEventTime) {
                    timeDifference = ((float) ((SupportMenu.USER_MASK + lastCrankEventTime) - this.mLastCrankEventTime)) / 1024.0f;
                } else {
                    timeDifference = ((float) (lastCrankEventTime - this.mLastCrankEventTime)) / 1024.0f;
                }
                float crankCadence = (((float) (crankRevolutions - this.mLastCrankRevolutions)) * 60.0f) / timeDifference;
                if (crankCadence > 0.0f) {
                    float gearRatio = this.mWheelCadence / crankCadence;
                    Intent broadcast = new Intent(BROADCAST_CRANK_DATA);
                    broadcast.putExtra(EXTRA_GEAR_RATIO, gearRatio);
                    broadcast.putExtra(EXTRA_CADENCE, (int) crankCadence);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
                }
            }
            this.mLastCrankRevolutions = crankRevolutions;
            this.mLastCrankEventTime = lastCrankEventTime;
        }
    }

    private void createNotifcation(int messageResId, int defaults) {
        boolean z = false;
        Intent parentIntent =new Intent(this, FeaturesActivity.class).addFlags(268435456);
        Intent targetIntent = new Intent(this, CSCActivity.class);
        PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, new Intent(ACTION_DISCONNECT), PendingIntent.FLAG_ONE_SHOT);
        Builder builder = new Builder(this).setContentIntent(PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_ONE_SHOT));
        Builder contentTitle = builder.setContentTitle(String.valueOf(C0063R.string.app_name));
        Object[] objArr = new Object[DISCONNECT_REQ];
        objArr[OPEN_ACTIVITY_REQ] = getDeviceName();
        contentTitle.setContentText(getString(messageResId, objArr));
        builder.setSmallIcon(Integer.valueOf(C0063R.drawable.ic_stat_notify_csc));
        if (defaults != 0) {
            z = true;
        }
        builder.setShowWhen(z).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(C0063R.drawable.ic_action_bluetooth, String.valueOf(C0063R.string.csc_notification_action_disconnect), disconnectAction);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
