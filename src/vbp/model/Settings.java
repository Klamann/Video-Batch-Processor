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
package vbp.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * Load from and save program configuration in settings files
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Settings {
    
    // sections
    private static String secInput = "input";
    private static String secOutput = "output";
    private static String secSearch = "search";
    private static String secEncoding = "encoding";
    private static String secTranscode = "transcode";
    
    /**
     * Writes the current program settings into a .ini file in the user home
     * directory.
     * @param model the model, where all the settings are stored
     */
    public static void writeSettings(Model model) {
        Ini ini = new Ini();
        writeCommonSettings(model, ini);
        writeToDisk(ini, getSettingsFile());
    }
    
    /**
     * Writes the current program settings and all selected file paths into a
     * .ini file in the specified directory
     * @param model the model, where all the settings are stored
     * @param projectFile the place where to save the project
     */
    public static void writeProject(Model model, File projectFile) {
        Ini ini = new Ini();
        writeCommonSettings(model, ini);
        
        Section input = ini.get(secInput);
        input.putAll("file", fileToStringList(model.inputFiles));
        
        Section transcode = ini.add(secTranscode);
        transcode.putAll("file", fileToStringList(model.filesToTranscode));
        
        writeToDisk(ini, projectFile);
    }
    
    /**
     * Write all settings that are shared between the settings.ini and the project
     * files into the given Ini
     * @param model the model, where all the settings are stored
     * @param ini the ini where the settings are written into
     * @return the modified ini
     */
    protected static Ini writeCommonSettings(Model model, Ini ini) {
        
        ini.put(secInput, "recursive", model.recursive);
        
        Section output = ini.add(secOutput);
        output.put("method", model.outputMethod.toString());
        output.put("pattern", model.renamePattern);
        if(model.outputLocation != null)
            output.put("folder",  model.outputLocation.toString());
        output.put("preserve", model.preserveFolders);
        
        Section search = ini.add(secSearch);
        search.put("method", model.searchPattern.toString());
        search.put("size", model.fileSize);
        search.put("sizeMin", model.minSize);
        search.put("sizeMax", model.maxSize);
        search.put("extension", model.fileExtension);
        search.put("extensionFilter", model.extensionFilter);
        search.put("regex", model.regex);
        
        ini.put(secEncoding, "handbrake", model.handBrakeQuery);
        
        return ini;
    }
    
    /**
     * Search for a settings file in the user home directory. If the file exists,
     * the previous configuration will be restored. If not, the default configuration
     * will be used instead.
     * @param model the model where the settings will be written into
     */
    public static void loadSettings(Model model) {
        // read .ini file
        if(getSettingsFile().canRead()) {
            try {
                Ini ini = new Ini(getSettingsFile());
                loadCommonSettings(model, ini);
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            loadDefaultSettings(model);
        }
    }
    
    /**
     * Load a project file from the specified location.
     * @param model the model where the settings will be written into
     * @param location the location where the project file is stored
     * @return true if loading was successful, false if not
     */
    public static boolean loadProject(Model model, File location) {
        if(location.canRead()) {
            try {
                Ini ini = new Ini(location);
                loadCommonSettings(model, ini);
                
                Section input = ini.get(secInput);
                String[] inputFiles = input.getAll("file", String[].class);
                model.inputFiles = stringToFileList(inputFiles);
                
                Section transcode = ini.get(secTranscode);
                String[] filesToTranscode = transcode.getAll("file", String[].class);
                model.filesToTranscode = stringToFileList(filesToTranscode);
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    /**
     * Write all settings that are shared between the settings.ini and the project
     * files from the given ini into the model.
     * @param model the model where the settings will be written into
     * @param ini the ini where the settings are stored
     */
    protected static void loadCommonSettings(Model model, Ini ini) {
        model.recursive = ini.get(secInput, "recursive", boolean.class);

        Section output = ini.get(secOutput);
        model.outputMethod = Model.OutputMethod.valueOf(output.get("method", String.class));
        model.renamePattern = output.get("pattern", String.class);
        if (output.containsKey("folder")) {
            model.outputLocation = new File(output.get("folder", String.class));
        }
        model.preserveFolders = output.get("preserve", boolean.class);

        Section search = ini.get(secSearch);
        model.searchPattern = Model.SearchPattern.valueOf(search.get("method", String.class));
        model.fileSize = search.get("size", boolean.class);
        model.minSize = search.get("sizeMin", long.class);
        model.maxSize = search.get("sizeMax", long.class);
        model.fileExtension = search.get("extension", boolean.class);
        model.extensionFilter = search.get("extensionFilter", String.class);
        model.regex = search.get("regex", String.class);

        model.handBrakeQuery = ini.get(secEncoding, "handbrake", String.class);
    }
    
    /**
     * Writes the default settings into the model. Use it if user configuration
     * is not available.
     * @param model the model where the settings will be written into
     */
    public static void loadDefaultSettings(Model model) {
        model.recursive = true;

        model.outputMethod = Model.OutputMethod.INPLACE;
        model.renamePattern = "{name}-conv";
        model.outputLocation = null;
        model.preserveFolders = false;

        model.searchPattern = Model.SearchPattern.FILE_PROPERTIES;
        model.fileSize = false;
        model.minSize = 0;
        model.maxSize = 1048576000000l;
        model.fileExtension = true;
        model.extensionFilter = "3gp|flv|mov|qt|divx|mkv|asf|wmv|avi|mpg|mpeg|mp2|mp4|m4v|rm|ogg|ogv|yuv";
        model.regex = ".*(\\.(avi|mkv|mp4))";

        model.handBrakeQuery = "-f mkv --strict-anamorphic -e x264 -q 25 -a 1 -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1";
    }
    
    /**
     * Writes a .ini into the specified file. Overwrites without asking!
     * @param ini the ini where the settings will be stored
     * @param file the designated file location
     */
    protected static void writeToDisk(Ini ini, File file) {
        try {
            ini.store(file);
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @return the settings.ini in the user home directory. This is just a reference,
     *         the file might not exist! (check with canRead())
     */
    protected static File getSettingsFile() {
        return new File(getSettingsDirectory(), "settings.ini");
    }
    
    /**
     * Returns the settings-directory (.vbp) in the user home directory and
     * creates this directory if it doesn't exist.
     * @return .vbp in user home
     */
    protected static File getSettingsDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("user.home==null");
        }
        File home = new File(userHome);
        File settingsDirectory = new File(home, ".vbp");
        if (!settingsDirectory.exists()) {
            if (!settingsDirectory.mkdir()) {
                throw new IllegalStateException(settingsDirectory.toString());
            }
        }
        return settingsDirectory;
    }

    /**
     * Turns a File list into a String list, where the string is generated with
     * <code>file.toString()</code>.
     * @param files input file list
     * @return same list with string representation of every file
     */
    protected static List<String> fileToStringList(List<File> files) {
        List<String> strings = new ArrayList<String>();
        for (File file : files) {
            strings.add(file.toString());
        }
        return strings;
    }
    
    /**
     * Turns a String-Array of file paths into a file-list.
     * @param strings input string array of file paths
     * @return file list of these file paths
     */
    protected static List<File> stringToFileList(String[] strings) {
        List<File> files = new ArrayList<File>();
        for (String string : strings) {
            files.add(new File(string));
        }
        return files;
    }
    
}
