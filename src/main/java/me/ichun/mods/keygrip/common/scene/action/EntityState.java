package me.ichun.mods.keygrip.common.scene.action;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class EntityState
{
    public EntityState()
    {
    }

    public EntityState(EntityPlayer player)
    {
        pos[0] = player.posX;
        pos[1] = player.posY;
        pos[2] = player.posZ;

        rot[0] = player.rotationYaw;
        rot[1] = player.rotationPitch;

        dropping = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop.getKeyCode());

        inventory = new ItemStack[] { player.inventory.getCurrentItem(), player.inventory.armorItemInSlot(0), player.inventory.armorItemInSlot(1), player.inventory.armorItemInSlot(2), player.inventory.armorItemInSlot(3), player.inventory.offHandInventory.get(0) };

        health = player.getHealth();
        hurtTime = player.hurtTime;
        deathTime = player.deathTime;

        fire = player.isBurning();

        swinging = player.isSwingInProgress;

        useItem = player.isHandActive();

        sprinting = player.isSprinting();

        sneaking = player.isSneaking();

        sleeping = player.isPlayerSleeping();

        elytraFlying = player.isElytraFlying();
    }

    public EntityLivingBase ent;
    public ArrayList<Entity> additionalEnts = new ArrayList<>();
    public double[] pos = new double[3];
    public double[] rot = new double[2];
    public ItemStack[] inventory = new ItemStack[6];
    public float health;
    public int hurtTime;
    public int deathTime;
    public boolean fire = false;
    public boolean dropping = false;
    public boolean swinging = false;
    public boolean useItem = false;
    public boolean sprinting = false;
    public boolean sneaking = false;
    public boolean sleeping = false;
    public boolean elytraFlying = false;
}
