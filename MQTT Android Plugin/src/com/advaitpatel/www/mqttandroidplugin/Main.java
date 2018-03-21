/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    
    private static Map<String, String> parsedOptions;

    public static void main(String[] args) {
        
        // Parse args
        CliHandler cliHandler = new CliHandler(args);
        cliHandler.parse();
        parsedOptions = cliHandler.vars;
        
        String templateDir=parsedOptions.get("template");
        String template=templateDir.substring(templateDir.lastIndexOf(File.separator)+1);
        templateDir=templateDir.substring(0,templateDir.lastIndexOf(File.separator));
        
        // Get variable names from template
        Set<TemplateVariable> variables;
        try {
            variables=VelocityWrapper.getFormattedVariables(templateDir+File.separator+template,null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (variables == null) {
            System.err.println("Parsing variables failed.");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        
        // Get user input for variable values
        for (TemplateVariable var: variables) {
            System.out.println("Enter the value for \"" + var.getFriendlyName() + "\" ("+var.getType().toString()+"): ");
            System.out.println("Default value: "+var.getValue().toString());
            System.out.println("Description: "+var.getDescription());
            String line=scanner.nextLine();
            if (line.equals("")) {
                // Nothing. Stay with default value.
            }
            else {
                var.setValue(TemplateVariable.resolveValue(var.getType(),line));
            }
        }
        
        // Generate template
        String rawFile=VelocityWrapper.generate(variables,templateDir+File.separator+template);
//        System.out.println(rawFile); // Debug
        String name=parsedOptions.get("name");
        if (!name.endsWith(".java")) {
            name += ".java";
        }
        
        try {
            saveFile(rawFile, parsedOptions.get("dest"), name);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a file with the specified name in the specified directory using the given text.
     * 
     * @param rawFile The {@link String String} representing the file.
     * @param directory The absolute path of the destination directory that does not end in 
     *                  a file separator (e.g. "/"), parsed from args.
     * @param fileName The name of the file to be created, including the extension and without 
     *                 a file separator (e.g. "newFile.java").
     * @throws IOException The file writer used failed to close (somehow).
     */
    private static void saveFile(String rawFile, String directory, String fileName) throws IOException {
        File file=new File(directory+File.separator+fileName);

        BufferedWriter writer = null;
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                // File already exists. Abort save file or overwrite?
                System.err.printf("Directory '%s' couldn't be created.%n", file.getParentFile());
                return;
            }
            if (!file.createNewFile()) {
                // File already exists. Abort save file or overwrite?
                System.out.printf("File '%s' already exists. Overwrite (y/n)?%n", file);
                Scanner scanner=new Scanner(System.in);
                String line=scanner.nextLine().toLowerCase();
                if (!line.equals("y") && !line.equals("yes")) {
                    System.out.println("File write aborted.");
                    return;
                }
            }
            writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(rawFile);
            System.out.printf("Wrote to file: %s%n", file);
            
        } catch (IOException e) {
            System.err.println("Writing to file failed: ");
            e.printStackTrace();
            return;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
