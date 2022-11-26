package com.warmthdawn.liang_sweep.sweeper;

import com.warmthdawn.liang_sweep.config.DefaultConfig;
import com.warmthdawn.liang_sweep.handler.PlayerLastPositionSavedData;
import com.warmthdawn.liang_sweep.handler.ServerTickHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;
import java.util.stream.Collectors;

public class Sweeper {
    public static final Sweeper INSTANCE = new Sweeper();
    private Timer timer;
    private TimerTask currentTask;

    private Sweeper() {

    }

    public void startSweep() {
        if (timer != null) {
            stopSweep();
        }

        timer = new Timer();
        resetTimer();
    }

    public void stopSweep() {
        timer.cancel();
        timer = null;
    }

    public void resetTimer() {
        if (currentTask != null)
            currentTask.cancel();
        timer.purge();
        currentTask = new TimerTask() {
            @Override
            public void run() {
                beginScheduleSweep();
            }
        };
        timer.schedule(currentTask, 0, (long) DefaultConfig.INSTANEC.SWEEP_PERIOD.get() * 60 * 1000);
    }

    public void beginScheduleSweep() {

        if (ServerUtils.server != null)
            ServerUtils.server.execute(Sweeper.this::noticeSweep);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ServerUtils.server != null)
                    ServerUtils.server.execute(Sweeper.this::startSweepTick);
            }
        }, (DefaultConfig.INSTANEC.SWEEP_NOTIFY.get() - DefaultConfig.INSTANEC.SWEEP_DISCOUNT.get()) * 1000L);
    }

    public boolean scan() {
        boolean flag = false;
        for (ServerWorld world : ServerUtils.server.getWorlds()) {
            synchronized (world) {
                Map<Long, Long> items = world.getEntities()
                    .filter(e -> e instanceof ItemEntity)
                    .collect(Collectors.groupingBy(e -> (e.chunkCoordX & 0x00000000FFFFFFFFL) | ((long) e.chunkCoordZ << 32)
                        , Collectors.counting()));

                int loadedChunkCount = world.getChunkProvider().chunkManager.getLoadedChunkCount();

                if (loadedChunkCount > 0) {
                    ServerUtils.sendMessagesToAll("维度：%s， 掉落物数量 %d， 加载区块 %d", world.getDimensionKey().getLocation().toString(), items.size(), loadedChunkCount);
                }
                for (Map.Entry<Long, Long> entry : items.entrySet()) {
                    int x = entry.getKey().intValue();
                    int z = (int) (entry.getKey() >> 32);
                    if (entry.getValue() > DefaultConfig.INSTANEC.DROPS_SCAN_MAXIMUM.get()) {
                        ServerUtils.sendMessagesToAll("侦测到维度：%s， 坐标 (x=%d, z=%d) 附近的掉落物过多！(%d)",
                            world.getDimensionKey().getLocation().toString(),
                            x * 16 + 8, z * 16 + 8,
                            entry.getValue());
                        PlayerEntity player = world.getClosestPlayer(x * 16 + 8, world.getSeaLevel(), x * 16 + 8, 64, null);
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
                        flag = true;
                    }
                }

                long count = world.getEntities().count();
                if (count > DefaultConfig.INSTANEC.DROPS_WORLD_MAXIMUM.get()) {
                    ServerUtils.sendMessagesToAll("维度 %s 的掉落物过多 (%d) ",
                        world.getDimensionKey().getLocation().toString(),
                        count
                    );
                    flag = true;
                }
            }
        }
        return flag;
    }

    public void sweep() {
        int killItemCount = 0;
        int killLivingCount = 0;
        int killXpCount = 0;
        int killProjectileCount = 0;
        int killOtherCount = 0;
        for (ServerWorld world : ServerUtils.server.getWorlds()) {
            synchronized (world) {
                Iterator<Entity> iterator = world.getEntities()
                    .filter(Objects::nonNull)
                    .filter(entity -> !MinecraftForge.EVENT_BUS.post(new EntitySweepEvent(entity)))
                    .filter(entity -> !entity.hasCustomName()
                        && !DefaultConfig.INSTANEC.BLACKLISTED_ENTITY.get().contains(entity.getEntityString()))
                    .iterator();

                LinkedList<Entity> entitiesToRemove = new LinkedList<>();
                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    if (entity instanceof ItemEntity) {
                        //清理物品
                        if (SweeperHooks.canClearItem((ItemEntity) entity)) {
                            killItemCount++;
                            entitiesToRemove.add(entity);
                        }
                    } else if (entity instanceof LivingEntity) {
                        //生物
                        if (SweeperHooks.canClearLiving((LivingEntity) entity)) {
                            killLivingCount++;
                            entitiesToRemove.add(entity);
                        }
                    } else if (entity instanceof ExperienceOrbEntity) {
                        //经验球
                        if (SweeperHooks.canClearXPOrb((ExperienceOrbEntity) entity)) {
                            killXpCount++;
                            entitiesToRemove.add(entity);
                        }
                    } else if (entity instanceof ProjectileEntity) {
                        //经验球
                        if (SweeperHooks.canClearProjectile((ProjectileEntity) entity)) {
                            killProjectileCount++;
                            entitiesToRemove.add(entity);
                        }
                    } else {
                        if (SweeperHooks.canClearOthers(entity)) {
                            killOtherCount++;
                            entitiesToRemove.add(entity);
                        }
                    }

                }

                for (Entity entity : entitiesToRemove) {
                    world.removeEntity(entity);
                }
            }
        }

        ServerUtils.sendMessagesToAll(DefaultConfig.INSTANEC.SWEEP_NOTICE_COMPLETE.get(), killItemCount, killLivingCount, killXpCount, killProjectileCount, killOtherCount);
    }

    public void startSweepTick() {
        ServerTickHandler.beginSweepCountDown();
    }

    public void noticeSweep() {
        ServerUtils.sendMessagesToAll(DefaultConfig.INSTANEC.SWEEP_NOTICE.get(), DefaultConfig.INSTANEC.SWEEP_NOTIFY.get());
    }


}
