package no.nordicsemi.android.nrftoolbox.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class AppAdapter extends BaseAdapter {
    private static final String CATEGORY = "no.nordicsemi.android.nrftoolbox.LAUNCHER";
    private static final String MCP_PACKAGE = "no.nordicsemi.android.mcp";
    private final List<ResolveInfo> mApplications;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final PackageManager mPackageManager;

    /* renamed from: no.nordicsemi.android.nrftoolbox.adapter.AppAdapter.1 */
    class C00651 implements OnClickListener {
        final /* synthetic */ ResolveInfo val$info;

        C00651(ResolveInfo resolveInfo) {
            this.val$info = resolveInfo;
        }

        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this.val$info.activityInfo.packageName, this.val$info.activityInfo.name));
            intent.setFlags(AccessibilityNodeInfoCompat.ACTION_CUT);
            AppAdapter.this.mContext.startActivity(intent);
        }
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView label;
        private View view;

        private ViewHolder() {
        }
    }

    public AppAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        PackageManager pm = context.getPackageManager();
        this.mPackageManager = pm;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(CATEGORY);
        List<ResolveInfo> appList = pm.queryIntentActivities(intent, 0);
        this.mApplications = appList;
        for (ResolveInfo info : appList) {
            if (MCP_PACKAGE.equals(info.activityInfo.packageName)) {
                appList.remove(info);
                break;
            }
        }
        Collections.sort(appList, new DisplayNameComparator(pm));
    }

    public int getCount() {
        return this.mApplications.size();
    }

    public Object getItem(int position) {
        return this.mApplications.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = this.mInflater.inflate(C0063R.layout.feature_icon, parent, false);
            holder = new ViewHolder();
            holder.view = view;
            holder.icon = (ImageView) view.findViewById(C0063R.id.icon);
            holder.label = (TextView) view.findViewById(C0063R.id.label);
            view.setTag(holder);
        }
        ResolveInfo info = (ResolveInfo) this.mApplications.get(position);
        PackageManager pm = this.mPackageManager;
        holder = (ViewHolder) view.getTag();
        holder.icon.setImageDrawable(info.loadIcon(pm));
        holder.label.setText(info.loadLabel(pm).toString().toUpperCase(Locale.US));
        holder.view.setOnClickListener(new C00651(info));
        return view;
    }
}
