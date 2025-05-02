/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.musiccomposer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bernardoleal
 */
public class Track {
    private String instrument;
    private List<String> recordedChords;
    private String name;
    private boolean isMuted;
    
    public Track(String name, String instrument) {
        this.name = name;
        this.instrument = instrument;
        this.recordedChords = new ArrayList<>();
        this.isMuted = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getInstrument() {
        return instrument;
    }
    
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public List<String> getRecordedChords() {
        return new ArrayList<>(recordedChords);
    }
    
    public void addChord(String chord) {
        if (!recordedChords.isEmpty()) {
            recordedChords.add("➡ " + chord);
        } else {
            recordedChords.add(chord);
        }
    }
    
    public void clearChords() {
        recordedChords.clear();
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }
    
    public void toggleMute() {
        this.isMuted = !this.isMuted;
    }
}

