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

import sebi.util.system.Platform;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import vbp.model.Model;
import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class ExportHandbrakeTest {
    
    public ExportHandbrakeTest() {
        this.inputFiles = new ArrayList<File>();
        if(Platform.isWindows()) {
            inputFiles.add(new File("C:\\Movies\\film1.avi"));
            inputFiles.add(new File("C:\\Movies\\film2.avi"));
            inputFiles.add(new File("C:\\Movies\\film3.mkv"));
            inputFiles.add(new File("C:\\Movies\\film4 test.mov"));
            inputFiles.add(new File("C:\\Movies\\film5.divx"));
        } else {
            inputFiles.add(new File("/home/movies/film1.avi"));
            inputFiles.add(new File("/home/movies/film2.avi"));
            inputFiles.add(new File("/home/movies/film3.mkv"));
            inputFiles.add(new File("/home/movies/film4 test.mov"));
            inputFiles.add(new File("/home/movies/film5.divx"));
        }
    }
    
    @Before
    public void setUp() {
        this.handbrake = new ExportHandbrake(inputFiles, handBrakeQuery, renamePattern);
    }
    
    public void setUpVariant() {
        if (Platform.isWindows()) {
            outputFolder = new File("C:\\export");
        } else {
            outputFolder = new File("/home/export");
        }
        this.handbrake = new ExportHandbrake(inputFiles, handBrakeQuery, outputFolder, preserveFolders);
    }
    
    private Export handbrake;
    private List<File> inputFiles;
    private String handBrakeQuery = "-f mkv --strict-anamorphic -e x264 -q 25 -a 1 -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1";
    private String renamePattern = "{name}-conv";
    private File outputFolder;
    private boolean preserveFolders = true;
    
    
    @Test
    public void testClassInitialisationRename() {
        handbrake.buildScript();
        
        assertEquals(handBrakeQuery, handbrake.commandLine);
        assertEquals("mkv", handbrake.destinationFileExtension);
//        assertEquals(null, handbrake.executerPath);
        assertEquals(inputFiles, handbrake.files);
        assertEquals(Model.OutputMethod.INPLACE, handbrake.outputMethod);
        assertEquals(renamePattern, handbrake.renamePattern);
    }
    
    @Test
    public void testClassInitialisationFolder() {
        setUpVariant();
        
        handbrake.buildScript();
        
        assertEquals(handBrakeQuery, handbrake.commandLine);
        assertEquals("mkv", handbrake.destinationFileExtension);
//        assertEquals(null, handbrake.executerPath);
        assertEquals(inputFiles, handbrake.files);
        if (Platform.isWindows()) {
            assertEquals(new File("C:\\export"), handbrake.outputFolder);
        } else {
            assertEquals(new File("/home/export"), handbrake.outputFolder);
        }
        assertEquals(preserveFolders, handbrake.preserveFolders);
        assertEquals(Model.OutputMethod.SPECIFIC_FOLDER, handbrake.outputMethod);
    }
    
    @Test
    public void testBuildScriptRename() {
        String result = handbrake.buildScript();
        String expected = HandbrakeExportStatic.buildQueue(inputFiles, handBrakeQuery, renamePattern);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testBuildScriptFolder() {
        setUpVariant();
        
        String result = handbrake.buildScript();
        String expected = HandbrakeExportStatic.buildQueue(inputFiles, handBrakeQuery, outputFolder, preserveFolders);
        
        assertEquals(expected, result);
    }
    
}
