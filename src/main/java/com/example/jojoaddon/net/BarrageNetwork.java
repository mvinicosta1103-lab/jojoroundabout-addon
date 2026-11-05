package com.example.jojoaddon.net;

import com.example.jojoaddon.BarrageHold;
import com.example.jojoaddon.JojoAddon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Canal cliente -> servidor que informa se o jogador esta segurando B. */
public final class BarrageNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JojoAddon.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    /** Jogadores que o servidor sabe que estao segurando B. */
    public static final Set<UUID> HOLDING = ConcurrentHashMap.newKeySet();

    private BarrageNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, HoldPacket.class,
                HoldPacket::encode, HoldPacket::decode, HoldPacket::handle);
    }

    public static class HoldPacket {
        private final boolean holding;

        public HoldPacket(boolean holding) { this.holding = holding; }

        public static void encode(HoldPacket p, FriendlyByteBuf buf) { buf.writeBoolean(p.holding); }

        public static HoldPacket decode(FriendlyByteBuf buf) { return new HoldPacket(buf.readBoolean()); }

        public static void handle(HoldPacket p, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> {
                ServerPlayer sp = c.getSender();
                if (sp == null) return;
                if (p.holding) {
                    HOLDING.add(sp.getUUID());
                } else if (HOLDING.remove(sp.getUUID())) {
                    BarrageHold.release(sp);
                }
            });
            c.setPacketHandled(true);
        }
    }
}
