package com.autohelper.mixin;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        long windowHandle = client.getWindow().getHandle();

        // Maximize window (not fullscreen)
        GLFW.glfwMaximizeWindow(windowHandle);
    }
}
