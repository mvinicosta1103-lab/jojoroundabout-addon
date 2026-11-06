package com.example.jojoaddon.client;

import com.example.jojoaddon.JojoAddon;
import com.example.jojoaddon.net.BarrageNetwork;
import net.hydra.jojomod.entity.stand.DiverDownEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * A animacao da barragem da terra do Diver Down (DD_Barrage, 1.32s) e assim:
 *   0.000 - 0.149 : o Stand sobe da terra
 *   0.149 - 0.841 : golpes rapidos, em ciclos IDENTICOS de 0.231s (mesma pose a cada ciclo)
 *   0.841 - 1.32  : soco final e o Stand afunda de novo
 *
 * Enquanto o dono segura B, repetimos so UM ciclo de golpes: quando o tempo da animacao passa
 * de 0.3795s voltamos 0.231s. Como o ciclo e identico, a emenda e invisivel e o Stand nao
 * sobe/afunda de novo. Ao soltar B o tempo segue normalmente (soco final + afundar).
 *
 * O tempo acumulado do AnimationState e um campo privado; pegamos por reflexao o segundo
 * campo "long" nao estatico (declaracao: lastTime, accumulatedTime), em milissegundos.
 */
@Mod.EventBusSubscriber(modid = JojoAddon.MOD_ID, value = Dist.CLIENT)
public class DiverDownVisualLoop {
    private static final long LOOP_END_MS = 380;   // fim do 1o ciclo de golpes (0.1485 + 0.231)
    private static final long LOOP_LEN_MS = 231;   // duracao de um ciclo

    private static Field ACC;

    static {
        try {
            List<Field> longs = Arrays.stream(AnimationState.class.getDeclaredFields())
                    .filter(f -> f.getType() == long.class && !Modifier.isStatic(f.getModifiers()))
                    .toList();
            if (longs.size() >= 2) {
                ACC = longs.get(1);
                ACC.setAccessible(true);
            }
        } catch (Exception e) {
            ACC = null;
        }
        if (ACC == null) {
            JojoAddon.LOGGER.error("[jojoaddon] Nao consegui acessar o tempo do AnimationState; o loop visual do Diver Down fica desligado");
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || ACC == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || BarrageNetwork.LOOPING_CLIENT.isEmpty()) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof DiverDownEntity stand)) continue;
            LivingEntity user = stand.getUser();
            if (user == null || !BarrageNetwork.LOOPING_CLIENT.contains(user.getId())) continue;

            AnimationState state = stand.groundBarrage;
            if (!state.isStarted()) continue;
            try {
                long acc = ACC.getLong(state);
                while (acc >= LOOP_END_MS) acc -= LOOP_LEN_MS;
                ACC.setLong(state, acc);
            } catch (IllegalAccessException ex) {
                ACC = null;
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        BarrageNetwork.LOOPING_CLIENT.clear();
    }
}
