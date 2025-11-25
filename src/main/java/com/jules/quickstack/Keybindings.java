package com.jules.quickstack;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public static final KeyMapping QUICK_STACK_KEY = new KeyMapping(
            "key.quickstack.desc",
            GLFW.GLFW_KEY_K,
            "key.quickstack.category"
    );
}
