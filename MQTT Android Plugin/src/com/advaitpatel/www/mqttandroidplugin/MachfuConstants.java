package com.advaitpatel.www.mqttandroidplugin;

import java.util.ResourceBundle;

public class MachfuConstants {

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/machfu");

    //  Titles
    public static final String COMPANY_NAME_TITLE = resourceBundle.getString("COMPANY_NAME_TITLE");
    public static final String PLUGIN_NAME_TITLE = resourceBundle.getString("PLUGIN_NAME_TITLE");
    public static final String PROJECT_CREATION_TITLE = resourceBundle.getString("PROJECT_CREATION_TITLE");
    public static final String SERVICE_TITLE = resourceBundle.getString("SERVICE_TITLE");

    // Common UI constants
    public static final String YES = resourceBundle.getString("YES");
    public static final String NO = resourceBundle.getString("NO");

    // WizardSettings keys
    public static final String PROJECT_KEY = "_project";
    public static final String ASK_SERVICE_KEY = "_ask_service";
    public static final String CREATE_SERVICE_KEY = "_create_service";
    public static final String REMOVE_KEY = "_remove";
    public static final String MODIFY_KEY = "_modify";
    public static final String CLASSES_KEY = "_classes";
    public static final String REMOVE_BOOLS_KEY = "_remove_bools";
    public static final String VARIABLES_KEY = "_variables";
    public static final String TEMPLATE_PATH_KEY = "_template_path";
    public static final String NAME_KEY = "NAME";
    public static final String PACKAGE_KEY = "PACKAGE_NAME";

    // Other keys
    public static final String MANIFEST_KEY = "_manifest";
    public static final String GRADLE_KEY = "_gradle";

    // File protocol substring index
    public static final int PROTOCOL_INDEX = 7;

    public static final String FOOTER_MESSAGE = resourceBundle.getString("FOOTER_MESSAGE");

    // Template file location
    public static final String TEMPLATE_RELATIVE_PATH = "fileTemplates/service_template_file";

    // Finish Page
    public static final String FINISH_PAGE_TITLE = resourceBundle.getString("FINISH_PAGE_TITLE");
    public static final String MODIFY_CONTENT_MESSAGE = resourceBundle.getString("MODIFY_CONTENT_MESSAGE");
    public static final String DELETE_CONTENT_MESSAGE = resourceBundle.getString("DELETE_CONTENT_MESSAGE");

    // Resource Page
    public static final String RESOURCE_PAGE_TITLE = resourceBundle.getString("RESOURCE_PAGE_TITLE");
    public static final String REMOVE_RESOURCES_TITLE = resourceBundle.getString("REMOVE_RESOURCES_TITLE");
    public static final String REMOVE_RESOURCES_MESSAGE = resourceBundle.getString("REMOVE_RESOURCES_MESSAGE");
    public static final String REMOVE_RESOURCE_CONFIRMATION_TITLE =  resourceBundle.getString("REMOVE_RESOURCE_CONFIRMATION_TITLE");
    public static final String REMOVE_CLASSES_CONFIRMATION_TITLE =  resourceBundle.getString("REMOVE_CLASSES_CONFIRMATION_TITLE");

    // Welcome page
    public static final String WELCOME_PAGE_TITLE =  resourceBundle.getString("WELCOME_PAGE_TITLE");
    public static final String WELCOME_MSG = resourceBundle.getString("WELCOME_MSG");

    // What's new page
    public static final String WHATSNEW_PAGE_TITLE = resourceBundle.getString("WHATSNEW_PAGE_TITLE");
    public static final String WHATSNEW_PAGE_CONTENT = resourceBundle.getString("WHATSNEW_PAGE_CONTENT");



}
