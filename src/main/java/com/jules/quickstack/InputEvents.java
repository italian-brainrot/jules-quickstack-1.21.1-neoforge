package com.jules.quickstack;

import com.jules.quickstack.network.QuickStackPacket;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class InputEvents {
    public static void onKeyInput(InputEvent.Key event) {
        if (Keybindings.QUICK_STACK_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new QuickStackPacket());
        }
    }
}
