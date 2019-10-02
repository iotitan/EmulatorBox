/**
 * Copyright 2019 Matthew Jones
 *
 * File: KillGames.js
 * Author: Matt Jones
 * Date: 2019.08.12
 * Desc: Kills any games running as an emulator or through steam.
 */

const process = require('process');
const SharedUtils = require('./SharedUtils');

let configDir = process.argv[2];
if (!configDir) {
    console.log("usage: ");
    console.log("    node.exe ./KillGames.js <config_dir>");
    return;
}

configDir = SharedUtils.addTrailingSlashIfNeeded(configDir);
let emuInfoFile = configDir + SharedUtils.EMULATOR_INFO_FILE_NAME;
let uiInfoFile = configDir + SharedUtils.UI_SYSTEM_INFO_FILE_NAME;

SharedUtils.killEmulators(SharedUtils.getJsonFromFile(emuInfoFile));

let uiInfo = SharedUtils.getJsonFromFile(uiInfoFile);
let steamBin = SharedUtils.getEntryForType(uiInfo, "Steam")["bin"];

if (SharedUtils.checkProcessRunning(steamBin)) {
    let steamPid = SharedUtils.getProcessId(steamBin);
    let children = SharedUtils.getProcessesFromParentId(steamPid);
    for (let i = 0; i < children.length; i++) {
        SharedUtils.killProcessByPID(children[i], false);
    }
}
