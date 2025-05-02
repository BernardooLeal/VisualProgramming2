/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.musiccomposer;

import java.util.List;
import javax.swing.SwingUtilities;
import java.util.Map;



/**
 *
 * @author bernardoleal
 */
public class MainPage extends javax.swing.JFrame {

    private String loggedInUser;
    private Thread recordingThread;
    private MusicSingleton playerSingleton;
    //private JTextArea jTxtAChords;
    
    public MainPage(String username) {
        initComponents();
        this.loggedInUser = username;
        jlblAccountName.setText(username);
        playerSingleton = MusicSingleton.getInstance();
        playerSingleton.setMainPage(this);
        refreshTrackSelector();
        updateTrackDisplay(playerSingleton.getCurrentTrack());
        instrumentSelection(playerSingleton.getInstrument());
        playbackControlsState(MusicSingleton.STATUS_IDLE);
    }
    
    public void updateTrackDisplay(Track track) {
        // Update the text area with current track's chords
        updateTextArea(String.join(" ", track.getRecordedChords()));

        // Update instrument selection
        instrumentSelection(track.getInstrument());

        // Update mute button status
        if (jTBtnMuteTrack != null) {
            jTBtnMuteTrack.setSelected(track.isMuted());
            jTBtnMuteTrack.setText(track.isMuted() ? "Unmute" : "Mute Track");
        }
    }
    
