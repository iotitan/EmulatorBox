/**
 * Copyright 2019 Matthew Jones
 *
 * File: KillEmulators.js
 * Author: Matt Jones
 * Date: 2019.08.12
 * Desc: A script that kills all the emulators running on a Windows system.
 */

const {exec} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

let emuInfoFile = process.argv[2];
if (!emuInfoFile) {
    console.log("usage: ");
    console.log("    node.exe ./KillEmulators.js <emu_info_file>");
    return;
}

/**
 * Asynchronously kill an emulator process by its process name (Windows specific).
 * @param {string} processName The name of the process to attempt to kill.
 * @param {boolean} force Whether the process should be forced to terminate. This is important
 *					because some emulators save on exit, forcing can cause saving to fail.
 */
function killEmulatorProcessNoPID(processName, force) {
    exec("taskkill " + (force ? "/f " : "") + "/fi \"IMAGENAME eq " + processName + "\"");
}

let emuInfo = SharedUtils.getJsonFromFile(emuInfoFile);

// Loop over known emulators and kill their processes.
for (let i = 0; i < emuInfo.length; i++) {
    killEmulatorProcessNoPID(emuInfo[i]["bin"], emuInfo[i]["force_kill"]);
}
