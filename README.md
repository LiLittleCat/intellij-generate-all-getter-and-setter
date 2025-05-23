<img src="src/main/resources/META-INF/pluginIcon.svg" align="right" width="128" height="128" alt="icon"/>

# Generate All Getter And Setter

[中文文档](README_zh.md)

![Build](https://github.com/LiLittleCat/intellij-generate-all-getter-and-setter/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/18969-generate-all-getter-and-setter.svg)](https://plugins.jetbrains.com/plugin/18969-generate-all-getter-and-setter)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18969-generate-all-getter-and-setter.svg)](https://plugins.jetbrains.com/plugin/18969-generate-all-getter-and-setter)

Generate All Getter And Setter is an IntelliJ IDEA plugin that generates all getter and setter methods for POJO
use [postfix completion](https://www.jetbrains.com/help/idea/settings-postfix-completion.html) like
<kbd>.var</kbd>.

[comment]: <> (<!-- Plugin description -->)

[comment]: <> (This Fancy IntelliJ Platform Plugin is going to be your implementation of the brilliant ideas that you have.)

[comment]: <> (This specific section is a source for the [plugin.xml]&#40;/src/main/resources/META-INF/plugin.xml&#41; file which will be extracted by the [Gradle]&#40;/build.gradle.kts&#41; during the build process.)

[comment]: <> (To keep everything working, do not remove `<!-- ... -->` sections. )

[comment]: <> (<!-- Plugin description end -->)

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Generate All Getter And Setter"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://plugins.jetbrains.com/plugin/18969-generate-all-getter-and-setter/versions)
  and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

- Use <kbd>.allget</kbd> to generate all getter methods for POJO.

![example-allget](example/generate-all-getter.gif)

- Use <kbd>.allset</kbd> to generate all setter methods without default value for POJO.

![example-allset](example/generate-all-setter-without-default-value.gif)

- Use <kbd>.allsetv</kbd> to generate all setter methods with default value for POJO.

![example-allsetv](example/generate-all-setter-with-default-value.gif)

- Use <kbd>.allsetc</kbd> to generate all setter methods with chain style for POJO.

![example-allsetc](example/generate-all-setter-with-chain-style.gif)

- Use <kbd>.allsetcv</kbd> to generate all setter methods with default value and chain style for POJO.

![example-allsetcv](example/generate-all-setter-with-default-value-and-chain-style.gif)

## Special Thanks

- Thanks [XiaoYao][XiaoYao's link] for helping design the plugin icon.
- [IntelliJ Platform Plugin Template][template].

---

[XiaoYao's link]: https://space.bilibili.com/15765234
[JetBrains's link]: https://jb.gg/OpenSourceSupport
[template]: https://github.com/JetBrains/intellij-platform-plugin-template


