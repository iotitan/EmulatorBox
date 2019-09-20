/**
 * Copyright 2019 Matthew Jones
 *
 * File: ControllerInfo.cs
 * Author: Matt Jones
 * Date: 2019.09.19
 * Desc: Output a JSON string listing information about connected controllers.
 */

using OpenTK.Input;
using System;

namespace UpdateConfigAndRun {
    static class UpdateConfigAndRun {
        /** The maximum number of controllers that are tested for. */
        private static readonly int MAX_CONTROLLERS = 6;

        static void Main(string[] args) {
            Console.WriteLine("[");
            for (int i = 0; i < MAX_CONTROLLERS; i++) {
                if (!isValidController(i)) continue;

                GamePadCapabilities gamePadCap = GamePad.GetCapabilities(i);
                JoystickCapabilities joystickCap = Joystick.GetCapabilities(i);
                if (!gamePadCap.IsConnected || !joystickCap.IsConnected) continue;

                Console.WriteLine("    {");
                Console.WriteLine("        \"name\": \"" + GamePad.GetName(i) + "\",");
                Console.Write("        \"type\": ");
                if (isXboxLike(i)) {
                    Console.WriteLine("\"xbox\",");
                } else if (isN64Like(i)) {
                    Console.WriteLine("\"n64\",");
                } else if (GamePad.GetState(i).IsConnected) {
                    Console.WriteLine("\"other\",");
                }
                Console.WriteLine("        \"axes\": " + joystickCap.AxisCount + ",");
                Console.WriteLine("        \"buttons\": " + joystickCap.ButtonCount);
                Console.Write("    }");
                Console.WriteLine(
                        (i + 1 >= MAX_CONTROLLERS && isValidController(i + 1)) ? "," : "");
            }
            Console.WriteLine("]");
        }

        /**
         * Check that a valid controller is plugged in at the specified index.
         * @param index The index of the controller to test.
         */
        private static bool isValidController(int index) {
            GamePadCapabilities gamePadCap = GamePad.GetCapabilities(index);
            JoystickCapabilities joystickCap = Joystick.GetCapabilities(index);
            return gamePadCap.IsConnected && joystickCap.IsConnected
                    && joystickCap.AxisCount > 0 && joystickCap.ButtonCount > 0;
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
