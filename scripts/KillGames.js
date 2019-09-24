/**
 * Copyright 2019 Matthew Jones
 *
 * File: KillGames.js
 * Author: Matt Jones
 * Date: 2019.08.12
 * Desc: Kills any games running as an emulator or through steam.
 */

const {exec} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

let emuInfoFile = process.argv[2];
let uiInfoFile = process.argv[3];
if (!emuInfoFile || !uiInfoFile) {
    console.log("usage: ");
    console.log("    node.exe ./KillGames.js <emu_info_file> <ui_info_file>");
    return;
}

/**
 * Kill a process given its ID.
 * @param {string} pid The process ID to kill.
 * @param {boolean} force Whether the process should be forced to terminate. This is important
 *                  because some emulators save on exit, forcing can cause saving to fail.
 */
function killEmulatorProcessWithPID(pid, force) {
    exec("taskkill " + (force ? "/f " : "") + "/fi \"PID eq " + pid + "\"");
}

/**
 * Asynchronously kill an emulator process by its process name (Windows specific).
 * @param {string} processName The name of the process to attempt to kill.
 * @param {boolean} force Whether the process should be forced to terminate. This is important
 *                  because some emulators save on exit, forcing can cause saving to fail.
 */
function killEmulatorProcessNoPID(processName, force) {
    exec("taskkill " + (force ? "/f " : "") + "/fi \"IMAGENAME eq " + processName + "\"");
}

let emuInfo = SharedUtils.getJsonFromFile(emuInfoFile);

// Loop over known emulators and kill their processes.
for (let i = 0; i < emuInfo.length; i++) {
    killEmulatorProcessNoPID(emuInfo[i]["bin"], emuInfo[i]["force_kill"]);
}

let uiInfo = SharedUtils.getJsonFromFile(uiInfoFile);
let steamBin = SharedUtils.getEntryForType(uiInfo, "Steam")["bin"];

if (SharedUtils.checkProcessRunning(steamBin)) {
    let steamPid = SharedUtils.getProcessId(steamBin);
    let children = SharedUtils.getProcessesFromParentId(steamPid);
    for (let i = 0; i < children.length; i++) {
        killEmulatorProcessWithPID(children[i], false);
    }
}
