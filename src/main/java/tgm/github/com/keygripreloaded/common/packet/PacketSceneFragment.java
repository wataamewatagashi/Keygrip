package tgm.github.com.keygripreloaded.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.packet.PacketDataFragment;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;

public class PacketSceneFragment extends PacketDataFragment
{
    public int startPoint = 0;

    public PacketSceneFragment() {}

    public PacketSceneFragment(int startPoint, String fileName, int packetTotal, int packetNumber, int fragmentSize, byte[] data)
    {
        super(fileName, packetTotal, packetNumber, fragmentSize, data);
        this.startPoint = startPoint;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        super.writeTo(buffer);
        buffer.writeInt(startPoint);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        super.readFrom(buffer);
        startPoint = buffer.readInt();
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }

    @Override
    public void execution(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            KeygripReloaded.eventHandlerServer.receiveProjectData(player.dimension, startPoint, fileName, packetTotal, packetNumber, data);
        }
    }
}
