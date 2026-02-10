[English](build_en.md) | [日本語](build_ja.md)

# Build

## 環境需求

### 通用

以下軟體為兩個平台共同需要的，請依序安裝：

1. **[Zulu JDK 8](https://www.azul.com/downloads/?version=java-8-lts&package=jdk#zulu)**
   - Linux 亦可透過套件管理器安裝：[Azul Zulu Doc](https://docs.azul.com/core/install/linux-ca-deb)
   - 若您習慣使用其他的 OpenJDK，那也是非常好的

2. **[Apache Ant](https://ant.apache.org/bindownload.cgi)**
   - Windows：下載 zip 解壓後將 ANT_HOME 的 `PATH` 加入系統變數
   - Linux：`sudo apt install ant`

3. **[proguard.jar](https://github.com/Guardsquare/proguard/releases)**
   - 下載後，在 zip 內並找到 `lib/proguard.jar`，然後放置於專案的 `lib/` 下

4. **[antenna-bin-1.2.1-beta.jar](https://sourceforge.net/projects/antenna/files/antenna/Antenna%201.2.1-beta/)**
   - 下載後放置於專案的 `lib/` 下

### Windows

5. **[Java ME SDK 3.4](https://www.oracle.com/java/technologies/javame-sdk-downloads.html)**
   - oracle-jmesdk-3-4-rr-win32-bin.exe
   - 預設安裝路徑：`C:\Java_ME_platform_SDK_3.4`
   - 若安裝在其他路徑，請修改打包腳本中對應的 `wtk.home`

### Linux

5. **[Sun Java Wireless Toolkit 2.5.2](https://www.oracle.com/java/technologies/java-archive-downloads-javame-downloads.html)**
   - sun_java_wireless_toolkit-2.5.2_01-linuxi486.bin.sh
   - 執行安裝（預設路徑：`~/WTK2.5.2`）：
     ```bash
     chmod +x sun_java_wireless_toolkit-2.5.2_01-linuxi486.bin.sh
     ./sun_java_wireless_toolkit-2.5.2_01-linuxi486.bin.sh
     ```
   - 若安裝在其他路徑，請修改打包腳本中對應的 `wtk.home`
   - `preverify` 為 32 位元執行檔，64 位元系統需安裝相容函式庫：
     ```bash
     sudo dpkg --add-architecture i386
     sudo apt update
     sudo apt install libc6:i386 libstdc++6:i386 zlib1g:i386 \
                      libxt6:i386 libxext6:i386 libx11-6:i386
     ```

> P.S.: 下載 `Java ME SDK 3.4` 和 `Sun Java Wireless Toolkit 2.5.2` 時，需要登入 Oracle 賬戶（說實話非常麻煩……如果可以在值得信賴的第三方渠道獲得『散裝』的依賴項，就可以免去安裝的煩瑣了。）。

## 建置指令

| 指令 | 說明 |
|------|------|
| `ant` | 正常構建 |
| `ant s` | 精簡 BGM 構建 |
| `ant run` | 正常構建並啟動模擬器 |
| `ant s run` | 精簡 BGM 構建並啟動模擬器 |
| `ant compile` | 僅編譯 |
| `ant clean` | 清理建置檔案 |

建置完成後，可在 `dist/` 目錄下取得 JAR 檔。

若想使用 `run` 指令，您需要先將 `freej2me.jar` 放到專案的 `emulator` 下。