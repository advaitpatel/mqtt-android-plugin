package com.advaitpatel.www.mqttandroidplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.*;
public class ProjectCreationAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        ProjectCreationWizard wizard=new ProjectCreationWizard(event.getProject());
        wizard.setTitle("Project Creation Wizard");
        wizard.setSize(800, 600);
        wizard.setMinimumSize(new Dimension(800, 600));
        wizard.setResizable(true);
        wizard.setLocationRelativeTo(null);
        wizard.pack();
        wizard.setVisible(true);
    }
}
