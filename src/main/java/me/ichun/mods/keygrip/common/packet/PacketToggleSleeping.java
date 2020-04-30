package me.ichun.mods.keygrip.common.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.keygrip.common.Keygrip;

public class PacketToggleSleeping extends AbstractPacket
{
    public int id;
    public boolean state;
    public int face;

    private static Field sleeping = ObfuscationReflectionHelper.findField(EntityPlayer.class, "field_71083_bS");

    public PacketToggleSleeping() {}

    public PacketToggleSleeping(int id, boolean state, int face)
    {
        this.id = id;
        this.state = state;
        this.face = face;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeInt(id);
        buffer.writeBoolean(state);
        buffer.writeInt(face);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        id = buffer.readInt();
        state = buffer.readBoolean();
        face = buffer.readInt();
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
        Entity ent = Minecraft.getMinecraft().world.getEntityByID(id);
        if(ent instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)ent;
            try {
                sleeping.set(player, true);
            } catch (IllegalAccessException ignored) {}

            player.renderOffsetX = 0.0F;
            player.renderOffsetZ = 0.0F;

            if(state)
            {
                if(face == EnumFacing.SOUTH.ordinal())
                {
                    player.renderOffsetZ = -1.8F;
                }
                else if(face == EnumFacing.NORTH.ordinal())
                {
                    player.renderOffsetZ = 1.8F;
                }
                else if(face == EnumFacing.WEST.ordinal())
                {
                    player.renderOffsetX = 1.8F;
                }
                else if(face == EnumFacing.EAST.ordinal())
                {
                    player.renderOffsetX = -1.8F;
                }
                if(!Keygrip.eventHandlerClient.sleepers.contains(player))
                {
                    Keygrip.eventHandlerClient.sleepers.add(player);
                }
            }
            else
            {
                try {
                    sleeping.set(player, false);
                } catch (IllegalAccessException ignored) {}
                player.bedLocation = null;
                Keygrip.eventHandlerClient.sleepers.remove(player);
            }
        }
    }
}
