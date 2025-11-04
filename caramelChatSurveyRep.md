# 📄 caramelChat プロジェクト解説レポート
## 〜初心者向け：日本語IME対応Minecraftモッドの仕組み〜

---

## 🎯 このモッドが解決しようとしている問題

**普通のMinecraftでの日本語入力**：
```
ユーザーが「にほんご」と入力
↓
何も表示されない（未確定文字が見えない）
↓
変換して確定すると突然「日本語」が表示される
```

**caramelChatの目標**：
```
ユーザーが「にほんご」と入力
↓
「にほんご」がリアルタイムで表示される（下線付き）
↓
変換候補を選んで「日本語」に確定
```

---

## 🏗️ システム構成（4層構造）

### 1️⃣ **ネイティブ層（一番下）**
```
┌─────────────────────────────────────┐
│ CocoaInput-lib（C/Objective-C）        │
│ └ macOSのIMEシステムと直接通信         │
└─────────────────────────────────────┘
```
**役割**: macOSのIME（日本語入力システム）とMinecraftを橋渡し

### 2️⃣ **ドライバー層**
```
┌─────────────────────────────────────┐
│ DarwinController + DarwinOperator    │
│ └ ネイティブライブラリをJavaから使用  │
└─────────────────────────────────────┘
```
**役割**: ネイティブ層からのイベントを受け取ってJavaに渡す

### 3️⃣ **ラッパー層**
```
┌─────────────────────────────────────┐
│ AbstractIMEWrapper + WrapperEditBox  │
│ └ IME処理のロジックを管理            │
└─────────────────────────────────────┘
```
**役割**: 未確定文字と確定文字の処理を制御

### 4️⃣ **Mixin層（一番上）**
```
┌─────────────────────────────────────┐
│ MixinEditBox                         │
│ └ Minecraftのテキストボックスを改造   │
└─────────────────────────────────────┘
```
**役割**: Minecraftの画面に未確定文字を表示

---

## 🔄 処理の流れ

### **日本語入力時に何が起きているか**

```
[ユーザー] → [macOS IME] → [ネイティブ層] → [ドライバー層] → [ラッパー層] → [Mixin層] → [Minecraft画面]

1. ユーザーが「にほんご」と入力
2. macOS IMEが未確定文字イベントを送信
3. ネイティブ層がsetMarkedText("にほんご")を呼び出し
4. ドライバー層がappendPreviewText("にほんご")を実行
5. ラッパー層がsetPreviewText("にほんご")を実行
6. Mixin層が下線付きで「にほんご」を表示

確定時：
1. ユーザーがスペースで変換
2. macOS IMEが確定文字イベントを送信
3. ネイティブ層がinsertText("日本語")を呼び出し
4. ドライバー層がinsertText("日本語")を実行
5. ラッパー層が通常のテキスト挿入を実行
6. Mixin層が「日本語」として確定表示
```

---

## 🔍 重要なファイルと処理内容

### **1. MixinEditBox.java** - 画面表示担当
**ファイル**: `common/src/main/java/moe/caramel/chat/mixin/MixinEditBox.java:25`

```java
// 📍 重要な箇所：未確定文字の表示方法（行64-102）
private EditBox.TextFormatter caramelChat$caretFormatter() {
    return ((original, firstPos) -> {
        // 🟢 通常モード：何もしない
        if (wrapper.getStatus() == NONE) {
            return null;
        }
        // 🟡 プレビューモード：下線付きで表示
        else {
            list.add(FormattedCharSequence.forward(input, 
                Style.EMPTY.withUnderlined(true))); // ← 下線表示
        }
    });
}
```

### **2. AbstractIMEWrapper.java** - ロジック担当
**ファイル**: `common/src/main/java/moe/caramel/chat/wrapper/AbstractIMEWrapper.java:11`

