package tgm.github.com.keygripreloaded.client.core;

import net.minecraftforge.common.MinecraftForge;

import tgm.github.com.keygripreloaded.common.KeygripReloaded;
import tgm.github.com.keygripreloaded.common.core.ProxyCommon;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        KeygripReloaded.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(KeygripReloaded.eventHandlerClient);
    }
}
