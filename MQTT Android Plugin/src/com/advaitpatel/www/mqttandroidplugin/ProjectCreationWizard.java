/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import com.github.cjwizard.WizardListener;
import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.FileASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LightVirtualFile;

import java.io.*;
import java.util.*;
import java.util.List;

import static com.advaitpatel.www.mqttandroidplugin.MachfuConstants.*;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.getFileBytes;
import static com.advaitpatel.www.mqttandroidplugin.Utilities.getPackageDirectory;

import org.jetbrains.plugins.groovy.GroovyFileType;

/**
 * The project creation wizard.
 *
 * @author Zeyao Jin (Mark)
 */
public class ProjectCreationWizard extends MachfuPluginWizard {
    
    private static final Logger log = Logger.getInstance(ProjectCreationWizard.class);

    /**
     * The project creation wizard. This should be called from an action.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     */
    public ProjectCreationWizard(Project project) {
        super(new PageFactory());
        getUserInput().put(PROJECT_KEY,project);
    
        addWizardListener(new WizardListener() {
        
            @Override
            public void onCanceled(List<WizardPage> path, WizardSettings settings) {
                ProjectCreationWizard.this.dispose();
            }
        
            @Override
            @SuppressWarnings("unchecked")
            public void onFinished(List<WizardPage> path, WizardSettings settings) {
                log.debug("Project Creation Wizard finished.  Settings: "+settings);
                
                // Delete and modify
                Map<String,Boolean> removeBools = (Map<String,Boolean>) settings.get(REMOVE_BOOLS_KEY);
                List<String> remove=new ArrayList<>(removeBools.size());
                for (String url: removeBools.keySet()) {
                    if (removeBools.get(url)) {
                        remove.add(url);
                    }
                }
                Map<String,String> modify = (Map<String,String>) settings.get(MODIFY_KEY);
                
                try {
                    makeMinimal(project, remove, modify);
                }
                catch (IOException e) {
                    log.error(e);
                }
                
                // Make service (if selected)
                if (settings.get(CREATE_SERVICE_KEY) != null && (boolean) settings.get(CREATE_SERVICE_KEY)) {
                    final String manifestPath = modify.get(MANIFEST_KEY);
                    Set<TemplateVariable> variables = (Set<TemplateVariable>) settings.get(VARIABLES_KEY);
                    
                    generateService(project,manifestPath,variables,settings);
                }
                
                project.getBaseDir().refresh(false,true);
                ProjectCreationWizard.this.dispose();
            }
        
            @Override
            public void onPageChanged(WizardPage newPage, List<WizardPage> path) {
//                // Set the dialog title to match the description of the new page:
//                ProjectCreationWizard.this.setTitle(newPage.getDescription());
            }
        });
    }

