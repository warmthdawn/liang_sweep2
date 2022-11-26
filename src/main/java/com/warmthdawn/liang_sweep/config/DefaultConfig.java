package com.warmthdawn.liang_sweep.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class DefaultConfig {
    public static final DefaultConfig INSTANEC;
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<DefaultConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(DefaultConfig::new);
        SPEC = specPair.getRight();
        INSTANEC = specPair.getLeft();
    }

    public ForgeConfigSpec.IntValue SWEEP_PERIOD;
    public ForgeConfigSpec.BooleanValue CLEAR_PASSIVE;
    public ForgeConfigSpec.IntValue SWEEP_NOTIFY;
    public ForgeConfigSpec.IntValue SWEEP_DISCOUNT;
    public ForgeConfigSpec.ConfigValue<String> SWEEP_NOTICE;
    public ForgeConfigSpec.ConfigValue<String> SWEEP_NOTICE_COMPLETE;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ENTITY;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEM;
    public ForgeConfigSpec.IntValue DROPS_SCAN_INTERVAL;
    public ForgeConfigSpec.IntValue DROPS_SCAN_MAXIMUM;
    public ForgeConfigSpec.IntValue DROPS_WORLD_MAXIMUM;

    @SuppressWarnings("unchecked")
    public DefaultConfig(ForgeConfigSpec.Builder builder) {
        SWEEP_PERIOD = builder
                .comment("扫地周期（分钟）")
                .worldRestart()
                .defineInRange("sweep_period", 60, 1, Integer.MAX_VALUE);
        CLEAR_PASSIVE = builder
                .comment("是否清理被动生物")
                .define("clear_passive", false);
        SWEEP_NOTIFY = builder
                .comment("提前通知时间（秒）")
                .worldRestart()
                .defineInRange("sweep_notify", 60, 1, Integer.MAX_VALUE);
        SWEEP_DISCOUNT = builder
                .comment("倒计时时间（秒）")
                .worldRestart()
                .defineInRange("sweep_discount", 10, 1, Integer.MAX_VALUE);
        SWEEP_NOTICE = builder
                .comment("通知提示")
                .define("sweep_notice", "<亮亮> 注意：还有 %d 秒就要去你家吃饭了");
        SWEEP_NOTICE_COMPLETE = builder
                .comment("通知提示")
                .define("sweep_notice_complete", "<亮亮> 这次一共吃掉了 %d 个掉落物， %d 个生物 %d 个经验球 %d 个投掷物和 %d 个其他实体");

        BLACKLISTED_ENTITY = builder
                .comment("黑名单（实体）")
                .defineList("blacklist_entity", Lists.newArrayList("minecraft:wither"),s -> s instanceof String);
        BLACKLISTED_ITEM = builder
                .comment("黑名单（物品）")
                .defineList("blacklist_item", Collections.EMPTY_LIST, s -> s instanceof String);

        DROPS_SCAN_INTERVAL = builder
                .comment("掉落物过于密集检测频率（tick）")
                .worldRestart()
                .defineInRange("drops_scan_interval", 40, 1, Integer.MAX_VALUE);
        DROPS_SCAN_MAXIMUM = builder
                .comment("区块掉落物数量上限")
                .worldRestart()
                .defineInRange("drops_scan_maximum", 4000, 1, Integer.MAX_VALUE);
        DROPS_WORLD_MAXIMUM = builder
                .comment("世界掉落物数量上限")
                .worldRestart()
                .defineInRange("drops_world_maximum", 100000, 1, Integer.MAX_VALUE);
    }

}
