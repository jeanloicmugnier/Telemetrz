package no.nordicsemi.android.nrftoolbox.dfu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;

public class NotificationActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTaskRoot()) {
            Intent startAppIntent = new Intent(this, FeaturesActivity.class).addFlags(268435456);
            Intent parentIntent = new Intent(this, DfuActivity.class).putExtras(getIntent().getExtras());
            startActivities(new Intent[]{parentIntent, startAppIntent});
        }
        finish();
    }
}
