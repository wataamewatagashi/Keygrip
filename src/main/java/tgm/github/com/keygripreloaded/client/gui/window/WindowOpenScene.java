package tgm.github.com.keygripreloaded.client.gui.window;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementListTree;
import tgm.github.com.keygripreloaded.client.core.ResourceHelper;
import tgm.github.com.keygripreloaded.client.gui.GuiWorkspace;
import tgm.github.com.keygripreloaded.common.scene.Scene;

public class WindowOpenScene extends Window
{
    public ElementListTree modelList;

    public WindowOpenScene(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.open.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        Arrays.stream(ResourceHelper.getScenesDir().listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> FilenameUtils.getExtension(file.getName()).equals("kgs"))
                .forEach(file -> modelList.createTree(null, file, 26, 0, false, false));
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        super.draw(mouseX, mouseY);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if((element.id != 1 && element.id != 3)) return;
        for (ElementListTree.Tree tree : modelList.trees) {
            if(!tree.selected) continue;
            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            Scene scene = Scene.openScene((File)tree.attachedObject);
            if(scene == null)
            {
                workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.open.failed").putInMiddleOfScreen());
            }
            else {
                ((GuiWorkspace)workspace).sceneManager.addScene(scene);
                workspace.removeWindow(this, true);
            }
            break;
            }

    }
}

