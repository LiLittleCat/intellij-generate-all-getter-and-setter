# Generate All Getter And Setter

![Build](https://github.com/LiLittleCat/intellij-generate-all-getter-and-setter/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/18969-generate-all-getter-and-setter.svg)](https://plugins.jetbrains.com/plugin/18969-generate-all-getter-and-setter)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18969-generate-all-getter-and-setter.svg)](https://plugins.jetbrains.com/plugin/18969-generate-all-getter-and-setter)

![icon](src/main/resources/META-INF/pluginIcon.svg)

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

---
Plugin based on the [IntelliJ Platform Plugin Template][template].
## Usage

- Use <kbd>.allget</kbd> to generate all getter methods for POJO.

![example-allget](example/GenerateAllGetter.gif)

- Use <kbd>.allset</kbd> to generate all setter methods without default value for POJO.

![example-allset](example/GenerateAllSetterWithoutDefaultValue.gif)

- Use <kbd>.allsetv</kbd> to generate all setter methods with default value for POJO.

![example-allsetv](example/GenerateAllSetterWithDefaultValue.gif)


## Special Thanks

- Thanks [XiaoYao][XiaoYao's link] for helping design the plugin icon.

[XiaoYao's link]: https://space.bilibili.com/15765234

[template]: https://github.com/JetBrains/intellij-platform-plugin-template


