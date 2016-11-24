package no.nordicsemi.android.dfu;

import java.io.IOException;
import java.util.zip.ZipInputStream;

public class ZipHexInputStream  {
    //extends ZipInputStream
    private static final String APPLICATION_NAME = "application.hex";
    private static final String BOOTLOADER_NAME = "bootloader.hex";
    private static final String SOFTDEVICE_NAME = "softdevice.hex";
    private byte[] applicationBytes;
    private int applicationSize;
    private byte[] bootloaderBytes;
    private int bootloaderSize;
    private int bytesRead;
    private int bytesReadFromCurrentSource;
    private byte[] currentSource;
    private byte[] softDeviceBytes;
    private int softDeviceSize;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump.
    public ZipHexInputStream(java.io.InputStream r14, int r15, int r16) throws java.io.IOException {
        super(this);
        /*
        r13 = this;
        r13.<init>(r14);
        r11 = 0;
        r13.bytesRead = r11;
        r11 = 0;
        r13.bytesReadFromCurrentSource = r11;
    L_0x0009:
        r10 = r13.getNextEntry();	 Catch:{ all -> 0x0039 }
        if (r10 == 0) goto L_0x00ba;
    L_0x000f:
        r6 = r10.getName();	 Catch:{ all -> 0x0039 }
        r11 = "softdevice.hex";
        r8 = r11.matches(r6);	 Catch:{ all -> 0x0039 }
        r11 = "bootloader.hex";
        r2 = r11.matches(r6);	 Catch:{ all -> 0x0039 }
        r11 = "application.hex";
        r0 = r11.matches(r6);	 Catch:{ all -> 0x0039 }
        r11 = r10.isDirectory();	 Catch:{ all -> 0x0039 }
        if (r11 != 0) goto L_0x0031;
    L_0x002b:
        if (r8 != 0) goto L_0x003e;
    L_0x002d:
        if (r2 != 0) goto L_0x003e;
    L_0x002f:
        if (r0 != 0) goto L_0x003e;
    L_0x0031:
        r11 = new java.io.IOException;	 Catch:{ all -> 0x0039 }
        r12 = "ZIP content not supported. Only softdevice.hex, bootloader.hex or application.hex are allowed.";
        r11.<init>(r12);	 Catch:{ all -> 0x0039 }
        throw r11;	 Catch:{ all -> 0x0039 }
    L_0x0039:
        r11 = move-exception;
        super.close();
        throw r11;
    L_0x003e:
        if (r16 == 0) goto L_0x0052;
    L_0x0040:
        if (r8 == 0) goto L_0x0046;
    L_0x0042:
        r11 = r16 & 1;
        if (r11 == 0) goto L_0x0009;
    L_0x0046:
        if (r2 == 0) goto L_0x004c;
    L_0x0048:
        r11 = r16 & 2;
        if (r11 == 0) goto L_0x0009;
    L_0x004c:
        if (r0 == 0) goto L_0x0052;
    L_0x004e:
        r11 = r16 & 4;
        if (r11 == 0) goto L_0x0009;
    L_0x0052:
        r1 = new java.io.ByteArrayOutputStream;	 Catch:{ all -> 0x0039 }
        r1.<init>();	 Catch:{ all -> 0x0039 }
        r11 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r3 = new byte[r11];	 Catch:{ all -> 0x0039 }
    L_0x005b:
        r5 = super.read(r3);	 Catch:{ all -> 0x0039 }
        r11 = -1;
        if (r5 == r11) goto L_0x0067;
    L_0x0062:
        r11 = 0;
        r1.write(r3, r11, r5);	 Catch:{ all -> 0x0039 }
        goto L_0x005b;
    L_0x0067:
        r4 = r1.toByteArray();	 Catch:{ all -> 0x0039 }
        r9 = 0;
        r7 = new no.nordicsemi.android.dfu.HexInputStream;	 Catch:{ all -> 0x0039 }
        r7.<init>(r4, r15);	 Catch:{ all -> 0x0039 }
        if (r8 == 0) goto L_0x0088;
    L_0x0073:
        r11 = r7.available();	 Catch:{ all -> 0x0039 }
        r13.softDeviceSize = r11;	 Catch:{ all -> 0x0039 }
        r9 = new byte[r11];	 Catch:{ all -> 0x0039 }
        r13.softDeviceBytes = r9;	 Catch:{ all -> 0x0039 }
        r11 = r13.softDeviceBytes;	 Catch:{ all -> 0x0039 }
        r7.read(r11);	 Catch:{ all -> 0x0039 }
        r13.currentSource = r9;	 Catch:{ all -> 0x0039 }
    L_0x0084:
        r7.close();	 Catch:{ all -> 0x0039 }
        goto L_0x0009;
    L_0x0088:
        if (r2 == 0) goto L_0x00a2;
    L_0x008a:
        r11 = r7.available();	 Catch:{ all -> 0x0039 }
        r13.bootloaderSize = r11;	 Catch:{ all -> 0x0039 }
        r9 = new byte[r11];	 Catch:{ all -> 0x0039 }
        r13.bootloaderBytes = r9;	 Catch:{ all -> 0x0039 }
        r11 = r13.bootloaderBytes;	 Catch:{ all -> 0x0039 }
        r7.read(r11);	 Catch:{ all -> 0x0039 }
        r11 = r13.currentSource;	 Catch:{ all -> 0x0039 }
        r12 = r13.applicationBytes;	 Catch:{ all -> 0x0039 }
        if (r11 != r12) goto L_0x0084;
    L_0x009f:
        r13.currentSource = r9;	 Catch:{ all -> 0x0039 }
        goto L_0x0084;
    L_0x00a2:
        if (r0 == 0) goto L_0x0084;
    L_0x00a4:
        r11 = r7.available();	 Catch:{ all -> 0x0039 }
        r13.applicationSize = r11;	 Catch:{ all -> 0x0039 }
        r9 = new byte[r11];	 Catch:{ all -> 0x0039 }
        r13.applicationBytes = r9;	 Catch:{ all -> 0x0039 }
        r11 = r13.applicationBytes;	 Catch:{ all -> 0x0039 }
        r7.read(r11);	 Catch:{ all -> 0x0039 }
        r11 = r13.currentSource;	 Catch:{ all -> 0x0039 }
        if (r11 != 0) goto L_0x0084;
    L_0x00b7:
        r13.currentSource = r9;	 Catch:{ all -> 0x0039 }
        goto L_0x0084;
    L_0x00ba:
        super.close();
        return;

        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.ZipHexInputStream.<init>(java.io.InputStream, int, int):void");
    }

    public void close() throws IOException {
        this.softDeviceBytes = null;
        this.bootloaderBytes = null;
        this.softDeviceBytes = null;
        this.applicationSize = 0;
        this.bootloaderSize = 0;
        this.softDeviceSize = 0;
        this.currentSource = null;
        this.bytesReadFromCurrentSource = 0;
        this.bytesRead = 0;
        super.close();
    }

    public int read(byte[] buffer) throws IOException {
        int size;
        int maxSize = this.currentSource.length - this.bytesReadFromCurrentSource;
        if (buffer.length <= maxSize) {
            size = buffer.length;
        } else {
            size = maxSize;
        }
        System.arraycopy(this.currentSource, this.bytesReadFromCurrentSource, buffer, 0, size);
        this.bytesReadFromCurrentSource += size;
        if (buffer.length > size) {
            if (startNextFile() == null) {
                this.bytesRead += size;
                return size;
            }
            int nextSize;
            maxSize = this.currentSource.length;
            if (buffer.length - size <= maxSize) {
                nextSize = buffer.length - size;
            } else {
                nextSize = maxSize;
            }
            System.arraycopy(this.currentSource, 0, buffer, size, nextSize);
            this.bytesReadFromCurrentSource += nextSize;
            size += nextSize;
        }
        this.bytesRead += size;
        return size;
    }

    public int getContentType() {
        byte b = (byte) 0;
        if (this.softDeviceSize > 0) {
            b = (byte) 1;
        }
        if (this.bootloaderSize > 0) {
            b = (byte) (b | 2);
        }
        if (this.applicationSize > 0) {
            return (byte) (b | 4);
        }
        return b;
    }

    public int setContentType(int type) {
        if (this.bytesRead > 0) {
            throw new UnsupportedOperationException("Content type must not be change after reading content");
        }
        int t = getContentType() & type;
        if ((t & 1) == 0) {
            this.softDeviceBytes = null;
            this.softDeviceSize = 0;
        }
        if ((t & 2) == 0) {
            this.bootloaderBytes = null;
            this.bootloaderSize = 0;
        }
        if ((t & 4) == 0) {
            this.applicationBytes = null;
            this.applicationSize = 0;
        }
        return t;
    }

    private byte[] startNextFile() {
        byte[] ret;
        if (this.currentSource == this.softDeviceBytes && this.bootloaderBytes != null) {
            ret = this.bootloaderBytes;
            this.currentSource = ret;
        } else if (this.currentSource == this.applicationBytes || this.applicationBytes == null) {
            ret = null;
            this.currentSource = null;
        } else {
            ret = this.applicationBytes;
            this.currentSource = ret;
        }
        this.bytesReadFromCurrentSource = 0;
        return ret;
    }

    public int available() {
        return ((this.softDeviceSize + this.bootloaderSize) + this.applicationSize) - this.bytesRead;
    }

    public int softDeviceImageSize() {
        return this.softDeviceSize;
    }

    public int bootloaderImageSize() {
        return this.bootloaderSize;
    }

    public int applicationImageSize() {
        return this.applicationSize;
    } */
}
