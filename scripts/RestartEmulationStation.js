/**
 * File: RestartEmulationStation.js
 * Author: Matt Jones
 * Date: 2019.09.01
 * Desc: Restart Emulation Station if no emulators are running.
 */

const {execSync} = require('child_process');
const process = require('process');
const SharedUtils = require('./SharedUtils');

let emuInfoFile = process.argv[2];
let esFullPath = process.argv[3];
if (!emuInfoFile || !esFullPath) {
    console.log("usage: ");
    console.log("    node.exe ./RestartEmulationStation.js <emu_info_file> <full_ES_bin_path>");
    console.log("");
    console.log("    NOTE: Paths require forward slashes ('/' not '\\').");
}


/**
 * Check to see if a process is running.
 * @param {string} processName The name of the process to check.
 * @return {boolean} Whether the process is running.
 */
function checkProcessRunning(processName) {
    // List tasks without column headers (/nh), in CSV format (/fo "csv"), with the specified
    // image name (/fi "IMAGENAME eq emu.exe").
    let out = execSync("tasklist /nh /fo \"csv\" /fi \"IMAGENAME eq " + processName + "\"");
    let outSplit = out.toString().split(",");

    // The first item in the csv is always the process name. If the process wasn't
    // found, do nothing. Remove the quotes from the output and check the name if it's
    // there.
    return outSplit.length >= 1 && outSplit[0].substr(1, outSplit[0].length - 2) == processName;
}

let emuInfo = SharedUtils.getProcessNameList(emuInfoFile);
let killOk = true;

// Loop over known emulators and kill their processes.
for (let i = 0; i < emuInfo.length; i++) {
    if (checkProcessRunning(emuInfo[i].bin)) {
        killOk = false;
        break;
    }
}

// If nothing was running restart Emulation Station.
if (killOk) {
    let pathComponents = esFullPath.split('/');
    let esExeName = pathComponents[pathComponents.length - 1];

    execSync("taskkill /f /fi \"IMAGENAME eq " + esExeName + "\"");
    execSync("start " + esFullPath);
}
