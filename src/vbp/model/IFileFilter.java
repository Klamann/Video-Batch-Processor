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

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public interface IFileFilter {
    
    /**
     * decides if the given file is in accordance with the current filter settings.
     * @param file File to check
     * @return true if the file matches the filter
     */
    public boolean filter(File file);
    
    /**
     * updates the filter settings
     */
    public void update();
    
}
