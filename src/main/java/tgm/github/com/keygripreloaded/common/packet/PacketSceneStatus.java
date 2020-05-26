package tgm.github.com.keygripreloaded.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import tgm.github.com.keygripreloaded.client.gui.GuiWorkspace;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;

public class PacketSceneStatus extends AbstractPacket
{
    public int startPoint;
    public String sceneName;
    public boolean playing;

    public PacketSceneStatus() {}

    public PacketSceneStatus(int start, String name, boolean play)
    {
        startPoint = start;
        sceneName = name;
        playing = play;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeInt(startPoint);
        ByteBufUtils.writeUTF8String(buffer, sceneName);
        buffer.writeBoolean(playing);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        startPoint = buffer.readInt();
        sceneName = ByteBufUtils.readUTF8String(buffer);
        playing = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        GuiWorkspace workspace = KeygripReloaded.eventHandlerClient.workspace;
        if(!workspace.hasOpenScene() || !workspace.getOpenScene().identifier.equals(sceneName)) return;
        workspace.getOpenScene().playing = playing;
        workspace.timeline.timeline.setCurrentPos(startPoint);
        workspace.timeline.timeline.focusOnTicker();
        if(playing && KeygripReloaded.eventHandlerClient.sceneFrom != null && sceneName.equals(KeygripReloaded.eventHandlerClient.sceneFrom.identifier) && KeygripReloaded.config.playbackSceneWhileRecording == 1 && KeygripReloaded.eventHandlerClient.actionToRecord != null)
        {
            KeygripReloaded.eventHandlerClient.startRecord = true;
        }
    }
}
