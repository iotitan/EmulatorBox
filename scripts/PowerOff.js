/**
 * File: PowerOff.js
 * Author: Matt Jones
 * Date: 2019.08.28
 * Desc: A script that powers the machine down without warning about open programs. Shutdown is set
 *       to occur 5 seconds after the script is executed.
 *
 * Run: node ./PowerOff.js
 */

const {exec} = require('child_process');

exec("shutdown /s /f /t 5 /d p:0:0 /c \"Power off from custom button controls.\"");
