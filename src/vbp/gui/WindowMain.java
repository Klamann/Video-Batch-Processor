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
package vbp.gui;

import java.net.URISyntaxException;
import vbp.model.Model;
import vbp.model.Model.GuiComponents;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.positioners.LeftAbovePositioner;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.EdgedBalloonStyle;
import net.java.balloontip.utils.ToolTipUtils;
import sebi.util.observer.Event;
import sebi.util.observer.ObserverArgs;
import sebi.util.threads.ThreadedExecutor;

/**
 * The main gui of the application. Built with Netbeans (about 3/4 of the code lines
 * in this class are generated)
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class WindowMain extends javax.swing.JFrame implements Saveable {

    protected Model model;
    protected GUI gui;
    
    /**
     * Main Window Constructor. Creates a new Instance of the main window.
     * @param model 
     */
    public WindowMain(Model model, GUI gui) {
        this.model = model;
        this.gui = gui;
        
        initComponents();
        initCustomComponents();
        initEvents();
        setInitialValues();
    }
    
    // <editor-fold desc="GUI manipulation">

    /**
     * initialize the gui elements with values from the model.
     */
    private void setInitialValues() {
        // input
        jCheckBoxRecursive.setSelected(model.isRecursive());

        // output
        jRadioButtonSamePlace.setSelected(model.isOutputSamePlace());
        jRadioButtonDifferentFolder.setSelected(model.isOutputDifferentFolder());
        jTextFieldRenamePattern.setText(model.getRenamePattern());
        jTextFieldDifferentFolder.setText(model.getOutputLocation());
        jCheckBoxPreserveFolders.setSelected(model.getPreserveFolders());

        // search pattern
        jRadioButtonSelectProperties.setSelected(model.isSearchPatternProperties());
        jRadioButtonSelectRegex.setSelected(model.isSearchPatternRegex());
        jCheckBoxExtension.setSelected(model.getFileExtension());
        jTextFieldExtensions.setText(model.getExtensionFilter());
        jCheckBoxSize.setSelected(model.getFileSize());
        jFormattedTextFieldSizeMin.setValue(model.getMinSize());
        jFormattedTextFieldSizeMax.setValue(model.getMaxSize());
        jTextFieldRegex.setText(model.getRegex());
    }
    
    @Override
    public void updateModelValues() {
        
        // input
        model.setRecursive(jCheckBoxRecursive.isSelected());
        
        // output
        if(jRadioButtonSamePlace.isSelected()) {
            model.setOutputSamePlace();
        } else if(jRadioButtonDifferentFolder.isSelected()) {
            model.setOutputDifferentFolder();
        }
        model.setRenamePattern(jTextFieldRenamePattern.getText());
        if(!jTextFieldDifferentFolder.getText().isEmpty()) {
            model.setOutputLocation(new File(jTextFieldDifferentFolder.getText()));
        } else {
            model.setOutputLocation(null);      // remove entry
        }
        model.setPreserveFolders(jCheckBoxPreserveFolders.isSelected());
        
        // search pattern
        if(jRadioButtonSelectProperties.isSelected()) {
            model.setSearchPatternProperties();
        } else if(jRadioButtonSelectRegex.isSelected()) {
            model.setSearchPatternRegex();
        }
        model.setFileSize(jCheckBoxSize.isSelected());
        model.setFileExtension(jCheckBoxExtension.isSelected());
        model.setExtensionFilter(jTextFieldExtensions.getText());
        model.setMinSize((Integer) jFormattedTextFieldSizeMin.getValue());
        model.setMaxSize((Integer) jFormattedTextFieldSizeMax.getValue());
        model.setRegex(jTextFieldRegex.getText());
    }
    
    @Override
    public void updateGuiValues() {
        setInitialValues();
        updateInputFiles();
        updateTranscodeFiles();
    }
    
    private void runProgressBar(boolean bar) {
        jProgressBarScan.setIndeterminate(bar);
    }
    
    /**
     * @return the icon image for the application
     */
    protected Image getIcon() {
        return gui.getIcon();
    }
    
    // </editor-fold>
    // <editor-fold desc="GUI Actions">
    
    @Override
    public void safeExit() {
        updateModelValues.fire();
        model.safeExit();
    }

    /**
     * Replace the list view of the input files in the GUI with the current
     * model values
     */
    protected void updateInputFiles() {
        List<String> files = model.getInputFiles();
        listModelInput.clear();
        for (String file : files) {
            listModelInput.addElement(file);
        }
    }

    /**
     * Replace the list view of the files to transcode in the GUI with the
     * current model values
     */
    protected void updateTranscodeFiles() {
        List<String> files = model.getFilesToTranscode();
        listModelTranscode.clear();
        for (String file : files) {
            listModelTranscode.addElement(file);
        }
        jProgressBarScan.setIndeterminate(false);
    }
    
    protected void browseInput() {
        if (jFileChooserInput.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            runProgressBar(true);
            new ThreadedExecutor() {

                @Override
                public void execute() {
                    updateModelValues.fire();
                    model.addInputFiles(jFileChooserInput.getSelectedFiles());
                }
            }.start();
        }
    }
    
    protected void removeInput() {
        runProgressBar(true);
        new ThreadedExecutor() {

            @Override
            public void execute() {
                int[] selected = jListInput.getSelectedIndices();

                String[] files = new String[selected.length];
                for (int i = 0; i < selected.length; i++) {
                    files[i] = listModelInput.get(selected[i]);
                }

                model.removeInputFiles(files);
            }
        }.start();
    }
    
    protected void browseOutput() {
        if (jFileChooserOutput.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            model.setOutputLocation(jFileChooserOutput.getSelectedFile());
            jTextFieldDifferentFolder.setText(model.getOutputLocation());
        }
    }
    
    protected void rescan() {
        runProgressBar(true);
        new ThreadedExecutor() {

            @Override
            public void execute() {
                updateModelValues.fire();
                model.updateFilesToTranscode();
            }
        }.start();
    }
    
    
    
    private static void openWeb(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException ex) {
                Logger.getLogger(WindowMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, String.format("Your java runtime does not seem to support the opening of weblinks.\n"
                    + "You can open the link manually though:\n%s", uri.toString()), "Unable to open weblink", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void openWeb(URL url) {
        try {
            openWeb(url.toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(WindowMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void openWeb(String uri) {
        try {
            openWeb(new URI(uri));
        } catch (URISyntaxException ex) {
            Logger.getLogger(WindowMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // </editor-fold>
    // <editor-fold desc="Events">
    
    // GUI
    
    protected Event updateModelValues = new Event();
    
    public Event eventUpdateModelValues() {
        return updateModelValues;
    }
    
    // Model
    
    private void initEvents() {
        model.eventUpdateGUI().addObserver(onUpdateGUI());
    }

    private ObserverArgs<GuiComponents> onUpdateGUI() {
        return new ObserverArgs<GuiComponents>() {

            @Override
            public void update(GuiComponents component) {
                switch (component) {
                    case DIFFERENT_FOLDER:
                        // TODO
                        break;
                    case HANDBRAKE_QUERY:
                        // TODO
                        break;
                    case LIST_INPUT:
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                updateInputFiles();
                            }
                        });
                        break;
                    case LIST_TRANSCODE:
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                updateTranscodeFiles();
                                runProgressBar(false);
                            }
                        });
                        break;
                    case PATTERN_EXTENSIONS:
                        // TODO
                        break;
                    case PATTERN_REGEX:
                        // TODO
                        break;
                    case PATTERN_SIZE_MAX:
                        // TODO
                        break;
                    case PATTERN_SIZE_MIN:
                        // TODO
                        break;
                    case RENAME_PATTERN:
                        // TODO
                        break;
                }
            }
        };
    }
    
    // </editor-fold>
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPatternChooser = new javax.swing.ButtonGroup();
        outputTypeChooser = new javax.swing.ButtonGroup();
        jFileChooserInput = new javax.swing.JFileChooser();
        jFileChooserProjectLoad = new javax.swing.JFileChooser();
        jFileChooserProjectSave = new javax.swing.JFileChooser();
        jFrameAbout = new javax.swing.JFrame();
        jButtonAboutClose = new javax.swing.JButton();
        jButtonHomepage = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPaneAboutText = new javax.swing.JEditorPane();
        jLabelAboutTitle = new javax.swing.JLabel();
        jFileChooserOutput = new javax.swing.JFileChooser();
        jPanelFileView = new javax.swing.JPanel();
        jScrollPaneFileList = new javax.swing.JScrollPane();
        listModelTranscode = new javax.swing.DefaultListModel<String>();
        jListTranscode = new javax.swing.JList(listModelTranscode);
        jButtonUp = new javax.swing.JButton();
        jButtonDel = new javax.swing.JButton();
        jButtonDown = new javax.swing.JButton();
        jButtonRescan = new javax.swing.JButton();
        jProgressBarScan = new javax.swing.JProgressBar();
        jButtonCopyToClipboard = new javax.swing.JButton();
        jButtonSaveListAs = new javax.swing.JButton();
        jButtonClearTranscode = new javax.swing.JButton();
        jToolBar = new javax.swing.JToolBar();
        jButtonLoadProject = new javax.swing.JButton();
        jButtonSaveProject = new javax.swing.JButton();
        jButtonAddFiles = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonExportHandbrake = new javax.swing.JButton();
        jButtonExportFFmpeg = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonExit = new javax.swing.JButton();
        jTabbedPaneSettings = new javax.swing.JTabbedPane();
        jPanelInput = new javax.swing.JPanel();
        jPanelSearch = new javax.swing.JPanel();
        jScrollPaneInput = new javax.swing.JScrollPane();
        listModelInput = new javax.swing.DefaultListModel<String>();
        jListInput = new javax.swing.JList(listModelInput);
        jButtonInputUp = new javax.swing.JButton();
        jButtonInputDel = new javax.swing.JButton();
        jButtonInputDown = new javax.swing.JButton();
        jButtonInputBrowse = new javax.swing.JButton();
        jCheckBoxRecursive = new javax.swing.JCheckBox();
        jButtonClearInput = new javax.swing.JButton();
        jPanelOutput = new javax.swing.JPanel();
        jRadioButtonSamePlace = new javax.swing.JRadioButton();
        jLabelRenamePattern = new javax.swing.JLabel();
        jTextFieldRenamePattern = new javax.swing.JTextField();
        jButtonRenamePatternHelp = new javax.swing.JButton();
        jRadioButtonDifferentFolder = new javax.swing.JRadioButton();
        jTextFieldDifferentFolder = new javax.swing.JTextField();
        jButtonOutputBrowse = new javax.swing.JButton();
        jCheckBoxPreserveFolders = new javax.swing.JCheckBox();
        jPanelSearchPattern = new javax.swing.JPanel();
        jRadioButtonSelectProperties = new javax.swing.JRadioButton();
        jRadioButtonSelectRegex = new javax.swing.JRadioButton();
        jPanelProperties = new javax.swing.JPanel();
        jLabelSizeFrom = new javax.swing.JLabel();
        jLabelSizeTo = new javax.swing.JLabel();
        jLabelSizeMb = new javax.swing.JLabel();
        jCheckBoxSize = new javax.swing.JCheckBox();
        jCheckBoxExtension = new javax.swing.JCheckBox();
        jTextFieldExtensions = new javax.swing.JTextField();
        jButtonSizeHelp = new javax.swing.JButton();
        jButtonExtensionHelp = new javax.swing.JButton();
        jFormattedTextFieldSizeMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldSizeMax = new javax.swing.JFormattedTextField();
        jCheckBoxName = new javax.swing.JCheckBox();
        jComboBoxNameSearchType = new javax.swing.JComboBox();
        jTextFieldSearch = new javax.swing.JTextField();
        jButtonSearchHelp = new javax.swing.JButton();
        jTextFieldRegex = new javax.swing.JTextField();
        jPanelCleanup = new javax.swing.JPanel();
        jPanelDeleteFiles = new javax.swing.JPanel();
        jLabelDeleteFiles = new javax.swing.JLabel();
        jButtonDeleteAll = new javax.swing.JButton();
        jPanelRenameFiles = new javax.swing.JPanel();
        jLabelRenameFiles = new javax.swing.JLabel();
        jButtonRenameAll = new javax.swing.JButton();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNew = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemOpenProject = new javax.swing.JMenuItem();
        jMenuItemOpenFiles = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSave = new javax.swing.JMenuItem();
        jMenuItemSaveAs = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemExportFFmpeg = new javax.swing.JMenuItem();
        jMenuItemExportHandbrake = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItemAbout = new javax.swing.JMenuItem();

        jFileChooserInput.setDialogTitle("Select video files and folders");
        jFileChooserInput.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        jFileChooserInput.setMultiSelectionEnabled(true);

        jFileChooserProjectLoad.setDialogTitle("Select a vbp-Project");
        jFileChooserProjectLoad.setFileFilter(FileFilters.projectFilter());
        jFileChooserProjectLoad.setMultiSelectionEnabled(true);

        jFileChooserProjectSave.setDialogTitle("Save vbs-Project");
        jFileChooserProjectSave.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooserProjectSave.setFileFilter(FileFilters.projectFilter());

        jFrameAbout.setTitle("About VideoBatchProcessor");
        jFrameAbout.setAlwaysOnTop(true);
        jFrameAbout.setIconImage(getIcon());
        jFrameAbout.setMinimumSize(new java.awt.Dimension(540, 490));
        jFrameAbout.setLocationRelativeTo(null);

        jButtonAboutClose.setText("Close");
        jButtonAboutClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAboutCloseActionPerformed(evt);
            }
        });

        jButtonHomepage.setText("Homepage");
        jButtonHomepage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHomepageActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jEditorPaneAboutText.setBorder(null);
        jEditorPaneAboutText.setContentType("text/html");
        jEditorPaneAboutText.setEditable(false);
        jEditorPaneAboutText.setText("<html>\n<p>Video Batch Processor, version 0.1.1 (2011-10-09)<br />\ndeveloped by Sebastian Straub &lt;<a href=\"mailto:sebastian-straub@gmx.net\">sebastian-straub@gmx.net</a>&gt;</p>\n<p>... is a tool that allows you to batch process any video files on your storage devices according to your specific needs. Search for video files matching specific criteria (like name, extension, size) and batch-process them with the video converter of your choice. <a href=\"https://github.com/Klamann/Video-Batch-Processor#readme\">Read more...</a></p>\n<p>For updates, visit the <a href=\"https://github.com/Klamann/Video-Batch-Processor\">Project Homepage</a>.<br />\nFor further information, visit <a href=\"https://github.com/Klamann/Video-Batch-Processor/wiki\">the Wiki</a>.<br />\nFound any bugs? Please report them to the <a href=\"https://github.com/Klamann/Video-Batch-Processor/issues\">Issue Tracker</a>.</p>\n<p>Video Batch Processor is free software, licenced unter the <a href=\"https://www.gnu.org/licenses/gpl-3.0.html\">GPLv3</a>. The source code is hosted on <a href=\"https://github.com/Klamann/Video-Batch-Processor\">GitHub</a>.</p>\n<p>Please note that this is still an early release. It may contain serious bugs, so use it at your own risk. The functionality behind some buttons is not yet implemented, these are grayed-out. Have a lookout for upcoming releases, if you need them :)</p>\n</html>");
        jEditorPaneAboutText.setToolTipText("");
        jEditorPaneAboutText.setAutoscrolls(false);
        jEditorPaneAboutText.setOpaque(false);
        jEditorPaneAboutText.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                jEditorPaneAboutTextHyperlinkUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(jEditorPaneAboutText);
        jEditorPaneAboutText.getAccessibleContext().setAccessibleName("");

        jLabelAboutTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelAboutTitle.setText("<html><h2>Video Batch Processor</h2></html>");

        javax.swing.GroupLayout jFrameAboutLayout = new javax.swing.GroupLayout(jFrameAbout.getContentPane());
        jFrameAbout.getContentPane().setLayout(jFrameAboutLayout);
        jFrameAboutLayout.setHorizontalGroup(
            jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrameAboutLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addComponent(jLabelAboutTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addGroup(jFrameAboutLayout.createSequentialGroup()
                        .addComponent(jButtonHomepage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 378, Short.MAX_VALUE)
                        .addComponent(jButtonAboutClose)))
                .addContainerGap())
        );
        jFrameAboutLayout.setVerticalGroup(
            jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrameAboutLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelAboutTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonHomepage)
                    .addComponent(jButtonAboutClose))
                .addContainerGap())
        );

        jFileChooserOutput.setDialogTitle("Choose the output location for transcoded video files");
        jFileChooserOutput.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooserOutput.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Video Batch Processor");
        setIconImage(getIcon());
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(490, 560));
        setName("mainFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanelFileView.setBorder(javax.swing.BorderFactory.createTitledBorder("Files to transcode"));

        jListTranscode.setToolTipText("");
        jScrollPaneFileList.setViewportView(jListTranscode);

        jButtonUp.setText("↑");
        jButtonUp.setToolTipText("move selection up");
        jButtonUp.setActionCommand("up");
        jButtonUp.setEnabled(false);
        jButtonUp.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonUp.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonUp.setPreferredSize(new java.awt.Dimension(42, 23));

        jButtonDel.setText("x");
        jButtonDel.setToolTipText("remove selected file from list (mutiple selection possible)");
        jButtonDel.setEnabled(false);
        jButtonDel.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonDel.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonDel.setPreferredSize(new java.awt.Dimension(42, 23));
        jButtonDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDelActionPerformed(evt);
            }
        });

        jButtonDown.setText("↓");
        jButtonDown.setToolTipText("move selection down");
        jButtonDown.setActionCommand("down");
        jButtonDown.setEnabled(false);
        jButtonDown.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonDown.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonDown.setPreferredSize(new java.awt.Dimension(42, 23));
        jButtonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDownActionPerformed(evt);
            }
        });

        jButtonRescan.setText("Rescan");
        jButtonRescan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRescanActionPerformed(evt);
            }
        });

        jProgressBarScan.setPreferredSize(new java.awt.Dimension(146, 23));

        jButtonCopyToClipboard.setText("Copy to Clipboard");
        jButtonCopyToClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyToClipboardActionPerformed(evt);
            }
        });

        jButtonSaveListAs.setText("Save List as...");
        jButtonSaveListAs.setEnabled(false);

        jButtonClearTranscode.setText("Clear List");
        jButtonClearTranscode.setToolTipText("");
        jButtonClearTranscode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearTranscodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelFileViewLayout = new javax.swing.GroupLayout(jPanelFileView);
        jPanelFileView.setLayout(jPanelFileViewLayout);
        jPanelFileViewLayout.setHorizontalGroup(
            jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneFileList, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addGroup(jPanelFileViewLayout.createSequentialGroup()
                        .addComponent(jButtonClearTranscode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 162, Short.MAX_VALUE)
                        .addComponent(jButtonCopyToClipboard)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSaveListAs))
                    .addGroup(jPanelFileViewLayout.createSequentialGroup()
                        .addComponent(jProgressBarScan, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRescan)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonDel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanelFileViewLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonDel, jButtonDown, jButtonUp});

        jPanelFileViewLayout.setVerticalGroup(
            jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelFileViewLayout.createSequentialGroup()
                        .addComponent(jButtonUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11))
                    .addComponent(jScrollPaneFileList, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonSaveListAs)
                        .addComponent(jButtonCopyToClipboard))
                    .addComponent(jButtonClearTranscode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jProgressBarScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonRescan))
                .addContainerGap())
        );

        jPanelFileViewLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonDel, jButtonDown, jButtonUp});

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);

        jButtonLoadProject.setText("Load");
        jButtonLoadProject.setToolTipText("Load a BatchForHandbrake-Project");
        jButtonLoadProject.setFocusable(false);
        jButtonLoadProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonLoadProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonLoadProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadProjectActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonLoadProject);

        jButtonSaveProject.setText("Save");
        jButtonSaveProject.setToolTipText("Save the currently opened project");
        jButtonSaveProject.setFocusable(false);
        jButtonSaveProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSaveProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSaveProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveProjectActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonSaveProject);

        jButtonAddFiles.setText("Add Files");
        jButtonAddFiles.setToolTipText("Add (multiple) files or folders");
        jButtonAddFiles.setFocusable(false);
        jButtonAddFiles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonAddFiles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonAddFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFilesActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonAddFiles);
        jToolBar.add(jSeparator1);

        jButtonExportHandbrake.setText("Handbrake");
        jButtonExportHandbrake.setToolTipText("Export the current project to a Handbrake-Queue");
        jButtonExportHandbrake.setFocusable(false);
        jButtonExportHandbrake.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExportHandbrake.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExportHandbrake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportHandbrakeActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonExportHandbrake);

        jButtonExportFFmpeg.setText("FFmpeg");
        jButtonExportFFmpeg.setFocusable(false);
        jButtonExportFFmpeg.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExportFFmpeg.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExportFFmpeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportFFmpegActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonExportFFmpeg);
        jToolBar.add(jSeparator2);

        jButtonExit.setText("Exit");
        jButtonExit.setToolTipText("Leave the program");
        jButtonExit.setFocusable(false);
        jButtonExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonExit);

        jPanelSearch.setBorder(javax.swing.BorderFactory.createTitledBorder("Folders to Search"));

        jListInput.setToolTipText("Single video files and folders where video files will be searched in, according to your filter rules");
        jScrollPaneInput.setViewportView(jListInput);

        jButtonInputUp.setText("↑");
        jButtonInputUp.setToolTipText("move selection up");
        jButtonInputUp.setActionCommand("up");
        jButtonInputUp.setEnabled(false);
        jButtonInputUp.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonInputUp.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonInputUp.setPreferredSize(new java.awt.Dimension(42, 23));

        jButtonInputDel.setText("x");
        jButtonInputDel.setToolTipText("remove selected file from list (mutiple selection possible)");
        jButtonInputDel.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonInputDel.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonInputDel.setPreferredSize(new java.awt.Dimension(42, 23));
        jButtonInputDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInputDelActionPerformed(evt);
            }
        });

        jButtonInputDown.setText("↓");
        jButtonInputDown.setToolTipText("move selection down");
        jButtonInputDown.setActionCommand("down");
        jButtonInputDown.setEnabled(false);
        jButtonInputDown.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonInputDown.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonInputDown.setPreferredSize(new java.awt.Dimension(42, 23));
        jButtonInputDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInputDownActionPerformed(evt);
            }
        });

        jButtonInputBrowse.setText("Browse");
        jButtonInputBrowse.setToolTipText("Add (multiple) files or folders");
        jButtonInputBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInputBrowseActionPerformed(evt);
            }
        });

        jCheckBoxRecursive.setSelected(true);
        jCheckBoxRecursive.setText("recursive search");
        jCheckBoxRecursive.setToolTipText("Activate this, when subfolders should be crawled too");

        jButtonClearInput.setText("Clear List");
        jButtonClearInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearInputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelSearchLayout = new javax.swing.GroupLayout(jPanelSearch);
        jPanelSearch.setLayout(jPanelSearchLayout);
        jPanelSearchLayout.setHorizontalGroup(
            jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSearchLayout.createSequentialGroup()
                        .addComponent(jButtonClearInput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 189, Short.MAX_VALUE)
                        .addComponent(jCheckBoxRecursive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonInputBrowse)
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSearchLayout.createSequentialGroup()
                        .addComponent(jScrollPaneInput, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonInputUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonInputDel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonInputDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jPanelSearchLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonInputDel, jButtonInputDown, jButtonInputUp});

        jPanelSearchLayout.setVerticalGroup(
            jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelSearchLayout.createSequentialGroup()
                        .addComponent(jButtonInputUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonInputDel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonInputDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPaneInput, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonInputBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBoxRecursive))
                    .addComponent(jButtonClearInput))
                .addContainerGap())
        );

        jPanelSearchLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonInputDel, jButtonInputDown, jButtonInputUp});

        javax.swing.GroupLayout jPanelInputLayout = new javax.swing.GroupLayout(jPanelInput);
        jPanelInput.setLayout(jPanelInputLayout);
        jPanelInputLayout.setHorizontalGroup(
            jPanelInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelInputLayout.setVerticalGroup(
            jPanelInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Input", jPanelInput);

        outputTypeChooser.add(jRadioButtonSamePlace);
        jRadioButtonSamePlace.setSelected(true);
        jRadioButtonSamePlace.setText("Same Place");

        jLabelRenamePattern.setText("Rename Pattern:");

        jTextFieldRenamePattern.setText("{name}-conv");

        jButtonRenamePatternHelp.setText("?");

        outputTypeChooser.add(jRadioButtonDifferentFolder);
        jRadioButtonDifferentFolder.setText("Different Folder");

        jButtonOutputBrowse.setText("Browse");
        jButtonOutputBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOutputBrowseActionPerformed(evt);
            }
        });

        jCheckBoxPreserveFolders.setText("preserve folder structure");

        javax.swing.GroupLayout jPanelOutputLayout = new javax.swing.GroupLayout(jPanelOutput);
        jPanelOutput.setLayout(jPanelOutputLayout);
        jPanelOutputLayout.setHorizontalGroup(
            jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonDifferentFolder)
                    .addComponent(jRadioButtonSamePlace))
                .addGap(18, 18, 18)
                .addGroup(jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxPreserveFolders)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOutputLayout.createSequentialGroup()
                        .addGroup(jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelOutputLayout.createSequentialGroup()
                                .addComponent(jLabelRenamePattern)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldRenamePattern, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                            .addComponent(jTextFieldDifferentFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonRenamePatternHelp)
                            .addComponent(jButtonOutputBrowse))))
                .addContainerGap())
        );
        jPanelOutputLayout.setVerticalGroup(
            jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonSamePlace)
                    .addComponent(jTextFieldRenamePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRenamePattern)
                    .addComponent(jButtonRenamePatternHelp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonDifferentFolder)
                    .addComponent(jTextFieldDifferentFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonOutputBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxPreserveFolders)
                .addContainerGap(143, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Output", jPanelOutput);

        searchPatternChooser.add(jRadioButtonSelectProperties);
        jRadioButtonSelectProperties.setSelected(true);
        jRadioButtonSelectProperties.setText("Select by File Properties");

        searchPatternChooser.add(jRadioButtonSelectRegex);
        jRadioButtonSelectRegex.setText("Custom Regex");

        jPanelProperties.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelSizeFrom.setText("from");

        jLabelSizeTo.setText("MB to");

        jLabelSizeMb.setText("MB");

        jCheckBoxSize.setText("Size:");

        jCheckBoxExtension.setSelected(true);
        jCheckBoxExtension.setText("Extension:");

        jTextFieldExtensions.setText("3gp,flv,mov,qt,divx,mkv,asf,wmv,avi,mpg,mpeg,mp2,mp4,m4v,rm,ogg,ogv,yuv");
        jTextFieldExtensions.setToolTipText("accept only files with one of these extensions");

        jButtonSizeHelp.setText("?");
        jButtonSizeHelp.setEnabled(false);

        jButtonExtensionHelp.setText("?");
        jButtonExtensionHelp.setEnabled(false);

        jFormattedTextFieldSizeMin.setColumns(7);
        jFormattedTextFieldSizeMin.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jFormattedTextFieldSizeMin.setToolTipText("when activated, files with size lower than this will be ignored");
        jFormattedTextFieldSizeMin.setValue(Integer.valueOf(0));

        jFormattedTextFieldSizeMax.setColumns(7);
        jFormattedTextFieldSizeMax.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jFormattedTextFieldSizeMax.setToolTipText("when activated, files with size higher than this will be ignored");
        jFormattedTextFieldSizeMax.setValue(Integer.valueOf(1000000));

        jCheckBoxName.setText("Name:");
        jCheckBoxName.setEnabled(false);

        jComboBoxNameSearchType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "contains", "begins with", "ends with" }));

        jButtonSearchHelp.setText("?");
        jButtonSearchHelp.setEnabled(false);

        javax.swing.GroupLayout jPanelPropertiesLayout = new javax.swing.GroupLayout(jPanelProperties);
        jPanelProperties.setLayout(jPanelPropertiesLayout);
        jPanelPropertiesLayout.setHorizontalGroup(
            jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxExtension)
                    .addComponent(jCheckBoxSize)
                    .addComponent(jCheckBoxName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPropertiesLayout.createSequentialGroup()
                        .addComponent(jComboBoxNameSearchType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonSearchHelp))
                    .addGroup(jPanelPropertiesLayout.createSequentialGroup()
                        .addComponent(jLabelSizeFrom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldSizeMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelSizeTo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldSizeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelSizeMb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 177, Short.MAX_VALUE)
                        .addComponent(jButtonSizeHelp))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPropertiesLayout.createSequentialGroup()
                        .addComponent(jTextFieldExtensions, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonExtensionHelp)))
                .addContainerGap())
        );
        jPanelPropertiesLayout.setVerticalGroup(
            jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxSize)
                    .addComponent(jLabelSizeFrom)
                    .addComponent(jButtonSizeHelp)
                    .addComponent(jFormattedTextFieldSizeMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSizeTo)
                    .addComponent(jFormattedTextFieldSizeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSizeMb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxExtension)
                    .addComponent(jTextFieldExtensions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExtensionHelp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxNameSearchType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSearchHelp))
                    .addComponent(jCheckBoxName))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextFieldRegex.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        jTextFieldRegex.setText(".*(\\.(avi|mkv|mp4))");

        javax.swing.GroupLayout jPanelSearchPatternLayout = new javax.swing.GroupLayout(jPanelSearchPattern);
        jPanelSearchPattern.setLayout(jPanelSearchPatternLayout);
        jPanelSearchPatternLayout.setHorizontalGroup(
            jPanelSearchPatternLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSearchPatternLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSearchPatternLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldRegex, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addComponent(jRadioButtonSelectRegex)
                    .addComponent(jPanelProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButtonSelectProperties))
                .addContainerGap())
        );
        jPanelSearchPatternLayout.setVerticalGroup(
            jPanelSearchPatternLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSearchPatternLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonSelectProperties)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonSelectRegex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldRegex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Search Pattern", jPanelSearchPattern);

        jPanelDeleteFiles.setBorder(javax.swing.BorderFactory.createTitledBorder("Delete Files"));

        jLabelDeleteFiles.setText("<html>Delete all files currently in the list. This can be useful if you already have transcoded all files and want to get rid of oversized originals.</html>");
        jLabelDeleteFiles.setAutoscrolls(true);

        jButtonDeleteAll.setText("Delete all original files");
        jButtonDeleteAll.setEnabled(false);
        jButtonDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDeleteFilesLayout = new javax.swing.GroupLayout(jPanelDeleteFiles);
        jPanelDeleteFiles.setLayout(jPanelDeleteFilesLayout);
        jPanelDeleteFilesLayout.setHorizontalGroup(
            jPanelDeleteFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDeleteFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDeleteFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDeleteFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(jButtonDeleteAll))
                .addContainerGap())
        );
        jPanelDeleteFilesLayout.setVerticalGroup(
            jPanelDeleteFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDeleteFilesLayout.createSequentialGroup()
                .addComponent(jLabelDeleteFiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteAll)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelRenameFiles.setBorder(javax.swing.BorderFactory.createTitledBorder("Rename Files"));

        jLabelRenameFiles.setText("<html>Rename all transcoded files back to the original file names, after the original files have been deleted (see above)</html>");

        jButtonRenameAll.setText("Rename all transcoded files");
        jButtonRenameAll.setEnabled(false);
        jButtonRenameAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelRenameFilesLayout = new javax.swing.GroupLayout(jPanelRenameFiles);
        jPanelRenameFiles.setLayout(jPanelRenameFilesLayout);
        jPanelRenameFilesLayout.setHorizontalGroup(
            jPanelRenameFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRenameFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRenameFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelRenameFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(jButtonRenameAll))
                .addContainerGap())
        );
        jPanelRenameFilesLayout.setVerticalGroup(
            jPanelRenameFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRenameFilesLayout.createSequentialGroup()
                .addComponent(jLabelRenameFiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRenameAll)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelCleanupLayout = new javax.swing.GroupLayout(jPanelCleanup);
        jPanelCleanup.setLayout(jPanelCleanupLayout);
        jPanelCleanupLayout.setHorizontalGroup(
            jPanelCleanupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCleanupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCleanupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelRenameFiles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelDeleteFiles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelCleanupLayout.setVerticalGroup(
            jPanelCleanupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCleanupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelDeleteFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelRenameFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Cleanup", jPanelCleanup);

        jMenuFile.setText("File");

        jMenuItemNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemNew.setText("New");
        jMenuItemNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemNew);
        jMenuFile.add(jSeparator3);

        jMenuItemOpenProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemOpenProject.setText("Open Project...");
        jMenuItemOpenProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenProjectActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOpenProject);

        jMenuItemOpenFiles.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemOpenFiles.setText("Open Files...");
        jMenuItemOpenFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenFilesActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOpenFiles);
        jMenuFile.add(jSeparator4);

        jMenuItemSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSave.setText("Save");
        jMenuItemSave.setEnabled(false);
        jMenuFile.add(jMenuItemSave);

        jMenuItemSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSaveAs.setText("Save as...");
        jMenuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAsActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveAs);
        jMenuFile.add(jSeparator5);

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenu1.setText("Export");

        jMenuItemExportFFmpeg.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemExportFFmpeg.setText("FFmpeg");
        jMenu1.add(jMenuItemExportFFmpeg);

        jMenuItemExportHandbrake.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemExportHandbrake.setText("Handbrake");
        jMenuItemExportHandbrake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportHandbrakeActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportHandbrake);

        jMenuBar.add(jMenu1);

        jMenuHelp.setText("Help");

        jMenuItemHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemHelp.setText("Help (Online)");
        jMenuItemHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelp);
        jMenuHelp.add(jSeparator6);

        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelFileView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPaneSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelFileView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPaneSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleDescription("Main Application Window");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold desc="Custom WindowMain Contents">
