package moe.caramel.chat.driver.arch.darwin;

import com.mojang.blaze3d.platform.Window;
import moe.caramel.chat.driver.IController;
import moe.caramel.chat.driver.IOperator;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import java.util.UUID;

/**
 * Darwin IME Operator
 */
public class DarwinOperator implements IOperator {

    private final DarwinController controller;
    private final AbstractIMEWrapper wrapper;
    private final String uuid;
    private boolean nowFocused;

    public DarwinOperator(final DarwinController controller, final AbstractIMEWrapper wrapper) {
        ModLogger.log("[DEBUG-INIT] *** DarwinOperator constructor START ***");
        this.controller = controller;
        this.wrapper = wrapper;
        this.uuid = UUID.randomUUID().toString();

        ModLogger.log("[DEBUG-INIT] DarwinOperator created with UUID: {}", uuid);
        ModLogger.log("[DEBUG-INIT] Calling driver.addInstance...");
        this.controller.getDriver().addInstance(
            // UUID
            this.uuid,
            // Insert Text
            (str, position, length) -> {
                ModLogger.log("[DEBUG-IME] *** INSERT TEXT CALLED *** - text: '{}', position: {}, length: {}, wrapper status: {}", str, position, length, this.wrapper.getStatus());
                this.wrapper.insertText(str);
                ModLogger.log("[DEBUG-IME] *** INSERT TEXT COMPLETED ***");
            },
            // Set Marked Text
            (str, position1, length1, position2, length2) -> {
                ModLogger.log("[DEBUG-IME] *** SET MARKED TEXT CALLED *** - text: '{}', pos1: {}, len1: {}, pos2: {}, len2: {}, wrapper status: {}", str, position1, length1, position2, length2, this.wrapper.getStatus());
                this.wrapper.appendPreviewText(str);
                ModLogger.log("[DEBUG-IME] *** SET MARKED TEXT COMPLETED *** - wrapper status now: {}", this.wrapper.getStatus());
            },
            // Rect Range
            (pointer) -> {
                ModLogger.debug("[Native|Java] Called to determine where to draw.");
                final float[] buff = this.wrapper.getRect().copy();
                final Window window = Minecraft.getInstance().getWindow();
                final float factor = (float) window.getGuiScale();
                buff[0] *= factor;
                buff[1] *= factor;
                buff[2] *= factor;
                buff[3] *= factor;

                buff[0] += window.getX();
                buff[1] += window.getY();

                pointer.write(0, buff, 0, 4);
            }
        );
        ModLogger.log("[DEBUG-INIT] *** DarwinOperator initialization COMPLETED *** UUID: {}", uuid);
    }

    @Override
    public IController getController() {
        return controller;
    }

    @Override
    public void setFocused(final boolean focus) {
        if (focus != this.nowFocused) {
            ModLogger.log("[DEBUG-IME] *** IMEOperator.setFocused: {} (uuid: {}) ***", focus, this.uuid);
            this.controller.getDriver().setIfReceiveEvent(this.uuid, (focus ? 1 : 0));
            this.nowFocused = focus;
            ModLogger.log("[DEBUG-IME] *** setFocused completed: nowFocused={} ***", this.nowFocused);
        } else {
            ModLogger.log("[DEBUG-IME] setFocused called but no change: focus={}, nowFocused={}", focus, this.nowFocused);
        }
    }

    @Override
    public boolean isFocused() {
        return nowFocused;
    }
}
