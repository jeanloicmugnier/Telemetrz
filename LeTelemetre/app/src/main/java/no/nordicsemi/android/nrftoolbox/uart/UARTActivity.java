package no.nordicsemi.android.nrftoolbox.uart;

import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.SimplePanelSlideListener;
import android.view.View;
import java.util.UUID;
import no.nordicsemi.android.nrftoolbox.C0063R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.uart.UARTControlFragment.ControlFragmentListener;
import no.nordicsemi.android.nrftoolbox.uart.UARTService.UARTBinder;

public class UARTActivity extends BleProfileServiceReadyActivity<UARTBinder> implements ControlFragmentListener, UARTInterface {
    private static final String SIS_EDIT_MODE = "sis_edit_mode";
    private boolean mEditMode;
    private UARTBinder mServiceBinder;
    private SlidingPaneLayout mSlider;

    /* renamed from: no.nordicsemi.android.nrftoolbox.uart.UARTActivity.1 */
    class C01781 extends SimplePanelSlideListener {
        C01781() {
        }

        public void onPanelClosed(View panel) {
            ((UARTLogFragment) UARTActivity.this.getFragmentManager().findFragmentById(C0063R.id.fragment_log)).onFragmentHidden();
        }
    }

    protected Class<? extends BleProfileService> getServiceClass() {
        return UARTService.class;
    }

    protected void onServiceBinded(UARTBinder binder) {
        this.mServiceBinder = binder;
    }

    protected void onServiceUnbinded() {
        this.mServiceBinder = null;
    }

    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(C0063R.layout.activity_feature_uart);
        SlidingPaneLayout slidingPane = (SlidingPaneLayout) findViewById(C0063R.id.sliding_pane);
        this.mSlider = slidingPane;
        if (slidingPane != null) {
            slidingPane.setSliderFadeColor(0);
            slidingPane.setShadowResourceLeft(C0063R.drawable.shadow_r);
            slidingPane.setPanelSlideListener(new C01781());
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mEditMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
        setEditMode(this.mEditMode, false);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SIS_EDIT_MODE, this.mEditMode);
    }

    protected boolean onOptionsItemSelected(int itemId) {
        switch (itemId) {
            case C0063R.id.action_show_log:
                this.mSlider.openPane();
                return true;
            default:
                return false;
        }
    }

    protected int getLoggerProfileTitle() {
        return C0063R.string.uart_feature_title;
    }

    protected Uri getLocalAuthorityLogger() {
        return UARTLocalLogContentProvider.AUTHORITY_URI;
    }

    protected void setDefaultUI() {
    }

    public void onServicesDiscovered(boolean optionalServicesFound) {
    }

    public void onDeviceSelected(BluetoothDevice device, String name) {
        super.onDeviceSelected(device, name);
        ((UARTLogFragment) getFragmentManager().findFragmentById(C0063R.id.fragment_log)).onServiceStarted();
    }

    protected int getDefaultDeviceName() {
        return C0063R.string.uart_default_name;
    }

    protected int getAboutTextId() {
        return C0063R.string.uart_about_text;
    }

    protected UUID getFilterUUID() {
        return null;
    }

    protected boolean isDiscoverableRequired() {
        return false;
    }

    public void send(String text) {
        if (this.mServiceBinder != null) {
            this.mServiceBinder.send(text);
        }
    }

    public void setEditMode(boolean editMode) {
        setEditMode(editMode, true);
    }

    public void onBackPressed() {
        if (this.mSlider != null && this.mSlider.isOpen()) {
            this.mSlider.closePane();
        } else if (this.mEditMode) {
            ((UARTControlFragment) getFragmentManager().findFragmentById(C0063R.id.fragment_control)).setEditMode(false);
        } else {
            super.onBackPressed();
        }
    }

    private void setEditMode(boolean editMode, boolean change) {
        this.mEditMode = editMode;
        if (change) {
            TransitionDrawable transition = (TransitionDrawable) getResources().getDrawable(editMode ? C0063R.drawable.start_edit_mode : C0063R.drawable.stop_edit_mode);
            transition.setCrossFadeEnabled(true);
            getActionBar().setBackgroundDrawable(transition);
            transition.startTransition(200);
            if (this.mSlider != null && editMode) {
                this.mSlider.closePane();
                return;
            }
            return;
        }
        ColorDrawable color = new ColorDrawable();
        if (editMode) {
            color.setColor(getResources().getColor(C0063R.color.orange));
        } else {
            color.setColor(getResources().getColor(C0063R.color.actionBarColor));
        }
        getActionBar().setBackgroundDrawable(color);
    }
}
