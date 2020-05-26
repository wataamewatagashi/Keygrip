package tgm.github.com.keygripreloaded.common.core;

import net.minecraftforge.common.MinecraftForge;

import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import tgm.github.com.keygripreloaded.client.core.ResourceHelper;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;
import tgm.github.com.keygripreloaded.common.packet.PacketSceneFragment;
import tgm.github.com.keygripreloaded.common.packet.PacketSceneStatus;
import tgm.github.com.keygripreloaded.common.packet.PacketStopScene;
import tgm.github.com.keygripreloaded.common.packet.PacketToggleSleeping;

public class ProxyCommon
{
    public void preInit()
    {
        ResourceHelper.init();

        KeygripReloaded.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(KeygripReloaded.eventHandlerServer);

        KeygripReloaded.channel = new PacketChannel("Keygrip", PacketSceneFragment.class, PacketStopScene.class, PacketSceneStatus.class, PacketToggleSleeping.class);
    }
}
