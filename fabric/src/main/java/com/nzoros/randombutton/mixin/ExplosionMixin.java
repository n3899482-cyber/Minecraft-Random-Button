package com.nzoros.randombutton.mixin;

import com.nzoros.randombutton.lobby.LobbyProtectionManager;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow @Final private Level level;

    @Inject(method = "explode", at = @At("TAIL"))
    private void randombutton$protectLobby(CallbackInfo callbackInfo) {
        Explosion explosion = (Explosion) (Object) this;
        explosion.getToBlow().removeIf(pos -> LobbyProtectionManager.isProtected(level, pos));
    }
}
