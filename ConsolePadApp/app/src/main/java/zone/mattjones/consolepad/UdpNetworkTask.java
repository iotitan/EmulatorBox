/**
 * File: UdpNetworkTask.js
 * Author: Matt Jones
 * Date: 2019.09.07
 * Desc: An async task that sends a message over UDP and waits for a response.
 */

package zone.mattjones.consolepad;

import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;

public class UdpNetworkTask extends AsyncTask<String, Integer, String> {
    /** Interface for handling messages from the console. */
    public interface ResponseHandler {
        /**
         * Handle an incoming message.
         * @param messageParts The message response, if available, already split into its different
         *                     components.
         * @param error Whether there was an error with the response.
         * @param timedOut Whether the socket timed out waiting for a valid response.
         */
        void handleResponse(ArrayList<String> messageParts, boolean error, boolean timedOut);
    }

    /** The default port to send and receive messages on. */
    private static final int DEFAULT_PORT = 19002;

    // Different actions this host knows how to handle.
    public static final String ACTION_INFO = "Info";
    public static final String ACTION_POWER_OFF = "PowerOff";
    public static final String ACTION_EMULATION_STATION = "RestartEmulationStation";
    public static final String ACTION_STEAM = "RestartSteamBP";
    public static final String ACTION_HOME = "KillEmulators";

    public static final String RESPONSE_OK = "OK";

    /** The max amount of data the socket is willing to read from the console. */
    public static final int MAX_PACKET_SIZE = 4096;

    /** The allowed time to wait for a message from the console. */
    public static final long SOCKET_TIMEOUT_MS = 500;

    /** The allowed latency for a message to be considered valid by the system. */
    public static final long VALID_MESSAGE_LATENCY_MS = 3000;

    /** A magic string to identify messages using this simple protocol. */
    public static final String MAGIC_PREFIX = "!!ConsoleMessage:";

    /** A delimited for individual message parts. */
    public static final String SEPARATOR = "|";

    /** The IP used to broadcast messages over UDP. */
    public static final String BROADCAST_IP = "255.255.255.255";

    /** The IP to send messages to. */
    private String mRemoteIp;

    /** The message to send to the host. */
    private String mMessage;

    /** The object responsible for handing responses from the console. */
    private ResponseHandler mHandler;

    /** The UDP socket used to send and receive information. */
    private DatagramSocket mSocket;

    public UdpNetworkTask(ResponseHandler handler, String targetIp, String message) {
        mRemoteIp = targetIp;
        mHandler = handler;
        mMessage = message;
    }

    /**
     * Get a list of IP addresses for this device.
     * @return A list of IP addressed in string form.
     * @throws SocketException
     */
    private static ArrayList<String> getDeviceIps() throws SocketException {
        ArrayList<String> outList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface curInterface = interfaces.nextElement();
            if (curInterface.isLoopback()) continue;
            Enumeration<InetAddress> addrs = curInterface.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress curAddr = addrs.nextElement();
                if (curAddr.isLoopbackAddress() && !curAddr.isLinkLocalAddress()) continue;
                outList.add(curAddr.getHostAddress());
            }

        }
        return outList;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            mSocket = new DatagramSocket(DEFAULT_PORT);
            mSocket.setSoTimeout((int) SOCKET_TIMEOUT_MS);

            // First send the message to the console.
            if (BROADCAST_IP.equals(mRemoteIp)) mSocket.setBroadcast(true);

            ArrayList<String> myIps = getDeviceIps();
            byte[] message = buildMessage(mMessage);
            mSocket.send(new DatagramPacket(
                    message, message.length, new InetSocketAddress(mRemoteIp, DEFAULT_PORT)));

            // Now wait for a response.
            String data = null;
            ArrayList<String> finalResponseComponents = new ArrayList<>();
            byte[] sharedPacketBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket receivedPacket = new DatagramPacket(sharedPacketBuffer, MAX_PACKET_SIZE);
            while (true) {
                // Clean out the shared buffer.
                for (int i = 0; i < sharedPacketBuffer.length; i++) sharedPacketBuffer[i] = 0;

                mSocket.receive(receivedPacket);

                // Test if the response is an echo from broadcast.
                boolean isEcho = false;
                for (String ip : myIps) {
                    isEcho = isEcho || receivedPacket.getAddress().toString().contains(ip);
                }
                if (isEcho) continue;


                data = new String(receivedPacket.getData(), Charset.forName("UTF8"));
                String[] dataSections = data.split("\\" + SEPARATOR);

                // Collect messages until one is valid.
                if (!MAGIC_PREFIX.equals(dataSections[0])) continue;

                // Make sure the message isn't too old.
                try {
                    long remoteTime = Long.parseLong(dataSections[1]);
                    long curTime = System.currentTimeMillis();
                    long diff = Math.abs(curTime - remoteTime);
                    if (Math.abs(curTime - remoteTime) > VALID_MESSAGE_LATENCY_MS) continue;
                } catch (NumberFormatException ne) {
                    continue;
                }

                // If all the tests pass, send the data pieces to the handler without the tail of
                // the packet (all empty info).
                for (int i = 0; i < dataSections.length - 1; i++) {
                    finalResponseComponents.add(dataSections[i]);
                }
                break;
            }

            mHandler.handleResponse(finalResponseComponents, false, false);

        } catch (SocketException se) {
            mHandler.handleResponse(null, true, false);
        } catch (UnknownHostException ue) {
            mHandler.handleResponse(null, true, false);
        } catch (SocketTimeoutException se) {
            mHandler.handleResponse(null, true, true);
        } catch (IOException ie) {
            mHandler.handleResponse(null, true, false);
        }

        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }

        return null;
    }

    /**
     * Build a new message to send over the network.
     * @param message The message to append. This will be converted to a base 64 string.
     * @return The message in bytes.
     */
    private static byte[] buildMessage(String message) {
        StringBuilder builder = new StringBuilder();
        builder.append(MAGIC_PREFIX);
        builder.append(SEPARATOR);
        builder.append(System.currentTimeMillis());
        builder.append(SEPARATOR);
        builder.append(Build.DEVICE);
        builder.append(SEPARATOR);
        builder.append(
                new String(Base64.getEncoder().encode(message.getBytes(Charset.forName("UTF8")))));
        builder.append(SEPARATOR);
        return builder.toString().getBytes(Charset.forName("UTF8"));
    }
}
