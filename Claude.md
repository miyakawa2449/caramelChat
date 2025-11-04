# caramelChat MOD Development Guide

## プロジェクト概要

caramelChatは、Minecraft Java EditionでCJK（中韓日）言語のIME入力体験を向上させるMODです。
現在、日本語入力時に未確定文字（変換前の文字）が表示されない問題があり、これを修正することが目標です。

## 開発目標

### 主要な課題
1. **日本語入力時に未確定文字が表示されない**
   - 症状：日本語入力中、変換前の文字（プリエディット）がチャット欄やカンバンに表示されない
   - 現状：確定後に初めて文字が表示される
   - 期待：入力中の文字がリアルタイムで表示される

2. **Minecraft 1.21.10への対応**
   - 現在：1.21.4まで対応
   - 目標：1.21.10で動作するようにする

### 技術的背景
- このMODは韓国語ユーザー向けに最適化されている
- 日本語と中国語のサポートは副次的なため、日本語IMEで問題が発生している
- CocoaInput-libをネイティブライブラリとして使用

## プロジェクト構造

```
caramelChat/
├── build.gradle              # Gradleビルド設定
├── gradle/                   # Gradleラッパー
├── src/
│   └── main/
│       ├── java/
│       │   └── caramelchat/
│       │       ├── CaramelChat.java           # MODのメインクラス
│       │       ├── config/                    # 設定管理
│       │       ├── mixin/                     # Minecraft既存処理への割り込み
│       │       │   ├── EditBoxMixin.java     # テキスト入力処理
│       │       │   └── ScreenMixin.java      # 画面描画処理
│       │       └── platform/                  # プラットフォーム固有処理
│       │           └── common/
│       │               └── IMEHandler.java   # IME処理のコア
│       └── resources/
│           ├── caramelchat.mixins.json       # Mixin設定
│           ├── fabric.mod.json               # MOD情報（Fabric版）
│           └── META-INF/
└── libs/                                      # ネイティブライブラリ（CocoaInput-lib）
```

## 重要なファイル

### 1. EditBoxMixin.java
- **役割**: テキスト入力欄（チャット欄など）の処理を拡張
- **問題箇所の可能性**: 未確定文字の描画処理がここで行われている

### 2. IMEHandler.java
- **役割**: IME（Input Method Editor）との連携
- **CocoaInput-libとの接続部分**: ネイティブコードとのブリッジ

### 3. caramelchat.mixins.json
- **役割**: どのMinecraftクラスに割り込むかを定義
- **重要**: 新しいMixinを追加する場合はここに登録が必要

## 技術スタック

- **言語**: Java 21
- **MODローダー**: Fabric / Forge / NeoForge対応
- **ビルドツール**: Gradle 8.5
- **Minecraftバージョン**: 1.20.3 - 1.21.4（目標：1.21.10）
- **ネイティブライブラリ**: CocoaInput-lib

## 開発環境

### 必要なツール
- Java 21 (openjdk@21)
- VS Code + Extension Pack for Java
- Gradle（プロジェクトに含まれる）
- Minecraft Java Edition 1.21.x

### ビルドコマンド

```bash
# ビルド
./gradlew build

# 開発モードでMinecraft起動
./gradlew runClient

# クリーンビルド
./gradlew clean build

# テスト実行
./gradlew test
```

## 開発ガイドライン

### コーディング規約
1. **Java命名規則に従う**
   - クラス: PascalCase
   - メソッド/変数: camelCase
   - 定数: UPPER_SNAKE_CASE

2. **Mixinの使用**
   - `@Mixin`: 既存クラスへの割り込み
   - `@Inject`: メソッド実行前後に処理を追加
   - `@ModifyVariable`: 変数を書き換え
   - `@Redirect`: メソッド呼び出しを置き換え

3. **ログ出力**
   ```java
   // デバッグログ
   CaramelChat.LOGGER.info("Debug: " + message);
   CaramelChat.LOGGER.debug("Detailed: " + details);
   ```

### デバッグ方法

1. **ログ追加**
   ```java
   System.out.println("DEBUG: preedit text = " + preeditText);
   ```

