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
import java.util.ArrayList;
import java.io.File;
import java.util.List;
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
public class ExportFFmpegTest {
    
    public ExportFFmpegTest() {
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
    
    private List<File> inputFiles;
    private String commandLine = "ffmpeg -i video_origine.avi video_finale.mpg";
    private String renamePattern = "{name}-conv";
//    private File outputFolder;
//    private boolean preserveFolders = true;

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//    
//    @Before
//    public void setUp() {
//    }
//    
//    @After
//    public void tearDown() {
//    }

    
    @Test
    public void testFFmpeg() {
        Export ffmpeg = new ExportFFmpeg(inputFiles, commandLine, "ffmpeg", renamePattern);
        String script = ffmpeg.buildScript();
    }
    
}