    // Low key chatGPT helped me on setting the combo box
    public void refreshTrackSelector() {
        if (jCmbTrackSelector != null) {
            jCmbTrackSelector.removeAllItems();
            jCmbTrackSelector.addItem("All Tracks"); 
            Map<Integer, Track> tracks = playerSingleton.getAllTracks();
            for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
                jCmbTrackSelector.addItem(entry.getValue().getName() + (entry.getValue().isMuted() ? " (Muted)" : ""));
            }
            // Set selection to current track or "All Tracks" if that was previously selected
            if (jCmbTrackSelector.getSelectedItem() != null && 
                jCmbTrackSelector.getSelectedItem().toString().equals("All Tracks")) {
                jCmbTrackSelector.setSelectedIndex(0);
            } else {
                jCmbTrackSelector.setSelectedIndex(playerSingleton.getCurrentTrackIndex() + 1); // +1 because "All Tracks" is at index 0
            }
        }
    }
    
    public void updateTextArea(String text) {
        if (jTxtAChords != null) {
            jTxtAChords.setText(text);
            jTxtAChords.setCaretPosition(0); // Scroll to top
        }
    }
    
    // In order to call the combo box in the singleton
    public boolean isAllTracksSelected() {
        return jCmbTrackSelector.getSelectedIndex() == 0;
    }
    
    //Used chat to help me with the string append
    public void updateAllTracksDisplay() {
        StringBuilder allTracks = new StringBuilder();
        Map<Integer, Track> tracks = playerSingleton.getAllTracks();

        for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
            Track track = entry.getValue();
            allTracks.append("Track ").append(entry.getKey()).append(": ")
                    .append(track.getName())
                    .append(track.isMuted() ? " (Muted)" : "")
                    .append("\n");
            allTracks.append("Instrument: ").append(track.getInstrument()).append("\n");
            allTracks.append(String.join(" ", track.getRecordedChords())).append("\n\n");
        }

        updateTextArea(allTracks.toString());
    }
    
    public void instrumentSelection(String instrument) {
        switch(instrument) {
            case "Piano":
                jRBtnPiano.setSelected(true);
                break;
            case "Guitar":
                jRBtnGuitar.setSelected(true);
                break;
            case "Violin":
                jRBtnViolin.setSelected(true);
                break;
            case "Flute":
                jRBtnFlute.setSelected(true);
                break;
            case "Trumpet":
                jRBtnTrumpet.setSelected(true);
                break;
            default:
                jRBtnPiano.setSelected(true);
                break;
        }
    }
    
    public void playbackControlsState(String status) {
        SwingUtilities.invokeLater(() -> {
            // I got the idea to use switch from chat tbh. 
            if (jLblPlayback != null) {
                jLblPlayback.setText("Status: " + status);
            }
            
            switch (status) {
                case MusicSingleton.STATUS_PLAYING:
                    jBtnPlay.setEnabled(false);
                    jBtnPause.setEnabled(false);
                    jBtnResume.setEnabled(false);
                    jBtnStop.setEnabled(false);
                    break;
                case MusicSingleton.STATUS_EPLAYING:
                    jBtnPlay.setEnabled(false);
                    jBtnPause.setEnabled(true);
                    jBtnResume.setEnabled(false);
                    jBtnStop.setEnabled(true);
                    break;
                case MusicSingleton.STATUS_PAUSED:
                    jBtnPlay.setEnabled(false);
                    jBtnPause.setEnabled(false);
                    jBtnResume.setEnabled(true);
                    jBtnStop.setEnabled(true);
                    break;
                case MusicSingleton.STATUS_IDLE:
                    jBtnPlay.setEnabled(true);
                    jBtnPause.setEnabled(false);
                    jBtnResume.setEnabled(false);
                    jBtnStop.setEnabled(false);
                    break;
            }
        });
    }
    
    public void highlightCurrentChord(int index) { //I used GPT help for the visual indicator stuff on each chord and the SwingUtilities
        SwingUtilities.invokeLater(() -> {
            try {
                List<String> chords = MusicSingleton.getInstance().getRecordedChords();
                if (index >= 0 && index < chords.size()) {
                    // Clear text
                    jTxtAChords.setText("");
                    
                    // Rebuild text with highlighting
                    StringBuilder displayText = new StringBuilder();
                    for (int i = 0; i < chords.size(); i++) {
                        if (i == index) {
                            // Add visual indicator for current chord
                            displayText.append("▶ ").append(chords.get(i)).append(" ◀");
                        } else {
                            displayText.append(chords.get(i));
                        }
                        
                        // space between chords
                        if (i < chords.size() - 1) {
                            displayText.append(" ");
                        }
                    }
                    
                    jTxtAChords.setText(displayText.toString());
                }
            } catch (Exception e) {
                System.err.println("Error highlighting chord: " + e.getMessage());
            }
        });
    }
    
    public void clearChordHighlight() {
        SwingUtilities.invokeLater(() -> {
            List<String> chords = MusicSingleton.getInstance().getRecordedChords();
            updateTextArea(String.join(" ", chords));
        });
    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jlblAccountName = new javax.swing.JLabel();
        jBtnOut = new javax.swing.JButton();
        jBtnCmaj = new javax.swing.JButton();
        jBtnCmin = new javax.swing.JButton();
        jBtnCdim = new javax.swing.JButton();
        jBtnCaug = new javax.swing.JButton();
        jBtnCsus2 = new javax.swing.JButton();
        jBtnCsus4 = new javax.swing.JButton();
        jTBtnRecord = new javax.swing.JToggleButton();
        jBtnTempo = new javax.swing.JButton();
        jBtnSave = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTxtAChords = new javax.swing.JTextArea();
        jRBtnPiano = new javax.swing.JRadioButton();
        jRBtnGuitar = new javax.swing.JRadioButton();
        jRBtnViolin = new javax.swing.JRadioButton();
        jRBtnFlute = new javax.swing.JRadioButton();
        jRBtnTrumpet = new javax.swing.JRadioButton();
        jBtnImport = new javax.swing.JButton();
        jLblPlayback = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jCmbTrackSelector = new javax.swing.JComboBox<>();
        jBtnAddTrack = new javax.swing.JButton();
        jBtnDeleteTrack = new javax.swing.JButton();
        jBtnRenameTrack = new javax.swing.JButton();
        jTBtnMuteTrack = new javax.swing.JToggleButton();
        jBtnPlay = new javax.swing.JButton();
        jBtnStop = new javax.swing.JButton();
        jBtnResume = new javax.swing.JButton();
        jBtnPause = new javax.swing.JButton();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Hiragino Mincho ProN", 2, 13)); // NOI18N
        jLabel1.setText("Welcome back,");

        jlblAccountName.setFont(new java.awt.Font("Hiragino Mincho ProN", 3, 14)); // NOI18N
        jlblAccountName.setText("username");

        jBtnOut.setText("Log Out");
        jBtnOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnOutActionPerformed(evt);
            }
        });

        jBtnCmaj.setText("Cmaj");
        jBtnCmaj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCmajActionPerformed(evt);
            }
        });

        jBtnCmin.setText("Cmin");
        jBtnCmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCminActionPerformed(evt);
            }
        });

        jBtnCdim.setText("Cdim");
        jBtnCdim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCdimActionPerformed(evt);
            }
        });

        jBtnCaug.setText("Caug");
        jBtnCaug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCaugActionPerformed(evt);
            }
        });

        jBtnCsus2.setText("Csus2");
        jBtnCsus2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCsus2ActionPerformed(evt);
            }
        });

        jBtnCsus4.setText("Csus4");
        jBtnCsus4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCsus4ActionPerformed(evt);
            }
        });

        jTBtnRecord.setText("🎤 Record (ON/OFF)");
        jTBtnRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTBtnRecordActionPerformed(evt);
            }
        });

        jBtnTempo.setText("🎼 Tempo");
        jBtnTempo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTempoActionPerformed(evt);
            }
        });

        jBtnSave.setText("💾 Save");
        jBtnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSaveActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Krungthep", 0, 13)); // NOI18N
        jLabel2.setText("Chord Player");

        jTxtAChords.setColumns(20);
        jTxtAChords.setRows(5);
        jScrollPane1.setViewportView(jTxtAChords);

        buttonGroup1.add(jRBtnPiano);
        jRBtnPiano.setText("Piano");
        jRBtnPiano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnPianoActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRBtnGuitar);
        jRBtnGuitar.setText("Guitar");
        jRBtnGuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnGuitarActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRBtnViolin);
        jRBtnViolin.setText("Violin");
        jRBtnViolin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnViolinActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRBtnFlute);
        jRBtnFlute.setText("Flute");
        jRBtnFlute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnFluteActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRBtnTrumpet);
        jRBtnTrumpet.setText("Trumpet");
        jRBtnTrumpet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnTrumpetActionPerformed(evt);
            }
        });

        jBtnImport.setText("Load");
        jBtnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImportActionPerformed(evt);
            }
        });

        jLblPlayback.setText("Playback Status");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Track Controls"));

        jCmbTrackSelector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jCmbTrackSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCmbTrackSelectorActionPerformed(evt);
            }
        });

        jBtnAddTrack.setText("Add Track");
        jBtnAddTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAddTrackActionPerformed(evt);
            }
        });

        jBtnDeleteTrack.setText("Delete Track");
        jBtnDeleteTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDeleteTrackActionPerformed(evt);
            }
        });

        jBtnRenameTrack.setText("Rename Track");
        jBtnRenameTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRenameTrackActionPerformed(evt);
            }
        });

        jTBtnMuteTrack.setText("Mute Track");
        jTBtnMuteTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTBtnMuteTrackActionPerformed(evt);
            }
        });

        jBtnPlay.setText("▶ Play");
        jBtnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnPlayActionPerformed(evt);
            }
        });

        jBtnStop.setText("⏹ Stop");
        jBtnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnStopActionPerformed(evt);
            }
        });

        jBtnResume.setText("⏯ Resume");
        jBtnResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResumeActionPerformed(evt);
            }
        });

        jBtnPause.setText("⏸ Pause");
        jBtnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnPauseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCmbTrackSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnPlay, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnAddTrack, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jBtnStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnDeleteTrack))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jBtnResume, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnRenameTrack))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jBtnPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTBtnMuteTrack))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnAddTrack)
                    .addComponent(jBtnDeleteTrack)
                    .addComponent(jBtnRenameTrack)
                    .addComponent(jTBtnMuteTrack))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnPlay)
                    .addComponent(jBtnStop)
                    .addComponent(jBtnResume)
                    .addComponent(jBtnPause))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCmbTrackSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGap(153, 153, 153)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jBtnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTBtnRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jBtnCmaj)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnCmin)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnCdim)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnCaug)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnCsus2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnCsus4)
                                .addGap(0, 6, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jlblAccountName, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jBtnOut))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(181, 181, 181)
                                .addComponent(jLblPlayback)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jRBtnPiano)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRBtnGuitar)
                                .addGap(12, 12, 12)
                                .addComponent(jRBtnViolin)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRBtnFlute)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRBtnTrumpet)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jBtnTempo)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jlblAccountName)
                    .addComponent(jBtnOut))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnCmaj)
                    .addComponent(jBtnCmin)
                    .addComponent(jBtnCdim)
                    .addComponent(jBtnCaug)
                    .addComponent(jBtnCsus2)
                    .addComponent(jBtnCsus4))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnSave)
                    .addComponent(jBtnImport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTBtnRecord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLblPlayback)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnTempo)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRBtnPiano)
                        .addComponent(jRBtnGuitar)
                        .addComponent(jRBtnViolin)
                        .addComponent(jRBtnFlute)
                        .addComponent(jRBtnTrumpet)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnOutActionPerformed
        // TODO add your handling code here:
        new LogInMenu().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jBtnOutActionPerformed

    private void jBtnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnPlayActionPerformed
       if (isAllTracksSelected()) {
           playerSingleton.playMultiRecordedChords();
       } else {
           playerSingleton.playRecordedChords();
       }
    }//GEN-LAST:event_jBtnPlayActionPerformed

    private void jBtnCmajActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCmajActionPerformed
        playerSingleton.playChord("Cmaj");
    }//GEN-LAST:event_jBtnCmajActionPerformed

    private void jBtnCminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCminActionPerformed
        playerSingleton.playChord("Cmin");
    }//GEN-LAST:event_jBtnCminActionPerformed

    private void jBtnCdimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCdimActionPerformed
        playerSingleton.playChord("Cdim");
    }//GEN-LAST:event_jBtnCdimActionPerformed

    private void jBtnCaugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCaugActionPerformed
        playerSingleton.playChord("Caug");
    }//GEN-LAST:event_jBtnCaugActionPerformed

    private void jBtnCsus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCsus2ActionPerformed
        playerSingleton.playChord("Csus2");
    }//GEN-LAST:event_jBtnCsus2ActionPerformed

    private void jBtnCsus4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCsus4ActionPerformed
       playerSingleton.playChord("Csus4");
    }//GEN-LAST:event_jBtnCsus4ActionPerformed

    private void jTBtnRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTBtnRecordActionPerformed
       playerSingleton.startRecording();
       if (playerSingleton.isRecording()) {
           jTBtnRecord.setText("🛑 Stop Recording");
       } else {
           jTBtnRecord.setText("🎤 Record (ON/OFF)");
       }
    }//GEN-LAST:event_jTBtnRecordActionPerformed

    private void jBtnTempoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTempoActionPerformed
        playerSingleton.setTempo(playerSingleton.getTempo());
    }//GEN-LAST:event_jBtnTempoActionPerformed

    private void jRBtnPianoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnPianoActionPerformed
        playerSingleton.setInstrument("Piano");
    }//GEN-LAST:event_jRBtnPianoActionPerformed

    private void jRBtnGuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnGuitarActionPerformed
        playerSingleton.setInstrument("Guitar");
    }//GEN-LAST:event_jRBtnGuitarActionPerformed

    private void jRBtnViolinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnViolinActionPerformed
       playerSingleton.setInstrument("Violin");
    }//GEN-LAST:event_jRBtnViolinActionPerformed

    private void jRBtnFluteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnFluteActionPerformed
        playerSingleton.setInstrument("Flute");
    }//GEN-LAST:event_jRBtnFluteActionPerformed

    private void jRBtnTrumpetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnTrumpetActionPerformed
        playerSingleton.setInstrument("Trumpet");
    }//GEN-LAST:event_jRBtnTrumpetActionPerformed

    private void jBtnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSaveActionPerformed
       String composition = jTxtAChords.getText();
       playerSingleton.saveComposition(composition);
        
    }//GEN-LAST:event_jBtnSaveActionPerformed

    private void jBtnResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResumeActionPerformed
        playerSingleton.resumePlayback();
        playbackControlsState(MusicSingleton.STATUS_PLAYING);
    }//GEN-LAST:event_jBtnResumeActionPerformed

    private void jBtnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnPauseActionPerformed
        playerSingleton.pausePlayback();
        playbackControlsState(MusicSingleton.STATUS_PAUSED);
    }//GEN-LAST:event_jBtnPauseActionPerformed

    private void jBtnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnStopActionPerformed
        playerSingleton.stopPlayback();
        playbackControlsState(MusicSingleton.STATUS_IDLE);
        clearChordHighlight();
    }//GEN-LAST:event_jBtnStopActionPerformed

    private void jBtnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnImportActionPerformed
        // TODO add your handling code here:
        playerSingleton.loadComposition();
    }//GEN-LAST:event_jBtnImportActionPerformed

    private void jCmbTrackSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCmbTrackSelectorActionPerformed
        // TODO add your handling code here:
        int selectedIndex = jCmbTrackSelector.getSelectedIndex();
        if (selectedIndex == 0) {
            // "All Tracks" option selected
            updateAllTracksDisplay();
            jTBtnMuteTrack.setEnabled(false);
            jBtnRenameTrack.setEnabled(false);
            jBtnDeleteTrack.setEnabled(false);
            jTBtnRecord.setEnabled(false);
        } else {
            // Regular track selected
            playerSingleton.switchTrack(selectedIndex - 1); // -1 because "All Tracks" is at index 0
            updateTrackDisplay(playerSingleton.getCurrentTrack());
            jTBtnMuteTrack.setEnabled(true);
            jBtnRenameTrack.setEnabled(true);
            jBtnDeleteTrack.setEnabled(true);
            jTBtnRecord.setEnabled(true);
        }
    }//GEN-LAST:event_jCmbTrackSelectorActionPerformed

    private void jBtnAddTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAddTrackActionPerformed
        // TODO add your handling code here:
        String name = javax.swing.JOptionPane.showInputDialog(MainPage.this, "Enter track name:", "New Track", javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            String instrument = playerSingleton.getInstrument();
            int newIndex = playerSingleton.createTrack(name, instrument);
            playerSingleton.switchTrack(newIndex);
            refreshTrackSelector();
            updateTrackDisplay(playerSingleton.getCurrentTrack());
        }
    }//GEN-LAST:event_jBtnAddTrackActionPerformed

    private void jBtnDeleteTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDeleteTrackActionPerformed
        // TODO add your handling code here:
        int currentIndex = playerSingleton.getCurrentTrackIndex();
        playerSingleton.deleteTrack(currentIndex);
        refreshTrackSelector();
        updateTrackDisplay(playerSingleton.getCurrentTrack());
    }//GEN-LAST:event_jBtnDeleteTrackActionPerformed

    private void jBtnRenameTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRenameTrackActionPerformed
        // TODO add your handling code here:
        int currentIndex = playerSingleton.getCurrentTrackIndex();
        Track currentTrack = playerSingleton.getTrack(currentIndex);
        String newName = javax.swing.JOptionPane.showInputDialog(MainPage.this, 
                "Enter new track name:", 
                currentTrack.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            playerSingleton.renameTrack(currentIndex, newName);
            refreshTrackSelector();
        }
    }//GEN-LAST:event_jBtnRenameTrackActionPerformed

    private void jTBtnMuteTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTBtnMuteTrackActionPerformed
        // TODO add your handling code here:
        int currentIndex = playerSingleton.getCurrentTrackIndex();
        playerSingleton.muteTrack(currentIndex);
        jTBtnMuteTrack.setText(playerSingleton.getTrack(currentIndex).isMuted() ? "Unmute" : "Mute Track");
        refreshTrackSelector();
    }//GEN-LAST:event_jTBtnMuteTrackActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainPage("test").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jBtnAddTrack;
    private javax.swing.JButton jBtnCaug;
    private javax.swing.JButton jBtnCdim;
    private javax.swing.JButton jBtnCmaj;
    private javax.swing.JButton jBtnCmin;
    private javax.swing.JButton jBtnCsus2;
    private javax.swing.JButton jBtnCsus4;
    private javax.swing.JButton jBtnDeleteTrack;
    private javax.swing.JButton jBtnImport;
    private javax.swing.JButton jBtnOut;
    private javax.swing.JButton jBtnPause;
    private javax.swing.JButton jBtnPlay;
    private javax.swing.JButton jBtnRenameTrack;
    private javax.swing.JButton jBtnResume;
    private javax.swing.JButton jBtnSave;
    private javax.swing.JButton jBtnStop;
    private javax.swing.JButton jBtnTempo;
    private javax.swing.JComboBox<String> jCmbTrackSelector;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLblPlayback;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRBtnFlute;
    private javax.swing.JRadioButton jRBtnGuitar;
    private javax.swing.JRadioButton jRBtnPiano;
    private javax.swing.JRadioButton jRBtnTrumpet;
    private javax.swing.JRadioButton jRBtnViolin;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToggleButton jTBtnMuteTrack;
    private javax.swing.JToggleButton jTBtnRecord;
    private javax.swing.JTextArea jTxtAChords;
    private javax.swing.JLabel jlblAccountName;
    // End of variables declaration//GEN-END:variables
}
