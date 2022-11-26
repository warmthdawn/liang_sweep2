package com.warmthdawn.liang_sweep.handler;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerHandler {

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote) {
            return;
        }

        PlayerLastPositionSavedData data = PlayerLastPositionSavedData.get((ServerWorld) player.world);
        data.removeLastPosition(player.getGameProfile().getName());

    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote) {
            return;
        }

        PlayerLastPositionSavedData data = PlayerLastPositionSavedData.get((ServerWorld) player.world);
        data.setLastPosition(player.getGameProfile().getName(), player.getPosition());
    }

    @SubscribeEvent
    public void playerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote) {
            return;
        }

        MinecraftServer server = player.world.getServer();
        if (server == null) {
            return;
        }
        ServerWorld fromWorld = server.getWorld(event.getFrom());
        if(fromWorld != null) {
            PlayerLastPositionSavedData dataFrom = PlayerLastPositionSavedData.get(fromWorld);
            dataFrom.setLastPosition(player.getGameProfile().getName(), player.getPosition());
        }

        ServerWorld toWorld = server.getWorld(event.getFrom());
        if(toWorld != null) {
            PlayerLastPositionSavedData dataTo = PlayerLastPositionSavedData.get(toWorld);
            dataTo.removeLastPosition(player.getGameProfile().getName());
        }
    }
}
