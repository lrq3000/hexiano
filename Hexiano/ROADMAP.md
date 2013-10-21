Hexiano Roadmap
==============


GOALS
-----------

The current main goal of Hexiano is to extend it as to be a fully nomad on-the-run performance keyboard for diverse instruments, which would particularly fit street performers.

Longer term goals would have Hexiano be a full midi keyboard and synthesizer, which just begins to be possible with the very recent (as of 2013) efforts of Google with low-latency Android Audio.

Anyway if you have any other idea of a feature or any code you would like to propose to the project, feel free to post in the Github Issues or Fork+Pull Request!


TODO
---------

Here is a list of a few features that are planned in near-term:

- better quality piano sound (3 sounds every 4 are interpolated by the program currently!)
- ability to add soundbanks on sdcard directly without recompiling the program
- a new free orchestral soundbank (maybe as an external module to keep the Hexiano low in size)
- a multi-instruments feature: ability to map multiple instruments on the keyboard (eg: the first 2 rows will be piano, then 2 rows of violin, etc...)
- Connection to computer (via USB or OSC or Bluetooth)

MAYBE/ONE DAY
-------------
- Velocity via pressure surface (but how to do that with ogg files and soundpool?)
- effects and effects via gyrometer.
- multi-screens keyboards, so that you could quickly swap between different (multi-)instruments mapping by just swiping on the screen.
- Interactive tutorials (one for each keyboard) to know how to play major scales, minor, change root note, etc...
- Midi tutorial: load a midi file and the keys to press will be highlighted on the screen.

NOT TO DO
---------
- Hexiano used as a host for an external Midi keyboard. I see no objection if someone contributes such a code, but it's not part of the roadmap, there are plenty of others softwares that can do that, and the main purpose of Hexiano is the keyboard interface.
