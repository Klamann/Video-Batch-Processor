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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class CommandLineTest {
    
    /**
     * Test of removeArgFromString method, of class Model.
     */
    @Test
    public void testRemoveArgFromString() {
        String commandLine = " -i \"C:\\path\\to\\some\\folder on my disk\\P1020037.MOV\" -t 1 -c 1 -o \"C:\\path\\to\\some\\folder on my disk\\P1020037.mkv\" -f mkv --strict-anamorphic  -e x264 -q 25 -a 1 -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1";
        
        String result = CommandLine.removeArgFromString(commandLine, "-i");
        String expected = "  -t 1 -c 1 -o \"C:\\path\\to\\some\\folder on my disk\\P1020037.mkv\" -f mkv --strict-anamorphic  -e x264 -q 25 -a 1 -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1";
        assertEquals(expected, result);
        
        String result2 = CommandLine.removeArgFromString(result, "-o");
        String expected2 = "  -t 1 -c 1  -f mkv --strict-anamorphic  -e x264 -q 25 -a 1 -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1";
        assertEquals(expected2, result2);
        
        assertEquals(result2, CommandLine.removeArgFromString(commandLine, "-i", "-o"));
        
    }

    /**
     * Test of nthIndexOf method, of class Model.
     */
    @Test
    public void testNthIndexOf() {
        String count = "01234n6789n1234n56789n123";
        
        int index = CommandLine.nthIndexOf(count, 'n', 0, 3);
        assertEquals(15, index);
    }
    
    /**
     * 
     */
    @Test
    public void testGetArgValue() {
        String commandLine = "ffmpeg -i film.avi film.mpeg";
        String result = CommandLine.getArgValue(commandLine, "-i");
        assertEquals("film.avi", result);
        
        String commandLine2 = "ffmpeg -i \"film with whitespace.avi\" film.mpeg";
        String result2 = CommandLine.getArgValue(commandLine2, "-i");
        assertEquals("film with whitespace.avi", result2);
        
        String fail = "ffmpeg this script failed...";
        String result3 = CommandLine.getArgValue(fail, "-i");
        assertEquals(null, result3);
    }
}
