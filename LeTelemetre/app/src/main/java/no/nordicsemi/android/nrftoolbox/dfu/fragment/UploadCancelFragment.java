package no.nordicsemi.android.nrftoolbox.dfu.fragment;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.nrftoolbox.C0063R;

public class UploadCancelFragment extends DialogFragment {
    private static final String TAG = "UploadCancelFragment";
    private CancelFragmentListener mListener;

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.fragment.UploadCancelFragment.1 */
    class C00851 implements OnClickListener {
        C00851() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    }

    /* renamed from: no.nordicsemi.android.nrftoolbox.dfu.fragment.UploadCancelFragment.2 */
    class C00862 implements OnClickListener {
        C00862() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(UploadCancelFragment.this.getActivity());
            Intent pauseAction = new Intent(DfuBaseService.BROADCAST_ACTION);
            pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, 2);
            manager.sendBroadcast(pauseAction);
            UploadCancelFragment.this.mListener.onCancelUpload();
        }
    }

    public interface CancelFragmentListener {
        void onCancelUpload();
    }

    public static UploadCancelFragment getInstance() {
        return new UploadCancelFragment();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (CancelFragmentListener) activity;
        } catch (ClassCastException e) {
            Log.d(TAG, "The parent Activity must implement CancelFragmentListener interface");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(C0063R.string.dfu_confirmation_dialog_title).setMessage(C0063R.string.dfu_upload_dialog_cancel_message).setCancelable(false).setPositiveButton(17039379, new C00862()).setNegativeButton(17039369, new C00851()).create();
    }

    public void onCancel(DialogInterface dialog) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        Intent pauseAction = new Intent(DfuBaseService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, 1);
        manager.sendBroadcast(pauseAction);
    }
}
