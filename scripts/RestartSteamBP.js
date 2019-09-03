/**
 * File: RestartSteamBP.js
 * Author: Matt Jones
 * Date: 2019.09.02
 * Desc: Restart Steam in big-picture mode.
 */

const {execSync} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

let emuInfoFile = process.argv[2];
let uiInfoFile = process.argv[3];
if (!emuInfoFile || !uiInfoFile) {
    console.log("usage: ");
    console.log("    node.exe ./RestartSteamBP.js <emu_info_file> <ui_sys_info_file>");
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
    let esInfo = SharedUtils.getEntryForType(uiSysInfo, "EmulationStation");

    // Don't let EmulationStation keep running if we're using Steam.
    if (SharedUtils.checkProcessRunning(esInfo["bin"])) {
        execSync("taskkill /f /fi \"IMAGENAME eq " + esInfo["bin"] + "\"");
    }

    let steamInfo = SharedUtils.getEntryForType(uiSysInfo, "Steam");

    execSync("taskkill /f /fi \"IMAGENAME eq " + steamInfo["bin"] + "\"");
    execSync("start " + steamInfo["path"] + steamInfo["bin"] + " -bigpicture");
}
