package kaiakk.sphericality.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityChangePayload(double change) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GravityChangePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("gravitymachine", "gravity_change"));

    public static final StreamCodec<ByteBuf, GravityChangePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, GravityChangePayload::change,
            GravityChangePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record ServerHandshakePayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ServerHandshakePayload> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("gravitymachine", "server_handshake"));
        
        public static final StreamCodec<ByteBuf, ServerHandshakePayload> CODEC = 
            StreamCodec.unit(new ServerHandshakePayload());

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientProbePayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClientProbePayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("gravitymachine", "client_probe"));

        public static final StreamCodec<ByteBuf, ClientProbePayload> CODEC = StreamCodec.unit(new ClientProbePayload());

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
