package no.nordicsemi.android.nrftoolbox;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class AppHelpFragment extends DialogFragment {
    private static final String ARG_TEXT = "ARG_TEXT";
    private static final String ARG_VERSION = "ARG_VERSION";

    public static AppHelpFragment getInstance(int aboutResId, boolean appendVersion) {
        AppHelpFragment fragment = new AppHelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TEXT, aboutResId);
        args.putBoolean(ARG_VERSION, appendVersion);
        fragment.setArguments(args);
        return fragment;
    }

    public static AppHelpFragment getInstance(int aboutResId) {
        AppHelpFragment fragment = new AppHelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TEXT, aboutResId);
        args.putBoolean(ARG_VERSION, false);
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        StringBuilder text = new StringBuilder(getString(args.getInt(ARG_TEXT)));
        if (args.getBoolean(ARG_VERSION)) {
            try {
                text.append(getString(C0063R.string.about_version, new Object[]{getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName}));
            } catch (NameNotFoundException e) {
            }
        }
        return new Builder(getActivity()).setTitle(C0063R.string.about_title).setMessage(text).setPositiveButton(17039370, null).create();
    }
}
