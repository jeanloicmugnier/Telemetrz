package no.nordicsemi.android.dfu;

import android.support.v4.media.TransportMediator;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import no.nordicsemi.android.dfu.exception.HexFileValidationException;
import org.achartengine.tools.Zoom;

public class HexInputStream extends FilterInputStream {
    private final int LINE_LENGTH;
    private int MBRsize;
    private int available;
    private int bytesRead;
    private int lastAddress;
    private final byte[] localBuf;
    private int localPos;
    private int pos;
    private int size;

    protected HexInputStream(InputStream in, int mbrSize) throws HexFileValidationException, IOException {
        super(new BufferedInputStream(in));
        this.LINE_LENGTH = 16;
        this.localBuf = new byte[16];
        this.localPos = 16;
        this.size = this.localBuf.length;
        this.lastAddress = 0;
        this.MBRsize = mbrSize;
        this.available = calculateBinSize(mbrSize);
    }

    protected HexInputStream(byte[] data, int mbrSize) throws HexFileValidationException, IOException {
        super(new ByteArrayInputStream(data));
        this.LINE_LENGTH = 16;
        this.localBuf = new byte[16];
        this.localPos = 16;
        this.size = this.localBuf.length;
        this.lastAddress = 0;
        this.MBRsize = mbrSize;
        this.available = calculateBinSize(mbrSize);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int calculateBinSize(int r13) throws java.io.IOException {
        /*
        r12 = this;
        r1 = 0;
        r2 = r12.in;
        r10 = r2.available();
        r2.mark(r10);
        r4 = 0;
        r3 = 0;
        r0 = r2.read();	 Catch:{ all -> 0x0053 }
    L_0x0010:
        r12.checkComma(r0);	 Catch:{ all -> 0x0053 }
        r5 = r12.readByte(r2);	 Catch:{ all -> 0x0053 }
        r8 = r12.readAddress(r2);	 Catch:{ all -> 0x0053 }
        r9 = r12.readByte(r2);	 Catch:{ all -> 0x0053 }
        switch(r9) {
            case 0: goto L_0x0073;
            case 1: goto L_0x0037;
            case 2: goto L_0x0058;
            case 3: goto L_0x0022;
            case 4: goto L_0x003b;
            default: goto L_0x0022;
        };	 Catch:{ all -> 0x0053 }
    L_0x0022:
        r10 = r5 * 2;
        r10 = r10 + 2;
        r10 = (long) r10;	 Catch:{ all -> 0x0053 }
        r2.skip(r10);	 Catch:{ all -> 0x0053 }
    L_0x002a:
        r0 = r2.read();	 Catch:{ all -> 0x0053 }
        r10 = 10;
        if (r0 == r10) goto L_0x002a;
    L_0x0032:
        r10 = 13;
        if (r0 == r10) goto L_0x002a;
    L_0x0036:
        goto L_0x0010;
    L_0x0037:
        r2.reset();
    L_0x003a:
        return r1;
    L_0x003b:
        r7 = r12.readAddress(r2);	 Catch:{ all -> 0x0053 }
        if (r1 <= 0) goto L_0x004b;
    L_0x0041:
        r10 = r4 >> 16;
        r10 = r10 + 1;
        if (r7 == r10) goto L_0x004b;
    L_0x0047:
        r2.reset();
        goto L_0x003a;
    L_0x004b:
        r4 = r7 << 16;
        r10 = 2;
        r2.skip(r10);	 Catch:{ all -> 0x0053 }
        goto L_0x002a;
    L_0x0053:
        r10 = move-exception;
        r2.reset();
        throw r10;
    L_0x0058:
        r10 = r12.readAddress(r2);	 Catch:{ all -> 0x0053 }
        r6 = r10 << 4;
        if (r1 <= 0) goto L_0x006c;
    L_0x0060:
        r10 = r6 >> 16;
        r11 = r4 >> 16;
        r11 = r11 + 1;
        if (r10 == r11) goto L_0x006c;
    L_0x0068:
        r2.reset();
        goto L_0x003a;
    L_0x006c:
        r4 = r6;
        r10 = 2;
        r2.skip(r10);	 Catch:{ all -> 0x0053 }
        goto L_0x002a;
    L_0x0073:
        r3 = r4 + r8;
        if (r3 < r13) goto L_0x0022;
    L_0x0077:
        r1 = r1 + r5;
        goto L_0x0022;
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.HexInputStream.calculateBinSize(int):int");
    }

    public int available() {
        return this.available - this.bytesRead;
    }

    public int readPacket(byte[] buffer) throws HexFileValidationException, IOException {
        int i = 0;
        while (i < buffer.length) {
            int i2;
            if (this.localPos < this.size) {
                int i3 = i + 1;
                byte[] bArr = this.localBuf;
                i2 = this.localPos;
                this.localPos = i2 + 1;
                buffer[i] = bArr[i2];
                i = i3;
            } else {
                int i4 = this.bytesRead;
                i2 = readLine();
                this.size = i2;
                this.bytesRead = i4 + i2;
                if (this.size == 0) {
                    break;
                }
            }
        }
        return i;
    }

    public int read() throws IOException {
        throw new UnsupportedOperationException("Please, use readPacket() method instead");
    }

    public int read(byte[] buffer) throws IOException {
        return readPacket(buffer);
    }

    public int read(byte[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Please, use readPacket() method instead");
    }

    public int sizeInBytes() {
        return this.available;
    }

    public int sizeInPackets(int packetSize) throws IOException {
        int sizeInBytes = sizeInBytes();
        return (sizeInBytes % packetSize > 0 ? 1 : 0) + (sizeInBytes / packetSize);
    }

    private int readLine() throws IOException {
        if (this.pos == -1) {
            return 0;
        }
        InputStream in = this.in;
        while (true) {
            int b = in.read();
            this.pos++;
            if (!(b == 10 || b == 13)) {
                checkComma(b);
                int lineSize = readByte(in);
                this.pos += 2;
                int offset = readAddress(in);
                this.pos += 4;
                int type = readByte(in);
                this.pos += 2;
                int address;
                switch (type) {
                    case Zoom.ZOOM_AXIS_XY /*0*/:
                        if (this.lastAddress + offset < this.MBRsize) {
                            type = -1;
                            this.pos = (int) (((long) this.pos) + in.skip((long) ((lineSize * 2) + 2)));
                            break;
                        }
                        break;
                    case Zoom.ZOOM_AXIS_X /*1*/:
                        this.pos = -1;
                        return 0;
                    case Zoom.ZOOM_AXIS_Y /*2*/:
                        address = readAddress(in) << 4;
                        this.pos += 4;
                        if (this.bytesRead <= 0 || (address >> 16) == (this.lastAddress >> 16) + 1) {
                            this.lastAddress = address;
                            this.pos = (int) (((long) this.pos) + in.skip(2));
                            break;
                        }
                        return 0;
                    case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                        address = readAddress(in);
                        this.pos += 4;
                        if (this.bytesRead <= 0 || address == (this.lastAddress >> 16) + 1) {
                            this.lastAddress = address << 16;
                            this.pos = (int) (((long) this.pos) + in.skip(2));
                            break;
                        }
                        return 0;
                    default:
                        this.pos = (int) (((long) this.pos) + in.skip((long) ((lineSize * 2) + 2)));
                        break;
                }
                if (type == 0) {
                    int i = 0;
                    while (i < this.localBuf.length && i < lineSize) {
                        b = readByte(in);
                        this.pos += 2;
                        this.localBuf[i] = (byte) b;
                        i++;
                    }
                    this.pos = (int) (((long) this.pos) + in.skip(2));
                    this.localPos = 0;
                    return lineSize;
                }
            }
        }
    }

    public synchronized void reset() throws IOException {
        super.reset();
        this.pos = 0;
        this.bytesRead = 0;
        this.localPos = 0;
    }

    private void checkComma(int comma) throws HexFileValidationException {
        if (comma != 58) {
            throw new HexFileValidationException("Not a HEX file");
        }
    }

    private int readByte(InputStream in) throws IOException {
        int first = asciiToInt(in.read());
        return (first << 4) | asciiToInt(in.read());
    }

    private int readAddress(InputStream in) throws IOException {
        return (readByte(in) << 8) | readByte(in);
    }

    private int asciiToInt(int ascii) {
        if (ascii >= 65) {
            return ascii - 55;
        }
        if (ascii >= 48) {
            return ascii - 48;
        }
        return -1;
    }
}
