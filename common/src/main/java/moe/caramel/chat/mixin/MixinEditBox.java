package moe.caramel.chat.mixin;

import moe.caramel.chat.controller.EditBoxController;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;
import moe.caramel.chat.wrapper.WrapperEditBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * EditBox Component Mixin
 */
@Mixin(value = EditBox.class, priority = 0)
public abstract class MixinEditBox implements EditBoxController {
    
    // 静的初期化ブロックでMixin適用を確認
    static {
        ModLogger.debug("[MIXIN-TEST] MixinEditBox class loaded!");
    }

    @Unique private WrapperEditBox caramelChat$wrapper;
    @Unique private int caramelChat$cacheCursorPos, caramelChat$cacheHighlightPos;
    @Shadow private boolean canLoseFocus;
    @Shadow public int highlightPos;
    @Shadow public int cursorPos;
    @Shadow public String value;
    @Shadow @Final private List<EditBox.TextFormatter> formatters;

    // より汎用的なコンストラクタパターンを使用
    @Inject(
        method = "<init>*",  // すべてのコンストラクタにマッチ
        at = @At("RETURN")
    )
    private void initWrapper(final CallbackInfo ci) {
        ModLogger.log("[MIXIN-TEST] *** EditBox constructor MIXIN TRIGGERED (generic)! ***");
        if (this.caramelChat$wrapper == null) {
            ModLogger.log("[DEBUG-INIT] Creating WrapperEditBox in generic constructor");
            this.caramelChat$wrapper = new WrapperEditBox((EditBox) (Object) this);
            ModLogger.log("[DEBUG-INIT] WrapperEditBox created successfully");
        } else {
            ModLogger.log("[DEBUG-INIT] Wrapper already exists!");
        }
    }

    // フォーマッタ追加も汎用的に
    @Inject(
        method = "<init>*",
        at = @At("TAIL")
    )
    private void addFormatter(final CallbackInfo ci) {
        ModLogger.log("[DEBUG-INIT] EditBox addFormatter called");
        if (this.caramelChat$wrapper == null) {
            ModLogger.log("[DEBUG-INIT] Wrapper is null in addFormatter, creating new WrapperEditBox");
            this.caramelChat$wrapper = new WrapperEditBox((EditBox) (Object) this);
        }
        if (this.formatters != null) {
            ModLogger.log("[DEBUG-INIT] Adding caret formatter");
            this.formatters.add(this.caramelChat$caretFormatter());
            ModLogger.log("[DEBUG-INIT] EditBox formatter addition complete");
        } else {
            ModLogger.log("[DEBUG-INIT] formatters list is null!");
        }
    }

    @Override
    public WrapperEditBox caramelChat$wrapper() {
        return caramelChat$wrapper;
    }

    // tickメソッドでMixinが動作していることを確認（renderよりtickが確実）
    @Inject(
        method = "tick", 
        at = @At("HEAD")
    )
    private void onTick(CallbackInfo ci) {
        if (this.caramelChat$wrapper == null) {
            ModLogger.log("[MIXIN-TEST] Creating wrapper in tick method!");
            this.caramelChat$wrapper = new WrapperEditBox((EditBox) (Object) this);
            if (this.formatters != null && !this.formatters.contains(this.caramelChat$caretFormatter())) {
                this.formatters.add(this.caramelChat$caretFormatter());
            }
        }
    }

    // ================================ (Formatter)

    @Unique
    private EditBox.TextFormatter caramelChat$caretFormatter() {
        // Set caret renderer
        return ((original, firstPos) -> {
            /* Original */
            ModLogger.log("[DEBUG-FORMATTER] *** caretFormatter CALLED *** - wrapper: {}, status: {}, original: '{}', firstPos: {}", caramelChat$wrapper, caramelChat$wrapper != null ? caramelChat$wrapper.getStatus() : "null", original, firstPos);
            if (caramelChat$wrapper == null || caramelChat$wrapper.getStatus() == AbstractIMEWrapper.InputStatus.NONE) {
                ModLogger.log("[DEBUG-FORMATTER] caretFormatter returning null (wrapper is null or status is NONE)");
                return null;
            }
            ModLogger.log("[DEBUG-FORMATTER] caretFormatter processing - status: {}, original: '{}'", caramelChat$wrapper.getStatus(), original);
            /* Warning */
            if (caramelChat$wrapper.blockTyping()) {
                return FormattedCharSequence.forward(original, Style.EMPTY.withColor(ChatFormatting.RED));
            }
            /* Custom */
            else {
                // Check Position
                // Empty
                // FirstPos ex. [ ABCD|EFG}(INPUT)HIJK ]
                // LastPos ex. [ ABCDEFG(INPUT)HI|JK} ]
                final int lastPos = (firstPos + original.length()); // firstPos ~ lastPos
                if (lastPos <= caramelChat$wrapper.getFirstEndPos() || caramelChat$wrapper.getSecondStartPos() < firstPos) {
                    return null;
                }

                // Process
                final int firstLen = (caramelChat$wrapper.getFirstEndPos() - firstPos);
                final int previewLen = (caramelChat$wrapper.getSecondStartPos() - caramelChat$wrapper.getFirstEndPos());
                final int inputEndPoint = Math.min(original.length(), (firstLen + previewLen));

                final List<FormattedCharSequence> list = new ArrayList<>();
                final String first = original.substring(0, firstLen);
                final String input = original.substring(firstLen, inputEndPoint);
                final String second = original.substring(inputEndPoint);
                list.add(FormattedCharSequence.forward(first, Style.EMPTY));
                list.add(FormattedCharSequence.forward(input, Style.EMPTY.withUnderlined(true)));
                list.add(FormattedCharSequence.forward(second, Style.EMPTY));

                return FormattedCharSequence.composite(list);
            }
        });
    }

