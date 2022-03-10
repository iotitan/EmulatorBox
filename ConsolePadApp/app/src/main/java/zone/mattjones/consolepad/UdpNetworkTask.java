/**
 * Copyright 2019 Matthew Jones
 *
 * File: UdpNetworkTask.java
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

import androidx.annotation.StringRes;

public class UdpNetworkTask extends AsyncTask<String, Integer, String> {
    /** Interface for handling messages from the console. */
    public interface ResponseHandler {
        /**
         * Handle an incoming message.
         * @param messageParts The message response, if available, already split into its different
         *                     components.
         * @param error Whether there was an error with the response.
         * @param errorMessageId The string ID for the error message if there was one.
         * @param remoteIp The IP address of the host console.
         */
        void handleResponse(ArrayList<String> messageParts, boolean error,
                            @StringRes int errorMessageId, String remoteIp);
    }

    /** The default port to send and receive messages on. */
    private static final int DEFAULT_PORT = 19002;

    // Different actions this host knows how to handle.
    public static final String ACTION_INFO = "INFO";
    public static final String ACTION_POWER_OFF = "POWER_OFF";
    public static final String ACTION_EMULATION_STATION = "RESTART_EMULATION_STATION";
    public static final String ACTION_STEAM = "RESTART_STEAM_BP";
    public static final String ACTION_HOME = "HOME";

    public static final String RESPONSE_OK = "OK";

    /** The max amount of data the socket is willing to read from the console. */
    public static final int MAX_PACKET_SIZE = 4096;

    /** The allowed time to wait for a message from the console. */
    public static final long SOCKET_TIMEOUT_MS = 3000;

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
                mSocket.receive(receivedPacket);

                // Test if the response is an echo from broadcast.
                boolean isEcho = false;
                for (String ip : myIps) {
                    isEcho = isEcho || receivedPacket.getAddress().toString().contains(ip);
                }
                if (isEcho) continue;

                // Make sure the packet isn't too large, otherwise reject and read the next.
                if (receivedPacket.getLength() >= MAX_PACKET_SIZE) continue;

                data = new String(receivedPacket.getData(), 0, receivedPacket.getLength(),
                        Charset.forName("UTF8"));
                String[] dataSections = data.split("\\" + SEPARATOR);

                // Collect messages until one is valid.
                if (!MAGIC_PREFIX.equals(dataSections[0])) continue;

                // If all the tests pass, send the data pieces to the handler.
                for (int i = 0; i < dataSections.length; i++) {
                    finalResponseComponents.add(dataSections[i]);
                }
                break;
            }

            mHandler.handleResponse(finalResponseComponents, false, R.string.no_error,
                    receivedPacket.getAddress().getHostAddress());

        } catch (SocketException se) {
            mHandler.handleResponse(null, true, R.string.generic_console_error, null);
        } catch (UnknownHostException ue) {
            mHandler.handleResponse(null, true, R.string.no_host_error, null);
        } catch (SocketTimeoutException se) {
            mHandler.handleResponse(null, true, R.string.response_timeout_error, null);
        } catch (IOException ie) {
            mHandler.handleResponse(null, true, R.string.generic_console_error, null);
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
