## Install Instructions
- Click on the most recent build. 
  - Older builds used the following naming scheme: Synth_v(month)-(day)-(year).apk
  - Now, they use conventional semantic versioning (the most recent is Synth_v1.0.0-alpha)
- Click "view raw." 
  - This should download the file. 
- Once downloaded, click on the download and it should automatically install. 
  - If it doesn't install, you'll need to go to your web browser app settings and enable the option titled "Install Unknown Apps"


## Changelogs 
- **12-6-2021 (v1.0.0-alpha)**
  - Finally at version 1.0.0-alpha! Nearing official release. Please note that:
    1. Because I went back to college, it has been a while since the last changelog.
    1. There are not many new features with this build - the majority of the time was spent on improving the underlying code architecture to have a more stable foundation.
      - Many, many minor bugs were introduced and squashed along the way. Most of these will not be mentioned
  - Fixed Bug: clicking noise when pressing note
  - Fixed Bug: popping noise when adjusting overtones/harmonic series
  - Added Feature: 2-way slider for scaling keyboard
  - Performance Improvement: Audio latency/input lag has been reduced, as the app now uses your phones native sample rate and buffer size
    - note: this may not improve latency on older phones
  - Known Issues:
    - Clicking noise when releasing keys
    - piano distorts when scaling above 3 octaves
- **8-27-2021:**
  - Fixed Bug: audio stuttering when playing 6+ notes
  - renamed wave shapes to their abbreviations (sin, sqr, tri, saw) so that they fit in one line
- **8-27-2021:**
  - Added Feature: Waveform selector.
    - You can now choose between Sine, Triangle, Sawtooth, and Square waves
  - Audio stuttering issue alleviated
    - Previously, pressing 4 notes would cause stuttering. Now the threshold is ~6 notes
  - UI layout now resizes based on phone orientation
  - On startup, the fundamental overtone is set to max volume
- **8-24-2021:**
  - All UI components have been rebuilt from the ground up to fit into the new Jetpack Compose framework. 
  - Added Feature: Piano can support any range of keys; it is no longer limited to one octave. User control for this will be added soon.
  - Added Feature: Harmonic Series Editor gives the user more control over their sound.
    - Sliders for controlling the volume of each overtone
    - Reset button (sets all sliders to zero)
    - Random button (adjusts sliders by randomly changing parameters decay, floor, ceiling, and filter)
  - Added Feature: Pitch Bend
  - Added Feature: Signal preview
    - similar to realtime-audio viewer, but instead shows 4 periods of the signal you've generated with the Harmonic Series. 
- **8-10-2021:**
  - Added Feature: replaced noise effect with harmonic overtones
    - this is the first step towards additive synthesis. For now, the user can only change the harmonic range; the volume of each overtone exponentially decays automatically . However, under the hood, these overtones can be manipulated with more control, and this control will be given to the user in a later update. 
  - Fixed Bug: all keys had to be released in order for changes from [-] and [+] buttons to apply 
  - Fixed Bug: when holding two or more keys on the piano, releasing one key would cause all keys would release momentarily
  - Fixed Bug: audio visualizer was not properly centered and had a range of 0.0 to 2.0 rather than -1.0 to 1.0
  - Fixed Bug: generated audio was off-pitch
- **8-3-2021:**
  - Added Feature: audio visualizer 
  - Fixed Bug: noise level indicator to display incorrectly
- **8-2-2021:**
  - Fixed Bug: excessive memory allocation that caused app to slow to a halt and even crash
    - this yields significant performance improvements
- **7-27-2021:**
  - Added Feature: harmonic white-noise effect
- **7-25-2021:**
  - Under the hood improvements which lessen the severity of the memory bug. This means that more notes can be played simultaneously without slowdown or crashing
  - Added Feature: the user can change the current octave
