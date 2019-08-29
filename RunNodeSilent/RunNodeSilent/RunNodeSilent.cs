/**
 * File: RunNodeSilent.cs
 * Author: Matt Jones
 * Date: 2019.08.28
 * Desc: A wrapper for Node (JavaScript interpreter) that silently runs scripts (no console window).
 */

using System;
using System.Diagnostics;

namespace RunNodeSilent {
    static class RunNodeSilent {
        [STAThread]
        static void Main(String[] args) {
            // The args must contain the name of the script to run.
            if (args.Length < 1) return;

            ProcessStartInfo info = new ProcessStartInfo();
            info.FileName = "node.exe";
            info.Arguments = args[0];
            info.WindowStyle = ProcessWindowStyle.Hidden;

            Process nodeProcess = new Process();
            nodeProcess.StartInfo = info;
            nodeProcess.Start();
        }
    }
}