    // ================================ (IME)

    @Inject(method = "setValue", at = @At("HEAD"))
    private void setValueHead(final String text, final CallbackInfo ci) {
        // setStatusToNone -> forceUpdateOrigin -> onValueChange
        ModLogger.debug("[DEBUG-STATE] setValue called with text: '{}', valueChanged: {}", text, this.caramelChat$wrapper != null ? this.caramelChat$wrapper.valueChanged : "wrapper-null");
        if (this.caramelChat$wrapper != null && this.caramelChat$wrapper.valueChanged) {
            ModLogger.debug("[DEBUG-STATE] setValue: valueChanged=true, caching cursor positions");
            this.caramelChat$cacheCursorPos = 0;
            this.caramelChat$cacheHighlightPos = 0;
        } else {
            ModLogger.debug("[DEBUG-STATE] setValue: calling setStatusToNone");
            this.caramelChat$setStatusToNone();
        }
    }

    @Redirect(
        method = "setValue",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"
        )
    )
    private boolean setValuePredicateTest(final Predicate<String> predicate, final Object value) {
        ModLogger.debug("[DEBUG-STATE] setValuePredicateTest called");
        if (this.caramelChat$wrapper != null && this.caramelChat$wrapper.valueChanged) {
            this.caramelChat$cacheCursorPos = this.cursorPos;
            this.caramelChat$cacheHighlightPos = this.highlightPos;
            return true;
        }

        return predicate.test((String) value);
    }

    @Inject(
        method = "setValue",
        at = @At(
            value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/gui/components/EditBox;moveCursorToEnd(Z)V"
        ), cancellable = true
    )
    private void setValueInvoke(final String text, final CallbackInfo ci) {
        if (this.caramelChat$wrapper != null && this.caramelChat$wrapper.valueChanged) {
            ci.cancel();
            // caxton Compatibility
            this.cursorPos = this.caramelChat$cacheCursorPos;
            this.highlightPos = this.caramelChat$cacheHighlightPos;
            this.caramelChat$wrapper.valueChanged = false;
            return;
        }

        this.caramelChat$forceUpdateOrigin();
    }

    @Inject(method = "insertText", at = @At("HEAD"))
    private void insertTextHead(final String text, final CallbackInfo ci) {
        // setStatusToNone -> forceUpdateOrigin -> onValueChange
        ModLogger.debug("[DEBUG-STATE] insertText called with text: '{}'", text);
        this.caramelChat$setStatusToNone();
    }

    @Inject(
        method = "insertText",
        at = @At(
            value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/gui/components/EditBox;onValueChange(Ljava/lang/String;)V"
        )
    )
    private void insertTextInvoke(final String text, final CallbackInfo ci) {
        this.caramelChat$forceUpdateOrigin();
    }

    @Inject(method = "onValueChange", at = @At("HEAD"))
    private void onValueChange(final String text, final CallbackInfo ci) {
        if (this.caramelChat$wrapper != null) {
            this.value = this.caramelChat$wrapper.getOrigin();
        }
    }

    @Inject(
        method = "deleteCharsToPos",
        at = @At(
            value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/gui/components/EditBox;moveCursorTo(IZ)V"
        )
    )
    private void deleteChars(final int pos, final CallbackInfo ci) {
        this.caramelChat$wrapper.setOrigin(this.value);
    }

    @Inject(method = "setFocused", at = @At("TAIL"))
    private void setFocused(final boolean focused, final CallbackInfo ci) {
        ModLogger.log("[DEBUG-FOCUS] *** setFocused CALLED *** - focused: {}, canLoseFocus: {}", focused, this.canLoseFocus);
        if (this.caramelChat$wrapper != null) {
            boolean actualFocus = focused || !this.canLoseFocus;
            ModLogger.log("[DEBUG-FOCUS] calling wrapper.setFocused with: {}", actualFocus);
            this.caramelChat$wrapper.setFocused(actualFocus);
            ModLogger.log("[DEBUG-FOCUS] wrapper.setFocused completed");
        } else {
            ModLogger.log("[DEBUG-FOCUS] wrapper is null in setFocused");
        }
    }

    @Inject(method = "setCanLoseFocus", at = @At("HEAD"))
    private void setCanLoseFocus(final boolean canLoseFocus, final CallbackInfo ci) {
        if (this.caramelChat$wrapper != null && !canLoseFocus) {
            this.caramelChat$wrapper.setFocused(true);
        }
    }

    @Unique
    private void caramelChat$setStatusToNone() {
        ModLogger.debug("[DEBUG-STATE] caramelChat$setStatusToNone called");
        if (this.caramelChat$wrapper != null) {
            ModLogger.debug("[DEBUG-STATE] wrapper exists, calling setToNoneStatus");
            this.caramelChat$wrapper.setToNoneStatus();
        } else {
            ModLogger.debug("[DEBUG-STATE] wrapper is null!");
        }
    }

    @Unique
    private void caramelChat$forceUpdateOrigin() {
        if (this.caramelChat$wrapper != null) {
            this.caramelChat$wrapper.setOrigin(value);
        }
    }
}
