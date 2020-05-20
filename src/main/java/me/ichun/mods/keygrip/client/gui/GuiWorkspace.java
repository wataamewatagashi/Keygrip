package me.ichun.mods.keygrip.client.gui;

import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementMinimize;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import me.ichun.mods.keygrip.client.core.EventHandlerClient;
import me.ichun.mods.keygrip.client.gui.window.WindowSaveAs;
import me.ichun.mods.keygrip.client.gui.window.WindowSceneSelection;
import me.ichun.mods.keygrip.client.gui.window.WindowTimeline;
import me.ichun.mods.keygrip.client.gui.window.WindowTopDock;
import me.ichun.mods.keygrip.common.Keygrip;
import me.ichun.mods.keygrip.common.packet.PacketStopScene;
import me.ichun.mods.keygrip.common.scene.Scene;
import me.ichun.mods.keygrip.common.scene.action.Action;
import me.ichun.mods.keygrip.common.scene.action.ActionComponent;
import me.ichun.mods.keygrip.common.scene.action.EntityState;
import me.ichun.mods.keygrip.common.scene.action.LimbComponent;

public class GuiWorkspace extends IWorkspace
{
    public int oriScale;
    public boolean init;

    public float renderTick;

    public WindowTimeline timeline;
    public WindowSceneSelection sceneManager;

    public int sceneSendingCooldown;

    public boolean keyHomeDown;
    public boolean keyEndDown;
    public boolean keyDelDown;
    public boolean keyCDown;
    public boolean keyVDown;

    public Action actionToCopy;

    public GuiWorkspace(int scale)
    {
        levels = new ArrayList<ArrayList<Window>>()
        {{
            add(0, new ArrayList<>()); // dock left - unused
            add(1, new ArrayList<>()); // dock right - unused
            add(2, new ArrayList<>()); // dock btm
            add(3, new ArrayList<>()); // dock top
        }};

        oriScale = scale;

        levels.get(3).add(new WindowTopDock(this, width, 20));
        sceneManager = new WindowSceneSelection(this, width, 20);
        levels.get(3).add(sceneManager);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        if(init) return;

        timeline = new WindowTimeline(this, 0, 0, 100, 100, 100, 50);
        addToDock(2, timeline);
        init = true;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);

