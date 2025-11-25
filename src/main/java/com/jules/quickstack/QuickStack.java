package com.jules.quickstack;

import com.jules.quickstack.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(QuickStack.MODID)
public class QuickStack {
    public static final String MODID = "quickstack";
    public static final Logger LOGGER = LogUtils.getLogger();

    public QuickStack(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(PacketHandler::register);

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(InputEvents::onKeyInput);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Keybindings.QUICK_STACK_KEY);
    }
}
