package com.example.jojoaddon.net;

import net.hydra.jojomod.client.ClientNetworking;
import net.hydra.jojomod.util.config.Config;

/**
 * O Roundabout tem a opcao "barragesOnlyKillOnLastHit": enquanto ela esta ligada, cada golpe
 * da barragem tem o dano zerado (mobs) ou 0.00001 (players) se ele fosse matar o alvo, entao
 * so o soco final consegue matar.
 *
 * Enquanto alguem estiver segurando B, desligamos essa opcao NO SERVIDOR para que os golpes
 * normais da barragem tambem possam matar. Quando ninguem esta segurando, o valor original
 * e restaurado.
 */
public final class BarrageDamage {
    private static Boolean original = null; // valor original da config, se estivermos sobrescrevendo

    private BarrageDamage() {}

    public static void update(boolean anyoneHolding) {
        Config cfg = ClientNetworking.getAppropriateConfig();
        if (cfg == null || cfg.generalStandSettings == null) return;

        if (anyoneHolding && original == null) {
            original = cfg.generalStandSettings.barragesOnlyKillOnLastHit;
            cfg.generalStandSettings.barragesOnlyKillOnLastHit = Boolean.FALSE;
        } else if (!anyoneHolding && original != null) {
            restore();
        }
    }

    public static void restore() {
        if (original == null) return;
        Config cfg = ClientNetworking.getAppropriateConfig();
        if (cfg != null && cfg.generalStandSettings != null) {
            cfg.generalStandSettings.barragesOnlyKillOnLastHit = original;
        }
        original = null;
    }
}
