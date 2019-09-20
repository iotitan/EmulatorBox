/**
 * Copyright 2019 Matthew Jones
 *
 * File: ConfigAndRunProject64.js
 * Author: Matt Jones
 * Date: 2019.09.19
 * Desc: Rewrite the Project64 config based on the number of connected controllers.
 */

const {execSync} = require('child_process');
const fs = require("fs");
const process = require('process');
const SharedUtils = require('./SharedUtils');

let emuPath = process.argv[2];
let romPath = process.argv[3];
if (!romPath || !emuPath) {
    console.log("usage: ");
    console.log("    node.exe ./ConfigAndRunProject64.js <emu_info_file> <rom_path>");
    return;
}

let basePath = "C:/emulator_box";
let configTemplate = basePath + "/EmulatorBox/configs/Project64/Config/NRage.ini";
let configTarget = basePath + "/emulators/Project64-2.3/Config/NRage.ini";

let controllerJson = JSON.parse(execSync(basePath + "/EmulatorBox/scripts/ControllerInfo.exe"));
let controllerCount = controllerJson.length;

// Remove the target file if it exists.
if (fs.existsSync(configTarget)) fs.unlinkSync(configTarget);

let templateLines = fs.readFileSync(configTemplate).split("\n");
let targetHandle = fs.openSync(configTarget, "w+");

let curControllerIndex = 0;
for (let i = 0; i < templateLines.length; i++) {
    if (templateLines[i].startsWith("Plugged=")) {
        if (curControllerIndex < controllerCount) {
            let type = controllerJson[curControllerIndex]["type"];
            fs.writeSync(targetHandle,
                    "Plugged=" + ((type == "n64" || type == "xbox") ? "1" : "0") + "\n");
            curControllerIndex++;
        }
    } else {
        fs.writeSync(targetHandle, templateLines[i] + "\n");
    }

}
fs.close(targetHandle);

let emuInfo = SharedUtils.getJsonFromFile(emuInfoFile);
let p64Info = SharedUtils.getEntryForType("N64");

execSync(p64Info["location"] + p64Info["bin"] + " " + romPath);
