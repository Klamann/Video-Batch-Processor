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
 * Hier werden diverse nuetzliche statische Methoden gesammelt, die
 * den Umgang mit XML und Catan vereinfachen.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class XMLUtil {

    /**
     * Gibt das XML als einzeiligen String zurueck
     * @param doc das XML Dokument (JDOM)
     * @return das XML als String
     */
    public static String xmlToString(Document doc) {
        XMLOutputter outp = new XMLOutputter(Format.getRawFormat());
        return outp.outputString(doc);
    }

    /**
     * Gibt das XML (JDOM) als formatierten String zurueck
     * @param doc das XML Dokument (JDOM)
     * @param format die Formatierung (z.B. getPrettyFormat())
     * @return das XML als String
     */
    public static String xmlToString(Document doc, Format format) {
        XMLOutputter outp = new XMLOutputter(format);
        return outp.outputString(doc);
    }

    /**
     * Gibt das erste Kindelement eines beliebigen Elements zurueck
     * oder null wenn es keine Kinder hat.
     * @param parent Elternelement
     * @return erstes Kindelement (in Document Order)
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
