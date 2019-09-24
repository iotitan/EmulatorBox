# Emulator Box

This a computer designed to behave like a typical gaming console; it hooks up to a TV and is
primarily controlled via USB controller and a small customizable button pad and/or Android app.
A keyboard and mouse should not be necessary to operate the device.

## Hardware

#### Machine
- **CPU**: AMD Ryzen 3400G
- **Motherboard**: ASRock Fatal1ty B450 Gaming-ITX/ac
- **RAM**: Corsair Vengeance LPX 16GB (2x8GB) DDR4 DRAM 3200MHz C16
- **Disk**: Samsung 970 EVO 500GB - NVMe PCIe M.2 2280 SSD

#### Peripherals
- Elgato Stream Deck Mini (6-button pad)
- 8 port USB hub
- 4x Rock Candy XBox USB controller

## Software
- Windows 10 x64
- [Emulation Station](https://github.com/Aloshi/EmulationStation/tree/unstable#emulationstation)
  - Graphical, controller-input front-end for emulator systems.
- [Node.js for windows](https://nodejs.org/en/)
  - This is responsible for running scripts for the button pad and app.
- Various emulators:
  - Dolphin
  - Project64
  - ZSNES
  - etc.
- [Stream Deck](https://www.elgato.com/en/gaming/downloads)
  - Software for programming the button pad.

## On-system Directory Structure
The config files in the repo are setup to work on the following directory structure:
- Root: ```C:/emulator_box/```
  - ```./EmulatorBox/```
    - This is a checkout of this git repo.
  - ```./roms/```
    - ```./gamebube/```
    - ```./n64/```
    - ```./gba/```
    - ```./snes/```
    - ```./nes/```
  - ```./emulators/```
    - Any emulators can be installed here. The local ```es_systems.cfg``` will need to be updated
      for any emulators in this directory.

## Project Directories
- ```./assets/```
  - Image assets for any of the sub-projects that need time (ex. app and Stream Deck icons).
- ```./configs/```
  - Configuration files for any of the base software that needs it.
  - Emulators have configs set up for xBox controllers.
  - Emulation Station is configured to work on the directory structure listed above.
  - Individual config files need to be move to replace the ones installed with each system.
- ```./ConsolePadApp/```
  - An Android app used with the ```ConsoleUDPResponder``` to perform the same actions as the
    Stream Deck but from your phone or device.
  - Automatically detects the console if the ```ConsoleUDPResponder``` is running.
- ```./ConsoleUDPResponder/```
  - The host software that executes commands issued by the ```Console Pad``` app.
  - This should be set to start when Windows starts.
  - **All actions performed by this software assume the directory structure listed above.**
- ```./ControllerInfo/```
  - A commandline utility to read out information about connected controllers in JSON format to
    std::out.
  - This currently uses the OpenTK library for controller input which is an absolute dumpster fire;
    axis and button counts are mangled and sometimes connection is flaky. A prototype using GLFW in
    C++ is in the pipe, but I was also like it to detect and output DirectInput IDs which are
    obsolete but old controllers still use.
- ```./RunProgramSilent/```
  - A wrapper program that tries to execute programs without showing a main window.
  - This is mainly for polish as it looks better when a emulator starts without numerous windows
    appearing.
  - Use: ```./RunProgramSilent.exe <PROGRAM_PATH> [<PARAM_1>] [<PARAM_2>] [...]```
- ```./scripts/```
  - Node JS scripts to perform various functionality (mostly for the Stream Deck).
  - Prebuilt executables for tools that need them.
- ```./system_wallpaper.png```
  - A basic wallpaper to show on system startup and desktop while things are loading.

## Setup

#### Startup
Create shortcuts to both the **Emulation Station** and **ConsoleUDPResponder** and put them in the
startup directory for windows.

#### Stream Deck
- Button 1: Home
  - ```./assets/button_icons/house.png```
  - ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/KillGames.js
        C:/emulator_box/EmulatorBox/scripts/emulator_info.json
        C:/emulator_box/EmulatorBox/scripts/ui_system_info.json
    ```
- Button 2: Emulators
  - Start or restart Emulation Station.
  - ```./assets/button_icons/emulationstation.png```
  - ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/RestartEmulationStation.js
        C:/emulator_box/EmulatorBox/scripts/emulator_info.json
        C:/emulator_box/EmulatorBox/scripts/ui_system_info.json
    ```
- Button 3: Steam
  - Start or restart Steam in big-picture mode.
  - ```./assets/button_icons/steam.png```
  - ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/RestartSteamBP.js
        C:/emulator_box/EmulatorBox/scripts/emulator_info.json
        C:/emulator_box/EmulatorBox/scripts/ui_system_info.json
    ```
- Button 4: Power Off
  - ```./assets/button_icons/zzz.png```
  - ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/PowerOff.js
    ```

#### Firewall (optional)
The following should have all outbound requests blocked:
- **Stream Deck updater**: %ProgramFiles%\Elgato\StreamDeck\StreamDeck.exe
  - This is to prevent the UI from popping up a notification for updating the software.