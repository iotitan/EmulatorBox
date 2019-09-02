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
    return;
}

let emuInfo = SharedUtils.getProcessNameList(emuInfoFile);
let killOk = true;

// Loop over known emulators and kill their processes.
for (let i = 0; i < emuInfo.length; i++) {
    if (SharedUtils.checkProcessRunning(emuInfo[i].bin)) {
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
