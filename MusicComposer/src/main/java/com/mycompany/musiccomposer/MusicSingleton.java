/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.musiccomposer;
import org.jfugue.player.Player;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.lang.Thread;



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
    
    private MusicSingleton() {
        this.player = new Player();
        this.recordedChords = new ArrayList<>();
        this.isRecording = false;
        this.tempo = 120;
        this.instrument = "Piano";
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
    
    public void playRecordedChords() {
        if (recordedChords.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No chords recorded");
        } else {
            new Thread(() -> player.play("T" + tempo + " I[" + instrument + "] " + String.join(" ", recordedChords))).start(); 
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
    
    
    private void startRecordingThread() {
        recordingThread = new Thread(() -> {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    if (mainPage != null) {
                        mainPage.updateTextArea(String.join("", recordedChords));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        recordingThread.start();
    }
    
    private void stopRecordingThread() {
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
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
}
