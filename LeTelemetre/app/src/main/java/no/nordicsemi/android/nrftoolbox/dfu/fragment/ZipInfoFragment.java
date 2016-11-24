package no.nordicsemi.android.nrftoolbox.dfu.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class ZipInfoFragment extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setView(LayoutInflater.from(getActivity()).inflate(C0063R.layout.fragment_zip_info, null)).setTitle(C0063R.string.dfu_file_info).setPositiveButton(17039370, null).create();
    }
}
