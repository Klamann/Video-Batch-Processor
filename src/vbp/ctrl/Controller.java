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
package vbp.ctrl;

import vbp.gui.GUI;
import vbp.model.Model;

/**
 * The Controller in the MVC-Designpattern. Not really in use right now...
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Controller {
    
    Model model;
    GUI gui;
    
    public Controller() {
        
        model = new Model();
        model.init();
        
        gui = new GUI(model);
        gui.setVisible(true);
        
    }
    
}
