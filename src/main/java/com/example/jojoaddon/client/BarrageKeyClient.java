package com.example.jojoaddon.client;

import com.example.jojoaddon.BarrageHold;
import com.example.jojoaddon.JojoAddon;
import com.example.jojoaddon.net.BarrageNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.hydra.jojomod.event.powers.StandPowers;
import net.hydra.jojomod.event.powers.StandUser;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class BarrageKeyClient {
    public static final KeyMapping BARRAGE_KEY = new KeyMapping(
            "key.jojoaddon.barrage", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.jojoaddon");

    /** Estado enviado ao servidor por ultimo (so manda pacote quando muda). */
    private static boolean sentHolding = false;

    @Mod.EventBusSubscriber(modid = JojoAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent e) {
            e.register(BARRAGE_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = JojoAddon.MOD_ID, value = Dist.CLIENT)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (!(e.player instanceof LocalPlayer lp) || e.player != mc.player) return;

            boolean down = mc.screen == null && BARRAGE_KEY.isDown();
            StandUser user = (StandUser) lp;

            if (down) {
                // Inicia a barragem pelo mesmo caminho do input original do Roundabout
                // (respeita habilidades de cada Stand, cooldowns, discos, etc).
                if (!user.roundabout$isBarraging()) {
                    StandPowers powers = user.roundabout$getStandPowers();
                    powers.preCheckButtonInputBarrage(true, mc.options);
                }
                BarrageHold.tick(lp, true);
            }

            if (down != sentHolding) {
                sentHolding = down;
                BarrageNetwork.CHANNEL.sendToServer(new BarrageNetwork.HoldPacket(down));
                if (!down) BarrageHold.release(lp);
            }
        }
    }
}
