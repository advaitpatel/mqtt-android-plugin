/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.*;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.*;



public class ResourcesPage extends WizardPage {
    
    private static final Logger log = Logger.getInstance(ResourcesPage.class);
    
    public ResourcesPage() {
        super(RESOURCE_PAGE_TITLE, "Description");
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        setPrevEnabled(true);
        setNextEnabled(true);
        setFinishEnabled(false);
        layoutScreen(settings);
    }
    
    private void layoutScreen(WizardSettings settings) {
        
        // Preparing variables
        Project project = (Project) settings.get(PROJECT_KEY);
        
        List<String> removeList = findRemove(project);
        Map<String,String> modify = findModify(project,MANIFEST_KEY, GRADLE_KEY);
        if (removeList == null) {
            log.error("findRemove: returned null list");
            return;
        }
        if (modify == null) {
            log.error("findModify: returned null map");
            return;
        }
        List<String> classes = findClasses(project);
        Map<String,Boolean> removeBools = new TreeMap<>();
        
        settings.put(REMOVE_KEY,removeList);
        settings.put(MODIFY_KEY,modify);
        settings.put(CLASSES_KEY,classes);
        settings.put(REMOVE_BOOLS_KEY,removeBools);
        if (!findService(project,modify.get(MANIFEST_KEY))) {
            settings.put(ASK_SERVICE_KEY, true);
        }
    
        // Preparing UI
        this.removeAll();
        this.setLayout(new GridBagLayout());
        int yCounter=0;
        
        JLabel title=new JLabel(REMOVE_RESOURCES_TITLE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE_1));
        addComponent(this,title,0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        addComponent(this,new JLabel(" "),0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        JLabel body=new JLabel(REMOVE_RESOURCES_MESSAGE);
        body.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
        addComponent(this,body,0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        addComponent(this,new JLabel(" "),0,yCounter++,1,1,GridBagConstraints.NORTH);
    
        if (removeList.size() > 0) {
            Box deletedFileBox = Box.createVerticalBox();
            deletedFileBox.setBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.BLACK),
                            REMOVE_RESOURCE_CONFIRMATION_TITLE,
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            TitledBorder.DEFAULT_POSITION,
                            new Font(Font.SANS_SERIF,Font.PLAIN,TEXT_FONT_SIZE_1),
                            Color.BLACK
                    )
            );
            for (String url : removeList) {
                JCheckBox itemBox = new JCheckBox(url.substring(PROTOCOL_INDEX).replace('/', File.separatorChar));
                itemBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
                itemBox.setName(url);
                itemBox.setSelected(true);
                deletedFileBox.add(itemBox);
                removeBools.put(url, true);
    
                itemBox.addItemListener(e -> {
                    if (itemBox.isSelected()) {
                        removeBools.put(url, true);
                    } else {
                        removeBools.put(url, false);
                    }
                });
            }
            addComponent(this, deletedFileBox, 0, yCounter++, 1, 1, GridBagConstraints.NORTH);
        }
    
        if (classes.size() > 0) {
            
            Box deletedFileBox = Box.createVerticalBox();
            deletedFileBox.setBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.BLACK),
                            REMOVE_CLASSES_CONFIRMATION_TITLE,
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            TitledBorder.DEFAULT_POSITION,
                            new Font(Font.SANS_SERIF,Font.PLAIN,TEXT_FONT_SIZE_1),
                            Color.BLACK
                    )
            );
            for (String url : classes) {
                JCheckBox itemBox = new JCheckBox(url.substring(PROTOCOL_INDEX).replace('/', File.separatorChar));
                itemBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
                itemBox.setName(url);
                itemBox.setSelected(true);
                deletedFileBox.add(itemBox);
                removeBools.put(url, true);
    
                itemBox.addItemListener(e -> {
                    if (itemBox.isSelected()) {
                        removeBools.put(url, true);
                    } else {
                        removeBools.put(url, false);
                    }
                });
            }
            addComponent(this, deletedFileBox, 0, yCounter++, 1, 1, GridBagConstraints.NORTH);
        }
    
        this.setVisible(true);
    }
    
    @Override
    public boolean isScrollable() {
        return true;
    }
}
