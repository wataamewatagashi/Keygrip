package tgm.github.com.keygripreloaded.common.core;

import org.lwjgl.input.Keyboard;

import java.io.File;

import me.ichun.mods.ichunutil.client.keybind.KeyBind;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;

public class Config extends ConfigBase
{
    @ConfigProp
    @IntBool
    public int playbackSceneWhileRecording = 1;

    @ConfigProp
    public KeyBind toggleScenePlayback = new KeyBind(Keyboard.KEY_F9);

    @ConfigProp
    public KeyBind startStopRecord = new KeyBind(Keyboard.KEY_F10);

    @ConfigProp
    public KeyBind toggleSceneRecorder = new KeyBind(Keyboard.KEY_F12);

    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return KeygripReloaded.MOD_ID;
    }

    @Override
    public String getModName()
    {
        return "Keygrip";
    }
}
