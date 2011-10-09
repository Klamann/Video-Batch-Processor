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
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.positioners.LeftAbovePositioner;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.EdgedBalloonStyle;
import net.java.balloontip.utils.ToolTipUtils;
import sebi.util.observer.ObserverArgs;
import sebi.util.threads.ThreadedExecutor;

/**
 * The main gui of the application. Built with Netbeans (about 3/4 of the code lines
 * in this class are generated)
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class GUI extends javax.swing.JFrame {

    protected Model model;
    
    /**
     * GUI-Constructor. Creates a new Instance of the main GUI.
     * @param model 
     */
    public GUI(Model model) {
        this.model = model;

        setLookAndFeel();
        
        initComponents();
        initCustomComponents();
        initEvents();
        setInitialValues();
    }

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

        // encoding
        jTextPaneHandbrakeQuery.setText(model.getHandBrakeQuery());
    }
    
    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @return the icon image for the application
     */
    private Image getIcon() {
        URL imageURL = getClass().getResource("/vbp/assets/icons/video.png");
        return new ImageIcon(imageURL).getImage();
    }

    // <editor-fold desc="Shared Actions">
    
    protected void safeExit() {
        updateModelValues();
        model.safeExit();
    }

    protected void updateInputFiles() {
        List<String> files = model.getInputFiles();
        listModelInput.clear();
        for (String file : files) {
            listModelInput.addElement(file);
        }
    }

    protected void updateTranscodeFiles() {
        List<String> files = model.getFilesToTranscode();
        listModelTranscode.clear();
        for (String file : files) {
            listModelTranscode.addElement(file);
        }
    }

    protected void exportToHandbrake() {
        new ThreadedExecutor() {

            @Override
            public void execute() {
                updateModelValues();
                model.exportToHandbrake(jFileChooserExportHandbrake);
            }
        }.start();
    }

    protected void updateModelValues() {
        
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
        
        // encoding
        model.setHandBrakeQuery(jTextPaneHandbrakeQuery.getText());
    }
    
    protected void updateGuiValues() {
        setInitialValues();
        updateInputFiles();
        updateTranscodeFiles();
    }

    protected void browseInput() {
        if (jFileChooserInput.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            new ThreadedExecutor() {

                @Override
                public void execute() {
                    updateModelValues();
                    model.addInputFiles(jFileChooserInput.getSelectedFiles());
                }
            }.start();
        }
    }

    protected void loadDefaults() {
        // run on swing thread!
        model.loadDefaults();
        updateGuiValues();
    }

    protected void loadProject() {
        // run on swing thread!
        model.loadProject(jFileChooserProjectLoad);
        updateGuiValues();
    }
    
    protected void saveProject() {
        new ThreadedExecutor() {
            @Override
            public void execute() {
                updateModelValues();
                model.saveProject(jFileChooserProjectSave);
            }
        }.start();
    }
    
    private static void openWeb(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void openWeb(String uri) {
        try {
            openWeb(new URI(uri));
        } catch (URISyntaxException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    // </editor-fold>
    
    // <editor-fold desc="Model-Events">
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
        jFileChooserExportHandbrake = new javax.swing.JFileChooser();
        jFileChooserProjectLoad = new javax.swing.JFileChooser();
        jFileChooserProjectSave = new javax.swing.JFileChooser();
        jFrameAbout = new javax.swing.JFrame();
        jButtonAboutClose = new javax.swing.JButton();
        jButtonHomepage = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPaneAboutText = new javax.swing.JEditorPane();
        jLabelAboutTitle = new javax.swing.JLabel();
        jPanelFileView = new javax.swing.JPanel();
        jScrollPaneFileList = new javax.swing.JScrollPane();
        listModelTranscode = new javax.swing.DefaultListModel<String>();
        jListTranscode = new javax.swing.JList(listModelTranscode);
        jButtonUp = new javax.swing.JButton();
        jButtonDel = new javax.swing.JButton();
        jButtonDown = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButtonClearTranscode = new javax.swing.JButton();
        jToolBar = new javax.swing.JToolBar();
        jButtonLoadProject = new javax.swing.JButton();
        jButtonAddFiles = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonSaveProject = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonExit = new javax.swing.JButton();
        jTabbedPaneSettings = new javax.swing.JTabbedPane();
        jPanelInput = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
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
        jLabel1 = new javax.swing.JLabel();
        jTextFieldRenamePattern = new javax.swing.JTextField();
        jButtonRenamePatternHelp = new javax.swing.JButton();
        jRadioButtonDifferentFolder = new javax.swing.JRadioButton();
        jTextFieldDifferentFolder = new javax.swing.JTextField();
        jButtonOutputBrowse = new javax.swing.JButton();
        jCheckBoxPreserveFolders = new javax.swing.JCheckBox();
        jPanelSearchPattern = new javax.swing.JPanel();
        jRadioButtonSelectProperties = new javax.swing.JRadioButton();
        jRadioButtonSelectRegex = new javax.swing.JRadioButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxSize = new javax.swing.JCheckBox();
        jCheckBoxExtension = new javax.swing.JCheckBox();
        jTextFieldExtensions = new javax.swing.JTextField();
        jButtonSizeHelp = new javax.swing.JButton();
        jButtonExtensionHelp = new javax.swing.JButton();
        jFormattedTextFieldSizeMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldSizeMax = new javax.swing.JFormattedTextField();
        jTextFieldRegex = new javax.swing.JTextField();
        jPanelEncoding = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jButtonEncodingHelp = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPaneHandbrakeQuery = new javax.swing.JTextPane();
        jPanelCleanup = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jButtonDeleteAll = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
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
        jMenuExport = new javax.swing.JMenu();
        jMenuItemHandbrake = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItemAbout = new javax.swing.JMenuItem();

        jFileChooserInput.setDialogTitle("Select video files and folders");
        jFileChooserInput.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        jFileChooserInput.setMultiSelectionEnabled(true);

        jFileChooserExportHandbrake.setDialogTitle("Save as Handbrake-Queue");
        jFileChooserExportHandbrake.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        jFileChooserProjectLoad.setDialogTitle("Select a vbp-Project");
        jFileChooserProjectLoad.setMultiSelectionEnabled(true);

        jFileChooserProjectSave.setDialogTitle("Save vbs-Project");
        jFileChooserProjectSave.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

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

        jButtonHomepage.setBackground(new java.awt.Color(255, 255, 255));
        jButtonHomepage.setText("Homepage");
        jButtonHomepage.setBorderPainted(false);
        jButtonHomepage.setOpaque(false);
        jButtonHomepage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHomepageActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jEditorPaneAboutText.setBorder(null);
        jEditorPaneAboutText.setContentType("text/html");
        jEditorPaneAboutText.setEditable(false);
        jEditorPaneAboutText.setText("<html>\n<p>Video Batch Processor, version 0.1.1 (2011-10-09)<br />\ndeveloped by Sebastian Straub &lt;<a href=\"mailto:sebastian-straub@gmx.net\">sebastian-straub@gmx.net</a>&gt;</p>\n<p>... is a tool that allows you to batch process any video files on your storage devices according to your specific needs. Search for video files matching specific criteria (like name, extension, size) and batch-process them with the video converter of your choice. <a href=\"https://github.com/Klamann/Video-Batch-Processor#readme\">Read more...</a></p>\n<p>For updates, visit the <a href=\"https://github.com/Klamann/Video-Batch-Processor\">Project Homepage</a>.<br />\nFor further information, visit <a href=\"https://github.com/Klamann/Video-Batch-Processor/wiki\">the Wiki</a>.<br />\nFound any bugs? Please report them to the <a href=\"https://github.com/Klamann/Video-Batch-Processor/issues\">Issue Tracker</a>.</p>\n<p>Video Batch Processor is free software, licenced unter the <a href=\"https://www.gnu.org/licenses/gpl-3.0.html\">GPLv3</a>. The source code is hosted on <a href=\"https://github.com/Klamann/Video-Batch-Processor\">GitHub</a>.</p>\n<p>Please note that this is still an early release. It may contain serious bugs, so use it at your own risk. The buttons to functions that are not yet activated are drawn with grey text, so don't mind if nothing happens when you click on them ;D</p>\n</html>");
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Video Batch Processor");
        setIconImage(getIcon());
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

        jButtonUp.setForeground(new java.awt.Color(153, 153, 153));
        jButtonUp.setText("↑");
        jButtonUp.setToolTipText("");
        jButtonUp.setActionCommand("up");
        jButtonUp.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonUp.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonUp.setPreferredSize(new java.awt.Dimension(42, 23));

        jButtonDel.setForeground(new java.awt.Color(153, 153, 153));
        jButtonDel.setText("x");
        jButtonDel.setToolTipText("delete selected file (mutiple selection possible)");
        jButtonDel.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonDel.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonDel.setPreferredSize(new java.awt.Dimension(42, 23));
        jButtonDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDelActionPerformed(evt);
            }
        });

        jButtonDown.setForeground(new java.awt.Color(153, 153, 153));
        jButtonDown.setText("↓");
        jButtonDown.setActionCommand("down");
        jButtonDown.setMaximumSize(new java.awt.Dimension(42, 23));
        jButtonDown.setMinimumSize(new java.awt.Dimension(42, 23));
        jButtonDown.setPreferredSize(new java.awt.Dimension(42, 23));
        jButtonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDownActionPerformed(evt);
            }
        });

        jButton1.setText("Rescan");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jProgressBar1.setPreferredSize(new java.awt.Dimension(146, 23));

        jButton2.setForeground(new java.awt.Color(153, 153, 153));
        jButton2.setText("Copy to Clipboard");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setForeground(new java.awt.Color(153, 153, 153));
        jButton3.setText("Save List as...");

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
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3))
                    .addGroup(jPanelFileViewLayout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
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
                        .addComponent(jButton3)
                        .addComponent(jButton2))
                    .addComponent(jButtonClearTranscode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFileViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
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

        jButtonAddFiles.setText("Add");
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

        jButtonExport.setText("Export");
        jButtonExport.setToolTipText("Export the current project to a Handbrake-Queue");
        jButtonExport.setFocusable(false);
        jButtonExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonExport);
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

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Folders to Search"));

        jListInput.setToolTipText("Single video files and folders where video files will be searched in, according to your filter rules");
        jScrollPane2.setViewportView(jListInput);

        jButtonInputUp.setForeground(new java.awt.Color(153, 153, 153));
        jButtonInputUp.setText("↑");
        jButtonInputUp.setActionCommand("up");

        jButtonInputDel.setText("x");
        jButtonInputDel.setToolTipText("delete selected file (mutiple selection possible)");
        jButtonInputDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInputDelActionPerformed(evt);
            }
        });

        jButtonInputDown.setForeground(new java.awt.Color(153, 153, 153));
        jButtonInputDown.setText("↓");
        jButtonInputDown.setActionCommand("down");
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

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButtonClearInput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 192, Short.MAX_VALUE)
                        .addComponent(jCheckBoxRecursive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonInputBrowse))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonInputUp)
                    .addComponent(jButtonInputDown)
                    .addComponent(jButtonInputDel))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonInputDel, jButtonInputDown, jButtonInputUp});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButtonInputUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonInputDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonInputDown))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonInputBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBoxRecursive))
                    .addComponent(jButtonClearInput))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonInputDel, jButtonInputDown, jButtonInputUp});

        javax.swing.GroupLayout jPanelInputLayout = new javax.swing.GroupLayout(jPanelInput);
        jPanelInput.setLayout(jPanelInputLayout);
        jPanelInputLayout.setHorizontalGroup(
            jPanelInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelInputLayout.setVerticalGroup(
            jPanelInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Input", jPanelInput);

        outputTypeChooser.add(jRadioButtonSamePlace);
        jRadioButtonSamePlace.setSelected(true);
        jRadioButtonSamePlace.setText("Same Place");

        jLabel1.setText("Rename Pattern:");

        jTextFieldRenamePattern.setText("{name}-conv");

        jButtonRenamePatternHelp.setText("?");

        outputTypeChooser.add(jRadioButtonDifferentFolder);
        jRadioButtonDifferentFolder.setForeground(new java.awt.Color(153, 153, 153));
        jRadioButtonDifferentFolder.setText("Different Folder");
        jRadioButtonDifferentFolder.setEnabled(false);

        jButtonOutputBrowse.setForeground(new java.awt.Color(153, 153, 153));
        jButtonOutputBrowse.setText("Browse");
        jButtonOutputBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOutputBrowseActionPerformed(evt);
            }
        });

        jCheckBoxPreserveFolders.setForeground(new java.awt.Color(153, 153, 153));
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
                                .addComponent(jLabel1)
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
                    .addComponent(jLabel1)
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
        jRadioButtonSelectRegex.setForeground(new java.awt.Color(153, 153, 153));
        jRadioButtonSelectRegex.setText("Custom Regex");
        jRadioButtonSelectRegex.setEnabled(false);

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("from");

        jLabel3.setText("MB to");

        jLabel4.setText("MB");

        jCheckBoxSize.setText("Size:");

        jCheckBoxExtension.setSelected(true);
        jCheckBoxExtension.setText("Extension:");

        jTextFieldExtensions.setText("3gp,flv,mov,qt,divx,mkv,asf,wmv,avi,mpg,mpeg,mp2,mp4,m4v,rm,ogg,ogv,yuv");
        jTextFieldExtensions.setToolTipText("accept only files with one of these extensions");

        jButtonSizeHelp.setForeground(new java.awt.Color(153, 153, 153));
        jButtonSizeHelp.setText("?");

        jButtonExtensionHelp.setForeground(new java.awt.Color(153, 153, 153));
        jButtonExtensionHelp.setText("?");

        jFormattedTextFieldSizeMin.setColumns(7);
        jFormattedTextFieldSizeMin.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jFormattedTextFieldSizeMin.setToolTipText("when activated, files with size lower than this will be ignored");
        jFormattedTextFieldSizeMin.setValue(Integer.valueOf(0));

        jFormattedTextFieldSizeMax.setColumns(7);
        jFormattedTextFieldSizeMax.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jFormattedTextFieldSizeMax.setToolTipText("when activated, files with size higher than this will be ignored");
        jFormattedTextFieldSizeMax.setValue(Integer.valueOf(1000000));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxExtension)
                    .addComponent(jCheckBoxSize))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldSizeMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldSizeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 177, Short.MAX_VALUE)
                        .addComponent(jButtonSizeHelp))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jTextFieldExtensions, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonExtensionHelp)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxSize)
                    .addComponent(jLabel2)
                    .addComponent(jButtonSizeHelp)
                    .addComponent(jFormattedTextFieldSizeMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jFormattedTextFieldSizeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxExtension)
                    .addComponent(jTextFieldExtensions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExtensionHelp))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextFieldRegex.setFont(new java.awt.Font("Courier New", 0, 12));
        jTextFieldRegex.setText(".*(\\.(avi|mkv|mp4))");
        jTextFieldRegex.setPreferredSize(new java.awt.Dimension(139, 22));

        javax.swing.GroupLayout jPanelSearchPatternLayout = new javax.swing.GroupLayout(jPanelSearchPattern);
        jPanelSearchPattern.setLayout(jPanelSearchPatternLayout);
        jPanelSearchPatternLayout.setHorizontalGroup(
            jPanelSearchPatternLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSearchPatternLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSearchPatternLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldRegex, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addComponent(jRadioButtonSelectRegex, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButtonSelectProperties, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanelSearchPatternLayout.setVerticalGroup(
            jPanelSearchPatternLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSearchPatternLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonSelectProperties)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonSelectRegex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldRegex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Search Pattern", jPanelSearchPattern);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Enter Handbrake-Query"));

        jLabel7.setText("<html>Enter your preferred transcoding-settings in Handbrake, generate a query for these and paste them in the textarea below. All files will be encoded with these settings</html>");

        jButtonEncodingHelp.setForeground(new java.awt.Color(153, 153, 153));
        jButtonEncodingHelp.setText("?");
        jButtonEncodingHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEncodingHelpActionPerformed(evt);
            }
        });

        jTextPaneHandbrakeQuery.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextPaneHandbrakeQuery.setText(" -o \"\"  -f mkv --strict-anamorphic  -e x264 -q 25 -a 1 -E lame -6 dpl2 -R Auto -B 128 -D 0.0 -x ref=2:bframes=2:subq=6:mixed-refs=0:weightb=0:8x8dct=0:trellis=0 --verbose=1");
        jScrollPane3.setViewportView(jTextPaneHandbrakeQuery);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonEncodingHelp)))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonEncodingHelp)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelEncodingLayout = new javax.swing.GroupLayout(jPanelEncoding);
        jPanelEncoding.setLayout(jPanelEncodingLayout);
        jPanelEncodingLayout.setHorizontalGroup(
            jPanelEncodingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEncodingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelEncodingLayout.setVerticalGroup(
            jPanelEncodingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEncodingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
        );

        jTabbedPaneSettings.addTab("Encoding", jPanelEncoding);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Delete Files"));

        jLabel5.setText("<html>Delete all files currently in the list. This can be useful if you already have transcoded all files and want to get rid of oversized originals.</html>");
        jLabel5.setAutoscrolls(true);

        jButtonDeleteAll.setForeground(new java.awt.Color(153, 153, 153));
        jButtonDeleteAll.setText("Delete all original files");
        jButtonDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(jButtonDeleteAll))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAll)
                .addContainerGap())
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Rename Files"));

        jLabel6.setText("<html>Rename all transcoded files back to the original file names, after the original files have been deleted (see above)</html>");

        jButtonRenameAll.setForeground(new java.awt.Color(153, 153, 153));
        jButtonRenameAll.setText("Rename all transcoded files");
        jButtonRenameAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(jButtonRenameAll))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonRenameAll)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelCleanupLayout = new javax.swing.GroupLayout(jPanelCleanup);
        jPanelCleanup.setLayout(jPanelCleanupLayout);
        jPanelCleanupLayout.setHorizontalGroup(
            jPanelCleanupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCleanupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCleanupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelCleanupLayout.setVerticalGroup(
            jPanelCleanupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCleanupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
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
        jMenuItemSave.setForeground(new java.awt.Color(153, 153, 153));
        jMenuItemSave.setText("Save");
        jMenuFile.add(jMenuItemSave);

        jMenuItemSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSaveAs.setText("Save as...");
        jMenuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAsActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveAs);

        jMenuExport.setText("Export");

        jMenuItemHandbrake.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemHandbrake.setText("Handbrake-Queue");
        jMenuItemHandbrake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHandbrakeActionPerformed(evt);
            }
        });
        jMenuExport.add(jMenuItemHandbrake);

        jMenuFile.add(jMenuExport);
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

    // <editor-fold desc="Custom GUI Contents">
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
}//GEN-LAST:event_jButtonInputDelActionPerformed

