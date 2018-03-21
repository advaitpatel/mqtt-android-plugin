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
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.*;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.TEXT_FONT_SIZE_1;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.TITLE_FONT_SIZE_1;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.addComponent;


public class FinishPage extends WizardPage {
    
    private static final Logger log = Logger.getInstance(FinishPage.class);
    
    public FinishPage() {
        super(FINISH_PAGE_TITLE, "");
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        setPrevEnabled(true);
        setNextEnabled(false);
        setFinishEnabled(true);
        layoutScreen(settings);
    }
    
    private void layoutScreen(WizardSettings settings) {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        int yCounter=0;
    
        JLabel title=new JLabel(FINISH_PAGE_TITLE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE_1));
        addComponent(this,title,0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        addComponent(this,new JLabel(" "),0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        Map<String,String> modify = (Map<String,String>) settings.get(MODIFY_KEY);
        Map<String,Boolean> removeBools = (Map<String,Boolean>) settings.get(REMOVE_BOOLS_KEY);
    
        JLabel modifyText=new JLabel(MODIFY_CONTENT_MESSAGE);
        modifyText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
        addComponent(this,modifyText,0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        Box box1 = Box.createVerticalBox();
        box1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        for (String url: modify.values()) {
            JLabel label=new JLabel(url.substring(PROTOCOL_INDEX).replace('/', File.separatorChar));
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
            box1.add(label);
        }
        addComponent(this,box1,0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        if (removeBools.size() > 0) {
            JLabel deleteText = new JLabel(DELETE_CONTENT_MESSAGE);
            deleteText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
            addComponent(this, deleteText, 0, yCounter++, 1, 1, GridBagConstraints.NORTH);
    
            Box box2 = Box.createVerticalBox();
            box2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            for (String url : removeBools.keySet()) {
                if (removeBools.get(url)) {
                    JLabel label=new JLabel(url.substring(PROTOCOL_INDEX).replace('/', File.separatorChar));
                    label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
                    box2.add(label);
                }
            }
            addComponent(this, box2, 0, yCounter++, 1, 1, GridBagConstraints.NORTH);
        }
        
        this.setVisible(true);
    }
    
    @Override
    public boolean isScrollable() {
        return true;
    }
}