```java
// 📍 重要な箇所：未確定文字の管理（行124-169）
public final void appendPreviewText(final String typing) {
    ModLogger.debug("[Preview] Current: ({}) / Preview: ({})", 
                    this.origin, typing);
    this.status = InputStatus.PREVIEW; // ← プレビュー状態に変更
    // 未確定文字を現在のテキストに追加
}

// 📍 重要な箇所：確定文字の処理（行176-189）
public final void insertText(final String input) {
    ModLogger.debug("[Complete] Current: ({}) / Preview: ({})", 
                    this.origin, input);
    this.status = InputStatus.NONE; // ← 通常状態に戻す
    // 確定文字を挿入
}
```

### **3. DarwinOperator.java** - イベント受信担当
**ファイル**: `common/src/main/java/moe/caramel/chat/driver/arch/darwin/DarwinOperator.java:14`

```java
// 📍 重要な箇所：ネイティブからのコールバック（行27-56）
this.controller.getDriver().addInstance(
    uuid,
    // ✅ 確定文字が来た時（行31-34）
    (str, position, length) -> {
        ModLogger.debug("[Native|Java] Textfield received inserted text.");
        this.wrapper.insertText(str); // ← 確定処理
    },
    // ⚠️ 未確定文字が来た時（行36-39）
    (str, position1, length1, position2, length2) -> {
        ModLogger.debug("[Native|Java] MarkedText changed.");
        this.wrapper.appendPreviewText(str); // ← プレビュー処理
    }
);
```

### **4. DarwinController.java** - IME判定担当
**ファイル**: `common/src/main/java/moe/caramel/chat/driver/arch/darwin/DarwinController.java:18`

```java
// 📍 重要な箇所：日本語IME判定（行387-401）
private static Language parseSourceFromString(final String imeSource) {
    if (imeSource.contains("abc")) {
        return Language.ENGLISH;
    } else if (imeSource.contains("korean")) {
        return Language.KOREAN;
    } else if (imeSource.contains("kotoeri") || imeSource.contains("japanese")) {
        return Language.JAPANESE; // ← 日本語IME判定
    } else if (imeSource.contains("scim")) {
        return Language.CHINESE_SIMPLIFIED;
    } else if (imeSource.contains("tcim")) {
        return Language.CHINESE_TRADITIONAL;
    } else {
        return Language.OTHER;
    }
}
```

---

## 🐛 日本語入力で問題が起きる可能性

### **症状：未確定文字が表示されない**

**考えられる原因**：

#### **1. 🔍 IME判定の問題**
```java
// DarwinController.java:392 の日本語判定
else if (imeSource.contains("kotoeri") || imeSource.contains("japanese")) {
    return Language.JAPANESE;
}
```
**問題**: 実際の日本語IME名が想定と違う可能性
- 最新のmacOSでIME識別子が変更されている
- `driver.getStatus()`が返す値が期待と異なる

#### **2. 🔍 コールバック呼び出しの問題**
```java
// DarwinOperator.java:36-39 未確定文字のコールバック
(str, position1, length1, position2, length2) -> {
    ModLogger.debug("[Native|Java] MarkedText changed");
    this.wrapper.appendPreviewText(str);
}
```
**問題**: ネイティブ層からのイベントが来ていない可能性
- CocoaInput-libとmacOS IMEの連携に問題
- ネイティブライブラリの初期化に問題

#### **3. 🔍 状態管理の問題**
```java
// MixinEditBox.java:68-70 状態チェック
if (wrapper.getStatus() == AbstractIMEWrapper.InputStatus.NONE) {
    return null; // 通常処理
}
```
**問題**: IME状態が正しく更新されていない可能性
- `InputStatus.PREVIEW`に変更されていない
- ラッパーオブジェクトの初期化に問題

#### **4. 🔍 フォーカス管理の問題**
```java
// DarwinOperator.java:65-71 フォーカス制御
public void setFocused(final boolean focus) {
    this.controller.getDriver().setIfReceiveEvent(this.uuid, (focus ? 1 : 0));
    this.nowFocused = focus;
}
```
**問題**: テキストボックスのフォーカス状態が正しく伝わっていない
- チャット画面でのフォーカス検出に問題
- IMEイベントの受信設定に問題

