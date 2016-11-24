package no.nordicsemi.android.nrftoolbox.proximity;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;

public class ProximityService extends BleProfileService implements ProximityManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.proximity.ACTION_DISCONNECT";
    private static final int DISCONNECT_REQ = 1;
    private static final int NOTIFICATION_ID = 100;
    private static final int OPEN_ACTIVITY_REQ = 0;
    private static final String TAG = "ProximityService";
    private boolean mBinded;
    private final LocalBinder mBinder;
    private BroadcastReceiver mDisconnectActionBroadcastReceiver;
    private ProximityManager mProximityManager;

    /* renamed from: no.nordicsemi.android.nrftoolbox.proximity.ProximityService.1 */
    class C01321 extends BroadcastReceiver {
        C01321() {
        }

        public void onReceive(Context context, Intent intent) {
            Logger.m13i(ProximityService.this.getLogSession(), "[Proximity] Disconnect action pressed");
            if (ProximityService.this.isConnected()) {
                ProximityService.this.getBinder().disconnect();
            } else {
                ProximityService.this.stopSelf();
            }
        }
    }

    public class ProximityBinder extends LocalBinder {
        public ProximityBinder() {
            super();
        }

        public void startImmediateAlert() {
            Logger.m13i(getLogSession(), "[Proximity] Immediate alarm request: ON");
            ProximityService.this.mProximityManager.writeImmediateAlertOn();
        }

        public void stopImmediateAlert() {
            Logger.m13i(getLogSession(), "[Proximity] Immediate alarm request: OFF");
            ProximityService.this.mProximityManager.writeImmediateAlertOff();
        }
    }

    public ProximityService() {
        this.mBinder = new ProximityBinder();
        this.mDisconnectActionBroadcastReceiver = new C01321();
    }

    protected LocalBinder getBinder() {
        return this.mBinder;
    }

    protected BleManager<ProximityManagerCallbacks> initializeManager() {
        BleManager proximityManager = new ProximityManager(this);
        this.mProximityManager = (ProximityManager) proximityManager;
        return proximityManager;
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
    }

    public boolean onUnbind(Intent intent) {
        this.mBinded = false;
        createNotifcation(C0063R.string.proximity_notification_connected_message, OPEN_ACTIVITY_REQ);
        return super.onUnbind(intent);
    }

    protected void onServiceStarted() {
        this.mProximityManager.setLogger(getLogSession());
    }

    public void onLinklossOccur() {
        super.onLinklossOccur();
        if (!this.mBinded) {
            createNotifcation(C0063R.string.proximity_notification_linkloss_alert, -1);
        }
    }

    private void createNotifcation(int messageResId, int defaults) {
        boolean z = false;
        Intent parentIntent=new Intent(this, FeaturesActivity.class).addFlags(268435456);
        Intent targetIntent = new Intent(this, ProximityActivity.class);
        PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, new Intent(ACTION_DISCONNECT), PendingIntent.FLAG_CANCEL_CURRENT);
        Builder builder = new Builder(this).setContentIntent(PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_CANCEL_CURRENT));
        Builder contentTitle = builder.setContentTitle(getString(Integer.valueOf(C0063R.string.app_name)));
        Object[] objArr = new Object[DISCONNECT_REQ];
        objArr[OPEN_ACTIVITY_REQ] = getDeviceName();
        contentTitle.setContentText(getString(messageResId, objArr));
        builder.setSmallIcon(Integer.valueOf(C0063R.drawable.ic_stat_notify_proximity));
        if (defaults != 0) {
            z = true;
        }
        builder.setShowWhen(z).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(C0063R.drawable.ic_action_bluetooth, getString(Integer.valueOf(C0063R.string.proximity_notification_action_disconnect)), disconnectAction);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
