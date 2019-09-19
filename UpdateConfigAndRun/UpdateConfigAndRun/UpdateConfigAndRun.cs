/**
 * Copyright 2019 Matthew Jones
 *
 * File: WriteConfig.cs
 * Author: Matt Jones
 * Date: 2019.09.18
 * Desc: Write the config for emulators based on the number and type of controllers.
 * 
 * Usage: UpdateConfigAndRun.exe <EMULATOR_ID> <EMULATOR_PATH> [<PARAMS>]
 * 
 * ==========================================================
 * NOTE: This currently only works for N64 controller counts.
 * ==========================================================
 */

using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;

namespace UpdateConfigAndRun {
    static class UpdateConfigAndRun {
        /** The root directory of the emulator systems. */
        private static readonly String BASE_PATH = "C:/emulator_box";

        /** The maximum number of controllers that are tested for. */
        private static readonly int MAX_CONTROLLERS = 4;

        /** Possible controller types. */
        private enum ControllerType {
            NONE = 0,
            XBOX = 1,
            N64 = 2,
            OTHER = 3
        }

        static void Main(string[] args) {
            ControllerType[] controllerTypes = new ControllerType[MAX_CONTROLLERS];
            for (int i = 0; i < MAX_CONTROLLERS; i++) {
                if (isXboxLike(i)) {
                    controllerTypes[i] = ControllerType.XBOX;
                } else if (isN64Like(i)) {
                    controllerTypes[i] = ControllerType.N64;
                } else if (GamePad.GetState(i).IsConnected) {
                    controllerTypes[i] = ControllerType.OTHER;
                } else {
                    controllerTypes[i] = ControllerType.NONE;
                }
            }

            // First argument should be the system type.
            if (args.Length < 2) return;
            if (args[1].Equals("n64")) writeProject64Config(controllerTypes);

            List<String> procArgs = new List<String>();
            for (int i = 2; i < args.Length; i++) procArgs.Add(args[i]);

            startProcess(procArgs);
        }

        /**
         * Start a process with the provided args.
         * @param procArgs The list of arguments to start the process with index 0 being the
         *                 process name.
         */
        static void startProcess(List<String> procArgs) {
            // The args must at least contain an executable to run.
            if (procArgs.Count < 1) return;

            ProcessStartInfo info = new ProcessStartInfo();
            info.FileName = procArgs[0];

            // Apply all provided params after 1 to the program that will be run.
            for (int i = 1; i < procArgs.Count; i++) info.Arguments += " \"" + procArgs[i] + "\"";

            Process proc = new Process();
            proc.StartInfo = info;
            proc.Start();
        }

        /**
         * Write the Project64 config for the controller count.
         * @param pluggedControllers The list of controller types currently plugged in.
         */
        private static void writeProject64Config(ControllerType[] pluggedControllers) {
            String configTemplate = BASE_PATH + "/EmulatorBox/configs/Project64/Config/NRage.ini";
            String configPath = BASE_PATH + "/emulators/Project64-2.3/Config/NRage.ini";

            if (File.Exists(configPath)) File.Delete(configPath);
            StreamReader reader = new StreamReader(configTemplate);
            StreamWriter writer = new StreamWriter(configPath);
            int pluggedIndex = 0;
            String line;
            while ((line = reader.ReadLine()) != null) {
                if (line.Contains("Plugged=")) {

                    if (pluggedControllers[pluggedIndex] == ControllerType.XBOX
                            || pluggedControllers[pluggedIndex] == ControllerType.N64) {
                        writer.WriteLine("Plugged=1");
                    } else {
                        writer.WriteLine("Plugged=0");
                    }
                    pluggedIndex++;
                } else {
                    writer.WriteLine(line);
                }
            }
            reader.Close();
            writer.Close();
        }

        /**
         * Determine if a controller is XBox-like by expecting at least 6 axes and 12 or more
         * buttons.
         * @param index The index of the controller.
         * @return Whether the controller is XBox-like.
         */
        private static bool isXboxLike(int index) {
            if (!GamePad.GetState(index).IsConnected) return false;

            JoystickCapabilities cap = Joystick.GetCapabilities(index);
            return cap.AxisCount >= 6 && cap.ButtonCount >= 12;
        }

        /**
         * Determine if a controller is N64-like by expecting 4 axes and 10 or more buttons.
         * @param index The index of the controller.
         * @return Whether the controller is N64-like.
         */
        private static bool isN64Like(int index) {
            if (!GamePad.GetState(index).IsConnected) return false;

            JoystickCapabilities cap = Joystick.GetCapabilities(index);
            return cap.AxisCount == 4 && cap.ButtonCount >= 10;
        }
    }
}
