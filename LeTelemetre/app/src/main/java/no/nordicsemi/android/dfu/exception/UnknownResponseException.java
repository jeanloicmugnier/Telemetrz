package no.nordicsemi.android.dfu.exception;

import android.support.v4.view.MotionEventCompat;

public class UnknownResponseException extends Exception {
    private static final char[] HEX_ARRAY;
    private static final long serialVersionUID = -8716125467309979289L;
    private final int mExpectedOpCode;
    private final byte[] mResponse;

    static {
        HEX_ARRAY = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    }

    public UnknownResponseException(String message, byte[] response, int expectedOpCode) {
        super(message);
        if (response == null) {
            response = new byte[0];
        }
        this.mResponse = response;
        this.mExpectedOpCode = expectedOpCode;
    }

    public String getMessage() {
        return String.format("%s (response: %s, expected: 0x10%02X..)", new Object[]{super.getMessage(), bytesToHex(this.mResponse, 0, this.mResponse.length), Integer.valueOf(this.mExpectedOpCode)});
    }

    public static String bytesToHex(byte[] bytes, int start, int length) {
        if (bytes == null || bytes.length <= start || length <= 0) {
            return "";
        }
        int maxLength = Math.min(length, bytes.length - start);
        char[] hexChars = new char[(maxLength * 2)];
        for (int j = 0; j < maxLength; j++) {
            int v = bytes[start + j] & MotionEventCompat.ACTION_MASK;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[(j * 2) + 1] = HEX_ARRAY[v & 15];
        }
        return "0x" + new String(hexChars);
    }
}
