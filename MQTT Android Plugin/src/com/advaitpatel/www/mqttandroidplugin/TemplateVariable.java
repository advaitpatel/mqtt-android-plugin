/*
 * Copyright (c) 2015-2017. All information contained herein is subject to the terms and conditions
 * defined in file 'MACHFU_LICENSE.txt', which is part of this source code package.
 *
 */

package com.advaitpatel.www.mqttandroidplugin;

public class TemplateVariable implements Comparable<TemplateVariable> {
    
    @Override
    public int compareTo(TemplateVariable o) {
        return this.getName().compareTo(o.getName());
    }
    
    public static enum types {
        text,bool,choice,integer,real
    }
    
    private String name;
    private String friendly;
    private types type;
    private Object value;
    private String description;
    private long flags;
    
    public TemplateVariable(String name, String friendly, String type, String def, String description) {
        this.name=name;
        this.friendly=friendly;
        this.type=resolveType(type);
        this.value=resolveValue(this.type,def);
        this.description=description;
        flags=0;
    }
    
    /**
     * Returns the type where its name is the same as the input String.
     * 
     * @param type A String of a type name.
     * @return The type of the same name.
     */
    private static types resolveType(String type) {
        for (types aType: types.values()) {
            if (aType.toString().equals(type)) {
                return aType;
            }
        }
        throw new RuntimeException("TemplateLib: Failed to resolve variable type from template: "+type);
    }
    
    /**
     * Converts the String into an appropriate Object depending on the type.
     * 
     * @param type The type of the input.
     * @param input The input in String form.
     * @return The input, converted into an Object based on the type.
     */
    public static Object resolveValue(types type, String input) {
        Object value=null;
        switch (type) {
            case text: {
                value=input;
                break;
            }
            case bool: {
                value=Boolean.parseBoolean(input);
                break;
            }
            case choice: { // Subject to change.
                value=Long.parseLong(input);
                break;
            }
            case integer: {
                value=Long.parseLong(input);
                break;
            }
            case real: {
                value=Double.parseDouble(input);
                break;
            }
        }
        return value;
    }
    
    /**
     * Override for {@link Object#toString() toString()}.
     * Does not end with a newline.
     * 
     * @return See {@link Object#toString() toString()}.
     */
    @Override
    public String toString() {
        if (value != null) {
            return "Name: " + name
                    + "\nFriendly name: " + friendly
                    + "\nType: " + type.toString()
                    + "\nValue: " + value.toString()
                    + "\nDescription: " + description;
        }
        return "Name: " + name
                + "\nFriendly name: " + friendly
                + "\nType: " + type.toString()
                + "\nValue: null"
                + "\nDescription: " + description;
    }
    
    /**
     * @return The Velocity variable name as it is in the template file.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the user-friendly version of the Velocity variable name meant for use in the UI, 
     * obtained from the template.
     * 
     * @return The user-friendly name.
     */
    public String getFriendlyName() {
        return friendly;
    }
    
    /**
     * @return The type of the variable, an enum within {@link com.machfu.plugin.util.TemplateVariable.types types}.
     */
    public types getType() {
        return type;
    }
    
    /**
     * @return The value of the Velocity variable that this class represents.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * @param value A new value for the Velocity variable that this class represents.
     */
    public void setValue(Object value) {
        this.value=value;
    }
    
    /**
     * @return Any description associated with the variable. This is intended for users to see.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Not currently used. Possibly useful in the future.
     * 
     * @return "flags" associated with the variable.
     */
    public long getFlags() {
        return flags;
    }
}
