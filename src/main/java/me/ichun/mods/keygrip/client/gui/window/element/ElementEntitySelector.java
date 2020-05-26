package me.ichun.mods.keygrip.client.gui.window.element;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.input.Mouse;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;

public class ElementEntitySelector extends Element {
    private final GuiWorkspace gWorkspace;

    public Class<? extends Entity> currentEntity;

    public boolean min = true;
    boolean hasScrollVert;

    private final int originalHeight;

    public double sliderProgVert;

    public ElementEntitySelector(Window window, int x, int y, int w, int h, int ID, Class<? extends Entity> entityClass) {
        super(window, x, y, w, h, ID, false);

        originalHeight = h;
        gWorkspace = (GuiWorkspace) parent.workspace;
        currentEntity = entityClass;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover) {
        // button
        if (min) {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementButtonBorder[0], parent.workspace.currentTheme.elementButtonBorder[1], parent.workspace.currentTheme.elementButtonBorder[2], 255, getPosX(), getPosY(), width, height, 0);
            if (hover) {
                if (Mouse.isButtonDown(0)) {
                    RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementButtonClick[0], parent.workspace.currentTheme.elementButtonClick[1], parent.workspace.currentTheme.elementButtonClick[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
                } else {
                    RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementButtonBackgroundHover[0], parent.workspace.currentTheme.elementButtonBackgroundHover[1], parent.workspace.currentTheme.elementButtonBackgroundHover[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
                }
            } else {
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementButtonBackgroundInactive[0], parent.workspace.currentTheme.elementButtonBackgroundInactive[1], parent.workspace.currentTheme.elementButtonBackgroundInactive[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
            parent.workspace.getFontRenderer().drawString(net.minecraft.client.resources.I18n.format(currentEntity.getSimpleName()), getPosX() + (width / 2) - (parent.workspace.getFontRenderer().getStringWidth(I18n.format(currentEntity.getSimpleName())) / 2), getPosY() + height - (height / 2) - (parent.workspace.getFontRenderer().FONT_HEIGHT / 2), Theme.getAsHex(parent.workspace.currentTheme.font), false);
            return;
        }

        // list selector
        height = getPosY();


        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeBorder[0], parent.workspace.currentTheme.elementTreeBorder[1], parent.workspace.currentTheme.elementTreeBorder[2], 255, getPosX()+ width - 1, getPosY(), 10, height, 0);

        RendererHelper.startGlScissor(getPosX(), getPosY(), width -1, height);

        final int spacingY = 13;
        // get total element height
        int size = gWorkspace.containsEntity.size() * spacingY;
        hasScrollVert = size > height - 20;

        GlStateManager.pushMatrix();
//        GlStateManager.translate(0D, -((size - (height - 20)) * sliderProgVert), 0D);
        GlStateManager.translate(0D, -((size - height) * sliderProgVert), 0D);

        // Draw animation elements
        if(!gWorkspace.containsEntity.isEmpty()) {
            int idClicked = -1;
            if(mouseX < posX + width - 1 && mouseX >= posX && mouseY >= posY && mouseY < posY + height) {
//                idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / width;
                idClicked = (int)(mouseY - posY + ((size - height) * sliderProgVert)) / width;
            }

            final int[] idHovered = {0};
            final int[] offY = {0};
            int finalIdClicked = idClicked;
            gWorkspace.containsEntity.forEach((name, aClass) -> {
                drawCompElement(name, offY[0], aClass.isAssignableFrom(currentEntity), finalIdClicked == idHovered[0]);

                idHovered[0]++;
                offY[0] += spacingY;
            });
        }
        GlStateManager.popMatrix();
//        RendererHelper.startGlScissor(getPosX() + width, getPosY(), width - (hasScrollVert ?  10 : 0), height);

        GlStateManager.pushMatrix();
//        GlStateManager.translate(0D, -((size - (height - 20)) * sliderProgVert), 0D);
        GlStateManager.translate(0D, -((size - height) * sliderProgVert), 0D);

        //Animation Component areas --> do not need to do
        GlStateManager.popMatrix();

//        RendererHelper.startGlScissor(getPosX() + width, getPosY() - 1, width - (hasScrollVert ?  10 : 0), height + 3);

        GlStateManager.pushMatrix();

        GlStateManager.popMatrix();

        RendererHelper.startGlScissor(getPosX() + width, getPosY() - 1, 10, height + 3); //vert scroll bar

        if (hasScrollVert && !min) {
            int x2 = getPosX() + width - 10;

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, x2, getPosY(), 10, height - 19, 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, x2 + 4, getPosY() + ((double) (height - 20) / 40), 2, (height - 20) - ((double) ((height - 20) / 40) * 2), 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, x2, getPosY() + (((height - 20) - ((double) (height - 20) / 11)) * sliderProgVert), 10, Math.ceil((float)(height - 20) / 10D), 0);
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBar[0], parent.workspace.currentTheme.elementTreeScrollBar[1], parent.workspace.currentTheme.elementTreeScrollBar[2], 255, x2 + 1, getPosY() + 1 + (((height - 20) - ((double) (height - 20) / 11)) * sliderProgVert), 8, Math.ceil(((float)(height - 20) / 10D) - 2), 0);

            int sbx1 = x2 + 1 - parent.posX;
            int sbx2 = sbx1 + 10;
            int sby1 = getPosY() - parent.posY;
            int sby2 = getPosY() + 1 + (height - 20) - parent.posY;

            if(Mouse.isButtonDown(0) && mouseX >= sbx1 && mouseX <= sbx2 && mouseY >= sby1 && mouseY <= sby2) {
                sby1 += 10;
                sby2 -= 10;
                sliderProgVert = 1.0F - MathHelper.clamp((double)(sby2 - mouseY) / (double)(sby2 - sby1), 0.0D, 1.0D);
            }
        } else {
            sliderProgVert = 0.0D;
        }

    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id) {
        if (min) {
            min = false;
            resized();
            return true;
        } else {
            min = true;
            resized();
        }

        int size = gWorkspace.containsEntity.size() * 13;
        int idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13;

        for(int i = 0; i < gWorkspace.containsEntity.size(); i++) {
            if (idClicked != i) continue;
            gWorkspace.selectedEntity = currentEntity = gWorkspace.containsEntity.entrySet().stream().skip(i).findFirst().get().getValue();
            break;
        }
        return true;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k) {
        if (min) return false;

        sliderProgVert += 0.05D * -k;
        sliderProgVert = MathHelper.clamp(sliderProgVert, 0.0D, 1.0D);
        return false;
    }

