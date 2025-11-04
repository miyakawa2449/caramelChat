<img src="common/src/main/resources/icon.png" width="96" alt="caramelChat Icon"/>

# caramelChat-Tahoe

**Enhanced IME (Input Method Editor) support for Minecraft with CJK language input - macOS 26 Tahoe Compatible Fork**

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-green.svg)](https://minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)

🍯 **Forked from [LemonCaramel/caramelChat](https://github.com/LemonCaramel/caramelChat)**

🌏 **[日本語版 README](README-ja.md)** | **[English README](README.md)**

---

## 🚨 Important Notice

This is a **community fork** specifically created to address **macOS 26 Tahoe compatibility issues**. 

**For general caramelChat support**, please refer to the [original repository](https://github.com/LemonCaramel/caramelChat).

**For macOS 26 Tahoe-specific issues**, use this fork's [Issues page](https://github.com/miyakawa2449/caramelChat/issues).

---

## 📕 Introduction

caramelChat-Tahoe is a specialized fork of the original caramelChat mod, designed to provide seamless IME input experience for CJK (Chinese, Japanese, Korean) languages in Minecraft, **with specific focus on macOS 26 Tahoe compatibility**.

The original caramelChat, inspired by [CocoaInput](https://github.com/Axeryok/CocoaInput), enables real-time display of pre-edit text (未確定文字) during Japanese input. However, due to compatibility issues with macOS 26 Tahoe, this fork is developing a new implementation using LWJGL/GLFW.

### ✨ Key Features

- **macOS 26 Tahoe Support**: Dedicated compatibility for the latest macOS
- **Real-time Pre-edit Text Display**: See your Japanese hiragana input before conversion
- **Multi-platform Support**: Works on Windows, macOS, and Linux
- **Multi-loader Compatibility**: Supports Fabric, Forge, and NeoForge
- **Enhanced Chat Experience**: Improved visual feedback for CJK language input
- **Keyboard Status Display**: Visual indicator for current input method

### 🔄 Differences from Original

- **macOS 26 Detection**: Enhanced detection and warning for macOS 26 users
- **LWJGL/GLFW Implementation**: (In Development) New IME handler to replace CocoaInput-lib
- **Java 21 LTS**: Unified development environment
- **Enhanced Debugging**: Detailed logging for troubleshooting macOS 26 issues

---

## 🚀 Quick Start

### Requirements

- **Minecraft**: 1.21.10+
- **Java**: 21 LTS or later
- **Mod Loader**: Fabric 0.17.3+ / Forge / NeoForge

### Installation

1. Download the latest release from [Fork Releases](https://github.com/miyakawa2449/caramelChat/releases)
2. Place the `.jar` file in your `mods` folder
3. Launch Minecraft with your preferred mod loader
4. Enable Japanese IME and start typing in chat (T key)

---

## 💻 Compatibility

### Operating System Support

|             OS              |        Status         |                    Notes                    |
|:---------------------------:|:--------------------:|:-------------------------------------------:|
|    **Windows** (x86_64)     |     🟢 Compatible     |               Full support                |
|     **Windows** (arm64)     |    🔴 Incompatible    |           Not yet supported             |
|      **macOS** (Intel)      |     🟢 Compatible     |               Full support                |
|  **macOS** (Apple Silicon)  |     🟢 Compatible     |               Full support                |
|    **macOS 26 Tahoe**       |     🟡 In Progress    | **This fork's primary focus** - Developing LWJGL/GLFW solution |
|   **Linux X11** (x86_64)    | 🟡 Partial Support   |        May require configuration        |
|    **Linux X11** (arm64)    |    🔴 Incompatible    |           Not yet supported             |
| **Linux Wayland** (x86_64)  | 🟡 Partial Support   |    See Wayland troubleshooting below    |
|  **Linux Wayland** (arm64)  |    🔴 Incompatible    |           Not yet supported             |

### Mod Loader Support

|    Platform    |    Status     |     Version     |
|:--------------:|:-------------:|:---------------:|
| Fabric / Quilt | 🟢 Compatible |    0.17.3+     |
|     Forge      | 🟢 Compatible | 1.21.10-60.0.15+ |
|    NeoForge    | 🟢 Compatible | 21.10.47-beta+ |

---

## ⚠️ macOS 26 Tahoe Status

### Current Status

**🚧 In Development**: LWJGL/GLFW-based IME implementation

**Current Issues**:
- Pre-edit text display works ~50% of the time for the first word
- Pre-edit text rarely displays for subsequent words
- Text display may freeze mid-input while actual input continues

**Root Cause**: CocoaInput-lib incompatibility with macOS 26 Tahoe (released September 2025)

### Development Progress

- ✅ **Problem Diagnosis**: Identified CocoaInput-lib + macOS 26 compatibility issue
- ✅ **Environment Setup**: Java 21 LTS unified development environment
- ✅ **Investigation Report**: Comprehensive analysis completed
- 🚧 **LWJGL/GLFW Implementation**: Phase 1 - In Progress
- ⏳ **Testing & Optimization**: Phase 2 - Pending
- ⏳ **Release**: Phase 3 - Pending

### For macOS 26 Users

1. **Current Workaround**: None available - confirmed text input works normally
2. **Reporting Issues**: Use this fork's [Issues](https://github.com/miyakawa2449/caramelChat/issues)
3. **Updates**: Watch this repository for development progress

---

## 🛠️ Troubleshooting

### macOS 14.0+ (Sonoma) Input Tooltip

If you are using macOS Sonoma or later versions, you may experience:
- Characters being skipped when typing quickly
- Client crashes when system keys are pressed (e.g., input source switch)

**Solution**: Disable the Input Tooltip system-wide:

```bash
sudo mkdir -p /Library/Preferences/FeatureFlags/Domain
sudo /usr/libexec/PlistBuddy -c "Add 'redesigned_text_cursor:Enabled' bool false" /Library/Preferences/FeatureFlags/Domain/UIKit.plist
```

Then reboot your Mac.

### Linux Wayland

**Solution**: Use WayGL mod for better Wayland compatibility:
- **WayGL**: [Modrinth](https://modrinth.com/mod/waygl) | [GitHub](https://github.com/wired-tomato/WayGL)

### Linux Fcitx5

1. Open `fcitx5-config-qt`
2. Navigate to `Fcitx Configuration → Addons`
3. Find `X Input Method Frontend` and click the settings button
4. Enable `Use On The Spot Style (Needs restarting)`
5. Reboot your system

---

## 🏗️ Development

### Building from Source

```bash
git clone https://github.com/miyakawa2449/caramelChat.git
cd caramelChat
./gradlew build
```

### Requirements

- **Java**: 21 LTS
- **Gradle**: 8.14.1+

### Project Structure

```
caramelChat/
├── common/          # Shared code for all platforms
├── fabric/          # Fabric-specific implementation
├── forge/           # Forge-specific implementation
├── neoforge/        # NeoForge-specific implementation
└── reports/         # Investigation and development reports (Fork-specific)
```

---

## 📞 Support & Issues

### For This Fork (macOS 26 Tahoe issues)

- **Bug Reports**: [miyakawa2449/caramelChat Issues](https://github.com/miyakawa2449/caramelChat/issues)
- **Feature Requests**: Related to macOS 26 compatibility
- **Development Updates**: Watch this repository

### For Original caramelChat (General issues)

- **General Issues**: [LemonCaramel/caramelChat Issues](https://github.com/LemonCaramel/caramelChat/issues)
- **Original Documentation**: [Original Wiki](https://github.com/LemonCaramel/caramelChat/wiki)
- **Original Releases**: [Original Releases](https://github.com/LemonCaramel/caramelChat/releases)

---

## 🚀 Contributing

Contributions are welcome, especially for:

- **macOS 26 Compatibility**: Help with LWJGL/GLFW implementation
- **Testing**: Test on different macOS 26 configurations
- **Bug Reports**: Report macOS 26-specific issues
- **Documentation**: Improve macOS 26 troubleshooting guides

### Development Guidelines

- Follow existing code style and conventions
- Focus on macOS 26 compatibility improvements
- Include tests for new features
- Update documentation as needed

---

## 📜 License

caramelChat-Tahoe is licensed under [GNU LGPLv3](LICENSE), same as the original project.

---

## 🙏 Acknowledgments

- **[LemonCaramel/caramelChat](https://github.com/LemonCaramel/caramelChat)**: Original project and inspiration
- **[CocoaInput](https://github.com/Axeryok/CocoaInput)**: Original IME implementation reference
- **[CocoaInput-lib](https://github.com/Korea-Minecraft-Forum/CocoaInput-lib)**: Native library foundation
- **Minecraft Modding Community**: Tools, documentation, and support

---

*Specialized fork for macOS 26 Tahoe compatibility - Made with ❤️ for the Minecraft CJK community*