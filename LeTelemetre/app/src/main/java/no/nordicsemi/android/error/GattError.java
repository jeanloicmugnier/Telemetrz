package no.nordicsemi.android.error;

import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.WindowCompat;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.dfu.DfuSettingsConstants;
import no.nordicsemi.android.log.LogContract.Log.Level;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import org.achartengine.tools.Zoom;

public class GattError {
    public static String parse(int error) {
        switch (error) {
            case Zoom.ZOOM_AXIS_X /*1*/:
                return "GATT INVALID HANDLE";
            case Zoom.ZOOM_AXIS_Y /*2*/:
                return "GATT READ NOT PERMIT";
            case BleProfileService.STATE_DISCONNECTING /*3*/:
                return "GATT WRITE NOT PERMIT";
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                return "GATT INVALID PDU";
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                return "GATT INSUF AUTHENTICATION";
            //case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
            //    return "GATT REQ NOT SUPPORTED";
            case MotionEventCompat.ACTION_HOVER_MOVE /*7*/:
                return "GATT INVALID OFFSET";
            case TransportMediator.FLAG_KEY_MEDIA_PLAY_PAUSE /*8*/:
                return "GATT INSUF AUTHORIZATION";
            case WindowCompat.FEATURE_ACTION_BAR_OVERLAY /*9*/:
                return "GATT PREPARE Q FULL";
            case Level.APPLICATION /*10*/:
                return "GATT NOT FOUND";
            case 11:
                return "GATT NOT LONG";
            case 12:
                return "GATT INSUF KEY SIZE";
            case 13:
                return "GATT INVALID ATTR LEN";
            case 14:
                return "GATT ERR UNLIKELY";
            case Level.WARNING /*15*/:
                return "GATT INSUF ENCRYPTION";
            case TransportMediator.FLAG_KEY_MEDIA_PAUSE /*16*/:
                return "GATT UNSUPPORT GRP TYPE";
            case 17:
                return "GATT INSUF RESOURCE";
            case TransportMediator.FLAG_KEY_MEDIA_NEXT /*128*/:
                return "GATT NO RESOURCES";
            case 129:
                return "GATT INTERNAL ERROR";
            case TransportMediator.KEYCODE_MEDIA_RECORD /*130*/:
                return "GATT WRONG STATE";
            case 131:
                return "GATT DB FULL";
            case 132:
                return "GATT BUSY";
            case 133:
                return "GATT ERROR";
            case 134:
                return "GATT CMD STARTED";
            case 135:
                return "GATT ILLEGAL PARAMETER";
            case 136:
                return "GATT PENDING";
            case 137:
                return "GATT AUTH FAIL";
            case 138:
                return "GATT MORE";
            case 139:
                return "GATT INVALID CFG";
            case 140:
                return "GATT SERVICE STARTED";
            case 141:
                return "GATT ENCRYPED NO MITM";
            case 142:
                return "GATT NOT ENCRYPTED";
            case MotionEventCompat.ACTION_MASK /*255*/:
                return "DFU SERVICE DISCOVERY NOT STARTED";
            case 257:
                return "TOO MANY OPEN CONNECTIONS";
            case DfuSettingsConstants.SETTINGS_DEFAULT_MBR_SIZE /*4096*/:
                return "DFU DEVICE DISCONNECTED";
            case DfuBaseService.ERROR_FILE_NOT_FOUND /*4097*/:
                return "DFU FILE NOT FOUND";
            case DfuBaseService.ERROR_FILE_ERROR /*4098*/:
                return "DFU FILE ERROR";
            case DfuBaseService.ERROR_FILE_INVALID /*4099*/:
                return "DFU NOT A VALID HEX FILE";
            case DfuBaseService.ERROR_FILE_IO_EXCEPTION /*4100*/:
                return "DFU IO EXCEPTION";
            case DfuBaseService.ERROR_SERVICE_DISCOVERY_NOT_STARTED /*4101*/:
                return "DFU ERROR WHILE SERVICE DISCOVERY";
            case DfuBaseService.ERROR_SERVICE_NOT_FOUND /*4102*/:
                return "DFU SERVICE NOT FOUND";
            case DfuBaseService.ERROR_CHARACTERISTICS_NOT_FOUND /*4103*/:
                return "DFU CHARACTERISTICS NOT FOUND";
            case DfuBaseService.ERROR_FILE_TYPE_UNSUPPORTED /*4105*/:
                return "DFU FILE TYPE NOT SUPPORTED";
            default:
                if ((error & DfuBaseService.ERROR_REMOTE_MASK) > 0) {
                    switch (error & -8193) {
                        case Zoom.ZOOM_AXIS_Y /*2*/:
                            return "REMOTE DFU INVALID STATE";
                        case BleProfileService.STATE_DISCONNECTING /*3*/:
                            return "REMOTE DFU NOT SUPPORTED";
                        case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                            return "REMOTE DFU DATA SIZE EXCEEDS LIMIT";
                        case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                            return "REMOTE DFU INVALID CRC ERROR";
                       // case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                        //     return "REMOTE DFU OPERATION FAILED";
                    }
                }
                return "UNKNOWN (" + error + ")";
        }
    }
}
