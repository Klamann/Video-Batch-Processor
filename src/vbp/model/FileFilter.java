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

import vbp.model.Model.SearchPattern;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class implements a file filter that can be applied on any file to decide
 * if it meets certain expectations (as size, file name, file extension, etc.)
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class FileFilter implements IFileFilter {
    
    // model - update filter settings from here
    private Model model;
    
    // settings inherited from model
    protected SearchPattern searchPattern;
    protected boolean fileSize;
    protected boolean fileExtension;
    protected long minSize;
    protected long maxSize;
    protected String extensionFilter;
    protected String userRegex;
    
    // compiled filter settings
    protected boolean applyRegex;
    protected Pattern regex;
    
    /**
     * creates an empty file filter. Any file can pass.
     */
    public FileFilter() {
        this.fileExtension = false;
        this.fileSize = false;
        this.searchPattern = SearchPattern.FILE_PROPERTIES;
    }
    
    /**
     * Creates a file filter using the settings from the model. Call update()
     * before applying the filter!
     * @param model model where the filter settings are stored
     */
    public FileFilter(Model model) {
        this.model = model;
    }

    @Override
    public boolean filter(File file) {
        switch (searchPattern) {
            case FILE_PROPERTIES:
                if (fileSize) {
                    long length = file.length();
                    if (length < minSize) {
                        return false;
                    } else if (length > maxSize) {
                        return false;
                    }
                }
                if (applyRegex) {
                    if (!applyRegex(file, regex)) {
                        return false;
                    }
                }
                break;
            case REGEX:
                if (!applyRegex(file, regex)) {
                    return false;
                }
        }
        // nothing returned false so far? then it works :D
        return true;
    }
    
    /**
     * Applys a regex on the canocial path of a file. Returns true if the regex
     * matches, else false (in case of not matching or IO exceptions).
     * @param file regex will be applied on the canocial path of this file
     * @param regex pre-compiled java-regex
     * @return true if regex matches, else false
     */
    protected static boolean applyRegex(File file, Pattern regex) {
        try {   // if regex matches -> win
            if (regex.matcher(file.getCanonicalPath()).matches()) {
                return true;
            }
        } catch (IOException ex) {     // if file path can't be read -> fail
            Logger.getLogger(FileFilter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return false;       // regex did'nt match
    }

    @Override
    public void update() {
        
        // reset values
        this.applyRegex = false;
        
        // update fields
        this.fileSize = model.fileSize;
        this.fileExtension = model.fileExtension;
        this.minSize = model.minSize;
        this.maxSize = model.maxSize;
        this.extensionFilter = model.extensionFilter;
        this.userRegex = model.regex;
        this.searchPattern = model.searchPattern;
        
        switch(searchPattern) {
            case FILE_PROPERTIES:
                if (fileExtension /* or other user regex stuff be here! */) {
                    applyRegex = true;

                    // regex contents
                    String extensionRegex = ".*(\\.(" + extensionFilter + "))";
                    // TODO file names

                    this.regex = Pattern.compile(extensionRegex, Pattern.CASE_INSENSITIVE);
                }
                break;
            case REGEX:
                this.regex = Pattern.compile(userRegex, Pattern.CASE_INSENSITIVE);
        }
    }
    
    /**
     * static method to create an empty file filter
     * @return an empty file filter. Any file will pass.
     */
    public static FileFilter empty() {
        return new FileFilter();
    }
    
    /**
     * Static Method that returns a fully working file filter. All values are
     * initialized.
     * @param model the model to get filter settings from
     * @return working file filter according to settings in the model.
     */
    public static FileFilter initialize(Model model) {
        FileFilter filter = new FileFilter(model);
        filter.update();
        return filter;
    }
    
}
