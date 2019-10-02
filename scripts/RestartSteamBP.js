/**
 * Copyright 2019 Matthew Jones
 *
 * File: RestartSteamBP.js
 * Author: Matt Jones
 * Date: 2019.09.02
 * Desc: Restart Steam in big-picture mode.
 */

const {execSync} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

let configDir = process.argv[2];
if (!configDir) {
    console.log("usage: ");
    console.log("    node.exe ./RestartSteamBP.js <config_dir>");
    return;
}

configDir = SharedUtils.addTrailingSlashIfNeeded(configDir);
let emuInfoFile = configDir + "/" + SharedUtils.EMULATOR_INFO_FILE_NAME;
let uiInfoFile = configDir + "/" + SharedUtils.UI_SYSTEM_INFO_FILE_NAME;

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
    let esInfo = SharedUtils.getEntryForType(uiSysInfo, "EmulationStation");

    // Don't let EmulationStation keep running if we're using Steam.
    if (SharedUtils.checkProcessRunning(esInfo["bin"])) {
        execSync("taskkill /f /fi \"IMAGENAME eq " + esInfo["bin"] + "\"");
    }

    let steamInfo = SharedUtils.getEntryForType(uiSysInfo, "Steam");

    execSync("taskkill /f /fi \"IMAGENAME eq " + steamInfo["bin"] + "\"");
    execSync("start \"\" \"" + steamInfo["location"] + steamInfo["bin"] + "\" -bigpicture");
}
