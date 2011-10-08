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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class implements the ICrawler interface, which describes some abstract
 * ways to search for files on a device. Detailed descriptions of the functions
 * can be found in the interface documentation.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Crawler implements ICrawler {
    
    /** Files crawled so far. */
    protected List<File> crawled = new ArrayList<File>();
    /** Folders already used as input. Can be skipped on recrawl */
    protected List<File> knownInput = new ArrayList<File>();
    
    @Override
    public List<File> crawl(List<File> input, boolean recursive) {
        for (File file : input)
            if(!knownInput.contains(file))
                knownInput.add(file);
        
        // TODO implement!
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<File> crawl(List<File> input, boolean recursive, IFileFilter filter) {
        // TODO implement!
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * most simple crawler of all...
     */
    @Override
    public List<File> crawlComplete(List<File> input, boolean recursive) {
        crawled.clear();

        // crawl
        for (File file : input) {
            if (file.isFile()) {
                crawled.add(file);
            } else if (file.isDirectory()) {    // is folder -> dig deeper
                if (recursive) {         // recursive
                    addFilesRecursively(file, crawled);
                } else {                // just one layer below
                    File[] children = file.listFiles();
                    for (File child : children) {
                        if (child.isFile()) {
                            crawled.add(child);
                        }
                    }
                }
            }
        }
        return crawled;
    }
    
    @Override
    public List<File> crawlComplete(List<File> input, boolean recursive, IFileFilter filter) {
        crawled.clear();

        // crawl
        for (File file : input) {
            applyFilter(file, filter, crawled);
            if (file.isDirectory()) {    // is folder -> dig deeper
                if (recursive) {         // recursive
                    addFilesRecursively(file, crawled, filter);
                } else {                // just one layer below
                    File[] children = file.listFiles();
                    for (File child : children) {
                        applyFilter(child, filter, crawled);
                    }
                }
            }
        }
        return crawled;
    }
    
    // ++++++++++++++ static filters +++++++++++++++
    
    /**
     * Apply a file filter on a single file. If the file-object really is a file,
     * and the file passes the filter, it will be added to the specified collection
     * and the function returns true.
     * Else (file is really a folder or filter not passed) no change will be done
     * and false is returned.
     * @param file file-object to apply filter (if applicable)
     * @param filter filter to be applied on the file
     * @param list list where the file will be added to if it passes the filter
     * @return true if the file has passed the filter, else false.
     */
    private static boolean applyFilter(File file, IFileFilter filter, Collection<File> list) {
        if (file.isFile()) {
            if (filter.filter(file)) {
                list.add(file);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Searches all files and folders recursively from the specified root file.
     * Adds all files to the specified file collection. Folders will not be added.
     * @param root file to start searching from recursively
     * @param fileList list where the found files will be written into
     */
    public static void addFilesRecursively(File root, Collection<File> fileList) {
        final File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isFile()) {
                    fileList.add(child);
                } else if (child.isDirectory()) {
                    addFilesRecursively(child, fileList);
                }
            }
        }
    }
    
    /**
     * Searches all files and folders recursively from the specified root file.
     * Adds all files that pass the specified filter to the specified file collection.
     * Folders will not be added.
     * @param root file to start searching from recursively
     * @param fileList list where the found files will be written into
     * @param filter filter settings that will be applied on every found file before adding
     */
    public static void addFilesRecursively(File root, Collection<File> fileList, IFileFilter filter) {
        final File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) {
                applyFilter(child, filter, fileList);
                if(child.isDirectory()) {
                    addFilesRecursively(child, fileList, filter);
                }
            }
        }
    }
    
}
