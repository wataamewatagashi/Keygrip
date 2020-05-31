package tgm.github.com.keygripreloaded.client.gui.window;

import java.util.ArrayList;

import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import tgm.github.com.keygripreloaded.client.gui.GuiWorkspace;
import tgm.github.com.keygripreloaded.client.gui.window.element.ElementSceneTab;
import tgm.github.com.keygripreloaded.common.scene.Scene;

public class WindowSceneSelection extends WindowTopDock
{
    public ArrayList<Scene> scenes = new ArrayList<>();
    public int selectedScene;

    public WindowSceneSelection(GuiWorkspace parent, int w, int h)
    {
        super(parent, w, h);

        elements.clear();

        selectedScene = -1;
    }

    @Override
    public void elementTriggered(Element element)
    {
        selectedScene = element.id;
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        if(scenes.isEmpty() || width <= 0) return;
        super.draw(mouseX, mouseY);
        RendererHelper.drawColourOnScreen(workspace.currentTheme.tabSideInactive[0], workspace.currentTheme.tabSideInactive[1], workspace.currentTheme.tabSideInactive[2], 255, posX, posY, width, 1, 0);
    }

    @Override
    public void resized()
    {
        elements.forEach(Element::resized);
        if(!workspace.levels.get(0).isEmpty())
        {
            posX = workspace.levels.get(0).get(0).width - 2;
        } else {
            posX = 0;
        }
        posY = workspace.TOP_DOCK_HEIGHT + 1;
        if(!workspace.levels.get(1).isEmpty())
        {
            width = workspace.width - posX - workspace.levels.get(1).get(0).width + 2;
        } else {
            width = workspace.width - posX;
        }
        height = 12;
    }

    @Override
    public void shutdown()
    {
        scenes.stream().forEach(Scene::destroy);
    }

    public void addScene(Scene scene)
    {
        while (scenes.parallelStream().anyMatch(scene1 -> scene1.identifier.equals(scene.identifier))) {
            String count = scene.identifier.substring(scene.identifier.length()-2);
            if  (count.startsWith("_") && count.substring(1).matches("[0-9]")) {
                int c = Integer.parseInt(count.substring(1)) + 1;
                scene.identifier = scene.identifier.substring(0, scene.identifier.length()-2) + "_" + c;
            } else {
                scene.identifier += "_1";
            }
        }
        while (scenes.parallelStream().anyMatch(scene1 -> scene1.name.equals(scene.name))) {
            String count = scene.name.substring(scene.name.length()-2);
            if  (count.startsWith("_") && count.substring(1).matches("[0-9]")) {
                int c = Integer.parseInt(count.substring(1)) + 1;
                scene.name = scene.name.substring(0, scene.name.length()-2) + "_" + c;
            } else {
                scene.name += "_1";
            }
        }
        scenes.add(scene);
        elements.add(new ElementSceneTab(this, 0, 0, 10, 10, elements.size(), scene));
        if(scenes.size() == 1)
        {
            changeScene(scenes.size() - 1);
        }

        resized();
    }


    public void removeScene(String identifier)
    {
        for(int i = scenes.size() - 1; i >= 0; i--)
        {
            Scene project = scenes.get(i);
            if(!(project.identifier.equals(identifier))) continue;

            project.destroy();
            scenes.remove(i);
            if(i == selectedScene || selectedScene == scenes.size())
            {
                selectedScene--;
                if(selectedScene < 0 && !scenes.isEmpty())
                {
                    selectedScene = 0;
                }
            }
            changeScene(selectedScene);
            break;
        }

        ArrayList<Element> els = new ArrayList<>(elements);
        for(int i = scenes.size() - 1; i >= 0; i--)
        {
            Scene project = scenes.get(i);
            for(Element e : elements)
            {
                if(!(e instanceof ElementSceneTab)) continue;
                ElementSceneTab tab = (ElementSceneTab)e;
                if(tab.info.identifier.equals(project.identifier)) {
                    tab.id = i;
                    els.remove(e);
                }
            }
        }

        for(int i = els.size() - 1; i >= 0; i--)
        {
            if(els.get(i) instanceof ElementSceneTab)
            {
                elements.remove(els.get(i));
            }
        }
        //        if(selectedScene >= 0)
        //        {
        //            updateModelTree(projects.get(selectedProject));
        //        }
        //        else
        //        {
        //            ((GuiWorkspace)workspace).windowModelTree.modelList.trees.clear();
        //            ((GuiWorkspace)workspace).windowAnimate.animList.trees.clear();
        //            ((GuiWorkspace)workspace).windowControls.selectedObject = null;
        //            ((GuiWorkspace)workspace).windowControls.refresh = true;
        //        }

        resized();
    }

    public void changeScene(Scene info)
    {
        scenes.stream().filter(scene -> scene == info)
                .findFirst().ifPresent(this::changeScene);
    }

    public void changeScene(int i)
    {
        selectedScene = i;
        parent.timeline.timeline.selectedIdentifier = "";
        parent.timeline.timeline.setCurrentPos(0);
        parent.timeline.timeline.focusOnTicker();
    }

    @Override
    public int getHeight()
    {
        return 12;
    }
}
