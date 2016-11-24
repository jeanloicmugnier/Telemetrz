package no.nordicsemi.android.nrftoolbox;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import no.nordicsemi.android.nrftoolbox.adapter.AppAdapter;

public class FeaturesActivity extends Activity {
    private static final String MCP_CLASS = "no.nordicsemi.android.mcp.DeviceListActivity";
    private static final String MCP_MARKET_URI = "market://details?id=no.nordicsemi.android.mcp";
    private static final String MCP_PACKAGE = "no.nordicsemi.android.mcp";
    private static final String UTILS_CATEGORY = "no.nordicsemi.android.nrftoolbox.UTILS";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    /* renamed from: no.nordicsemi.android.nrftoolbox.FeaturesActivity.1 */
    class C00611 implements OnClickListener {
        final /* synthetic */ ResolveInfo val$mcpInfo;
        final /* synthetic */ Intent val$mcpIntent;

        C00611(Intent intent, ResolveInfo resolveInfo) {
            this.val$mcpIntent = intent;
            this.val$mcpInfo = resolveInfo;
        }

        public void onClick(View v) {
            Intent action = this.val$mcpIntent;
            if (this.val$mcpInfo == null) {
                action = new Intent("android.intent.action.VIEW", Uri.parse(FeaturesActivity.MCP_MARKET_URI));
            }
            action.setFlags(AccessibilityNodeInfoCompat.ACTION_CUT);
            try {
                FeaturesActivity.this.startActivity(action);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(FeaturesActivity.this, Integer.valueOf(C0063R.string.no_application_play), Toast.LENGTH_SHORT).show();
            }
            FeaturesActivity.this.mDrawerLayout.closeDrawers();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.FeaturesActivity.2 */
    class C00622 implements OnClickListener {
        final /* synthetic */ ResolveInfo val$info;

        C00622(ResolveInfo resolveInfo) {
            this.val$info = resolveInfo;
        }

        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this.val$info.activityInfo.packageName, this.val$info.activityInfo.name));
            intent.setFlags(AccessibilityNodeInfoCompat.ACTION_CUT);
            FeaturesActivity.this.startActivity(intent);
            FeaturesActivity.this.mDrawerLayout.closeDrawers();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(Integer.valueOf(C0063R.layout.activity_features));
        if (!ensureBLEExists()) {
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(Integer.valueOf(C0063R.id.drawer_layout));
        this.mDrawerLayout = drawer;
        drawer.setDrawerShadow((int) Integer.valueOf(C0063R.drawable.drawer_shadow), (int) GravityCompat.START);
        DrawerListener actionBarDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawerLayout, Integer.valueOf(C0063R.drawable.ic_drawer), Integer.valueOf(C0063R.string.drawer_open), Integer.valueOf(C0063R.string.drawer_close));
        this.mDrawerToggle = (ActionBarDrawerToggle) actionBarDrawerToggle;
        drawer.setDrawerListener(actionBarDrawerToggle);
        setupPluginsInDrawer((ViewGroup) drawer.findViewById(Integer.valueOf(C0063R.id.plugin_container)));
        GridView grid = (GridView) findViewById(Integer.valueOf(C0063R.id.grid));
        grid.setAdapter(new AppAdapter(this));
        grid.setEmptyView(findViewById(Integer.valueOf(16908292)));
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(Integer.valueOf(C0063R.menu.help), menu);
        return true;
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mDrawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (!this.mDrawerToggle.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                case C0063R.id.action_about:
                    AppHelpFragment.getInstance(C0063R.string.about_text, true).show(getFragmentManager(), null);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void setupPluginsInDrawer(ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(this);
        PackageManager pm = getPackageManager();
        Intent mcpIntent = new Intent("android.intent.action.MAIN");
        mcpIntent.setClassName(MCP_PACKAGE, MCP_CLASS);
        ResolveInfo mcpInfo = pm.resolveActivity(mcpIntent, 0);
        TextView mcpItem = (TextView) container.findViewById(Integer.valueOf(C0063R.id.link_mcp));
        if (mcpInfo == null) {
            mcpItem.setTextColor(Integer.valueOf(-7829368));
            ColorMatrix grayscale = new ColorMatrix();
            grayscale.setSaturation(0.0f);
            mcpItem.getCompoundDrawables()[0].setColorFilter(new ColorMatrixColorFilter(grayscale));
        }
        mcpItem.setOnClickListener(new C00611(mcpIntent, mcpInfo));
        Intent utilsIntent = new Intent("android.intent.action.MAIN");
        utilsIntent.addCategory(UTILS_CATEGORY);
        for (ResolveInfo info : pm.queryIntentActivities(utilsIntent, 0)) {
            View item = inflater.inflate(Integer.valueOf(C0063R.layout.drawer_plugin), container, false);
            ImageView icon = (ImageView) item.findViewById(Integer.valueOf(16908294));
            ((TextView) item.findViewById(Integer.valueOf(16908308))).setText(info.loadLabel(pm));
            icon.setImageDrawable(info.loadIcon(pm));
            item.setOnClickListener(new C00622(info));
            container.addView(item);
        }
    }

    private boolean ensureBLEExists() {
        if (getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            return true;
        }
        Toast.makeText(this, Integer.valueOf(C0063R.string.no_ble), Toast.LENGTH_LONG).show();
        return false;
    }
}
