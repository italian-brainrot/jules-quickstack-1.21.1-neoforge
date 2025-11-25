package com.jules.quickstack.network;

import com.jules.quickstack.Config;
import com.jules.quickstack.QuickStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public record QuickStackPacket() implements CustomPacketPayload {
    public static final Type<QuickStackPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuickStack.MODID, "quick_stack"));
    public static final StreamCodec<ByteBuf, QuickStackPacket> STREAM_CODEC = StreamCodec.unit(new QuickStackPacket());

    private static Method ipnIsSlotLockedMethod;
    private static Object ipnLockSlotsHandlerInstance;

    static {
        try {
            Class<?> lockSlotsHandlerClass = Class.forName("org.anti_ad.mc.ipnext.event.LockSlotsHandler");
            ipnLockSlotsHandlerInstance = lockSlotsHandlerClass.getField("INSTANCE").get(null);
            ipnIsSlotLockedMethod = lockSlotsHandlerClass.getMethod("isSlotLocked", int.class);
        } catch (Exception e) {
            ipnIsSlotLockedMethod = null;
            ipnLockSlotsHandlerInstance = null;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler {
        public static void handle(final QuickStackPacket packet, final IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                if (player instanceof ServerPlayer serverPlayer) {
                    Level world = serverPlayer.level();
                    BlockPos playerPos = serverPlayer.blockPosition();
                    List<IItemHandler> nearbyInventories = new ArrayList<>();
                    int range = Config.QUICK_STACK_RANGE.get();
                    int chunkRange = range >> 4;

                    for (int chunkX = (playerPos.getX() >> 4) - chunkRange; chunkX <= (playerPos.getX() >> 4) + chunkRange; chunkX++) {
                        for (int chunkZ = (playerPos.getZ() >> 4) - chunkRange; chunkZ <= (playerPos.getZ() >> 4) + chunkRange; chunkZ++) {
                            LevelChunk chunk = world.getChunk(chunkX, chunkZ);
                            chunk.getBlockEntities().values().forEach(be -> {
                                if (playerPos.distSqr(be.getBlockPos()) <= range * range) {
                                    IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
                                    if (handler != null) {
                                        nearbyInventories.add(handler);
                                    }
                                }
                            });
                        }
                    }

                    if (nearbyInventories.isEmpty()) {
                        return;
                    }

                    // The player's main inventory and hotbar are slots 0-35. 36 is the first armor slot.
                    for (int i = 0; i < 36; i++) {
                        ItemStack playerStack = serverPlayer.getInventory().getItem(i);
                        if (playerStack.isEmpty()) {
                            continue;
                        }

                        boolean isIpnLocked = false;
                        if (ipnIsSlotLockedMethod != null) {
                            try {
                                isIpnLocked = (boolean) ipnIsSlotLockedMethod.invoke(ipnLockSlotsHandlerInstance, i);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }

                        if (isIpnLocked) {
                            continue;
                        }

                        CompoundTag customData = playerStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                        boolean isPinned = false;
                        for (String tag : Config.PINNED_SLOT_TAGS.get()) {
                            if (customData.getBoolean(tag)) {
                                isPinned = true;
                                break;
                            }
                        }
                        if (isPinned) {
                            continue;
                        }

                        for (IItemHandler inventory : nearbyInventories) {
                            boolean itemExists = false;
                            for (int j = 0; j < inventory.getSlots(); j++) {
                                if (ItemStack.isSameItemSameComponents(playerStack, inventory.getStackInSlot(j))) {
                                    itemExists = true;
                                    break;
                                }
                            }

                            if (itemExists) {
                                ItemStack remainingStack = ItemHandlerHelper.insertItem(inventory, playerStack, false);
                                serverPlayer.getInventory().setItem(i, remainingStack);
                                if (remainingStack.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
