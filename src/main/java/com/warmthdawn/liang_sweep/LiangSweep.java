package com.warmthdawn.liang_sweep;

import com.warmthdawn.liang_sweep.config.DefaultConfig;
import com.warmthdawn.liang_sweep.handler.PlayerHandler;
import com.warmthdawn.liang_sweep.handler.ServerTickHandler;
import com.warmthdawn.liang_sweep.sweeper.ServerUtils;
import com.warmthdawn.liang_sweep.sweeper.Sweeper;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LiangSweep.MOD_ID)
public class LiangSweep {

    public static final String MOD_ID = "liang_sweep";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public LiangSweep() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DefaultConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerTickHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerHandler());
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        ServerUtils.server = event.getServer();
        Sweeper.INSTANCE.startSweep();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStoppingEvent event) {
        Sweeper.INSTANCE.stopSweep();
        ServerUtils.server = null;
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("liang_sweep")
            .requires(cs -> cs.hasPermissionLevel(2))
            .executes(ctx -> {
                    ServerUtils.sendMessagesToAll("管理员 %s 主动开启扫地", ctx.getSource().getName());
                    Sweeper.INSTANCE.beginScheduleSweep();
                    ctx.getSource().sendFeedback(new StringTextComponent("扫地开始"), false);

                    return 0;
                }
            ));

        event.getDispatcher().register(Commands.literal("sweep_scan")
            .requires(cs -> cs.hasPermissionLevel(2))
            .executes(ctx -> {
                    ServerUtils.sendMessagesToAll("管理员 %s 开启扫地检测", ctx.getSource().getName());
                    boolean scan = Sweeper.INSTANCE.scan();
                    if (!scan) {
                        ServerUtils.sendMessagesToAll("扫描结束，未发现掉落物密集区", ctx.getSource().getName());
                    }

                    return 0;
                }
            ));
    }

}