        Minecraft.getMinecraft().gameSettings.guiScale = oriScale;
    }

    @Override
    public void updateScreen()
    {
        if(!init) return;

        super.updateScreen();
        if(sceneSendingCooldown > 0)
        {
            sceneSendingCooldown--;
        }
        for(Scene scene : sceneManager.scenes)
        {
            scene.update();
        }
        //TODO want to exit checks? *shrug*
    }

    @Override
    public boolean canClickOnElement(Window window, Element element)
    {
        return !(sceneManager.scenes.isEmpty() && !window.interactableWhileNoProjects() && !(element instanceof ElementMinimize));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float renderTick)
    {
        this.renderTick = renderTick;
        if(!init) return;

        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        ScaledResolution resolution = new ScaledResolution(mc);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, -5000.0D, 5000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        GlStateManager.pushMatrix();

        drawWindows(mouseX, mouseY);

        EventHandlerClient eventHandler = Keygrip.eventHandlerClient;

        if(eventHandler.actionToRecord != null)
        {
            int pX = 5;
            int pY = 35;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RendererHelper.drawTextureOnScreen(eventHandler.txRec, pX, pY, 20, 20, 100);
            GlStateManager.pushMatrix();
            float scale = 2.0F;
            GlStateManager.scale(scale, scale, scale);
            if(eventHandler.sceneFrom.playTime < eventHandler.actionToRecord.startKey + eventHandler.startRecordTime)
            {
                mc.fontRenderer.drawString(Integer.toString((int)(Math.ceil(((eventHandler.actionToRecord.startKey + eventHandler.startRecordTime) - eventHandler.sceneFrom.playTime) / 20D))), (pX + 25) / scale, (pY + 2) / scale, 0xffffff, true);
            }
            else
            {
                mc.fontRenderer.drawString(I18n.format("window.recording"), (pX + 25) / scale, (pY + 2) / scale, 0xffffff, true);
            }
            GlStateManager.popMatrix();
        }

        int scroll = Mouse.getDWheel();

        updateElementHovered(mouseX, mouseY, scroll);

        GlStateManager.popMatrix();

        if(elementSelected == null || elementSelected instanceof ElementButton)
        {
            if(Keyboard.isKeyDown(Keyboard.KEY_HOME) && !keyHomeDown)
            {
                timeline.timeline.setCurrentPos(0);
                timeline.timeline.focusOnTicker();
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_END) && !keyEndDown)
            {
                if(hasOpenScene())
                {
                    timeline.timeline.setCurrentPos(getOpenScene().getLength());
                    timeline.timeline.focusOnTicker();
                }
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_DELETE) && !keyDelDown && !timeline.timeline.selectedIdentifier.isEmpty())
            {
                if(hasOpenScene())
                {
                    Action remove = getOpenScene().actions.parallelStream()
                            .filter(action -> action.identifier.equals(timeline.timeline.selectedIdentifier))
                            .findFirst().orElse(null);
                    if(remove != null)
                    {
                        getOpenScene().actions.remove(remove);
                        Collections.sort(getOpenScene().actions);
                    }
                }
                timeline.timeline.selectedIdentifier = "";
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_C) && !keyCDown
                && !timeline.timeline.selectedIdentifier.isEmpty() && hasOpenScene())
            {
                actionToCopy = getOpenScene().actions.parallelStream()
                        .filter(action -> action.identifier.equals(timeline.timeline.selectedIdentifier))
                        .findFirst().orElse(actionToCopy);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_V) && !keyVDown && hasOpenScene() && actionToCopy != null)
            {
                Action action = (new Gson()).fromJson((new Gson()).toJson(actionToCopy), Action.class);
                if(action != null)
                {
                    Scene scene = getOpenScene();
                    action.identifier = RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH);
                    action.startKey = timeline.timeline.getCurrentPos();
                    timeline.timeline.selectedIdentifier = action.identifier;
                    if(!GuiScreen.isShiftKeyDown())
                    {
                        action.offsetPos = new int[] { (int)Math.round(mc.player.posX * Scene.PRECISION) - scene.startPos[0], (int)Math.round(mc.player.posY * Scene.PRECISION) - scene.startPos[1], (int)Math.round(mc.player.posZ * Scene.PRECISION) - scene.startPos[2] };
                    }
                    scene.actions.add(action);
                    Collections.sort(scene.actions);
                }
            }
        }

        updateKeyStates();

        updateWindowDragged(mouseX, mouseY);

        updateElementDragged(mouseX, mouseY);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
    }

    @Override
    public void updateKeyStates()
    {
        super.updateKeyStates();
        keyHomeDown = Keyboard.isKeyDown(Keyboard.KEY_HOME);
        keyEndDown = Keyboard.isKeyDown(Keyboard.KEY_END);
        keyDelDown = Keyboard.isKeyDown(Keyboard.KEY_DELETE);
        keyCDown = Keyboard.isKeyDown(Keyboard.KEY_C);
        keyVDown = Keyboard.isKeyDown(Keyboard.KEY_V);
    }

    @Override
    public void keyTyped(char c, int key)
    {
        if(key == 1)
        {
            this.mc.displayGuiScreen(null);

            if(this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
        else if(elementSelected != null)
        {
            elementSelected.keyInput(c, key);
        }
    }

    public boolean hasOpenScene()
    {
        return !sceneManager.scenes.isEmpty();
    }

    public Scene getOpenScene()
    {
        return sceneManager.selectedScene >= 0 ? sceneManager.scenes.get(sceneManager.selectedScene) : null;
    }

    public void toggleRecording()
    {
        if(!hasOpenScene()) return;

        Scene scene = getOpenScene();
        if(Keygrip.eventHandlerClient.actionToRecord != null)
        {
            if(Keygrip.config.playbackSceneWhileRecording == 1 && Keygrip.eventHandlerClient.sceneFrom != null)
            {
                Keygrip.eventHandlerClient.sceneFrom.stop();
                Keygrip.channel.sendToServer(new PacketStopScene(Keygrip.eventHandlerClient.sceneFrom.identifier));
            }
            Keygrip.eventHandlerClient.actionToRecord.actionComponents.put(Keygrip.eventHandlerClient.recordActionFrom, new ArrayList<ActionComponent>()
            {{
                add(new ActionComponent(0, 0, null));
            }});
            Keygrip.eventHandlerClient.actionToRecord = null;
            Keygrip.eventHandlerClient.sceneFrom = null;
            Keygrip.eventHandlerClient.recordActionFrom = 0;
            Keygrip.eventHandlerClient.startRecordTime = 0;
            Keygrip.eventHandlerClient.startRecord = false;
        }
        else {
            boolean started = false;
            scene.playTime = 0;
            for(Action a : scene.actions)
            {
                if(!a.identifier.equals(timeline.timeline.selectedIdentifier)) continue;

                started = true;
                Keygrip.eventHandlerClient.sceneFrom = scene;
                Keygrip.eventHandlerClient.actionToRecord = a;
                Keygrip.eventHandlerClient.recordActionFrom = Math.max(Math.min(a.getLength(), timeline.timeline.getCurrentPos() - a.startKey), 0);
                Keygrip.eventHandlerClient.startRecordTime = Keygrip.eventHandlerClient.recordActionFrom;
                if(Keygrip.config.playbackSceneWhileRecording != 1 || scene.actions.size() == 1 || a.startKey > scene.getLength())
                {
                    Keygrip.eventHandlerClient.startRecord = true;
                }
                else {
                    if(this.sceneSendingCooldown <= 0)
                    {
                        if(this.timeline.timeline.getCurrentPos() > scene.getLength())
                        {
                            this.timeline.timeline.setCurrentPos(0);
                        }
                        scene.actions.remove(a);// Prevent scene playing from showing the action you're recording/rerecording as well
                        Scene.sendSceneToServer(scene);
                        scene.actions.add(a);
                        Collections.sort(scene.actions);
                    }
                    this.sceneSendingCooldown = 10;
                }
                if(Keygrip.eventHandlerClient.recordActionFrom == 0)
                {
                    Keygrip.eventHandlerClient.actionToRecord.offsetPos = new int[] { (int)Math.round(mc.player.posX * Scene.PRECISION) - scene.startPos[0], (int)Math.round(mc.player.posY * Scene.PRECISION) - scene.startPos[1], (int)Math.round(mc.player.posZ * Scene.PRECISION) - scene.startPos[2] };
                    Keygrip.eventHandlerClient.actionToRecord.rotation = new int[] {Math.round(mc.player.rotationYaw * Scene.PRECISION), Math.round(mc.player.rotationPitch * Scene.PRECISION)};
                    NBTTagCompound tag = new NBTTagCompound();
                    Minecraft.getMinecraft().player.writeToNBT(tag);
                    tag.setInteger("playerGameType", Minecraft.getMinecraft().playerController.getCurrentGameType().getID());
                    if(tag.isEmpty())
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            CompressedStreamTools.writeCompressed(tag, baos);
                            a.nbtToRead = baos.toByteArray();
                        }
                        catch(IOException ignored) {}
                    }
                } else {
                    LimbComponent lastLook = null;
                    LimbComponent lastPos = null;
                    int lastLookInt = -1;
                    for(Map.Entry<Integer, LimbComponent> e : a.lookComponents.entrySet()) {
                        if(e.getKey() > lastLookInt && e.getKey() < Keygrip.eventHandlerClient.recordActionFrom) {
                            lastLookInt = e.getKey();
                            lastLook = e.getValue();
                        }
                    }
                    int lastPosInt = -1;
                    for(Map.Entry<Integer, LimbComponent> e : a.posComponents.entrySet())
                    {
                        if(e.getKey() > lastPosInt && e.getKey() < Keygrip.eventHandlerClient.recordActionFrom)
                        {
                            lastPosInt = e.getKey();
                            lastPos = e.getValue();
                            }
                        }
                        Minecraft mc = Minecraft.getMinecraft();
                        if(lastLook == null)
                        {
                            lastLook = new LimbComponent(a.rotation[0] / (double)Scene.PRECISION, a.rotation[1] / (double)Scene.PRECISION);
                        }
                        if(lastPos == null)
                        {
                            lastPos = new LimbComponent(0, 0, 0);
                        }
                        mc.player.setLocationAndAngles((lastPos.actionChange[0] + (a.offsetPos[0] + scene.startPos[0])) / (double)Scene.PRECISION, (lastPos.actionChange[1] + (a.offsetPos[1] + scene.startPos[1])) / (double)Scene.PRECISION, (lastPos.actionChange[2] + (a.offsetPos[2] + scene.startPos[2])) / (double)Scene.PRECISION, (float) lastLook.actionChange[0] / Scene.PRECISION, (float) lastLook.actionChange[1] / Scene.PRECISION);
                    }
                Keygrip.eventHandlerClient.prevState = new EntityState(Minecraft.getMinecraft().player);
                Keygrip.eventHandlerClient.nextState = new EntityState(Minecraft.getMinecraft().player);
                Keygrip.eventHandlerClient.dimension = Minecraft.getMinecraft().world.provider.getDimension();
                a.actionComponents.entrySet().removeIf(e -> e.getKey() >= Keygrip.eventHandlerClient.recordActionFrom);
                a.posComponents.entrySet().removeIf(e -> e.getKey() >= Keygrip.eventHandlerClient.recordActionFrom);
                break;
            }
            if(!started || mc.currentScreen != this) return;

            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    public void save(boolean close)
    {
        if(this.sceneManager.scenes.isEmpty()) return;

        Scene scene = this.sceneManager.scenes.get(this.sceneManager.selectedScene);
        boolean saveAs = true;
        boolean error = false;

        if(scene.saveFile != null && scene.saveFile.exists())
        {
            String md5 = IOUtil.getMD5Checksum(scene.saveFile);
            if(md5 != null && md5.equals(scene.saveFileMd5))
            {
                if(Scene.saveScene(scene, scene.saveFile))
                {
                    scene.saveFileMd5 = IOUtil.getMD5Checksum(scene.saveFile);
                    Scene.saveSceneActions(scene);
                    saveAs = false;
                    if(close)
                    {
                        sceneManager.removeScene(scene.identifier);
                    }
                }
                else {
                    error = true;
                }
            }
        }
        if(saveAs)
        {
            this.addWindowOnTop(new WindowSaveAs(this, this.width / 2 - 100, this.height / 2 - 80, 200, 100, 200, 100, close).putInMiddleOfScreen());
        }
        if(error)
        {
            this.addWindowOnTop(new WindowPopup(this, 0, 0, 180, 80, 180, 80, "window.saveAs.failed").putInMiddleOfScreen());
        }
    }


    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
