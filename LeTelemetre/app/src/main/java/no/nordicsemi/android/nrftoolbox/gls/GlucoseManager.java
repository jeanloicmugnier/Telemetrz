package no.nordicsemi.android.nrftoolbox.gls;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.v4.media.TransportMediator;
import android.util.SparseArray;
import java.util.Calendar;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.gls.GlucoseRecord.MeasurementContext;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class GlucoseManager implements BleManager<GlucoseManagerCallbacks> {
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC;
    public static final UUID BATTERY_SERVICE;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final int FILTER_TYPE_SEQUENCE_NUMBER = 1;
    private static final int FILTER_TYPE_USER_FACING_TIME = 2;
    private static final UUID GF_CHARACTERISTIC;
    public static final UUID GLS_SERVICE_UUID;
    private static final UUID GM_CHARACTERISTIC;
    private static final UUID GM_CONTEXT_CHARACTERISTIC;
    private static final int OPERATOR_ALL_RECORDS = 1;
    private static final int OPERATOR_FIRST_RECORD = 5;
    private static final int OPERATOR_GREATER_THEN_OR_EQUAL = 3;
    private static final int OPERATOR_LAST_RECORD = 6;
    private static final int OPERATOR_LESS_THEN_OR_EQUAL = 2;
    private static final int OPERATOR_NULL = 0;
    private static final int OPERATOR_WITHING_RANGE = 4;
    private static final int OP_CODE_ABORT_OPERATION = 3;
    private static final int OP_CODE_DELETE_STORED_RECORDS = 2;
    private static final int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
    private static final int OP_CODE_REPORT_NUMBER_OF_RECORDS = 4;
    private static final int OP_CODE_REPORT_STORED_RECORDS = 1;
    private static final int OP_CODE_RESPONSE_CODE = 6;
    private static final UUID RACP_CHARACTERISTIC;
    private static final int RESPONSE_ABORT_UNSUCCESSFUL = 7;
    private static final int RESPONSE_INVALID_OPERAND = 5;
    private static final int RESPONSE_INVALID_OPERATOR = 3;
    private static final int RESPONSE_NO_RECORDS_FOUND = 6;
    private static final int RESPONSE_OPERAND_NOT_SUPPORTED = 9;
    private static final int RESPONSE_OPERATOR_NOT_SUPPORTED = 4;
    private static final int RESPONSE_OP_CODE_NOT_SUPPORTED = 2;
    private static final int RESPONSE_PROCEDURE_NOT_COMPLETED = 8;
    private static final int RESPONSE_SUCCESS = 1;
    private static final String TAG = "GlucoseManager";
    private static GlucoseManager mInstance;
    private boolean mAbort;
    private BluetoothGattCharacteristic mBatteryLevelCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BroadcastReceiver mBondingBroadcastReceiver;
    private GlucoseManagerCallbacks mCallbacks;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGattCharacteristic mGlucoseFeatureCharacteristic;
    private BluetoothGattCharacteristic mGlucoseMeasurementCharacteristic;
    private BluetoothGattCharacteristic mGlucoseMeasurementContextCharacteristic;
    private Handler mHandler;
    private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;
    private final SparseArray<GlucoseRecord> mRecords;

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseManager.1 */
    class C00941 extends BroadcastReceiver {
        C00941() {
        }

        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
            DebugLogger.m18d(GlucoseManager.TAG, "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1));
            if (!device.getAddress().equals(GlucoseManager.this.mBluetoothGatt.getDevice().getAddress())) {
                return;
            }
            if (bondState == 11) {
                GlucoseManager.this.mCallbacks.onBondingRequired();
            } else if (bondState == 12) {
                GlucoseManager.this.enableGlucoseMeasurementNotification(GlucoseManager.this.mBluetoothGatt);
                GlucoseManager.this.mCallbacks.onBonded();
            }
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseManager.2 */
    class C00962 extends BluetoothGattCallback {

        /* renamed from: no.nordicsemi.android.nrftoolbox.gls.GlucoseManager.2.1 */
        class C00951 implements Runnable {
            final /* synthetic */ boolean val$contextInfoFollows;
            final /* synthetic */ GlucoseRecord val$record;

            C00951(GlucoseRecord glucoseRecord, boolean z) {
                this.val$record = glucoseRecord;
                this.val$contextInfoFollows = z;
            }

            public void run() {
                GlucoseManager.this.mRecords.put(this.val$record.sequenceNumber, this.val$record);
                if (!this.val$contextInfoFollows) {
                    GlucoseManager.this.mCallbacks.onDatasetChanged();
                }
            }
        }

        C00962() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0) {
                DebugLogger.m19e(GlucoseManager.TAG, "onConnectionStateChange error " + status);
                GlucoseManager.this.mCallbacks.onError(GlucoseManager.ERROR_CONNECTION_STATE_CHANGE, status);
            } else if (newState == GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED) {
                GlucoseManager.this.mCallbacks.onDeviceConnected();
                gatt.discoverServices();
            } else if (newState == 0) {
                GlucoseManager.this.mCallbacks.onDeviceDisconnected();
                gatt.close();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (GlucoseManager.GLS_SERVICE_UUID.equals(service.getUuid())) {
                        GlucoseManager.this.mGlucoseMeasurementCharacteristic = service.getCharacteristic(GlucoseManager.GM_CHARACTERISTIC);
                        GlucoseManager.this.mGlucoseMeasurementContextCharacteristic = service.getCharacteristic(GlucoseManager.GM_CONTEXT_CHARACTERISTIC);
                        GlucoseManager.this.mGlucoseFeatureCharacteristic = service.getCharacteristic(GlucoseManager.GF_CHARACTERISTIC);
                        GlucoseManager.this.mRecordAccessControlPointCharacteristic = service.getCharacteristic(GlucoseManager.RACP_CHARACTERISTIC);
                    } else if (GlucoseManager.BATTERY_SERVICE.equals(service.getUuid())) {
                        GlucoseManager.this.mBatteryLevelCharacteristic = service.getCharacteristic(GlucoseManager.BATTERY_LEVEL_CHARACTERISTIC);
                    }
                }
                if (GlucoseManager.this.mGlucoseMeasurementCharacteristic == null || GlucoseManager.this.mRecordAccessControlPointCharacteristic == null) {
                    GlucoseManager.this.mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                    return;
                }
                GlucoseManager.this.mCallbacks.onServicesDiscovered(GlucoseManager.this.mGlucoseMeasurementContextCharacteristic != null);
                if (GlucoseManager.this.mBatteryLevelCharacteristic != null) {
                    readBatteryLevel(gatt);
                    return;
                } else {
                    GlucoseManager.this.enableGlucoseMeasurementNotification(gatt);
                    return;
                }
            }
            DebugLogger.m19e(GlucoseManager.TAG, "onServicesDiscovered error " + status);
            GlucoseManager.this.mCallbacks.onError(GlucoseManager.ERROR_DISCOVERY_SERVICE, status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != 0) {
                DebugLogger.m19e(GlucoseManager.TAG, "onCharacteristicRead error " + status);
                GlucoseManager.this.mCallbacks.onError(GlucoseManager.ERROR_DISCOVERY_SERVICE, status);
            } else if (GlucoseManager.BATTERY_LEVEL_CHARACTERISTIC.equals(characteristic.getUuid())) {
                GlucoseManager.this.mCallbacks.onBatteryValueReceived(characteristic.getIntValue(17, GlucoseManager.OPERATOR_NULL).intValue());
                GlucoseManager.this.enableGlucoseMeasurementNotification(gatt);
            }
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == 0) {
                if (GlucoseManager.GM_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid())) {
                    GlucoseManager.this.mCallbacks.onGlucoseMeasurementNotificationEnabled();
                    if (GlucoseManager.this.mGlucoseMeasurementContextCharacteristic != null) {
                        GlucoseManager.this.enableGlucoseMeasurementContextNotification(gatt);
                    } else {
                        GlucoseManager.this.enableRecordAccessControlPointIndication(gatt);
                    }
                }
                if (GlucoseManager.GM_CONTEXT_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid())) {
                    GlucoseManager.this.mCallbacks.onGlucoseMeasurementContextNotificationEnabled();
                    GlucoseManager.this.enableRecordAccessControlPointIndication(gatt);
                }
                if (GlucoseManager.RACP_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid())) {
                    GlucoseManager.this.mCallbacks.onRecordAccessControlPointIndicationsEnabled();
                }
            } else if (status != GlucoseManager.RESPONSE_INVALID_OPERAND) {
                DebugLogger.m19e(GlucoseManager.TAG, "onDescriptorWrite error " + status);
                GlucoseManager.this.mCallbacks.onError(GlucoseManager.ERROR_WRITE_DESCRIPTOR, status);
            } else if (gatt.getDevice().getBondState() != 10) {
                DebugLogger.m22w(GlucoseManager.TAG, GlucoseManager.ERROR_AUTH_ERROR_WHILE_BONDED);
                GlucoseManager.this.mCallbacks.onError(GlucoseManager.ERROR_AUTH_ERROR_WHILE_BONDED, status);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            int offset;
            GlucoseRecord record;
            int flags;
            if (GlucoseManager.GM_CHARACTERISTIC.equals(uuid)) {
                flags = characteristic.getIntValue(17, GlucoseManager.OPERATOR_NULL).intValue();
                offset = GlucoseManager.OPERATOR_NULL + GlucoseManager.RESPONSE_SUCCESS;
                boolean timeOffsetPresent = (flags & GlucoseManager.RESPONSE_SUCCESS) > 0;
                boolean typeAndLocationPresent = (flags & GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED) > 0;
                int concentrationUnit = (flags & GlucoseManager.RESPONSE_OPERATOR_NOT_SUPPORTED) > 0 ? GlucoseManager.RESPONSE_SUCCESS : GlucoseManager.OPERATOR_NULL;
                boolean sensorStatusAnnunciationPresent = (flags & GlucoseManager.RESPONSE_PROCEDURE_NOT_COMPLETED) > 0;
                boolean contextInfoFollows = (flags & 16) > 0;
                record = new GlucoseRecord();
                record.sequenceNumber = characteristic.getIntValue(18, offset).intValue();
                offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                int year = characteristic.getIntValue(18, GlucoseManager.RESPONSE_INVALID_OPERATOR).intValue();
                int month = characteristic.getIntValue(17, GlucoseManager.RESPONSE_INVALID_OPERAND).intValue();
                int day = characteristic.getIntValue(17, GlucoseManager.RESPONSE_NO_RECORDS_FOUND).intValue();
                int hours = characteristic.getIntValue(17, GlucoseManager.RESPONSE_ABORT_UNSUCCESSFUL).intValue();
                int minutes = characteristic.getIntValue(17, GlucoseManager.RESPONSE_PROCEDURE_NOT_COMPLETED).intValue();
                int seconds = characteristic.getIntValue(17, GlucoseManager.RESPONSE_OPERAND_NOT_SUPPORTED).intValue();
                offset += GlucoseManager.RESPONSE_ABORT_UNSUCCESSFUL;
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, hours, minutes, seconds);
                record.time = calendar;
                if (timeOffsetPresent) {
                    record.timeOffset = characteristic.getIntValue(34, offset).intValue();
                    offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                }
                if (typeAndLocationPresent) {
                    record.glucoseConcentration = characteristic.getFloatValue(50, offset).floatValue();
                    record.unit = concentrationUnit;
                    int typeAndLocation = characteristic.getIntValue(17, offset + GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED).intValue();
                    record.type = (typeAndLocation & 240) >> GlucoseManager.RESPONSE_OPERATOR_NOT_SUPPORTED;
                    record.sampleLocation = typeAndLocation & 15;
                    offset += GlucoseManager.RESPONSE_INVALID_OPERATOR;
                }
                if (sensorStatusAnnunciationPresent) {
                    record.status = characteristic.getIntValue(18, offset).intValue();
                    offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                }
                GlucoseManager.this.mHandler.post(new C00951(record, contextInfoFollows));
                return;
            }
            if (GlucoseManager.GM_CONTEXT_CHARACTERISTIC.equals(uuid)) {
                flags = characteristic.getIntValue(17, GlucoseManager.OPERATOR_NULL).intValue();
                offset = GlucoseManager.OPERATOR_NULL + GlucoseManager.RESPONSE_SUCCESS;
                boolean carbohydratePresent = (flags & GlucoseManager.RESPONSE_SUCCESS) > 0;
                boolean mealPresent = (flags & GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED) > 0;
                boolean testerHealthPresent = (flags & GlucoseManager.RESPONSE_OPERATOR_NOT_SUPPORTED) > 0;
                boolean exercisePresent = (flags & GlucoseManager.RESPONSE_PROCEDURE_NOT_COMPLETED) > 0;
                boolean medicationPresent = (flags & 16) > 0;
                int medicationUnit = (flags & 32) > 0 ? GlucoseManager.RESPONSE_SUCCESS : GlucoseManager.OPERATOR_NULL;
                boolean hbA1cPresent = (flags & 64) > 0;
                boolean moreFlagsPresent = (flags & TransportMediator.FLAG_KEY_MEDIA_NEXT) > 0;
                int sequenceNumber = characteristic.getIntValue(18, offset).intValue();
                offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                record = (GlucoseRecord) GlucoseManager.this.mRecords.get(sequenceNumber);
                if (record == null) {
                    DebugLogger.m22w(GlucoseManager.TAG, "Context information with unknown sequence number: " + sequenceNumber);
                    return;
                }
                MeasurementContext context = new MeasurementContext();
                record.context = context;
                if (moreFlagsPresent) {
                    offset += GlucoseManager.RESPONSE_SUCCESS;
                }
                if (carbohydratePresent) {
                    context.carbohydrateId = characteristic.getIntValue(17, offset).intValue();
                    context.carbohydrateUnits = characteristic.getFloatValue(50, offset + GlucoseManager.RESPONSE_SUCCESS).floatValue();
                    offset += GlucoseManager.RESPONSE_INVALID_OPERATOR;
                }
                if (mealPresent) {
                    context.meal = characteristic.getIntValue(17, offset).intValue();
                    offset += GlucoseManager.RESPONSE_SUCCESS;
                }
                if (testerHealthPresent) {
                    int testerHealth = characteristic.getIntValue(17, offset).intValue();
                    context.tester = (testerHealth & 240) >> GlucoseManager.RESPONSE_OPERATOR_NOT_SUPPORTED;
                    context.health = testerHealth & 15;
                    offset += GlucoseManager.RESPONSE_SUCCESS;
                }
                if (exercisePresent) {
                    context.exerciseDurtion = characteristic.getIntValue(18, offset).intValue();
                    context.exerciseIntensity = characteristic.getIntValue(17, offset + GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED).intValue();
                    offset += GlucoseManager.RESPONSE_INVALID_OPERATOR;
                }
                if (medicationPresent) {
                    context.medicationId = characteristic.getIntValue(17, offset).intValue();
                    context.medicationQuantity = characteristic.getFloatValue(50, offset + GlucoseManager.RESPONSE_SUCCESS).floatValue();
                    context.medicationUnit = medicationUnit;
                    offset += GlucoseManager.RESPONSE_INVALID_OPERATOR;
                }
                if (hbA1cPresent) {
                    context.HbA1c = characteristic.getFloatValue(50, offset).floatValue();
                    offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                }
                GlucoseManager.this.mCallbacks.onDatasetChanged();
                return;
            }
            int opCode = characteristic.getIntValue(17, GlucoseManager.OPERATOR_NULL).intValue();
            offset = GlucoseManager.OPERATOR_NULL + GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
            if (opCode == GlucoseManager.RESPONSE_INVALID_OPERAND) {
                int number = characteristic.getIntValue(18, offset).intValue();
                offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                GlucoseManager.this.mCallbacks.onNumberOfRecordsRequested(number);
                BluetoothGattCharacteristic racpCharacteristic = GlucoseManager.this.mRecordAccessControlPointCharacteristic;
                GlucoseManager.this.setOpCode(racpCharacteristic, GlucoseManager.RESPONSE_SUCCESS, GlucoseManager.RESPONSE_SUCCESS, new Integer[GlucoseManager.OPERATOR_NULL]);
                GlucoseManager.this.mBluetoothGatt.writeCharacteristic(racpCharacteristic);
            } else if (opCode == GlucoseManager.RESPONSE_NO_RECORDS_FOUND) {
                int requestedOpCode = characteristic.getIntValue(17, offset).intValue();
                int responseCode = characteristic.getIntValue(17, GlucoseManager.RESPONSE_INVALID_OPERATOR).intValue();
                offset += GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED;
                DebugLogger.m18d(GlucoseManager.TAG, "Response result for: " + requestedOpCode + " is: " + responseCode);
                switch (responseCode) {
                    case GlucoseManager.RESPONSE_SUCCESS /*1*/:
                        if (!GlucoseManager.this.mAbort) {
                            GlucoseManager.this.mCallbacks.onOperationCompleted();
                            break;
                        } else {
                            GlucoseManager.this.mCallbacks.onOperationAborted();
                            break;
                        }
                    case GlucoseManager.RESPONSE_OP_CODE_NOT_SUPPORTED /*2*/:
                        GlucoseManager.this.mCallbacks.onOperationNotSupported();
                        break;
                    case GlucoseManager.RESPONSE_NO_RECORDS_FOUND /*6*/:
                        GlucoseManager.this.mCallbacks.onOperationCompleted();
                        break;
                    default:
                        GlucoseManager.this.mCallbacks.onOperationFailed();
                        break;
                }
                GlucoseManager.this.mAbort = false;
            }
        }

        private void readBatteryLevel(BluetoothGatt gatt) {
            DebugLogger.m18d(GlucoseManager.TAG, "readBatteryLevel()");
            gatt.readCharacteristic(GlucoseManager.this.mBatteryLevelCharacteristic);
        }
    }

    public GlucoseManager() {
        this.mRecords = new SparseArray();
        this.mBondingBroadcastReceiver = new C00941();
        this.mGattCallback = new C00962();
    }

    static {
        GLS_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
        BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
        GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");
        GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb");
        RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");
        BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static GlucoseManager getGlucoseManager() {
        if (mInstance == null) {
            mInstance = new GlucoseManager();
        }
        return mInstance;
    }

    public void setGattCallbacks(GlucoseManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public SparseArray<GlucoseRecord> getRecords() {
        return this.mRecords;
    }

    public void connect(Context context, BluetoothDevice device) {
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        this.mContext = context;
        context.registerReceiver(this.mBondingBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
        this.mBluetoothGatt = device.connectGatt(context, false, this.mGattCallback);
    }

    public void disconnect() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        }
    }

    public void clear() {
        this.mRecords.clear();
        this.mCallbacks.onDatasetChanged();
    }

    public void getLastRecord() {
        if (this.mBluetoothGatt != null && this.mRecordAccessControlPointCharacteristic != null) {
            clear();
            this.mCallbacks.onOperationStarted();
            BluetoothGattCharacteristic characteristic = this.mRecordAccessControlPointCharacteristic;
            setOpCode(characteristic, RESPONSE_SUCCESS, RESPONSE_NO_RECORDS_FOUND, new Integer[OPERATOR_NULL]);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void getFirstRecord() {
        if (this.mBluetoothGatt != null && this.mRecordAccessControlPointCharacteristic != null) {
            clear();
            this.mCallbacks.onOperationStarted();
            BluetoothGattCharacteristic characteristic = this.mRecordAccessControlPointCharacteristic;
            setOpCode(characteristic, RESPONSE_SUCCESS, RESPONSE_INVALID_OPERAND, new Integer[OPERATOR_NULL]);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void getAllRecords() {
        if (this.mBluetoothGatt != null && this.mRecordAccessControlPointCharacteristic != null) {
            clear();
            this.mCallbacks.onOperationStarted();
            BluetoothGattCharacteristic characteristic = this.mRecordAccessControlPointCharacteristic;
            setOpCode(characteristic, RESPONSE_OPERATOR_NOT_SUPPORTED, RESPONSE_SUCCESS, new Integer[OPERATOR_NULL]);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void refreshRecords() {
        if (this.mBluetoothGatt != null && this.mRecordAccessControlPointCharacteristic != null) {
            if (this.mRecords.size() == 0) {
                getAllRecords();
                return;
            }
            this.mCallbacks.onOperationStarted();
            int sequenceNumber = this.mRecords.keyAt(this.mRecords.size() - 1) + RESPONSE_SUCCESS;
            BluetoothGattCharacteristic characteristic = this.mRecordAccessControlPointCharacteristic;
            Integer[] numArr = new Integer[RESPONSE_SUCCESS];
            numArr[OPERATOR_NULL] = Integer.valueOf(sequenceNumber);
            setOpCode(characteristic, RESPONSE_SUCCESS, RESPONSE_INVALID_OPERATOR, numArr);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void abort() {
        if (this.mBluetoothGatt != null && this.mRecordAccessControlPointCharacteristic != null) {
            this.mAbort = true;
            BluetoothGattCharacteristic characteristic = this.mRecordAccessControlPointCharacteristic;
            setOpCode(characteristic, RESPONSE_INVALID_OPERATOR, OPERATOR_NULL, new Integer[OPERATOR_NULL]);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void deleteAllRecords() {
        if (this.mBluetoothGatt != null && this.mRecordAccessControlPointCharacteristic != null) {
            clear();
            this.mCallbacks.onOperationStarted();
            BluetoothGattCharacteristic characteristic = this.mRecordAccessControlPointCharacteristic;
            setOpCode(characteristic, RESPONSE_OP_CODE_NOT_SUPPORTED, RESPONSE_SUCCESS, new Integer[OPERATOR_NULL]);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    private void enableGlucoseMeasurementNotification(BluetoothGatt gatt) {
        DebugLogger.m18d(TAG, "enableGlucoseMeasurementNotification()");
        gatt.setCharacteristicNotification(this.mGlucoseMeasurementCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mGlucoseMeasurementCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableGlucoseMeasurementContextNotification(BluetoothGatt gatt) {
        DebugLogger.m18d(TAG, "enableGlucoseMeasurementContextNotification()");
        gatt.setCharacteristicNotification(this.mGlucoseMeasurementContextCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mGlucoseMeasurementContextCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableRecordAccessControlPointIndication(BluetoothGatt gatt) {
        DebugLogger.m18d(TAG, "enableGlucoseMeasurementContextNotification()");
        gatt.setCharacteristicNotification(this.mRecordAccessControlPointCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mRecordAccessControlPointCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void setOpCode(BluetoothGattCharacteristic characteristic, int opCode, int operator, Integer... params) {
        characteristic.setValue(new byte[(((params.length > 0 ? RESPONSE_SUCCESS : OPERATOR_NULL) + RESPONSE_OP_CODE_NOT_SUPPORTED) + (params.length * RESPONSE_OP_CODE_NOT_SUPPORTED))]);
        characteristic.setValue(opCode, 17, OPERATOR_NULL);
        int offset = OPERATOR_NULL + RESPONSE_SUCCESS;
        characteristic.setValue(operator, 17, offset);
        offset += RESPONSE_SUCCESS;
        if (params.length > 0) {
            characteristic.setValue(RESPONSE_SUCCESS, 17, offset);
            offset += RESPONSE_SUCCESS;
            Integer[] arr$ = params;
            int len$ = arr$.length;
            for (int i$ = OPERATOR_NULL; i$ < len$; i$ += RESPONSE_SUCCESS) {
                characteristic.setValue(arr$[i$].intValue(), 18, offset);
                offset += RESPONSE_OP_CODE_NOT_SUPPORTED;
            }
        }
    }

    public void closeBluetoothGatt() {
        try {
            this.mContext.unregisterReceiver(this.mBondingBroadcastReceiver);
        } catch (Exception e) {
        }
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mRecords.clear();
            this.mGlucoseMeasurementCharacteristic = null;
            this.mGlucoseMeasurementContextCharacteristic = null;
            this.mGlucoseFeatureCharacteristic = null;
            this.mRecordAccessControlPointCharacteristic = null;
            this.mBatteryLevelCharacteristic = null;
            this.mBluetoothGatt = null;
        }
    }
}