2. **リビルド不要な変更**
   - ログ出力の追加は、Minecraftを再起動するだけで反映される

3. **完全リビルドが必要な変更**
   - 新しいクラスの追加
   - Mixin設定の変更
   - ネイティブライブラリの変更

## 既知の問題

### 日本語入力の問題
- **現象**: 未確定文字（プリエディット）が表示されない
- **推測される原因**: 
  - EditBoxMixinでの描画処理が韓国語向けに最適化されている
  - IMEHandlerでのイベント処理が日本語IMEに対応していない可能性
- **確認方法**: 
  1. `./gradlew runClient`でMinecraft起動
  2. チャットを開く（Tキー）
  3. 日本語入力で「nihongo」と入力
  4. 変換候補は左上に表示されるが、チャット欄には何も表示されない

### macOS Sonoma以降の問題
- 高速入力時に文字がスキップされる
- システムキー押下時にクラッシュする可能性
- **回避策**: ターミナルで以下を実行
  ```bash
  sudo mkdir -p /Library/Preferences/FeatureFlags/Domain
  sudo /usr/libexec/PlistBuddy -c "Add 'redesigned_text_cursor:Enabled' bool false" /Library/Preferences/FeatureFlags/Domain/UIKit.plist
  ```

## 参考情報

### 元のプロジェクト
- **GitHub**: https://github.com/LemonCaramel/caramelChat
- **ライセンス**: GNU LGPLv3
- **貢献歓迎**: "All contributions are welcome"

### CocoaInput-lib
- **GitHub**: https://github.com/Korea-Minecraft-Forum/CocoaInput-lib
- **役割**: マルチプラットフォームIMEサポート
- **対応言語**: 日本語、中国語、韓国語

### 関連ドキュメント
- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Minecraft Forge Documentation](https://docs.minecraftforge.net/)
- [Mixin Documentation](https://github.com/SpongePowered/Mixin/wiki)

## 開発のヒント

### よくある質問

**Q: ビルドに失敗する**
```bash
# Gradleキャッシュをクリア
./gradlew clean --refresh-dependencies
./gradlew build
```

**Q: Minecraftが起動しない**
```bash
# 詳細ログを表示
./gradlew runClient --info
```

**Q: 変更が反映されない**
- ログ追加：Minecraft再起動のみでOK
- コード変更：`./gradlew build`してからMinecraft再起動
- Mixin変更：完全リビルド必要

### デバッグのワークフロー

1. **問題を再現**
   ```bash
   ./gradlew runClient
   # Minecraftでチャットを開いて日本語入力
   ```

2. **ログを追加**
   ```java
   System.out.println("DEBUG: " + variableName);
   ```

3. **再起動して確認**
   ```bash
   # Minecraftを終了
   ./gradlew runClient
   # ログを確認
   ```

4. **修正を実装**
   ```bash
   # コード修正後
   ./gradlew build
   ./gradlew runClient
   ```

## 次のステップ

### Phase 1: 問題箇所の特定
1. EditBoxMixin.javaにデバッグログを追加
2. IMEHandler.javaのイベント処理を確認
3. 日本語と韓国語で処理が異なる箇所を特定

### Phase 2: 修正実装
1. 日本語IME用の処理を追加
2. プリエディットテキストの描画ロジックを修正
3. テスト・デバッグ

### Phase 3: 1.21.10対応
1. build.gradleのMinecraftバージョンを更新
2. API変更点を確認・修正
3. 動作確認

---

## Claude Codeでの作業例

### コード調査
```bash
claude "EditBoxMixin.javaでプリエディットテキストを処理している箇所を見つけて説明して"
```

### デバッグログ追加
```bash
claude "IMEHandler.javaの主要メソッドにデバッグログを追加して、何が起きているか確認できるようにして"
```

### 問題修正
```bash
claude "日本語入力時にプリエディットテキストがチャット欄に表示されるように修正して"
```

---

**更新日**: 2025-11-04
**開発者**: miyakawa2449 (fork)
**元プロジェクト**: LemonCaramel/caramelChat
