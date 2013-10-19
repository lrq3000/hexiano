Hexiano
======

![Hexiano logo](https://raw.github.com/lrq3000/hexiano/master/wiki/images/hexiano-logo.png)
&nbsp;
![Example of Hexiano running](https://raw.github.com/lrq3000/hexiano/master/wiki/images/hexiano-keyboard.jpg)


Description
----------------
Hexiano is an Open-source isomorphic musical keyboard.

Isomorphic means that all intervals, chords, and scales have identical patterns, regardless of the key in which one plays.

Hexiano aims to deliver the isomorphic keyboard concept to the Android platform.

See http://www.altkeyboards.com/ for more information about these instruments.


Build and install
-----------------------

### Via commandline

    $ cd ~/src/hexiano/Hexiano
    
    $ # Only needed first time:
    $ android list targets # I build against API level 10.
    $ android update project --name Hexiano --target 1 --path ./
    $ git checkout build.xml # Update, not replace. Bad android update!
    
    $ # Everytime:
    $ ant filter-package && ant debug && adb -d install -r build/bin/Hexiano-debug.apk

### Via Eclipse ADT

In Eclipse ADT, select File > Import and then select Android > Existing Android Code Into Workspace.
Then select the hexiano/Hexiano directory (where src is contained), and import that. It should work right away.
As an alternative: use the build.gradle file.

### Via Android Studio

Not tested, but you can try to use the build.gradle file to import the project.


Project architecture
----------------------------

To quick-start your contributions, here is a short description of how the project is architectured, by somewhat chronological order of calling in the program flow:

- _Play.java_ is the entry point of the app, launching the board setup (HexKeyboard) and managing the menu call and what happens on exiting.

- _HexKeyboard.java_ is the main program, where the keyboard is setup. It defines the screen dimensions and the number of rows/columns of keys, it creates the keys (HexKey, SonomeKey, etc.), draws them and manages the touch event (when you press on the screen), and redirect these events to the correct key. This is also where most of the SharedPreferences are managed (loaded in static variables used across the app).

- _HexKey.java_ is the prototype of each keys you see on the screen. It is an abstract class that must be implemented by other keys. It manages the play() and stop() events for each keys (and calls the sound in Instrument), draws the color, defines the disposition and coordinates (eg: vertical or horizontal orientation of hexagons), and a few other stuffs. This is probably the most important part of the app with HexKeyboard.java
    * _SonomeKey.java_ defines one sonome key. It is the first key implemented in the system, thus you can clearly see that it inspired the others types of keys.
    * _JankoKey.java_ defines the janko keys.
    * _JammerKey.java_ defines the jammer keys.
    * _SustainKey.java_ defines the sustain modifier key. It can be used as an example for other modifier keys, which are keys that does not produce sound but modify the behaviour of other keys.

- _Note.java_ manages the name of the keys (with octave).

- _Instrument.java_ manages the sound, it plays the notes you hear when you press a key. It is an abstract class that must be implemented by other instruments.
    * Piano.java is the standard (and only for now) piano instrument. It simply plays the ogg files in res/raw/

- _Point.java_ simply defines a geometric point. It is primarily used to define the center of each HexKey.

- AboutDialog.java and Prefer.java are just small UI parts that couldn't fit in XML.


License
-----------
Licensed under the terms of the General Public License v3 (GPLv3).

Copyleft 2013 by Stephen Larroque
Copyright © 2012 by James Haigh,
Copyright © 2011, 2012 by David Randolph.
Hexiano™ and the Hexiano logo are trademarks of James Haigh.

Forked project at:
https://github.com/lrq3000/hexiano

Original project by James Haigh at:
https://gitorious.org/hexiano

From the sourcecode of the opensourced version of Isokeys by David Randolph.

Piano instrument uses sounds from http://zenvoid.org/audio.html under CC BY.
