/**
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
 */
function killEmulatorProcessNoPID(processName) {
    exec("taskkill /f /fi \"IMAGENAME eq " + processName + "\"");
}

let emuInfo = SharedUtils.getProcessNameList(emuInfoFile);

// Loop over known emulators and kill their processes.
for (let i = 0; i < emuInfo.length; i++) {
    killEmulatorProcessNoPID(emuInfo[i].bin);
}
