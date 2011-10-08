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
import java.util.List;

/**
 * This Interface describes a set of tools that can be used to search for files
 * on a device, optionally using a file filter.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public interface ICrawler {
    
    /**
     * Searches for all files that haven't been crawled for before (incremental crawling).
     * Uses no filters, so all files will be listed.
     * @param input search locations
     * @param recursive folders will be crawled recursively (to the bottom) if set to true
     * @return list of all crawled files
     */
    public List<File> crawl(List<File> input, boolean recursive);
    
    /**
     * Searches for all files that haven't been crawled for before (incremental crawling).
     * Applies a filter on every file before adding it to the return list.
     * @param input search locations
     * @param recursive folders will be crawled recursively (to the bottom) if set to true
     * @param filter file filter that decides which file comes on the list
     * @return list of all crawled files that have passed the filter
     */
    public List<File> crawl(List<File> input, boolean recursive, IFileFilter filter);
    
    /**
     * Searches for all files no matter if they have been crawled for before (complete crawling).
     * Uses no filters, so all files will be listed.
     * @param input search locations
     * @param recursive folders will be crawled recursively (to the bottom) if set to true
     * @return list of all crawled files
     */
    public List<File> crawlComplete(List<File> input, boolean recursive);
    
    /**
     * Searches for all files no matter if they have been crawled for before (complete crawling).
     * Applies a filter on every file before adding it to the return list.
     * @param input search locations
     * @param recursive folders will be crawled recursively (to the bottom) if set to true
     * @param filter file filter that decides which file comes on the list
     * @return list of all crawled files that have passed the filter
     */
    public List<File> crawlComplete(List<File> input, boolean recursive, IFileFilter filter);
    
}