    private static void generateService(Project project, final String manifestPath, Set<TemplateVariable> variables, WizardSettings settings) {
        
        for (TemplateVariable variable : variables) {
            variable.setValue(settings.get(variable.getName()));
        }
    
        String fileName = (String) settings.get(NAME_KEY);
        if (!fileName.endsWith(".java")) { // add extension if not present.
            fileName += ".java";
        }
    
        String packageName = (String) settings.get(PACKAGE_KEY);
        String templatePath = (String) settings.get(TEMPLATE_PATH_KEY);
    
        log.debug("Generating template with " + variables);
        String text = VelocityWrapper.generate(variables, templatePath);
    
        // done with template file.
        File templateFile = new File(templatePath);
        if (!templateFile.delete()) {
            templateFile.deleteOnExit();
        }
    
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
    
        PsiDirectory psiDirectory = getPackageDirectory(packageName, project);
        if (psiDirectory == null) {
            Messages.showMessageDialog("The project is not setup correctly, or there is no file open. (The Plugin can not find the Java source root)", "Error", Messages.getErrorIcon());
            log.error("PSIDirectory is null.  Couldn't find java source root. Is this a java project? No files have been generated.");
            return;
        } else {
            try {
                PsiFile psiFile = psiFileFactory.createFileFromText(fileName, FileTypes.PLAIN_TEXT, text);
            
                // Add service to manifest
                VirtualFile manifest = VirtualFileManager.getInstance().findFileByUrl(manifestPath);
            
                XmlTag manifestTag = ((XmlFile) PsiManager.getInstance(project).findViewProvider(manifest).getPsi(StdLanguages.XML)).getRootTag();
                for (XmlTag element : manifestTag.getSubTags()) {
                    if (element.getName().equals("application")) {
                        XmlTag service = element.createChildTag("service", "", "", false);
                        service.setAttribute("android:enabled", "true");
                        service.setAttribute("android:exported", "true");
                        service.setAttribute("android:name", packageName + "." + fileName.substring(0, fileName.length() - 5));
                        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                            @Override
                            public void run() {
                                log.debug("Modifying manifest (adding service tag): " + manifest);
                                element.addSubTag(service, false);
                            }
                        });
                    }
                }
            
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    psiDirectory.add(psiFile);
                });
            } catch (RuntimeException e) {
                log.info("Modifying manifest error (most likely worked regardless)", e);
            }
        }
    }
    
    /**
     * Removes the files and directories selected in this wizard and modifies the build.gradle and AndroidManifest.xml files.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     * @param remove List of url paths representing the files and directories to be deleted.
     * @param modifyPaths Map of the manifest and gradle keys to their respective url paths.
     * @throws IOException This could be thrown for any number of reasons, most likely some file failed to be modified/deleted.
     */
    private static void makeMinimal(Project project, List<String> remove, Map<String,String> modifyPaths) throws IOException {
        for (String url: remove) {
            try {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    try {
                        VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
                        log.debug("Deleting file/directory: " + file);
                        file.delete(null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (RuntimeException e) {
                log.error("Failed to delete file/directory: " + url);
                throw new IOException(e);
            }
        }
        
        final String manifestUrl=modifyPaths.get(MANIFEST_KEY);
        try {
            VirtualFile manifest=VirtualFileManager.getInstance().findFileByUrl(manifestUrl);
            
            XmlTag manifestTag=((XmlFile) PsiManager.getInstance(project).findViewProvider(manifest).getPsi(StdLanguages.XML)).getRootTag();
            for (XmlTag element: manifestTag.getSubTags()) {
                if (element.getName().equals("application")) {
                    for (XmlTag element2: element.getSubTags()) {
                        if (element2.getName().equals("activity")) {
                            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                                @Override
                                public void run() {
                                    log.debug("Modifying manifest (deleting activity tags): " + manifest);
                                    ((CompositePsiElement) element).removeChild(element2.getNode());
                                }
                            });
                        }
                    }
                    for (XmlAttribute attribute: element.getAttributes()) {
                        if (attribute.getName().equals("android:allowBackup")) {
                            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                                @Override
                                public void run() {
                                    attribute.setValue("false");
                                }
                            });
                        } else if (attribute.getName().equals("android:supportsRtl")) {
                            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                                @Override
                                public void run() {
                                    attribute.setValue("false");
                                }
                            });
                        } else if (attribute.getName().equals("android:label")) {
                        
                        } else {
                            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                                @Override
                                public void run() {
                                    ((CompositePsiElement) element).removeChild(attribute.getNode());
                                }
                            });
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            log.error("Failed to modify the Android manifest file: " + manifestUrl);
            throw new IOException(e);
        }
        
        final String gradleUrl=modifyPaths.get(GRADLE_KEY);
        try {
            VirtualFile gradle = VirtualFileManager.getInstance().findFileByUrl(gradleUrl);
            FileEditorManager.getInstance(project).openFile(gradle,true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(gradle.contentsToByteArray())));
            StringBuilder builder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("appcompat") || line.contains("constraint-layout")) {
                    if (!line.startsWith("//")) {
                        builder.append("//");
                    }
                }
                builder.append(line);
                builder.append("\n");
            }
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    log.debug("Modifying gradle: " + gradle);
                    gradle.setBinaryContent(builder.toString().getBytes());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            log.error("Failed to modify the build.gradle file: " + gradleUrl);
            throw new IOException(e);
        }
        
        final String name="build_gradle";
        final String build_gradle="other/build_gradle";
        final byte[] bytes=getFileBytes(build_gradle,ProjectCreationWizard.class);
        
        // To be changed: proguard/minify, sdk versions
        VirtualFile gradle=VirtualFileManager.getInstance().findFileByUrl(gradleUrl);
        
        // Get copies of the nodes we want
        ASTNode buildTypesNode;
        ASTNode compileNode;
        ASTNode targetNode;
        
        {
            LightVirtualFile gradleTemp=new LightVirtualFile(name, GroovyFileType.GROOVY_FILE_TYPE, new String(bytes).replaceAll("\r",""));
            FileASTNode root = PsiManager.getInstance(project).findViewProvider(gradleTemp).getPsi(Language.findLanguageByID("Groovy")).getNode();
            
            ASTNode android=getContent(root, "android");
            if (android == null) {
                log.error("build.gradle: couldn't find android");
                return;
            }
            compileNode=getAttribute(android,"compileSdkVersion");
            if (compileNode == null) {
                log.error("build.gradle: couldn't find compileSdkVersion");
                return;
            }
            ASTNode defaultConfig=getContent(android,"defaultConfig");
            if (defaultConfig == null) {
                log.error("build.gradle: couldn't find defaultConfig");
                return;
            }
            targetNode=getAttribute(defaultConfig,"targetSdkVersion");
            if (targetNode == null) {
                log.error("build.gradle: couldn't find targetSdkVersion");
                return;
            }
            buildTypesNode=getContent(android,"buildTypes");
            if (buildTypesNode == null) {
                log.error("build.gradle: couldn't find buildTypes");
                return;
            }
        }
        
        // Replace existing nodes with template nodes
        {
            FileASTNode root = PsiManager.getInstance(project).findViewProvider(gradle).getPsi(Language.findLanguageByID("Groovy")).getNode();
            
            ASTNode android=getContent(root,"android");
            if (android == null) {
                log.error("build.gradle: couldn't find android (2)");
                return;
            }
            ASTNode compile=getAttribute(android,"compileSdkVersion");
            if (compile == null) {
                log.error("build.gradle: couldn't find compileSdkVersion (2)");
                return;
            }
            ASTNode defaultConfig=getContent(android,"defaultConfig");
            if (defaultConfig == null) {
                log.error("build.gradle: couldn't find defaultConfig (2)");
                return;
            }
            ASTNode target=getAttribute(defaultConfig,"targetSdkVersion");
            if (target == null) {
                log.error("build.gradle: couldn't find targetSdkVersion (2)");
                return;
            }
            ASTNode buildTypes=getContent(android,"buildTypes");
            if (buildTypes == null) {
                log.error("build.gradle: couldn't find buildTypes (2)");
                return;
            }
            
            Document gradleDoc=PsiDocumentManager.getInstance(project).getCachedDocument(PsiManager.getInstance(project).findFile(gradle));
            
            // Replace
            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                @Override
                public void run() {
                    PsiDocumentManager.getInstance(project).commitDocument(gradleDoc);
                    try {
                        android.replaceChild(buildTypes, buildTypesNode);
                        defaultConfig.replaceChild(target, targetNode);
                        android.replaceChild(compile, compileNode);
                    } catch (IndexNotReadyException e) { // Does not actually matter
                        log.debug(e);
                    }
                }
            });
        }
    }
    
    /**
     * This has only been tested for use with Groovy. Part of the API made for going through Groovy files.
     *
     * @param node Parent node of the node to be found.
     * @param name Name of the child node to be found.
     * @return The content (part within brackets) of the child node with the same name, or null if not found.
     */
    private static ASTNode getContent(ASTNode node, String name) {
        for (ASTNode child: node.getChildren(null)) {
            if (child.getChildren(null).length > 0) {
                String childName=child.getChildren(null)[0].getText();
                if (childName.equals(name)) {
                    return child.getChildren(null)[3];
                }
            }
        }
        return null;
    }
    
    /**
     * This has only been tested for use with Groovy. Part of the API made for going through Groovy files.
     *
     * @param node Parent node of the node to be found.
     * @param name Name of the child node to be found.
     * @return The child attribute node with the same name, or null if not found.
     */
    private static ASTNode getAttribute(ASTNode node, String name) {
        for (ASTNode child: node.getChildren(null)) {
            ASTNode[] children=child.getChildren(null);
            if (children.length > 0) {
                String childName=children[0].getText();
                if (childName.equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }
}
