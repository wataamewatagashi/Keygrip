package tgm.github.com.keygripreloaded.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;

public class PacketStopScene extends AbstractPacket
{
    public String sceneIdent;

    public PacketStopScene() {}

    public PacketStopScene(String s)
    {
        sceneIdent = s;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, sceneIdent);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        sceneIdent = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        KeygripReloaded.eventHandlerServer.scenesToPlay.removeIf(scene -> {
            if (!scene.identifier.equals(sceneIdent)) return false;
            scene.stop();
            scene.destroy();
            KeygripReloaded.channel.sendToDimension(new PacketSceneStatus(scene.playTime, sceneIdent, false), player.dimension);
            return true;
        });
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
