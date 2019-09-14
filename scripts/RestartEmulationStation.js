/**
 * Copyright 2019 Matthew Jones
 *
 * File: RestartEmulationStation.js
 * Author: Matt Jones
 * Date: 2019.09.01
 * Desc: Restart Emulation Station if no emulators are running.
 */

const {execSync} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

let emuInfoFile = process.argv[2];
let uiInfoFile = process.argv[3];
if (!emuInfoFile || !uiInfoFile) {
    console.log("usage: ");
    console.log("    node.exe ./RestartEmulationStation.js <emu_info_file> <ui_sys_info_file>");
    return;
}

let emuInfo = SharedUtils.getJsonFromFile(emuInfoFile);
let killOk = true;

// Loop over known emulators to check if they are running.
for (let i = 0; i < emuInfo.length; i++) {
    if (SharedUtils.checkProcessRunning(emuInfo[i]["bin"])) {
        killOk = false;
        break;
    }
}

// If nothing was running restart Emulation Station.
if (killOk) {
	let uiSysInfo = SharedUtils.getJsonFromFile(uiInfoFile);
	let steamInfo = SharedUtils.getEntryForType(uiSysInfo, "Steam");

    // Don't let Steam keep running if we're using Emulation Station.
    if (SharedUtils.checkProcessRunning(steamInfo["bin"])) {
        execSync("taskkill /f /fi \"IMAGENAME eq " + steamInfo["bin"] + "\"");
    }

	let esInfo = SharedUtils.getEntryForType(uiSysInfo, "EmulationStation");

    execSync("taskkill /f /fi \"IMAGENAME eq " + esInfo["bin"] + "\"");
    execSync("start \"\" \"" + esInfo["location"] + esInfo["bin"] + "\" -bigpicture");
}
