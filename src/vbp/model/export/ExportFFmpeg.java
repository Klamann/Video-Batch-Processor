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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class ExportFFmpeg extends Export {

    public ExportFFmpeg(List<File> files, String commandLine, String executerPath, String renamePattern) {
        super(files, commandLine, executerPath, renamePattern);
    }

    public ExportFFmpeg(List<File> files, String commandLine, String executerPath, File outputFolder, boolean preserveFolders) {
        super(files, commandLine, executerPath, outputFolder, preserveFolders);
    }
    
    @Override
    protected String buildScriptImplementation(Map<File, String> outputMapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String buildScriptLine(String source, String destination) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String extractFileExtension() {
        
        /*
         * ffmpeg gets the file format from two possible sources:
         * 1. guessed file format by the output file extension (output is always the last entry)
         * 2. forced file format determined by the -f trigger (this overrides 1.)
         * 
         * To get the correct file extension, this method needs the unfiltered
         * ffmpeg command line. The extension will be determined only once. Afterwards
         * the command line will be cut, so it can be formatted later as
         * "ffmpeg "+INPUT+COMMANDLINE+OUTPUT
         */
        
        if(destinationFileExtension == null || destinationFileExtension.equals("")) {
            // TODO use CommandLine utility class to get the right file extension and to clean the command Line
        } else {
            return destinationFileExtension;
        }
        
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
}
