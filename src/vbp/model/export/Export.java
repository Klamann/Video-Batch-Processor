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
package vbp.model.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import sebi.util.threads.FutureBuilder;
import vbp.gui.FileFilters;
import vbp.model.Model.OutputMethod;

/**
 * This class is an abstract implementation of a script to generate batch files
 * or other exchangeable documents that carry the necessary information for the
 * target application to transcode all files selected by the user.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public abstract class Export {
    
    /** all files to transcode */
    protected List<File> files;
    /* the extension of the resulting script file */
    protected String fileExtension;
    /** the command line containing the software-specific transcoding settings */
    protected String commandLine;
    /** path to the software used to execute the commands */
    protected String executerPath;
    /** how the files should be saved */
    protected OutputMethod outputMethod;
    
    /** rename pattern, used when files are stored in the same folder */
    protected String renamePattern;
    
    /** folder where all transcoded files shall be saved */
    protected File outputFolder;
    /** false: put all files directly in the output folder, true: write the file path to the source file into the output folder */
    protected boolean preserveFolders;
    
    /** the file extension of the destination file - this won't be set before {@link saveScript()} or {@link buildScript()} is called for the first time! */
    protected String destinationFileExtension;
    
    /**
     * Generate a script file for a specific transcoding software.
     * All files in the transcoding list will be added to the script.
     * The resulting files will be renamed according to your specified rename pattern.
     * @param files all files to transcode
     * @param fileExtension the extension of the resulting script file
     * @param commandLine the command line containing the software-specific transcoding settings
     * @param renamePattern rename pattern, used when files are stored in the same folder
     */
    public Export(List<File> files, String fileExtension, String commandLine, String renamePattern) {
        this.files = files;
        this.fileExtension = fileExtension;
        this.commandLine = commandLine;
        
        this.outputMethod = OutputMethod.INPLACE;
        this.renamePattern = renamePattern;
    }
    
    /**
     * Generate a script file for a specific transcoding software.
     * All files in the transcoding list will be added to the script.
     * The resulting files will be renamed according to your specified rename pattern.
     * @param files all files to transcode
     * @param fileExtension the extension of the resulting script file
     * @param commandLine the command line containing the software-specific transcoding settings
     * @param executerPath path to the software used to execute the commands
     * @param renamePattern rename pattern, used when files are stored in the same folder
     */
    public Export(List<File> files, String fileExtension, String commandLine, String executerPath, String renamePattern) {
        this.files = files;
        this.fileExtension = fileExtension;
        this.commandLine = commandLine;
        this.executerPath = executerPath;
        
        this.outputMethod = OutputMethod.INPLACE;
        this.renamePattern = renamePattern;
    }
    
    /**
     * Generate a script file for a specific transcoding software.
     * All files in the transcoding list will be added to the script.
     * The resulting files will placed in the specified output folder, optionally
     * preserving the folder structure.
     * @param files all files to transcode
     * @param fileExtension the extension of the resulting script file
     * @param commandLine the command line containing the software-specific transcoding settings
     * @param outputFolder folder where all transcoded files shall be saved
     * @param preserveFolders false: put all files directly in the output folder, true: write the file path to the source file into the output folder
     */
    public Export(List<File> files, String fileExtension, String commandLine, File outputFolder, boolean preserveFolders) {
        this.files = files;
        this.fileExtension = fileExtension;
        this.commandLine = commandLine;
        
        this.outputMethod = OutputMethod.SPECIFIC_FOLDER;
        this.outputFolder = outputFolder;
        this.preserveFolders = preserveFolders;
    }
    
    /**
     * Generate a script file for a specific transcoding software.
     * All files in the transcoding list will be added to the script.
     * The resulting files will placed in the specified output folder, optionally
     * preserving the folder structure.
     * @param files all files to transcode
     * @param fileExtension the extension of the resulting script file
     * @param commandLine the command line containing the software-specific transcoding settings
     * @param executerPath path to the software used to execute the commands
     * @param outputFolder folder where all transcoded files shall be saved
     * @param preserveFolders false: put all files directly in the output folder, true: write the file path to the source file into the output folder
     */
    public Export(List<File> files, String fileExtension, String commandLine, String executerPath, File outputFolder, boolean preserveFolders) {
        this.files = files;
        this.fileExtension = fileExtension;
        this.commandLine = commandLine;
        this.executerPath = executerPath;
        
        this.outputMethod = OutputMethod.SPECIFIC_FOLDER;
        this.outputFolder = outputFolder;
        this.preserveFolders = preserveFolders;
    }
    
    /**
     * This function acts as a wrapper for buildScript(). It generates the script-
     * File and promts the user with a saving-dialogue, so the file can be written
     * to the desired location. Puts the correct file extension on the saved script
     * by default.
     * @param fileChooser the jFileChooser to popup
     */
    public void saveScript(JFileChooser fileChooser) {
        saveScript(fileChooser, true);
    }
    
    /**
     * This function acts as a wrapper for buildScript(). It generates the script-
     * File and promts the user with a saving-dialogue, so the file can be written
     * to the desired location.
     * @param fileChooser the jFileChooser to popup
     * @param enforceExtension force the output file to have the correct file extension
     */
    public void saveScript(JFileChooser fileChooser, boolean enforceExtension) {
        
        Future<String> queue = new FutureBuilder<String>() {
            @Override
            public String build() {
                return buildScript();
            }
        }.getFuture();
        
        // open dialogue and write file as soon as export finishes
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                Writer fileWriter = null;
                try {
                    File output = fileChooser.getSelectedFile();
                    if(enforceExtension) {
                        output = FileFilters.enforceFileExtension(output, fileExtension);
                    }
                    fileWriter = new FileWriter(output);
                    fileWriter.write(queue.get());
                } catch (IOException ex) {
                    Logger.getLogger(Export.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Export.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Export.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Export.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            queue.cancel(true);
        }
    }
    
    /**
     * Generates a script file according to the settings used when this class has
     * been initialized.
     * @return the full script as String
     */
    public String buildScript() {
        this.destinationFileExtension = extractFileExtension();
        
        Map<File,String> outputMapping = new HashMap<File, String>(files.size());
        switch (outputMethod) {
            case INPLACE:
                for (File input : files) {
                    outputMapping.put(input, applyRenamePattern(input));
                }
                break;
            case SPECIFIC_FOLDER:
                for (File input : files) {
                    outputMapping.put(input, generateOutputFile(input));
                }
                break;
        }
        return buildScriptImplementation(outputMapping);
    }
    
    /**
     * Implementation of the build script, independent of the output method.
     * @param outputMapping a File -> String map, that contains a key for every
     *        input file that shall be transcoded. Each key refers to the corresponding
     *        canonical output file path.
     * @return the full script as String
     */
    protected abstract String buildScriptImplementation(Map<File,String> outputMapping);
    
    // ++++++++++ Generate Output Files ++++++++++
    
    /**
     * Applies the user's rename pattern on the source file, to generate the correct
     * output file name. It reflects the correct file extension from the command
     * line.
     * @param source the source file path
     * @return the destination file path, renamed and with correct extension.
     */
    protected String applyRenamePattern(File source) {
        
        // gather properties
        String fullName = removeExtension(source.getName());
        String path = source.getParent();
        
        // replace generics (e.g. {name} )
        String name = renamePattern.replaceAll("\\{name\\}", fullName);
        
        return composeOutputFile(path, name);
    }
    
    /**
     * Generates the location of the output file, when using a specific folder for
     * the output, according to the settings when the class was initialized.
     * @param input the input file
     * @return the destination file path, with correct file extension.
     */
    protected String generateOutputFile(File input) {
        
        String fileName = removeExtension(input.getName());
        String path = outputFolder.getPath();
        
        if (preserveFolders) {
            String pathParent = path;
            String pathChild = input.getParent().replaceAll(":", "");
            File pathBoth = new File(String.format("%s/%s", pathParent, pathChild));
            path = pathBoth.getPath();
        }
        
        return composeOutputFile(path, fileName);
    }
    
    // ++++++++++ Helpers ++++++++++
    
    /**
     * Generates a single command line for the given input and output file.
     * (This is a helper method that does not need to be implemented)
     * @param source the canonical path to the source file
     * @param destination the canonical path to the destination file
     * @return the full handbrake query
     */
    protected abstract String buildScriptLine(String source, String destination);
    
    /**
     * Composes the correct output String from the filepath, the filename and the
     * file extension according to the transcoding command.
     * @param path absolute path to your destonation file's parent folder
     * @param name name of your destination file (without extension)
     * @return the complete file path (canonical, as string)
     */
    protected String composeOutputFile(String path, String name) {
        // add correct file extension
        File result = new File(String.format("%s/%s.%s", path, name, destinationFileExtension));
        
        // generate canonical path
        try {
            return result.getCanonicalPath();
        } catch (IOException ex) {
            // backup: just put strings together
            return String.format("%s%s.%s", path, name, destinationFileExtension);
        }
    }
    
    /**
     * Define the file extension for the given command line. This extension will
     * be used for all transcoded files instead of the original file extension
     * (if there was any)
     * @return file extension for the files to transcode
     */
    protected abstract String extractFileExtension();
    
    /**
     * removes the file extension from a file name, if the name has an extension.
     * If not the unchanged filename will be returned
     * @param fileName name of the file as String (use File.getName() to get it)
     * @return filename without extension
     */
    protected static String removeExtension(String fileName) {
        if (fileName.contains(".")) {
            // remove file extension
            int extIndex = fileName.lastIndexOf('.');
            return fileName.substring(0, extIndex);
        }
        // file has no extension -> return unchanged
        return fileName;
    }
    
    
}
