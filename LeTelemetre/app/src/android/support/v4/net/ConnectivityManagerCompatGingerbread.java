package android.support.v4.net;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.media.TransportMediator;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import org.achartengine.tools.Zoom;

class ConnectivityManagerCompatGingerbread {
    ConnectivityManagerCompatGingerbread() {
    }

    public static boolean isActiveNetworkMetered(ConnectivityManager cm) {
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return true;
        }
        switch (info.getType()) {
            case Zoom.ZOOM_AXIS_XY /*0*/:
            case Zoom.ZOOM_AXIS_Y /*2*/:
            case BleProfileService.STATE_DISCONNECTING /*3*/:
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                return true;
            case Zoom.ZOOM_AXIS_X /*1*/:
                return false;
            default:
                return true;
        }
    }
}
