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
package vbp.util.data;

import java.util.Iterator;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class StringIterator implements Iterable<Character> {

    private final String string;

    public StringIterator(final String string) {
        this.string = string;
    }
    
    public Iterator<Character> iterator() {
        return new Iterator<Character>() {

            private int position = 0;

            public boolean hasNext() {
                return (this.position < string.length());
            }

            public Character next() {
                if (this.position >= string.length()) {
                    return null;
                }
                return string.charAt(this.position++);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}