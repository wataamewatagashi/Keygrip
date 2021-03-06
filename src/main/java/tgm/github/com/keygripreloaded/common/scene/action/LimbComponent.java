package tgm.github.com.keygripreloaded.common.scene.action;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import tgm.github.com.keygripreloaded.common.scene.Scene;

public class LimbComponent
{
    public LimbComponent(double... changes)
    {
        actionChange = Arrays.stream(changes).mapToInt(value -> Math.toIntExact(Math.round(value * (double) Scene.PRECISION))).toArray();
    }

    @SerializedName("a")
    public int[] actionChange; // multiplied by precision and rounded off for storage.
}
