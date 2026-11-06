package com.example.jojoaddon;

import net.hydra.jojomod.event.powers.StandUser;
import net.hydra.jojomod.stand.powers.PowersDiverDown;
import net.minecraft.world.entity.player.Player;

/**
 * Logica compartilhada (cliente + servidor) da barragem infinita.
 *
 * Como a barragem do Roundabout funciona (StandPowers / NewPunchingStand):
 *  - activePower == 5 significa "barragem em andamento";
 *  - attackTimeDuring conta os ticks da barragem; os golpes 1..(length-1) sao normais,
 *    o golpe numero "length" e o finalizador (knockback forte) e depois a barragem acaba.
 *
 * Para deixar a barragem infinita enquanto B esta pressionado, basta voltar o contador
 * ao inicio antes de ele chegar no finalizador. Ao soltar B, deixamos o contador
 * seguir (ou o adiantamos) para o finalizador acontecer e a barragem terminar.
 */
public final class BarrageHold {
    private BarrageHold() {}

    /** Chamado todo tick de jogador, nos dois lados, com o estado "B segurado". */
    public static void tick(Player player, boolean holding) {
        StandUser user = (StandUser) player;

        // Diver Down: barragem saindo da terra (poder 62), com o proprio contador
        if (user.roundabout$getActivePower() == PowersDiverDown.GROUND_DIVE_BARRAGE
                && user.roundabout$getStandPowers() instanceof PowersDiverDown dd) {
            DiverGroundBarrage.tick(dd, holding);
            return;
        }

        if (user.roundabout$getActivePower() != 5) return; // so durante a barragem (nao o windup)

        int length = user.roundabout$getStandPowers().getBarrageLength();
        int t = user.roundabout$getAttackTimeDuring();

        if (holding && t >= length - 1) {
            user.roundabout$setAttackTimeDuring(0); // proximo tick vira 1 -> recomeca sem finalizador
        }
    }

    /** Ao soltar B: pula direto para o golpe final, que encerra a barragem. */
    public static void release(Player player) {
        StandUser user = (StandUser) player;
        if (user.roundabout$getActivePower() != 5) return;

        int length = user.roundabout$getStandPowers().getBarrageLength();
        if (user.roundabout$getAttackTimeDuring() < length - 1) {
            user.roundabout$setAttackTimeDuring(length - 1);
        }
    }
}