private void jButtonInputDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInputDownActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonInputDownActionPerformed

private void jButtonInputBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInputBrowseActionPerformed
    browseInput();
}//GEN-LAST:event_jButtonInputBrowseActionPerformed

private void jButtonOutputBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOutputBrowseActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonOutputBrowseActionPerformed

private void jButtonDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonDeleteAllActionPerformed

private void jButtonRenameAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameAllActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonRenameAllActionPerformed

private void jButtonSaveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveProjectActionPerformed
    saveProject();
}//GEN-LAST:event_jButtonSaveProjectActionPerformed

private void jButtonEncodingHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEncodingHelpActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButtonEncodingHelpActionPerformed

private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
    safeExit();
}//GEN-LAST:event_jMenuItemExitActionPerformed

private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewActionPerformed
    loadDefaults();
}//GEN-LAST:event_jMenuItemNewActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        exportToHandbrake();
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jMenuItemHandbrakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHandbrakeActionPerformed
        exportToHandbrake();
    }//GEN-LAST:event_jMenuItemHandbrakeActionPerformed

    private void jButtonClearTranscodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearTranscodeActionPerformed
        model.clearFilesToTranscode();
    }//GEN-LAST:event_jButtonClearTranscodeActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        safeExit();
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
//        JOptionPane.showMessageDialog(null, aboutText, aboutTitle, JOptionPane.INFORMATION_MESSAGE);
        jFrameAbout.setVisible(true);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new ThreadedExecutor() {

            @Override
            public void execute() {
                updateModelValues();
                model.updateFilesToTranscode();
            }
        }.start();
    }//GEN-LAST:event_jButton1ActionPerformed

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
        loadProject();
    }//GEN-LAST:event_jButtonLoadProjectActionPerformed

    private void jMenuItemOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenProjectActionPerformed
        loadProject();
    }//GEN-LAST:event_jMenuItemOpenProjectActionPerformed

    private void jMenuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAsActionPerformed
        saveProject();
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
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
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
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables declaration - generated by GUI builder">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonAboutClose;
    private javax.swing.JButton jButtonAddFiles;
    private javax.swing.JButton jButtonClearInput;
    private javax.swing.JButton jButtonClearTranscode;
    private javax.swing.JButton jButtonDel;
    private javax.swing.JButton jButtonDeleteAll;
    private javax.swing.JButton jButtonDown;
    private javax.swing.JButton jButtonEncodingHelp;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonExport;
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
    private javax.swing.JButton jButtonSaveProject;
    private javax.swing.JButton jButtonSizeHelp;
    private javax.swing.JButton jButtonUp;
    private javax.swing.JCheckBox jCheckBoxExtension;
    private javax.swing.JCheckBox jCheckBoxPreserveFolders;
    private javax.swing.JCheckBox jCheckBoxRecursive;
    private javax.swing.JCheckBox jCheckBoxSize;
    private javax.swing.JEditorPane jEditorPaneAboutText;
    private javax.swing.JFileChooser jFileChooserExportHandbrake;
    private javax.swing.JFileChooser jFileChooserInput;
    private javax.swing.JFileChooser jFileChooserProjectLoad;
    private javax.swing.JFileChooser jFileChooserProjectSave;
    private javax.swing.JFormattedTextField jFormattedTextFieldSizeMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldSizeMin;
    private javax.swing.JFrame jFrameAbout;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelAboutTitle;
    private javax.swing.JList jListInput;
    private javax.swing.DefaultListModel<String> listModelInput;
    private javax.swing.JList jListTranscode;
    private javax.swing.DefaultListModel<String> listModelTranscode;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuExport;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemHandbrake;
    private javax.swing.JMenuItem jMenuItemHelp;
    private javax.swing.JMenuItem jMenuItemNew;
    private javax.swing.JMenuItem jMenuItemOpenFiles;
    private javax.swing.JMenuItem jMenuItemOpenProject;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JMenuItem jMenuItemSaveAs;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelCleanup;
    private javax.swing.JPanel jPanelEncoding;
    private javax.swing.JPanel jPanelFileView;
    private javax.swing.JPanel jPanelInput;
    private javax.swing.JPanel jPanelOutput;
    private javax.swing.JPanel jPanelSearchPattern;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JRadioButton jRadioButtonDifferentFolder;
    private javax.swing.JRadioButton jRadioButtonSamePlace;
    private javax.swing.JRadioButton jRadioButtonSelectProperties;
    private javax.swing.JRadioButton jRadioButtonSelectRegex;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPaneFileList;
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
    private javax.swing.JTextPane jTextPaneHandbrakeQuery;
    private javax.swing.JToolBar jToolBar;
    private javax.swing.ButtonGroup outputTypeChooser;
    private javax.swing.ButtonGroup searchPatternChooser;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
