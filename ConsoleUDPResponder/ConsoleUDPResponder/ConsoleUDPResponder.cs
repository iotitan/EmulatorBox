/**
 * File: ConsoleUDPResponder.cs
 * Author: Matt Jones
 * Date: 2019.09.03
 * Desc: Respond to UDP connections with information about the console (name, IP, etc.).
 */

using System;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace ConsoleUDPResponder {
    static class ConsoleUDPResponder {

        private static readonly String MAGIC_PREFIX = "!ConsoleInfo:";
        private static readonly String SEPARATOR = "|";

        [STAThread]
        static void Main(String[] args) {
            int port = 19002;
            if (args.Length > 0) port = Int32.Parse(args[0]);

            UdpClient socket = new UdpClient(port);

            // This object will be populated with the sender's info when a connection is established.
            IPEndPoint senderInfo = new IPEndPoint(IPAddress.Any, 0);

            // Keep listening for incoming info requests until the machine turns off.
            while (true) {
                String broadcastData = Encoding.UTF8.GetString(socket.Receive(ref senderInfo));

                byte[] responseBytes = Encoding.UTF8.GetBytes(
                        MAGIC_PREFIX + SEPARATOR + Environment.MachineName + SEPARATOR);

                socket.Send(responseBytes, responseBytes.Length, new IPEndPoint(senderInfo.Address, 19003));

            }
        }
    }
}
