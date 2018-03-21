/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import com.github.cjwizard.APageFactory;
import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;

import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.ASK_SERVICE_KEY;

/**
 * The page factory.
 *
 * @author Zeyao Jin (Mark)
 */
public class PageFactory extends APageFactory {
    
    private static final Logger log = Logger.getInstance(PageFactory.class);
    
    private final WizardPage[] wizardPages;
    
    /**
     * The page factory. Wizard pages should be initialized here.
     */
    public PageFactory() {
        wizardPages = new WizardPage[] {
                new WelcomePage(),
                new ResourcesPage(),
                new FinishPage(),
                new AskServicePage(),
//                new ServicePage(),
        };
    }
    
    @Override
    public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
        log.info("Path: "+path.toString());
        if (path.size() <= 2 && settings.get(ASK_SERVICE_KEY) != null && (boolean) settings.get(ASK_SERVICE_KEY) && path.contains(wizardPages[1])) {
            return wizardPages[3];
        }
//        if (path.size() <= 3 && settings.get(CREATE_SERVICE_KEY) != null && (boolean) settings.get(CREATE_SERVICE_KEY) && path.contains(wizardPages[3])) {
//            return wizardPages[4];
//        }
        if (path.size() >= 2) {
            return wizardPages[2];
        }
        return wizardPages[path.size()];
    }
}
