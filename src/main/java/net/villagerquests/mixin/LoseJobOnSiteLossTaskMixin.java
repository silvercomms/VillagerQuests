package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.villagerquests.accessor.MerchantAccessor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoseJobOnSiteLossTask.class)
public class LoseJobOnSiteLossTaskMixin {

    @Inject(method = "method_47038", at = @At("HEAD"))
    private static void runMixin(ServerWorld world, VillagerEntity entity, long time, CallbackInfoReturnable<Boolean> cir) {
        ((MerchantAccessor) entity).clearQuestList();
    }
}