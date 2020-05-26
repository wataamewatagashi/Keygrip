package tgm.github.com.keygripreloaded.common;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import tgm.github.com.keygripreloaded.client.core.EventHandlerClient;
import tgm.github.com.keygripreloaded.client.core.ResourceHelper;
import tgm.github.com.keygripreloaded.common.core.Config;
import tgm.github.com.keygripreloaded.common.core.EventHandlerServer;
import tgm.github.com.keygripreloaded.common.core.ProxyCommon;

@Mod(modid = KeygripReloaded.MOD_ID, name = KeygripReloaded.MOD_NAME,
        version = KeygripReloaded.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR + ".0.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR + ".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)"
)
public class KeygripReloaded
{
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";
    public static final String MOD_NAME = "KeygripReloaded";
    public static final String MOD_ID = "keygripreloaded";

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static KeygripReloaded instance;

    @SidedProxy(clientSide = "tgm.github.com.keygripreloaded.client.core.ProxyClient", serverSide = "tgm.github.com.keygripreloaded.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static Config config;

    public static PacketChannel channel;

    public static EventHandlerServer eventHandlerServer;
    public static EventHandlerClient eventHandlerClient;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();

        if(FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            config = ConfigHandler.registerConfig(new Config(new File(ResourceHelper.getConfigDir(), "config.cfg")));
        }

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }
}
