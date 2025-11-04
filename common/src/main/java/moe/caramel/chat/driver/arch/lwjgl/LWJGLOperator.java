package moe.caramel.chat.driver.arch.lwjgl;

import moe.caramel.chat.driver.IController;
import moe.caramel.chat.driver.IOperator;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.UUID;

/**
 * LWJGL/GLFW-based IME Operator
 * 
 * This implementation provides IME input handling using LWJGL/GLFW native
 * capabilities, replacing CocoaInput-lib dependency for macOS 26 Tahoe compatibility.
 */
public class LWJGLOperator implements IOperator {

    private final LWJGLController controller;
    private final AbstractIMEWrapper wrapper;
    private final String uuid;
    private final long windowHandle;
    private boolean nowFocused;
    
    // GLFW callbacks
    private GLFWCharCallback charCallback;
    private GLFWKeyCallback keyCallback;
    
    // IME state tracking
    private StringBuilder preEditBuffer;
    private boolean imeActive;

    public LWJGLOperator(final LWJGLController controller, final AbstractIMEWrapper wrapper, final long windowHandle) {
        ModLogger.log("[DEBUG-INIT] *** LWJGLOperator constructor START ***");
        this.controller = controller;
        this.wrapper = wrapper;
        this.windowHandle = windowHandle;
        this.uuid = UUID.randomUUID().toString();
        this.preEditBuffer = new StringBuilder();
        this.imeActive = false;

        ModLogger.log("[DEBUG-INIT] LWJGLOperator created with UUID: {}", uuid);
        ModLogger.log("[DEBUG-INIT] GLFW window handle: {}", windowHandle);
        
        this.setupCallbacks();
        
        ModLogger.log("[DEBUG-INIT] *** LWJGLOperator initialization COMPLETED *** UUID: {}", uuid);
    }
    
    /**
     * Setup GLFW callbacks for IME handling
     */
    private void setupCallbacks() {
        ModLogger.log("[DEBUG-INIT] Setting up GLFW callbacks...");
        
        // Character callback for basic text input
        this.charCallback = new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {
                if (nowFocused && window == windowHandle) {
                    handleCharInput(codepoint);
                }
            }
        };
        
