package no.nordicsemi.android.nrftoolbox.dfu.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class FileBrowserAppsAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Resources mResources;

    public FileBrowserAppsAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mResources = context.getResources();
    }

    public int getCount() {
        return this.mResources.getStringArray(C0063R.array.dfu_app_file_browser).length;
    }

    public Object getItem(int position) {
        return this.mResources.getStringArray(C0063R.array.dfu_app_file_browser_action)[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = this.mInflater.inflate(C0063R.layout.app_file_browser_item, parent, false);
        }
        TextView item = (TextView) view;
        item.setText(this.mResources.getStringArray(C0063R.array.dfu_app_file_browser)[position]);
        item.getCompoundDrawablesRelative()[0].setLevel(position);
        return view;
    }
}
