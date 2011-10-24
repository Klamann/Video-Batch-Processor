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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import vbp.util.xml.XMLUtil;

/**
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class ExportHandbrake extends Export {

    public ExportHandbrake(List<File> files, String commandLine, String renamePattern) {
        super(files, "queue", commandLine, renamePattern);
    }

    public ExportHandbrake(List<File> files, String commandLine, File outputFolder, boolean preserveFolders) {
        super(files, "queue", commandLine, outputFolder, preserveFolders);
    }
    
    @Override
    protected String buildScriptImplementation(Map<File, String> outputMapping) {
        
        // basic document
        Document doc = buildBasicDocument();
        Element root = doc.getRootElement();

        // job properties
        long counter = 0;
        String title = "1";
        boolean customQuery = true;

        for (File file : files) {
            try {
                // build missing contents
                String source = file.getCanonicalPath();
                String destination = outputMapping.get(file);

                // build job wrapper in xml
                Element job = new Element("Job");
                root.addContent(job);

                // add specific properties
                job.addContent(new Element("Id").setText(String.valueOf(counter)));
                job.addContent(new Element("Title").setText(title));
                job.addContent(new Element("Query").setText(buildScriptLine(source, destination)));
                job.addContent(new Element("CustomQuery").setText(Boolean.toString(customQuery)));
                job.addContent(new Element("Source").setText(source));
                job.addContent(new Element("Destination").setText(destination));

                // finally
                counter++;
                
            } catch (IOException ex) {
                Logger.getLogger(HandbrakeExportStatic.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        // Debugging
        System.out.println(XMLUtil.xmlToString(doc, Format.getPrettyFormat()));

        return XMLUtil.xmlToString(doc, Format.getPrettyFormat());
    }

    @Override
    protected String buildScriptLine(String source, String destination) {
        return String.format("-i \"%s\" -o \"%s\" %s", source, destination, commandLine);
    }

    @Override
    protected String extractFileExtension() {
        int formatBegin = commandLine.indexOf("-f ") + 3;
        while(commandLine.charAt(formatBegin) == ' ') {
            formatBegin++;
        }
        int formatEnd = formatBegin+1;
        while(commandLine.charAt(formatEnd) != ' ') {
            formatEnd++;
        }
        return commandLine.substring(formatBegin, formatEnd);
    }
    
    // helpers
    
    /**
     * Generates the basic Handbrake-Queue-XML structure
     * @return the handbrake queue xml-object (as jdom document)
     */
    protected static Document buildBasicDocument() {
        Element root = new Element("ArrayOfJob");
        Document doc = new Document(root);

        Namespace xmlSchemaInstance = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace xmlSchema = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        root.addNamespaceDeclaration(xmlSchemaInstance);
        root.addNamespaceDeclaration(xmlSchema);

        return doc;
    }
    
}
