package moe.caramel.chat.driver;

import moe.caramel.chat.driver.arch.darwin.DarwinController;
import moe.caramel.chat.driver.arch.unknown.UnknownController;
import moe.caramel.chat.driver.arch.wayland.WaylandController;
import moe.caramel.chat.driver.arch.win.WinController;
import moe.caramel.chat.driver.arch.x11.X11Controller;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Controller Interface
 */
public interface IController {

    /**
     * Create the IME Operator.
     *
     * @param wrapper IME Wrapper
     * @return IME Operator
     */
    IOperator createOperator(final AbstractIMEWrapper wrapper);

    /**
     * Replace to current focused screen.
     *
     * @param screen focused screen
     */
    void changeFocusedScreen(final Screen screen);

    /**
     * Set whether to focus or not. (Driver)
     *
     * @param focus focus
     */
    void setFocus(final boolean focus);

    /**
     * Gets the current keyboard status.
     *
     * @return keyboard status (if {@code null}, OS isn't supported)
     */
    @Nullable
    default KeyboardStatus getKeyboardStatus() {
        return null;
    }

    /**
     * Gets the controller.
     *
     * @return controller
     */
    static IController getController() {
        try {
            final int platform = GLFW.glfwGetPlatform();
            ModLogger.log("[DEBUG-INIT] Detected GLFW platform: {}", platform);
            ModLogger.log("[DEBUG-INIT] GLFW_PLATFORM_COCOA = {}", GLFW.GLFW_PLATFORM_COCOA);
            
            return switch (platform) {
                // Windows
                case GLFW.GLFW_PLATFORM_WIN32 -> {
                    ModLogger.log("[DEBUG-INIT] Creating WinController");
                    yield new WinController();
                }
                // macOS
                case GLFW.GLFW_PLATFORM_COCOA -> {
                    ModLogger.log("[DEBUG-INIT] Creating DarwinController for macOS");
                    yield new DarwinController();
                }
                // Linux (X11)
                case GLFW.GLFW_PLATFORM_X11 -> {
                    ModLogger.log("[DEBUG-INIT] Creating X11Controller");
                    yield new X11Controller();
                }
                // Linux (Wayland)
                case GLFW.GLFW_PLATFORM_WAYLAND -> {
                    ModLogger.log("[DEBUG-INIT] Creating WaylandController");
                    yield new WaylandController();
                }
                // What?
                default -> {
                    ModLogger.log("[DEBUG-INIT] Unsupported platform: {}", platform);
                    throw new UnsupportedOperationException();
                }
            };
        } catch (final UnsupportedOperationException ignored) {
            ModLogger.error("This platform is not supported by CocoaInput Driver.");
        } catch (final Exception exception) {
            ModLogger.error("Error while loading the CocoaInput Driver.", exception);
        }
        ModLogger.log("[DEBUG-INIT] Falling back to UnknownController");
        return UnknownController.INSTANCE;
    }
}
