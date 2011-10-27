/*
 * Copyright (C) 2011 Sebastian Straub <sebastian-straub@gmx.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vbp.util.arg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import vbp.util.data.StringIterator;

/**
 * This class implements a basic command line parser. No CLI-Interaction possible,
 * just String manipulation.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class CLParser {
    
    /** List of all Arguments in this command line. The first arg can be the program to be called */
    protected List<Arg> args = new ArrayList<Arg>();
    
    /**
     * Initializes the CLIParser and parses the commandLine given as an argument.
     * @param commandLine the input command line
     */
    public CLParser(String commandLine) {

        Iterator<Character> it = new StringIterator(commandLine).iterator();
        boolean leave;

        while (it.hasNext()) {
            leave = false;
            char c = it.next();
            if (c == ' ') {
                // skip
            } else {
                Arg arg = new Arg();
                args.add(arg);
                StringBuilder str = new StringBuilder();
                str.append(c);

                if (c == '"') {
                    arg.surrounded = true;
                    while (it.hasNext() && leave == false) {
                        char c2 = it.next();
                        str.append(c2);
                        if (c2 == '"') {
                            leave = true;
                        }
                    }

                } else {
                    while (it.hasNext() && leave == false) {
                        char c2 = it.next();
                        if (c2 == ' ') {
                            leave = true;
                        } else {
                            str.append(c2);
                        }
                    }
                }

                arg.value = str.toString();
            }
        }
    }
    
    // <editor-fold desc="retreive">
    
    /**
     * 
     * @return the first argument of the command line (may be the program name)
     *         or null if input was empty
     */
    public String getFirstArg() {
        if(args.size() > 0) {
            return args.get(0).getValue();
        }
        return null;
    }
    
    /**
     * 
     * @return the last argument of the command line or null if no arguments exist
     */
    public String getLastArg() {
        if(args.size() > 0) {
            return args.get(args.size()-1).getValue();
        }
        return null;
    }
    
    /**
     * 
     * @param n index of the argument to retrieve
     * @return the String representation of the argument, or null if nonexistent
     */
    public String getNthArg(int n) {
        if(n < args.size() && n >= 0) {
            return args.get(n).getValue();
        } else {
            return null;
        }
    }
    
    /**
     * Returns the content of the argument that directly follows the given argument.
     * @param preceding the preceding argument
     * @return the subsequent argument or null if nonexistent
     */
    public String getSubsequentArg(String preceding) {
        int index = getArgIndex(preceding) + 1;
        if(index < args.size()) {
            return args.get(index).getValue();
        }
        return null;
    }
    
    // </editor-fold>
    
    // <editor-fold desc="print">
    
    /**
     * Returns a cleaned String representation of the current command line.
     * @return command line as String
     */
    public String printString() {
        StringBuilder strb = new StringBuilder();
        
        for (Arg arg : args) {
            strb.append(arg.value);
            strb.append(' ');
        }
        strb.deleteCharAt(strb.length()-1);
        
        return strb.toString();
    }
    
    /**
     * Returns a cleaned String representation of the current command line.
     * @return command line as String
     */
    @Override
    public String toString() {
        return printString();
    }
    
    /**
     * Cleans the command line. Removes leading and trailing whitespaces, as
     * well as duplicate whitespaces between arguments.
     * @param commandLine the input command line
     * @return the cleaned command line
     */
    public static String clean(String commandLine) {
        CLParser clp = CLParser.parse(commandLine);
        return clp.printString();
    }
    
    // </editor-fold>
    
    // <editor-fold desc="remove">
    
    /**
     * Removes the first appearance of this argument.
     * @param arg the argument to remove
     * @return true on success, false means usually the argument does not exist
     */
    public boolean removeArg(String arg) {
        if(exists(arg)) {
            args.remove(getArgIndex(arg));
            return true;
        } else {
            return false;   // arg not found
        }
    }
    
    /**
     * Removes the arg given to this method and it's subsequent arg, if existent
     * @param arg argument to remove
     * @return true on success, false means at least one of them was not found and
     *         therefore not removed
     */
    public boolean removeSubsequentArg(String arg) {
        if(exists(arg)) {
            int index = getArgIndex(arg);
            args.remove(index);     // remove arg
            if(index < args.size()) {
                args.remove(index);     // remove subsequent
                return true;
            } else {
                return false;   // no subsequent arg exists
            }
        } else {
            return false;   // arg not found
        }
    }
    
    /**
     * Removes the Nth argument, begin counting on 0
     * @param n the argument to remove
     * @return true on success, false means index out of bounds
     */
    public boolean removeNthArg(int n) {
        if(n < args.size() && n >= 0) {
            args.remove(n);
            return true;
        } else {
            return false;   // out of bounds
        }
    }
    
    /**
     * Removes the first argument in the command line (this can be the program name)
     * @return true on success, false means no args left.
     */
    public boolean removeFirstArg() {
        if(!args.isEmpty()) {
            args.remove(0);
            return true;
        } else {
            return false;   // empty
        }
    }
    
    /**
     * Removes the last argument in the command line
     * @return true on success, false means no args left.
     */
    public boolean removeLastArg() {
        if(!args.isEmpty()) {
            args.remove(args.size()-1);
            return true;
        } else {
            return false;   // empty
        }
    }
    
    // </editor-fold>
    
    // <editor-fold desc="helpers">
    
    /**
     * Checks if the command line contains this arg
     * @param arg the arg to examine
     * @return true if the arg is part of the command line, false if not
     *         (only exact fit, whitespaces will not be ignored)
     */
    public boolean exists(String arg) {
        for (Arg argo : args) {
            if(arg.equals(argo.getValue()) || arg.equals(argo.value)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     * @param arg the arg of which the index number shall be retreived.
     * @return the index of the arg in the list of arguments
     */
    public int getArgIndex(String arg) {
        for (int i=0; i<args.size(); i++) {
            if(arg.equals(args.get(i).getValue()) || arg.equals(args.get(i).value)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Creates a new CLParser. This static method just calls the Constructor.
     * No magic here, but can be useful sometimes...
     * @param commandLine the commandLine to parse
     * @return a freshly initialized CLParser instance
     */
    public static CLParser parse(String commandLine) {
        return new CLParser(commandLine);
    }
    
    // </editor-fold>
    
    // <editor-fold desc="Inner Classes">
    
    protected class Arg {
        
        protected boolean surrounded = false;
        protected String value;
        
        public String getValue() {
            if(surrounded) {
                return value.substring(1, value.length()-1);
            } else {
                return value;
            }
        }
        
    }
    
    // </editor-fold>
    
}
