<img src="common/src/main/resources/icon.png" width="96" alt="caramelChat アイコン"/>

# caramelChat-Tahoe

**Minecraft で CJK言語入力のための拡張IME（入力メソッドエディタ）サポート - macOS 26 Tahoe 対応フォーク版**

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-green.svg)](https://minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)

🍯 **[LemonCaramel/caramelChat](https://github.com/LemonCaramel/caramelChat) からフォーク**

🌏 **[日本語版 README](README-ja.md)** | **[English README](README.md)**

---

## 🚨 重要なお知らせ

これは **macOS 26 Tahoe の互換性問題に対処する**ために作成された**コミュニティフォーク**です。

**一般的なcaramelChatのサポート**については、[オリジナルリポジトリ](https://github.com/LemonCaramel/caramelChat)をご参照ください。

**macOS 26 Tahoe 固有の問題**については、このフォークの[Issues ページ](https://github.com/miyakawa2449/caramelChat/issues)をご利用ください。

---

## 📕 概要

caramelChat-Tahoe は、Minecraft で CJK言語（中国語、日本語、韓国語）のシームレスなIME入力体験を提供するオリジナル caramelChat MOD の特別なフォーク版で、**macOS 26 Tahoe の互換性に特化**しています。

オリジナルの caramelChat は [CocoaInput](https://github.com/Axeryok/CocoaInput) にインスパイアされ、日本語入力時の未確定文字をリアルタイムで表示します。しかし、macOS 26 Tahoe との互換性問題により、このフォークでは LWJGL/GLFW を使用した新しい実装を開発中です。

### ✨ 主な機能

- **macOS 26 Tahoe 対応**: 最新 macOS への専用互換性
- **リアルタイム未確定文字表示**: 変換前のひらがな入力をリアルタイムで表示
- **マルチプラットフォーム対応**: Windows、macOS、Linux で動作
- **マルチローダー対応**: Fabric、Forge、NeoForge をサポート
- **拡張チャット体験**: CJK言語入力のための改善された視覚的フィードバック
- **キーボード状態表示**: 現在の入力メソッドの視覚的インジケーター

### 🔄 オリジナル版との違い

- **macOS 26 検出**: macOS 26 ユーザーへの強化された検出と警告
- **LWJGL/GLFW 実装**: （開発中）CocoaInput-lib を置き換える新しい IME ハンドラー
- **Java 21 LTS**: 統一された開発環境
- **拡張デバッグ**: macOS 26 問題のトラブルシューティングのための詳細ログ

---

## 🚀 クイックスタート

### 必要条件

- **Minecraft**: 1.21.10+
- **Java**: 21 LTS以降
- **MODローダー**: Fabric 0.17.3+ / Forge / NeoForge

### インストール

1. [フォーク版リリース](https://github.com/miyakawa2449/caramelChat/releases)から最新版をダウンロード
2. `.jar`ファイルを`mods`フォルダーに配置
3. お好みのMODローダーでMinecraftを起動
4. 日本語IMEを有効にして、チャットで入力開始（Tキー）

---

## 💻 対応状況

### オペレーティングシステム対応

|             OS              |        状態        |                    備考                     |
|:---------------------------:|:-----------------:|:-------------------------------------------:|
|    **Windows** (x86_64)     |     🟢 対応済み     |               完全サポート                |
|     **Windows** (arm64)     |     🔴 未対応      |           まだサポートされていません           |
|      **macOS** (Intel)      |     🟢 対応済み     |               完全サポート                |
|  **macOS** (Apple Silicon)  |     🟢 対応済み     |               完全サポート                |
|    **macOS 26 Tahoe**       |     🟡 開発中      | **このフォークの主要焦点** - LWJGL/GLFW ソリューション開発中 |
|   **Linux X11** (x86_64)    |    🟡 部分対応     |         設定が必要な場合があります          |
|    **Linux X11** (arm64)    |     🔴 未対応      |           まだサポートされていません           |
| **Linux Wayland** (x86_64)  |    🟡 部分対応     |     下記Waylandトラブルシューティング参照    |
|  **Linux Wayland** (arm64)  |     🔴 未対応      |           まだサポートされていません           |

### MODローダー対応

|    プラットフォーム    |     状態      |      バージョン      |
|:-------------------:|:-----------:|:------------------:|
| Fabric / Quilt      | 🟢 対応済み   |      0.17.3+       |
|       Forge         | 🟢 対応済み   |  1.21.10-60.0.15+  |
|      NeoForge       | 🟢 対応済み   |  21.10.47-beta+    |

---

## ⚠️ macOS 26 Tahoe の状況

### 現在の状況

**🚧 開発中**: LWJGL/GLFW ベース IME 実装

**現在の問題**:
- 未確定文字表示が最初の単語で約50%の確率で動作
- 後続の単語で未確定文字がほとんど表示されない
- 実際の入力は継続しているが、表示が途中で停止する場合がある

**根本原因**: CocoaInput-lib と macOS 26 Tahoe（2025年9月リリース）の非互換性

### 開発進捗

- ✅ **問題診断**: CocoaInput-lib + macOS 26 互換性問題を特定
- ✅ **環境設定**: Java 21 LTS 統一開発環境
- ✅ **調査レポート**: 包括的な分析完了
- 🚧 **LWJGL/GLFW 実装**: フェーズ1 - 進行中
- ⏳ **テスト・最適化**: フェーズ2 - 待機中
- ⏳ **リリース**: フェーズ3 - 待機中

### macOS 26 ユーザーの方へ

1. **現在の回避策**: 利用できません - 確定文字入力は正常に動作します
2. **問題報告**: このフォークの [Issues](https://github.com/miyakawa2449/caramelChat/issues) をご利用ください
3. **更新情報**: このリポジトリをウォッチして開発進捗を追跡してください

---

## 🛠️ トラブルシューティング

### macOS 14.0+ (Sonoma) 入力ツールチップ

macOS Sonoma以降のバージョンを使用している場合、以下の問題が発生する可能性があります：
- 高速入力時に文字がスキップされる
- システムキー押下時（入力ソース切り替えなど）にクライアントがクラッシュする

**解決策**: 入力ツールチップをシステム全体で無効にする：

```bash
sudo mkdir -p /Library/Preferences/FeatureFlags/Domain
sudo /usr/libexec/PlistBuddy -c "Add 'redesigned_text_cursor:Enabled' bool false" /Library/Preferences/FeatureFlags/Domain/UIKit.plist
```

その後、Macを再起動してください。

### Linux Wayland

**解決策**: より良いWayland互換性のためにWayGL MODを使用：
- **WayGL**: [Modrinth](https://modrinth.com/mod/waygl) | [GitHub](https://github.com/wired-tomato/WayGL)

### Linux Fcitx5

1. `fcitx5-config-qt`を開く
2. `Fcitx Configuration → Addons`に移動
3. `X Input Method Frontend`を見つけて設定ボタンをクリック
4. `Use On The Spot Style (Needs restarting)`を有効にする
5. システムを再起動

---

## 🏗️ 開発

### ソースからのビルド

```bash
git clone https://github.com/miyakawa2449/caramelChat.git
cd caramelChat
./gradlew build
```

### 必要条件

- **Java**: 21 LTS
- **Gradle**: 8.14.1+

### プロジェクト構造

```
caramelChat/
├── common/          # 全プラットフォーム共通コード
├── fabric/          # Fabric固有の実装
├── forge/           # Forge固有の実装
├── neoforge/        # NeoForge固有の実装
└── reports/         # 調査・開発レポート（フォーク固有）
```

---

## 📞 サポート・Issues

### このフォーク版について（macOS 26 Tahoe 問題）

- **バグレポート**: [miyakawa2449/caramelChat Issues](https://github.com/miyakawa2449/caramelChat/issues)
- **機能リクエスト**: macOS 26 互換性に関連するもの
- **開発更新**: このリポジトリをウォッチ

### オリジナル caramelChat について（一般的な問題）

- **一般的な問題**: [LemonCaramel/caramelChat Issues](https://github.com/LemonCaramel/caramelChat/issues)
- **オリジナルドキュメント**: [オリジナル Wiki](https://github.com/LemonCaramel/caramelChat/wiki)
- **オリジナルリリース**: [オリジナルリリース](https://github.com/LemonCaramel/caramelChat/releases)

---

## 🚀 貢献

特に以下の分野での貢献を歓迎します：

- **macOS 26 互換性**: LWJGL/GLFW 実装の支援
- **テスト**: 異なる macOS 26 構成でのテスト
- **バグレポート**: macOS 26 固有の問題の報告
- **ドキュメント**: macOS 26 トラブルシューティングガイドの改善

### 開発ガイドライン

- 既存のコードスタイルと規約に従う
- macOS 26 互換性改善に焦点を当てる
- 新機能にはテストを含める
- 必要に応じてドキュメントを更新

---

## 📜 ライセンス

caramelChat-Tahoe は、オリジナルプロジェクトと同じ [GNU LGPLv3](LICENSE) の下でライセンスされています。

---

## 🙏 謝辞

- **[LemonCaramel/caramelChat](https://github.com/LemonCaramel/caramelChat)**: オリジナルプロジェクトとインスピレーション
- **[CocoaInput](https://github.com/Axeryok/CocoaInput)**: オリジナル IME 実装リファレンス
- **[CocoaInput-lib](https://github.com/Korea-Minecraft-Forum/CocoaInput-lib)**: ネイティブライブラリの基盤
- **Minecraft MODコミュニティ**: ツール、ドキュメント、サポートのために

---

*macOS 26 Tahoe 互換性のための特別フォーク - Minecraft CJKコミュニティのために ❤️ で作られました*