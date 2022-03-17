The config for this emulator should be moved to the root directory and be
specified in the command to start the emulator (mupen64plus.cfg). The config
has custom directories set up for save files based on the intended structure
of the emulator system (C:\emulator_box\saves\mupen64plus\...). The input
config (InputAutoCfg.ini) also has a few controllers added to it. Run the
emulator with a command similar to below:

.\mupen64plus-ui-console.exe --windowed --resolution 800x600 --configdir .\ 'C:\Users\Matt\emulators\roms\n64\Pokemon Snap.n64'