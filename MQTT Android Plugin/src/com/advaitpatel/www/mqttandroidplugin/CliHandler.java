/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

import org.apache.commons.cli.*;
import java.util.HashMap;
import java.util.Map;

/**
 * CliHandler class. A CliHandler object is used to parse arguments given to 
 * a main method (or generally, any String[]).
 *
 * @author Zeyao Jin (Mark)
 */
public class CliHandler {
    
    private String[] args;
    private Options options;
    protected Map<String,String> vars;
    
    /**
     * Constructor. Any non-Velocity specific parameters should be added here.
     * 
     * @param args String[] args from a Java main method.
     */
    public CliHandler(String[] args) {
        this.args=args;
        options=new Options();
        vars=new HashMap<>();
        options.addOption("h", "help",false,"Help");
        options.addOption("n", "name", true, "File name to generate");
        options.addOption("d", "dest", true, "Destination directory");
        options.addOption("t", "template", true, "Absolute path of the template to use");
    }

    public void parse() {
        CommandLineParser parser=new DefaultParser();
        CommandLine cmd=null;
        try {
            cmd=parser.parse(options,args);
            if (cmd.hasOption("h")) {
                help();
            }
            if (cmd.hasOption("d")) {
                vars.put("dest", cmd.getOptionValue("d"));
            }
            if (cmd.hasOption("n")) {
                vars.put("name", cmd.getOptionValue("n"));
            }
            if (cmd.hasOption("t")) {
                vars.put("template", cmd.getOptionValue("t"));
            }
        }
        catch (Exception e) {
            System.out.println("Bad usage");
            help();
        }
    }
    
    /**
     * Prepares and outputs a formatted help message.
     */
    private void help() {
        HelpFormatter helpFormatter=new HelpFormatter();
        helpFormatter.printHelp("Main",options);
    }
}
