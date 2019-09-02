/**
 * File: KillEmulators.js
 * Author: Matt Jones
 * Date: 2019.08.12
 * Desc: A script that kills all the emulators running on a Windows system.
 */

const {exec} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

/**
 * Asynchronously kill an emulator process by its process name (Windows specific). This first finds
 * the PID of the named process and calles process.kill(...).
 * @param {string} processName The name of the process to attempt to kill.
 */
function killEmulatorProcess(processName) {
    // List tasks without column headers (/nh), in CSV format (/fo "csv"), with the specified
    // image name (/fi "IMAGENAME eq emu.exe").
    exec("tasklist /nh /fo \"csv\" /fi \"IMAGENAME eq " + processName + "\"",
            (error, stdout, stderr)=> {
                let out = stdout.split(",");

                // The first item in the csv is always the process name. If the process wasn't
                // found, do nothing. Remove the quotes from the output and check the name if it's
                // there.
                if (out.length < 2 || (out[0].substr(1, out[0].length - 2) != processName)) return;

                // Remove ther quotes from the output of index 1 which should always have the PID.
                let pid = out[1].substr(1, out[1].length - 2);

                process.kill(pid);
            });
}

/**
 * Asynchronously kill an emulator process by its process name (Windows specific).
 * @param {string} processName The name of the process to attempt to kill.
 */
function killEmulatorProcessNoPID(processName) {
    exec("taskkill /f /fi \"IMAGENAME eq " + processName + "\"");
}

// Args are: [0] node.exe, [1] KillEmulators.js, [2] file name
let emuInfoFile = process.argv[2];
if (!emuInfoFile) return;

let emuInfo = SharedUtils.getProcessNameList(emuInfoFile);

// Loop over known emulators and kill their processes.
for (let i = 0; i < emuInfo.length; i++) {
    killEmulatorProcessNoPID(emuInfo[i].bin);
}