//    jButtonRenamePatternHelp
//    BalloonTipStyle edgedLook = new EdgedBalloonStyle(Color.WHITE, Color.BLUE);
//    BalloonTip myBalloonTip = new BalloonTip(jButtonRenamePatternHelp, "Hello world!", edgedLook, false);
    private void initCustomComponents() {
        initBalloonTips();
    }
    protected String tipRenamePattern = "<html>Define your rename pattern: This is how your transcoded files will be named.<br>"
            + "You can use a static name for all files or keep the original name and add something to it,<br>"
            + " using the <code>{name}</code>-placeholder.<br>"
            + "<br>Example:<br>Apply Pattern <code>{name}-compressed</code> on the input file named <code>CamVideo.avi</code><br>"
            + "The resulting file name will be <code>CamVideo-compressed.mp4</code><br>"
            + "(note that the file extension changes according to your transcoding settings)<br>"
            + "<br>Hints:<br>"
            + "<ul><li>be careful using only <code>{name}</code> as rename pattern. The encoder might delete your original<br> file without transcoding it first!</li>"
            + "<li>when using static names, multiple transcoded files in a single folder may overwritte each other.</li>"
            + "<li><b>always backup your original files! I can not guarantee for any loss of data!</b></li></ul></html>";
    protected String tipRenamePatternShort = "<html>Input: <code>file.avi</code><br>Your Pattern: <code>{name}-compressed</code>"
            + "<br>Result: <code>file-compressed.avi</code><br>Hover over the ? to the right for more information.</html>";

    private void initBalloonTips() {

        setToolTip(jButtonRenamePatternHelp, tipRenamePattern, 0, 300000);
        setToolTip(jTextFieldRenamePattern, tipRenamePatternShort);

    }

    /**
     * Set a tooltip
     * @param comp		sets a tooltip for this component
     * @param text		the contents of the tooltip (you may use html)
     */
    public static void setToolTip(final javax.swing.JComponent comp, final String text) {
        setToolTip(comp, text, 500, 10000);
    }

    /**
     * Set a tooltip
     * @param comp		sets a tooltip for this component
     * @param text		the contents of the tooltip (you may use html)
     * @param initDelay         time in ms until popup shows up
     * @param showDelay         time in ms until it disappears
     */
    public static void setToolTip(final javax.swing.JComponent comp, final String text, int initDelay, int showDelay) {
        BalloonTipStyle style = createBalloonTipStyle();
        final BalloonTip balloon = new BalloonTip(comp, new javax.swing.JLabel(text), style, BalloonTip.Orientation.LEFT_ABOVE, BalloonTip.AttachLocation.ALIGNED, 15, 10, false);
        balloon.addDefaultMouseListener(false);
        ToolTipUtils.balloonToToolTip(balloon, initDelay, showDelay);
    }

    /**
     * Retrieve an instance of the balloon tip style to be used throughout the application
     * @return	the balloon tip style
     */
    public static BalloonTipStyle createBalloonTipStyle() {
        return new EdgedBalloonStyle(new Color(255, 253, 245), new Color(64, 64, 64));
    }

    /**
     * Retrieve an instance of the balloon tip positioner to be used throughout the application
     * @return	the balloon tip positioner
     */
    public static BalloonTipPositioner createBalloonTipPositioner() {
        return new LeftAbovePositioner(15, 10);
    }

    // </editor-fold>
    // <editor-fold desc="Swing-Events">
