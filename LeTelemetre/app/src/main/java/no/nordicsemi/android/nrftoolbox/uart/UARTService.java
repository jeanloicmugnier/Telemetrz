package no.nordicsemi.android.nrftoolbox.uart;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;

public class UARTService extends BleProfileService implements UARTManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.uart.ACTION_DISCONNECT";
    public static final String BROADCAST_UART_RX = "no.nordicsemi.android.nrftoolbox.uart.BROADCAST_UART_RX";
    public static final String BROADCAST_UART_TX = "no.nordicsemi.android.nrftoolbox.uart.BROADCAST_UART_TX";
    private static final int DISCONNECT_REQ = 97;
    public static final String EXTRA_DATA = "no.nordicsemi.android.nrftoolbox.uart.EXTRA_DATA";
    private static final int NOTIFICATION_ID = 349;
    private static final int OPEN_ACTIVITY_REQ = 67;
    private boolean mBinded;
    private final LocalBinder mBinder;
    private BroadcastReceiver mDisconnectActionBroadcastReceiver;
    private UARTManager mManager;

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTService.1 */
    class C01521 extends BroadcastReceiver {
        C01521() {
        }

        public void onReceive(Context context, Intent intent) {
            Logger.m13i(UARTService.this.getLogSession(), "Disconnect action pressed");
            if (UARTService.this.isConnected()) {
                UARTService.this.getBinder().disconnect();
            } else {
                UARTService.this.stopSelf();
            }
        }
    }

    public class UARTBinder extends LocalBinder implements UARTInterface {
        public UARTBinder() {
            super();
        }

        public void send(String text) {
            UARTService.this.mManager.send(text);
        }

        public ILogSession getLogSession() {
            return super.getLogSession();
        }
    }

    public UARTService() {
        this.mBinder = new UARTBinder();
        this.mDisconnectActionBroadcastReceiver = new C01521();
    }

    protected LocalBinder getBinder() {
        return this.mBinder;
    }

    protected BleManager<UARTManagerCallbacks> initializeManager() {
        BleManager uARTManager = new UARTManager(this);
        this.mManager = (UARTManager) uARTManager;
        return uARTManager;
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
        createNotifcation(C0063R.string.uart_notification_connected_message, 0);
        return super.onUnbind(intent);
    }

    public void onDataReceived(String data) {
        Logger.m7a(getLogSession(), "\"" + data + "\" received");
        Intent broadcast = new Intent(BROADCAST_UART_RX);
        broadcast.putExtra(EXTRA_DATA, data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    public void onDataSent(String data) {
        Logger.m7a(getLogSession(), "\"" + data + "\" sent");
        Intent broadcast = new Intent(BROADCAST_UART_TX);
        broadcast.putExtra(EXTRA_DATA, data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void createNotifcation(int messageResId, int defaults) {
        boolean z = false;
        Intent parentIntent =new Intent(this, FeaturesActivity.class).addFlags(268435456);
        Intent targetIntent = new Intent(this, UARTActivity.class);
        PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, new Intent(ACTION_DISCONNECT), PendingIntent.FLAG_CANCEL_CURRENT);
        Builder builder = new Builder(this).setContentIntent(PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentTitle(getString(Integer.valueOf(C0063R.string.app_name))).setContentText(getString(messageResId, new Object[]{getDeviceName()}));
        builder.setSmallIcon(Integer.valueOf(C0063R.drawable.ic_stat_notify_uart));
        if (defaults != 0) {
            z = true;
        }
        builder.setShowWhen(z).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(C0063R.drawable.ic_action_bluetooth, getString(Integer.valueOf(C0063R.string.uart_notification_action_disconnect)), disconnectAction);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
