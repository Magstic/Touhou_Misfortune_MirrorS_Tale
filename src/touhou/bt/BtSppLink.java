package touhou.bt;

import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public final class BtSppLink {
    // Custom 128-bit UUID for Touhou replay sharing over SPP.
    private static final String UUID_HEX = "35B0C2B1A8C34D25A4E5A9C6F03F2B11";
    private static final UUID SERVICE_UUID = new UUID(UUID_HEX, false);

    private BtSppLink() {
    }

    public interface ProgressListener {
        void onProgress(int done, int total);
    }

    public interface StatusListener {
        void onStatus(int status);

        void onRemoteDevice(String addr, String name);
    }

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_SELECT_SERVICE = 1;
    public static final int STATUS_CONNECTING = 2;
    public static final int STATUS_CONNECTED = 3;
    public static final int STATUS_WAITING = 4;
    public static final int STATUS_SENDING = 5;
    public static final int STATUS_RECEIVING = 6;
    public static final int STATUS_DONE = 7;
    public static final int STATUS_CANCELED = 8;

    public static final class CancelRef {
        private volatile boolean canceled;
        private volatile StreamConnectionNotifier sn;
        private volatile StreamConnection c;

        public void cancel() {
            canceled = true;
            StreamConnectionNotifier x = sn;
            if (x != null) {
                try {
                    x.close();
                } catch (Throwable ignore) {
                }
            }

            StreamConnection y = c;
            if (y != null) {
                try {
                    y.close();
                } catch (Throwable ignore) {
                }
            }
        }

        private boolean isCanceled() {
            return canceled;
        }
    }

    public static boolean isSupported() {
        try {
            LocalDevice.getLocalDevice();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void sendPacket(byte[] packet) throws Exception {
        sendPacket(packet, null, null, null);
    }

    public static void sendPacket(byte[] packet, ProgressListener progress, CancelRef cancel, StatusListener status) throws Exception {
        if (packet == null) {
            throw new IllegalArgumentException("packet");
        }

        if (cancel != null && cancel.isCanceled()) {
            if (status != null) {
                status.onStatus(STATUS_CANCELED);
            }
            throw new Exception("canceled");
        }

        if (status != null) {
            status.onStatus(STATUS_SELECT_SERVICE);
        }

        String url = selectServiceUrl();
        if (url == null || url.length() == 0) {
            throw new Exception("no service");
        }

        if (cancel != null && cancel.isCanceled()) {
            if (status != null) {
                status.onStatus(STATUS_CANCELED);
            }
            throw new Exception("canceled");
        }

        StreamConnection c = null;
        OutputStream out = null;
        try {
            if (status != null) {
                status.onStatus(STATUS_CONNECTING);
            }
            c = (StreamConnection) Connector.open(url);
            if (cancel != null) {
                cancel.c = c;
            }

            if (status != null) {
                status.onStatus(STATUS_CONNECTED);
                reportRemoteDevice(status, c);
            }
            out = c.openOutputStream();

            byte[] len4 = new byte[4];
            writeI32LE(len4, 0, packet.length);

            int total = packet.length + 4;
            if (progress != null) {
                progress.onProgress(0, total);
            }

            if (status != null) {
                status.onStatus(STATUS_SENDING);
            }

            if (!writeFully(out, len4, 0, 4, progress, cancel, 0, total)) {
                if (status != null) {
                    status.onStatus(STATUS_CANCELED);
                }
                throw new Exception("canceled");
            }
            if (!writeFully(out, packet, 0, packet.length, progress, cancel, 4, total)) {
                if (status != null) {
                    status.onStatus(STATUS_CANCELED);
                }
                throw new Exception("canceled");
            }
            out.flush();

            if (status != null) {
                status.onStatus(STATUS_DONE);
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Throwable ignore) {
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static byte[] waitAndReceivePacket() throws Exception {
        return waitAndReceivePacket(null, null, null);
    }

    public static byte[] waitAndReceivePacket(ProgressListener progress, CancelRef cancel) throws Exception {
        return waitAndReceivePacket(progress, cancel, null);
    }

    public static byte[] waitAndReceivePacket(ProgressListener progress, CancelRef cancel, StatusListener status) throws Exception {
        StreamConnectionNotifier sn = null;
        StreamConnection c = null;
        InputStream in = null;
        try {
            try {
                LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
            } catch (Throwable ignore) {
            }

            if (status != null) {
                status.onStatus(STATUS_WAITING);
            }

            String url = "btspp://localhost:" + UUID_HEX + ";name=THReplay;authenticate=false;encrypt=false";
            sn = (StreamConnectionNotifier) Connector.open(url);
            if (cancel != null) {
                cancel.sn = sn;
                if (cancel.isCanceled()) {
                    if (status != null) {
                        status.onStatus(STATUS_CANCELED);
                    }
                    return null;
                }
            }

            try {
                c = sn.acceptAndOpen();
            } catch (Throwable t) {
                if (cancel != null && cancel.isCanceled()) {
                    if (status != null) {
                        status.onStatus(STATUS_CANCELED);
                    }
                    return null;
                }
                if (t instanceof Exception) {
                    throw (Exception) t;
                }
                throw new Exception(String.valueOf(t));
            }
            if (cancel != null) {
                cancel.c = c;
            }
            if (cancel != null && cancel.isCanceled()) {
                if (status != null) {
                    status.onStatus(STATUS_CANCELED);
                }
                return null;
            }

            if (status != null) {
                status.onStatus(STATUS_CONNECTED);
                reportRemoteDevice(status, c);
            }

            in = c.openInputStream();

            byte[] len4 = new byte[4];
            if (status != null) {
                status.onStatus(STATUS_RECEIVING);
            }
            if (!readFully(in, len4, 0, 4, progress, cancel, 0, 4)) {
                if (cancel != null && cancel.isCanceled()) {
                    if (status != null) {
                        status.onStatus(STATUS_CANCELED);
                    }
                    return null;
                }
                throw new Exception("eof");
            }
            int len = readI32LE(len4, 0);
            if (len <= 0 || len > (128 * 1024)) {
                throw new Exception("invalid len");
            }

            if (progress != null) {
                progress.onProgress(0, len);
            }

            byte[] data = new byte[len];
            if (!readFully(in, data, 0, len, progress, cancel, 0, len)) {
                if (cancel != null && cancel.isCanceled()) {
                    if (status != null) {
                        status.onStatus(STATUS_CANCELED);
                    }
                    return null;
                }
                throw new Exception("eof");
            }

            if (status != null) {
                status.onStatus(STATUS_DONE);
            }
            return data;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignore) {
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (Throwable ignore) {
                }
            }
            if (sn != null) {
                try {
                    sn.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static void reportRemoteDevice(StatusListener status, StreamConnection c) {
        if (status == null || c == null) {
            return;
        }
        try {
            RemoteDevice rd = RemoteDevice.getRemoteDevice(c);
            if (rd == null) {
                return;
            }
            String addr = rd.getBluetoothAddress();
            String name = null;
            try {
                name = rd.getFriendlyName(false);
            } catch (Throwable ignore) {
                name = null;
            }
            status.onRemoteDevice(addr, name);
        } catch (Throwable ignore) {
        }
    }

    private static String selectServiceUrl() throws BluetoothStateException {
        LocalDevice ld = LocalDevice.getLocalDevice();
        DiscoveryAgent da = ld.getDiscoveryAgent();

        // Let the platform handle device/service selection UI.
        return da.selectService(SERVICE_UUID, ServiceSecurity.NOAUTH_NOENC, false);
    }

    private static final class ServiceSecurity {
        private static final int NOAUTH_NOENC = 0;
    }

    private static boolean readFully(InputStream in, byte[] b, int off, int len,
            ProgressListener progress, CancelRef cancel,
            int progressBase, int progressTotal) throws Exception {
        int done = 0;
        while (len > 0) {
            if (cancel != null && cancel.isCanceled()) {
                return false;
            }
            int n = in.read(b, off, len);
            if (n < 0) {
                return false;
            }
            off += n;
            len -= n;
            done += n;
            if (progress != null && progressTotal > 0) {
                progress.onProgress(progressBase + done, progressTotal);
            }
        }
        return true;
    }

    private static boolean writeFully(OutputStream out, byte[] b, int off, int len,
            ProgressListener progress, CancelRef cancel,
            int progressBase, int progressTotal) throws Exception {
        int done = 0;
        while (len > 0) {
            if (cancel != null && cancel.isCanceled()) {
                return false;
            }
            int n = (len > 512) ? 512 : len;
            out.write(b, off, n);
            off += n;
            len -= n;
            done += n;
            if (progress != null && progressTotal > 0) {
                progress.onProgress(progressBase + done, progressTotal);
            }
        }
        return true;
    }

    private static int readI32LE(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16) | ((b[p + 3] & 0xFF) << 24);
    }

    private static void writeI32LE(byte[] b, int p, int v) {
        b[p] = (byte) (v & 0xFF);
        b[p + 1] = (byte) ((v >> 8) & 0xFF);
        b[p + 2] = (byte) ((v >> 16) & 0xFF);
        b[p + 3] = (byte) ((v >> 24) & 0xFF);
    }
}
