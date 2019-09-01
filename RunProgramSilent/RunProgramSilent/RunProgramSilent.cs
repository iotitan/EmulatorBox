/**
 * File: RunProgramSilent.cs
 * Author: Matt Jones
 * Date: 2019.08.28
 * Desc: A wrapper programs that hides the main window when run (no console window). This can be
 *       used to prevent the console window from appearing when running node js scripts or the main
 *       window when starting an emulator (for those that kick off a separate window).
 */

using System;
using System.Diagnostics;

namespace RunProgramSilent {
    static class RunProgramSilent {
        [STAThread]
        static void Main(String[] args) {
            // The args must at least contain an executable to run.
            if (args.Length < 1) return;

            ProcessStartInfo info = new ProcessStartInfo();
            info.FileName = args[0];
            info.WindowStyle = ProcessWindowStyle.Hidden;

            // Apply all provided params after 1 to the program that will be run.
            for (int i = 1; i < args.Length; i++) info.Arguments += " " + args[i];

            Process nodeProcess = new Process();
            nodeProcess.StartInfo = info;
            nodeProcess.Start();
        }
    }
}
