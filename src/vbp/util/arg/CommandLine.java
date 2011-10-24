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

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class CommandLine {
    
    
    /**
     * <p>This is a very specialized function to remove certain parts from a command
     * line. For any substring that shall be removed from the command line (e.g. -i)
     * this function also removes the statement after this argument included in
     * quotation marks (e.g. "/home/folder/file.txt").</p>
     * 
     * <p>So, calling <code>removeArgFromString(input, "-i");</code> for the input string
     * <code>-f -i "/home/folder/file.txt" -g 100 ...</code> returns <code>-f  -g 100 ...</code>
     * (note that the double whitespace after -f remains preserved)</p>
     * 
     * <p>This function can cause unexpected behaviour when called on Strings that
     * don't follow the schema: <code>arg "content to this arg"</code>.</p>
     * 
     * @param input file string to remove command line args from
     * @param args command line arguments to remove from the string
     * @return same string without the args and subsequent expressions in quotation marks.
     */
    public static String removeArgFromString(String input, String... args) {
        /*
         * 2 cases:
         * - arg starts with ": remove all until next "
         * - else: remove until next whitespace
         */
        
        // TODO implement!
        
        for (String arg : args) {
            if (input.contains(arg)) {
                int argBegin = input.indexOf(arg);          // find argument
                int argEnd = nthIndexOf(input, '\"', argBegin, 2) + 1;

                StringBuilder removed = new StringBuilder(input.length());
                removed.append(input.substring(0, argBegin));
                removed.append(input.substring(argEnd));

                input = removed.toString();
            }
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
        /*
         * 2 cases:
         * - arg starts with a quotation mark: copy all until next quotation mark
         * - else: copy until next whitespace
         */
        
        if (commandLine.contains(arg)) {
            int argBegin = commandLine.indexOf(arg);          // find argument
            int argEnd = argBegin + arg.length();

            int contentBegin = argEnd;
            while (commandLine.charAt(contentBegin) == ' ') {
                contentBegin++;
            }

            int contentEnd = contentBegin;
            if (commandLine.charAt(contentBegin) == '"') {
                contentBegin++;     // skip the leading quotation mark
                contentEnd = nthIndexOf(commandLine, '\"', contentBegin, 1);
            } else {
                contentEnd = commandLine.indexOf(' ', contentBegin);
            }

            return commandLine.substring(contentBegin, contentEnd);
        }
        
        // arg not found, return null
        return null;
    }
    
}
