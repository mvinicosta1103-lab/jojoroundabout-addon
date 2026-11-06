package com.example.jojoaddon.mixin;

import com.example.jojoaddon.client.AnimDebug;
import net.hydra.jojomod.client.models.stand.MagiciansRedModel;
import net.hydra.jojomod.client.models.stand.MagiciansRedOVAModel;
import net.hydra.jojomod.client.models.stand.StandModel;
import net.hydra.jojomod.client.models.stand.animations.StandAnimations;
import net.hydra.jojomod.entity.stand.MagiciansRedEntity;
import net.hydra.jojomod.entity.stand.StandEntity;
import net.hydra.jojomod.event.powers.StandUser;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O modelo do Magician's Red do Roundabout so aplica as animacoes proprias dele (bafo de fogo,
 * bolas de fogo, chicotes...). Os estados da animacao generica de barragem (que o Roundabout
 * ja liga no StandEntity quando o Stand faz uma barragem de socos) nunca sao aplicados ao
 * modelo dele, porque nessa versao a barragem dele e a de fogo.
 *
 * Este mixin aplica, no fim do setupAnim, as mesmas tres animacoes genericas que o
 * Star Platinum / The World usam (carga, barragem de socos e golpe final).
 *
 * remap = false e a descricao completa do metodo evitam pegar o metodo "bridge".
 */
@Mixin(value = {MagiciansRedModel.class, MagiciansRedOVAModel.class}, remap = false)
public abstract class MagiciansRedBarrageAnimMixin<T extends StandEntity> extends StandModel<T> {

    @Inject(
            // O metodo real do Roundabout se chama "setupAnim" ate no jar de producao (o nome SRG
            // m_6973_ so existe no metodo "bridge", que nao queremos).
            method = "setupAnim(Lnet/hydra/jojomod/entity/stand/MagiciansRedEntity;FFFFF)V",
            at = @At("TAIL"),
            remap = false,
            require = 1
    )
    private void jojoaddon$genericBarrageAnimations(MagiciansRedEntity entity, float limbSwing,
                                                    float limbSwingAmount, float ageInTicks,
                                                    float netHeadYaw, float headPitch, CallbackInfo ci) {
        AnimDebug.once("mixin-hit", "[jojoaddon] Mixin do setupAnim do Magician's Red esta ATIVO.");
        if (entity.barrageChargeAnimationState.isStarted() || entity.barrageAnimationState.isStarted()
                || entity.barrageEndAnimationState.isStarted()) {
            AnimDebug.once("state-on", "[jojoaddon] Estado de animacao de barragem LIGADO no Magician's Red (anim byte="
                    + entity.getAnimation() + ").");
        }

        // Mesma escala de tempo do Star Platinum: o windup da config casa com a animacao de carga
        float windupSpeed = 1.0f;
        LivingEntity user = entity.getUser();
        if (user != null) {
            int windup = ((StandUser) user).roundabout$getStandPowers().getBarrageWindup();
            if (windup > 0) windupSpeed = 1.0f / ((float) windup / 20.0f);
        }

        this.animate(entity.barrageChargeAnimationState, StandAnimations.BARRAGECHARGE, ageInTicks, windupSpeed);
        this.animate(entity.barrageAnimationState, StandAnimations.BARRAGE, ageInTicks, 1.0f);
        this.animate(entity.barrageEndAnimationState, StandAnimations.COMBO3, ageInTicks, 2.2f);
    }
}
