package com.autohelper.mixin;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Unique
    private static final ConcurrentLinkedQueue<KeyEvent> injectedKeys = new ConcurrentLinkedQueue<>();

    @Shadow
    private void onKey(long window, int key, int scancode, int action, int modifiers) {}

    @Unique
    public static void injectKey(long window, int key, int scancode, int action, int modifiers) {
        injectedKeys.offer(new KeyEvent(window, key, scancode, action, modifiers));
    }

    @Unique
    public void processInjectedKeys() {
        KeyEvent event;
        while ((event = injectedKeys.poll()) != null) {
            this.onKey(event.window, event.key, event.scancode, event.action, event.modifiers);
        }
    }

    @Unique
    private static class KeyEvent {
        final long window;
        final int key;
        final int scancode;
        final int action;
        final int modifiers;

        KeyEvent(long window, int key, int scancode, int action, int modifiers) {
            this.window = window;
            this.key = key;
            this.scancode = scancode;
            this.action = action;
            this.modifiers = modifiers;
        }
    }
}
