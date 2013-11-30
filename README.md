Hexiano
======

![Hexiano logo](https://raw.github.com/lrq3000/hexiano/master/wiki/images/hexiano-logo.png)
&nbsp;
![Example of Hexiano running](https://raw.github.com/lrq3000/hexiano/master/wiki/images/hexiano-keyboard.jpg)


Description
----------------
Hexiano is an Open-source isomorphic musical keyboard, and technically a sample-based synthesizer.

Isomorphic means that all intervals, chords, and scales have identical patterns, regardless of the key in which one plays.

Isomorphic keyboards are an old concept, and some layouts are more than a century old (Janko, Wicki-Hayden).

Hexiano not only aims to deliver the isomorphic keyboard concept to the Android platform, but it is also committed to give you ergonomic tools to get the most out of these amazing keyboards, with all the options you need to customize your own Hexiano experience.

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

- __Play.java__ is the entry point of the app, launching the board setup (HexKeyboard) and managing the menu call and what happens on exiting. Since multi-instruments was implemented, it also manages the sound manager (SoundPool) as a singleton shared all across the app, and the array of Instrument (that is then used by HexKeyboard and HexKey).

- __HexKeyboard.java__ is the main program, where the keyboard is setup. It defines the screen dimensions and the number of rows/columns of keys, it creates the keys (HexKey, SonomeKey, etc.), draws them and manages the touch event (when you press on the screen), and redirect these events to the correct key. This is also where most of the SharedPreferences are managed (loaded in static variables used across the app).

- __HexKey.java__ is the prototype of each keys you see on the screen. It is an abstract class that must be implemented by other keys. It manages the play() and stop() events for each keys (and calls the sound in Instrument), draws the color, defines the disposition and coordinates (eg: vertical or horizontal orientation of hexagons), and a few other stuffs. This is probably the most important part of the app with HexKeyboard.java
    * _SonomeKey.java_ defines one sonome key. It is the first key implemented in the system, thus you can clearly see that it inspired the others types of keys.
    * _JankoKey.java_ defines the janko keys.
    * _JammerKey.java_ defines the jammer keys.
    * _ModifierKey.java_ is an abstract class which defines modifier keys (modifying the functionnalities or sending CC messages instead of producing a Note sound).
    * _SustainKey.java_ defines the sustain modifier key. It can be used as an example for other modifier keys, which are keys that does not produce sound but modify the behaviour of other keys.

- __Note.java__ manages the name of the keys (with octave) and correspondance between MIDI number and the note in different paradigms (eg: English, DoReMi, Deutsch, etc.).

- __CC.java__ manages the CC messages number correspondance with their name and print the label on ModifierKeys.

- __Instrument.java__ manages the sound, it plays the notes you hear when you press a key. It also extrapolates missing notes sounds by modulating existing sounds playback rate (=frequency), and the velocity effect. It is an abstract class that must be implemented by other instruments.
    * _Piano.java_ is the standard (and only for now) piano instrument. It simply plays the ogg files in res/raw/
    * _GenericInstrument.java_ is a generic instrument which provides a neat facility to automatically discover and load external instruments by using soundbanks from your SD Card.

- __Point.java__ simply defines a geometric point. It is primarily used to define the center of each HexKey.

- __Prefer.java__ extends the Menu by adding dynamically generated submenus (eg: loading external instruments via GenericInstrument, generate multi-instruments and multi-panels configs, etc.).

- _AboutDialog.java_ manage the AboutDialog with UI parts that couldn't fit in XML.


Feedback
--------------

If you have found any bug or have a suggestion for an amazing new feature you would like to see in Hexiano, feel free to post a request in the github issue tracker:

https://github.com/lrq3000/hexiano/issues


License
-----------
Licensed under the terms of the General Public License v3 (GPLv3).

Copyleft @ 2013 by Stephen Larroque,
Copyright © 2012 by James Haigh,
Copyright © 2011, 2012 by David Randolph.
Hexiano™ and the Hexiano logo are trademarks of James Haigh.

Latest project at:
https://github.com/lrq3000/hexiano

Original project by James Haigh at:
https://gitorious.org/hexiano

From the sourcecode of the opensourced version (under GPLv3) of Isokeys by David Randolph:
http://isokeys.sourceforge.net/

Piano instrument uses sounds from http://zenvoid.org/audio.html under CC BY.
