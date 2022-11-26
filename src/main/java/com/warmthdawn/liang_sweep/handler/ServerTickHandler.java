package com.warmthdawn.liang_sweep.handler;

import com.warmthdawn.liang_sweep.LiangSweep;
import com.warmthdawn.liang_sweep.config.DefaultConfig;
import com.warmthdawn.liang_sweep.sweeper.ServerUtils;
import com.warmthdawn.liang_sweep.sweeper.Sweeper;
import javafx.util.Pair;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Map;
import java.util.stream.Collectors;

public class ServerTickHandler {
    public static long elapsedTicks = 0;
    public static int sweepRemains = -1;
    public static int lastSweep = 0;


    public static void beginSweepCountDown() {
        sweepRemains = DefaultConfig.INSTANEC.SWEEP_DISCOUNT.get() * 20;
    }


    @SubscribeEvent
    public void tickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.type != TickEvent.Type.SERVER || !event.side.isServer()) {
            return;
        }

        elapsedTicks++;
        if (sweepRemains >= 0) {
            if (sweepRemains == 0) {
                lastSweep = DefaultConfig.INSTANEC.SWEEP_NOTIFY.get() * 40;
                Sweeper.INSTANCE.sweep();
                sweepRemains = -1;
            } else {
                if (sweepRemains % 20 == 0) {
                    ServerUtils.sendMessagesToAll(DefaultConfig.INSTANEC.SWEEP_NOTICE.get(), sweepRemains / 20);
                }
                sweepRemains--;
            }

        }

        if (lastSweep > 0) {
            lastSweep--;
        }


    }

    @SubscribeEvent
    public void serverWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof ServerWorld)) {
            return;
        }
        if (sweepRemains >= 0 || lastSweep > 0) {
            return;
        }

        ServerWorld world = (ServerWorld) event.world;

        if (world.getGameTime() % DefaultConfig.INSTANEC.DROPS_SCAN_INTERVAL.get() == 0) {

            Map<Long, Long> items = world.getEntities()
                .filter(e -> e instanceof ItemEntity)
                .collect(Collectors.groupingBy(e -> (e.chunkCoordX & 0x00000000FFFFFFFFL) | ((long) e.chunkCoordZ << 32)
                    , Collectors.counting()));

            boolean isSweep = false;
            for (Map.Entry<Long, Long> entry : items.entrySet()) {
                int x = entry.getKey().intValue();
                int z = (int) (entry.getKey() >> 32);
                if (entry.getValue() > DefaultConfig.INSTANEC.DROPS_SCAN_MAXIMUM.get()) {
                    ServerUtils.sendMessagesToAll("侦测到维度：%s， 坐标 (x=%d, z=%d) 附近的掉落物过多！(%d)",
                        world.getDimensionKey().getLocation().toString(),
                        x * 16 + 8, z * 16 + 8,
                        entry.getValue());
                    PlayerEntity player = event.world.getClosestPlayer(x * 16 + 8, world.getSeaLevel(), x * 16 + 8, 64, null);

                    if (player != null) {
                        ServerUtils.sendMessagesToAll("该坐标在玩家 [%s] 附近",
                            player.getDisplayName());
                    } else {
                        String nearest = PlayerLastPositionSavedData.get(world).getNearest(x * 16 + 8, z * 16 + 8);
                        if (nearest != null) {
                            ServerUtils.sendMessagesToAll("玩家 [%s] 之前在该坐标附近出没过",
                                nearest);
                        }
                    }
                    isSweep = true;
                    break;
                }
            }

            long count = isSweep ? 0 : world.getEntities().count();
            if (count > DefaultConfig.INSTANEC.DROPS_WORLD_MAXIMUM.get()) {
                ServerUtils.sendMessagesToAll("维度 %s 的掉落物过多 (%d) ",
                    event.world.getDimensionKey().getLocation().toString(),
                    count
                );
                isSweep = true;
            }
            if (isSweep) {
                lastSweep = DefaultConfig.INSTANEC.SWEEP_NOTIFY.get() * 40;
                Sweeper.INSTANCE.resetTimer();
                Sweeper.INSTANCE.beginScheduleSweep();
            }


        }

    }


}
