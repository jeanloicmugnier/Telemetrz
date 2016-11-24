package no.nordicsemi.android.dfu.exception;

import java.io.IOException;

public class HexFileValidationException extends IOException {
    private static final long serialVersionUID = -6467104024030837875L;

    public HexFileValidationException(String message) {
        super(message);
    }
}
