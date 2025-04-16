/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.musiccomposer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.jfugue.player.Player;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.lang.Thread;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;



/**
 *
 * @author bernardoleal
 */
public class MusicSingleton {
    
    private static MusicSingleton instance;
    private Player player;
    private List<String> recordedChords;
    private boolean isRecording;
    private int tempo;
    private String instrument;
    private MainPage mainPage;
    private Thread recordingThread;
    private Thread playbackThread;
    private boolean isPaused;
    private boolean isStopped;
    private int currentChordIndex;
    //Used chat to remember how to use switch names format
    public static final String STATUS_PLAYING = "Playing";
    public static final String STATUS_PAUSED = "Paused";
    public static final String STATUS_IDLE = "Idle";
    
    
    private MusicSingleton() {
        this.player = new Player();
        this.recordedChords = new ArrayList<>();
        this.isRecording = false;
        this.tempo = 120;
        this.instrument = "Piano";
        this.currentChordIndex = -1;
        
    }

     public static MusicSingleton getInstance() {
        if (instance == null) {
            synchronized (MusicSingleton.class) {
                if (instance == null) {
                    instance = new MusicSingleton();
                }
            }
        }
        return instance;
    }
     
    public void setMainPage(MainPage mainPage) {
        this.mainPage = mainPage;
    }
    
    public void playChord(String chord) {
        new Thread(() -> player.play("T" + tempo + " I[" + instrument + "] " + chord)).start(); //crazy API syntax 
        if (isRecording) {
            if (!recordedChords.isEmpty()) {
                recordedChords.add("➡ " + chord);
            } else {
                recordedChords.add(chord);
            }
        }
    }
    
//    public void playRecordedChords() {
//        if (recordedChords.isEmpty()) {
//            JOptionPane.showMessageDialog(null, "No chords recorded");
//        } else {
//            new Thread(() -> player.play("T" + tempo + " I[" + instrument + "] " + String.join(" ", recordedChords))).start(); 
//        }
//    }
    
    public void playRecordedChords() {
        if (recordedChords.isEmpty()) {
        JOptionPane.showMessageDialog(mainPage, "No chords recorded");
        return;
    }

        if (playbackThread != null && playbackThread.isAlive()) {
            JOptionPane.showMessageDialog(mainPage, "Playback is already running.");
            return;
        }

        isPaused = false;
        isStopped = false;
        
        mainPage.playbackControlsState(STATUS_PLAYING);

        playbackThread = new Thread(() -> {
            try {
                for (int i = 0; i < recordedChords.size(); i++) {
                    String chord = recordedChords.get(i);
                    
                    synchronized (this) {
                        while (isPaused) {
                            wait();
                        }
                        if (isStopped) {
                            break;
                        }
                    }

                    // Update current chord index
                    currentChordIndex = i;
                    
                    // highlight current chord
                    mainPage.highlightCurrentChord(i);
                    
                    
                    player.play("T" + tempo + " I[" + instrument + "] " + chord);
                    Thread.sleep(10); //the time spent in each chord
                }
                
                mainPage.playbackControlsState(STATUS_IDLE);
                mainPage.clearChordHighlight();
                currentChordIndex = -1;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                
                if (!isPaused) {
                    mainPage.playbackControlsState(STATUS_IDLE);
                    mainPage.clearChordHighlight();
                }
            }
        });
        playbackThread.start();
    }
    
    public synchronized void pausePlayback() {
        if (playbackThread != null && playbackThread.isAlive()) {
            isPaused = true;
            mainPage.playbackControlsState(STATUS_PAUSED);
            
        }
    }

    public synchronized void resumePlayback() {
        if (isPaused) {
            isPaused = false;
            mainPage.playbackControlsState(STATUS_PLAYING);
            notifyAll();
        }
    }

    public synchronized void stopPlayback() {
        if (playbackThread != null && playbackThread.isAlive()) {
            isStopped = true;
            isPaused = false;
            mainPage.playbackControlsState(STATUS_IDLE);
            mainPage.clearChordHighlight();
            currentChordIndex = -1;
            playbackThread.interrupt();
            notifyAll();
        }
    }
    
    public void startRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            recordedChords.clear();
            startRecordingThread();
            JOptionPane.showMessageDialog(mainPage,"🎤 Recording started!");
        } else {
            stopRecordingThread();
            JOptionPane.showMessageDialog(mainPage,"🛑 Recording stopped!");
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    
    private void startRecordingThread() {
        recordingThread = new Thread(() -> {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mainPage.updateTextArea(String.join("", recordedChords));
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        recordingThread.start();
    }
    
    public List<String> getRecordedChords() {
        return new ArrayList<>(recordedChords); 
    }
    
    private void stopRecordingThread() {
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
    }
    
    public Thread getPlaybackThread() {
        return playbackThread;
    }
    
    public boolean isPlaybackActive() {
        return playbackThread != null && playbackThread.isAlive();
    }
    
    public boolean isPlaybackPaused() {
        return isPaused;
    }
    
    public int getCurrentChordIndex() {
        return currentChordIndex;
    }
    
    public void setTempo(int tempo) {
        String newTempo = JOptionPane.showInputDialog("Enter new tempo:");
        try {
            tempo = Integer.parseInt(newTempo);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainPage,"Invalid tempo input!");
        }
    }
    
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getInstrument() {
        return instrument;
    }

    public int getTempo() {
        return tempo;
    }
    
    public boolean saveComposition(String composition) {
        if (composition == null || composition.isEmpty()) {
            JOptionPane.showMessageDialog(mainPage, "No composition to save.");
            return false;
        }
        
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + "/Desktop/MusicCompositions"; 

        File directory = new File(desktopPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        JFileChooser fileChooser = new JFileChooser(desktopPath);
        fileChooser.setDialogTitle("Save Composition");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        
        if (fileChooser.showSaveDialog(mainPage) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // error fix checking, I got this error for some reason :0
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("# Music Composition\n");
                writer.write("# Instrument: " + instrument + "\n");
                writer.write("# Tempo: " + tempo + "\n\n");
                writer.write(composition);
                
                JOptionPane.showMessageDialog(mainPage, "Composition saved successfully to " + file.getName());
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainPage, "Error saving composition: " + e.getMessage(), 
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return false;
    }
    
    
}
