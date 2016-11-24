package no.nordicsemi.android.nrftoolbox.proximity;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class LinklossFragment extends DialogFragment {
    private static final String ARG_NAME = "name";
    private String mName;

    public static LinklossFragment getInstance(String name) {
        LinklossFragment fragment = new LinklossFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mName = getArguments().getString(ARG_NAME);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(getString(C0063R.string.app_name)).setMessage(getString(C0063R.string.proximity_notification_linkloss_alert, new Object[]{this.mName})).setPositiveButton(17039370, null).create();
    }
}
