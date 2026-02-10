[日本語](README_ja.md) | [English](README_en.md)

# 東方氷幻鏡 ～ Misfortune Mirror's Tale

## Info

基於『東方冰幻鏡 Plus V1.0.3』進行移植。

在修復原版 BUG 的同時，進行了諸多的便利性改進。

---

## Item

請見 [TODO](https://github.com/Magstic/Touhou_Misfortune_MirrorS_Tale/issues/1)。

---

## Play

| Item | Recommended |  | Item | Recommended |
|------|------|------|------|------|
| Platform | *MIDP 2.0, CLDC 1.1* |  | CPU & RAM | *≥ 434 MHz*, *JVM ≥ 1.5 MB* |
| Screen | *240 x 240* |  |Optional | *JSR-82（BlueTooth Replay）*|

---

### PC

推薦使用客製化的 [**FreeJ2ME-Plus**](https://github.com/Magstic/freej2me-plus/releases/tag/VirtualMIDISynth) + [**VirtualMIDI Synth**](https://coolsoft.altervista.org/en/virtualmidisynth)（[**Nokia 3110c.SF2**](https://musical-artifacts.com/artifacts/828)），這可能是最接近 Docomo 混音的遊玩方式（不過 VirtualMIDI Synth 僅限 Windows）。

KEmulatormod 執行該移植版非常卡頓，且不支援程式內控制 VirtualMIDI Synth 的音量，MIDI 的斷點播放也無法做到，因此並不推薦用來執行該遊戲。

---

### Android

支援自訂 SF2 音源的 [JL-MOD](https://github.com/woesss/JL-Mod/releases) 是至高選擇。

當然，我保證你不會想戳玻璃 PLAY STG，更不提儘管有 SF2 加持，但混音依然垃圾的 ANDROID lol。

此外，JL-MOD 的 JSR-82 也有問題，這意味著無法正常使用藍牙收發 Replay（J2ME-Loader 可以接收，但無法發送，音質就另當別論了）。

---

### Real Device

推薦在 **關閉聲音** 或者 **使用 MIDI 降級版** 的條件下使用真機遊玩。

使用 Nokia N86 測試，無聲狀態下可以維持 `14`-`16` FPS，但有聲狀態下，則根據彈幕壓力呈現 `10`-`16` FPS。

這種情況下，可以較為流暢地遊玩 Easy 和 Normal 難度。

彈幕的渲染會吃掉幾乎全部 CPU 的算力，如果再加上 MIDI，則更為雪上加霜。

如果您有 CPU 效能 ≥ 434 MHz，且 JVM 堆 ≥ 2.0 MB 的裝置，或許可以流暢運行 Normal，甚至 Hrad！

---

## Build

[中文（繁體）](docs/build.md) / [English](docs/build_en.md) / [日本語](docs/build_ja.md)。

---

## Thanks

**L-Garden** & **トッパツプラン**：完成了如此驚人的 STG

**Keitai Archive**：存檔了這款作品

**ChatGPT 5.2**：99% 的程式碼整理，重構工作，最終檢查

**Claude 4.6 Opus**：MD 文檔的草案和翻譯，遊戲内字形的檢查

**Fusion Pixel 12Px**：提供位圖字型

---

## About

本專案為《東方氷幻鏡 ～ Misfortune Mirror's Tale》的衍生作品。

《東方氷幻鏡 ～ Misfortune Mirror's Tale》是《東方 Project》的衍生作品，由同人社團 L-Garden & トッパツプラン 開發，版權歸 L-Garden 所有。

若您是 L-Garden 所屬，且不希望這種行為，我深感歉意，且隨時歡迎使用 [主頁](https://magstic.art/) 的 Email 聯絡我。

若您有興趣，可 Clone 到本地自行編譯遊玩，但 **禁止任何形式的 JAR 分發**。