private void jButtonDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDelActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonDelActionPerformed

private void jButtonDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDownActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonDownActionPerformed

private void jButtonClearInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearInputActionPerformed
    model.clearInputFiles();
}//GEN-LAST:event_jButtonClearInputActionPerformed

private void jButtonInputDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInputDelActionPerformed
    removeInput();
}//GEN-LAST:event_jButtonInputDelActionPerformed

private void jButtonInputDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInputDownActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonInputDownActionPerformed

private void jButtonInputBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInputBrowseActionPerformed
    browseInput();
}//GEN-LAST:event_jButtonInputBrowseActionPerformed

private void jButtonOutputBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOutputBrowseActionPerformed
    browseOutput();
}//GEN-LAST:event_jButtonOutputBrowseActionPerformed

private void jButtonDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonDeleteAllActionPerformed

private void jButtonRenameAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameAllActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonRenameAllActionPerformed

private void jButtonSaveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveProjectActionPerformed
    gui.saveProject();
}//GEN-LAST:event_jButtonSaveProjectActionPerformed

private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
    safeExit();
}//GEN-LAST:event_jMenuItemExitActionPerformed

private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewActionPerformed
    gui.loadDefaults();
}//GEN-LAST:event_jMenuItemNewActionPerformed

    private void jButtonCopyToClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyToClipboardActionPerformed
        model.copyToClipboard();
    }//GEN-LAST:event_jButtonCopyToClipboardActionPerformed

    private void jButtonExportHandbrakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportHandbrakeActionPerformed
        gui.popupWindowHandbrake();
    }//GEN-LAST:event_jButtonExportHandbrakeActionPerformed

    private void jButtonClearTranscodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearTranscodeActionPerformed
        model.clearFilesToTranscode();
    }//GEN-LAST:event_jButtonClearTranscodeActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        safeExit();
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        jFrameAbout.setVisible(true);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jButtonRescanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRescanActionPerformed
        rescan();
    }//GEN-LAST:event_jButtonRescanActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        safeExit();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonAddFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFilesActionPerformed
        browseInput();
    }//GEN-LAST:event_jButtonAddFilesActionPerformed

    private void jMenuItemOpenFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenFilesActionPerformed
        browseInput();
    }//GEN-LAST:event_jMenuItemOpenFilesActionPerformed

    private void jButtonLoadProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadProjectActionPerformed
        gui.loadProject();
    }//GEN-LAST:event_jButtonLoadProjectActionPerformed

    private void jMenuItemOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenProjectActionPerformed
        gui.loadProject();
    }//GEN-LAST:event_jMenuItemOpenProjectActionPerformed

    private void jMenuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAsActionPerformed
        gui.saveProject();
    }//GEN-LAST:event_jMenuItemSaveAsActionPerformed

    private void jButtonAboutCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAboutCloseActionPerformed
        jFrameAbout.setVisible(false);
    }//GEN-LAST:event_jButtonAboutCloseActionPerformed

    private void jButtonHomepageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHomepageActionPerformed
        new ThreadedExecutor() {

            @Override
            public void execute() {
                openWeb("https://github.com/Klamann/Video-Batch-Processor");
            }
        }.start();
    }//GEN-LAST:event_jButtonHomepageActionPerformed

    private void jEditorPaneAboutTextHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_jEditorPaneAboutTextHyperlinkUpdate
        if (javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.equals(evt.getEventType())) {
            try {
                openWeb(evt.getURL().toURI());
            } catch (URISyntaxException ex) {
                Logger.getLogger(WindowMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jEditorPaneAboutTextHyperlinkUpdate

    private void jMenuItemHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpActionPerformed
        new ThreadedExecutor() {

            @Override
            public void execute() {
                openWeb("https://github.com/Klamann/Video-Batch-Processor/wiki");
            }
        }.start();
    }//GEN-LAST:event_jMenuItemHelpActionPerformed

    private void jMenuItemExportHandbrakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportHandbrakeActionPerformed
        gui.popupWindowHandbrake();
    }//GEN-LAST:event_jMenuItemExportHandbrakeActionPerformed

    private void jButtonExportFFmpegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportFFmpegActionPerformed
        gui.popupWindowFFmpeg();
    }//GEN-LAST:event_jButtonExportFFmpegActionPerformed
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables declaration - generated by WindowMain builder">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAboutClose;
    private javax.swing.JButton jButtonAddFiles;
    private javax.swing.JButton jButtonClearInput;
    private javax.swing.JButton jButtonClearTranscode;
    private javax.swing.JButton jButtonCopyToClipboard;
    private javax.swing.JButton jButtonDel;
    private javax.swing.JButton jButtonDeleteAll;
    private javax.swing.JButton jButtonDown;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonExportFFmpeg;
    private javax.swing.JButton jButtonExportHandbrake;
    private javax.swing.JButton jButtonExtensionHelp;
    private javax.swing.JButton jButtonHomepage;
    private javax.swing.JButton jButtonInputBrowse;
    private javax.swing.JButton jButtonInputDel;
    private javax.swing.JButton jButtonInputDown;
    private javax.swing.JButton jButtonInputUp;
    private javax.swing.JButton jButtonLoadProject;
    private javax.swing.JButton jButtonOutputBrowse;
    private javax.swing.JButton jButtonRenameAll;
    private javax.swing.JButton jButtonRenamePatternHelp;
    private javax.swing.JButton jButtonRescan;
    private javax.swing.JButton jButtonSaveListAs;
    private javax.swing.JButton jButtonSaveProject;
    private javax.swing.JButton jButtonSearchHelp;
    private javax.swing.JButton jButtonSizeHelp;
    private javax.swing.JButton jButtonUp;
    private javax.swing.JCheckBox jCheckBoxExtension;
    private javax.swing.JCheckBox jCheckBoxName;
    private javax.swing.JCheckBox jCheckBoxPreserveFolders;
    private javax.swing.JCheckBox jCheckBoxRecursive;
    private javax.swing.JCheckBox jCheckBoxSize;
    private javax.swing.JComboBox jComboBoxNameSearchType;
    private javax.swing.JEditorPane jEditorPaneAboutText;
    private javax.swing.JFileChooser jFileChooserInput;
    private javax.swing.JFileChooser jFileChooserOutput;
    protected javax.swing.JFileChooser jFileChooserProjectLoad;
    protected javax.swing.JFileChooser jFileChooserProjectSave;
    private javax.swing.JFormattedTextField jFormattedTextFieldSizeMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldSizeMin;
    private javax.swing.JFrame jFrameAbout;
    private javax.swing.JLabel jLabelAboutTitle;
    private javax.swing.JLabel jLabelDeleteFiles;
    private javax.swing.JLabel jLabelRenameFiles;
    private javax.swing.JLabel jLabelRenamePattern;
    private javax.swing.JLabel jLabelSizeFrom;
    private javax.swing.JLabel jLabelSizeMb;
    private javax.swing.JLabel jLabelSizeTo;
    private javax.swing.JList jListInput;
    private javax.swing.DefaultListModel<String> listModelInput;
    private javax.swing.JList jListTranscode;
    private javax.swing.DefaultListModel<String> listModelTranscode;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemExportFFmpeg;
    private javax.swing.JMenuItem jMenuItemExportHandbrake;
    private javax.swing.JMenuItem jMenuItemHelp;
    private javax.swing.JMenuItem jMenuItemNew;
    private javax.swing.JMenuItem jMenuItemOpenFiles;
    private javax.swing.JMenuItem jMenuItemOpenProject;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JMenuItem jMenuItemSaveAs;
    private javax.swing.JPanel jPanelCleanup;
    private javax.swing.JPanel jPanelDeleteFiles;
    protected javax.swing.JPanel jPanelFileView;
    private javax.swing.JPanel jPanelInput;
    private javax.swing.JPanel jPanelOutput;
    private javax.swing.JPanel jPanelProperties;
    private javax.swing.JPanel jPanelRenameFiles;
    private javax.swing.JPanel jPanelSearch;
    private javax.swing.JPanel jPanelSearchPattern;
    private javax.swing.JProgressBar jProgressBarScan;
    private javax.swing.JRadioButton jRadioButtonDifferentFolder;
    private javax.swing.JRadioButton jRadioButtonSamePlace;
    private javax.swing.JRadioButton jRadioButtonSelectProperties;
    private javax.swing.JRadioButton jRadioButtonSelectRegex;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneFileList;
    private javax.swing.JScrollPane jScrollPaneInput;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JTabbedPane jTabbedPaneSettings;
    private javax.swing.JTextField jTextFieldDifferentFolder;
    private javax.swing.JTextField jTextFieldExtensions;
    private javax.swing.JTextField jTextFieldRegex;
    private javax.swing.JTextField jTextFieldRenamePattern;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JToolBar jToolBar;
    private javax.swing.ButtonGroup outputTypeChooser;
    private javax.swing.ButtonGroup searchPatternChooser;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
