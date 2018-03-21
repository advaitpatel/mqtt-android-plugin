/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import com.intellij.lang.StdLanguages;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Series of (foreseeable) common IntelliJ Platform related utility methods.
 * Most of these are for manipulating Psi structure.
 *
 * @author Zeyao Jin (Mark)
 */
public class Utilities {
    
    private static final Logger log = Logger.getInstance(Utilities.class);
    
    // (Global) Font size constants for use with wizards.
    public static final int TITLE_FONT_SIZE_1 = 28;
    public static final int TEXT_FONT_SIZE_1 = 14;
    
    /**
     * Adds JComponents to a GridBagLayout JPanel "in a good way".
     *
     * @param p A GridBagLayout JPanel.
     * @param c The JComponent to be added.
     * @param x The x position for the JComponent.
     * @param y The y position for the JComponent.
     * @param width Grid width of the JComponent.
     * @param height Grid height of the JComponent.
     * @param align GridBagConstraints alignment for the JComponent.
     */
    public static void addComponent(JPanel p, JComponent c, int x, int y, int width, int height, int align) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.gridheight = height;
        /*gc.weightx = 100.0;
        gc.weighty = 100.0;*/
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = align;
        gc.fill = GridBagConstraints.NONE;
        p.add(c, gc);
    }
    
    /**
     * Given the project and manifest url path, checks to see if there is a service defined in the manifest.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     * @param manifestUrl The url of the manifest (protocol + path).
     * @return True if a service tag was found in the manifest, false otherwise.
     */
    public static boolean findService(Project project, final String manifestUrl) {
        try {
            VirtualFile manifest=VirtualFileManager.getInstance().findFileByUrl(manifestUrl);
            
            XmlTag manifestTag=((XmlFile) PsiManager.getInstance(project).findViewProvider(manifest).getPsi(StdLanguages.XML)).getRootTag();
            for (XmlTag element: manifestTag.getSubTags()) {
                if (element.getName().equals("application")) {
                    for (XmlTag element2: element.getSubTags()) {
                        if (element2.getName().equals("service")) {
                            return true;
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            log.error("findService failed: " + manifestUrl, e);
        }
        return false;
    }
    
    /**
     * Given the relative path of a file and an appropriate object (used to get class loader),
     * returns the contents of the file in a byte array.
     *
     * @param path Relative path of the file.
     * @param clazz Object used to get a class loader.
     * @return A byte[] representing the contents of the file.
     * @throws IOException If the file failed to be read.
     */
    public static byte[] getFileBytes(String path, Class clazz) throws IOException {
        int length;
        byte[] bytes=new byte[4096];
        InputStream is = clazz.getClassLoader().getResourceAsStream(path);
        length = is.available();
        
        int read = 0, total = 0;
        while ((read = is.read(bytes, 0, Math.min(length-total, 4096))) > 0) {
            total+=read;
        }
        is.close();
        
        return bytes;
    }
    
    /**
     * Temporary File workaround for accessing template files (in order to get a working canonical path).
     *
     * @param path Relative path of the file.
     * @param clazz Object used to get a class loader.
     * @return A temporary File that has the contents of the template.
     * @throws IOException If the file failed to be read.
     */
    public static File getTemplateFile(String path, Class clazz) throws IOException {
        int length;
        File tempFile = File.createTempFile("service_creation", ".vm");
        InputStream is = clazz.getClassLoader().getResourceAsStream(path);
        OutputStream os = new FileOutputStream(tempFile);
        length = is.available();
        
        byte[] bytes = new byte[4096];
        int read = 0, total = 0;
        while ((read = is.read(bytes, 0, Math.min(length-total, 4096))) > 0) {
            os.write(bytes, 0, read);
            total+=read;
        }
        is.close();
        os.close();
        
        log.debug("Template File stored in " + tempFile);
        return tempFile;
    }
    
    /**
     * Finds all directories in res/ and all files in res/values/ except for strings.xml.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     * @return A list of the path urls of the directories and files found.
     */
    public static List<String> findRemove(Project project) {
        List<String> filesFound = new ArrayList<>();
        
        VirtualFile root = getJavaSourceRoot(project);
        if (root == null) {
            log.error("findRemove: root is null");
            return null;
        }
        root=root.getParent();
        root=root.findChild("res"); // res
        PsiDirectory res = PsiDirectoryFactory.getInstance(project).createDirectory(root);
        
        for (PsiDirectory dir : res.getSubdirectories()) {
            if (dir.getName().equals("values")) {
                PsiFile[] files = dir.getFiles();
                for (PsiFile file : files) {
                    if (!file.getVirtualFile().getName().toLowerCase().equals("strings.xml")) {
                        filesFound.add(file.getVirtualFile().getUrl());
                    }
                }
            }
            else {
                filesFound.add(dir.getVirtualFile().getUrl());
            }
        }
        return filesFound;
    }
    
    /**
     * Tries to find the manifest and build.gradle files.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     * @param MANIFEST_KEY The key that the manifest url should be stored with.
     * @param GRADLE_KEY The key that the manifest url should be stored with.
     * @return A map of MANIFEST_KEY and GRADLE_KEY to the url of the manifest and gradle respectively. If they are not found, the value for the keys will be null.
     */
    public static Map<String,String> findModify(Project project, final String MANIFEST_KEY, final String GRADLE_KEY) {
        Map<String,String> filesFound = new HashMap<>();
        
        VirtualFile root = getJavaSourceRoot(project);
        if (root == null) {
            log.error("findModify: root is null");
            return null;
        }
        
        root=root.getParent(); // main
        VirtualFile manifest = root.findChild("AndroidManifest.xml");
        filesFound.put(MANIFEST_KEY,manifest.getUrl());
        
        root=root.getParent().getParent(); // app
        VirtualFile gradle = root.findChild("build.gradle");
        filesFound.put(GRADLE_KEY,gradle.getUrl());
        
        return filesFound;
    }
    
    /**
     * Only meant to be used on NEW "no activity" or "empty activity" projects.
     * Looks down one group of nested packages and returns as soon as it sees any
     * files in its current subdirectory (very not robust). This will most likely
     * look bad to the user if they try to use this on an existing/other project.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     * @return A list of the urls of the first classes found in a subdirectory, as defined above.
     */
    public static List<String> findClasses(Project project) {
        List<String> filesFound = new ArrayList<>();
        
        VirtualFile root = getJavaSourceRoot(project);
        if (root == null) {
            return filesFound;
        }
        PsiDirectory dir=PsiDirectoryFactory.getInstance(project).createDirectory(root);
        
        while (true) {
            if (dir.getFiles().length > 0) {
                for (PsiFile file: dir.getFiles()) {
                    filesFound.add(file.getVirtualFile().getUrl());
                }
                break;
            }
            if (dir.getSubdirectories().length == 1) {
                dir=dir.getSubdirectories()[0];
                continue;
            }
            break;
        }
        
        return filesFound;
    }
    
    /**
     * This method returns a PsiDirectory representing the last directory of the package.
     * New files should be added directly into this PsiDirectory.
     *
     * @param pack The package string (e.g. "com.example.test").
     * @param project The {@link Project project} (from AnActionEvent).
     * @return The PsiDirectory representing the package string, or null if no java source roots were found.
     */
    public static PsiDirectory getPackageDirectory(String pack, Project project) {
        PsiDirectoryFactory dirFactory = PsiDirectoryFactory.getInstance(project);
        
        VirtualFile rootFile = getJavaSourceRoot(project);
        if (rootFile == null) {
            return null;
        }
        PsiDirectory directory = dirFactory.createDirectory(rootFile);
        
        // Make directories for package string parameter
        String[] dirs = pack.split("\\.");
        
        for (String dir : dirs) {
            boolean dirExists = false;
            log.trace("Directory: " + dir + "; Virtual File Path: " +
                    directory.getVirtualFile().getCanonicalPath());
            for (PsiDirectory psiDir : directory.getSubdirectories()) {
                if (psiDir.getName().equals(dir)) {
                    dirExists = true;
                    break;
                }
            }
            if (dirExists) {
                directory = directory.findSubdirectory(dir);
            } else {
                List<PsiDirectory> listDirectory = new LinkedList<>();
                PsiDirectory finalDirectory = directory;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    listDirectory.add(finalDirectory.createSubdirectory(dir));
                });
                directory = listDirectory.get(0);
            }
        }
        
        return directory;
    }
    
    /**
     * Given a project, tries to find a VirtualFile corresponding to a Java
     * source root. So far, this will only work every time if there is
     * exactly 1 Java source root in the project, e.g. there is only one
     * module in the project.
     *
     * @param project The {@link Project project} (from AnActionEvent).
     * @return The VirtualFile representing the top directory of the Java source
     *         root, or {@code null} if there is no Java source root or if there
     *         is more than one Java source root and the algorithm was unable to
     *         resolve the intended one.
     */
    public static VirtualFile getJavaSourceRoot(Project project) {
    
        // First check if there is only 1 Java source root
        VirtualFile[] roots=ProjectRootManager.getInstance(project).getContentSourceRoots();
        if (roots.length == 0) {
            log.error("No source roots found");
            return null; // Reaching this is a significant error. Trying to create a file is meaningless in this case.
        }
        VirtualFile rootFile = null;
        VirtualFile virtualFile = null;
        Module module = null;
    
        for (VirtualFile root: roots) {
            if (root.getName().toLowerCase().contains("java")) {
                if (!root.getParent().getName().toLowerCase().contains("test")) {
                    if (rootFile != null) {
                        log.error("Multiple Java source roots found.");
                        rootFile=null; // More than one module in the project
                        break;
                    }
                    rootFile=root;
                }
            }
        }
    
        // Looking up the module for the currently open file is more like a backup solution.
        // This won't always work, especially in Android Studio.
        if (rootFile == null) {
            PsiDirectoryFactory dirFactory = PsiDirectoryFactory.getInstance(project);
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            try {
                virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
                if (!virtualFile.getCanonicalPath().contains(project.getBaseDir().getCanonicalPath())) {
                    throw new NullPointerException(); // Open file isn't part of the user's project.
                }
                module = ModuleUtil.findModuleForFile(virtualFile, project);
            } catch (NullPointerException e) {
                log.debug("No valid files open in editor.");
                module = null;
            }
        
            if (module != null) {
                roots = ModuleRootManager.getInstance(module).getSourceRoots(false);
                for (VirtualFile root: roots) {
                    if (isJavaSource(dirFactory.createDirectory(root))) {
                        rootFile=root;
                    }
                }
            }
        }
    
        return rootFile;
    }
    
    /**
     * Tries to determine whether or not a PsiDirectory is a Java source root by
     * recursively looking through the directories in the given PsiDirectory to see if
     * it contains any files with the .java extension.
     *
     * @param dir The Java source root directory under examination.
     * @return True if a .java file was found, false otherwise.
     */
    private static boolean isJavaSource(PsiDirectory dir) {
        for (PsiFile file : dir.getFiles()) {
            if (file.getVirtualFile().getName().endsWith(".java")) {
                return true;
            }
        }
        for (PsiDirectory subDir : dir.getSubdirectories()) {
            if (isJavaSource(subDir)) {
                return true;
            }
        }
        return false;
    }
}
