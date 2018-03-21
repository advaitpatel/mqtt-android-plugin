package com.advaitpatel.www.mqttandroidplugin;

import com.github.cjwizard.*;
import com.github.cjwizard.pagetemplates.PageTemplate;

public class MachfuPluginWizard extends javax.swing.JDialog {

    private final WizardContainer wizardContainer;

    /**
     * @param factory To create the new page to the wizard
     */
    public MachfuPluginWizard(PageFactory factory) {
        wizardContainer = new WizardContainer(factory);
        init();
    }

    /**
     * @param factory To create the new page to the wizard
     * @param template The template to surround the wizard pages of this dialog.
     * @param settings he collection of settings gleaned during the wizard.
     */
    public MachfuPluginWizard(PageFactory factory, PageTemplate template, WizardSettings settings) {
        wizardContainer = new WizardContainer(factory, template, settings);
        init();
    }

    /**
     * @param forgetTraversedPath the forgetTraversedPath to set
     */
    protected void setForgetTraversedPath(boolean forgetTraversedPath) {
        wizardContainer.setForgetTraversedPath(forgetTraversedPath);
    }

    /**
     * @param wizardListener List of listeners to update on wizard events.
     */
    protected void addWizardListener(WizardListener wizardListener) {
        wizardContainer.addWizardListener(wizardListener);
    }

    /**
     * This method will invoke every time when the wizard will create a new page.
     */
    protected void init() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.getContentPane().add(wizardContainer);
        this.setLocationRelativeTo(getOwner());
        this.pack();
    }

    /**
     * @return settings from the WizardContainer class
     */
    public WizardSettings getUserInput() {
        return wizardContainer.getSettings();
    }
}
