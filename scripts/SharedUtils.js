/**
 * Copyright 2019 Matthew Jones
 *
 * File: SharedUtils.js
 * Author: Matt Jones
 * Date: 2019.09.01
 * Desc: Shared utility functions.
 */

const {exec, execSync} = require('child_process');
const fs = require('fs');

module.exports = {
    /** The name of the file containing the information about the different UI systems. */
    UI_SYSTEM_INFO_FILE_NAME: "ui_system_info.json",

    /** The name of the file containing the information about the emulators. */
    EMULATOR_INFO_FILE_NAME: "emulator_info.json",

    /**
     * @param {string} fileName The name of the file that contains JSON in the format described
     *                          below.
     * @return {object} A JSON object parsed from the file. The format is as follows:
     *      [
     *          {
     *              "type": <STRING>,
     *              "location": <STRING>
     *              "bin": <STRING>
     *              "force_kill": <BOOLEAN>
     *          }
     *          ...
     *      ]
     */
    getJsonFromFile(fileName) {
        if (!fs.existsSync(fileName)) return [];

        return JSON.parse(fs.readFileSync(fileName).toString());
    },

    /**
     * 
     * @param {object} json The JSON object retrieved from #getJsonFromFile.
     * @param {string} typeName The name of the type to get the info for (gc, snes, steam, etc.).
     * @return {object} A single entry from the info file if it exists, null otherwise.
     */
    getEntryForType(json, typeName) {
        let lowerCaseType = typeName.toLowerCase();
        for (let i = 0; i < json.length; i++) {
            if (lowerCaseType == json[i]["type"].toLowerCase()) return json[i];
        }
        return null;
    },

    /**
     * Check to see if a process is running.
     * @param {string} processName The name of the process to check.
     * @return {boolean} Whether the process is running.
     */
    checkProcessRunning(processName) {
        // List tasks without column headers (/nh), in CSV format (/fo "csv"), with the specified
        // image name (/fi "IMAGENAME eq emu.exe").
        let out = execSync("tasklist /nh /fo \"csv\" /fi \"IMAGENAME eq " + processName + "\"");
        let outSplit = out.toString().split(",");

        // The first item in the csv is always the process name. If the process wasn't
        // found, do nothing. Remove the quotes from the output and check the name if it's
        // there.
        return outSplit.length >= 1 && outSplit[0].substr(1, outSplit[0].length - 2) == processName;
    },

    /**
     * Get the process ID of the named process.
     * @param {string} processName The name of the process to find the ID for.
     * @return {string} The PID of the provided process.
     */
    getProcessId(processName) {
        let out = execSync("tasklist /nh /fo \"csv\" /fi \"IMAGENAME eq " + processName + "\"");
        let outSplit = out.toString().split(",");

        // Remove the quotes from the output at index 1 (the second value in the CSV).
        return outSplit[1].substr(1, outSplit[1].length - 2);
    },

    /**
     * Get the child processes of the provided parent.
     * @param {string} parentId The ID of the parent whose child ID should be retrieved.
     * @return {string[]} A list of child IDs.
     */
    getProcessesFromParentId(parentId) {
        let out = execSync("wmic process where \"ParentProcessId=" + parentId
                + "\" get ProcessId /format:csv");
        let lineSplit = out.toString().split("\n");
        let childIds = new Array();
        for (let i = 0; i < lineSplit.length; i++) {
            let items = lineSplit[i].split(",");
            // If the split doesn't have at least two entries, continue. Expecting "MachineName,ID".
            if (items.length < 2) continue;
            // Try to parse the number to make sure it's an ID.
            if (isNaN(parseInt(items[1]))) continue;
            childIds.push(items[1]);
        }
        return childIds;
    },

    /**
     * Close all the emulators currently running on the system.
     * @param {object} emuJson A JSON object containing all the information about the emulators on
     *                         the system.
     */
    killEmulators(emuJson) {
        for (let i = 0; i < emuInfo.length; i++) {
            killEmulatorByName(emuInfo[i]["bin"], emuInfo[i]["force_kill"]);
        }
    },

    /**
     * Asynchronously kill a process by its process name.
     * @param {string} processName The name of the process to attempt to kill.
     * @param {boolean} force Whether the process should be forced to terminate. This is important
     *                  because some emulators save on exit, forcing can cause saving to fail.
     */
    killProcessByName(processName, force) {
        exec("taskkill " + (force ? "/f " : "") + "/fi \"IMAGENAME eq " + processName + "\"");
    },

    /**
     * Kill a process given its ID.
     * @param {string} pid The process ID to kill.
     * @param {boolean} force Whether the process should be forced to terminate. This is important
     *                  because some emulators save on exit, forcing can cause saving to fail.
     */
    killProcessByPID(pid, force) {
        exec("taskkill " + (force ? "/f " : "") + "/fi \"PID eq " + pid + "\"");
    },

    /**
     * Given a path, add a trailing slash if there isn't one.
     * @param {string} path The path to check.
     * @return {string} The path with a slash at the end if there wasn't one.
     */
    addTrailingSlashIfNeeded(path) {
        if (!path) return "/";
        let lastChar = path.charAt(path.length - 1);
        console.log("last char " + lastChar);
        if (lastChar == "/" || lastChar == "\\") return path;
        return path + "/";
    }
}
