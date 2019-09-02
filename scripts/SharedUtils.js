/**
 * File: SharedUtils.js
 * Author: Matt Jones
 * Date: 2019.09.01
 * Desc: Shared utility functions.
 */

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
    }
}
