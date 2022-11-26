package com.warmthdawn.liang_sweep.sweeper;

import com.warmthdawn.liang_sweep.config.DefaultConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public class SweeperHooks {
    public static boolean canClearItem(ItemEntity item) {
        return !DefaultConfig.INSTANEC.BLACKLISTED_ITEM.get().contains(item.getItem().getItem().getRegistryName().toString());
    }


    public static boolean canClearLiving(LivingEntity living) {
        if (living instanceof AnimalEntity) {
            return DefaultConfig.INSTANEC.CLEAR_PASSIVE.get();
        }
        if (living instanceof MonsterEntity) {
            return true;
        }
        return false;
    }

    public static boolean canClearXPOrb(ExperienceOrbEntity xpOrb) {
        return true;
    }

    public static boolean canClearOthers(Entity entity) {
        return false;
    }

    public static boolean canClearProjectile(ProjectileEntity entity) {
        return true;
    }
}
