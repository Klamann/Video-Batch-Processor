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
package vbp.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class FileFilters {
    
    public static FileFilter handbrakeQueueFilter() {
        return new FileNameExtensionFilter("Handbrake-Queues (*.queue)", "queue");
    }
    
    public static FileFilter projectFilter() {
        return new FileNameExtensionFilter("Video Batch Processor Project (*.vbpp)", "vbpp");
    }
    
    /**
     * Makes sure that the input file has the desired file extension. If the
     * file already has the extension, it is returned unchanged. Else, the extension
     * is added to the filename and the new file object is returned.
     * @param file the input file
     * @param ext the desired file extension (without '.')
     * @return the input file with extension
     */
    public static File enforceFileExtension(File file, String ext) {
        String name = file.getName();
        
        if(name.endsWith(String.format(".%s", ext))) {
            return file;
        } else {
            String path = file.getParent();
            return new File(String.format("%s/%s.%s", path, name, ext));
        }
    }
    
    public static File enforceFileExtension(File file, FileNameExtensionFilter filter) {
        if(filter.getExtensions().length > 0) {
            return enforceFileExtension(file, filter.getExtensions()[0]);
        } else {
            return file;
        }
    }
    
}
