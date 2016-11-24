package no.nordicsemi.android.nrftoolbox.hts;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;

public class HTSService extends BleProfileService implements HTSManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.hts.ACTION_DISCONNECT";
    public static final String BROADCAST_HTS_MEASUREMENT = "no.nordicsemi.android.nrftoolbox.hts.BROADCAST_HTS_MEASUREMENT";
    private static final int DISCONNECT_REQ = 1;
    public static final String EXTRA_TEMPERATURE = "no.nordicsemi.android.nrftoolbox.hts.EXTRA_TEMPERATURE";
    private static final int NOTIFICATION_ID = 267;
    private static final int OPEN_ACTIVITY_REQ = 0;
    private boolean mBinded;
    private final LocalBinder mBinder;
    private BroadcastReceiver mDisconnectActionBroadcastReceiver;
    private HTSManager mManager;

    /* renamed from: no.nordicsemi.android.nrftoolbox.hts.HTSService.1 */
    class C01051 extends BroadcastReceiver {
        C01051() {
        }

        public void onReceive(Context context, Intent intent) {
            Logger.m13i(HTSService.this.getLogSession(), "[HTS] Disconnect action pressed");
            if (HTSService.this.isConnected()) {
                HTSService.this.getBinder().disconnect();
            } else {
                HTSService.this.stopSelf();
            }
        }
    }

    public class RSCBinder extends LocalBinder {
        public RSCBinder() {
            super();
        }
    }

    public HTSService() {
        this.mBinder = new RSCBinder();
        this.mDisconnectActionBroadcastReceiver = new C01051();
    }

    protected LocalBinder getBinder() {
        return this.mBinder;
    }

    protected BleManager<HTSManagerCallbacks> initializeManager() {
        BleManager hTSManager = new HTSManager();
        this.mManager = (HTSManager) hTSManager;
        return hTSManager;
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
        createNotifcation(C0063R.string.hts_notification_connected_message, 0);
        return super.onUnbind(intent);
    }

    public void onHTValueReceived(double value) {
        Intent broadcast = new Intent(BROADCAST_HTS_MEASUREMENT);
        broadcast.putExtra(EXTRA_TEMPERATURE, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void createNotifcation(int messageResId, int defaults) {
        boolean z = false;
        Intent parentIntent = new Intent(this, FeaturesActivity.class).addFlags(268435456);
        Intent targetIntent = new Intent(this, HTSActivity.class);
        PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, new Intent(ACTION_DISCONNECT), PendingIntent.FLAG_CANCEL_CURRENT);
        Builder builder = new Builder(this).setContentIntent(PendingIntent.getActivities(this, 0, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_CANCEL_CURRENT));
        Builder contentTitle = builder.setContentTitle(getString(Integer.valueOf(C0063R.string.app_name)));
        Object[] objArr = new Object[DISCONNECT_REQ];
        objArr[0] = getDeviceName();
        contentTitle.setContentText(getString(messageResId, objArr));
        builder.setSmallIcon(Integer.valueOf(C0063R.drawable.ic_stat_notify_hts));
        if (defaults != 0) {
            z = true;
        }
        builder.setShowWhen(z).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(C0063R.drawable.ic_action_bluetooth, getString(Integer.valueOf(C0063R.string.hts_notification_action_disconnect)), disconnectAction);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
