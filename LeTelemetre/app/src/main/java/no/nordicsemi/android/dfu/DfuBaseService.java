package no.nordicsemi.android.dfu;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
import no.nordicsemi.android.dfu.exception.DfuException;
import no.nordicsemi.android.dfu.exception.HexFileValidationException;
import no.nordicsemi.android.dfu.exception.UnknownResponseException;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;

public abstract class DfuBaseService extends IntentService {
    public static final int ACTION_ABORT = 2;
    public static final int ACTION_PAUSE = 0;
    public static final int ACTION_RESUME = 1;
    public static final String BROADCAST_ACTION = "no.nordicsemi.android.dfu.broadcast.BROADCAST_ACTION";
    public static final String BROADCAST_ERROR = "no.nordicsemi.android.dfu.broadcast.BROADCAST_ERROR";
    public static final String BROADCAST_LOG = "no.nordicsemi.android.dfu.broadcast.BROADCAST_LOG";
    public static final String BROADCAST_PROGRESS = "no.nordicsemi.android.dfu.broadcast.BROADCAST_PROGRESS";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG;
    private static final UUID DFU_CONTROL_POINT_UUID;
    public static final String DFU_IN_PROGRESS = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
    private static final UUID DFU_PACKET_UUID;
    public static final UUID DFU_SERVICE_UUID;
    public static final int DFU_STATUS_CRC_ERROR = 5;
    public static final int DFU_STATUS_DATA_SIZE_EXCEEDS_LIMIT = 4;
    public static final int DFU_STATUS_INVALID_STATE = 2;
    public static final int DFU_STATUS_NOT_SUPPORTED = 3;
    public static final int DFU_STATUS_OPERATION_FAILED = 6;
    public static final int DFU_STATUS_SUCCESS = 1;
    public static final int ERROR_CHARACTERISTICS_NOT_FOUND = 4103;
    public static final int ERROR_CONNECTION_MASK = 16384;
    public static final int ERROR_DEVICE_DISCONNECTED = 4096;
    public static final int ERROR_FILE_ERROR = 4098;
    public static final int ERROR_FILE_INVALID = 4099;
    public static final int ERROR_FILE_IO_EXCEPTION = 4100;
    public static final int ERROR_FILE_NOT_FOUND = 4097;
    public static final int ERROR_FILE_TYPE_UNSUPPORTED = 4105;
    public static final int ERROR_INVALID_RESPONSE = 4104;
    public static final int ERROR_MASK = 4096;
    public static final int ERROR_REMOTE_MASK = 8192;
    public static final int ERROR_SERVICE_DISCOVERY_NOT_STARTED = 4101;
    public static final int ERROR_SERVICE_NOT_FOUND = 4102;
    public static final String EXTRA_ACTION = "no.nordicsemi.android.dfu.extra.EXTRA_ACTION";
    public static final String EXTRA_AVG_SPEED_B_PER_MS = "no.nordicsemi.android.dfu.extra.EXTRA_AVG_SPEED_B_PER_MS";
    public static final String EXTRA_DATA = "no.nordicsemi.android.dfu.extra.EXTRA_DATA";
    public static final String EXTRA_DEVICE_ADDRESS = "no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_ADDRESS";
    public static final String EXTRA_DEVICE_NAME = "no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_NAME";
    public static final String EXTRA_FILE_MIME_TYPE = "no.nordicsemi.android.dfu.extra.EXTRA_MIME_TYPE";
    public static final String EXTRA_FILE_PATH = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_PATH";
    public static final String EXTRA_FILE_TYPE = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_TYPE";
    public static final String EXTRA_FILE_URI = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_URI";
    public static final String EXTRA_LOG_LEVEL = "no.nordicsemi.android.dfu.extra.EXTRA_LOG_LEVEL";
    public static final String EXTRA_LOG_MESSAGE = "no.nordicsemi.android.dfu.extra.EXTRA_LOG_INFO";
    public static final String EXTRA_LOG_URI = "no.nordicsemi.android.dfu.extra.EXTRA_LOG_URI";
    public static final String EXTRA_PARTS_TOTAL = "no.nordicsemi.android.dfu.extra.EXTRA_PARTS_TOTAL";
    public static final String EXTRA_PART_CURRENT = "no.nordicsemi.android.dfu.extra.EXTRA_PART_CURRENT";
    public static final String EXTRA_PROGRESS = "no.nordicsemi.android.dfu.extra.EXTRA_PROGRESS";
    public static final String EXTRA_SPEED_B_PER_MS = "no.nordicsemi.android.dfu.extra.EXTRA_SPEED_B_PER_MS";
    private static final char[] HEX_ARRAY;
    private static final int MAX_PACKET_SIZE = 20;
    public static final String MIME_TYPE_HEX = "application/octet-stream";
    public static final String MIME_TYPE_ZIP = "application/zip";
    public static final int NOTIFICATION_ID = 283;
    private static final byte[] OP_CODE_ACTIVATE_AND_RESET;
    private static final int OP_CODE_PACKET_RECEIPT_NOTIF_KEY = 17;
    private static final byte[] OP_CODE_PACKET_RECEIPT_NOTIF_REQ;
    private static final int OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY = 8;
    private static final int OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY = 5;
    private static final byte[] OP_CODE_RECEIVE_FIRMWARE_IMAGE;
    private static final int OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY = 3;
    private static final int OP_CODE_RECEIVE_RESET_KEY = 6;
    private static final int OP_CODE_RECEIVE_START_DFU_KEY = 1;
    private static final int OP_CODE_RECEIVE_VALIDATE_KEY = 4;
    private static final byte[] OP_CODE_RESET;
    private static final int OP_CODE_RESPONSE_CODE_KEY = 16;
    private static final byte[] OP_CODE_START_DFU;
    private static final byte[] OP_CODE_VALIDATE;
    public static final int PROGRESS_ABORTED = -7;
    public static final int PROGRESS_COMPLETED = -6;
    public static final int PROGRESS_CONNECTING = -1;
    public static final int PROGRESS_DISCONNECTING = -5;
    public static final int PROGRESS_STARTING = -2;
    public static final int PROGRESS_VALIDATING = -4;
    private static final int STATE_CLOSED = -5;
    private static final int STATE_CONNECTED = -2;
    private static final int STATE_CONNECTED_AND_READY = -3;
    private static final int STATE_CONNECTING = -1;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_DISCONNECTING = -4;
    private static final String TAG = "DfuService";
    public static final int TYPE_APPLICATION = 4;
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_BOOTLOADER = 2;
    public static final int TYPE_SOFT_DEVICE = 1;
    private boolean mAborted;
    private BluetoothAdapter mBluetoothAdapter;
    private byte[] mBuffer;
    private int mBytesConfirmed;
    private int mBytesSent;
    private int mConnectionState;
    private final BroadcastReceiver mConnectionStateBroadcastReceiver;
    private String mDeviceAddress;
    private String mDeviceName;
    private final BroadcastReceiver mDfuActionReceiver;
    private int mErrorState;
    private int mFileType;
    private final BluetoothGattCallback mGattCallback;
    private int mImageSizeInBytes;
    private boolean mImageSizeSent;
    private InputStream mInputStream;
    private int mLastBytesSent;
    private int mLastProgress;
    private long mLastProgressTime;
    private final Object mLock;
    private ILogSession mLogSession;
    private boolean mNotificationsEnabled;
    private int mPacketsBeforeNotification;
    private int mPacketsSentSinceNotification;
    private int mPartCurrent;
    private int mPartsTotal;
    private boolean mPaused;
    private byte[] mReceivedData;
    private boolean mRequestCompleted;
    private boolean mResetRequestSent;
    private long mStartTime;

