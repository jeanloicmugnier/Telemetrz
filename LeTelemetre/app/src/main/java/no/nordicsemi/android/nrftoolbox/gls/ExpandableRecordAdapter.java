package no.nordicsemi.android.nrftoolbox.gls;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.media.TransportMediator;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.gls.GlucoseRecord.MeasurementContext;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import org.achartengine.tools.Zoom;

public class ExpandableRecordAdapter extends BaseExpandableListAdapter {
    private final Context mContext;
    private final GlucoseManager mGlucoseManager;
    private final LayoutInflater mInflater;

    private class ChildViewHolder {
        private TextView details;
        private TextView title;

        private ChildViewHolder() {
        }
    }

    private class GroupViewHolder {
        private TextView concentration;
        private TextView details;
        private TextView time;

        private GroupViewHolder() {
        }
    }

    public ExpandableRecordAdapter(Context context, GlucoseManager manager) {
        this.mGlucoseManager = manager;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public int getGroupCount() {
        return this.mGlucoseManager.getRecords().size();
    }

    public Object getGroup(int groupPosition) {
        return this.mGlucoseManager.getRecords().valueAt(groupPosition);
    }

    public long getGroupId(int groupPosition) {
        return (long) this.mGlucoseManager.getRecords().keyAt(groupPosition);
    }

    public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView;
        GroupViewHolder holder;
        if (view == null) {
            view = this.mInflater.inflate(Integer.valueOf(C0063R.layout.activity_feature_gls_item), parent, false);
            holder = new GroupViewHolder();
            holder.time = (TextView) view.findViewById(Integer.valueOf(C0063R.id.time));
            holder.details = (TextView) view.findViewById(Integer.valueOf(C0063R.id.details));
            holder.concentration = (TextView) view.findViewById(Integer.valueOf(C0063R.id.gls_concentration));
            view.setTag(holder);
        }
        GlucoseRecord record = (GlucoseRecord) getGroup(position);
        if (record != null) {
            holder = (GroupViewHolder) view.getTag();
            holder.time.setText(this.mContext.getString(Integer.valueOf(C0063R.string.gls_timestamp), new Object[]{record.time}));
            try {
                holder.details.setText(this.mContext.getResources().getStringArray(Integer.valueOf(C0063R.array.gls_type))[record.type]);
            } catch (ArrayIndexOutOfBoundsException e) {
                holder.details.setText(this.mContext.getResources().getStringArray(Integer.valueOf(C0063R.array.gls_type))[0]);
            }
            if (record.unit == 0) {
                holder.concentration.setText(this.mContext.getString(Integer.valueOf(C0063R.string.gls_value), new Object[]{Float.valueOf(record.glucoseConcentration * 100000.0f)}));
            } else {
                holder.concentration.setText(this.mContext.getString(Integer.valueOf(C0063R.string.gls_value), new Object[]{Float.valueOf(record.glucoseConcentration * 1000.0f)}));
            }
        }
        return view;
    }

    public int getChildrenCount(int groupPosition) {
        GlucoseRecord record = (GlucoseRecord) getGroup(groupPosition);
        int count = (record.status != 0 ? 1 : 0) + 1;
        if (record.context == null) {
            return count;
        }
        MeasurementContext context = record.context;
        if (context.carbohydrateId != 0) {
            count++;
        }
        if (context.meal != 0) {
            count++;
        }
        if (context.tester != 0) {
            count++;
        }
        if (context.health != 0) {
            count++;
        }
        if (context.exerciseDurtion != 0) {
            count++;
        }
        if (context.medicationId != 0) {
            count++;
        }
        if (context.HbA1c != 0.0f) {
            return count + 1;
        }
        return count;
    }

    public Object getChild(int groupPosition, int childPosition) {
        Resources resources = this.mContext.getResources();
        GlucoseRecord record = (GlucoseRecord) getGroup(groupPosition);
        StringBuilder builder;
        switch (childPosition) {
            case Zoom.ZOOM_AXIS_XY /*0*/:
                String location;
                try {
                    location = resources.getStringArray(Integer.valueOf(C0063R.array.gls_location))[record.sampleLocation];
                } catch (ArrayIndexOutOfBoundsException e) {
                    location = resources.getStringArray(Integer.valueOf(C0063R.array.gls_location))[0];
                }
                return new Pair(resources.getString(Integer.valueOf(C0063R.string.gls_location_title)), location);
            case Zoom.ZOOM_AXIS_X /*1*/:
                builder = new StringBuilder();
                int status = record.status;
                for (int i = 0; i < 12; i++) {
                    if (((1 << i) & status) > 0) {
                        builder.append(resources.getStringArray(Integer.valueOf(C0063R.array.gls_status_annunciatioin))[i]).append("\n");
                    }
                }
                builder.setLength(builder.length() - 1);
                return new Pair(resources.getString(Integer.valueOf(C0063R.string.gls_status_annunciatioin_title)), builder.toString());
            case Zoom.ZOOM_AXIS_Y /*2*/:
                builder = new StringBuilder();
                builder.append(resources.getStringArray(Integer.valueOf(C0063R.array.gls_context_carbohydrare))[record.context.carbohydrateId]).append(" (" + record.context.carbohydrateUnits + " kg)");
                return new Pair(resources.getString(Integer.valueOf(C0063R.string.gls_context_carbohydrare_title)), builder.toString());
            case BleProfileService.STATE_DISCONNECTING /*3*/:
                builder = new StringBuilder();
                builder.append(resources.getStringArray(Integer.valueOf(C0063R.array.gls_context_meal))[record.context.meal]);
                return new Pair(resources.getString(Integer.valueOf(C0063R.string.gls_context_meal_title)), builder.toString());
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                builder = new StringBuilder();
                builder.append(resources.getStringArray(Integer.valueOf(C0063R.array.gls_context_tester))[record.context.tester]);
                return new Pair(resources.getString(Integer.valueOf(C0063R.string.gls_context_tester_title)), builder.toString());
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                builder = new StringBuilder();
                builder.append(resources.getStringArray(Integer.valueOf(C0063R.array.gls_context_health))[record.context.health]);
                return new Pair(resources.getString(Integer.valueOf(C0063R.string.gls_context_health_title)), builder.toString());
            default:
                return new Pair("Not implemented", "The value exists but is not shown");
        }
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) (groupPosition + childPosition);
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = this.mInflater.inflate(Integer.valueOf(C0063R.layout.activity_feature_gls_subitem), parent, false);
            holder = new ChildViewHolder();
            holder.title = (TextView) view.findViewById(Integer.valueOf(16908308));
            holder.details = (TextView) view.findViewById(Integer.valueOf(16908309));
            view.setTag(holder);
        }
        Pair<String, String> value = (Pair) getChild(groupPosition, childPosition);
        holder = (ChildViewHolder) view.getTag();
        holder.title.setText((CharSequence) value.first);
        holder.details.setText((CharSequence) value.second);
        return view;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
