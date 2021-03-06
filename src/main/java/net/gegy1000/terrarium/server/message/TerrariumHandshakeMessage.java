package net.gegy1000.terrarium.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TerrariumHandshakeMessage implements IMessage {
    private GenerationSettings settings;

    public TerrariumHandshakeMessage() {
    }

    public TerrariumHandshakeMessage(GenerationSettings settings) {
        this.settings = settings;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            this.settings = GenerationSettings.deserialize(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.settings != null);
        if (this.settings != null) {
            ByteBufUtils.writeUTF8String(buf, this.settings.serializeString());
        }
    }

    public static class Handler implements IMessageHandler<TerrariumHandshakeMessage, IMessage> {
        @Override
        public IMessage onMessage(TerrariumHandshakeMessage message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                MinecraftServer server = player.getServer();
                if (server == null) {
                    return null;
                }
                TerrariumWorldData worldData = server.getWorld(0).getCapability(TerrariumCapabilities.worldDataCapability, null);
                if (worldData != null) {
                    Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumHandshakeTracker.markPlayerFriendly(player));
                    return new TerrariumHandshakeMessage(worldData.getSettings());
                }
            } else {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumHandshakeTracker.provideSettings(Terrarium.PROXY.getWorld(ctx), message.settings));
            }
            return null;
        }
    }
}
