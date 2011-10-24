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

import sebi.util.observer.Event;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public interface Saveable {
    
    /**
     * Write GUI values into the model
     */
    void updateModelValues();
    
    /**
     * Write the current model values into the GUI, overwriting possible changes
     * of the user.
     */
    void updateGuiValues();
    
    /**
     * Write GUI values into the model before exiting the program
     */
    void safeExit();
    
    /**
     * This Event will be fired, when changes to the gui require the model to be
     * updated.
     * @return the event, shall be assigned to a listener
     */
    Event eventUpdateModelValues();

}
