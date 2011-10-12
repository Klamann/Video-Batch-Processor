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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class HandbrakeExportTest {
    
    private static String query = "-f mkv --strict-anamorphic -e x264 -q 25 -a 1"
            + " -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1";
    private static File input = new File("C:/movies/test/video.avi");
    private static File output = new File("C:/movies/output");
    
    
    
    
    @Test
    public void testComposeOutput() {
        String result = HandbrakeExport.composeOutput("C:/movies/test", "video", query);
        assertEquals("C:\\movies\\test\\video.mkv", result);
    }
    
    @Test
    public void testExtractFileExtension() {
        String ext = HandbrakeExport.extractFileExtension(query);
        assertEquals("mkv", ext);
    }
    
    @Test
    public void testGenerateOutputFileUseRename() {
        String result = HandbrakeExport.applyRenamePattern(input, "{name}-change", query);
        assertEquals("C:\\movies\\test\\video-change.mkv", result);
    }
    
    @Test
    public void testGenerateOutputFileUseFolder() {
        String flatten = HandbrakeExport.generateOutputFile(input, output, false, query);
        assertEquals("C:\\movies\\output\\video.mkv", flatten);
        
        String preserve = HandbrakeExport.generateOutputFile(input, output, true, query);
        assertEquals("C:\\movies\\output\\C\\movies\\test\\video.mkv", preserve);
    }
    
    @Test
    public void testRemoveExtension() {
        String pureName = HandbrakeExport.removeExtension("this.is.the.filename.avi");
        assertEquals("this.is.the.filename", pureName);
    }
    
//    @Test
//    public void test() {
//        
//    }

}
