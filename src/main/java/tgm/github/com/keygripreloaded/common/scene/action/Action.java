package tgm.github.com.keygripreloaded.common.scene.action;

import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import tgm.github.com.keygripreloaded.common.KeygripReloaded;
import tgm.github.com.keygripreloaded.common.core.FakeNetHandlerPlayServer;
import tgm.github.com.keygripreloaded.common.packet.PacketToggleSleeping;
import tgm.github.com.keygripreloaded.common.scene.Scene;

public class Action implements Comparable<Action>
{
    public static final int VERSION = 1;
    //Used names = ac, al, ap, i, k, n, nbt, p, s, t, v

    @SerializedName("n")
    public String name;
    @SerializedName("i")
    public String identifier;
    @SerializedName("t")
    public String entityType;
    @SerializedName("k")
    public int startKey;
    @SerializedName("nbt")
    public byte[] nbtToRead;
    @SerializedName("v")
    public int version = VERSION;
    @SerializedName("p")
    public int precreateEntity;
    @SerializedName("P")
    public int persistEntity;
    @SerializedName("h")
    public int hidden;

    @SerializedName("s")
    public int[] offsetPos = new int[3];
    @SerializedName("r")
    public int[] rotation = new int[2];

    @SerializedName("ac")
    public TreeMap<Integer, ArrayList<ActionComponent>> actionComponents = new TreeMap<>(Ordering.natural());
    @SerializedName("al")
    public TreeMap<Integer, LimbComponent> lookComponents = new TreeMap<>(Ordering.natural());
    @SerializedName("ap")
    public TreeMap<Integer, LimbComponent> posComponents = new TreeMap<>(Ordering.natural());

    public transient EntityState state;

    private int redundantSoundChecker = 0;

