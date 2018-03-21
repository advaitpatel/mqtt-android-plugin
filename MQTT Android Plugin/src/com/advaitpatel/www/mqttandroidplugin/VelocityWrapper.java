/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.util.*;

/**
 * The class to use when Velocity is needed to generate a file from a Velocity template.
 * 
 * @author Zeyao Jin (Mark)
 */
public class VelocityWrapper {
    
    /**
     * Uses Velocity on the given variables and template and returns the result as a String.
     * 
     * @param vars The Velocity template variables.
     * @param templatePath The absolute path of the template file (e.g. /.../template.ext).
     * @return The result of running Velocity with the given variables on the template as a string.
     */
    public static String generate(Set<TemplateVariable> vars, String templatePath) {
        File f=new File(templatePath);
        final VelocityEngine engine=getVelocityEngine(f.getParent());
        final VelocityContext velocityContext=new VelocityContext();
        
        for (TemplateVariable var: vars) {
            velocityContext.put(var.getName(),var.getValue());
        }
        
        Template template1=engine.getTemplate(f.getName());
        StringWriter writer=new StringWriter();
        template1.merge(velocityContext,writer);
        String result=writer.toString();
        // TODO: might not be portable (Mac)
        result=result.replaceAll("\r","");
        
        return result;
    }
    
    /**
     * Returns the set of all specially formatted variables found in the template.
     * Expected format in the template (first comment block in the template):
     * #*
     * name:friendly:type:default:description
     * ...
     * name:friendly:type:default:description
     * *#
     * 
     * @param templatePath The absolute path of the template file (e.g. /.../template.ext).
     * @param overrides A Map from variable names to values of any variable defaults to override. Important: this map should be able to hold null values. Recommended Map: HashMap.
     * @return The set of all variables found by this algorithm within the given template.
     * @throws IOException Failed to open or read the template file.
     */
    public static Set<TemplateVariable> getFormattedVariables(final String templatePath, Map<String,Object> overrides) throws IOException {
        Set<TemplateVariable> variables=new TreeSet<>();
        File template=new File(templatePath);
        BufferedReader reader=new BufferedReader(new FileReader(template));
        String line;
        boolean reading=false;
        boolean read=true;
        
        // Get variables from template
        while ((line=reader.readLine()) != null) {
            if (line.contains("*#")) {
                read=false;
            }
            if (reading && read) {
                // name:friendly:type:default:description
                String[] details=line.split(":");
                variables.add(new TemplateVariable(details[0],details[1],details[2],details[3],details[4]));
            }
            if (line.contains("#*")) {
                if (read) {
                    reading=true;
                }
            }
        }
        
        if (overrides != null) {
            for (TemplateVariable variable : variables) {
                Object value = overrides.get(variable.getName());
                if (value != null) {
                    variable.setValue(value);
                }
            }
        }
        
        return variables;
    }
    
    /**
     * Initializes a {@link VelocityEngine VelocityEngine} for generating a template file.
     * 
     * @param templatesPath Absolute path of the template directory.
     * @return A {@link VelocityEngine VelocityEngine} that is ready to be used.
     */
    private static VelocityEngine getVelocityEngine(String templatesPath) {
        VelocityEngine engine=new VelocityEngine();
        Properties properties=new Properties();
        properties.put("file.resource.loader.path",templatesPath);
        properties.setProperty("runtime.log.logsystem.class","org.apache.velocity.runtime.log.NullLogSystem");
        engine.init(properties);
        return engine;
    }
}
