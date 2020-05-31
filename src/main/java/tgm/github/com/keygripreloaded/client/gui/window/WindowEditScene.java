package tgm.github.com.keygripreloaded.client.gui.window;

import net.minecraft.client.resources.I18n;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import tgm.github.com.keygripreloaded.client.gui.GuiWorkspace;
import tgm.github.com.keygripreloaded.common.scene.Scene;

public class WindowEditScene extends Window
{
    public Scene currentScene;

    public WindowEditScene(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.editScene.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newScene.name", ((GuiWorkspace)workspace).getOpenScene().name));
        elements.add(new ElementNumberInput(this, 10, 65, 160, 12, -1, "window.editScene.position", 3, true, -30000000, 30000000, ((GuiWorkspace)workspace).getOpenScene().startPos[0] / (double)Scene.PRECISION, ((GuiWorkspace)workspace).getOpenScene().startPos[1] / (double)Scene.PRECISION, ((GuiWorkspace)workspace).getOpenScene().startPos[2] / (double)Scene.PRECISION));

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));

        currentScene = ((GuiWorkspace)parent).getOpenScene();
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(minimized) return;

        workspace.getFontRenderer().drawString(I18n.format("window.newScene.name"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
        workspace.getFontRenderer().drawString(I18n.format("window.editScene.position"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);

    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id <= 0) return;

        String projName = "";
        int[] startPos = new int[3];
        for (Element value : elements) {
            if (value instanceof ElementTextInput)
            {
                ElementTextInput text = (ElementTextInput) value;
                if (text.id == 1)
                {
                    projName = text.textField.getText();
                }
            } else if (value instanceof ElementNumberInput) {
                ElementNumberInput nums = (ElementNumberInput) value;
                startPos = new int[]{(int) Math.round(Double.parseDouble(nums.textFields.get(0).getText()) * Scene.PRECISION), (int) Math.round(Double.parseDouble(nums.textFields.get(1).getText()) * Scene.PRECISION), (int) Math.round(Double.parseDouble(nums.textFields.get(2).getText()) * Scene.PRECISION)};
            }
        }
        if(projName.isEmpty())
        {
            return;
        }
        currentScene.name = projName;
        currentScene.startPos = startPos;
        ((GuiWorkspace)workspace).sceneManager.resized();
        workspace.removeWindow(this, true);
    }
}
