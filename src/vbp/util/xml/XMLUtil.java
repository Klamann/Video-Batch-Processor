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
package vbp.util.xml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.List;

/**
 * This is a collection of static functions that extend the functionality of
 * the jdom-api
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class XMLUtil {

    /**
     * Returns a single line string representation of an xml-document
     * @param doc the xml document object (JDOM)
     * @return the document as single line string
     */
    public static String xmlToString(Document doc) {
        XMLOutputter outp = new XMLOutputter(Format.getRawFormat());
        return outp.outputString(doc);
    }

    /**
     * Returns a xml document as formatted string, using jdom's Format class
     * @param doc the xml document object (JDOM)
     * @param format the formatting (e.g. getPrettyFormat())
     * @return the document as formatted string
     */
    public static String xmlToString(Document doc, Format format) {
        XMLOutputter outp = new XMLOutputter(format);
        return outp.outputString(doc);
    }

    /**
     * Returns the first child of an element or <code>null</code> if it doesn't
     * have any child elements.
     * @param parent the parent element
     * @return first child element (in document order)
     */
    public static Element getFirstChild(Element parent) {
        List<Element> children = parent.getChildren();
        if (children.isEmpty()) {
            return null;
        } else {
            return children.get(0);
        }
    }
}
