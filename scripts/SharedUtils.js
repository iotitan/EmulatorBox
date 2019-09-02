/**
 * File: SharedUtils.js
 * Author: Matt Jones
 * Date: 2019.09.01
 * Desc: Shared utility functions.
 */

const {execSync} = require('child_process');
const fs = require('fs');

module.exports = {
    /**
     * @param {string} fileName The name of the file that lists the process names relevant to this
     *                          system.
     * @return {object} A JSON object parsed from the file. The format is as follows:
     *      [
     *          {
     *              "type": <STRING>,
     *              "location": <STRING>
     *              "bin": <STRING>
     *          }
     *          ...
     *      ]
     */
    getProcessNameList(fileName) {
        if (!fs.existsSync(fileName)) return [];

        return JSON.parse(fs.readFileSync(fileName).toString());
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
    }
}
