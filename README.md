Hexiano
======

![Hexiano logo](https://raw.github.com/lrq3000/hexiano/master/wiki/images/hexiano-logo.png)
&nbsp;
![Example of Hexiano running](https://raw.github.com/lrq3000/hexiano/master/wiki/images/hexiano-keyboard.jpg)


Description
----------------
Hexiano is an Open-source isomorphic musical keyboard, and technically a sample-based synthesizer.

Isomorphic means that all intervals, chords, and scales have identical patterns, regardless of the key in which one plays. In other words, in [isomorphic keyboard](https://en.wikipedia.org/wiki/Isomorphic_keyboard), also called [generalized keyboards](https://en.wikipedia.org/wiki/Generalized_keyboard), all musical concepts have an invariant pattern. This allows to understand theoretical musical concepts not only from the sound, but also visually.

Furthermore, this kind of keyboard being in two dimensions, they have a much higher density of keys compared to a unidimensional piano keyboard, which means that professional players will appreciate the ability to reach other octaves much faster than on a traditional keyboard: if you want to compete on your speed of playing music, this is what you need.

Finally, isomorphic keyboards are also ideal for microtonality, although this is currently not implemented in this app.

Isomorphic keyboards are an old concept, and some layouts are more than a century old (Janko, Wicki-Hayden). The original concept of isomorphic hexagonal keyboards with invariant musical patterns likely traces back to the [Tonnetz](https://en.wikipedia.org/wiki/Tonnetz).

Hexiano not only aims to deliver the isomorphic keyboard concept to the Android platform, but it is also committed to give you ergonomic tools to get the most out of these amazing keyboards, with all the options you need to customize your own Hexiano experience.

See http://www.altkeyboards.com/ for more information about these instruments.

This is a fork of the Hexiano app, with several bugfixes and new features, such as the possibility to use multiple instruments on the same keyboard, and add new instruments soundfonts from the SD card.

Build and install
-----------------------

### Via commandline

    ./gradlew installDebug

### Via Android Studio

1. Make sure an Android device is connected (either connect your phone via USB, or run an emulator)
2. hit the green 'play' button in the top left of the IDE!


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
