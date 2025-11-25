package com.jules.quickstack.network;

import com.jules.quickstack.QuickStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(QuickStack.MODID);
        registrar.playToServer(QuickStackPacket.TYPE, QuickStackPacket.STREAM_CODEC,
                QuickStackPacket.Handler::handle
        );
    }
}
