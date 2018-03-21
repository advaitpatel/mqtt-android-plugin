/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.PROJECT_CREATION_TITLE;
import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.WELCOME_MSG;
import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.WELCOME_PAGE_TITLE;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.TEXT_FONT_SIZE_1;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.TITLE_FONT_SIZE_1;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.addComponent;


/**
 * The first page for the wizard, the welcome page. Gives a summary of the purpose of the wizard.
 *
 * @author Zeyao Jin (Mark)
 */
public class WelcomePage extends WizardPage {
    
    private static final Logger log = Logger.getInstance(WelcomePage.class);

    public WelcomePage() {
        super(WELCOME_PAGE_TITLE, "Description");
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        setPrevEnabled(false);
        setNextEnabled(true);
        setFinishEnabled(false);
        layoutScreen(settings);
    }
    
    private void layoutScreen(WizardSettings settings) {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        int yCounter=0;
    
        JLabel title=new JLabel(PROJECT_CREATION_TITLE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE_1));
        addComponent(this,title,0,yCounter++,1,1,GridBagConstraints.NORTH);
        
        addComponent(this,new JLabel(" "),0,yCounter++,1,1,GridBagConstraints.NORTH);
        
        JLabel body=new JLabel(WELCOME_MSG);
        body.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
        addComponent(this,body,0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        addComponent(this,new JLabel(" "),0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        this.setVisible(true);
    }
    
    @Override
    public boolean isScrollable() {
        return true;
    }
}
