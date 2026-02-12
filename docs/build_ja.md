[English](build_en.md) | [繁體中文](build.md)

# ビルド

## 必要な環境

### 共通

以下のソフトウェアを順番にインストールしてください（両プラットフォーム共通）：

1. **[Zulu JDK 8](https://www.azul.com/downloads/?version=java-8-lts&package=jdk#zulu)**
   - Linux ではパッケージマネージャ経由でもインストール可能：[Azul Zulu Doc](https://docs.azul.com/core/install/linux-ca-deb)
   - 他の OpenJDK ディストリビューションでも問題ありません

2. **[Apache Ant](https://ant.apache.org/bindownload.cgi)**
   - Windows：zip をダウンロード・展開し、ANT_HOME の `PATH` をシステム環境変数に追加
   - Linux：`sudo apt install ant`

3. **[proguard.jar](https://github.com/Guardsquare/proguard/releases)**
   - ダウンロード後、zip 内の `lib/proguard.jar` をプロジェクトの `lib/` に配置

4. **[antenna-bin-1.2.1-beta.jar](https://sourceforge.net/projects/antenna/files/antenna/Antenna%201.2.1-beta/)**
   - ダウンロード後、プロジェクトの `lib/` に配置

### Windows / Linux

5. 以下の**いずれか一つ**をインストールしてください（`build.xml` が自動検出します）：

   - **[Java ME SDK 3.4](https://www.oracle.com/java/technologies/javame-sdk-downloads.html)**（Windows のみ）
     - oracle-jmesdk-3-4-rr-win32-bin.exe
     - デフォルトのインストールパス：`C:\Java_ME_platform_SDK_3.4`

   - **[Sun Java Wireless Toolkit 2.5.2](https://www.oracle.com/java/technologies/java-archive-downloads-javame-downloads.html)**（Windows / Linux）
     - Windows：sun_java_wireless_toolkit-2.5.2_01-win.exe（デフォルトパス：`C:\WTK2.5.2_01`）
     - Linux：sun_java_wireless_toolkit-2.5.2_01-linuxi486.bin.sh（デフォルトパス：`~/WTK2.5.2`）
       ```bash
       chmod +x sun_java_wireless_toolkit-2.5.2_01-linuxi486.bin.sh
       ./sun_java_wireless_toolkit-2.5.2_01-linuxi486.bin.sh
       ```

   - 別の場所にインストールした場合は、ビルドスクリプトの `wtk.home` を変更してください

### Linux 追加手順

   - `preverify` は 32 ビットバイナリです。64 ビット環境では互換ライブラリが必要です (Debianを例として)：
     ```bash
     sudo dpkg --add-architecture i386
     sudo apt update
     sudo apt install libc6:i386 libstdc++6:i386 zlib1g:i386 \
                      libxt6:i386 libxext6:i386 libx11-6:i386
     ```

---

> NOTE 1: `Java ME SDK 3.4` と `Sun Java Wireless Toolkit 2.5.2` のダウンロードには Oracle アカウントが必要です（正直かなり面倒です……信頼できるサードパーティから依存ファイルを入手できれば、インストーラの煩雑さを省けます）。

> NOTE 2: `Java ME SDK 3.0` も `3.4` の代替として使用できる可能性があります。違いは `preverify` のバージョンのみです。ただし、`build.xml` 内の `wtk.home` を `3.0` のインストールパスに変更する必要があります。

## ビルドコマンド

| コマンド | 説明 |
|----------|------|
| `ant` | 通常ビルド |
| `ant s` | 簡易 BGM ビルド |
| `ant run` | 通常ビルド＋エミュレータ起動 |
| `ant s run` | 簡易 BGM ビルド＋エミュレータ起動 |
| `ant compile` | コンパイルのみ |
| `ant clean` | ビルド成果物を削除 |

ビルド完了後、`dist/` ディレクトリに JAR ファイルが生成されます。

`run` コマンドを使用する場合は、まず `freej2me.jar` をプロジェクトの `emulator` ディレクトリに配置する必要があります。