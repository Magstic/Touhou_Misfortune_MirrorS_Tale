[English](README_en.md) | [繁體中文](README.md)

# 東方氷幻鏡 ～ Misfortune Mirror's Tale

## Info

『東方氷幻鏡 Plus V1.0.3』をベースに移植。

オリジナル版のバグ修正に加え、多数の利便性向上を行っています。

---

## Item

[TODO](https://github.com/Magstic/Touhou_Misfortune_MirrorS_Tale/issues/1) を参照。

To improve compatibility with a wider range of real devices, this project will undergo a rewrite of the sprite clip and the gc system (maybe long time).

---

## Play

| Item | Recommended |  | Item | Recommended |
|------|------|------|------|------|
| Platform | *MIDP 2.0, CLDC 1.1* |  | CPU & RAM | *≥ 434 MHz*, *JVM ≥ 1.5 MB* |
| Screen | *240 x 240* |  |Optional | *JSR-82（BlueTooth Replay）*|

---

### PC

カスタマイズ版 [**FreeJ2ME-Plus**](https://github.com/Magstic/freej2me-plus/releases/tag/VirtualMIDISynth) + [**VirtualMIDI Synth**](https://coolsoft.altervista.org/en/virtualmidisynth)（[**Nokia 3110c.SF2**](https://musical-artifacts.com/artifacts/828)）の組み合わせを推奨。Docomo のオリジナルミキシングに最も近い再生環境です（ただし VirtualMIDI Synth は Windows 限定）。また、この Fork は移植版の RMS 永続化を正しく実装しています。

KEmulatormod ではこの移植版の動作が非常に重く、アプリ内での VirtualMIDI Synth 音量制御や MIDI のブレークポイント再生にも対応していないため、推奨しません。

---

### Android

カスタム SF2 サウンドフォントに対応した [JL-MOD](https://github.com/woesss/JL-Mod/releases) が最良の選択です。

もちろん、ガラスをタップして STG をプレイしたいとは思わないでしょうし、SF2 を使っても Android のミキシングは相変わらずひどいですが lol。

また、JL-MOD の JSR-82 実装にも問題があり、Bluetooth による Replay の送受信が正常に動作しません（J2ME-Loader は受信可能ですが送信不可 — 音質はまた別の話です）。

---

### Real Device

**サウンドオフ** または **簡易 MIDI ビルド** での実機プレイを推奨。

Nokia N86 でのテスト結果：無音時は `14`〜`16` FPS を維持、有音時は弾幕密度に応じて `10`〜`16` FPS。

この条件下では、Easy と Normal を比較的スムーズにプレイできます。

弾幕の描画が CPU パワーのほぼ全てを消費し、MIDI を加えるとさらに悪化します。

CPU ≥ 434 MHz、JVM ヒープ ≥ 2.0 MB のデバイスであれば、Normal をスムーズに、あるいは Hard でもプレイできるかもしれません！

---

## Build

本プロジェクトのビルドにはいかなる IDE も必要なく、ルートディレクトリに同梱されている `build.xml` だけで作業を 100% 完了できます。

[日本語](docs/build_ja.md) | [中文（繁體）](docs/build.md) | [English](docs/build_en.md)

---

## Thanks

**L-Garden** & **トッパツプラン** — この驚異的な STG を完成させた

**Keitai Archive** — この作品をアーカイブしてくれた

**ChatGPT 5.2** — コード整理・リファクタリング・最終チェック作業の 99%

**Claude 4.6 Opus** — 一部の MD ドキュメントの草案・翻訳、および字形チェック

**Fusion Pixel 12Px** — ビットマップフォント

---

## About

本プロジェクトは『東方氷幻鏡 ～ Misfortune Mirror's Tale』の派生作品です。

『東方氷幻鏡 ～ Misfortune Mirror's Tale』は東方 Project の二次創作であり、同人サークル L-Garden & トッパツプラン が開発、著作権は L-Garden に帰属します。

L-Garden の関係者の方で、本プロジェクトの存在を望まない場合は、深くお詫び申し上げます。[ホームページ](https://magstic.art/)の Email からいつでもご連絡ください。

### Clone してローカルでビルド・プレイすることは歓迎しますが、**いかなる形式の JAR 再配布も禁止** です。
