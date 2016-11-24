package no.nordicsemi.android.nrftoolbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

public class SplashscreenActivity extends Activity {
    private static final int DELAY = 1000;

    /* renamed from: no.nordicsemi.android.nrftoolbox.SplashscreenActivity.1 */
    class C00641 implements Runnable {
        C00641() {
        }

        public void run() {
            Intent intent = new Intent(SplashscreenActivity.this, FeaturesActivity.class);
            intent.addFlags(AccessibilityNodeInfoCompat.ACTION_CUT);
            SplashscreenActivity.this.startActivity(intent);
            SplashscreenActivity.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(Integer.valueOf(C0063R.layout.activity_splashscreen));
        new Handler().postDelayed(new C00641(), 1000);
    }

    public void onBackPressed() {
    }
}