---

## 🛠️ 次にやるべきデバッグ手順

### **Phase 1: ログを仕込んで状況を把握**

#### **1. DarwinOperator.java にログ追加**
```java
// ネイティブからイベントが来ているかチェック
ModLogger.debug("[DEBUG] Native callback - insertText: {}", str);
ModLogger.debug("[DEBUG] Native callback - setMarkedText: {}", str);
```

#### **2. AbstractIMEWrapper.java にログ追加**
```java
// 未確定文字処理が動いているかチェック
ModLogger.debug("[DEBUG] appendPreviewText called: {} -> status: {}", typing, status);
ModLogger.debug("[DEBUG] insertText called: {} -> status: {}", input, status);
```

#### **3. MixinEditBox.java にログ追加**
```java
// 画面表示処理が呼ばれているかチェック
ModLogger.debug("[DEBUG] caretFormatter called - status: {}", wrapper.getStatus());
```

#### **4. DarwinController.java にログ追加**
```java
// IME判定が正しく動いているかチェック
ModLogger.debug("[DEBUG] IME Source detected: {}", imeSource);
ModLogger.debug("[DEBUG] Parsed language: {}", source);
```

### **Phase 2: 実際に問題を再現**

#### **手順**：
1. `./gradlew fabric:runClient` でMinecraftを起動
2. チャット画面を開く（Tキー）
3. 日本語で「にほんご」と入力
4. ログを確認してどこで処理が止まっているか特定

#### **確認ポイント**：
- ネイティブコールバックが呼ばれているか
- `appendPreviewText`が実行されているか
- IME状態が`PREVIEW`に変更されているか
- フォーマッターが呼び出されているか

### **Phase 3: 修正の方向性**

#### **ネイティブ層の問題の場合**：
- CocoaInput-libの設定見直し
- ネイティブライブラリの再ビルド
- macOS権限設定の確認

#### **ドライバー層の問題の場合**：
- IME判定ロジックの修正
- フォーカス管理の改善
- ネイティブコールバックの修正

#### **ラッパー層の問題の場合**：
- 状態管理の修正
- プレビューテキスト処理の改善
- カーソル位置計算の修正

#### **Mixin層の問題の場合**：
- 表示ロジックの修正
- フォーマッター処理の改善
- Minecraft統合部分の修正

---

## 🔧 技術詳細

### **Mixinとは**
Minecraftの既存クラス（EditBox）に処理を「注入」する技術
- `@Inject`: メソッドの前後に処理を追加
- `@Redirect`: メソッド呼び出しを置き換え
- `@Shadow`: 既存のフィールド/メソッドにアクセス

### **CocoaInput-libとは**
韓国のMinecraftコミュニティが開発したIMEライブラリ
- 元々韓国語入力に最適化
- C/Objective-Cで実装
- JNAを使ってJavaから呼び出し

### **IME処理の流れ**
1. **未確定文字**: ユーザーが入力中の文字（変換前）
2. **確定文字**: 変換が完了した文字
3. **マークドテキスト**: macOSでの未確定文字の呼び方

---

## 💡 まとめ

このプロジェクトは**4層のシステム**で日本語IMEをMinecraftに統合しています。

**現在の問題**: 「**未確定文字がリアルタイム表示されない**」

**主な原因候補**:
1. 日本語IME判定の問題
2. ネイティブコールバックの問題  
3. IME状態管理の問題
4. フォーカス管理の問題

**次のステップ**: **デバッグログを追加**して、実際にどの層で処理が止まっているかを特定することが最優先です。

---

**作成日**: 2025-11-04  
**バージョン**: Minecraft 1.21.10対応版  
**作成者**: Claude Code Analysis