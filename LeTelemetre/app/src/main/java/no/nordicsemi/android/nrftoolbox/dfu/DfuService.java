package no.nordicsemi.android.nrftoolbox.dfu;

import android.app.Activity;
import no.nordicsemi.android.dfu.DfuBaseService;

public abstract class DfuService extends DfuBaseService {
    protected Class<? extends Activity> getNotificationTarget() {
        return NotificationActivity.class;
    }
}
