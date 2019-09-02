# Emulator Box

This a computer designed to behave like a typical gaming console; it hooks up to a TV and is
primarily controlled via USB controller and a small customizable button pad. A keyboard and
mouse should not be necessary to operate the device.

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
- ```./button_icons/```
  - Icons for the Stream Deck buttons.
- ```./configs/```
  - Configuration files for any of the base software that needs it.
  - Emulators have configs set up for xBox controllers.
  - Emulation Station is configured to work on the directory structure listed above.
- ```./EmulationStation_theme/```
  - A thin version of the stock "simple" theme that ships with Emulation Station (only includes the
    emulators that are actually used).
- ```./RunProgramSilent/```
  - A wrapper program that tries to execute programs without showing a main window.
  - This is mainly for polish as it looks better when a emulator starts without numerous windows
    appearing.
  - Use: ```./RunProgramSilent.exe <PROGRAM_PATH> [<PARAM_1>] [<PARAM_2>] [...]```
- ```./scripts/```
  - Node JS scripts to perform various functionality (mostly for the Stream Deck).
- ```./system_wallpaper.png```
  - A basic wallpaper to show on system startup and desktop while things are loading.

#### Firewall
The following should have all outbound requests blocked:
- **Stream Deck updater**: %ProgramFiles%\Elgato\StreamDeck\StreamDeck.exe
  - This is to prevent the UI from popping up a notification for updating the software.

#### Stream Deck
- Button 1: Home
 - ```./button_icons/house.png```
 -  ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/KillEmulators.js
        C:/emulator_box/EmulatorBox/scripts/emulator_info.json
    ```
- Button 2: Reset ES
 - ```./button_icons/reset.png```
 -  ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/RestartEmulationStation.js
        C:/emulator_box/EmulatorBox/scripts/emulator_info.json
        C:/emulator_box/emulationstation_2.0.1a_win32/emulationstation.exe
    ```
- Button 3: Power Off
 - ```./button_icons/zzz.png```
 -  ```
    C:/emulator_box/EmulatorBox/scripts/RunProgramSilent.exe
        node.exe
        C:/emulator_box/EmulatorBox/scripts/PowerOff.js
    ```