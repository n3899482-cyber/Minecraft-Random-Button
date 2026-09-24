package com.nzoros.randombutton;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CubeStatePayload(int activations, int lucky, int unlucky, int mode)
    implements CustomPacketPayload {
    public static final Type<CubeStatePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, "cube_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CubeStatePayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.activations);
            buf.writeVarInt(payload.lucky);
            buf.writeVarInt(payload.unlucky);
            buf.writeVarInt(payload.mode);
        },
        buf -> new CubeStatePayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
