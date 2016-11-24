package no.nordicsemi.android.dfu.exception;

public class DeviceDisconnectedException extends Exception {
    private static final long serialVersionUID = -6901728550661937942L;
    private final int mState;

    public DeviceDisconnectedException(String message, int state) {
        super(message);
        this.mState = state;
    }

    public int getConnectionState() {
        return this.mState;
    }

    public String getMessage() {
        return super.getMessage() + " (connection state: " + this.mState + ")";
    }
}
