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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import sebi.util.threads.FutureBuilder;
import vbp.gui.FileFilters;

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
        
        Future<String> queue = new FutureBuilder<String>() {
            @Override
            public String build() {
                return HandbrakeExport.buildQueue(files, query, renamePattern);
            }
        }.getFuture();
        saveQueueWorker(fileChooser, queue);
    }
    
    // comment
    public static void saveQueue(JFileChooser fileChooser, final List<File> files, final String query, final File outputFolder, final boolean preserveFolders) {
        
        Future<String> queue = new FutureBuilder<String>() {
            @Override
            public String build() {
                return HandbrakeExport.buildQueue(files, query, outputFolder, preserveFolders);
            }
        }.getFuture();
        saveQueueWorker(fileChooser, queue);
    }
    
    // comment
    protected static void saveQueueWorker(JFileChooser fileChooser, Future<String> queue) {
        // open dialogue and write file as soon as export finishes
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                Writer fileWriter = null;
                try {
                    fileWriter = new FileWriter(FileFilters.enforceFileExtension(fileChooser.getSelectedFile(), "queue"));
                    fileWriter.write(queue.get());
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
            queue.cancel(true);
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
    protected static String buildQueue(List<File> files, String query, String renamePattern) {

        Map<File,String> output = new HashMap<File, String>(files.size());
        for (File input : files) {
            output.put(input, applyRenamePattern(input, renamePattern, query));
        }
        
        return buildQueueWorker(files, query, output);
    }

    /**
     * Generates a Handbrake .queue-File containing all Files passed as an argument
     * to this method. The files will be transcoded using the given Handbrake-Query.
     * The transcoded files will be written to the specified location, either all
     * files in the same folder or the folder structure will be copied into the destination
     * folder.
     * @param files all files that shall be transcoded by handbrake
     * @param query the handbrake query defining the coding settings that shall be
     *        applied to all files
     * @param outputFolder the output location
     * @param preserveStructure false: put all files directly in the output folder,
     *        true: write the file path to the source file into the output folder
     * @return the full query-file as string
     */
    public static String buildQueue(List<File> files, String query, File outputFolder, boolean preserveStructure) {
        
        Map<File,String> output = new HashMap<File, String>(files.size());
        for (File input : files) {
            output.put(input, generateOutputFile(input, outputFolder, preserveStructure, query));
        }
        
        return buildQueueWorker(files, query, output);
    }
    
    /**
     * Abstraction of the queue-Generator. Needs a map of Input files to output
     * file as input. This must be generated by the specific users of this method.
     * @param input all files that shall be transcoded by handbrake
     * @param query the handbrake query defining the coding settings that shall be
     *        applied to all files
     * @param output a map containing the exact output location for every file
     * @return the full query-file as string
     */
    private static String buildQueueWorker(List<File> input, String query, Map<File,String> output) {
        
        // basic document
        Document doc = buildBasicDocument();
        Element root = doc.getRootElement();

        // job properties
        long counter = 0;
        String title = "1";
        boolean customQuery = true;

        for (File file : input) {
            try {
                // build missing contents
                String source = file.getCanonicalPath();
                String destination = output.get(file);

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
        String fullName = removeExtension(source.getName());
        String path = source.getParent();
        
        // replace generics (e.g. {name} )
        String name = pattern.replaceAll("\\{name\\}", fullName);
        
        return composeOutput(path, name, query);
    }
    
    /**
     * Generates the location of the output file, when using a specific folder for
     * the output. Can flatten the output (so all files go into the output folder)
     * or replicate the folder structure to the input file in the output file folder
     * @param input the input file
     * @param outputFolder the user defined output folder
     * @param preserveStructure false: put all files directly in the output folder,
     *        true: write the file path to the source file into the output folder
     * @param query the handbrake query
     * @return the destination file path, with correct extension.
     */
    protected static String generateOutputFile(File input, File outputFolder, boolean preserveStructure, String query) {
        
        String fileName = removeExtension(input.getName());
        String path = outputFolder.getPath();
        
        if (preserveStructure) {
            String pathParent = path;
            String pathChild = input.getParent().replaceAll(":", "");
            File pathBoth = new File(String.format("%s/%s", pathParent, pathChild));
            path = pathBoth.getPath();
        }
        
        return composeOutput(path, fileName, query);
    }
    
    /**
     * Composes the correct output String from the filepath, the filename and the
     * Handbrake-Query (to determine the file extension).
     * So all you have to do is to find out where you wanna locate your file and what
     * name it shall have.
     * @param path absolute path to your destonation file's parent folder
     * @param name name of your destination file (without extension)
     * @param query the unchanged Handbrake-query
     * @return the complete file path (canonical, as string)
     */
    protected static String composeOutput(String path, String name, String query) {
        // add correct file extension
        String ext = extractFileExtension(query);
        File result = new File(String.format("%s/%s.%s", path, name, ext));
        
        // generate canocial path
        try {
            return result.getCanonicalPath();
        } catch (IOException ex) {
            // backup: just put strings together
            return String.format("%s%s.%s", path, name, ext);
        }
    }
    
    /**
     * Extract the file extension for the transcoded file from the Handbrake-query
     * @param query Handbrake-query
     * @return file extension for the file to transcode
     */
    protected static String extractFileExtension(String query) {
        int formatBegin = query.indexOf("-f ") + 3;
        while(query.charAt(formatBegin) == ' ') {
            formatBegin++;
        }
        int formatEnd = formatBegin+1;
        while(query.charAt(formatEnd) != ' ') {
            formatEnd++;
        }
        return query.substring(formatBegin, formatEnd);
    }
    
    /**
     * removes the file extension from a file name, if the name has an extension.
     * If not the unchanged filename will be returned
     * @param fileName name of the file as String (use File.getName() to get it)
     * @return filename without extension
     */
    protected static String removeExtension(String fileName) {
        if (fileName.contains(".")) {
            // remove file extension
            int extIndex = fileName.lastIndexOf('.');
            String pureName = fileName.substring(0, extIndex);
            return pureName;
        }
        // file has no extension -> return unchanged
        return fileName;
    }
    
}
