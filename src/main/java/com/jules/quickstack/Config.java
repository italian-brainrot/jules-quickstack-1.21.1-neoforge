package com.jules.quickstack;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue QUICK_STACK_RANGE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PINNED_SLOT_TAGS;

    static {
        BUILDER.push("general");

        QUICK_STACK_RANGE = BUILDER.comment("The range in which to check for nearby chests.")
                .defineInRange("quickStackRange", 16, 1, 64);

        PINNED_SLOT_TAGS = BUILDER.comment("A list of NBT tags that should be treated as pinned.")
                .defineList("pinnedSlotTags", Collections.singletonList("quickstack:pinned_slot"), o -> o instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
