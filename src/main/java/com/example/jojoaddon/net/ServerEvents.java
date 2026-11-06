package com.example.jojoaddon.net;

import com.example.jojoaddon.BarrageHold;
import com.example.jojoaddon.JojoAddon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JojoAddon.MOD_ID)
public class ServerEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        if (BarrageNetwork.HOLDING.contains(sp.getUUID())) {
            BarrageHold.tick(sp, true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        // Liga/desliga o "dano letal em todos os golpes" conforme alguem segura B
        BarrageDamage.update(!BarrageNetwork.HOLDING.isEmpty());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        BarrageNetwork.HOLDING.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        BarrageNetwork.HOLDING.clear();
        BarrageDamage.restore();
    }
}
