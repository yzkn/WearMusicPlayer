WearMusicPlayer
====

## Overview
---

Android Wearでオフライン音楽再生するためのアプリです

This is the app that make it possible to listen to music on Android Wear devices without an internet connection.

## Install
---

### Please download app-release.apk from [here](https://github.com/YA-androidapp/WearMusicPlayer/blob/master/app/app-release.apk?raw=true).

### Please install it.

    cd "C:\android-sdk\platform-tools"
    adb forward tcp:5555 localabstract:/adb-hub
    adb connect localhost:5555
    adb -s localhost:5555 install app-release.apk
    adb shell
    am start -n jp.gr.java_conf.ya.wearmusicplayer/.MainActivity

## Usage
---

### Push audio files to your device

    cd "C:\android-sdk\platform-tools"
    adb forward tcp:5555 localabstract:/adb-hub
    adb connect localhost:5555
    adb -s localhost:5555 push foobar.mp3 /sdcard/Music/
    adb -s localhost:5555 push foobar.ogg /sdcard/Music/
    adb -s localhost:5555 push foobar.wav /sdcard/Music/
    adb -s localhost:5555 push foobar.flac /sdcard/Music/

## Libraries etc.
---

* [Android Building Audio Player Tutorial](http://www.androidhive.info/2012/03/android-building-audio-player-tutorial/)

## Licence
---

[Apache License, 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Author
---

[YA-androidapp](https://github.com/YA-androidapp)

---

Copyright (c) 2014 YA-androidapp(https://github.com/YA-androidapp) All rights reserved.