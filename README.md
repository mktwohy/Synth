# Synth
An Android app for creating and playing sounds using additive synthesis

# App Architecture
### note: this is now outdated
 - CircularIntArray is no longer used, as Signals have been rewritten to generate data in realtime
    - Signal still uses CircularIndex to keep track of its current position though
 - PianoKey does not reference a Signal. Instead, PianoView tells MainActivity what Notes are being played, and it creates the associated Signal
 
![alt text](https://github.com/mktwohy/Synth/blob/master/Images/UmlDiagram.jpg)

PianoGrid class is easier to understand with this blueprint:
![alt text](https://github.com/mktwohy/Synth/blob/master/Images/PianoGridBlueprint.jpg)
