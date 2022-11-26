package com.warmthdawn.liang_sweep.handler;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerLastPositionSavedData extends WorldSavedData {
    public PlayerLastPositionSavedData() {
        super("player_last_position");
    }

    private final Map<String, BlockPos> lastPositions = new HashMap<>();


    public static PlayerLastPositionSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(PlayerLastPositionSavedData::new, "player_last_position");
    }


    public void setLastPosition(String playerName, BlockPos pos) {
        lastPositions.put(playerName, pos);
        this.markDirty();
    }

    public BlockPos getLastPosition(String playerName) {
        return lastPositions.get(playerName);
    }

    public void removeLastPosition(String playerName) {
        lastPositions.remove(playerName);
        this.markDirty();
    }

    public String getNearest(int x, int z) {
        String nearestName = null;
        int nearestDistSq = 64 * 64;
        for (Map.Entry<String, BlockPos> e : lastPositions.entrySet()) {
            int x1 = e.getValue().getX();
            int z1 = e.getValue().getZ();
            int distSq = (x1 - x) * (x1 - x) + (z1 - z) * (z1 - z);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearestName = e.getKey();
            }
        }
        return nearestName;
    }


    @Override
    public void read(CompoundNBT nbt) {
        nbt.getList("lastPositions", 10).forEach(n -> {
            CompoundNBT compound = (CompoundNBT) n;
            lastPositions.put(compound.getString("name"), BlockPos.fromLong(compound.getLong("pos")));
        });
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for (Map.Entry<String, BlockPos> e : lastPositions.entrySet()) {
            CompoundNBT compoundNBT = new CompoundNBT();
            compoundNBT.putString("name", e.getKey());
            compoundNBT.putLong("pos", e.getValue().toLong());
            list.add(compoundNBT);
        }
        compound.put("lastPositions", list);
        return compound;
    }
}