    /* renamed from: no.nordicsemi.android.dfu.DfuBaseService.1 */
    class C00571 extends BroadcastReceiver {
        C00571() {
        }

        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.TYPE_AUTO)) {
                case DfuBaseService.TYPE_AUTO /*0*/:
                    DfuBaseService.this.mPaused = true;
                case DfuBaseService.TYPE_SOFT_DEVICE /*1*/:
                    DfuBaseService.this.mPaused = false;
                    synchronized (DfuBaseService.this.mLock) {
                        DfuBaseService.this.mLock.notifyAll();
                        break;
                    }
                case DfuBaseService.TYPE_BOOTLOADER /*2*/:
                    DfuBaseService.this.mPaused = false;
                    DfuBaseService.this.mAborted = true;
                    synchronized (DfuBaseService.this.mLock) {
                        DfuBaseService.this.mLock.notifyAll();
                        break;
                    }
                default:
            }
        }
    }

    /* renamed from: no.nordicsemi.android.dfu.DfuBaseService.2 */
    class C00582 extends BroadcastReceiver {
        C00582() {
        }

        public void onReceive(Context context, Intent intent) {
            if (((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")).getAddress().equals(DfuBaseService.this.mDeviceAddress)) {
                DfuBaseService.this.logi("Action received: " + intent.getAction());
                DfuBaseService.this.mConnectionState = DfuBaseService.TYPE_AUTO;
            }
        }
    }

    /* renamed from: no.nordicsemi.android.dfu.DfuBaseService.3 */
    class C00593 extends BluetoothGattCallback {
        C00593() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                DfuBaseService.this.loge("Connection state change error: " + status + " newState: " + newState);
                DfuBaseService.this.mErrorState = status | DfuBaseService.ERROR_CONNECTION_MASK;
            } else if (newState == DfuBaseService.TYPE_BOOTLOADER) {
                DfuBaseService.this.logi("Connected to GATT server");
                DfuBaseService.this.mConnectionState = DfuBaseService.STATE_CONNECTED;
                boolean success = gatt.discoverServices();
                DfuBaseService.this.logi("Attempting to start service discovery... " + (success ? "succeed" : "failed"));
                if (!success) {
                    DfuBaseService.this.mErrorState = DfuBaseService.ERROR_SERVICE_DISCOVERY_NOT_STARTED;
                } else {
                    return;
                }
            } else if (newState == 0) {
                DfuBaseService.this.logi("Disconnected from GATT server");
                DfuBaseService.this.mPaused = false;
                DfuBaseService.this.mConnectionState = DfuBaseService.TYPE_AUTO;
            }
            synchronized (DfuBaseService.this.mLock) {
                DfuBaseService.this.mLock.notifyAll();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                DfuBaseService.this.logi("Services discovered");
                DfuBaseService.this.mConnectionState = DfuBaseService.STATE_CONNECTED_AND_READY;
            } else {
                DfuBaseService.this.loge("Service discovery error: " + status);
                DfuBaseService.this.mErrorState = status | DfuBaseService.ERROR_CONNECTION_MASK;
            }
            synchronized (DfuBaseService.this.mLock) {
                DfuBaseService.this.mLock.notifyAll();
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            boolean z = true;
            if (status != 0) {
                DfuBaseService.this.loge("Descriptor write error: " + status);
                DfuBaseService.this.mErrorState = status | DfuBaseService.ERROR_CONNECTION_MASK;
            } else if (DfuBaseService.CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())) {
                DfuBaseService dfuBaseService = DfuBaseService.this;
                if (descriptor.getValue()[DfuBaseService.TYPE_AUTO] != (byte) 1) {
                    z = false;
                }
                dfuBaseService.mNotificationsEnabled = z;
            }
            synchronized (DfuBaseService.this.mLock) {
                DfuBaseService.this.mLock.notifyAll();
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (!DfuBaseService.DFU_PACKET_UUID.equals(characteristic.getUuid())) {
                    DfuBaseService.this.sendLogBroadcast(DfuBaseService.OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY, "Data written to " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
                    DfuBaseService.this.mRequestCompleted = true;
                } else if (DfuBaseService.this.mImageSizeSent) {
                    DfuBaseService.access$1212(DfuBaseService.this, characteristic.getValue().length);
                    DfuBaseService.this.mPacketsSentSinceNotification = DfuBaseService.this.mPacketsSentSinceNotification + DfuBaseService.TYPE_SOFT_DEVICE;
                    boolean notificationExpected = DfuBaseService.this.mPacketsBeforeNotification > 0 && DfuBaseService.this.mPacketsSentSinceNotification == DfuBaseService.this.mPacketsBeforeNotification;
                    boolean lastPacketTransfered;
                    if (DfuBaseService.this.mBytesSent == DfuBaseService.this.mImageSizeInBytes) {
                        lastPacketTransfered = true;
                    } else {
                        lastPacketTransfered = false;
                    }
                    if (!notificationExpected && !lastPacketTransfered) {
                        try {
                            DfuBaseService.this.waitIfPaused();
                            if (DfuBaseService.this.mAborted) {
                                synchronized (DfuBaseService.this.mLock) {
                                    DfuBaseService.this.mLock.notifyAll();
                                }
                                return;
                            }
                            byte[] buffer = DfuBaseService.this.mBuffer;
                            DfuBaseService.this.writePacket(gatt, characteristic, buffer, DfuBaseService.this.mInputStream.read(buffer));
                            DfuBaseService.this.updateProgressNotification();
                            return;
                        } catch (HexFileValidationException e) {
                            DfuBaseService.this.loge("Invalid HEX file");
                            DfuBaseService.this.mErrorState = DfuBaseService.ERROR_FILE_INVALID;
                        } catch (IOException e2) {
                            DfuBaseService.this.loge("Error while reading the input stream", e2);
                            DfuBaseService.this.mErrorState = DfuBaseService.ERROR_FILE_IO_EXCEPTION;
                        }
                    } else {
                        return;
                    }
                } else {
                    DfuBaseService.this.mImageSizeSent = true;
                }
            } else if (DfuBaseService.this.mResetRequestSent) {
                DfuBaseService.this.mRequestCompleted = true;
            } else {
                DfuBaseService.this.loge("Characteristic write error: " + status);
                DfuBaseService.this.mErrorState = status | DfuBaseService.ERROR_CONNECTION_MASK;
            }
            synchronized (DfuBaseService.this.mLock) {
                DfuBaseService.this.mLock.notifyAll();
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            switch (characteristic.getIntValue(DfuBaseService.OP_CODE_PACKET_RECEIPT_NOTIF_KEY, DfuBaseService.TYPE_AUTO).intValue()) {
                case DfuBaseService.OP_CODE_PACKET_RECEIPT_NOTIF_KEY /*17*/:
                    BluetoothGattCharacteristic packetCharacteristic = gatt.getService(DfuBaseService.DFU_SERVICE_UUID).getCharacteristic(DfuBaseService.DFU_PACKET_UUID);
                    try {
                        DfuBaseService.this.mBytesConfirmed = characteristic.getIntValue(DfuBaseService.MAX_PACKET_SIZE, DfuBaseService.TYPE_SOFT_DEVICE).intValue();
                        DfuBaseService.this.mPacketsSentSinceNotification = DfuBaseService.TYPE_AUTO;
                        DfuBaseService.this.waitIfPaused();
                        if (!DfuBaseService.this.mAborted) {
                            byte[] buffer = DfuBaseService.this.mBuffer;
                            DfuBaseService.this.writePacket(gatt, packetCharacteristic, buffer, DfuBaseService.this.mInputStream.read(buffer));
                            DfuBaseService.this.updateProgressNotification();
                            return;
                        }
                    } catch (HexFileValidationException e) {
                        DfuBaseService.this.loge("Invalid HEX file");
                        DfuBaseService.this.mErrorState = DfuBaseService.ERROR_FILE_INVALID;
                        break;
                    } catch (IOException e2) {
                        DfuBaseService.this.loge("Error while reading the input stream", e2);
                        DfuBaseService.this.mErrorState = DfuBaseService.ERROR_FILE_IO_EXCEPTION;
                        break;
                    }
                    break;
                default:
                    DfuBaseService.this.sendLogBroadcast(DfuBaseService.OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY, "Received Read Response from " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
                    DfuBaseService.this.mReceivedData = characteristic.getValue();
                    break;
            }
            synchronized (DfuBaseService.this.mLock) {
                DfuBaseService.this.mLock.notifyAll();
            }
        }

        public String parse(BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data == null) {
                return "";
            }
            int length = data.length;
            if (length == 0) {
                return "";
            }
            char[] out = new char[((length * DfuBaseService.OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY) + DfuBaseService.STATE_CONNECTING)];
            for (int j = DfuBaseService.TYPE_AUTO; j < length; j += DfuBaseService.TYPE_SOFT_DEVICE) {
                int v = data[j] & MotionEventCompat.ACTION_MASK;
                out[j * DfuBaseService.OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY] = DfuBaseService.HEX_ARRAY[v >>> DfuBaseService.TYPE_APPLICATION];
                out[(j * DfuBaseService.OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY) + DfuBaseService.TYPE_SOFT_DEVICE] = DfuBaseService.HEX_ARRAY[v & 15];
                if (j != length + DfuBaseService.STATE_CONNECTING) {
                    out[(j * DfuBaseService.OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY) + DfuBaseService.TYPE_BOOTLOADER] = '-';
                }
            }
            return new String(out);
        }
    }

    protected abstract Class<? extends Activity> getNotificationTarget();

    static /* synthetic */ int access$1212(DfuBaseService x0, int x1) {
        int i = x0.mBytesSent + x1;
        x0.mBytesSent = i;
        return i;
    }

    static {
        HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        OP_CODE_START_DFU = new byte[]{(byte) 1, (byte) 0};
        byte[] bArr = new byte[TYPE_SOFT_DEVICE];
        bArr[TYPE_AUTO] = (byte) 3;
        OP_CODE_RECEIVE_FIRMWARE_IMAGE = bArr;
        bArr = new byte[TYPE_SOFT_DEVICE];
        bArr[TYPE_AUTO] = (byte) 4;
        OP_CODE_VALIDATE = bArr;
        bArr = new byte[TYPE_SOFT_DEVICE];
        bArr[TYPE_AUTO] = (byte) 5;
        OP_CODE_ACTIVATE_AND_RESET = bArr;
        bArr = new byte[TYPE_SOFT_DEVICE];
        bArr[TYPE_AUTO] = (byte) 6;
        OP_CODE_RESET = bArr;
        OP_CODE_PACKET_RECEIPT_NOTIF_REQ = new byte[]{(byte) 8, (byte) 0, (byte) 0};
        DFU_SERVICE_UUID = new UUID(23296205844446L, 1523193452336828707L);
        DFU_CONTROL_POINT_UUID = new UUID(23300500811742L, 1523193452336828707L);
        DFU_PACKET_UUID = new UUID(23304795779038L, 1523193452336828707L);
        CLIENT_CHARACTERISTIC_CONFIG = new UUID(45088566677504L, -9223371485494954757L);
    }

    public DfuBaseService() {
        super(TAG);
        this.mLock = new Object();
        this.mPacketsBeforeNotification = 10;
        this.mBuffer = new byte[MAX_PACKET_SIZE];
        this.mReceivedData = null;
        this.mDfuActionReceiver = new C00571();
        this.mConnectionStateBroadcastReceiver = new C00582();
        this.mGattCallback = new C00593();
        this.mLastProgress = STATE_CONNECTING;
    }

    public void onCreate() {
        super.onCreate();
        initialize();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        IntentFilter actionFilter = makeDfuActionIntentFilter();
        manager.registerReceiver(this.mDfuActionReceiver, actionFilter);
        registerReceiver(this.mDfuActionReceiver, actionFilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        registerReceiver(this.mConnectionStateBroadcastReceiver, filter);
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDfuActionReceiver);
        unregisterReceiver(this.mDfuActionReceiver);
        unregisterReceiver(this.mConnectionStateBroadcastReceiver);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump.
    protected void onHandleIntent(android.content.Intent r45) {
        /*
        r44 = this;
        r33 = android.preference.PreferenceManager.getDefaultSharedPreferences(r44);
        r17 = r33.edit();
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 1;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);
        r17.commit();
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_ADDRESS";
        r0 = r45;
        r12 = r0.getStringExtra(r5);
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_NAME";
        r0 = r45;
        r13 = r0.getStringExtra(r5);
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_PATH";
        r0 = r45;
        r21 = r0.getStringExtra(r5);
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_URI";
        r0 = r45;
        r23 = r0.getParcelableExtra(r5);
        r23 = (android.net.Uri) r23;
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_LOG_URI";
        r0 = r45;
        r26 = r0.getParcelableExtra(r5);
        r26 = (android.net.Uri) r26;
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_TYPE";
        r40 = 0;
        r0 = r45;
        r1 = r40;
        r22 = r0.getIntExtra(r5, r1);
        if (r21 == 0) goto L_0x0066;
    L_0x0050:
        if (r22 != 0) goto L_0x0066;
    L_0x0052:
        r5 = java.util.Locale.US;
        r0 = r21;
        r5 = r0.toLowerCase(r5);
        r40 = "zip";
        r0 = r40;
        r5 = r5.endsWith(r0);
        if (r5 == 0) goto L_0x00ce;
    L_0x0064:
        r22 = 0;
    L_0x0066:
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_MIME_TYPE";
        r0 = r45;
        r28 = r0.getStringExtra(r5);
        if (r28 == 0) goto L_0x00d1;
    L_0x0070:
        r0 = r44;
        r1 = r26;
        r5 = no.nordicsemi.android.log.Logger.openSession(r0, r1);
        r0 = r44;
        r0.mLogSession = r5;
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_PART_CURRENT";
        r40 = 1;
        r0 = r45;
        r1 = r40;
        r5 = r0.getIntExtra(r5, r1);
        r0 = r44;
        r0.mPartCurrent = r5;
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_PARTS_TOTAL";
        r40 = 1;
        r0 = r45;
        r1 = r40;
        r5 = r0.getIntExtra(r5, r1);
        r0 = r44;
        r0.mPartsTotal = r5;
        r5 = r22 & -8;
        if (r5 > 0) goto L_0x00b4;
    L_0x00a0:
        r5 = "application/zip";
        r0 = r28;
        r5 = r5.equals(r0);
        if (r5 != 0) goto L_0x00d9;
    L_0x00aa:
        r5 = "application/octet-stream";
        r0 = r28;
        r5 = r5.equals(r0);
        if (r5 != 0) goto L_0x00d9;
    L_0x00b4:
        r5 = "File type or file mime-type not supported";
        r0 = r44;
        r0.logw(r5);
        r5 = 15;
        r40 = "File type or file mime-type not supported";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);
        r5 = 4105; // 0x1009 float:5.752E-42 double:2.028E-320;
        r0 = r44;
        r0.sendErrorBroadcast(r5);
    L_0x00cd:
        return;
    L_0x00ce:
        r22 = 4;
        goto L_0x0066;
    L_0x00d1:
        if (r22 != 0) goto L_0x00d6;
    L_0x00d3:
        r28 = "application/zip";
        goto L_0x0070;
    L_0x00d6:
        r28 = "application/octet-stream";
        goto L_0x0070;
    L_0x00d9:
        r5 = "application/octet-stream";
        r0 = r28;
        r5 = r5.equals(r0);
        if (r5 == 0) goto L_0x010c;
    L_0x00e3:
        r5 = 1;
        r0 = r22;
        if (r0 == r5) goto L_0x010c;
    L_0x00e8:
        r5 = 2;
        r0 = r22;
        if (r0 == r5) goto L_0x010c;
    L_0x00ed:
        r5 = 4;
        r0 = r22;
        if (r0 == r5) goto L_0x010c;
    L_0x00f2:
        r5 = "Unable to determine file type";
        r0 = r44;
        r0.logw(r5);
        r5 = 15;
        r40 = "Unable to determine file type";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);
        r5 = 4105; // 0x1009 float:5.752E-42 double:2.028E-320;
        r0 = r44;
        r0.sendErrorBroadcast(r5);
        goto L_0x00cd;
    L_0x010c:
        r0 = r44;
        r0.mDeviceAddress = r12;
        r0 = r44;
        r0.mDeviceName = r13;
        r5 = 0;
        r0 = r44;
        r0.mConnectionState = r5;
        r5 = 0;
        r0 = r44;
        r0.mBytesSent = r5;
        r5 = 0;
        r0 = r44;
        r0.mBytesConfirmed = r5;
        r5 = 0;
        r0 = r44;
        r0.mPacketsSentSinceNotification = r5;
        r5 = 0;
        r0 = r44;
        r0.mErrorState = r5;
        r5 = 0;
        r0 = r44;
        r0.mAborted = r5;
        r5 = 0;
        r0 = r44;
        r0.mPaused = r5;
        r5 = 0;
        r0 = r44;
        r0.mNotificationsEnabled = r5;
        r5 = 0;
        r0 = r44;
        r0.mResetRequestSent = r5;
        r5 = 0;
        r0 = r44;
        r0.mRequestCompleted = r5;
        r5 = 0;
        r0 = r44;
        r0.mImageSizeSent = r5;
        r5 = "settings_packet_receipt_notification_enabled";
        r40 = 1;
        r0 = r33;
        r1 = r40;
        r32 = r0.getBoolean(r5, r1);
        r5 = "settings_number_of_packets";
        r40 = 10;
        r40 = java.lang.String.valueOf(r40);
        r0 = r33;
        r1 = r40;
        r38 = r0.getString(r5, r1);
        r30 = 10;
        r30 = java.lang.Integer.parseInt(r38);	 Catch:{ NumberFormatException -> 0x02a1 }
        if (r30 < 0) goto L_0x0176;
    L_0x016f:
        r5 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r0 = r30;
        if (r0 <= r5) goto L_0x0178;
    L_0x0176:
        r30 = 10;
    L_0x0178:
        if (r32 != 0) goto L_0x017c;
    L_0x017a:
        r30 = 0;
    L_0x017c:
        r0 = r30;
        r1 = r44;
        r1.mPacketsBeforeNotification = r0;
        r5 = "settings_mbr_size";
        r40 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        r40 = java.lang.String.valueOf(r40);
        r0 = r33;
        r1 = r40;
        r38 = r0.getString(r5, r1);
        r27 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        r27 = java.lang.Integer.parseInt(r38);	 Catch:{ NumberFormatException -> 0x02a6 }
        if (r27 >= 0) goto L_0x019c;
    L_0x019a:
        r27 = 0;
    L_0x019c:
        r5 = 1;
        r40 = "Starting DFU service";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);
        r25 = 0;
        r5 = 1;
        r40 = "Opening file...";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        if (r23 == 0) goto L_0x02ab;
    L_0x01b4:
        r0 = r44;
        r1 = r23;
        r2 = r28;
        r3 = r27;
        r4 = r22;
        r25 = r0.openInputStream(r1, r2, r3, r4);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
    L_0x01c2:
        r0 = r25;
        r1 = r44;
        r1.mInputStream = r0;	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r24 = r25.available();	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r0 = r24;
        r1 = r44;
        r1.mImageSizeInBytes = r0;	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        if (r22 != 0) goto L_0x01e8;
    L_0x01d4:
        r5 = "application/zip";
        r0 = r28;
        r5 = r5.equals(r0);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        if (r5 == 0) goto L_0x01e8;
    L_0x01de:
        r0 = r25;
        r0 = (no.nordicsemi.android.dfu.ZipHexInputStream) r0;	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r39 = r0;
        r22 = r39.getContentType();	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
    L_0x01e8:
        r0 = r22;
        r1 = r44;
        r1.mFileType = r0;	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r5 = 5;
        r40 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r40.<init>();	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r41 = "Image file opened (";
        r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r0 = r44;
        r0 = r0.mImageSizeInBytes;	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r41 = r0;
        r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r41 = " bytes in total)";
        r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r40 = r40.toString();	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        r5 = 1;
        r40 = "Connecting to DFU target...";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = -1;
        r0 = r44;
        r0.updateProgressNotification(r5);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r6 = r0.connect(r12);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r5 = r0.mErrorState;	 Catch:{ all -> 0x0771 }
        if (r5 <= 0) goto L_0x0311;
    L_0x0231:
        r0 = r44;
        r5 = r0.mErrorState;	 Catch:{ all -> 0x0771 }
        r0 = r5 & -16385;
        r20 = r0;
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0771 }
        r5.<init>();	 Catch:{ all -> 0x0771 }
        r40 = "An error occurred while connecting to the device:";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0771 }
        r0 = r20;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0771 }
        r5 = r5.toString();	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r0.loge(r5);	 Catch:{ all -> 0x0771 }
        r5 = 20;
        r40 = "Connection failed (0x%02X): %s";
        r41 = 2;
        r0 = r41;
        r0 = new java.lang.Object[r0];	 Catch:{ all -> 0x0771 }
        r41 = r0;
        r42 = 0;
        r43 = java.lang.Integer.valueOf(r20);	 Catch:{ all -> 0x0771 }
        r41[r42] = r43;	 Catch:{ all -> 0x0771 }
        r42 = 1;
        r43 = no.nordicsemi.android.error.GattError.parse(r20);	 Catch:{ all -> 0x0771 }
        r41[r42] = r43;	 Catch:{ all -> 0x0771 }
        r40 = java.lang.String.format(r40, r41);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r5 = r0.mErrorState;	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ all -> 0x0771 }
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c62 }
        r17.commit();	 Catch:{ IOException -> 0x0c62 }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c62 }
        if (r25 == 0) goto L_0x029d;
    L_0x029a:
        r25.close();	 Catch:{ IOException -> 0x0c62 }
    L_0x029d:
        r25 = 0;
        goto L_0x00cd;
    L_0x02a1:
        r15 = move-exception;
        r30 = 10;
        goto L_0x0178;
    L_0x02a6:
        r15 = move-exception;
        r27 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        goto L_0x019c;
    L_0x02ab:
        r0 = r44;
        r1 = r21;
        r2 = r28;
        r3 = r27;
        r4 = r22;
        r25 = r0.openInputStream(r1, r2, r3, r4);	 Catch:{ FileNotFoundException -> 0x02bb, IOException -> 0x02e6 }
        goto L_0x01c2;
    L_0x02bb:
        r15 = move-exception;
        r5 = "An exception occured while opening file";
        r0 = r44;
        r0.loge(r5, r15);	 Catch:{ all -> 0x0771 }
        r5 = 4097; // 0x1001 float:5.741E-42 double:2.024E-320;
        r0 = r44;
        r0.sendErrorBroadcast(r5);	 Catch:{ all -> 0x0771 }
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c68 }
        r17.commit();	 Catch:{ IOException -> 0x0c68 }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c68 }
        if (r25 == 0) goto L_0x02e2;
    L_0x02df:
        r25.close();	 Catch:{ IOException -> 0x0c68 }
    L_0x02e2:
        r25 = 0;
        goto L_0x00cd;
    L_0x02e6:
        r15 = move-exception;
        r5 = "An exception occured while calculating file size";
        r0 = r44;
        r0.loge(r5, r15);	 Catch:{ all -> 0x0771 }
        r5 = 4098; // 0x1002 float:5.743E-42 double:2.0247E-320;
        r0 = r44;
        r0.sendErrorBroadcast(r5);	 Catch:{ all -> 0x0771 }
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c65 }
        r17.commit();	 Catch:{ IOException -> 0x0c65 }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c65 }
        if (r25 == 0) goto L_0x030d;
    L_0x030a:
        r25.close();	 Catch:{ IOException -> 0x0c65 }
    L_0x030d:
        r25 = 0;
        goto L_0x00cd;
    L_0x0311:
        r0 = r44;
        r5 = r0.mAborted;	 Catch:{ all -> 0x0771 }
        if (r5 == 0) goto L_0x034b;
    L_0x0317:
        r5 = "Upload aborted";
        r0 = r44;
        r0.logi(r5);	 Catch:{ all -> 0x0771 }
        r5 = 15;
        r40 = "Upload aborted";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = -7;
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ all -> 0x0771 }
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c5f }
        r17.commit();	 Catch:{ IOException -> 0x0c5f }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c5f }
        if (r25 == 0) goto L_0x0347;
    L_0x0344:
        r25.close();	 Catch:{ IOException -> 0x0c5f }
    L_0x0347:
        r25 = 0;
        goto L_0x00cd;
    L_0x034b:
        r5 = DFU_SERVICE_UUID;	 Catch:{ all -> 0x0771 }
        r14 = r6.getService(r5);	 Catch:{ all -> 0x0771 }
        if (r14 != 0) goto L_0x0388;
    L_0x0353:
        r5 = "DFU service does not exists on the device";
        r0 = r44;
        r0.loge(r5);	 Catch:{ all -> 0x0771 }
        r5 = 15;
        r40 = "Connected. DFU Service not found";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = 4102; // 0x1006 float:5.748E-42 double:2.0267E-320;
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ all -> 0x0771 }
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c5c }
        r17.commit();	 Catch:{ IOException -> 0x0c5c }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c5c }
        if (r25 == 0) goto L_0x0384;
    L_0x0381:
        r25.close();	 Catch:{ IOException -> 0x0c5c }
    L_0x0384:
        r25 = 0;
        goto L_0x00cd;
    L_0x0388:
        r5 = DFU_CONTROL_POINT_UUID;	 Catch:{ all -> 0x0771 }
        r11 = r14.getCharacteristic(r5);	 Catch:{ all -> 0x0771 }
        r5 = DFU_PACKET_UUID;	 Catch:{ all -> 0x0771 }
        r7 = r14.getCharacteristic(r5);	 Catch:{ all -> 0x0771 }
        if (r11 == 0) goto L_0x0398;
    L_0x0396:
        if (r7 != 0) goto L_0x03cd;
    L_0x0398:
        r5 = "DFU characteristics not found in the DFU service";
        r0 = r44;
        r0.loge(r5);	 Catch:{ all -> 0x0771 }
        r5 = 15;
        r40 = "Connected. DFU Characteristics not found";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = 4103; // 0x1007 float:5.75E-42 double:2.027E-320;
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ all -> 0x0771 }
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c59 }
        r17.commit();	 Catch:{ IOException -> 0x0c59 }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c59 }
        if (r25 == 0) goto L_0x03c9;
    L_0x03c6:
        r25.close();	 Catch:{ IOException -> 0x0c59 }
    L_0x03c9:
        r25 = 0;
        goto L_0x00cd;
    L_0x03cd:
        r5 = 5;
        r40 = "Connected. Services discovered";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = -2;
        r0 = r44;
        r0.updateProgressNotification(r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 1;
        r0 = r44;
        r0.setCharacteristicNotification(r6, r11, r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 5;
        r40 = "Notifications enabled";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r34 = 0;
        r37 = 0;
        r5 = r22 & 1;
        if (r5 <= 0) goto L_0x05b5;
    L_0x03f5:
        r8 = r24;
    L_0x03f7:
        r5 = r22 & 2;
        if (r5 <= 0) goto L_0x05b8;
    L_0x03fb:
        r9 = r24;
    L_0x03fd:
        r5 = r22 & 4;
        if (r5 <= 0) goto L_0x05bb;
    L_0x0401:
        r10 = r24;
    L_0x0403:
        r5 = "application/zip";
        r0 = r28;
        r5 = r5.equals(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        if (r5 == 0) goto L_0x041f;
    L_0x040d:
        r0 = r25;
        r0 = (no.nordicsemi.android.dfu.ZipHexInputStream) r0;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r39 = r0;
        r8 = r39.softDeviceImageSize();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r9 = r39.bootloaderImageSize();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r10 = r39.applicationImageSize();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x041f:
        r5 = OP_CODE_START_DFU;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = 1;
        r0 = r22;
        r0 = (byte) r0;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = r0;
        r5[r40] = r41;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5.<init>();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = "Sending Start DFU command (Op Code = 1, Upload Mode = ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = ")";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = r5.toString();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r0.logi(r5);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = OP_CODE_START_DFU;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40.<init>();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = "DFU Start sent (Op Code 1, Upload Mode = ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r1 = r22;
        r40 = r0.append(r1);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = r40.toString();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5.<init>();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = "Sending image size array to DFU Packet: [";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = r5.append(r8);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = "b, ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = r5.append(r9);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = "b, ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = r5.append(r10);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = "b]";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = r5.toString();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r0.logi(r5);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = r44;
        r5.writeImageSize(r6, r7, r8, r9, r10);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40.<init>();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = "Firmware image size sent [";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r40 = r0.append(r8);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = "b, ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r40 = r0.append(r9);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = "b, ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r40 = r0.append(r10);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = "b]";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = r40.toString();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r34 = r44.readNotificationResponse();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = 1;
        r0 = r44;
        r1 = r34;
        r37 = r0.getStatusCode(r1, r5);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40.<init>();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = "Responce received (Op Code: ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = 1;
        r41 = r34[r41];	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = " Status: ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r1 = r37;
        r40 = r0.append(r1);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = r40.toString();	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r5 = 1;
        r0 = r37;
        if (r0 == r5) goto L_0x095a;
    L_0x053c:
        r5 = new no.nordicsemi.android.dfu.exception.RemoteDfuException;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        r40 = "Starting DFU failed";
        r0 = r40;
        r1 = r37;
        r5.<init>(r0, r1);	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
        throw r5;	 Catch:{ RemoteDfuException -> 0x0548, UnknownResponseException -> 0x0560 }
    L_0x0548:
        r15 = move-exception;
        r5 = r15.getErrorNumber();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = 3;
        r0 = r40;
        if (r5 == r0) goto L_0x05be;
    L_0x0553:
        throw r15;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
    L_0x0554:
        r16 = move-exception;
        r5 = r16.getErrorNumber();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 3;
        r0 = r40;
        if (r5 == r0) goto L_0x07c0;
    L_0x055f:
        throw r16;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x0560:
        r15 = move-exception;
        r20 = 4104; // 0x1008 float:5.751E-42 double:2.0276E-320;
        r5 = r15.getMessage();	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r0.loge(r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 20;
        r40 = r15.getMessage();	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = "Sending Reset command (Op Code = 6)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = OP_CODE_RESET;	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 10;
        r40 = "Reset request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 4104; // 0x1008 float:5.751E-42 double:2.0276E-320;
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
    L_0x0599:
        r5 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r40 = 0;
        r0 = r17;
        r1 = r40;
        r0.putBoolean(r5, r1);	 Catch:{ IOException -> 0x0c4d }
        r17.commit();	 Catch:{ IOException -> 0x0c4d }
        r5 = 0;
        r0 = r44;
        r0.mInputStream = r5;	 Catch:{ IOException -> 0x0c4d }
        if (r25 == 0) goto L_0x05b1;
    L_0x05ae:
        r25.close();	 Catch:{ IOException -> 0x0c4d }
    L_0x05b1:
        r25 = 0;
        goto L_0x00cd;
    L_0x05b5:
        r8 = 0;
        goto L_0x03f7;
    L_0x05b8:
        r9 = 0;
        goto L_0x03fd;
    L_0x05bb:
        r10 = 0;
        goto L_0x0403;
    L_0x05be:
        r5 = r22 & 4;
        if (r5 <= 0) goto L_0x0792;
    L_0x05c2:
        r5 = r22 & 3;
        if (r5 <= 0) goto L_0x0792;
    L_0x05c6:
        r5 = "DFU target does not support (SD/BL)+App update";
        r0 = r44;
        r0.logw(r5);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 15;
        r40 = "DFU target does not support (SD/BL)+App update";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r22 = r22 & -5;
        r0 = r22;
        r1 = r44;
        r1.mFileType = r0;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = OP_CODE_START_DFU;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = 1;
        r0 = r22;
        r0 = (byte) r0;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = r0;
        r5[r40] = r41;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 2;
        r0 = r44;
        r0.mPartsTotal = r5;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r25;
        r0 = (no.nordicsemi.android.dfu.ZipHexInputStream) r0;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r39 = r0;
        r0 = r39;
        r1 = r22;
        r0.setContentType(r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r10 = 0;
        r5 = r25.available();	 Catch:{ IOException -> 0x0c56 }
        r0 = r44;
        r0.mImageSizeInBytes = r5;	 Catch:{ IOException -> 0x0c56 }
    L_0x0606:
        r5 = 1;
        r40 = "Sending only SD/BL";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5.<init>();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = "Resending Start DFU command (Op Code = 1, Upload Mode = ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = ")";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = r5.toString();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r0.logi(r5);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = OP_CODE_START_DFU;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40.<init>();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = "DFU Start sent (Op Code 1, Upload Mode = ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r1 = r22;
        r40 = r0.append(r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = r40.toString();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5.<init>();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = "Sending image size array to DFU Packet: [";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = r5.append(r8);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = "b, ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = r5.append(r9);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = "b, ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = r5.append(r10);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = "b]";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = r5.toString();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r0.logi(r5);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = r44;
        r5.writeImageSize(r6, r7, r8, r9, r10);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40.<init>();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = "Firmware image size sent [";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r40 = r0.append(r8);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = "b, ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r40 = r0.append(r9);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = "b, ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r40 = r0.append(r10);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = "b]";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = r40.toString();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r34 = r44.readNotificationResponse();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 1;
        r0 = r44;
        r1 = r34;
        r37 = r0.getStatusCode(r1, r5);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40.<init>();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = "Responce received (Op Code: ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = 1;
        r41 = r34[r41];	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = " Status: ";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r40;
        r1 = r37;
        r40 = r0.append(r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = r40.toString();	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r5 = 1;
        r0 = r37;
        if (r0 == r5) goto L_0x095a;
    L_0x0722:
        r5 = new no.nordicsemi.android.dfu.exception.RemoteDfuException;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        r40 = "Starting DFU failed";
        r0 = r40;
        r1 = r37;
        r5.<init>(r0, r1);	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
        throw r5;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
    L_0x072e:
        r15 = move-exception;
        r5 = "Upload aborted";
        r0 = r44;
        r0.logi(r5);	 Catch:{ all -> 0x0771 }
        r5 = 15;
        r40 = "Upload aborted";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r5 = r0.mConnectionState;	 Catch:{ all -> 0x0771 }
        r40 = -3;
        r0 = r40;
        if (r5 != r0) goto L_0x0769;
    L_0x074b:
        r5 = 0;
        r0 = r44;
        r0.mAborted = r5;	 Catch:{ Exception -> 0x0c53 }
        r5 = "Sending Reset command (Op Code = 6)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ Exception -> 0x0c53 }
        r5 = OP_CODE_RESET;	 Catch:{ Exception -> 0x0c53 }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ Exception -> 0x0c53 }
        r5 = 10;
        r40 = "Reset request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ Exception -> 0x0c53 }
    L_0x0769:
        r5 = -7;
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ all -> 0x0771 }
        goto L_0x0599;
    L_0x0771:
        r5 = move-exception;
        r40 = "no.nordicsemi.android.dfu.PREFS_DFU_IN_PROGRESS";
        r41 = 0;
        r0 = r17;
        r1 = r40;
        r2 = r41;
        r0.putBoolean(r1, r2);	 Catch:{ IOException -> 0x0c4a }
        r17.commit();	 Catch:{ IOException -> 0x0c4a }
        r40 = 0;
        r0 = r40;
        r1 = r44;
        r1.mInputStream = r0;	 Catch:{ IOException -> 0x0c4a }
        if (r25 == 0) goto L_0x078f;
    L_0x078c:
        r25.close();	 Catch:{ IOException -> 0x0c4a }
    L_0x078f:
        r25 = 0;
    L_0x0791:
        throw r5;
    L_0x0792:
        throw r15;	 Catch:{ RemoteDfuException -> 0x0554, UnknownResponseException -> 0x0560 }
    L_0x0793:
        r15 = move-exception;
        r5 = 20;
        r40 = "Device has disconneted";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = r15.getMessage();	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r0.loge(r5);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r5 = r0.mNotificationsEnabled;	 Catch:{ all -> 0x0771 }
        if (r5 == 0) goto L_0x07b2;
    L_0x07ae:
        r5 = 0;
        r6.setCharacteristicNotification(r11, r5);	 Catch:{ all -> 0x0771 }
    L_0x07b2:
        r0 = r44;
        r0.close(r6);	 Catch:{ all -> 0x0771 }
        r5 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        r0 = r44;
        r0.updateProgressNotification(r5);	 Catch:{ all -> 0x0771 }
        goto L_0x0599;
    L_0x07c0:
        r5 = 4;
        r0 = r22;
        if (r0 != r5) goto L_0x0959;
    L_0x07c5:
        r5 = "DFU target does not support DFU v.2";
        r0 = r44;
        r0.logw(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 15;
        r40 = "DFU target does not support DFU v.2";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 1;
        r40 = "Switching to DFU v.1";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = "Resending Start DFU command (Op Code = 1)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = OP_CODE_START_DFU;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = "DFU Start sent (Op Code 1)";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Sending application image size to DFU Packet: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r24;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " bytes";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = r5.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r5 = r0.mImageSizeInBytes;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.writeImageSize(r6, r7, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = "Firmware image size sent (";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r1 = r24;
        r40 = r0.append(r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = " bytes)";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r34 = r44.readNotificationResponse();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 1;
        r0 = r44;
        r1 = r34;
        r37 = r0.getStatusCode(r1, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = "Responce received (Op Code: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = 1;
        r41 = r34[r41];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = " Status: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r1 = r37;
        r40 = r0.append(r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 1;
        r0 = r37;
        if (r0 == r5) goto L_0x095a;
    L_0x0893:
        r5 = new no.nordicsemi.android.dfu.exception.RemoteDfuException;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Starting DFU failed";
        r0 = r40;
        r1 = r37;
        r5.<init>(r0, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        throw r5;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x089f:
        r15 = move-exception;
        r5 = r15.getErrorNumber();	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r5 | 8192;
        r20 = r0;
        r5 = r15.getMessage();	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r0.loge(r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 20;
        r40 = "Remote DFU error: %s";
        r41 = 1;
        r0 = r41;
        r0 = new java.lang.Object[r0];	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r41 = r0;
        r42 = 0;
        r43 = no.nordicsemi.android.error.GattError.parse(r20);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r41[r42] = r43;	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r40 = java.lang.String.format(r40, r41);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = "Sending Reset command (Op Code = 6)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = OP_CODE_RESET;	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r5 = 10;
        r40 = "Reset request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        r0 = r44;
        r1 = r20;
        r0.terminateConnection(r6, r1);	 Catch:{ UploadAbortedException -> 0x072e, DeviceDisconnectedException -> 0x0793, DfuException -> 0x08f2 }
        goto L_0x0599;
    L_0x08f2:
        r15 = move-exception;
        r5 = r15.getErrorNumber();	 Catch:{ all -> 0x0771 }
        r0 = r5 & -16385;
        r20 = r0;
        r5 = 20;
        r40 = "Error (0x%02X): %s";
        r41 = 2;
        r0 = r41;
        r0 = new java.lang.Object[r0];	 Catch:{ all -> 0x0771 }
        r41 = r0;
        r42 = 0;
        r43 = java.lang.Integer.valueOf(r20);	 Catch:{ all -> 0x0771 }
        r41[r42] = r43;	 Catch:{ all -> 0x0771 }
        r42 = 1;
        r43 = no.nordicsemi.android.error.GattError.parse(r20);	 Catch:{ all -> 0x0771 }
        r41[r42] = r43;	 Catch:{ all -> 0x0771 }
        r40 = java.lang.String.format(r40, r41);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ all -> 0x0771 }
        r5 = r15.getMessage();	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r0.loge(r5);	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r5 = r0.mConnectionState;	 Catch:{ all -> 0x0771 }
        r40 = -3;
        r0 = r40;
        if (r5 != r0) goto L_0x094e;
    L_0x0935:
        r5 = "Sending Reset command (Op Code = 6)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ Exception -> 0x0c50 }
        r5 = OP_CODE_RESET;	 Catch:{ Exception -> 0x0c50 }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ Exception -> 0x0c50 }
        r5 = 10;
        r40 = "Reset request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ Exception -> 0x0c50 }
    L_0x094e:
        r5 = r15.getErrorNumber();	 Catch:{ all -> 0x0771 }
        r0 = r44;
        r0.terminateConnection(r6, r5);	 Catch:{ all -> 0x0771 }
        goto L_0x0599;
    L_0x0959:
        throw r16;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x095a:
        r0 = r44;
        r0 = r0.mPacketsBeforeNotification;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r31 = r0;
        if (r31 <= 0) goto L_0x09bc;
    L_0x0962:
        r5 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Sending the number of packets before notifications (Op Code = 8, value = ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r31;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = ")";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = r5.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = OP_CODE_PACKET_RECEIPT_NOTIF_REQ;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r31;
        r0.setNumberOfPackets(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = OP_CODE_PACKET_RECEIPT_NOTIF_REQ;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = "Packet Receipt Notif Req (Op Code 8) sent (value: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r1 = r31;
        r40 = r0.append(r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x09bc:
        r5 = "Sending Receive Firmware Image request (Op Code = 3)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = OP_CODE_RECEIVE_FIRMWARE_IMAGE;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = "Receive Firmware Image request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r35 = android.os.SystemClock.elapsedRealtime();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r35;
        r2 = r44;
        r2.mStartTime = r0;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r35;
        r2 = r44;
        r2.mLastProgressTime = r0;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r44.updateProgressNotification();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = "Starting upload...";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ DeviceDisconnectedException -> 0x0aef, UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f, UploadAbortedException -> 0x072e, DfuException -> 0x08f2 }
        r0 = r44;
        r1 = r25;
        r34 = r0.uploadFirmwareImage(r6, r7, r1);	 Catch:{ DeviceDisconnectedException -> 0x0aef, UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f, UploadAbortedException -> 0x072e, DfuException -> 0x08f2 }
        r18 = android.os.SystemClock.elapsedRealtime();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Transfer of ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0 = r0.mBytesSent;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r0;
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " bytes has taken ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r18 - r35;
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " ms";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = r5.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = "Upload completed in ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = r18 - r35;
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = " ms";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 3;
        r0 = r44;
        r1 = r34;
        r37 = r0.getStatusCode(r1, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = "Responce received (Op Code: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = 1;
        r41 = r34[r41];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = " Status: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r1 = r37;
        r40 = r0.append(r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Response received. Op Code: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 0;
        r40 = r34[r40];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " Req Op Code: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 1;
        r40 = r34[r40];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " status: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 2;
        r40 = r34[r40];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = r5.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 1;
        r0 = r37;
        if (r0 == r5) goto L_0x0af8;
    L_0x0ae3:
        r5 = new no.nordicsemi.android.dfu.exception.RemoteDfuException;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Device returned error after sending file";
        r0 = r40;
        r1 = r37;
        r5.<init>(r0, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        throw r5;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x0aef:
        r15 = move-exception;
        r5 = "Disconnected while sending data";
        r0 = r44;
        r0.loge(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        throw r15;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x0af8:
        r5 = "Sending Validate request (Op Code = 4)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = OP_CODE_VALIDATE;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = "Validate request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r34 = r44.readNotificationResponse();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 4;
        r0 = r44;
        r1 = r34;
        r37 = r0.getStatusCode(r1, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Response received. Op Code: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 0;
        r40 = r34[r40];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " Req Op Code: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 1;
        r40 = r34[r40];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = " status: ";
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = 2;
        r40 = r34[r40];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r5 = r5.append(r0);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = r5.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = new java.lang.StringBuilder;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = "Responce received (Op Code: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = 1;
        r41 = r34[r41];	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = " Status: ";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r40;
        r1 = r37;
        r40 = r0.append(r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r41 = ")";
        r40 = r40.append(r41);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r40.toString();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 1;
        r0 = r37;
        if (r0 == r5) goto L_0x0ba7;
    L_0x0b9b:
        r5 = new no.nordicsemi.android.dfu.exception.RemoteDfuException;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = "Device returned validation error";
        r0 = r40;
        r1 = r37;
        r5.<init>(r0, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        throw r5;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
    L_0x0ba7:
        r5 = -5;
        r0 = r44;
        r0.updateProgressNotification(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 0;
        r6.setCharacteristicNotification(r11, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 5;
        r40 = "Notifications disabled";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = "Sending Activate and Reset request (Op Code = 5)";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = OP_CODE_ACTIVATE_AND_RESET;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.writeOpCode(r6, r11, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 10;
        r40 = "Activate and Reset request sent";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r44.waitUntilDisconnected();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 5;
        r40 = "Disconnected by remote device";
        r0 = r44;
        r1 = r40;
        r0.sendLogBroadcast(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.refreshDeviceCache(r6);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0.close(r6);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r5 = r0.mPartCurrent;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r0 = r0.mPartsTotal;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r0;
        r0 = r40;
        if (r5 != r0) goto L_0x0c01;
    L_0x0bf9:
        r5 = -6;
        r0 = r44;
        r0.updateProgressNotification(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        goto L_0x0599;
    L_0x0c01:
        r5 = "Starting service that will upload application";
        r0 = r44;
        r0.logi(r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r29 = new android.content.Intent;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r29.<init>();	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = 24;
        r0 = r29;
        r1 = r45;
        r0.fillIn(r1, r5);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_TYPE";
        r40 = 4;
        r0 = r29;
        r1 = r40;
        r0.putExtra(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_PART_CURRENT";
        r0 = r44;
        r0 = r0.mPartCurrent;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r0;
        r40 = r40 + 1;
        r0 = r29;
        r1 = r40;
        r0.putExtra(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r5 = "no.nordicsemi.android.dfu.extra.EXTRA_PARTS_TOTAL";
        r0 = r44;
        r0 = r0.mPartsTotal;	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r40 = r0;
        r0 = r29;
        r1 = r40;
        r0.putExtra(r5, r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        r0 = r44;
        r1 = r29;
        r0.startService(r1);	 Catch:{ UnknownResponseException -> 0x0560, RemoteDfuException -> 0x089f }
        goto L_0x0599;
    L_0x0c4a:
        r40 = move-exception;
        goto L_0x0791;
    L_0x0c4d:
        r5 = move-exception;
        goto L_0x00cd;
    L_0x0c50:
        r5 = move-exception;
        goto L_0x094e;
    L_0x0c53:
        r5 = move-exception;
        goto L_0x0769;
    L_0x0c56:
        r5 = move-exception;
        goto L_0x0606;
    L_0x0c59:
        r5 = move-exception;
        goto L_0x00cd;
    L_0x0c5c:
        r5 = move-exception;
        goto L_0x00cd;
    L_0x0c5f:
        r5 = move-exception;
        goto L_0x00cd;
    L_0x0c62:
        r5 = move-exception;
        goto L_0x00cd;
    L_0x0c65:
        r5 = move-exception;
        goto L_0x00cd;
    L_0x0c68:
        r5 = move-exception;
        goto L_0x00cd;

        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.onHandleIntent(android.content.Intent):void");
    }*/

    private void setNumberOfPackets(byte[] data, int value) {
        data[TYPE_SOFT_DEVICE] = (byte) (value & MotionEventCompat.ACTION_MASK);
        data[TYPE_BOOTLOADER] = (byte) ((value >> OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY) & MotionEventCompat.ACTION_MASK);
    }

    private InputStream openInputStream(String filePath, String mimeType, int mbrSize, int types) throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(filePath);
        /*if (MIME_TYPE_ZIP.equals(mimeType)) {
            return new ZipHexInputStream(is, mbrSize, types);
        }*/
        return new HexInputStream(is, mbrSize);
    }

    private InputStream openInputStream(Uri stream, String mimeType, int mbrSize, int types) throws FileNotFoundException, IOException {
        InputStream is = getContentResolver().openInputStream(stream);
        /*if (MIME_TYPE_ZIP.equals(mimeType)) {
            return new ZipHexInputStream(is, mbrSize, types);
        }*/
        return new HexInputStream(is, mbrSize);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.bluetooth.BluetoothGatt connect(java.lang.String r8) {
        /*
        r7 = this;
        r6 = -1;
        r7.mConnectionState = r6;
        r3 = "Connecting to the device...";
        r7.logi(r3);
        r3 = r7.mBluetoothAdapter;
        r0 = r3.getRemoteDevice(r8);
        r3 = 0;
        r4 = r7.mGattCallback;
        r2 = r0.connectGatt(r7, r3, r4);
        r4 = r7.mLock;	 Catch:{ InterruptedException -> 0x0036 }
        monitor-enter(r4);	 Catch:{ InterruptedException -> 0x0036 }
    L_0x0018:
        r3 = r7.mConnectionState;	 Catch:{ all -> 0x0033 }
        if (r3 == r6) goto L_0x0021;
    L_0x001c:
        r3 = r7.mConnectionState;	 Catch:{ all -> 0x0033 }
        r5 = -2;
        if (r3 != r5) goto L_0x0029;
    L_0x0021:
        r3 = r7.mErrorState;	 Catch:{ all -> 0x0033 }
        if (r3 != 0) goto L_0x0029;
    L_0x0025:
        r3 = r7.mAborted;	 Catch:{ all -> 0x0033 }
        if (r3 == 0) goto L_0x002d;
    L_0x0029:
        r3 = r7.mPaused;	 Catch:{ all -> 0x0033 }
        if (r3 == 0) goto L_0x003d;
    L_0x002d:
        r3 = r7.mLock;	 Catch:{ all -> 0x0033 }
        r3.wait();	 Catch:{ all -> 0x0033 }
        goto L_0x0018;
    L_0x0033:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0033 }
        throw r3;	 Catch:{ InterruptedException -> 0x0036 }
    L_0x0036:
        r1 = move-exception;
        r3 = "Sleeping interrupted";
        r7.loge(r3, r1);
    L_0x003c:
        return r2;
    L_0x003d:
        monitor-exit(r4);	 Catch:{ all -> 0x0033 }
        goto L_0x003c;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.connect(java.lang.String):android.bluetooth.BluetoothGatt");
    }

    private void terminateConnection(BluetoothGatt gatt, int error) {
        if (this.mConnectionState != 0) {
            updateProgressNotification(STATE_CLOSED);
            try {
                BluetoothGattService dfuService = gatt.getService(DFU_SERVICE_UUID);
                if (dfuService != null) {
                    setCharacteristicNotification(gatt, dfuService.getCharacteristic(DFU_CONTROL_POINT_UUID), false);
                    sendLogBroadcast(OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY, "Notifications disabled");
                }
            } catch (DeviceDisconnectedException e) {
            } catch (DfuException e2) {
            } catch (Exception e3) {
            }
            disconnect(gatt);
            sendLogBroadcast(OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY, "Disconnected");
        }
        refreshDeviceCache(gatt);
        close(gatt);
        updateProgressNotification(error);
    }

    private void disconnect(BluetoothGatt gatt) {
        if (this.mConnectionState != 0) {
            this.mConnectionState = STATE_DISCONNECTING;
            logi("Disconnecting from the device...");
            gatt.disconnect();
            waitUntilDisconnected();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void waitUntilDisconnected() {
        /*
        r3 = this;
        r2 = r3.mLock;	 Catch:{ InterruptedException -> 0x0014 }
        monitor-enter(r2);	 Catch:{ InterruptedException -> 0x0014 }
    L_0x0003:
        r1 = r3.mConnectionState;	 Catch:{ all -> 0x0011 }
        if (r1 == 0) goto L_0x001b;
    L_0x0007:
        r1 = r3.mErrorState;	 Catch:{ all -> 0x0011 }
        if (r1 != 0) goto L_0x001b;
    L_0x000b:
        r1 = r3.mLock;	 Catch:{ all -> 0x0011 }
        r1.wait();	 Catch:{ all -> 0x0011 }
        goto L_0x0003;
    L_0x0011:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0011 }
        throw r1;	 Catch:{ InterruptedException -> 0x0014 }
    L_0x0014:
        r0 = move-exception;
        r1 = "Sleeping interrupted";
        r3.loge(r1, r0);
    L_0x001a:
        return;
    L_0x001b:
        monitor-exit(r2);	 Catch:{ all -> 0x0011 }
        goto L_0x001a;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.waitUntilDisconnected():void");
    }

    private void close(BluetoothGatt gatt) {
        logi("Cleaning up...");
        gatt.close();
        this.mConnectionState = STATE_CLOSED;
    }

    private void refreshDeviceCache(BluetoothGatt gatt) {
        try {
            Method refresh = gatt.getClass().getMethod("refresh", new Class[TYPE_AUTO]);
            if (refresh != null) {
                logi("Refreshing result: " + ((Boolean) refresh.invoke(gatt, new Object[TYPE_AUTO])).booleanValue());
            }
        } catch (Exception e) {
            loge("An exception occured while refreshing device", e);
        }
    }

    private int getStatusCode(byte[] response, int request) throws UnknownResponseException {
        if (response != null && response.length == OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY && response[TYPE_AUTO] == OP_CODE_RESPONSE_CODE_KEY && response[TYPE_SOFT_DEVICE] == request && response[TYPE_BOOTLOADER] >= (byte) 1 && response[TYPE_BOOTLOADER] <= OP_CODE_RECEIVE_RESET_KEY) {
            return response[TYPE_BOOTLOADER];
        }
        throw new UnknownResponseException("Invalid response received", response, request);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setCharacteristicNotification(android.bluetooth.BluetoothGatt r8, android.bluetooth.BluetoothGattCharacteristic r9, boolean r10) throws no.nordicsemi.android.dfu.exception.DeviceDisconnectedException, no.nordicsemi.android.dfu.exception.DfuException, no.nordicsemi.android.dfu.exception.UploadAbortedException {
        /*
        r7 = this;
        r6 = 1;
        r5 = 0;
        r4 = -3;
        r2 = r7.mConnectionState;
        if (r2 == r4) goto L_0x0011;
    L_0x0007:
        r2 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r3 = "Unable to set notifications state";
        r4 = r7.mConnectionState;
        r2.<init>(r3, r4);
        throw r2;
    L_0x0011:
        r7.mErrorState = r5;
        r2 = r7.mNotificationsEnabled;
        if (r2 != r10) goto L_0x0018;
    L_0x0017:
        return;
    L_0x0018:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        if (r10 == 0) goto L_0x00b3;
    L_0x001f:
        r2 = "Enabling ";
    L_0x0021:
        r2 = r3.append(r2);
        r3 = " notifications...";
        r2 = r2.append(r3);
        r2 = r2.toString();
        r7.logi(r2);
        if (r10 == 0) goto L_0x00b7;
    L_0x0034:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Enabling notifications for ";
        r2 = r2.append(r3);
        r3 = r9.getUuid();
        r2 = r2.append(r3);
        r2 = r2.toString();
        r7.sendLogBroadcast(r6, r2);
    L_0x004e:
        r8.setCharacteristicNotification(r9, r10);
        r2 = CLIENT_CHARACTERISTIC_CONFIG;
        r0 = r9.getDescriptor(r2);
        if (r10 == 0) goto L_0x00d3;
    L_0x0059:
        r2 = android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    L_0x005b:
        r0.setValue(r2);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "gatt.writeDescriptor(";
        r2 = r2.append(r3);
        r3 = r0.getUuid();
        r3 = r2.append(r3);
        if (r10 == 0) goto L_0x00d6;
    L_0x0073:
        r2 = ", value=0x01-00)";
    L_0x0075:
        r2 = r3.append(r2);
        r2 = r2.toString();
        r7.sendLogBroadcast(r5, r2);
        r8.writeDescriptor(r0);
        r3 = r7.mLock;	 Catch:{ InterruptedException -> 0x00a3 }
        monitor-enter(r3);	 Catch:{ InterruptedException -> 0x00a3 }
    L_0x0086:
        r2 = r7.mNotificationsEnabled;	 Catch:{ all -> 0x00a0 }
        if (r2 == r10) goto L_0x0096;
    L_0x008a:
        r2 = r7.mConnectionState;	 Catch:{ all -> 0x00a0 }
        if (r2 != r4) goto L_0x0096;
    L_0x008e:
        r2 = r7.mErrorState;	 Catch:{ all -> 0x00a0 }
        if (r2 != 0) goto L_0x0096;
    L_0x0092:
        r2 = r7.mAborted;	 Catch:{ all -> 0x00a0 }
        if (r2 == 0) goto L_0x009a;
    L_0x0096:
        r2 = r7.mPaused;	 Catch:{ all -> 0x00a0 }
        if (r2 == 0) goto L_0x00d9;
    L_0x009a:
        r2 = r7.mLock;	 Catch:{ all -> 0x00a0 }
        r2.wait();	 Catch:{ all -> 0x00a0 }
        goto L_0x0086;
    L_0x00a0:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x00a0 }
        throw r2;	 Catch:{ InterruptedException -> 0x00a3 }
    L_0x00a3:
        r1 = move-exception;
        r2 = "Sleeping interrupted";
        r7.loge(r2, r1);
    L_0x00a9:
        r2 = r7.mAborted;
        if (r2 == 0) goto L_0x00db;
    L_0x00ad:
        r2 = new no.nordicsemi.android.dfu.exception.UploadAbortedException;
        r2.<init>();
        throw r2;
    L_0x00b3:
        r2 = "Disabling";
        goto L_0x0021;
    L_0x00b7:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Disabling notifications for ";
        r2 = r2.append(r3);
        r3 = r9.getUuid();
        r2 = r2.append(r3);
        r2 = r2.toString();
        r7.sendLogBroadcast(r6, r2);
        goto L_0x004e;
    L_0x00d3:
        r2 = android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        goto L_0x005b;
    L_0x00d6:
        r2 = ", value=0x00-00)";
        goto L_0x0075;
    L_0x00d9:
        monitor-exit(r3);	 Catch:{ all -> 0x00a0 }
        goto L_0x00a9;
    L_0x00db:
        r2 = r7.mErrorState;
        if (r2 == 0) goto L_0x00e9;
    L_0x00df:
        r2 = new no.nordicsemi.android.dfu.exception.DfuException;
        r3 = "Unable to set notifications state";
        r4 = r7.mErrorState;
        r2.<init>(r3, r4);
        throw r2;
    L_0x00e9:
        r2 = r7.mConnectionState;
        if (r2 == r4) goto L_0x0017;
    L_0x00ed:
        r2 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r3 = "Unable to set notifications state";
        r4 = r7.mConnectionState;
        r2.<init>(r3, r4);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.setCharacteristicNotification(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, boolean):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeOpCode(android.bluetooth.BluetoothGatt r7, android.bluetooth.BluetoothGattCharacteristic r8, byte[] r9) throws no.nordicsemi.android.dfu.exception.DeviceDisconnectedException, no.nordicsemi.android.dfu.exception.DfuException, no.nordicsemi.android.dfu.exception.UploadAbortedException {
        /*
        r6 = this;
        r3 = 1;
        r5 = -3;
        r2 = 0;
        r1 = 0;
        r6.mReceivedData = r1;
        r6.mErrorState = r2;
        r6.mRequestCompleted = r2;
        r1 = r9[r2];
        r4 = 6;
        if (r1 == r4) goto L_0x0014;
    L_0x000f:
        r1 = r9[r2];
        r4 = 5;
        if (r1 != r4) goto L_0x0087;
    L_0x0014:
        r1 = r3;
    L_0x0015:
        r6.mResetRequestSent = r1;
        r8.setValue(r9);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r4 = "Writing to characteristic ";
        r1 = r1.append(r4);
        r4 = r8.getUuid();
        r1 = r1.append(r4);
        r1 = r1.toString();
        r6.sendLogBroadcast(r3, r1);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r3 = "gatt.writeCharacteristic(";
        r1 = r1.append(r3);
        r3 = r8.getUuid();
        r1 = r1.append(r3);
        r3 = ")";
        r1 = r1.append(r3);
        r1 = r1.toString();
        r6.sendLogBroadcast(r2, r1);
        r7.writeCharacteristic(r8);
        r3 = r6.mLock;	 Catch:{ InterruptedException -> 0x0077 }
        monitor-enter(r3);	 Catch:{ InterruptedException -> 0x0077 }
    L_0x005a:
        r1 = r6.mRequestCompleted;	 Catch:{ all -> 0x0074 }
        if (r1 != 0) goto L_0x006a;
    L_0x005e:
        r1 = r6.mConnectionState;	 Catch:{ all -> 0x0074 }
        if (r1 != r5) goto L_0x006a;
    L_0x0062:
        r1 = r6.mErrorState;	 Catch:{ all -> 0x0074 }
        if (r1 != 0) goto L_0x006a;
    L_0x0066:
        r1 = r6.mAborted;	 Catch:{ all -> 0x0074 }
        if (r1 == 0) goto L_0x006e;
    L_0x006a:
        r1 = r6.mPaused;	 Catch:{ all -> 0x0074 }
        if (r1 == 0) goto L_0x0089;
    L_0x006e:
        r1 = r6.mLock;	 Catch:{ all -> 0x0074 }
        r1.wait();	 Catch:{ all -> 0x0074 }
        goto L_0x005a;
    L_0x0074:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0074 }
        throw r1;	 Catch:{ InterruptedException -> 0x0077 }
    L_0x0077:
        r0 = move-exception;
        r1 = "Sleeping interrupted";
        r6.loge(r1, r0);
    L_0x007d:
        r1 = r6.mAborted;
        if (r1 == 0) goto L_0x008b;
    L_0x0081:
        r1 = new no.nordicsemi.android.dfu.exception.UploadAbortedException;
        r1.<init>();
        throw r1;
    L_0x0087:
        r1 = r2;
        goto L_0x0015;
    L_0x0089:
        monitor-exit(r3);	 Catch:{ all -> 0x0074 }
        goto L_0x007d;
    L_0x008b:
        r1 = r6.mErrorState;
        if (r1 == 0) goto L_0x00ac;
    L_0x008f:
        r1 = new no.nordicsemi.android.dfu.exception.DfuException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Unable to write Op Code ";
        r3 = r3.append(r4);
        r2 = r9[r2];
        r2 = r3.append(r2);
        r2 = r2.toString();
        r3 = r6.mErrorState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x00ac:
        r1 = r6.mConnectionState;
        if (r1 == r5) goto L_0x00cd;
    L_0x00b0:
        r1 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Unable to write Op Code ";
        r3 = r3.append(r4);
        r2 = r9[r2];
        r2 = r3.append(r2);
        r2 = r2.toString();
        r3 = r6.mConnectionState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x00cd:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.writeOpCode(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, byte[]):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeImageSize(android.bluetooth.BluetoothGatt r7, android.bluetooth.BluetoothGattCharacteristic r8, int r9) throws no.nordicsemi.android.dfu.exception.DeviceDisconnectedException, no.nordicsemi.android.dfu.exception.DfuException, no.nordicsemi.android.dfu.exception.UploadAbortedException {
        /*
        r6 = this;
        r5 = 1;
        r4 = -3;
        r3 = 0;
        r1 = 0;
        r6.mReceivedData = r1;
        r6.mErrorState = r3;
        r6.mImageSizeSent = r3;
        r8.setWriteType(r5);
        r1 = 4;
        r1 = new byte[r1];
        r8.setValue(r1);
        r1 = 20;
        r8.setValue(r9, r1, r3);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Writing to characteristic ";
        r1 = r1.append(r2);
        r2 = r8.getUuid();
        r1 = r1.append(r2);
        r1 = r1.toString();
        r6.sendLogBroadcast(r5, r1);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "gatt.writeCharacteristic(";
        r1 = r1.append(r2);
        r2 = r8.getUuid();
        r1 = r1.append(r2);
        r2 = ")";
        r1 = r1.append(r2);
        r1 = r1.toString();
        r6.sendLogBroadcast(r3, r1);
        r7.writeCharacteristic(r8);
        r2 = r6.mLock;	 Catch:{ InterruptedException -> 0x0075 }
        monitor-enter(r2);	 Catch:{ InterruptedException -> 0x0075 }
    L_0x0058:
        r1 = r6.mImageSizeSent;	 Catch:{ all -> 0x0072 }
        if (r1 != 0) goto L_0x0068;
    L_0x005c:
        r1 = r6.mConnectionState;	 Catch:{ all -> 0x0072 }
        if (r1 != r4) goto L_0x0068;
    L_0x0060:
        r1 = r6.mErrorState;	 Catch:{ all -> 0x0072 }
        if (r1 != 0) goto L_0x0068;
    L_0x0064:
        r1 = r6.mAborted;	 Catch:{ all -> 0x0072 }
        if (r1 == 0) goto L_0x006c;
    L_0x0068:
        r1 = r6.mPaused;	 Catch:{ all -> 0x0072 }
        if (r1 == 0) goto L_0x0085;
    L_0x006c:
        r1 = r6.mLock;	 Catch:{ all -> 0x0072 }
        r1.wait();	 Catch:{ all -> 0x0072 }
        goto L_0x0058;
    L_0x0072:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0072 }
        throw r1;	 Catch:{ InterruptedException -> 0x0075 }
    L_0x0075:
        r0 = move-exception;
        r1 = "Sleeping interrupted";
        r6.loge(r1, r0);
    L_0x007b:
        r1 = r6.mAborted;
        if (r1 == 0) goto L_0x0087;
    L_0x007f:
        r1 = new no.nordicsemi.android.dfu.exception.UploadAbortedException;
        r1.<init>();
        throw r1;
    L_0x0085:
        monitor-exit(r2);	 Catch:{ all -> 0x0072 }
        goto L_0x007b;
    L_0x0087:
        r1 = r6.mErrorState;
        if (r1 == 0) goto L_0x0095;
    L_0x008b:
        r1 = new no.nordicsemi.android.dfu.exception.DfuException;
        r2 = "Unable to write Image Size";
        r3 = r6.mErrorState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x0095:
        r1 = r6.mConnectionState;
        if (r1 == r4) goto L_0x00a3;
    L_0x0099:
        r1 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r2 = "Unable to write Image Size";
        r3 = r6.mConnectionState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x00a3:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.writeImageSize(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeImageSize(android.bluetooth.BluetoothGatt r7, android.bluetooth.BluetoothGattCharacteristic r8, int r9, int r10, int r11) throws no.nordicsemi.android.dfu.exception.DeviceDisconnectedException, no.nordicsemi.android.dfu.exception.DfuException, no.nordicsemi.android.dfu.exception.UploadAbortedException {
        /*
        r6 = this;
        r5 = 1;
        r4 = -3;
        r2 = 20;
        r3 = 0;
        r1 = 0;
        r6.mReceivedData = r1;
        r6.mErrorState = r3;
        r6.mImageSizeSent = r3;
        r8.setWriteType(r5);
        r1 = 12;
        r1 = new byte[r1];
        r8.setValue(r1);
        r8.setValue(r9, r2, r3);
        r1 = 4;
        r8.setValue(r10, r2, r1);
        r1 = 8;
        r8.setValue(r11, r2, r1);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Writing to characteristic ";
        r1 = r1.append(r2);
        r2 = r8.getUuid();
        r1 = r1.append(r2);
        r1 = r1.toString();
        r6.sendLogBroadcast(r5, r1);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "gatt.writeCharacteristic(";
        r1 = r1.append(r2);
        r2 = r8.getUuid();
        r1 = r1.append(r2);
        r2 = ")";
        r1 = r1.append(r2);
        r1 = r1.toString();
        r6.sendLogBroadcast(r3, r1);
        r7.writeCharacteristic(r8);
        r2 = r6.mLock;	 Catch:{ InterruptedException -> 0x007f }
        monitor-enter(r2);	 Catch:{ InterruptedException -> 0x007f }
    L_0x0062:
        r1 = r6.mImageSizeSent;	 Catch:{ all -> 0x007c }
        if (r1 != 0) goto L_0x0072;
    L_0x0066:
        r1 = r6.mConnectionState;	 Catch:{ all -> 0x007c }
        if (r1 != r4) goto L_0x0072;
    L_0x006a:
        r1 = r6.mErrorState;	 Catch:{ all -> 0x007c }
        if (r1 != 0) goto L_0x0072;
    L_0x006e:
        r1 = r6.mAborted;	 Catch:{ all -> 0x007c }
        if (r1 == 0) goto L_0x0076;
    L_0x0072:
        r1 = r6.mPaused;	 Catch:{ all -> 0x007c }
        if (r1 == 0) goto L_0x008f;
    L_0x0076:
        r1 = r6.mLock;	 Catch:{ all -> 0x007c }
        r1.wait();	 Catch:{ all -> 0x007c }
        goto L_0x0062;
    L_0x007c:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x007c }
        throw r1;	 Catch:{ InterruptedException -> 0x007f }
    L_0x007f:
        r0 = move-exception;
        r1 = "Sleeping interrupted";
        r6.loge(r1, r0);
    L_0x0085:
        r1 = r6.mAborted;
        if (r1 == 0) goto L_0x0091;
    L_0x0089:
        r1 = new no.nordicsemi.android.dfu.exception.UploadAbortedException;
        r1.<init>();
        throw r1;
    L_0x008f:
        monitor-exit(r2);	 Catch:{ all -> 0x007c }
        goto L_0x0085;
    L_0x0091:
        r1 = r6.mErrorState;
        if (r1 == 0) goto L_0x009f;
    L_0x0095:
        r1 = new no.nordicsemi.android.dfu.exception.DfuException;
        r2 = "Unable to write Image Sizes";
        r3 = r6.mErrorState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x009f:
        r1 = r6.mConnectionState;
        if (r1 == r4) goto L_0x00ad;
    L_0x00a3:
        r1 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r2 = "Unable to write Image Sizes";
        r3 = r6.mConnectionState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x00ad:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.writeImageSize(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int, int, int):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] uploadFirmwareImage(android.bluetooth.BluetoothGatt r8, android.bluetooth.BluetoothGattCharacteristic r9, java.io.InputStream r10) throws no.nordicsemi.android.dfu.exception.DeviceDisconnectedException, no.nordicsemi.android.dfu.exception.DfuException, no.nordicsemi.android.dfu.exception.UploadAbortedException {
        /*
        r7 = this;
        r6 = -3;
        r3 = 0;
        r7.mReceivedData = r3;
        r3 = 0;
        r7.mErrorState = r3;
        r0 = r7.mBuffer;
        r2 = r10.read(r0);	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r3 = 1;
        r4 = new java.lang.StringBuilder;	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r4.<init>();	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r5 = "Sending firmware to characteristic ";
        r4 = r4.append(r5);	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r5 = r9.getUuid();	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r4 = r4.append(r5);	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r5 = "...";
        r4 = r4.append(r5);	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r4 = r4.toString();	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r7.sendLogBroadcast(r3, r4);	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r7.writePacket(r8, r9, r0, r2);	 Catch:{ HexFileValidationException -> 0x0061, IOException -> 0x006c }
        r4 = r7.mLock;	 Catch:{ InterruptedException -> 0x0051 }
        monitor-enter(r4);	 Catch:{ InterruptedException -> 0x0051 }
    L_0x0034:
        r3 = r7.mReceivedData;	 Catch:{ all -> 0x004e }
        if (r3 != 0) goto L_0x0044;
    L_0x0038:
        r3 = r7.mConnectionState;	 Catch:{ all -> 0x004e }
        if (r3 != r6) goto L_0x0044;
    L_0x003c:
        r3 = r7.mErrorState;	 Catch:{ all -> 0x004e }
        if (r3 != 0) goto L_0x0044;
    L_0x0040:
        r3 = r7.mAborted;	 Catch:{ all -> 0x004e }
        if (r3 == 0) goto L_0x0048;
    L_0x0044:
        r3 = r7.mPaused;	 Catch:{ all -> 0x004e }
        if (r3 == 0) goto L_0x0077;
    L_0x0048:
        r3 = r7.mLock;	 Catch:{ all -> 0x004e }
        r3.wait();	 Catch:{ all -> 0x004e }
        goto L_0x0034;
    L_0x004e:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x004e }
        throw r3;	 Catch:{ InterruptedException -> 0x0051 }
    L_0x0051:
        r1 = move-exception;
        r3 = "Sleeping interrupted";
        r7.loge(r3, r1);
    L_0x0057:
        r3 = r7.mAborted;
        if (r3 == 0) goto L_0x0079;
    L_0x005b:
        r3 = new no.nordicsemi.android.dfu.exception.UploadAbortedException;
        r3.<init>();
        throw r3;
    L_0x0061:
        r1 = move-exception;
        r3 = new no.nordicsemi.android.dfu.exception.DfuException;
        r4 = "HEX file not valid";
        r5 = 4099; // 0x1003 float:5.744E-42 double:2.025E-320;
        r3.<init>(r4, r5);
        throw r3;
    L_0x006c:
        r1 = move-exception;
        r3 = new no.nordicsemi.android.dfu.exception.DfuException;
        r4 = "Error while reading file";
        r5 = 4100; // 0x1004 float:5.745E-42 double:2.0257E-320;
        r3.<init>(r4, r5);
        throw r3;
    L_0x0077:
        monitor-exit(r4);	 Catch:{ all -> 0x004e }
        goto L_0x0057;
    L_0x0079:
        r3 = r7.mErrorState;
        if (r3 == 0) goto L_0x0087;
    L_0x007d:
        r3 = new no.nordicsemi.android.dfu.exception.DfuException;
        r4 = "Uploading Fimrware Image failed";
        r5 = r7.mErrorState;
        r3.<init>(r4, r5);
        throw r3;
    L_0x0087:
        r3 = r7.mConnectionState;
        if (r3 == r6) goto L_0x0095;
    L_0x008b:
        r3 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r4 = "Uploading Fimrware Image failed: device disconnected";
        r5 = r7.mConnectionState;
        r3.<init>(r4, r5);
        throw r3;
    L_0x0095:
        r3 = r7.mReceivedData;
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.uploadFirmwareImage(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, java.io.InputStream):byte[]");
    }

    private void writePacket(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] buffer, int size) {
        byte[] locBuffer = buffer;
        if (buffer.length != size) {
            locBuffer = new byte[size];
            System.arraycopy(buffer, TYPE_AUTO, locBuffer, TYPE_AUTO, size);
        }
        characteristic.setValue(locBuffer);
        gatt.writeCharacteristic(characteristic);
    }

    private void waitIfPaused() {
        synchronized (this.mLock) {
            while (this.mPaused) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    loge("Sleeping interrupted", e);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readNotificationResponse() throws no.nordicsemi.android.dfu.exception.DeviceDisconnectedException, no.nordicsemi.android.dfu.exception.DfuException, no.nordicsemi.android.dfu.exception.UploadAbortedException {
        /*
        r4 = this;
        r3 = -3;
        r1 = 0;
        r4.mErrorState = r1;
        r2 = r4.mLock;	 Catch:{ InterruptedException -> 0x0024 }
        monitor-enter(r2);	 Catch:{ InterruptedException -> 0x0024 }
    L_0x0007:
        r1 = r4.mReceivedData;	 Catch:{ all -> 0x0021 }
        if (r1 != 0) goto L_0x0017;
    L_0x000b:
        r1 = r4.mConnectionState;	 Catch:{ all -> 0x0021 }
        if (r1 != r3) goto L_0x0017;
    L_0x000f:
        r1 = r4.mErrorState;	 Catch:{ all -> 0x0021 }
        if (r1 != 0) goto L_0x0017;
    L_0x0013:
        r1 = r4.mAborted;	 Catch:{ all -> 0x0021 }
        if (r1 == 0) goto L_0x001b;
    L_0x0017:
        r1 = r4.mPaused;	 Catch:{ all -> 0x0021 }
        if (r1 == 0) goto L_0x0034;
    L_0x001b:
        r1 = r4.mLock;	 Catch:{ all -> 0x0021 }
        r1.wait();	 Catch:{ all -> 0x0021 }
        goto L_0x0007;
    L_0x0021:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0021 }
        throw r1;	 Catch:{ InterruptedException -> 0x0024 }
    L_0x0024:
        r0 = move-exception;
        r1 = "Sleeping interrupted";
        r4.loge(r1, r0);
    L_0x002a:
        r1 = r4.mAborted;
        if (r1 == 0) goto L_0x0036;
    L_0x002e:
        r1 = new no.nordicsemi.android.dfu.exception.UploadAbortedException;
        r1.<init>();
        throw r1;
    L_0x0034:
        monitor-exit(r2);	 Catch:{ all -> 0x0021 }
        goto L_0x002a;
    L_0x0036:
        r1 = r4.mErrorState;
        if (r1 == 0) goto L_0x0044;
    L_0x003a:
        r1 = new no.nordicsemi.android.dfu.exception.DfuException;
        r2 = "Unable to write Op Code";
        r3 = r4.mErrorState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x0044:
        r1 = r4.mConnectionState;
        if (r1 == r3) goto L_0x0052;
    L_0x0048:
        r1 = new no.nordicsemi.android.dfu.exception.DeviceDisconnectedException;
        r2 = "Unable to write Op Code";
        r3 = r4.mConnectionState;
        r1.<init>(r2, r3);
        throw r1;
    L_0x0052:
        r1 = r4.mReceivedData;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.DfuBaseService.readNotificationResponse():byte[]");
    }

    private void updateProgressNotification() {
        int progress = (int) ((100.0f * ((float) this.mBytesSent)) / ((float) this.mImageSizeInBytes));
        if (this.mLastProgress != progress) {
            this.mLastProgress = progress;
            updateProgressNotification(progress);
        }
    }

    private void updateProgressNotification(int progress) {
        String deviceAddress = this.mDeviceAddress;
        String deviceName = this.mDeviceName != null ? this.mDeviceName : getString(Integer.valueOf(C0060R.string.dfu_unknown_name));
        Builder builder = new Builder(this).setSmallIcon(Integer.valueOf(17301640)).setOnlyAlertOnce(true).setLargeIcon(BitmapFactory.decodeResource(getResources(), C0060R.drawable.ic_stat_notify_dfu));
        Builder contentTitle;
        int i;
        Object[] objArr;
        switch (progress) {
            case PROGRESS_ABORTED /*-7*/:
                builder.setOngoing(false).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_aborted))).setContentText(getString(Integer.valueOf(C0060R.string.dfu_status_aborted_msg))).setAutoCancel(true);
                break;
            case PROGRESS_COMPLETED /*-6*/:
                builder.setOngoing(false).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_completed))).setContentText(getString(Integer.valueOf(C0060R.string.dfu_status_completed_msg))).setAutoCancel(true);
                break;
            case STATE_CLOSED /*-5*/:
                contentTitle = builder.setOngoing(true).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_disconnecting)));
                i = C0060R.string.dfu_status_disconnecting_msg;
                objArr = new Object[TYPE_SOFT_DEVICE];
                objArr[TYPE_AUTO] = deviceName;
                contentTitle.setContentText(getString(i, objArr)).setProgress(100, TYPE_AUTO, true);
                break;
            case STATE_DISCONNECTING /*-4*/:
                contentTitle = builder.setOngoing(true).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_validating)));
                i = C0060R.string.dfu_status_validating_msg;
                objArr = new Object[TYPE_SOFT_DEVICE];
                objArr[TYPE_AUTO] = deviceName;
                contentTitle.setContentText(getString(i, objArr)).setProgress(100, TYPE_AUTO, true);
                break;
            case STATE_CONNECTED /*-2*/:
                contentTitle = builder.setOngoing(true).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_starting)));
                i = C0060R.string.dfu_status_starting_msg;
                objArr = new Object[TYPE_SOFT_DEVICE];
                objArr[TYPE_AUTO] = deviceName;
                contentTitle.setContentText(getString(i, objArr)).setProgress(100, TYPE_AUTO, true);
                break;
            case STATE_CONNECTING /*-1*/:
                contentTitle = builder.setOngoing(true).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_connecting)));
                i = C0060R.string.dfu_status_connecting_msg;
                objArr = new Object[TYPE_SOFT_DEVICE];
                objArr[TYPE_AUTO] = deviceName;
                contentTitle.setContentText(getString(i, objArr)).setProgress(100, TYPE_AUTO, true);
                break;
            default:
                if (progress < ERROR_MASK) {
                    String title;
                    int i2;
                    Object[] objArr2;
                    String text;
                    if (this.mPartsTotal == TYPE_SOFT_DEVICE) {
                        title = getString(Integer.valueOf(C0060R.string.dfu_status_uploading));
                    } else {
                        i2 = C0060R.string.dfu_status_uploading_part;
                        objArr2 = new Object[TYPE_BOOTLOADER];
                        objArr2[TYPE_AUTO] = Integer.valueOf(this.mPartCurrent);
                        objArr2[TYPE_SOFT_DEVICE] = Integer.valueOf(this.mPartsTotal);
                        title = getString(i2, objArr2);
                    }
                    if ((this.mFileType & TYPE_APPLICATION) > 0) {
                        i2 = C0060R.string.dfu_status_uploading_msg;
                        objArr2 = new Object[TYPE_SOFT_DEVICE];
                        objArr2[TYPE_AUTO] = deviceName;
                        text = getString(i2, objArr2);
                    } else {
                        i2 = C0060R.string.dfu_status_uploading_components_msg;
                        objArr2 = new Object[TYPE_SOFT_DEVICE];
                        objArr2[TYPE_AUTO] = deviceName;
                        text = getString(i2, objArr2);
                    }
                    builder.setOngoing(true).setContentTitle(title).setContentText(text).setProgress(100, progress, false);
                    break;
                }
                builder.setOngoing(false).setContentTitle(getString(Integer.valueOf(C0060R.string.dfu_status_error))).setContentText(getString(Integer.valueOf(C0060R.string.dfu_status_error_msg))).setAutoCancel(true);
                break;
        }
        if (progress < ERROR_MASK) {
            sendProgressBroadcast(progress);
        } else {
            sendErrorBroadcast(progress);
        }
        Intent intent = new Intent(this, getNotificationTarget());
        intent.addFlags(268435456);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(EXTRA_DEVICE_NAME, deviceName);
        intent.putExtra(EXTRA_PROGRESS, progress);
        if (this.mLogSession != null) {
            intent.putExtra(EXTRA_LOG_URI, this.mLogSession.getSessionUri());
        }
        builder.setContentIntent(PendingIntent.getActivity(this, TYPE_AUTO, intent,PendingIntent.FLAG_ONE_SHOT));
        if (!(progress == PROGRESS_ABORTED || progress == PROGRESS_COMPLETED)) {
            Intent abortIntent = new Intent(BROADCAST_ACTION);
            abortIntent.putExtra(EXTRA_ACTION, TYPE_BOOTLOADER);
            builder.addAction(C0060R.drawable.ic_action_notify_cancel, getString(Integer.valueOf(C0060R.string.dfu_action_abort)), PendingIntent.getBroadcast(this, TYPE_SOFT_DEVICE, abortIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        }
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void sendProgressBroadcast(int progress) {
        long now = SystemClock.elapsedRealtime();
        float speed = ((float) (this.mBytesSent - this.mLastBytesSent)) / ((float) (now - this.mLastProgressTime));
        float avgSpeed = ((float) this.mBytesSent) / ((float) (now - this.mStartTime));
        this.mLastProgressTime = now;
        this.mLastBytesSent = this.mBytesSent;
        Intent broadcast = new Intent(BROADCAST_PROGRESS);
        broadcast.putExtra(EXTRA_DATA, progress);
        broadcast.putExtra(EXTRA_DEVICE_ADDRESS, this.mDeviceAddress);
        broadcast.putExtra(EXTRA_PART_CURRENT, this.mPartCurrent);
        broadcast.putExtra(EXTRA_PARTS_TOTAL, this.mPartsTotal);
        broadcast.putExtra(EXTRA_SPEED_B_PER_MS, speed);
        broadcast.putExtra(EXTRA_AVG_SPEED_B_PER_MS, avgSpeed);
        if (this.mLogSession != null) {
            broadcast.putExtra(EXTRA_LOG_URI, this.mLogSession.getSessionUri());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void sendErrorBroadcast(int error) {
        Intent broadcast = new Intent(BROADCAST_ERROR);
        broadcast.putExtra(EXTRA_DATA, error & -16385);
        broadcast.putExtra(EXTRA_DEVICE_ADDRESS, this.mDeviceAddress);
        if (this.mLogSession != null) {
            broadcast.putExtra(EXTRA_LOG_URI, this.mLogSession.getSessionUri());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void sendLogBroadcast(int level, String message) {
        ILogSession session = this.mLogSession;
        String fullMessage = "[DFU] " + message;
        if (session == null) {
            Intent broadcast = new Intent(BROADCAST_LOG);
            broadcast.putExtra(EXTRA_LOG_MESSAGE, fullMessage);
            broadcast.putExtra(EXTRA_LOG_LEVEL, level);
            broadcast.putExtra(EXTRA_DEVICE_ADDRESS, this.mDeviceAddress);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
            return;
        }
        Logger.log(session, level, fullMessage);
    }

    private boolean initialize() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            loge("Unable to initialize BluetoothManager.");
            return false;
        }
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        if (this.mBluetoothAdapter != null) {
            return true;
        }
        loge("Unable to obtain a BluetoothAdapter.");
        return false;
    }

    private static IntentFilter makeDfuActionIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        return intentFilter;
    }

    private void loge(String message) {
    }

    private void loge(String message, Throwable e) {
    }

    private void logw(String message) {
    }

    private void logi(String message) {
    }
}
