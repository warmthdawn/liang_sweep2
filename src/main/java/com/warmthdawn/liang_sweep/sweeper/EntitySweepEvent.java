package com.warmthdawn.liang_sweep.sweeper;

import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class EntitySweepEvent extends Event {
    private final Entity entity;

    public EntitySweepEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
