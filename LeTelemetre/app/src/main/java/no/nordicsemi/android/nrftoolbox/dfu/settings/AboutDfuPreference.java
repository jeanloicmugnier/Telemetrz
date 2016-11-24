package no.nordicsemi.android.nrftoolbox.dfu.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class AboutDfuPreference extends Preference {
    public AboutDfuPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AboutDfuPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onClick() {
        Context context = getContext();
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://devzone.nordicsemi.com/documentation/nrf51/6.1.0/s110/html/a00056.html"));
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setFlags(268435456);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(getContext(), C0063R.string.no_application, 1).show();
        }
    }
}
