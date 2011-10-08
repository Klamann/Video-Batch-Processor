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

import vbp.util.xml.XMLUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This is the export script to create Handbrake-Queue files out of a list of
 * video files.
 * The queue files can be imported into Handbrake to be processed there.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class HandbrakeExport {
    
    /**
     * This function acts as a wrapper for buildQueue(). It generates the .queue-
     * File and promts the user with a saving-dialogue, so the file can be written
     * to the desired location.
     * @param fileChooser the jFileChooser to popup
     * @param files all files that shall be transcoded by handbrake
     * @param query the handbrake query defining the coding settings that shall be
     *        applied to all files
     * @param renamePattern the pattern after which the files are renamed (because
     *        they will be saved in the same folder as the originals the cannot have
     *        the exact same name, including extension)
     */
    public static void saveQueue(JFileChooser fileChooser, final List<File> files, final String query, final String renamePattern) {
        
        // export in new thread
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return HandbrakeExport.buildQueue(files, query, renamePattern);
            }
        };
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<String> result = executor.submit(callable);
        
        // open dialogue and write file as soon as export finishes
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                Writer fileWriter = null;
                try {
                    fileWriter = new FileWriter(fileChooser.getSelectedFile());
                    fileWriter.write(result.get());
                } catch (IOException ex) {
                    Logger.getLogger(HandbrakeExport.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException ex) {
                            Logger.getLogger(HandbrakeExport.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(HandbrakeExport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(HandbrakeExport.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            executor.shutdown();
        }
        
    }

    /**
     * Generates a Handbrake .queue-File containing all Files passed as an argument
     * to this method. The files will be transcoded using the given Handbrake-Query.
     * The transcoded files will be written in the same location, using the specified
     * rename-pattern.
     * @param files all files that shall be transcoded by handbrake
     * @param query the handbrake query defining the coding settings that shall be
     *        applied to all files
     * @param renamePattern the pattern after which the files are renamed (because
     *        they will be saved in the same folder as the originals the cannot have
     *        the exact same name, including extension)
     * @return the full query-file as string
     */
    public static String buildQueue(List<File> files, String query, String renamePattern) {

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
                String destination = applyRenamePattern(file, renamePattern, query);

                // build job wrapper in xml
                Element job = new Element("Job");
                root.addContent(job);

                // add specific properties
                job.addContent(new Element("Id").setText(String.valueOf(counter)));
                job.addContent(new Element("Title").setText(title));
                job.addContent(new Element("Query").setText(buildHandbrakeQuery(query, source, destination)));
                job.addContent(new Element("CustomQuery").setText(Boolean.toString(customQuery)));
                job.addContent(new Element("Source").setText(source));
                job.addContent(new Element("Destination").setText(destination));

                // finally
                counter++;
                
            } catch (IOException ex) {
                Logger.getLogger(HandbrakeExport.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        // Debugging
        System.out.println(XMLUtil.xmlToString(doc, Format.getPrettyFormat()));

        return XMLUtil.xmlToString(doc, Format.getPrettyFormat());
    }

    /**
     * not yet implemented!
     */
    public static String buildQeue(List<File> files, String query, File destination, boolean preserveStructure) {
        throw new NotImplementedException();
    }

    // ++++++++++ static xml stuff ++++++++++
    
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

    /**
     * Generates the Handbrake-Query. Takes the original query without path names,
     * the source file and the destination as inputs
     * @param query original handbrake query without -i and -o
     * @param source the canocial path to the source file
     * @param destination the canocial path to the destination file
     * @return the full handbrake query
     */
    protected static String buildHandbrakeQuery(String query, String source, String destination) {
        return String.format("-i \"%s\" -o \"%s\" %s", source, destination, query);
    }
    
    /**
     * Applys the user's rename pattern on the source file, to generate the correct
     * output file name. It reflects the correct file extension from the handbrake-
     * query.
     * @param source the source file path
     * @param pattern the rename pattern
     * @param query the handbrake query
     * @return the destination file path, renamed and with correct extension.
     */
    protected static String applyRenamePattern(File source, String pattern, String query) {
        
        // gather properties
        String fullName = source.getName();
        String path = source.getParent();
        
        // get filename, remove extension, replace generics (e.g. {name} )
        int extIndex = fullName.lastIndexOf('.');
        String nameSource = fullName.substring(0, extIndex);
        String name = pattern.replaceAll("\\{name\\}", nameSource);
        
        // extract file extension from handbrake-query
        int formatBegin = query.indexOf("-f ") + 3;
        while(query.charAt(formatBegin) == ' ') {
            formatBegin++;
        }
        int formatEnd = formatBegin+1;
        while(query.charAt(formatEnd) != ' ') {
            formatEnd++;
        }
        String ext = query.substring(formatBegin, formatEnd);
        
        // full file path with new filename and extension according to handbrake-query
        File result = new File(String.format("%s/%s.%s", path, name, ext));
        try {
            return result.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(HandbrakeExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return String.format("%s%s.%s", path, name, ext);
    }
}
