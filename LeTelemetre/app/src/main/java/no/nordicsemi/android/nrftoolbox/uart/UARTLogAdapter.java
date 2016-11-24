package no.nordicsemi.android.nrftoolbox.uart;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.internal.view.SupportMenu;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import java.util.Calendar;
import no.nordicsemi.android.nrftoolbox.C0063R;
import org.achartengine.renderer.DefaultRenderer;

public class UARTLogAdapter extends CursorAdapter {
    private static final SparseIntArray mColors;

    private class ViewHolder {
        private TextView data;
        private TextView time;

        private ViewHolder() {
        }
    }

    static {
        mColors = new SparseIntArray();
        mColors.put(0, -16737058);
        mColors.put(1, -4673450);
        mColors.put(5, DefaultRenderer.BACKGROUND_COLOR);
        mColors.put(10, -14447601);
        mColors.put(15, -2655962);
        mColors.put(20, SupportMenu.CATEGORY_MASK);
    }

    public UARTLogAdapter(Context context) {
        super(context, null, 0);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(Integer.valueOf(C0063R.layout.log_item), parent, false);
        ViewHolder holder = new ViewHolder();
        holder.time = (TextView) view.findViewById(Integer.valueOf(C0063R.id.time));
        holder.data = (TextView) view.findViewById(Integer.valueOf(C0063R.id.data));
        view.setTag(holder);
        return view;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        Calendar.getInstance().setTimeInMillis(cursor.getLong(1));
        holder.time.setText(context.getString(Integer.valueOf(C0063R.string.log), new Object[]{"calendar"}));
        int level = cursor.getInt(2);
        holder.data.setText(cursor.getString(3));
        holder.data.setTextColor(mColors.get(level));
    }

    public boolean isEnabled(int position) {
        return false;
    }
}
