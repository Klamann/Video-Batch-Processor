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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vbp.util.arg.CLParser;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class ExportFFmpeg extends Export {
    
    /** This command line does not contain the "ffmpeg" and no information about
     input and output files. */
    protected String reducedCommandLine;

    public ExportFFmpeg(List<File> files, String commandLine, String executerPath, String renamePattern) {
        super(files, "bat", commandLine, executerPath, renamePattern);
    }

    public ExportFFmpeg(List<File> files, String commandLine, String executerPath, File outputFolder, boolean preserveFolders) {
        super(files, "bat", commandLine, executerPath, outputFolder, preserveFolders);
    }
    
    @Override
    protected String buildScriptImplementation(Map<File, String> outputMapping) {
        
        /*
         * TODO improve cli, linux sh script, connect to gui
         */
        
        this.scriptFileExtension = "bat";
        this.reducedCommandLine = reduceCommandLine(commandLine);
        StringBuilder strb = new StringBuilder();
        
        for (File file : files) {
            try {
                String source = file.getCanonicalPath();
                String destination = outputMapping.get(file);
                strb.append(buildScriptLine(source, destination));
            } catch (IOException ex) {
                Logger.getLogger(ExportFFmpeg.class.getName()).log(Level.SEVERE, null, ex);
            }
                
        }
        
        return strb.toString();
    }

    @Override
    protected String buildScriptLine(String source, String destination) {
        if(reducedCommandLine != null) {
            return String.format("%s -i \"%s\" %s \"%s\"\n", executerPath, source, reducedCommandLine, destination);
        } else {
            return String.format("%s -i \"%s\" \"%s\"\n", executerPath, source, destination);
        }
        
    }

    @Override
    protected String extractFileExtension() {
        
        /*
         * ffmpeg gets the file format from two possible sources:
         * 1. guessed file format by the output file extension (output is always the last entry)
         * 2. forced file format determined by the -f trigger
         * 
         * This funcion will use the file extension of the given output file name
         * if available, or use the forced file format name as extension (though
         * this can look ugly...)
         * 
         * Therefore, to get the correct file extension, this method needs the unfiltered
         * ffmpeg command line. The extension will be determined only once. Afterwards
         * the command line will be cut, so it can be formatted later as
         * "ffmpeg "+INPUT+COMMANDLINE+OUTPUT
         */
        
        if(destinationFileExtension == null) {
            CLParser parser = new CLParser(commandLine);
            String destinationFileName = parser.getLastArg();
            
            if(isFile(destinationFileName)) {
                int extIndex = destinationFileName.lastIndexOf('.') + 1;
                destinationFileExtension = destinationFileName.substring(extIndex);
            } else if (parser.exists("-f")) {
                destinationFileExtension = parser.getSubsequentArg("-f");
            } else {
                destinationFileExtension = "avi";
            }
        }
        
        return destinationFileExtension;
    }
    
    // -------------- helpers --------------
    
    protected static String reduceCommandLine(String commandLine) {
        
        CLParser clp = new CLParser(commandLine);
        
        if(clp.getFirstArg().toLowerCase().equals("ffmpeg")) {
            clp.removeFirstArg();
        }
        clp.removeSubsequentArg("-i");
        if(isFile(clp.getLastArg())) {
            clp.removeLastArg();
        }
        
        return null;
    }
    
    /**
     * Guesses if a String could represent a file name. Not very accurate...
     * @param name
     * @return 
     */
    protected static boolean isFile(String name) {
        if(name.contains(".") && name.charAt(0) != '-') {
            return true;
        }
        return false;
    }
}
