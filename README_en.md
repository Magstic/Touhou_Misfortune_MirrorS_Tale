[繁體中文](README.md) | [日本語](README_ja.md)

# 東方氷幻鏡 ～ Misfortune Mirror's Tale

## Info

Ported from "Touhou Hyougenkyou Plus V1.0.3".

Includes numerous bug fixes and quality-of-life improvements over the original.

---

## Item

See [TODO](https://github.com/Magstic/Touhou_Misfortune_MirrorS_Tale/issues/1).

---

## Play

| Item | Recommended |  | Item | Recommended |
|------|------|------|------|------|
| Platform | *MIDP 2.0, CLDC 1.1* |  | CPU & RAM | *≥ 434 MHz*, *JVM ≥ 1.5 MB* |
| Screen | *240 x 240* |  |Optional | *JSR-82 (BlueTooth Replay)*|

### PC

Recommended: customized [**FreeJ2ME-Plus**](https://github.com/Magstic/freej2me-plus/releases/tag/VirtualMIDISynth) + [**VirtualMIDI Synth**](https://coolsoft.altervista.org/en/virtualmidisynth) ([**Nokia 3110c.SF2**](https://musical-artifacts.com/artifacts/828)). This is likely the closest experience to the original Docomo mixing (though VirtualMIDI Synth is Windows-only).

KEmulatormod runs this port very poorly and does not support in-app VirtualMIDI Synth volume control or MIDI breakpoint playback — not recommended.

### Android

[JL-MOD](https://github.com/woesss/JL-Mod/releases) with custom SF2 soundfont support is the top choice.

Of course, I guarantee you won't enjoy playing an STG by tapping glass, let alone the still-garbage mixing on Android even with SF2 lol.

JL-MOD's JSR-82 implementation also has issues, meaning Bluetooth replay send/receive won't work properly (J2ME-Loader can receive but not send — and the sound quality is another story).

### Real Device

Recommended to play with **sound off** or the **simplified MIDI build**.

Tested on Nokia N86: silent mode maintains `14`–`16` FPS; with sound enabled, FPS ranges from `10`–`16` depending on danmaku density.

Under these conditions, Easy and Normal can be played fairly smoothly.

Bullet rendering consumes nearly all CPU power; adding MIDI on top makes it even worse.

If your device has CPU ≥ 434 MHz and JVM heap ≥ 2.0 MB, you may be able to run Normal smoothly — or even Hard!

---

## Build

[中文（繁體）](docs/build.md) / [English](docs/build_en.md) / [日本語](docs/build_ja.md)。

---

## Thanks

**L-Garden** & **トッパツプラン** — Created this amazing STG

**Keitai Archive** — Preserved this title

**ChatGPT 5.2** — 99% of the code cleanup and refactoring work

**Claude 4.6 Opus** — Drafting and translation of some MD documents, and glyph verification

**Fusion Pixel 12Px** — Bitmap font

---

## About

This project is a derivative work of "東方氷幻鏡 ～ Misfortune Mirror's Tale".

"東方氷幻鏡 ～ Misfortune Mirror's Tale" is a fan-made derivative of the Touhou Project, developed by doujin circles L-Garden & トッパツプラン. All rights belong to L-Garden.

If you are affiliated with L-Garden and do not wish for this project to exist, I sincerely apologize and welcome you to contact me anytime via the Email on my [homepage](https://magstic.art/).

You are welcome to clone and build locally for personal play, but **redistribution of JAR files in any form is strictly prohibited**.
