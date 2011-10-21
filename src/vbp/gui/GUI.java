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

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import vbp.model.Model;

/**
 * This class serves as the common basis for all application windows.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class GUI {
    
    protected Model model;
    protected GUI gui;
    
    protected WindowMain windowMain;
    protected WindowExportFFmpeg windowFFmpeg;
    
    /**
     * Constructor of the GUI. Don't forget to call init() for the gui to become
     * visible.
     * @param model 
     */
    public GUI(Model model) {
        this.model = model;
        setLookAndFeel();
    }
    
    /**
     * Initializes the main window and sets it visible
     */
    public void init() {
        this.gui = this;
        initMainWindow();
    }
    
    /**
     * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    private void setLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    private void initMainWindow() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                windowMain = new WindowMain(model, gui);
                windowMain.setVisible(true);
            }
        });
    }
    
    protected void popupWindowFFmpeg() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                windowFFmpeg = new WindowExportFFmpeg(windowMain);
                windowFFmpeg.setVisible(true);
            }
        });
    }
    
    /**
     * @return the icon image for the application
     */
    protected Image getIcon() {
        URL imageURL = getClass().getResource("/vbp/assets/icons/video.png");
        return new ImageIcon(imageURL).getImage();
    }

}
