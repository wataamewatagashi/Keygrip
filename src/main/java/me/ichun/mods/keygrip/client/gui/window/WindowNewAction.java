package me.ichun.mods.keygrip.client.gui.window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementCheckBox;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.client.gui.window.element.ElementEntitySelector;
import me.ichun.mods.keygrip.common.scene.action.Action;

public class WindowNewAction extends Window
{

    ElementEntitySelector elementEntitySelector;

    List<Element> underEntitySelector = new ArrayList<>();

    public WindowNewAction(IWorkspace parent, int x, int y, int w, int h, int minW, int minH) {
        super(parent, x, y, w, h, minW, minH, "window.newAction.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newAction.name"));

        elements.add(elementEntitySelector = new ElementEntitySelector(this, 10, 65, width-20, 13, -3, EntityPlayer.class));

        underEntitySelector.add(new ElementTextInput(this, 10, 135, width - 20, 12, 2, "window.newAction.playerName"));

        underEntitySelector.add(new ElementCheckBox(this, 11, 89, -1, false, 0, 0, "window.newAction.preCreate", false));
        underEntitySelector.add(new ElementCheckBox(this, 11, 109, -2, false, 0, 0, "window.newAction.persist", false));
        underEntitySelector.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        underEntitySelector.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        elements.addAll(underEntitySelector);
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized) {
            workspace.getFontRenderer().drawString(I18n.format("window.newAction.name"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.format("window.newAction.entityType"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
            if (!elementEntitySelector.min) return;
            workspace.getFontRenderer().drawString(I18n.format("window.newAction.preCreate"), posX + 23, posY + 90, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.format("window.newAction.persist"), posX + 23, posY + 110, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.format("window.newAction.playerName"), posX + 11, posY + 125, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void update() {
        if (elementEntitySelector.min) {
            elements.addAll(underEntitySelector);
        } else {
            underEntitySelector.forEach(element -> elements.remove(element));
        }
        super.update();
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0) {
            workspace.removeWindow(this, true);
        }
        if(element.id > 0 && element.id != 3 && element.id != 4) {
            String actName = "";
            String playerName = "";
            String clzName = "";
            boolean create = false;
            boolean persist = false;
            boolean isPlayer = false;
            NBTTagCompound tag = new NBTTagCompound();
            Minecraft.getMinecraft().player.writeToNBT(tag);
            tag.setInteger("playerGameType", Minecraft.getMinecraft().playerController.getCurrentGameType().getID());

            for (Element e : elements) {
                if (e instanceof ElementTextInput) {
                    ElementTextInput text = (ElementTextInput) e;
                    if (text.id == 1) {
                        actName = text.textField.getText();
                    } else if (text.id == 2) {
                        playerName = text.textField.getText();
                    }
                } else if (e instanceof ElementCheckBox) {
                    if (e.id == -1) {
                        create = ((ElementCheckBox) e).toggledState;
                    } else if (e.id == -2) {
                        persist = ((ElementCheckBox) e).toggledState;
                    }
//                } else if (e instanceof ElementSelector) {
//                    ElementSelector selector = (ElementSelector) e;
//                    Object obj = selector.choices.get(selector.selected);
//                    if (obj == null) continue;
//                    clzName = ((Class) obj).getName();
//                    if (EntityPlayer.class.isAssignableFrom((Class) obj)) {
//                        isPlayer = true;
//                        if (playerName.isEmpty()) {
//                            playerName = Minecraft.getMinecraft().player.getName();
//                        }
//                    }
                }
            }
            GuiWorkspace parent = (GuiWorkspace)workspace;
            clzName = parent.selectedEntity.getSimpleName();
            if (EntityPlayer.class.isAssignableFrom(parent.selectedEntity)) {
                isPlayer = true;
                if (playerName.isEmpty()) {
                    playerName = Minecraft.getMinecraft().player.getName();
                }
            }
            if(parent.hasOpenScene()) {
                if(actName.isEmpty()) {
                    actName = "NewAction" + (parent.getOpenScene().actions.size() + 1);
                }
                Action action = new Action(actName, !isPlayer ? clzName : "player::" + playerName, parent.timeline.timeline.getCurrentPos(), tag, create, persist);
                parent.getOpenScene().actions.add(action);
                parent.timeline.timeline.selectedIdentifier = action.identifier;
                Collections.sort(parent.getOpenScene().actions);
            }
            workspace.removeWindow(this, true);
        }
    }
}
