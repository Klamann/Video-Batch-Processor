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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
}
