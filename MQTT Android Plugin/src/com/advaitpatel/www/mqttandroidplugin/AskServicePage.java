/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.*;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.*;

public class AskServicePage extends WizardPage {
    
    private static final Logger log = Logger.getInstance(AskServicePage.class);
    
    private static final String TEMPLATE_RELATIVE_PATH = "fileTemplates/service_template_file";
    
    public AskServicePage() {
        super(SERVICE_TITLE, "");
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
        List<JTextField> userInputTF = new ArrayList<>();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
    
        try {
            // Get package for overriding
            Project project = (Project) settings.get(PROJECT_KEY);
            Map<String,String> modify = (Map<String,String>) settings.get(MODIFY_KEY);
        
            Map<String, Object> overrides=new HashMap<>();
            final String manifestUrl=modify.get(MANIFEST_KEY);
            VirtualFile manifest= VirtualFileManager.getInstance().findFileByUrl(manifestUrl);
        
            XmlTag manifestTag=((XmlFile) PsiManager.getInstance(project).findViewProvider(manifest).getPsi(StdLanguages.XML)).getRootTag();
            for (XmlAttribute attribute: manifestTag.getAttributes()) {
                if (attribute.getName().equals("package")) {
                    overrides.put(PACKAGE_KEY, attribute.getValue());
                }
            }
        
            // Prepare variables and variable UI
            File templateFile = getTemplateFile(TEMPLATE_RELATIVE_PATH,ProjectCreationWizard.class);
            Set<TemplateVariable> variables = VelocityWrapper.getFormattedVariables(templateFile.getCanonicalPath(), overrides);
        
            settings.put(VARIABLES_KEY,variables);
            settings.put(TEMPLATE_PATH_KEY,templateFile.getCanonicalPath());
        
            GridBagConstraints gBC = new GridBagConstraints();
            gBC.insets = JBUI.insets(5);
        
            if (variables != null) {
                int index = 0;
                for (TemplateVariable variable : variables) {
                
                    switch (variable.getType()) {
                        // TODO: add other types
                        default: {
                            gBC.gridx = 0;
                            gBC.gridy = index;
                            gBC.anchor = GridBagConstraints.LINE_START;
                            JLabel label=new JLabel(variable.getFriendlyName());
                            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
                            jPanel.add(label, gBC);
                        
                            gBC.gridx = 1;
                            gBC.gridy = index;
                            gBC.anchor = GridBagConstraints.LINE_END;
                        
                            String value = variable.getValue() == null ? "" : variable.getValue().toString();
                        
                            JTextField jTextField = new JTextField(value);
                            jTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
                            jTextField.setName(variable.getName());
                            jTextField.setColumns(30);
                            jTextField.setToolTipText(variable.getDescription());
                            jPanel.add(jTextField, gBC);
                        
                            jTextField.getDocument().addDocumentListener(new DocumentListener() {
                                @Override
                                public void insertUpdate(DocumentEvent e) {
                                    inputValidationEmpty(userInputTF);
                                }
                            
                                @Override
                                public void removeUpdate(DocumentEvent e) {
                                    inputValidationEmpty(userInputTF);
                                }
                            
                                @Override
                                public void changedUpdate(DocumentEvent e) {
                                    inputValidationEmpty(userInputTF);
                                }
                            });
                        
                            userInputTF.add(jTextField);
                        }
                    }
                    index++;
                }
            }
        } catch (IOException e) {
            log.error(e);
            return;
        }
    
        // Prepare UI
        this.removeAll();
        this.setLayout(new GridBagLayout());
        int yCounter = 0;
    
        JLabel title = new JLabel("Service Creation");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE_1));
        addComponent(this, title, 0, yCounter++, 2, 1, GridBagConstraints.NORTH);
    
        addComponent(this, new JLabel(" "), 0, yCounter++, 2, 1, GridBagConstraints.NORTH);
    
        JLabel body=new JLabel("Would you like to create a Service class?");
        body.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
        addComponent(this,body,0,yCounter++,2,1,GridBagConstraints.NORTH);
    
        addComponent(this, new JLabel(" "), 0, yCounter++, 2, 1, GridBagConstraints.NORTH);
    
        JCheckBox serviceBox=new JCheckBox(YES, true);
        serviceBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_FONT_SIZE_1));
        serviceBox.setName(CREATE_SERVICE_KEY);
        serviceBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (serviceBox.isSelected()) {
                    jPanel.setVisible(true);
                    inputValidationEmpty(userInputTF);
                }
                else {
                    jPanel.setVisible(false);
                    setNextEnabled(true);
                }
            }
        });
        addComponent(this,serviceBox,0,yCounter++,2,1,GridBagConstraints.NORTH);
        
        jPanel.setVisible(true);
        addComponent(this,jPanel,0,yCounter++,2,3,GridBagConstraints.NORTH);
    
        this.setVisible(true);
    }
    
    @Override
    public boolean isScrollable() {
        return true;
    }
    
    /**
     * Basic input validation. Checks to see if the text is empty/whitespace only.
     *
     * @param userInputTF List of JTextFields to validate.
     */
    private void inputValidationEmpty(List<JTextField> userInputTF) {
        for (JTextField jTextField: userInputTF) {
            String input = jTextField.getText();
            if (input == null || "".equals(input.trim())) {
                setNextEnabled(false);
                return;
            }
        }
        setNextEnabled(true);
    }
}