    public Action(String name, String type, int startKey, NBTTagCompound tag, boolean preCreate, boolean persist) // name of action, entity type (player::<NAME> or entity class name), start key for action, NBT Tag if player, pre-create the player.
    {
        this.identifier = RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH);
        update(name, type, startKey, tag, preCreate, persist);
    }

    public void update(String name, String type, int startKey, NBTTagCompound tag, boolean preCreate, boolean persist)
    {
        this.name = name;
        this.entityType = type;
        this.startKey = startKey;
        if(tag != null)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                CompressedStreamTools.writeCompressed(tag, baos);
                nbtToRead = baos.toByteArray();
            }
            catch(IOException ignored) {}
        }
        precreateEntity = preCreate ? 1 : 0;
        persistEntity = persist ? 1 : 0;
    }

    public int getLength()
    {
        return IntStream.of(
                actionComponents.entrySet().parallelStream().mapToInt(Map.Entry::getKey).max().orElse(0),
                posComponents.entrySet().parallelStream().mapToInt(Map.Entry::getKey).max().orElse(0),
                lookComponents.entrySet().parallelStream().mapToInt(Map.Entry::getKey).max().orElse(0)
        ).max().orElse(0);
    }

    public void doAction(Scene scene, int time)
    {
        if(state == null || state.ent == null) return;

        //            state.ent.worldObj.setBlockState(new BlockPos(state.ent.posX, state.ent.posY + state.ent.getEyeHeight(), state.ent.posZ), Blocks.torch.getDefaultState(), 3);
        ArrayList<ActionComponent> act = actionComponents.get(time);
        if(act != null)
        {
            for(ActionComponent comp : act)
            {
                switch(comp.toggleType)
                {
                    case 1: {
                        state.useItem = !state.useItem;
                        if(!(state.ent instanceof EntityPlayer)) {break;}
                        EntityPlayer player = (EntityPlayer)state.ent;
                        player.stopActiveHand();
                        if(state.useItem && !state.ent.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
                            player.setActiveHand(EnumHand.MAIN_HAND);
                        } else if (state.useItem && !state.ent.getHeldItem(EnumHand.OFF_HAND).isEmpty()) {
                            player.setActiveHand(EnumHand.OFF_HAND);
                        }
                        break;
                    }
                    case 2: {
                        state.sprinting = !state.sprinting;
                        state.ent.setSprinting(state.sprinting);
                        break;
                    }
                    case 3: {
                        state.sneaking = !state.sneaking;
                        state.ent.setSneaking(state.sneaking);
                        break;
                    }
                    case 4: {
                        state.ent.swingArm(EnumHand.MAIN_HAND);
                        RayTraceResult mop = EntityHelper.getEntityLook(state.ent, 4);
                        if(mop != null && mop.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                            EntityHelper.faceEntity(state.ent, mop.entityHit, 1, 1);
                        }
                        break;
                    }
                    case 5: {
                        state.health = comp.itemAction / (float)Scene.PRECISION;
                        state.ent.setHealth(state.health);
                        state.ent.hurtTime = comp.itemNBT[0];
                        state.ent.deathTime = comp.itemNBT[1];
                        if(state.ent.hurtTime <= 0) break;
                        if(state.ent instanceof EntityPlayer) {
                            state.ent.playSound(state.ent.deathTime > 0 ? SoundEvents.ENTITY_PLAYER_DEATH : SoundEvents.ENTITY_PLAYER_HURT, 1.0F, (state.ent.getRNG().nextFloat() - state.ent.getRNG().nextFloat()) * 0.2F + 1.0F);
                        }
                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityStatus(state.ent, (byte)(state.ent.deathTime > 0 ? 3 : 2)), state.ent.dimension);
                        break;
                    }
                    case 6: {
                        state.fire = !state.fire;
                        if(state.fire) {
                            state.ent.setFire(24000);
                        } else {
                            state.ent.extinguish();
                        }
                        break;
                    }
                    case 7: {
                        state.sleeping = !state.sleeping;
                        if(!(state.ent instanceof EntityPlayer)) break;
                        EntityPlayer player = (EntityPlayer)state.ent;
                        KeygripReloaded.channel.sendToDimension(new PacketToggleSleeping(player.getEntityId(), state.sleeping, comp.itemAction), player.dimension);
                        break;
                    }
                    case 8: {
                        state.elytraFlying = !state.elytraFlying;
                        if (!(state.ent instanceof FakePlayer)) break;
                        FakePlayer player = (FakePlayer) state.ent;
                        if (state.elytraFlying) {
                            player.setElytraFlying();
                        } else {
                            player.clearElytraFlying();
                        }
                        break;
                    }
                    case 0: {
                        if(comp.itemAction <= 0) break;
                        if(comp.itemAction <= 6) {
                            if(comp.itemNBT != null) {
                                ItemStack itemStack = ItemStack.EMPTY;
                                ItemStack currentItem = state.ent.getItemStackFromSlot(convertSlotNumToEnum(comp.itemAction));
                                try {
                                    itemStack = new ItemStack(CompressedStreamTools.readCompressed(new ByteArrayInputStream(comp.itemNBT)));
                                } catch(IOException ignored) {}
                                if  (currentItem.isEmpty()
                                        || comp.itemAction < 0 || comp.itemAction > 5
                                        || !currentItem.getDisplayName().equals(itemStack.getDisplayName()) )
                                {
                                    state.ent.setItemStackToSlot(convertSlotNumToEnum(comp.itemAction), itemStack);
                                }
                            } else {
                                state.ent.setItemStackToSlot(convertSlotNumToEnum(comp.itemAction), ItemStack.EMPTY);
                            }
                            if(state.ent instanceof EntityPlayer) {
                                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityEquipment(state.ent.getEntityId(), convertSlotNumToEnum(comp.itemAction), state.ent.getItemStackFromSlot(convertSlotNumToEnum(comp.itemAction))), ((EntityPlayer)state.ent).dimension);
                            }
                        }
                        if(comp.itemAction == 7 && comp.itemNBT != null)
                        {
                            ItemStack stack = null;
                            try {
                                stack = new ItemStack(CompressedStreamTools.readCompressed(new ByteArrayInputStream(comp.itemNBT)));
                            }
                            catch(IOException ignored) {}
                            if(stack != null)
                            {
                                double d0 = state.ent.posY - 0.30000001192092896D + (double)state.ent.getEyeHeight();
                                EntityItem entityitem = new EntityItem(state.ent.world, state.ent.posX, d0, state.ent.posZ, stack);
                                entityitem.setPickupDelay(40);
                                entityitem.setThrower(state.ent.getName());
                                float f;
                                float f1;
                                f = 0.3F;
                                entityitem.motionX = -MathHelper.sin(state.ent.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(state.ent.rotationPitch / 180.0F * (float)Math.PI) * f;
                                entityitem.motionZ = MathHelper.cos(state.ent.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(state.ent.rotationPitch / 180.0F * (float)Math.PI) * f;
                                entityitem.motionY = -MathHelper.sin(state.ent.rotationPitch / 180.0F * (float)Math.PI) * f + 0.1F;
                                f1 = state.ent.getRNG().nextFloat() * (float)Math.PI * 2.0F;
                                f = 0.02F * state.ent.getRNG().nextFloat();
                                entityitem.motionX += Math.cos(f1) * (double)f;
                                entityitem.motionY += (state.ent.getRNG().nextFloat() - state.ent.getRNG().nextFloat()) * 0.1F;
                                entityitem.motionZ += Math.sin(f1) * (double)f;
                                state.ent.world.spawnEntity(entityitem);
                                state.additionalEnts.add(entityitem);
                            }
                        }
                        break;
                    }
                }
                state.ent.onEntityUpdate();
            }
        }
        LimbComponent comp = lookComponents.get(time);
        if(comp != null) {
            state.rot[0] = comp.actionChange[0] / (double)Scene.PRECISION;
            state.rot[1] = comp.actionChange[1] / (double)Scene.PRECISION;
        }
        LimbComponent comp1 = posComponents.get(time);
        if(comp1 != null) {
            IntStream.range(0, 3).forEach(i -> state.pos[i] = (comp1.actionChange[i] + (offsetPos[i] + scene.startPos[i])) / (double)Scene.PRECISION);
        }

        state.ent.motionX = state.ent.motionY = state.ent.motionZ = 0.0D;
        state.ent.setPosition(state.pos[0], state.pos[1], state.pos[2]);
        state.ent.rotationYawHead = state.ent.rotationYaw = (float)state.rot[0];
        state.ent.rotationPitch = (float)state.rot[1];
        if(!(state.ent instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)state.ent;
        if(player.getHealth() > 0.0F && !player.isSpectator()) {
            AxisAlignedBB axisalignedbb;
            if(player.getRidingEntity() != null && !player.getRidingEntity().isDead) {
                axisalignedbb = player.getEntityBoundingBox().union(player.getRidingEntity().getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);
            } else {
                axisalignedbb = player.getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
            }
            player.world.getEntitiesWithinAABBExcludingEntity(player, axisalignedbb).stream()
                    .filter(entity -> entity.isDead)
                    .forEach(entity -> entity.onCollideWithPlayer(player));
        }
        if(state.useItem) {
            if (redundantSoundChecker == 3) {
                player.onUpdate();
                playUseSound(player);
                redundantSoundChecker = 0;
            } else {
                redundantSoundChecker++;
            }
        }
    }

    public boolean createState(WorldServer world, double x, double y, double z) {
        if(state != null && state.ent != null) return false;
        try {
            EntityPlayerMP playerDummy = new FakePlayer(world, EntityHelper.getDummyGameProfile());
            NBTTagCompound tag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(this.nbtToRead));
            playerDummy.readFromNBT(tag);
            playerDummy.setLocationAndAngles(x, y, z, playerDummy.rotationYaw, playerDummy.rotationPitch);
            playerDummy.writeToNBT(tag);
            this.state = new EntityState(playerDummy);

            if(this.entityType.startsWith("player::")) {
                this.state.ent = new FakePlayer(world, EntityHelper.getGameProfile(this.entityType.substring("player::".length())));
                this.state.ent.readFromNBT(tag);
                new FakeNetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.CLIENTBOUND), (FakePlayer)this.state.ent);
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, (FakePlayer)this.state.ent));
                state.ent.onEntityUpdate();
            } else {
                this.state.ent = (EntityLivingBase)Class.forName(this.entityType).getConstructor(World.class).newInstance(world);
                this.state.ent.setSprinting(playerDummy.isSprinting());
                this.state.ent.setSneaking(playerDummy.isSneaking());
                Arrays.stream(EntityEquipmentSlot.values()).forEach(e -> this.state.ent.setItemStackToSlot(e, state.ent.getItemStackFromSlot(e)));
            }
            if(state.ent instanceof EntityLiving) {
                ((EntityLiving)state.ent).setNoAI(true);
                ((EntityLiving)state.ent).tasks.taskEntries.clear();
                ((EntityLiving)state.ent).targetTasks.taskEntries.clear();
            }
            return true;
        }
        catch(Exception e) {
                e.printStackTrace();
        }
        return false;
    }

    public static Action openAction(File file) {
        try {
            byte[] data = new byte[(int)file.length()];
            FileInputStream stream = new FileInputStream(file);
            stream.read(data);
            stream.close();

            return (new Gson()).fromJson(IOUtil.decompress(data), Action.class);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int compareTo(Action comp)
    {
        return Integer.compare(startKey, comp.startKey);
    }

    /**
     * Play sound when player is using item.
     * If player is not using item, this method does nothing.
     * I created this method because of when fake player is using item, sound does not played.(I could not understand why)
     * @param player    Player which is using item
     */
    private static void playUseSound(EntityPlayer player) {
        if (!player.isHandActive() || player.getItemInUseCount() % 4 != 0) return;
        ItemStack itemStack = player.getActiveItemStack();
        if (itemStack.isEmpty()) return;
        if (itemStack.getItemUseAction() == EnumAction.DRINK) {
            player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5F, player.world.rand.nextFloat() * 0.1F + 0.9F);
        }
        if (itemStack.getItemUseAction() == EnumAction.EAT) {
            player.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float)player.world.rand.nextInt(2), (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    /**
     * convert slot number to EntityEquipmentSlot class
     * @param slot slot number (0 = mainHand, 1 = boots, 2 = legs, 3 = chest, 4 = head, 5 = offHand)
     * @return EntityEquipmentSlot class
     */
    public static EntityEquipmentSlot convertSlotNumToEnum(int slot) {
        switch (slot) {
            case 1: return EntityEquipmentSlot.MAINHAND;
            case 2: return EntityEquipmentSlot.FEET;
            case 3: return EntityEquipmentSlot.LEGS;
            case 4: return EntityEquipmentSlot.CHEST;
            case 5: return EntityEquipmentSlot.HEAD;
            case 6: return EntityEquipmentSlot.OFFHAND;
            default: return null;
        }
    }
}
