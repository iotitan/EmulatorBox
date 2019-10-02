/**
 * Copyright 2019 Matthew Jones
 *
 * File: PowerOff.js
 * Author: Matt Jones
 * Date: 2019.08.28
 * Desc: A script that powers the machine down without warning about open programs. Shutdown is set
 *       to occur 5 seconds after the script is executed.
 */

const {exec} = require('child_process');
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

if (emuInfoFile) SharedUtils.killEmulators(SharedUtils.getJsonFromFile(emuInfoFile));

// TODO(Matt): Steam should probably be shut down as well.

exec("shutdown /s /f /t 5 /d p:0:0 /c \"Power off from custom button controls.\"");
