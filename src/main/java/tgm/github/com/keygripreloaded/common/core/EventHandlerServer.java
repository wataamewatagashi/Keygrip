package tgm.github.com.keygripreloaded.common.core;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import tgm.github.com.keygripreloaded.client.core.ResourceHelper;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;
import tgm.github.com.keygripreloaded.common.packet.PacketSceneStatus;
import tgm.github.com.keygripreloaded.common.scene.Scene;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(!event.phase.equals(TickEvent.Phase.START)) return;
        scenesToPlay.removeIf(scene ->{
            scene.update();
            if (scene.playTime <= scene.getLength() + 10) return false;
            scene.stop();
            scene.destroy();
            KeygripReloaded.channel.sendToAll(new PacketSceneStatus(scene.playTime, scene.identifier, false));
            return true;
        });
    }

    public void receiveProjectData(int dimension, int startPoint, String sceneIdent, short packetTotal, short packetNumber, byte[] data)
    {
        ArrayList<byte[]> byteArray = sceneParts.get(sceneIdent);
        if(byteArray == null)
        {
            byteArray = new ArrayList<>();

            sceneParts.put(sceneIdent, byteArray);

            for(int i = 0; i < packetTotal; i++)
            {
                byteArray.add(new byte[0]);
            }
        }

        byteArray.set(packetNumber, data);

        boolean hasAllInfo = byteArray.parallelStream()
                .noneMatch(aByte -> aByte.length == 0);

        if(hasAllInfo)
        {
            int size = byteArray.parallelStream().mapToInt(value -> value.length).sum();
            byte[] bytes = new byte[size];
            int index = 0;

            for (byte[] aByte : byteArray) {
                System.arraycopy(aByte, 0, bytes, index, aByte.length);
                index += aByte.length;
            }

            //At this point, bytes has the full data. Do something with it.

            File temp = new File(ResourceHelper.getTempDir(), Math.abs(sceneIdent.hashCode()) + "-received.kgs");

            try
            {
                FileUtils.writeByteArrayToFile(temp, bytes);
                if(temp.exists())
                {
                    Scene scene = Scene.openScene(temp);

                    if(scene != null)
                    {
                        scenesToPlay.removeIf(scene1 -> {
                            if (!scene1.identifier.equals(scene.identifier)) return false;
                            scene1.stop();
                            scene1.destroy();
                            return true;
                        });

                        scene.playing = true;
                        scene.playTime = startPoint;

                        scenesToPlay.add(scene);

                        KeygripReloaded.channel.sendToDimension(new PacketSceneStatus(startPoint, sceneIdent, true), dimension);

                        scene.create(DimensionManager.getWorld(dimension));
                    }

                    temp.delete();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            sceneParts.remove(sceneIdent);
        }
    }

    public HashMap<String, ArrayList<byte[]>> sceneParts = new HashMap<>();
    public ArrayList<Scene> scenesToPlay = new ArrayList<>();
}
