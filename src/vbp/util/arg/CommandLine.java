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

import sebi.util.data.Tuple;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@Deprecated
public class CommandLine {
    
    
    /**
     * <p>This is a very specialized function to remove certain parts from a command
     * line. For any substring that shall be removed from the command line (e.g. -i)
     * this function also removes the statement after this argument included in
     * quotation marks (e.g. "/home/folder/file.txt") - from now on referred as
     * "value".</p>
     * 
     * <p>So, calling <code>removeArgFromString(input, "-i");</code> for the input string
     * <code>-f -i "/home/folder/file.txt" -g 100 ...</code> returns <code>-f -g 100 ...</code></p>
     * 
     * <p>This function can cause unexpected behaviour when called on Strings that
     * don't follow the schema: <code>-arg value</code> (value may be with or
     * without quotation marks).</p>
     * 
     * @param input file string to remove command line args from
     * @param args command line arguments to remove from the string
     * @return same string without the args and subsequent expressions
     */
    public static String removeArgFromString(String input, String... args) {
        
        for (String arg : args) {
            ArgBounds argb = new ArgBounds(input, arg);
            
            StringBuilder removed = new StringBuilder(input.length());
            removed.append(input.substring(0, argb.getArgBegin()));
            removed.append(input.substring(argb.getExpressionEnd()));
            
            input = removed.toString();
        }
        
        return input;
    }
    
    /**
     * Returns the nth appearance of a specified char inside a String (not including
     * the starting character). Good if you want to find the position of matching
     * characters like quotation marks or brackets.
     * @param input String to search in
     * @param match Character to find
     * @param fromIndex Index position to start the search
     * @param nth which occurance to return
     * @return index of the nth appearance of the specified char
     */
    public static int nthIndexOf(String input, char match, int fromIndex, int nth) {
        
        int tmpIndex = fromIndex;
        for (int i = 0; i < nth; i++) {
            tmpIndex = input.indexOf(match, tmpIndex+1);
        }
        
        return tmpIndex;
    }
    
    /**
     * Returns the value of a command line argument, e.g.
     * <code>app -arg value ...</code> returns <code>value</code> when your search for <code>-arg</code>
     * <code>app -arg "value with whitespace" ...</code> returns <code>value with whitespace</code> when your search for <code>-arg</code>
     * @param commandLine the command line to search in
     * @param arg the argument, of which the value shall be returned
     * @return the value of the argument, if existent, or null
     */
    public static String getArgValue(String commandLine, String arg) {
        ArgBounds argb = new ArgBounds(commandLine, arg);
        if (argb.argExists()) {
            return commandLine.substring(argb.getContentBegin(), argb.getContentEnd());
        } else {
            return null;    // arg not found, return null
        }
        
    }
    
    public static String getLastArg(String commandLine) {
        
        int end = commandLine.length()-1;
        while(commandLine.charAt(end) == ' ') {
            end--;
        }
        
        int start = end-1;
        if(commandLine.charAt(end) == '"') {
            end--;  // exclude last quotation mark
            while(commandLine.charAt(start) != '"') {
                start--;
            }
        } else {
            while(commandLine.charAt(start) != ' ') {
                start--;
            }
        }
        
        return commandLine.substring(start+1, end+1);
    }
    
//    public static String removeLastArg(String commandLine) {
//        
//    }
    
    protected static Tuple<Integer,Integer> getLastArgBounds(String commandLine) {
        
        // fail, cant use this function to get the boundries for copying AND removing
        
        int end = commandLine.length()-1;
        while(commandLine.charAt(end) == ' ') {
            end--;
        }
        
        int start = end-1;
        if(commandLine.charAt(end) == '"') {
            end--;  // exclude last quotation mark
            while(commandLine.charAt(start) != '"') {
                start--;
            }
        } else {
            while(commandLine.charAt(start) != ' ') {
                start--;
            }
        }
        start++;    // set to start after finding the border.
        
        return Tuple.of(start, end);
    }
    
    /**
     * ArgBounds is a little static helper class to unify the functionality
     * of the basic command line parser the CommandLine utility class implements.
     * It is used to get the position of command line arguments and following
     * values in a String.
     */
    protected static class ArgBounds {

        private String arg;
        
        /** does the command contain this arg? */
        private boolean exists;
        /** starting position of the arg */
        private int argBegin;
        /** end position of the arg (not the following value) */
        private int argEnd;
        /** starting position of the arg's value (without quotation marks, if any) */
        private int contentBegin;
        /** end position of the arg's value (without quotation marks, if any) */
        private int contentEnd;
        /** final position of the expression (arg, value and following whitespaces) */
        private int expressionEnd;
        
        
        public ArgBounds(String commandLine, String arg) {
            /*
             * 2 cases:
             * - arg starts with a quotation mark: scan until next quotation mark
             * - else: scan until next whitespace
             */

            if (commandLine.contains(arg)) {
                exists = true;
                
                argBegin = commandLine.indexOf(arg);          // find argument
                argEnd = argBegin + arg.length();

                contentBegin = argEnd;
                while (commandLine.charAt(contentBegin) == ' ') {
                    contentBegin++;
                }

                contentEnd = contentBegin;
                if (commandLine.charAt(contentBegin) == '"') {
                    contentBegin++;     // skip the leading quotation mark
                    contentEnd = nthIndexOf(commandLine, '\"', contentBegin, 1);
                    expressionEnd = contentEnd+1;
                } else {
                    contentEnd = commandLine.indexOf(' ', contentBegin);
                    expressionEnd = contentEnd;
                }
                
                while(commandLine.charAt(expressionEnd) == ' ') {
                    expressionEnd++;
                }
            } else {
                exists = false;
            }
        }
        
        public boolean argExists() {
            return exists;
        }
        
        public int getArgBegin() {
            return argBegin;
        }

        public int getArgEnd() {
            return argEnd;
        }

        public int getContentBegin() {
            return contentBegin;
        }

        public int getContentEnd() {
            return contentEnd;
        }
        
        public int getExpressionEnd() {
            return expressionEnd;
        }
    }
    
}
