package no.nordicsemi.android.nrftoolbox.uart;

import android.net.Uri;
import no.nordicsemi.android.log.localprovider.LocalLogContentProvider;

public class UARTLocalLogContentProvider extends LocalLogContentProvider {
    public static final String AUTHORITY = "no.nordicsemi.android.nrftoolbox.uart.log";
    public static final Uri AUTHORITY_URI;

    static {
        AUTHORITY_URI = Uri.parse("content://no.nordicsemi.android.nrftoolbox.uart.log");
    }

    protected Uri getAuthorityUri() {
        return AUTHORITY_URI;
    }
}
