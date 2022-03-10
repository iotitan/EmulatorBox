/**
 * Copyright 2019 Matthew Jones
 *
 * File: ConsoleUDPResponder.cs
 * Author: Matt Jones
 * Date: 2019.09.03
 * Desc: Respond to UDP connections with information about the console (name, IP, etc.). This
 *       system is built single-threaded, so it only handles one request at a time, which should
 *       be plenty for this use case.
 *       
 *       Message format:
 *       
 *       magic_string|current_time_ms|machine_name|base_64_message
 *       
 *       Example:
 *       
 *       !!ConsoleMessage:|1567889516854|TVBox|SW5mbw==|
 */

using System;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace ConsoleUDPResponder {
    static class ConsoleUDPResponder {
        /** The default port to send and recieve messages on. */
        private static readonly int DEFAULT_PORT = 19002;

        // Different actions this host knows how to handle.
        private static readonly String ACTION_INFO = "INFO";
        private static readonly String ACTION_POWER_OFF = "POWER_OFF";
        private static readonly String ACTION_EMULATION_STATION = "RESTART_EMULATION_STATION";
        private static readonly String ACTION_STEAM = "RESTART_STEAM_BP";
        private static readonly String ACTION_HOME = "HOME";

        private static readonly String RESPONSE_OK = "OK";

        /** The allowed latency for a message to be considered valid by the system. */
        private static readonly long VALID_MESSAGE_LATENCY_MS = 3000;

        /** A magic string to identify messages using this simple protocol. */
        private static readonly String MAGIC_PREFIX = "!!ConsoleMessage:";

        /** A delimited for individual message parts. */
        private static readonly char SEPARATOR = '|';

        /** The start time for many systems counting MS. */
        private static readonly DateTime EPOCH_1970 = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        [STAThread]
        static void Main(String[] args) {
            int port = DEFAULT_PORT;
            if (args.Length > 0) port = Int32.Parse(args[0]);

            UdpClient socket = new UdpClient(port);

            // This object will be populated with the sender's info when a connection is established.
            IPEndPoint senderInfo = new IPEndPoint(IPAddress.Any, 0);

            IPAddress[] localAddresses = Dns.GetHostAddresses(Dns.GetHostName());

            // Keep listening for incoming info requests until the machine turns off.
            while (true) {
                String incomingMessage = Encoding.UTF8.GetString(socket.Receive(ref senderInfo));
                String response = handleMessage(incomingMessage);

                if (response != null) {
                    byte[] responseBytes = buildMessage(response);
                    socket.Send(responseBytes, responseBytes.Length,
                            new IPEndPoint(senderInfo.Address, port));
                }

            }
        }

        /** @return The current time in ms since 1970/01/01. */
        private static long getCurrentTimeMs() {
            return (long) Math.Floor((DateTime.UtcNow - EPOCH_1970).TotalMilliseconds);
        }

        /**
         * Build a new message to send over the network.
         * @param message The message to append. This will be converted to a base 64 string.
         * @return The message in bytes.
         */
        private static byte[] buildMessage(String message) {
            StringBuilder builder = new StringBuilder();
            builder.Append(MAGIC_PREFIX);
            builder.Append(SEPARATOR);
            builder.Append(getCurrentTimeMs());
            builder.Append(SEPARATOR);
            builder.Append(Environment.MachineName);
            builder.Append(SEPARATOR);
            builder.Append(Convert.ToBase64String(Encoding.UTF8.GetBytes(message)));
            return Encoding.UTF8.GetBytes(builder.ToString());
        }

        /**
         * Parse a message if it is valid.
         * @param message The message being parsed.
         * @param A response to send back if any.
         */
        private static string handleMessage(String message) {
            String[] parts = message.Split(SEPARATOR);

            // Make sure the message is intended for this system.
            if (!MAGIC_PREFIX.Equals(parts[0])) return null;

            // Make sure we're not getting an old message.
            try {
                long remoteTime = long.Parse(parts[1]);
                long curTime = getCurrentTimeMs();
                long timeDiff = getCurrentTimeMs() - long.Parse(parts[1]);
                if (timeDiff > Math.Abs(VALID_MESSAGE_LATENCY_MS)) {
                    // TODO(Matt): Time skew causes some real issues here. Use UDP to find the
                    //             console then switch to TCP.
                    // return null;
                }
            } catch (Exception) {
                // If we failed to parse the time piece of the message, do nothing.
                return null;
            }

            String decodedAction = Encoding.UTF8.GetString(Convert.FromBase64String(parts[3]));
            if (ACTION_HOME.Equals(decodedAction)) {
                runScript("node.exe "
                    + "C:/emulator_box/EmulatorBox/scripts/KillGames.js "
                    + "C:/emulator_box/EmulatorBox/configs");
            } else if (ACTION_EMULATION_STATION.Equals(decodedAction)) {
                runScript("node.exe "
                    + "C:/emulator_box/EmulatorBox/scripts/RestartEmulationStation.js "
                    + "C:/emulator_box/EmulatorBox/configs");
            } else if (ACTION_STEAM.Equals(decodedAction)) {
                runScript("node.exe "
                    + "C:/emulator_box/EmulatorBox/scripts/RestartSteamBP.js "
                    + "C:/emulator_box/EmulatorBox/configs");
            } else if (ACTION_POWER_OFF.Equals(decodedAction)) {
                runScript("node.exe "
                    + "C:/emulator_box/EmulatorBox/scripts/PowerOff.js "
                    + "C:/emulator_box/EmulatorBox/configs");
            } else if (ACTION_INFO.Equals(decodedAction)) {
                // Intentionally do nothing for this command.
            }

            return RESPONSE_OK;
        }

        /**
         * Run a script via RunProgramSilent.exe.
         * @param args The arguments to pass to the script runner.
         */
        private static void runScript(String args) {
            ProcessStartInfo info = new ProcessStartInfo();
            info.FileName = "C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe";
            info.WindowStyle = ProcessWindowStyle.Hidden;
            info.Arguments = args;

            Process nodeProcess = new Process();
            nodeProcess.StartInfo = info;
            nodeProcess.Start();
        }
    }
}
