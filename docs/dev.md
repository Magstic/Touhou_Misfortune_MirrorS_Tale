# Development

## Environment

- **Visual Studio Code**
  - JAVA Runtime: Zulu JDK 17
    - (No joke, but it doesn't matter — as long as the syntax is correct.)
  - Libraries: `cldc_1.1.jar`, `midp_2.0.jar`, `jsr082_1.1.jar`


## Tree

See [Tree](docs/tree.md).

## Tools

### Build Tools

| Tools | Path | Description |
|------|------|------|
| FontDat | `tools/FontDatBuilder.java` | Build bitmap font from TTF |
| I18nPack | `tools/I18nPackBuilder.java` | Build i18n resource pack |

### Helper Tools

| Tools | Path | Description |
|------|------|------|
| CcDatCropViewer | `tools/CcDatCropViewer.java` | View and crop cc.dat |