        // Key callback for special keys and IME control
        this.keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (nowFocused && window == windowHandle && action == GLFW.GLFW_PRESS) {
                    handleKeyInput(key, scancode, mods);
                }
            }
        };
        
        ModLogger.log("[DEBUG-INIT] GLFW callbacks created successfully");
    }
    
    /**
     * Handle character input from GLFW
     */
    private void handleCharInput(int codepoint) {
        String character = new String(Character.toChars(codepoint));
        ModLogger.log("[DEBUG-LWJGL] Character input: '{}' (U+{:04X})", character, codepoint);
        
        // Check if this is likely IME input (non-ASCII characters)
        if (codepoint > 127) {
            // This might be IME composition or final text
            if (this.imeActive) {
                // Add to pre-edit buffer
                this.preEditBuffer.append(character);
                ModLogger.log("[DEBUG-LWJGL] *** SET MARKED TEXT (simulated) *** - text: '{}', wrapper status: {}", 
                             this.preEditBuffer.toString(), this.wrapper.getStatus());
                this.wrapper.appendPreviewText(this.preEditBuffer.toString());
            } else {
                // Direct insertion of composed text
                ModLogger.log("[DEBUG-LWJGL] *** INSERT TEXT *** - text: '{}', wrapper status: {}", 
                             character, this.wrapper.getStatus());
                this.wrapper.insertText(character);
            }
        } else {
            // ASCII character - likely direct input or composition end
            if (this.imeActive && this.preEditBuffer.length() > 0) {
                // Finalize IME composition
                String finalText = this.preEditBuffer.toString();
                this.preEditBuffer.setLength(0);
                this.imeActive = false;
                
                ModLogger.log("[DEBUG-LWJGL] *** INSERT TEXT (IME finalized) *** - text: '{}', wrapper status: {}", 
                             finalText, this.wrapper.getStatus());
                this.wrapper.insertText(finalText);
            } else {
                // Direct ASCII input
                ModLogger.log("[DEBUG-LWJGL] *** INSERT TEXT (direct) *** - text: '{}', wrapper status: {}", 
                             character, this.wrapper.getStatus());
                this.wrapper.insertText(character);
            }
        }
    }
    
    /**
     * Handle key input from GLFW
     */
    private void handleKeyInput(int key, int scancode, int mods) {
        ModLogger.debug("[DEBUG-LWJGL] Key input: key={}, scancode={}, mods={}", key, scancode, mods);
        
        // Handle special IME keys
        switch (key) {
            case GLFW.GLFW_KEY_ESCAPE:
                // Cancel IME composition
                if (this.imeActive && this.preEditBuffer.length() > 0) {
                    ModLogger.log("[DEBUG-LWJGL] IME composition cancelled");
                    this.preEditBuffer.setLength(0);
                    this.imeActive = false;
                    this.wrapper.setToNoneStatus();
                }
                break;
                
            case GLFW.GLFW_KEY_ENTER:
                // Finalize IME composition
                if (this.imeActive && this.preEditBuffer.length() > 0) {
                    String finalText = this.preEditBuffer.toString();
                    this.preEditBuffer.setLength(0);
                    this.imeActive = false;
                    
                    ModLogger.log("[DEBUG-LWJGL] *** INSERT TEXT (Enter finalized) *** - text: '{}', wrapper status: {}", 
                                 finalText, this.wrapper.getStatus());
                    this.wrapper.insertText(finalText);
                }
                break;
                
            case GLFW.GLFW_KEY_BACKSPACE:
                // Handle backspace in IME composition
                if (this.imeActive && this.preEditBuffer.length() > 0) {
                    this.preEditBuffer.setLength(this.preEditBuffer.length() - 1);
                    if (this.preEditBuffer.length() == 0) {
                        this.imeActive = false;
                        this.wrapper.setToNoneStatus();
                    } else {
                        this.wrapper.appendPreviewText(this.preEditBuffer.toString());
                    }
                    ModLogger.log("[DEBUG-LWJGL] IME backspace, remaining: '{}'", this.preEditBuffer.toString());
                }
                break;
                
            default:
                // Check for IME activation keys (e.g., Ctrl+Space on some systems)
                if ((mods & GLFW.GLFW_MOD_CONTROL) != 0 && key == GLFW.GLFW_KEY_SPACE) {
                    this.imeActive = !this.imeActive;
                    ModLogger.log("[DEBUG-LWJGL] IME toggled: {}", this.imeActive);
                }
                break;
        }
    }

    @Override
    public IController getController() {
        return controller;
    }

    @Override
    public void setFocused(final boolean focus) {
        if (focus != this.nowFocused) {
            ModLogger.log("[DEBUG-LWJGL] *** LWJGLOperator.setFocused: {} (uuid: {}) ***", focus, this.uuid);
            
            if (focus) {
                // Set callbacks when focused
                GLFW.glfwSetCharCallback(this.windowHandle, this.charCallback);
                GLFW.glfwSetKeyCallback(this.windowHandle, this.keyCallback);
                ModLogger.log("[DEBUG-LWJGL] GLFW callbacks activated");
            } else {
                // Clear IME state when losing focus
                if (this.imeActive) {
                    this.preEditBuffer.setLength(0);
                    this.imeActive = false;
                    this.wrapper.setToNoneStatus();
                }
                
                // Note: We don't remove callbacks here as they might be shared
                // The main GLFW system will handle callback management
                ModLogger.log("[DEBUG-LWJGL] IME state cleared on focus loss");
            }
            
            this.nowFocused = focus;
            ModLogger.log("[DEBUG-LWJGL] *** setFocused completed: nowFocused={} ***", this.nowFocused);
        } else {
            ModLogger.debug("[DEBUG-LWJGL] setFocused called but no change: focus={}, nowFocused={}", focus, this.nowFocused);
        }
    }

    @Override
    public boolean isFocused() {
        return nowFocused;
    }
    
    /**
     * Cleanup resources when operator is no longer needed
     */
    public void cleanup() {
        ModLogger.log("[DEBUG-LWJGL] Cleaning up LWJGLOperator: {}", uuid);
        
        // Clear IME state
        this.preEditBuffer.setLength(0);
        this.imeActive = false;
        
        // Free GLFW callbacks
        if (this.charCallback != null) {
            this.charCallback.free();
        }
        if (this.keyCallback != null) {
            this.keyCallback.free();
        }
        
        ModLogger.log("[DEBUG-LWJGL] LWJGLOperator cleanup completed");
    }
}