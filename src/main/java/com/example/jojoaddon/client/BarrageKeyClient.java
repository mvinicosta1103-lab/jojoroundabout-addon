package com.example.jojoaddon.client;

import com.example.jojoaddon.BarrageHold;
import com.example.jojoaddon.JojoAddon;
import com.example.jojoaddon.net.BarrageNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.hydra.jojomod.event.index.PowerTypes;
import net.hydra.jojomod.event.powers.StandPowers;
import net.hydra.jojomod.event.powers.StandUser;
import net.hydra.jojomod.event.powers.whitesnake.disc.DiscItemData;
import net.hydra.jojomod.stand.powers.PowersMagiciansRed;
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

    /** true enquanto estamos "segurando o botao direito" artificialmente. */
    private static boolean spoofingUse = false;

    @Mod.EventBusSubscriber(modid = JojoAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent e) {
            e.register(BARRAGE_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = JojoAddon.MOD_ID, value = Dist.CLIENT)
    public static class ForgeBus {

        /**
         * O Roundabout, a cada tick, chama onReleaseGuard() (que CANCELA a barragem) se o botao
         * direito NAO esta pressionado. Por isso a barragem original exige segurar o botao direito.
         *
         * Para tudo funcionar so com B, mantemos o botao "usar" marcado como pressionado enquanto
         * a barragem esta acontecendo (inclusive o soco final apos soltar B). Quando a barragem
         * acaba, devolvemos o estado fisico real do botao; o Roundabout entao sai sozinho do
         * estado de guarda em que a barragem termina.
         *
         * Roda no START do tick, antes do Minecraft processar as teclas.
         */
        @SubscribeEvent
        public static void onClientTickStart(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.START) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer lp = mc.player;
            if (lp == null) {
                spoofingUse = false;
                return;
            }

            boolean down = mc.screen == null && BARRAGE_KEY.isDown();
            boolean barraging = ((StandUser) lp).roundabout$isBarraging();

            if (barraging && (down || spoofingUse)) {
                mc.options.keyUse.setDown(true);
                spoofingUse = true;
            } else if (spoofingUse) {
                restorePhysicalUseState(mc);
                spoofingUse = false;
            }
        }

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
                    if (powers instanceof PowersMagiciansRed) {
                        // No Magician's Red o input de barragem original e o de fogo (bolas de
                        // fogo / bafo). B inicia a barragem de SOCOS generica; o fogo continua
                        // no input original.
                        startPunchBarrage(lp, powers);
                    } else {
                        powers.preCheckButtonInputBarrage(true, mc.options);
                    }
                }
                BarrageHold.tick(lp, true);
            }

            if (down != sentHolding) {
                sentHolding = down;
                BarrageNetwork.CHANNEL.sendToServer(new BarrageNetwork.HoldPacket(down));
                if (!down) BarrageHold.release(lp); // soltou B -> soco final
            }
        }

        /** Mesmo caminho do buttonInputBarrage generico do Roundabout (tryPower 4 = carga da barragem). */
        private static void startPunchBarrage(LocalPlayer lp, StandPowers powers) {
            if (!DiscItemData.canUseAbilities(lp) || !PowerTypes.hasStandActive(lp)) return;
            if (powers.getAttackTime() >= powers.getAttackTimeMax()
                    || powers.getActivePowerPhase() != powers.getActivePowerPhaseMax()) {
                powers.tryPower(4, true);
                powers.tryPowerPacket((byte) 4);
            }
        }

        /** Devolve ao botao "usar" o estado real do teclado/mouse. */
        private static void restorePhysicalUseState(Minecraft mc) {
            KeyMapping use = mc.options.keyUse;
            InputConstants.Key key = use.getKey();
            long window = mc.getWindow().getWindow();
            boolean physical;
            if (key.getType() == InputConstants.Type.MOUSE) {
                physical = GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            } else if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() != -1) {
                physical = InputConstants.isKeyDown(window, key.getValue());
            } else {
                physical = false;
            }
            use.setDown(physical && mc.screen == null);
        }
    }
}
