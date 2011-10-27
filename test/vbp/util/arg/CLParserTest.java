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
package vbp.util.arg;

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
public class CLParserTest {
    
    public CLParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        this.parsed1 = new CLParser(stringParsed1);
        this.parsed2 = new CLParser(stringParsed2);
        this.parsed3 = new CLParser(stringParsed3);
    }
    
    @After
    public void tearDown() {
    }
    
    private String stringParsed1 = "ffmpeg -i \"video origine.avi\" video_finale.mpg";
    private String stringParsed2 = "   ffmpeg  -i      \"video origine.avi\"  video_finale.mpg    ";
    private String stringParsed3 = "ffmpeg -i video_origine.avi -ab 56 -ar 44100 -b 200 -r 15 -s 320x240 -f flv \"video finale.flv\" ";
    
    private CLParser parsed1;
    private CLParser parsed2;
    private CLParser parsed3;

    /**
     * Test of getFirstArg method, of class CLParser.
     */
    @Test
    public void testGetFirstArg() {
        assertEquals("ffmpeg", parsed1.getFirstArg());
        assertEquals("ffmpeg", parsed2.getFirstArg());
        assertEquals("ffmpeg", parsed3.getFirstArg());
    }

    /**
     * Test of getLastArg method, of class CLParser.
     */
    @Test
    public void testGetLastArg() {
        assertEquals("video_finale.mpg", parsed1.getLastArg());
        assertEquals("video_finale.mpg", parsed2.getLastArg());
        assertEquals("video finale.flv", parsed3.getLastArg());
    }

    /**
     * Test of getNthArg method, of class CLParser.
     */
    @Test
    public void testGetNthArg() {
        assertEquals("-i", parsed1.getNthArg(1));
        assertEquals("video origine.avi", parsed2.getNthArg(2));
        assertEquals("-r", parsed3.getNthArg(9));
        assertNull(parsed3.getNthArg(100));
    }

    /**
     * Test of getSubsequentArg method, of class CLParser.
     */
    @Test
    public void testGetSubsequentArg() {
        assertEquals("video origine.avi", parsed1.getSubsequentArg("-i"));
        assertEquals("video origine.avi", parsed2.getSubsequentArg("-i"));
        assertEquals("200", parsed3.getSubsequentArg("-b"));
    }

    /**
     * Test of printString method, of class CLParser.
     */
    @Test
    public void testPrintString() {
        assertEquals(stringParsed1, parsed1.printString());
        assertEquals(stringParsed1, parsed1.toString());
        assertEquals(stringParsed1, parsed2.printString());
        assertEquals(stringParsed3, parsed3.printString()+" ");
    }

    /**
     * Test of clean method, of class CLParser.
     */
    @Test
    public void testClean() {
        assertEquals(stringParsed1, CLParser.clean(stringParsed1));
        assertEquals(stringParsed1, CLParser.clean(stringParsed2));
        assertEquals(stringParsed3, CLParser.clean(stringParsed3)+" ");
    }

    /**
     * Test of removeArg method, of class CLParser.
     */
    @Test
    public void testRemoveArg() {
        assertTrue(parsed1.removeArg("video_finale.mpg"));
        assertTrue(parsed1.removeArg("video origine.avi"));
        assertEquals("ffmpeg -i", parsed1.toString());
        
        assertTrue(parsed2.removeArg("video_finale.mpg"));
        assertTrue(parsed2.removeArg("\"video origine.avi\""));
        assertFalse(parsed2.removeArg("video_finale.mpg"));
        assertEquals("ffmpeg -i", parsed2.toString());
    }

    /**
     * Test of removeSubsequentArg method, of class CLParser.
     */
    @Test
    public void testRemoveSubsequentArg() {
        assertTrue(parsed1.removeSubsequentArg("-i"));
        assertEquals("ffmpeg video_finale.mpg", parsed1.toString());
    }

    /**
     * Test of removeNthArg method, of class CLParser.
     */
    @Test
    public void testRemoveNthArg() {
        assertTrue(parsed1.removeNthArg(1));
        assertTrue(parsed1.removeNthArg(1));
        assertFalse(parsed1.removeNthArg(2));
        assertEquals("ffmpeg video_finale.mpg", parsed1.toString());
    }

    /**
     * Test of removeFirstArg method, of class CLParser.
     */
    @Test
    public void testRemoveFirstArg() {
        assertTrue(parsed1.removeFirstArg());
        assertEquals("-i \"video origine.avi\" video_finale.mpg", parsed1.toString());
    }

    /**
     * Test of removeLastArg method, of class CLParser.
     */
    @Test
    public void testRemoveLastArg() {
        assertTrue(parsed1.removeLastArg());
        assertTrue(parsed1.removeLastArg());
        assertEquals("ffmpeg -i", parsed1.toString());
        
        assertTrue(parsed1.removeLastArg());
        assertTrue(parsed1.removeLastArg());
        assertFalse(parsed1.removeLastArg());
    }

    /**
     * Test of exists method, of class CLParser.
     */
    @Test
    public void testExists() {
        assertTrue(parsed1.exists("ffmpeg"));
        assertTrue(parsed1.exists("-i"));
        assertTrue(parsed1.exists("video origine.avi"));
        assertTrue(parsed1.exists("\"video origine.avi\""));
        assertTrue(parsed1.exists("video_finale.mpg"));
        
        assertFalse(parsed1.exists("\"ffmpeg\""));
        assertFalse(parsed1.exists(" -i"));
    }

    /**
     * Test of getArgIndex method, of class CLParser.
     */
    @Test
    public void testGetArgIndex() {
        assertEquals(0, parsed1.getArgIndex("ffmpeg"));
        assertEquals(1, parsed1.getArgIndex("-i"));
        assertEquals(2, parsed1.getArgIndex("\"video origine.avi\""));
        assertEquals(2, parsed1.getArgIndex("video origine.avi"));
        assertEquals(-1, parsed1.getArgIndex(" -i"));
    }

    /**
     * Test of parse method, of class CLParser.
     */
    @Test
    public void testParse() {
        CLParser testParse = CLParser.parse(stringParsed1);
        assertEquals(parsed1.toString(), testParse.toString());
    }
    
//    @Test
//    public void testSomethingElse() {
//        String destinationFileExtension = null;
//        String commandLine = stringParsed3;
//        
//        if(destinationFileExtension == null) {
//            CLParser parser = new CLParser(commandLine);
//            String destinationFileName = parser.getLastArg();
//            
//            if(destinationFileName.contains(".")) {
//                int extIndex = destinationFileName.lastIndexOf('.') + 1;
//                destinationFileExtension = destinationFileName.substring(extIndex);
//            } else {
//                destinationFileExtension = parser.getSubsequentArg("-f");
//            }
//        }
//        
//        assertEquals("flv", destinationFileExtension);
//    }
}
