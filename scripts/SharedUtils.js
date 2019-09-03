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
     * @param {string} fileName The name of the file that contains JSON in the format described
     *                          below.
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
    }
}
