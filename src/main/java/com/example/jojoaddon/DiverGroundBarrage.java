package com.example.jojoaddon;

import net.hydra.jojomod.stand.powers.PowersDiverDown;

import java.lang.reflect.Field;

/**
 * Barragem "saindo da terra" do Diver Down (poder 62), infinita enquanto B esta pressionado.
 *
 * Como o Roundabout faz (PowersDiverDown):
 *  - pilotInputAttack() (M1 durante o mergulho) chama tryPosPower(62): activePower = 62,
 *    barrageTicksLeft = MAX_GROUND_BARRAGE_TICKS (28);
 *  - a cada tick, nos DOIS lados, barrageTicksLeft diminui. Nos 8 primeiros ticks
 *    (valores 27..20) ele da dano rapido em todos os alvos perto do Stand; no valor 17 vem o
 *    soco final; em 0 a barragem acaba e o Stand volta pro usuario (exitGroundDive).
 *
 * Para ser infinita: quando B esta pressionado e a fase de golpes rapidos termina (valor 20),
 * voltamos o contador para 28. O soco final so acontece quando B e solto.
 *
 * A animacao NAO e reiniciada aqui (o Stand sai da terra uma vez so); o visual dos golpes e
 * repetido no cliente por {@link com.example.jojoaddon.client.DiverDownVisualLoop}.
 */
public final class DiverGroundBarrage {
    private static Field TICKS;
    private static Field MAX;

    static {
        try {
            TICKS = PowersDiverDown.class.getDeclaredField("barrageTicksLeft");
            TICKS.setAccessible(true);
            MAX = PowersDiverDown.class.getDeclaredField("MAX_GROUND_BARRAGE_TICKS");
            MAX.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TICKS = null;
            MAX = null;
            JojoAddon.LOGGER.error("[jojoaddon] Nao achei os campos da barragem do Diver Down", e);
        }
    }

    private DiverGroundBarrage() {}

    public static void tick(PowersDiverDown dd, boolean holding) {
        if (TICKS == null || !holding) return;
        try {
            int max = MAX != null ? MAX.getInt(dd) : 28;
            int left = TICKS.getInt(dd);
            // Fim da fase de golpes rapidos (valor max-8) com B pressionado -> recomeca os golpes
            if (left > 0 && left <= max - 8) {
                TICKS.setInt(dd, max);
            }
        } catch (IllegalAccessException e) {
            JojoAddon.LOGGER.error("[jojoaddon] Erro lendo a barragem do Diver Down", e);
        }
    }
}
