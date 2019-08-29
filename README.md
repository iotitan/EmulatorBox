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

## Setup

#### Firewall
The following should have all outbound requests blocked:
- **Stream Deck updater**: %ProgramFiles%\Elgato\StreamDeck\StreamDeck.exe
  - This is to prevent the UI from popping up a notification for updating the software.
#### Stream Deck