    @Override
    public boolean mouseInBoundary(int mouseX, int mouseY) {
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height && !(mouseX < posX + width - 1 && mouseY > posY + height - 20);
    }

    @Override
    public void resized() {
        if (min) {
            height = originalHeight;
        } else height = parent.height -posY;
    }

    private void drawCompElement(String name, int offY, boolean isSelected, boolean hover) {
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX(), getPosY() + offY, width - 1, 13, 0);

        if (isSelected) {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBgSelect[0], parent.workspace.currentTheme.elementTreeItemBgSelect[1], parent.workspace.currentTheme.elementTreeItemBgSelect[2], 255, getPosX() + 1, getPosY() + offY + 1, width - 1 - 2, 13 - 2, 0);
        } else if(hover) {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBgHover[0], parent.workspace.currentTheme.elementTreeItemBgHover[1], parent.workspace.currentTheme.elementTreeItemBgHover[2], 255, getPosX() + 1, getPosY() + offY + 1, width - 1 - 2, 13 - 2, 0);
        } else {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBg[0], parent.workspace.currentTheme.elementTreeItemBg[1], parent.workspace.currentTheme.elementTreeItemBg[2], 255, getPosX() + 1, getPosY() + offY + 1, width - 1 - 2, 13 - 2, 0);
        }

        parent.workspace.getFontRenderer().drawString(parent.workspace.reString(name, width - 1), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(parent.workspace.currentTheme.font), false);
    }
}
